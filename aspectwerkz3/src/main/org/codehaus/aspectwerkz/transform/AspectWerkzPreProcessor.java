/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.codehaus.aspectwerkz.util.Util;
import org.codehaus.aspectwerkz.expression.SubtypePatternType;
import org.codehaus.aspectwerkz.expression.regexp.Pattern;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;
import org.codehaus.aspectwerkz.hook.ClassPreProcessor;
import org.codehaus.aspectwerkz.hook.RuntimeClassProcessor;
import org.codehaus.aspectwerkz.transform.delegation.DelegationWeavingStrategy;
import org.codehaus.aspectwerkz.transform.inlining.InliningWeavingStrategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * AspectWerkzPreProcessor is the entry point of the AspectWerkz layer 2. <p/>It implements the ClassPreProcessor
 * interface defined in layer 1. <p/>Available options are:
 * <ul>
 * <li><code>-Daspectwerkz.transform.verbose=yes</code> turns on verbose mode: print on stdout all non filtered class
 * names and which transformation are applied</li>
 * <li><code>-Daspectwerkz.transform.dump=org.myapp.*</code> dumps transformed class matching pattern <i>org.myapp.*
 * </i>(even unmodified ones) in <i>./_dump </i> directory (relative to where applications starts). The syntax
 * <code>-Daspectwerkz.transform.dump=*</code> matchs all classes. The pattern language is the same as pointcut
 * pattern language.</li>
 * <li>else <code>-Daspectwerkz.transform.dump=org.myapp.*,before</code> dumps class before and after the
 * transformation whose name starts with <i>org.myapp. </i>(even unmodified ones) in <i>./_dump/before </i> and
 * <i>./_dump/after </i> directories (relative to where application starts)</li>
 * <li><code>-Daspectwerkz.transform.filter=no</code> (or false) disables filtering of
 * <code>org.codehaus.aspectwerkz</code> and related classes (trove, dom4j etc.). This should only be used in offline
 * mode where weaving of those classes is needed. Setting this option in online mode will lead to
 * <code>ClassCircularityError</code>.</li>
 * </ul>
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AspectWerkzPreProcessor implements ClassPreProcessor, RuntimeClassProcessor {

    private final static String AW_WEAVING_STRATEGY = "aspectwerkz.weaving.strategy";

    private final static String AW_TRANSFORM_FILTER = "aspectwerkz.transform.filter";

    private final static String AW_TRANSFORM_VERBOSE = "aspectwerkz.transform.verbose";

    private final static String AW_TRANSFORM_DUMP = "aspectwerkz.transform.dump";

    private final static TypePattern DUMP_PATTERN;

    private final static boolean NOFILTER;

    private final static boolean DUMP_BEFORE;

    private final static boolean DUMP_AFTER;

    public final static boolean VERBOSE;

    private final static int WEAVING_STRATEGY;

    static {
        // define the weaving strategy
        String weavingStrategy = System.getProperty(AW_WEAVING_STRATEGY, "delegation").trim();
        if (weavingStrategy.equalsIgnoreCase("inlining")) {
            WEAVING_STRATEGY = WeavingStrategy.INLINING;
        } else if (weavingStrategy.equalsIgnoreCase("delegation")) {
            WEAVING_STRATEGY = WeavingStrategy.DELEGATION;
        } else {
            throw new RuntimeException("unknown weaving strategy specified: " + weavingStrategy);
        }
        // define the tracing and dump options
        String verbose = System.getProperty(AW_TRANSFORM_VERBOSE, null);
        VERBOSE = "yes".equalsIgnoreCase(verbose) || "true".equalsIgnoreCase(verbose);
        String filter = System.getProperty(AW_TRANSFORM_FILTER, null);
        NOFILTER = "no".equalsIgnoreCase(filter) || "false".equalsIgnoreCase(filter);
        String dumpPattern = System.getProperty(AW_TRANSFORM_DUMP, null);
        if (dumpPattern == null) {
            DUMP_BEFORE = false;
            DUMP_AFTER = false;
            DUMP_PATTERN = null;
        } else {
            dumpPattern = dumpPattern.trim();
            DUMP_AFTER = true;
            DUMP_BEFORE = dumpPattern.indexOf(",before") > 0;
            if (DUMP_BEFORE) {
                DUMP_PATTERN = Pattern.compileTypePattern(
                    dumpPattern.substring(0, dumpPattern.indexOf(',')),
                    SubtypePatternType.NOT_HIERARCHICAL);
            } else {
                DUMP_PATTERN = Pattern.compileTypePattern(dumpPattern, SubtypePatternType.NOT_HIERARCHICAL);
            }
        }
    }

    /**
     * Bytecode cache for prepared class and runtime weaving.
     * 
     * @TODO: allow for other cache implementations (file, jms, clustered, jcache, JNDI, javagroups etc.)
     */
    private static Map s_classByteCache = new HashMap();

    /**
     * Marks the pre-processor as initialized.
     */
    private boolean m_initialized = false;

    /**
     * Pre processor weaving strategy.
     */
    private WeavingStrategy m_weavingStrategy;

    /**
     * Initializes the transformer stack.
     * 
     * @param params not used
     */
    public void initialize(final Hashtable params) {
        switch (WEAVING_STRATEGY) {
            case WeavingStrategy.DELEGATION:
                m_weavingStrategy = new DelegationWeavingStrategy();
                break;

            case WeavingStrategy.INLINING:
                m_weavingStrategy = new InliningWeavingStrategy();
                break;
        }
        m_weavingStrategy.initialize(params);
        m_initialized = true;
    }

    /**
     * Transform bytecode according to the transformer stack
     * 
     * @param name class name
     * @param bytecode bytecode to transform
     * @param loader classloader loading the class
     * @return modified (or not) bytecode
     */
    public byte[] preProcess(final String name, final byte[] bytecode, final ClassLoader loader) {

        // filter out ExtClassLoader and BootClassLoader
        if (!NOFILTER) {
            if ((loader == null) || (loader.getParent() == null)) {
                return bytecode;
            }
        }
        // needed for JRockit (as well as all in all TFs)
        final String className = name.replace('/', '.');

        if (filter(className) || !m_initialized) {
            return bytecode;
        }
        if (VERBOSE) {
            log(Util.classLoaderToString(loader) + ':' + className + '[' + Thread.currentThread().getName() + ']');
        }
        return _preProcess(className, bytecode, loader);
    }

    public byte[] _preProcess(final String name, final byte[] bytecode, final ClassLoader loader) {

        // needed for JRockit (as well as all in all TFs)
        final String className = name.replace('/', '.');

        final Context context;
        try {
            // create a new transformation context
            context = m_weavingStrategy.newContext(name, bytecode, loader);
        } catch (Exception e) {
            log("failed " + className);
            e.printStackTrace();
            return bytecode;
        }

        // dump before (not compliant with multiple CL weaving same class differently, since based
        // on class FQN name)
        dumpBefore(className, context);

        // do the transformation
        m_weavingStrategy.transform(className, context);

        // dump after as required
        dumpAfter(className, context);

        // handle the prepared Class cache for further runtime weaving
        if (context.isPrepared()) {
            ClassCacheTuple key = new ClassCacheTuple(loader, className);
            log("cache prepared " + className);
            s_classByteCache.put(key, new ByteArray(context.getCurrentBytecode()));
        }

        // return the transformed bytecode
        return context.getCurrentBytecode();
    }

    /**
     * Runtime weaving of given Class according to the actual definition
     * 
     * @param klazz
     * @return new bytes for Class representation
     * @throws Throwable
     */
    public byte[] preProcessActivate(final Class klazz) throws Throwable {
        String className = klazz.getName();

        // fetch class from prepared class cache
        ClassCacheTuple key = new ClassCacheTuple(klazz);
        ByteArray currentBytesArray = (ByteArray) s_classByteCache.get(key);
        if (currentBytesArray == null) {
            throw new RuntimeException("can not find cached class in cache for prepared classes: " + className);
        }

        // flush class info repository cache so that new weaving is aware of wrapper method
        // existence

        // FIXME implement and move method
        //        JavassistClassInfoRepository.removeClassInfoFromAllClassLoaders(klazz.getName());

        // transform as if multi weaving
        byte[] newBytes = preProcess(klazz.getName(), currentBytesArray.getBytes(), klazz.getClassLoader());

        // update cache
        s_classByteCache.put(key, new ByteArray(newBytes));
        return newBytes;
    }

    /**
     * Logs a message.
     * 
     * @param msg the message to log
     */
    public static void log(final String msg) {
        if (VERBOSE) {
            System.out.println(msg);
        }
    }

    /**
     * Excludes instrumentation for the class used during the instrumentation
     * 
     * @param klass the AspectWerkz class
     */
    private static boolean filter(final String klass) {
        return (klass == null)
            || klass.startsWith("org.codehaus.aspectwerkz.")
            || klass.startsWith("javassist.")
            || klass.startsWith("org.objectweb.asm.")
            || klass.startsWith("com.karneim.")
            || klass.startsWith("com.bluecast.")
            || klass.startsWith("gnu.trove.")
            || klass.startsWith("org.dom4j.")
            || klass.startsWith("org.xml.sax.")
            || klass.startsWith("javax.xml.parsers.")
            || klass.startsWith("sun.reflect.Generated")// issue on J2SE 5 reflection - AW-245

        // TODO: why have we had jMunit classes filtered out, they are not part of AW core, can't be filtered out since
        // users want to advise on those
            //|| klass.startsWith("junit.")
        ;
    }

    /**
     * Dumps class before weaving.
     * 
     * @param className
     * @param context
     */
    public static void dumpBefore(final String className, final Context context) {
        if (DUMP_BEFORE) {
            if (DUMP_PATTERN.matches(className)) {
                context.dump("_dump/before/");
            }
        }
    }

    /**
     * Dumps class after weaving.
     * 
     * @param className
     * @param context
     */
    public static void dumpAfter(final String className, final Context context) {
        if (DUMP_AFTER) {
            if (DUMP_PATTERN.matches(className)) {
                context.dump("_dump/" + (DUMP_BEFORE ? "after/" : ""));
            }
        }
    }

    /**
     * Always dumps class.
     * 
     * @param context
     */
    public static void dumpForce(final Context context) {
        context.dump("_dump/force/");
    }

    /**
     * Returns the caching tuples.
     * 
     * @return
     */
    public Collection getClassCacheTuples() {
        return s_classByteCache.keySet();
    }
}
/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.codehaus.aspectwerkz.hook.ClassPreProcessor;
import org.codehaus.aspectwerkz.hook.RuntimeClassProcessor;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;

/**
 * AspectWerkzPreProcessor is the entry point of the AspectWerkz layer 2.
 * <p/>
 * It implements the ClassPreProcessor interface defined in layer 1.
 * <p/>
 * Available options are: <ul> <li><code>-Daspectwerkz.transform.verbose=yes</code> turns on verbose mode: print on
 * stdout all non filtered class names and which transformation are applied</li> <li><code>-Daspectwerkz.transform.dump=org.myapp.*</code>
 * dumps transformed class matching pattern <i>org.myapp.*</i>(even unmodified ones) in <i>./_dump</i> directory
 * (relative to where applications starts). The syntax <code>-Daspectwerkz.transform.dump=*</code> matchs all classes.
 * The pattern language is the same as pointcut pattern language.</li> <li>else <code>-Daspectwerkz.transform.dump=org.myapp.*,before</code>
 * dumps class before and after the transformation whose name starts with <i>org.myapp.</i>(even unmodified ones) in
 * <i>./_dump/before</i> and <i>./_dump/after</i> directories (relative to where application starts)</li>
 * <li><code>-Daspectwerkz.transform.filter=no</code> (or false) disables filtering of
 * <code>org.codehaus.aspectwerkz</code> and related classes (trove, dom4j etc.). This should only be used in offline
 * mode where weaving of those classes is needed. Setting this option in online mode will lead to
 * <code>ClassCircularityError</code>.</li> </ul>
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @TODO: dump before/after broken on Javassist due to frozen status
 */
public class AspectWerkzPreProcessor implements ClassPreProcessor, RuntimeClassProcessor {

    private final static String AW_TRANSFORM_FILTER = "aspectwerkz.transform.filter";
    private final static String AW_TRANSFORM_VERBOSE = "aspectwerkz.transform.verbose";
    private final static String AW_TRANSFORM_DUMP = "aspectwerkz.transform.dump";
    private final static ClassPattern DUMP_PATTERN;
    private final static boolean NOFILTER;
    private final static boolean DUMP_BEFORE;
    private final static boolean DUMP_AFTER;
    private final static boolean VERBOSE;

    static {
        // check for verbose mode
        String verbose = System.getProperty(AW_TRANSFORM_VERBOSE, null);
        VERBOSE = "yes".equalsIgnoreCase(verbose) || "true".equalsIgnoreCase(verbose);

        // check for filter disabled
        String filter = System.getProperty(AW_TRANSFORM_FILTER, null);
        NOFILTER = "no".equalsIgnoreCase(filter) || "false".equalsIgnoreCase(filter);

        // check for dump configuration
        String dumpPattern = System.getProperty(AW_TRANSFORM_DUMP, null);
        if (dumpPattern == null) {
            DUMP_BEFORE = false;
            DUMP_AFTER = false;
            DUMP_PATTERN = null;
        }
        else {
            DUMP_AFTER = true;
            DUMP_BEFORE = dumpPattern.indexOf(",before") > 0;
            if (DUMP_BEFORE) {
                DUMP_PATTERN = Pattern.compileClassPattern(dumpPattern.substring(0, dumpPattern.indexOf(',')));
            }
            else {
                DUMP_PATTERN = Pattern.compileClassPattern(dumpPattern);
            }
        }
    }

    /**
     * Bytecode cache.
     */
    private static Map m_classByteCache = new HashMap();

    /**
     * The transformation m_stack
     */
    private List m_stack;

    /**
     * The transformer to add serial ver uid Out of the transformation stack to be applied only if class is weaved
     */
    private Transformer m_addSerialVerUidTransformer;

    /**
     * Marks the pre-processor as initialized.
     */
    private boolean m_initialized = false;

    /**
     * The mixin meta-data repositories, each repository is mapped to its class loader.
     */
    private Map m_metaDataRepository;

    /**
     * The XML definition repositories, each definition hierarchy is mapped to its class loader.
     */
    private Map m_definitionRepository;

    /**
     * Initializes the transformer stack.
     *
     * @param params not used
     */
    public void initialize(final Hashtable params) {
        m_metaDataRepository = new WeakHashMap();
        m_definitionRepository = new WeakHashMap();

        m_addSerialVerUidTransformer = new AddSerialVersionUidTransformer();

        // NOTE: order is important
        m_stack = new ArrayList();
        m_stack.add(new PrepareAdvisedClassTransformer());
        m_stack.add(new FieldSetGetTransformer());
        m_stack.add(new MethodCallTransformer());
        m_stack.add(new ConstructorCallTransformer());
        m_stack.add(new MethodExecutionTransformer());
        m_stack.add(new ConstructorExecutionTransformer());
        m_stack.add(new HandlerTransformer());
        m_stack.add(new AddInterfaceTransformer());
        m_stack.add(new AddImplementationTransformer());

//        m_stack.add(new PrepareTransformer());
//        m_stack.add(new AddMetaDataTransformer());
//        m_stack.add(new AddUuidTransformer());

        m_initialized = true;
    }

    /**
     * Transform bytecode going thru the interface transformation first.
     *
     * @param className class name
     * @param bytecode  bytecode to transform
     * @param loader    classloader loading the class
     * @return modified (or not) bytecode
     */
    public byte[] preProcess(final String className, final byte[] bytecode, final ClassLoader loader) {
        if (!m_initialized || (filter(className) && !NOFILTER)) {
            return bytecode;
        }
        if (VERBOSE) {
            log(loader.toString() + ':' + className + '[' + Thread.currentThread().getName() + ']');
        }

        // prepare BCEL ClassGen
        Klass klass = null;
        try {
            klass = new Klass(className, bytecode, loader);
        }
        catch (Exception e) {
            log("failed " + className);
            e.printStackTrace();
            return bytecode;
        }

        // dump before (not compliant with multiple CL weaving same class differently,
        // since based on class FQN name)
        if (DUMP_BEFORE) {
            if (DUMP_PATTERN.matches(className)) {
                try {
                    //TODO: dump before make CtClass frozen in Javassist
                    klass.getCtClass().getClassPool().writeFile(className, "_dump/before/");
                }
                catch (Exception e) {
                    log("failed to dump " + className);
                    e.printStackTrace();
                }
            }
        }

        // create a new transformation context
        final Context context = new Context(loader);
        context.setMetaDataRepository(m_metaDataRepository);

        for (Iterator it = m_stack.iterator(); it.hasNext();) {
            Object transformer = it.next();

            // if VERBOSE keep a copy of initial bytecode before transfo
            byte[] bytecodeBeforeLocalTransformation = null;
            if (VERBOSE) {
                bytecodeBeforeLocalTransformation = new byte[klass.getBytecode().length];
                System.arraycopy(klass.getBytecode(), 0,
                                 bytecodeBeforeLocalTransformation, 0,
                                 klass.getBytecode().length);
            }

            if (transformer instanceof Transformer) {
                Transformer tf = (Transformer)transformer;
                try {
                    tf.transform(context, klass);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                // if VERBOSE confirm modification
                if (VERBOSE && !java.util.Arrays.equals(klass.getBytecode(), bytecodeBeforeLocalTransformation)) {
                    log(className + " <- " + transformer.getClass().getName());
                }
            }
        }

        // handle the serial ver uid only if class was advised
        if (context.isAdvised()) {
            try {
                m_addSerialVerUidTransformer.transform(context, klass);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // handle the prepared Class cache
        if (context.isPrepared()) {
            ClassCacheTuple key = new ClassCacheTuple(loader, className);
            log("cache prepared " + className);
            m_classByteCache.put(key, new ByteArray(klass.getBytecode()));
        }

        // dump after (not compliant with multiple CL weaving same class differently,
        // since based on class FQN name)
        if (DUMP_AFTER) {
            if (DUMP_PATTERN.matches(className)) {
                try {
                    klass.getCtClass().getClassPool().writeFile(className, "_dump/" + (DUMP_BEFORE ? "after/" : ""));
                }
                catch (Exception e) {
                    log("failed to dump " + className);
                    e.printStackTrace();
                }
            }
        }
        return klass.getBytecode();
    }

    /**
     * Logs a message.
     *
     * @param msg the message to log
     */
    public static void log(final String msg) {
        if (VERBOSE) System.out.println(msg);
    }

    /**
     * Excludes instrumentation for the class used during the instrumentation
     *
     * @param klass the AspectWerkz class
     */
    private static boolean filter(final String klass) {
        return klass.startsWith("org.codehaus.aspectwerkz.")
                || klass.startsWith("javassist.")
                || klass.startsWith("com.karneim.")
                || klass.startsWith("org.apache.bcel.")
                || klass.startsWith("gnu.trove.")
                || klass.startsWith("org.dom4j.")
                || klass.startsWith("org.xml.sax.")
                || klass.startsWith("javax.xml.parsers.");
    }

//    public static byte[] preProcessActivateS(final Class klazz) throws Throwable {
//        return SELF.preProcessActivate(klazz);
//    }

    /**
     * TODO runtime weaving
     *
     * @param klazz
     * @return
     * @throws Throwable
     */
    public byte[] preProcessActivate(final Class klazz) throws Throwable {
        String className = klazz.getName();
        ClassLoader loader = klazz.getClassLoader();
        ClassCacheTuple key = new ClassCacheTuple(klazz);
        ByteArray bytesO = (ByteArray)m_classByteCache.get(key);
        if (bytesO == null) {
            log("CANNOT FIND CACHED " + className);
            throw new RuntimeException("CANNOT FIND CACHED " + className);
        }

        // create a new transformation context
        final Context context = new Context(loader);
        context.setMetaDataRepository(m_metaDataRepository);
        //byte[] bytecode = context.getClassPool().get(className).toBytecode();
        byte[] bytecode = bytesO.getBytes();

        if (!m_initialized || (filter(className) && !NOFILTER)) {
            return bytecode;
        }

        if (VERBOSE) {
            log(loader.toString() + ':' + className + '[' + Thread.currentThread().getName() + ']');
        }

        // prepare BCEL ClassGen
        Klass klass = null;
        try {
            klass = new Klass(className, bytecode, loader);
        }
        catch (Exception e) {
            log("failed " + className);
            e.printStackTrace();
            return bytecode;
        }

        // dump before (not compliant with multiple CL weaving same class differently,
        // since based on class FQN name)
        if (DUMP_BEFORE) {
            if (DUMP_PATTERN.matches(className)) {
                try {
                    klass.getCtClass().getClassPool().writeFile(className, "_dump2/before/");
                }
                catch (Exception e) {
                    log("failed to dump " + className);
                    e.printStackTrace();
                }
            }
        }

//        // rebuild from scratch if no bytecode cache
//        for (Iterator it = m_stack.iterator(); it.hasNext();) {
//            Object transformer = it.next();
//            Transformer tf = (Transformer)transformer;
//            try {
//                tf.transform(context, klass);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        for (Iterator it = m_stack.iterator(); it.hasNext();) {
            Object transformer = it.next();
            // if VERBOSE keep a copy of initial bytecode before transfo
            byte[] bytecodeBeforeLocalTransformation = null;
            if (VERBOSE) {
                bytecodeBeforeLocalTransformation = new byte[klass.getBytecode().length];
                System.arraycopy(klass.getBytecode(), 0,
                                 bytecodeBeforeLocalTransformation, 0,
                                 klass.getBytecode().length);
            }

            if (transformer instanceof Activator) {
                Activator tf = (Activator)transformer;
                try {
                    tf.activate(context, klass);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                // if VERBOSE confirm modification
                if (VERBOSE && !java.util.Arrays.equals(klass.getBytecode(), bytecodeBeforeLocalTransformation)) {
                    log(className + " <- " + transformer.getClass().getName());
                }
            }
        }

        // dump after (not compliant with multiple CL weaving same class differently,
        // since based on class FQN name)
        if (DUMP_AFTER) {
            if (DUMP_PATTERN.matches(className)) {
                try {
                    klass.getCtClass().getClassPool().writeFile(className, "_dump2/" + (DUMP_BEFORE ? "after/" : ""));
                }
                catch (Exception e) {
                    log("failed to dump " + className);
                    e.printStackTrace();
                }
            }
        }
        return klass.getBytecode();
    }
}

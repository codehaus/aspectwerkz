/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.HashSet;

import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.hook.ClassPreProcessor;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;

/**
 * AspectWerkzPreProcessor is the entry poinbt of the AspectWerkz layer 2
 *
 * It implements the ClassPreProcessor interface defined in layer 1.<br/>
 * Issued from JMangler, the transformer stack is hardcoded here - need refactoring.<br/>
 * <br/>
 * Available options are:
 * <ul>
 *      <li><code>-Daspectwerkz.transform.verbose=yes</code> turns on verbose mode:
 *      print on stdout all non filtered class names and which transformation are applied</li>
 *      <li><code>-Daspectwerkz.transform.dump=org.myapp.*</code> dumps transformed class matching
 *      pattern <i>org.myapp.*</i>(even unmodified ones)
 *      in <i>./_dump</i> directory (relative to where applications starts). The syntax
 *      <code>-Daspectwerkz.transform.dump=*</code> matchs all classes. The pattern language is the
 *      same as pointcut pattern language.</li>
 *      <li>else <code>-Daspectwerkz.transform.dump=org.myapp.*,before</code> dumps class before and after the
 *      transformation whose name starts with <i>org.myapp.</i>(even unmodified ones)
 *      in <i>./_dump/before</i> and <i>./_dump/after</i> directories (relative to where application starts)</li>
 *      <li><code>-Daspectwerkz.transform.filter=no</code> (or false) disables filtering of org.codehaus.aspectwerkz
 *      and related classes (jexl, trove, dom4j...). This should only be used in offline mode where weaving
 *      of those classes is needed. Setting this option in online mode will lead to ClassCircularityError.</li>
 * </ul>
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectWerkzPreProcessor implements ClassPreProcessor {

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
                DUMP_PATTERN = Pattern.compileClassPattern(
                        dumpPattern.substring(0, dumpPattern.indexOf(','))
                );
            }
            else {
                DUMP_PATTERN = Pattern.compileClassPattern(dumpPattern);
            }
        }
    }

    /**
     * The transformation m_stack
     */
    private List m_stack;

    /**
     * The transformer to add serial ver uid
     * Out of the transformation stack to be applied only if class is weaved
     */
    private AspectWerkzInterfaceTransformerComponent addSerialVerUidTransformer;

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

        addSerialVerUidTransformer = new AddSerialVersionUidTransformer();

        m_stack = new ArrayList();
        m_stack.add(new AddInterfaceTransformer());
        m_stack.add(new AddImplementationTransformer());
        m_stack.add(new AdviseMemberFieldTransformer());
        m_stack.add(new AdviseStaticFieldTransformer());
        m_stack.add(new AdviseCallerSideMethodTransformer());
        m_stack.add(new AdviseMemberMethodTransformer());
        m_stack.add(new AdviseStaticMethodTransformer());
//        m_stack.add(new AddMetaDataTransformer());
//        m_stack.add(new AddUuidTransformer());

        m_initialized = true;
    }

    /**
     * Transform bytecode going thru the interface transformation first.
     *
     * @param className class name
     * @param bytecode bytecode to transform
     * @param loader classloader loading the class
     * @return modified (or not) bytecode
     */
    public byte[] preProcess(final String className, final byte[] bytecode, final ClassLoader loader) {
        if (!m_initialized || (className==null) || (filter(className) && !NOFILTER)) {
            return bytecode;
        }

        buildMixinMetaDataRepository(loader);
        loadAndMergeXmlDefinitions(loader);
        if (VERBOSE) {
            log(loader + ":" + className + " [" + Thread.currentThread().getName() + "]");
        }

        // prepare BCEL ClassGen
        Klass klass = null;
        try {
            klass = new Klass(className, bytecode);
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
                    klass.getClassGen().getJavaClass().dump(
                            "_dump/before/" +
                            className.replace('.', '/') +
                            ".class"
                    );
                }
                catch (Exception e) {
                    System.err.println("failed to dump " + className);
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
                System.arraycopy(
                        klass.getBytecode(), 0,
                        bytecodeBeforeLocalTransformation, 0,
                        klass.getBytecode().length
                );
            }

            // do the interface transformations before the code transformations
            if (transformer instanceof AspectWerkzInterfaceTransformerComponent) {
                AspectWerkzInterfaceTransformerComponent intfTransformer =
                        (AspectWerkzInterfaceTransformerComponent)transformer;
                intfTransformer.sessionStart();
                intfTransformer.transformInterface(context, klass);
                intfTransformer.sessionEnd();
            }

            if (transformer instanceof AspectWerkzCodeTransformerComponent) {
                AspectWerkzCodeTransformerComponent codeTransformer =
                        (AspectWerkzCodeTransformerComponent)transformer;
                codeTransformer.sessionStart();
                codeTransformer.transformCode(context, klass);
                codeTransformer.sessionEnd();
            }

            // if VERBOSE confirm modification
            if (VERBOSE && !java.util.Arrays.equals(klass.getBytecode(), bytecodeBeforeLocalTransformation)) {
                log(className + " <- " + transformer.getClass().getName());
            }
        }

        // handle the serial ver uid only if class was advised
        if (context.isAdvised()) {
            addSerialVerUidTransformer.sessionStart();
            addSerialVerUidTransformer.transformInterface(context, klass);
            addSerialVerUidTransformer.sessionEnd();
        }

        // dump after (not compliant with multiple CL weaving same class differently,
        // since based on class FQN name)
        if (DUMP_AFTER) {
            if (DUMP_PATTERN.matches(className)) {
                try {
                    klass.getClassGen().getJavaClass().dump(
                            "_dump/" + (DUMP_BEFORE ? "after/" : "") +
                            className.replace('.', '/') + ".class"
                    );
                }
                catch (Exception e) {
                    System.err.println("failed to dump " + className);
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
     * Loads all the mixins loadable by the current classloader and creates and stores
     * meta-data for them.
     *
     * @param loader the current class loader
     */
    private void buildMixinMetaDataRepository(final ClassLoader loader) {
        if (m_metaDataRepository.containsKey(loader)) {
            return; // the repository have already been loaded by this class loader
        }
        Set repository = new HashSet();
        m_metaDataRepository.put(loader, repository); // add the loader here already to prevent recursive calls

        List definitions = DefinitionLoader.getDefinitionsForTransformation();
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            AspectWerkzDefinition definition = (AspectWerkzDefinition)it.next();
            definition.buildMixinMetaDataRepository(repository, loader);
        }
    }

    /**
     * Loads and stores all the XML definitions loadable by the current classloader.
     * <p/>
     * It searches the JAR/WAR/EAR for a 'META-INF/aspectwerkz.xml' file as well as the file
     * 'aspectwerkz.xml' on the classpath and the definition specified using the JVM option.
     *
     * @param loader the current class loader
     */
    private void loadAndMergeXmlDefinitions(final ClassLoader loader) {
        if (m_definitionRepository.containsKey(loader)) {
            return; // the definition have already been loaded by this class loader
        }
        m_definitionRepository.put(loader, null);

        DefinitionLoader.loadAndMergeDefinitions(loader);
    }

    /**
     * Excludes instrumentation for the class used during the instrumentation
     *
     * @param klass the AspectWerkz class
     */
    private static boolean filter(final String klass) {
        return klass.startsWith("org.codehaus.aspectwerkz.")
                || klass.startsWith("org.apache.commons.jexl.")
                || klass.startsWith("gnu.trove.")
                || klass.startsWith("org.dom4j.")
                || klass.startsWith("org.xml.sax.")
                || klass.startsWith("javax.xml.parsers.");
    }
}

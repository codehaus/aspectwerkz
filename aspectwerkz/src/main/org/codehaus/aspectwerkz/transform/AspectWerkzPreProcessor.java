/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
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
import java.util.Enumeration;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.XmlDefinitionParser;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.hook.ClassPreProcessor;
import org.dom4j.Document;

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
 *      <li><code>-Daspectwerkz.transform.dump=org.myapp.</code> dumps transformed class whose
 *      name starts with <i>org.myapp.</i>(even unmodified ones)
 *      in <i>./_dump</i> directory (relative to where applications starts). The syntax
 *      <code>-Daspectwerkz.transform.dump=*</code> matchs all classes</li>
 *      <li>else <code>-Daspectwerkz.transform.dump=org.myapp.,before</code> dumps class before and after the
 *      transformation whose name starts with <i>org.myapp.</i>(even unmodified ones)
 *      in <i>./_dump/before</i> and <i>./_dump/after</i> directories (relative to where application starts)</li>
 * </ul>
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectWerkzPreProcessor implements ClassPreProcessor {

    private final static boolean DUMP_BEFORE;
    private final static String AW_TRANSFORM_DUMP;
    private final static String AW_TRANSFORM_VERBOSE = "aspectwerkz.transform.verbose";
    private final static boolean VERBOSE;

    static {
        String verbose = System.getProperty(AW_TRANSFORM_VERBOSE, null);
        VERBOSE = "yes".equalsIgnoreCase(verbose) || "true".equalsIgnoreCase(verbose);

        // check for dump configuration
        String dumpPattern = System.getProperty("aspectwerkz.transform.dump", "");
        DUMP_BEFORE =  dumpPattern.indexOf(",before")>0;
        if (DUMP_BEFORE) {
            AW_TRANSFORM_DUMP = dumpPattern.substring(0, dumpPattern.indexOf(','));
        } else {
            AW_TRANSFORM_DUMP = dumpPattern;
        }
    }

    /**
     * The transformation m_stack
     */
    private List m_stack;

    /**
     * The mixin meta-data repositories, each repository is mapped to its class loader.
     */
    private Map m_metaDataRepository;

    /**
     * The XML definition repositories, each definition hierarchy is mapped to its class loader.
     */
    private Map m_definitionRepository;

    /**
     * Initializes the transformer m_stack
     *
     * @param params not used
     */
    public void initialize(final Hashtable params) {
        m_metaDataRepository = new WeakHashMap();
        m_definitionRepository = new WeakHashMap();
        m_stack = new ArrayList();
        m_stack.add(new AddSerialVersionUidTransformer());
        m_stack.add(new AdviseMemberFieldTransformer());
        m_stack.add(new AdviseStaticFieldTransformer());
        m_stack.add(new AdviseCallerSideMethodTransformer());
        m_stack.add(new AdviseMemberMethodTransformer());
        m_stack.add(new AdviseStaticMethodTransformer());
        m_stack.add(new AddInterfaceTransformer());
        m_stack.add(new AddImplementationTransformer());
//        m_stack.add(new AddMetaDataTransformer());
//        m_stack.add(new AddUuidTransformer());
    }

    /**
     * Transform bytecode going thru the interface transformation first
     *
     * @param className class name
     * @param bytecode bytecode to transform
     * @param loader classloader loading the class
     * @return modified (or not) bytecode
     */
    public byte[] preProcess(final String className, final byte[] bytecode, final ClassLoader loader) {
        if (filter(className)) {
            return bytecode;
        }

        buildMixinMetaDataRepository(loader);
        loadAndMergeXmlDefinitions(loader);

        if (VERBOSE)
            log(loader + ":" + className);

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
            if (className.startsWith(AW_TRANSFORM_DUMP) || "*".equals(AW_TRANSFORM_DUMP)) {
                try {
                    klass.getClassGen().getJavaClass().
                            dump("_dump/before/" + className.replace('.', '/') + ".class");
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
                //log(className + " : " +transformer.getClass().getName());
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

        // dump after (not compliant with multiple CL weaving same class differently,
        // since based on class FQN name)
        if (AW_TRANSFORM_DUMP.length() > 0) {
            if (className.startsWith(AW_TRANSFORM_DUMP) || "*".equals(AW_TRANSFORM_DUMP)) {
                try {
                    klass.getClassGen().getJavaClass().
                            dump("_dump/" + (DUMP_BEFORE?"after/":"") + className.replace('.', '/') + ".class");
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
        //@todo remove this - just for integration proto
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

        AspectWerkzDefinition def = AspectWerkzDefinition.getDefinitionForTransformation();
        for (Iterator it = def.getIntroductionDefinitions().iterator(); it.hasNext();) {
            String className = ((IntroductionDefinition)it.next()).getImplementation();
            if (className != null) {
                try {
                    Class mixin = loader.loadClass(className);
                    ClassMetaData metaData = ReflectionMetaDataMaker.createClassMetaData(mixin);
                    repository.add(metaData);
                }
                catch (ClassNotFoundException e) {
                    ;// ignore
                }
            }
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

        try {
            Enumeration definitions = loader.getResources(
                    "META-INF/" +
                    AspectWerkzDefinition.DEFAULT_DEFINITION_FILE_NAME
            );

            // grab the definition in the current class loader
            Document document = null;
            if (definitions.hasMoreElements()) {
                URL url = (URL)definitions.nextElement();
                document = XmlDefinitionParser.createDocument(url);
            }

            // merge the definition with the definitions in class loaders
            // higher up in the class loader hierachy
            while (definitions.hasMoreElements()) {
                document = XmlDefinitionParser.mergeDocuments(
                        document,
                        XmlDefinitionParser.createDocument((URL)definitions.nextElement())
                );
            }

            // handle the merging of the 'aspectwerkz.xml' definition on the classpath
            // (if there is one)
            InputStream stream = AspectWerkzDefinition.getDefinitionInputStream();
            if (stream != null) {
                document = XmlDefinitionParser.mergeDocuments(
                        document,
                        XmlDefinitionParser.createDocument(stream)
                );
            }

            // handle the merging of the definition file specified using the JVM option
            String definitionFile = AspectWerkzDefinition.DEFINITION_FILE;
            if (definitionFile != null) {
                document = XmlDefinitionParser.mergeDocuments(
                        document,
                        XmlDefinitionParser.createDocument(new File(definitionFile).toURL())
                );
            }

            // create a new definition based on the merged definition documents
            AspectWerkzDefinition.createDefinition(document);
        }
        catch (Exception e) {
            ;// ignore
        }
    }

    /**
     * Excludes instrumentation for the class used during the instrumentation
     *
     * @param klass the AspectWerkz class
     */
    private static boolean filter(final String klass) {
        return     klass.startsWith("org.codehaus.aspectwerkz.transform.")
                || klass.startsWith("org.codehaus.aspectwerkz.metadata.")
                || klass.startsWith("org.codehaus.aspectwerkz.")
                || klass.startsWith("org.apache.commons.jexl.")
                || klass.startsWith("org.dom4j.")
                || klass.startsWith("org.xml.sax.")
                || klass.startsWith("javax.xml.parsers.");
    }
}

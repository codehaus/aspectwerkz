/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.apache.bcel.Repository;
import org.apache.bcel.util.ClassLoaderRepository;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.InputStream;

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
 *      in <i>./_dump</i> directory (relative to where applications starts)</li>
 * </ul>
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectWerkzPreProcessor implements org.codehaus.aspectwerkz.hook.ClassPreProcessor {

    private final static String AW_TRANSFORM_DUMP = System.getProperty("aspectwerkz.transform.dump", "");

    private final static String AW_TRANSFORM_VERBOSE = "aspectwerkz.transform.verbose";
    private final static boolean VERBOSE = "yes".equalsIgnoreCase(System.getProperty(AW_TRANSFORM_VERBOSE, "no"));

    /** transformation stack */
    private List stack;

    /**
     * Excludes instrumentation for the class used during the instrumentation
     */
    private boolean filter(String klass) {
        return klass.startsWith("org.codehaus.aspectwerkz.transform.")
                || klass.startsWith("org.codehaus.aspectwerkz.metadata.")
                || klass.startsWith("org.apache.commons.jexl.")
                || klass.startsWith("org.codehaus.aspectwerkz.");
    }

    /**
     * Initializes the transformer stack
     * @param params not used
     */
    public void initialize(Hashtable params) {
        stack = new ArrayList();
        stack.add(new AddSerialVersionUidTransformer());
        stack.add(new AdviseMemberFieldTransformer());
        stack.add(new AdviseStaticFieldTransformer());
        stack.add(new AdviseCallerSideMethodTransformer());
        stack.add(new AdviseMemberMethodTransformer());
        stack.add(new AdviseStaticMethodTransformer());
        stack.add(new AddInterfaceTransformer());
        stack.add(new AddImplementationTransformer());
        stack.add(new AddMetaDataTransformer());
        stack.add(new AddUuidTransformer());
    }

    public static void log(String s) {
        //@todo remove this - just for integration proto
        if (VERBOSE) {
            System.out.println(s);
        }
    }

    /**
     * Transform bytecode going thru the interface transformation first
     * @param className class name
     * @param bytecode bytecode to transform
     * @param loader classloader loading the class
     * @return modified (or not) bytecode
     */
    public byte[] preProcess(String className, byte[] bytecode, ClassLoader loader) {
        if (filter(className)) {
            return bytecode;
        }
        //@todo review log
        //log(loader + ":" + className);

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

        // create a new transformation context
        Context context = new Context(loader);

        for (Iterator i = stack.iterator(); i.hasNext();) {
            Object transformer = i.next();

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

        //dump (not compliant with multiple CL weaving same class differently, since based on class FQN name)
        if (AW_TRANSFORM_DUMP.length() > 0) {
            if (className.startsWith(AW_TRANSFORM_DUMP)) {
                try {
                    klass.getClassGen().getJavaClass().
                            dump("_dump/" + className.replace('.', '/') + ".class");
                }
                catch (Exception e) {
                    System.err.println("failed to dump " + className);
                    e.printStackTrace();
                }
            }
        }

        return klass.getBytecode();
    }

}

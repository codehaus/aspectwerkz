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
 * @version $Id: AspectWerkzPreProcessor.java,v 1.1.2.6 2003-07-22 16:20:10 avasseur Exp $
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
        stack.add(new AdviseMemberMethodTransformer());
        stack.add(new AdviseStaticMethodTransformer());
        stack.add(new AdviseMemberFieldTransformer());
        stack.add(new AdviseStaticFieldTransformer());
        stack.add(new AdviseCallerSideMethodTransformer());
        stack.add(new AddInterfaceTransformer());
        stack.add(new AddImplementationTransformer());
        stack.add(new AddMetaDataTransformer());
        stack.add(new AddUuidTransformer());

        //@todo: check this: commented in AW 0.7 conf
        //stack.add(new AddReadObjectTransformer());
    }

    public static void log(String s) {
        //@todo remove this - just for integration proto
        if (VERBOSE)
            System.out.println(s);
    }

    /**
     * Transform bytecode going thru the interface transformation first
     * @param klass class name
     * @param bytecode bytecode to transform
     * @param loader classloader loading the class
     * @return modified (or not) bytecode
     */
    public /*synchronized*/ byte[] preProcess(String klass, byte[] bytecode, ClassLoader loader) {
        if (filter(klass))
            return bytecode;

        // prepare BCEL ClassGen
        AspectWerkzUnextendableClassSet cs = null;
        try {
            cs = new AspectWerkzUnextendableClassSet(klass, bytecode);
        } catch (Exception e) {
            log("failed " + klass);
            e.printStackTrace();
            return bytecode;
        }

        // set Repository, from where to start finding interfaces and parent classes
        cs.getClassGen().getJavaClass().setRepository(new ClassLoaderRepository(loader));

        //dump
        //@todo dump is not compliant with multiple CL weaving same class differently
        if (AW_TRANSFORM_DUMP.length()>0) {
            if (klass.startsWith(AW_TRANSFORM_DUMP)) {
                try {
                    cs.getClassGen().getJavaClass().dump("_dump/before/"+klass.replace('.', '/')+".class");
                } catch (Exception e) {
                    System.err.println("failed to dump " + klass);
                    e.printStackTrace();
                }
            }
        }
        int stackIndex=0;
        for (Iterator i = stack.iterator(); i.hasNext(); ) {
            Object transformer = i.next();
            stackIndex++;

            // if VERBOSE keep a copy of initial bytecode before transfo
            byte[] bytecodeBeforeLocalTransformation = null;
            if (VERBOSE) {
                bytecodeBeforeLocalTransformation = new byte[cs.getBytecode().length];
                System.arraycopy(cs.getBytecode(), 0, bytecodeBeforeLocalTransformation, 0, cs.getBytecode().length);
                if ( ! java.util.Arrays.equals(cs.getBytecode(), bytecodeBeforeLocalTransformation))
                    System.err.println("!!!!!! "+klass);

            }

            // JMangler doco say intf before code transfo
            if (transformer instanceof AspectWerkzAbstractInterfaceTransformer) {
                AspectWerkzAbstractInterfaceTransformer intfTransformer = (AspectWerkzAbstractInterfaceTransformer) transformer;
                intfTransformer.sessionStart();
                intfTransformer.transformInterface(new AspectWerkzExtensionSet(), cs);
                intfTransformer.sessionEnd();
            }

            if (transformer instanceof AspectWerkzCodeTransformerComponent) {
                AspectWerkzCodeTransformerComponent codeTransformer = (AspectWerkzCodeTransformerComponent) transformer;
                codeTransformer.sessionStart();
                codeTransformer.transformCode(cs);
                codeTransformer.sessionEnd();
            }

            // if VERBOSE confirm modification
            if (VERBOSE && !java.util.Arrays.equals(cs.getBytecode(), bytecodeBeforeLocalTransformation)) {
                //double check
                byte[] after = cs.getBytecode();
                int afterL = after.length;
                System.out.println("length: "+bytecodeBeforeLocalTransformation.length + "\t" + afterL);
                /*for (int bi = 0; bi < afterL; bi++) {
                    if (after[bi] != bytecodeBeforeLocalTransformation[bi])
                        System.out.println(bi+"\t"+after[bi]+"\t"+bytecodeBeforeLocalTransformation[bi]);
                }*/

                System.out.println(klass + " <- " + transformer.getClass().getName());
                // dump modified
                if (AW_TRANSFORM_DUMP.length()>0) {
                    if (klass.startsWith(AW_TRANSFORM_DUMP)) {
                        try {
                            cs.getClassGen().getJavaClass().dump("_dump/"+stackIndex+"_"+transformer.getClass().getName()+"/"+klass.replace('.', '/')+".class");
                        } catch (Exception e) {
                            System.err.println("failed to dump " + klass);
                            e.printStackTrace();
                        }
                    }
                }
            }

        }

        //dump
        //@todo dump is not compliant with multiple CL weaving same class differently
        if (AW_TRANSFORM_DUMP.length()>0) {
            if (klass.startsWith(AW_TRANSFORM_DUMP)) {
                try {
                    cs.getClassGen().getJavaClass().dump("_dump/after/"+klass.replace('.', '/')+".class");
                } catch (Exception e) {
                    System.err.println("failed to dump " + klass);
                    e.printStackTrace();
                }
            }
        }

        return cs.getBytecode();
    }



 }

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * AspectWerkzPreProcessor is the entry poinbt of the AspectWerkz layer 2
 *
 * It implements the ClassPreProcessor interface defined in layer 1.<br/>
 * Issued from JMangler, the transformer stack is hardcoded here - need refactoring.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @version $Id: AspectWerkzPreProcessor.java,v 1.1.2.2 2003-07-17 17:48:48 avasseur Exp $
 */
public class AspectWerkzPreProcessor implements org.codehaus.aspectwerkz.hook.ClassPreProcessor {

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
        if ("yes".equalsIgnoreCase(System.getProperty("aw.besee.verbose", "no")))
            System.out.println(s);
    }

    /**
     * Transform bytecode going thru the interface transformation first
     * @param klass class name
     * @param bytecode bytecode to transform
     * @param loader classloader loading the class
     * @return modified (or not) bytecode
     */
    public byte[] preProcess(String klass, byte[] bytecode, ClassLoader loader) {
        if (filter(klass))
            return bytecode;

        if (klass.startsWith("weblogic.rmi.internal.dgc.DGCServerImpl_WLSkel")) {
            return bytecode;
        }

        //@todo temp
        System.out.println(klass);

        // prepare BCEL ClassGen
        AspectWerkzUnextendableClassSet cs = null;
        try {
            cs = new AspectWerkzUnextendableClassSet(klass, bytecode);
        } catch (Exception e) {
            log("failed " + klass);
            e.printStackTrace();
            return bytecode;
        }

        for (Iterator i = stack.iterator(); i.hasNext(); ) {
            Object transformer = i.next();

            // JMangler doco say intf before code transfo
            if (transformer instanceof AspectWerkzAbstractInterfaceTransformer) {
                AspectWerkzAbstractInterfaceTransformer intfTransformer = (AspectWerkzAbstractInterfaceTransformer) transformer;
                //log(intfTransformer.verboseMessage());

                intfTransformer.sessionStart();
                intfTransformer.transformInterface(new AspectWerkzExtensionSet(), cs);
                intfTransformer.sessionEnd();
            }

            if (transformer instanceof AspectWerkzCodeTransformerComponent) {
                AspectWerkzCodeTransformerComponent codeTransformer = (AspectWerkzCodeTransformerComponent) transformer;
                //log(codeTransformer.verboseMessage());

                codeTransformer.sessionStart();
                codeTransformer.transformCode(cs);
                codeTransformer.sessionEnd();
            }

        }

        //log(klass + " for " + ((loader==null)?"BootstrapCL":loader.toString()));

        //dump
        //@todo refactor dump facility
        /*if ("yes".equalsIgnoreCase(System.getProperty("aw.besee.dump", "no"))) {
            try {
                cs.getClassGen().getJavaClass().dump("dump/"+klass.replace('.', '/')+".class");
            } catch (Exception e) {
                ;
            }
        }*/
        //@todo temp
        if (klass.startsWith("weblogic.rmi.internal.dgc.")) {
            try {
                cs.getClassGen().getJavaClass().dump("dump/"+klass.replace('.', '/')+".class");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return cs.getBytecode();
    }



 }

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.delegation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.Transformer;
import org.codehaus.aspectwerkz.transform.WeavingStrategy;

/**
 * A weaving strategy implementing a weaving scheme based on delegation.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class DelegationWeavingStrategy implements WeavingStrategy {
    /**
     * The transformation m_stack
     */
    private List m_stack;

    /**
     * The transformer to add serial ver uid Out of the transformation stack to be applied only if class is weaved
     */
    private Transformer m_addSerialVerUidTransformer;

    /**
     * Initializes the transformer stack.
     * 
     * @param params not used
     */
    public void initialize(final Hashtable params) {
        m_addSerialVerUidTransformer = new AddSerialVersionUidTransformer();

        // CAUTION: ORDER IS IMPORTANT!
        m_stack = new ArrayList();
        m_stack.add(new PrepareAdvisedClassTransformer());

        //                m_stack.add(new MethodCallUnTransformer());
        m_stack.add(new FieldSetGetTransformer());
        m_stack.add(new MethodCallTransformer());
        m_stack.add(new ConstructorCallTransformer());
        m_stack.add(new MethodExecutionTransformer());
        m_stack.add(new ConstructorExecutionTransformer());
        m_stack.add(new HandlerTransformer());
        m_stack.add(new AddImplementationTransformer());
        m_stack.add(new AddInterfaceTransformer());
    }

    /**
     * Transforms the class.
     * 
     * @param className
     * @param klass
     * @param context
     */
    public void transform(final String className, final Context context) {
//        ClassInfo classInfo = AsmClassInfo.getClassInfo(context.getInitialBytecode(), context.getLoader());
//        System.out.println("class name: " + classInfo.getName());
//        MethodInfo[] methods = classInfo.getMethods();
//        for (int i = 0; i < methods.length; i++) {
//            MethodInfo info = methods[i];
//            System.out.println("-------- method name: " + info.getName());
//            System.out.println("-------- return type: " + info.getReturnType().getName());
//            System.out.println("-------- nr annotations " + info.getAnnotations().size());
//            ClassInfo[] params = info.getParameterTypes();
//            for (int j = 0; j < params.length; j++) {
//                ClassInfo info2 = params[j];
//                System.out.println("----------------> param: " + info2.getName());
//            }
//        }

        boolean advisedAtLeastOnce = false;
        Klass klass = (Klass) context.getClassAbstraction();
        for (Iterator it = m_stack.iterator(); it.hasNext();) {
            Object transformer = it.next();
            if (transformer instanceof Transformer) {
                Transformer tf = (Transformer) transformer;
                context.resetAdvised();
                try {
                    tf.transform(context, klass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (context.isAdvised()) {
                    advisedAtLeastOnce = true;
                }
                if (context.isAdvised()) {
                    AspectWerkzPreProcessor.log(" " + className + " <- " + transformer.getClass().getName());
                }
            }
        }
        // handle the serial ver uid only if class was advised
        if (advisedAtLeastOnce) {
            try {
                m_addSerialVerUidTransformer.transform(context, klass);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            AspectWerkzPreProcessor.dumpForce(className, context);
        }
    }

    /**
     * Creates a new transformation context.
     * 
     * @param name
     * @param bytecode
     * @param loader
     * @return
     */
    public Context newContext(final String name, final byte[] bytecode, final ClassLoader loader) {
        return new ContextImpl(name, bytecode, loader);
    }
}
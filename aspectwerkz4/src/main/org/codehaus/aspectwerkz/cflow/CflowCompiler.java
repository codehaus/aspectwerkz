/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.cflow;

import org.objectweb.asm.Constants;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.expression.ast.ASTCflow;
import org.codehaus.aspectwerkz.expression.ast.ASTCflowBelow;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Compiler for the JIT cflow Aspect
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CflowCompiler implements Constants, TransformationConstants {

    private final static String JIT_CFLOW_CLASS = "org/codehaus/aspectwerkz/cflow/Cflow_";
    private final static String ABSTRACT_CFLOW_CLASS = "org/codehaus/aspectwerkz/cflow/AbstractCflowSystemAspect";
    private final static String ABSTRACT_CFLOW_SIGNATURE = "L"+ABSTRACT_CFLOW_CLASS+";";
    private final static String INSTANCE_CFLOW_FIELD_NAME = "INSTANCE";
    private final static String[] EMPTY_STRING_ARRAY = new String[0];
    public static final String IN_CFLOW_METOD_NAME = "inCflow";
    public static final String IN_CFLOW_METOD_SIGNATURE = "()Z";
    public static final String IN_CFLOWBELOW_METOD_NAME = "inCflowBelow";
    public static final String IN_CFLOWBELOW_METOD_SIGNATURE = "()Z";

    private String m_className;

    private ClassWriter m_cw;

    private CflowCompiler(int cflowId) {
        m_className = getCflowAspectClassName(cflowId);
    }

    private byte[] compile() {
        m_cw = AsmHelper.newClassWriter(true);

        m_cw.visit(
                AsmHelper.JAVA_VERSION,
                ACC_PUBLIC + ACC_SUPER + ACC_SYNTHETIC,
                m_className,
                ABSTRACT_CFLOW_CLASS,
                EMPTY_STRING_ARRAY,
                null
        );

        // static INSTANCE field
        m_cw.visitField(
                ACC_PRIVATE + ACC_STATIC,
                INSTANCE_CFLOW_FIELD_NAME,
                ABSTRACT_CFLOW_SIGNATURE,
                null,
                null
        );

        // ctor
        CodeVisitor cv = m_cw.visitMethod(
                ACC_PUBLIC,
                INIT_METHOD_NAME,
                NO_PARAM_RETURN_VOID_SIGNATURE,
                EMPTY_STRING_ARRAY,
                null
        );
        // invoke the constructor of abstract
        cv.visitVarInsn(ALOAD, 0);
        cv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_CFLOW_CLASS, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE);
        // assign the singleton field to this
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(PUTSTATIC, m_className, INSTANCE_CFLOW_FIELD_NAME, ABSTRACT_CFLOW_SIGNATURE);
        cv.visitInsn(RETURN);
        cv.visitMaxs(0, 0);

        // static isInCflow delegators
        cv = m_cw.visitMethod(
                ACC_PUBLIC + ACC_STATIC,
                IS_IN_CFLOW_METOD_NAME,
                IS_IN_CFLOW_METOD_SIGNATURE,
                EMPTY_STRING_ARRAY,
                null
        );
        cv.visitFieldInsn(GETSTATIC, m_className, INSTANCE_CFLOW_FIELD_NAME, ABSTRACT_CFLOW_SIGNATURE);
        cv.visitMethodInsn(INVOKEVIRTUAL, ABSTRACT_CFLOW_CLASS, IN_CFLOW_METOD_NAME, IN_CFLOW_METOD_SIGNATURE);
        cv.visitInsn(IRETURN);
        cv.visitMaxs(0, 0);

        // static isInCflowBelow delegators
        cv = m_cw.visitMethod(
                ACC_PUBLIC + ACC_STATIC,
                IS_IN_CFLOWBELOW_METOD_NAME,
                IS_IN_CFLOWBELOW_METOD_SIGNATURE,
                EMPTY_STRING_ARRAY,
                null
        );
        cv.visitFieldInsn(GETSTATIC, m_className, INSTANCE_CFLOW_FIELD_NAME, ABSTRACT_CFLOW_SIGNATURE);
        cv.visitMethodInsn(INVOKEVIRTUAL, ABSTRACT_CFLOW_CLASS, IN_CFLOWBELOW_METOD_NAME, IN_CFLOWBELOW_METOD_SIGNATURE);
        cv.visitInsn(IRETURN);
        cv.visitMaxs(0, 0);

        return m_cw.toByteArray();
    }

    public static String getCflowAspectClassName(int cflowID) {
        return JIT_CFLOW_CLASS + cflowID;
    }

    public static Class compileCflowAspectAndAttachToClassLoader(ClassLoader loader, int cflowID) {
        CflowCompiler compiler = new CflowCompiler(cflowID);
        byte[] cflowAspectBytes = compiler.compile();
        Class cflowAspect = AsmHelper.loadClass(
                loader,
                cflowAspectBytes,
                getCflowAspectClassName(cflowID)
        );
        return cflowAspect;
    }

    public static void main(String args[]) throws Throwable {
        CflowCompiler me = new CflowCompiler(4);
        me.compile();

        AsmHelper.dumpClass("_dump", me.m_className, me.m_cw);

        Class myCflow = AsmHelper.loadClass(ClassLoader.getSystemClassLoader(), me.compile(), me.m_className);
        AbstractCflowSystemAspect af = (AbstractCflowSystemAspect) myCflow.newInstance();
        System.out.println(af.getCflowID());

        ClassInfo cflowAspectInfo = AsmClassInfo.getClassInfo(me.compile(), myCflow.getClassLoader());
        System.out.println(cflowAspectInfo.getSignature());
        ClassInfo abstractCflowAspectInfo = cflowAspectInfo.getSuperclass();
        MethodInfo beforeAdvice = null;
        MethodInfo afterFinallyAdvice = null;
        for (int i = 0; i < abstractCflowAspectInfo.getMethods().length; i++) {
            MethodInfo methodInfo = abstractCflowAspectInfo.getMethods()[i];
            if (methodInfo.getName().equals("enter")) {
                beforeAdvice = methodInfo;
            } else if (methodInfo.getName().equals("exit")) {
                afterFinallyAdvice = methodInfo;
            }
        }
        if (beforeAdvice == null || afterFinallyAdvice == null) {
            throw new DefinitionException("Could not gather cflow advice from " + cflowAspectInfo.getName());
        }

    }

}

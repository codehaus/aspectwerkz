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
import org.objectweb.asm.Label;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
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

    /**
     * the jit cflow aspect class name (with /)
     */
    private String m_className;

    private ClassWriter m_cw;

    /**
     * private ctor
     * @param cflowId
     */
    private CflowCompiler(int cflowId) {
        m_className = getCflowAspectClassName(cflowId);
    }

    /**
     * compile the jit cflow aspect
     * @return bytecode for the concrete jit cflow aspect
     */
    private byte[] compile() {
        m_cw = AsmHelper.newClassWriter(true);

        // class extends AbstractCflowsystemAspect
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

        // static isInCflow() delegators
        cv = m_cw.visitMethod(
                ACC_PUBLIC + ACC_STATIC,
                IS_IN_CFLOW_METOD_NAME,
                IS_IN_CFLOW_METOD_SIGNATURE,
                EMPTY_STRING_ARRAY,
                null
        );
        Label isNull = new Label();
        cv.visitFieldInsn(GETSTATIC, m_className, INSTANCE_CFLOW_FIELD_NAME, ABSTRACT_CFLOW_SIGNATURE);
        cv.visitJumpInsn(IFNULL, isNull);
        cv.visitFieldInsn(GETSTATIC, m_className, INSTANCE_CFLOW_FIELD_NAME, ABSTRACT_CFLOW_SIGNATURE);
        cv.visitMethodInsn(INVOKEVIRTUAL, ABSTRACT_CFLOW_CLASS, IN_CFLOW_METOD_NAME, IN_CFLOW_METOD_SIGNATURE);
        cv.visitInsn(IRETURN);
        cv.visitLabel(isNull);
        cv.visitInsn(ICONST_0);
        cv.visitInsn(IRETURN);
        cv.visitMaxs(0, 0);

        // static isInCflowBelow() delegators
        cv = m_cw.visitMethod(
                ACC_PUBLIC + ACC_STATIC,
                IS_IN_CFLOWBELOW_METOD_NAME,
                IS_IN_CFLOWBELOW_METOD_SIGNATURE,
                EMPTY_STRING_ARRAY,
                null
        );
        Label isNull2 = new Label();
        cv.visitFieldInsn(GETSTATIC, m_className, INSTANCE_CFLOW_FIELD_NAME, ABSTRACT_CFLOW_SIGNATURE);
        cv.visitJumpInsn(IFNULL, isNull2);
        cv.visitFieldInsn(GETSTATIC, m_className, INSTANCE_CFLOW_FIELD_NAME, ABSTRACT_CFLOW_SIGNATURE);
        cv.visitMethodInsn(INVOKEVIRTUAL, ABSTRACT_CFLOW_CLASS, IN_CFLOWBELOW_METOD_NAME, IN_CFLOWBELOW_METOD_SIGNATURE);
        cv.visitInsn(IRETURN);
        cv.visitLabel(isNull2);
        cv.visitInsn(ICONST_0);
        cv.visitInsn(IRETURN);
        cv.visitMaxs(0, 0);

        m_cw.visitEnd();

        return m_cw.toByteArray();
    }

    /**
     * The naming strategy for jit cflow aspect
     * @param cflowID
     * @return org.codehaus.aspectwerkz.cflow.Cflow_cflowID
     */
    public static String getCflowAspectClassName(int cflowID) {
        return JIT_CFLOW_CLASS + cflowID;
    }

    /**
     * If necessary, compile a jit cflow aspect and attach it to the given classloader
     *
     * @param loader
     * @param cflowID
     * @return
     */
    public static Class compileCflowAspectAndAttachToClassLoader(ClassLoader loader, int cflowID) {
        //TODO do we need a Class.forName check first to avoid unecessary compilation ?
        CflowCompiler compiler = new CflowCompiler(cflowID);

        try {
            AsmHelper.dumpClass("_dump", getCflowAspectClassName(cflowID), compiler.m_cw);
        } catch (Throwable t) {;}

        byte[] cflowAspectBytes = compiler.compile();
        Class cflowAspect = AsmHelper.loadClass(
                loader,
                cflowAspectBytes,
                getCflowAspectClassName(cflowID)
        );
        return cflowAspect;
    }
}

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

/**
 * Compiler for the JIT cflow Aspect
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CflowCompiler implements Constants, TransformationConstants {

    private final static String JIT_CFLOW_CLASS = "org/codehaus/aspectwerkz/cflow/Cflow_";
    private final static String ABSTRACT_CFLOW_CLASS = "org/codehaus/aspectwerkz/cflow/AbstractCflowSystemAspect";

    private String m_className;

    private ClassWriter m_cw;

    public CflowCompiler(int cflowId) {
        m_className = JIT_CFLOW_CLASS + cflowId;
    }

    public byte[] compile() {
        m_cw = AsmHelper.newClassWriter(true);

        m_cw.visit(
                AsmHelper.JAVA_VERSION,
                ACC_PUBLIC + ACC_SUPER,
                m_className,
                ABSTRACT_CFLOW_CLASS,
                new String[0],
                null
        );

        // ctor
        CodeVisitor cv = m_cw.visitMethod(
                ACC_PUBLIC,
                INIT_METHOD_NAME,
                NO_PARAM_RETURN_VOID_SIGNATURE,
                new String[0],
                null
        );
        // invoke the constructor of abstract
        cv.visitVarInsn(ALOAD, 0);
        cv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_CFLOW_CLASS, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE);
        cv.visitInsn(RETURN);
        cv.visitMaxs(0, 0);

        // invoke the constructor of abstract
        cv.visitVarInsn(ALOAD, 0);
        cv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_CFLOW_CLASS, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE);
        cv.visitInsn(RETURN);
        cv.visitMaxs(0, 0);

        return m_cw.toByteArray();
    }



    public static void main(String args[]) throws Throwable {
        CflowCompiler me = new CflowCompiler(4);
        me.compile();

        AsmHelper.dumpClass("_dump", me.m_className, me.m_cw);

        Class myCflow = AsmHelper.loadClass(ClassLoader.getSystemClassLoader(), me.compile(), me.m_className);
        AbstractCflowSystemAspect af = (AbstractCflowSystemAspect) myCflow.newInstance();
        System.out.println(af.getCflowID());
    }

}

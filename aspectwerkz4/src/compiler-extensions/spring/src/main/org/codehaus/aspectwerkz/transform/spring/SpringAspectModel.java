/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.spring;

import org.codehaus.aspectwerkz.transform.aopalliance.AopAllianceAspectModel;
import org.codehaus.aspectwerkz.transform.inlining.spi.AspectModel;
import org.codehaus.aspectwerkz.transform.inlining.AdviceMethodInfo;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.definition.AspectDefinition;

import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;

//import org.objectweb.asm.CodeVisitor;
//import org.objectweb.asm.ClassWriter;
//import org.objectweb.asm.Type;
import org.codehaus.aspectwerkz.org.objectweb.asm.CodeVisitor;
import org.codehaus.aspectwerkz.org.objectweb.asm.ClassWriter;
import org.codehaus.aspectwerkz.org.objectweb.asm.Type;

/**
 * Implementation of the AspectModel interface for Spring framework.
 * <p/>
 * Provides methods for definition of aspects and framework specific bytecode generation
 * used by the join point compiler.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class SpringAspectModel extends AopAllianceAspectModel {

    protected static final String ASPECT_MODEL_TYPE = "spring";

    /**
     * Returns the aspect model type, which is an id for the the special aspect model, can be anything as long
     * as it is unique.
     *
     * @return the aspect model type id
     */
    public String getAspectModelType() {
        return ASPECT_MODEL_TYPE;
    }

    /**
     * Defines the aspect.
     *
     * @param classInfo
     * @param aspectDef
     * @param loader
     */
    public void defineAspect(final ClassInfo classInfo,
                             final AspectDefinition aspectDef,
                             final ClassLoader loader) {
        ClassInfo[] interfaces = classInfo.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            ClassInfo anInterface = interfaces[i];
            if (anInterface.getName().equals(MethodInterceptor.class.getName()) ||
                anInterface.getName().equals(MethodBeforeAdvice.class.getName()) ||
                anInterface.getName().equals(AfterReturningAdvice.class.getName()) ||
                anInterface.getName().equals(ThrowsAdvice.class.getName())) {
                aspectDef.setAspectModel(ASPECT_MODEL_TYPE);
                aspectDef.setContainerClassName(ASPECT_CONTAINER_CLASS_NAME);
                return;
            }
        }
    }

    /**
     * Returns info about the closure class, name and type (interface or class).
     *
     * @return the closure class info
     */
    public AroundClosureClassInfo getAroundClosureClassInfo() {
        return new AspectModel.AroundClosureClassInfo(
                null,
                new String[]{
                    AOP_ALLIANCE_CLOSURE_CLASS_NAME,
                    MethodBeforeAdvice.class.getName().replace('.', '/'),
                    AfterReturningAdvice.class.getName().replace('.', '/')
                }
        );
    }


    /**
     * Creates the methods required to implement or extend to implement the closure for the specific
     * aspect model type.
     *
     * @param cw
     * @param className
     */
    public void createMandatoryMethods(final ClassWriter cw, final String className) {
        super.createMandatoryMethods(cw, className);
    }

    /**
     * Handles the arguments to the before advice.
     *
     * @param cv
     * @param adviceMethodInfo
     */
    public void createBeforeAdviceArgumentHandling(final CodeVisitor cv, final AdviceMethodInfo adviceMethodInfo) {
        final String joinPointClassName = adviceMethodInfo.getJoinPointClassName();
        final int joinPointIndex = adviceMethodInfo.getJoinPointIndex();
        cv.visitFieldInsn(
                GETSTATIC,
                joinPointClassName,
                SIGNATURE_FIELD_NAME,
                METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
        );
        cv.visitMethodInsn(
                INVOKEVIRTUAL,
                METHOD_SIGNATURE_IMPL_CLASS_NAME,
                GET_METHOD_METHOD_NAME,
                GET_METHOD_METHOD_SIGNATURE
        );

        if (Type.getArgumentTypes(adviceMethodInfo.getCalleeMemberDesc()).length == 0) {
            cv.visitInsn(ACONST_NULL);
        } else {
            cv.visitVarInsn(ALOAD, joinPointIndex);
            cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    joinPointClassName,
                    GET_RTTI_METHOD_NAME,
                    GET_RTTI_METHOD_SIGNATURE
            );
            cv.visitTypeInsn(CHECKCAST, METHOD_RTTI_IMPL_CLASS_NAME);
            cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    METHOD_RTTI_IMPL_CLASS_NAME,
                    GET_PARAMETER_VALUES_METHOD_NAME,
                    GET_ARGUMENTS_METHOD_SIGNATURE
            );
        }
        cv.visitVarInsn(ALOAD, joinPointIndex);
        cv.visitFieldInsn(
                GETFIELD,
                joinPointClassName,
                CALLEE_INSTANCE_FIELD_NAME,
                adviceMethodInfo.getCalleeClassSignature()
        );
    }

    /**
     * Handles the arguments to the after advice.
     *
     * @param cv
     * @param adviceMethodInfo
     */
    public void createAfterAdviceArgumentHandling(final CodeVisitor cv, final AdviceMethodInfo adviceMethodInfo) {
        final String joinPointClassName = adviceMethodInfo.getJoinPointClassName();
        final int joinPointIndex = adviceMethodInfo.getJoinPointIndex();
        final String specArgDesc = adviceMethodInfo.getSpecialArgumentTypeDesc();
        if (specArgDesc == null) {
            cv.visitInsn(ACONST_NULL);
        } else {
            cv.visitVarInsn(ALOAD, adviceMethodInfo.getSpecialArgumentIndex());
            AsmHelper.wrapPrimitiveType(cv, Type.getType(specArgDesc));
        }
        cv.visitFieldInsn(
                GETSTATIC,
                joinPointClassName,
                SIGNATURE_FIELD_NAME,
                METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
        );
        cv.visitMethodInsn(
                INVOKEVIRTUAL,
                METHOD_SIGNATURE_IMPL_CLASS_NAME,
                GET_METHOD_METHOD_NAME,
                GET_METHOD_METHOD_SIGNATURE
        );

        if (Type.getArgumentTypes(adviceMethodInfo.getCalleeMemberDesc()).length == 0) {
            cv.visitInsn(ACONST_NULL);
        } else {
            cv.visitVarInsn(ALOAD, joinPointIndex);
            cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    joinPointClassName,
                    GET_RTTI_METHOD_NAME,
                    GET_RTTI_METHOD_SIGNATURE
            );
            cv.visitTypeInsn(CHECKCAST, METHOD_RTTI_IMPL_CLASS_NAME);
            cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    METHOD_RTTI_IMPL_CLASS_NAME,
                    GET_PARAMETER_VALUES_METHOD_NAME,
                    GET_ARGUMENTS_METHOD_SIGNATURE
            );
        }

        cv.visitVarInsn(ALOAD, joinPointIndex);
        cv.visitFieldInsn(
                GETFIELD,
                joinPointClassName,
                CALLEE_INSTANCE_FIELD_NAME,
                adviceMethodInfo.getCalleeClassSignature()
        );
    }
}

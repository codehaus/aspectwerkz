/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.aopalliance;

import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.transform.inlining.spi.AspectModel;
import org.codehaus.aspectwerkz.transform.inlining.AdviceMethodInfo;
import org.codehaus.aspectwerkz.transform.inlining.AspectInfo;
import org.codehaus.aspectwerkz.transform.inlining.compiler.AbstractJoinPointCompiler;
import org.codehaus.aspectwerkz.transform.TransformationConstants;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.ConstructorInterceptor;

//import org.objectweb.asm.CodeVisitor;
//import org.objectweb.asm.ClassWriter;

import org.codehaus.aspectwerkz.org.objectweb.asm.ClassWriter;
import org.codehaus.aspectwerkz.org.objectweb.asm.CodeVisitor;

/**
 * TODO support ConstructorInvocation (1h work) (plus tests)
 * <p/>
 * Implementation of the AspectModel interface for AOP Alliance based frameworks (e.g. Spring, dynaop etc.).
 * <p/>
 * Provides methods for definition of aspects and framework specific bytecode generation
 * used by the join point compiler.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class AopAllianceAspectModel implements AspectModel, TransformationConstants {

    private static final String ASPECT_MODEL_TYPE = "aop-alliance";
    private static final String AOP_ALLIANCE_AROUND_CLOSURE_CLASS_NAME = "org/aopalliance/intercept/MethodInvocation";
    private static final String AOP_ALLIANCE_AROUND_CLOSURE_RUN_METHOD_NAME = "invoke";
    private static final String AOP_ALLIANCE_AROUND_CLOSURE_RUN_METHOD_SIGNATURE = "(Lorg/aopalliance/intercept/MethodInvocation;)Ljava/lang/Object;";
    private static final String AOP_ALLIANCE_ASPECT_CONTAINER_CLASS_NAME = AopAllianceAspectContainer.class.getName();
    private static final String AOP_ALLIANCE_GET_METHOD_METHOD_NAME = "getMethod";
    private static final String AOP_ALLIANCE_GET_METHOD_METHOD_SIGNATURE = "()Ljava/lang/reflect/Method;";
    private static final String AOP_ALLIANCE_GET_STATIC_PART_METHOD_NAME = "getStaticPart";
    private static final String AOP_ALLIANCE_GET_STATIC_PART_METHOD_SIGNATURE = "()Ljava/lang/reflect/AccessibleObject;";
    private static final String GET_PARAMETER_VALUES_METHOD_NAME = "getParameterValues";
    private static final String AOP_ALLIANCE_GET_ARGUMENTS_METHOD_SIGNATURE = "()[Ljava/lang/Object;";
    private static final String AOP_ALLIANCE_GET_ARGUMENTS_METHOD_NAME = "getArguments";


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
                anInterface.getName().equals(ConstructorInterceptor.class.getName())) {
                aspectDef.setAspectModel(ASPECT_MODEL_TYPE);
                aspectDef.setContainerClassName(AOP_ALLIANCE_ASPECT_CONTAINER_CLASS_NAME);
                return;
            }
        }
    }

    /**
     * AOP Alliance is a reflection based model and therefore in need of RTTI info: returns true.
     *
     * @return true
     */
    public boolean requiresRttiInfo() {
        return true;
    }

    /**
     * Returns info about the closure class, name and type (interface or class).
     *
     * @return the closure class info
     */
    public AroundClosureClassInfo getAroundClosureClassInfo() {
        return new AspectModel.AroundClosureClassInfo(
                AOP_ALLIANCE_AROUND_CLOSURE_CLASS_NAME, AspectModel.AroundClosureClassInfo.INTERFACE
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
        CodeVisitor cv;

        // invoke
        {
            cv = cw.visitMethod(
                    ACC_PUBLIC,
                    AOP_ALLIANCE_AROUND_CLOSURE_RUN_METHOD_NAME,
                    AOP_ALLIANCE_AROUND_CLOSURE_RUN_METHOD_SIGNATURE,
                    new String[]{THROWABLE_CLASS_NAME},
                    null
            );
            cv.visitVarInsn(ALOAD, 0);
            cv.visitMethodInsn(INVOKEVIRTUAL, className, PROCEED_METHOD_NAME, PROCEED_METHOD_SIGNATURE);
            cv.visitInsn(ARETURN);
            cv.visitMaxs(0, 0);
        }

        // getStaticPart
        {
            cv = cw.visitMethod(
                    ACC_PUBLIC,
                    AOP_ALLIANCE_GET_STATIC_PART_METHOD_NAME,
                    AOP_ALLIANCE_GET_STATIC_PART_METHOD_SIGNATURE,
                    null, null
            );
            cv.visitFieldInsn(
                    GETSTATIC, className, SIGNATURE_FIELD_NAME, METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
            );
            cv.visitTypeInsn(CHECKCAST, METHOD_SIGNATURE_IMPL_CLASS_NAME);
            cv.visitMethodInsn(
                    INVOKEVIRTUAL, METHOD_SIGNATURE_IMPL_CLASS_NAME, AOP_ALLIANCE_GET_METHOD_METHOD_NAME,
                    AOP_ALLIANCE_GET_METHOD_METHOD_SIGNATURE
            );
            cv.visitInsn(ARETURN);
            cv.visitMaxs(1, 1);
        }

        // getMethod
        {
            cv =
            cw.visitMethod(
                    ACC_PUBLIC,
                    AOP_ALLIANCE_GET_METHOD_METHOD_NAME,
                    AOP_ALLIANCE_GET_METHOD_METHOD_SIGNATURE,
                    null, null);
            cv.visitFieldInsn(
                    GETSTATIC, className, SIGNATURE_FIELD_NAME, METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
            );
            cv.visitTypeInsn(CHECKCAST, METHOD_SIGNATURE_IMPL_CLASS_NAME);
            cv.visitMethodInsn(
                    INVOKEVIRTUAL, METHOD_SIGNATURE_IMPL_CLASS_NAME, AOP_ALLIANCE_GET_METHOD_METHOD_NAME,
                    AOP_ALLIANCE_GET_METHOD_METHOD_SIGNATURE
            );
            cv.visitInsn(ARETURN);
            cv.visitMaxs(1, 1);
        }

        // getArguments
        {
            cv = cw.visitMethod(
                    ACC_PUBLIC,
                    AOP_ALLIANCE_GET_ARGUMENTS_METHOD_NAME,
                    AOP_ALLIANCE_GET_ARGUMENTS_METHOD_SIGNATURE,
                    null, null
            );
            cv.visitVarInsn(ALOAD, 0);
            cv.visitMethodInsn(INVOKESPECIAL, className, GET_RTTI_METHOD_NAME, GET_RTTI_METHOD_SIGNATURE);
            cv.visitTypeInsn(CHECKCAST, METHOD_RTTI_IMPL_CLASS_NAME);
            cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    METHOD_RTTI_IMPL_CLASS_NAME,
                    GET_PARAMETER_VALUES_METHOD_NAME,
                    AOP_ALLIANCE_GET_ARGUMENTS_METHOD_SIGNATURE
            );
            cv.visitInsn(ARETURN);
            cv.visitMaxs(1, 1);
        }

    }

    /**
     * Creates an invocation of the around closure class' constructor.
     *
     * @param cv
     */
    public void createInitAroundClosureSuperClass(final CodeVisitor cv) {
    }

    /**
     * Creates instantiation of the aspectj aspect instance by invoking aspectOf().
     *
     * @param cv
     * @param aspectInfo
     * @param joinPointClassName
     */
    public void createAspectInstantiation(final CodeVisitor cv,
                                          final AspectInfo aspectInfo,
                                          final String joinPointClassName) {
        AbstractJoinPointCompiler.createAspectInstantiation(cv, aspectInfo, joinPointClassName);
    }

    /**
     * Handles the arguments to the before advice.
     *
     * @param cv
     * @param adviceMethodInfo
     */
    public void createBeforeAdviceArgumentHandling(final CodeVisitor cv, final AdviceMethodInfo adviceMethodInfo) {
    }

    /**
     * Handles the arguments to the after advice.
     *
     * @param cv
     * @param adviceMethodInfo
     */
    public void createAfterAdviceArgumentHandling(final CodeVisitor cv, final AdviceMethodInfo adviceMethodInfo) {
    }
}

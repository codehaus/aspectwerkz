/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.AdviceInfo;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.aspect.management.AspectRegistry;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaConstructorInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaFieldInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaMethodInfo;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Runtime (Just-In-Time/JIT) compiler. <p/>Compiles a custom JoinPoint class that invokes all advices in a specific
 * advice chain (at a specific join point) and the target join point statically.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class JitCompiler {
    private static final List EMTPTY_ARRAY_LIST = new ArrayList();

    private static final String JIT_CLASS_PREFIX = "org/codehaus/aspectwerkz/joinpoint/management/___AW_JP_";

    private static final String STACKFRAME_FIELD_NAME = "m_stackFrame";

    private static final String SIGNATURE_FIELD_NAME = "m_signature";

    private static final String RTTI_FIELD_NAME = "m_rtti";

    private static final String SYSTEM_FIELD_NAME = "m_system";

    private static final String TARGET_INSTANCE_FIELD_NAME = "m_targetInstanceRef";

    private static final String TARGET_CLASS_FIELD_NAME = "m_targetClass";

    private static final String AROUND_ADVICE_FIELD_PREFIX = "m_around";

    private static final String BEFORE_ADVICE_FIELD_PREFIX = "m_before";

    private static final String AFTER_ADVICE_FIELD_PREFIX = "m_after";

    private static final String SHORT_CLASS_NAME = "java/lang/Short";

    private static final String INTEGER_CLASS_NAME = "java/lang/Integer";

    private static final String LONG_CLASS_NAME = "java/lang/Long";

    private static final String FLOAT_CLASS_NAME = "java/lang/Float";

    private static final String DOUBLE_CLASS_NAME = "java/lang/Double";

    private static final String BYTE_CLASS_NAME = "java/lang/Byte";

    private static final String BOOLEAN_CLASS_NAME = "java/lang/Boolean";

    private static final String CHARACTER_CLASS_NAME = "java/lang/Character";

    private static final String OBJECT_CLASS_SIGNATURE = "Ljava/lang/Object;";

    private static final String CLASS_CLASS_SIGNATURE = "Ljava/lang/Class;";

    private static final String JOIN_POINT_BASE_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/management/JoinPointBase";

    private static final String SYSTEM_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/AspectSystem;";

    private static final String SYSTEM_CLASS_NAME = "org/codehaus/aspectwerkz/AspectSystem";

    private static final String ASPECT_MANAGER_CLASS_NAME = "org/codehaus/aspectwerkz/aspect/management/AspectManager";

    private static final String ASPECT_CONTAINER_CLASS_NAME = "org/codehaus/aspectwerkz/aspect/AspectContainer";

    private static final String THROWABLE_CLASS_NAME = "java/lang/Throwable";

    private static final String AROUND_ADVICE_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;";

    private static final String BEFORE_ADVICE_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)V";

    private static final String AFTER_ADVICE_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)V";

    private static final String JOIN_POINT_BASE_INIT_METHOD_SIGNATURE = "(Ljava/lang/String;ILjava/lang/Class;Ljava/util/List;Lorg/codehaus/aspectwerkz/expression/ExpressionContext;Lorg/codehaus/aspectwerkz/joinpoint/management/AroundAdviceExecutor;Lorg/codehaus/aspectwerkz/joinpoint/management/BeforeAdviceExecutor;Lorg/codehaus/aspectwerkz/joinpoint/management/AfterAdviceExecutor;)V";

    private static final String JIT_JOIN_POINT_INIT_METHOD_SIGNATURE = "(Ljava/lang/String;ILjava/lang/Class;Lorg/codehaus/aspectwerkz/joinpoint/Signature;Lorg/codehaus/aspectwerkz/joinpoint/Rtti;Ljava/util/List;Lorg/codehaus/aspectwerkz/expression/ExpressionContext;)V";

    private static final String SYSTEM_LOADER_CLASS_NAME = "org/codehaus/aspectwerkz/SystemLoader";

    private static final String INIT_METHOD_NAME = "<init>";

    private static final String GET_SYSTEM_METHOD_NAME = "getSystem";

    private static final String GET_SYSTEM_METHOD_NAME_SIGNATURE = "(Ljava/lang/Class;)Lorg/codehaus/aspectwerkz/AspectSystem;";

    private static final String GET_ASPECT_MANAGER_METHOD_NAME = "getAspectManager";

    private static final String GET_ASPECT_MANAGER_METHOD_NAME_SIGNATURE = "(Ljava/lang/String;)Lorg/codehaus/aspectwerkz/aspect/management/AspectManager;";

    private static final String GET_ASPECT_CONTAINER_METHOD_NAME = "getAspectContainer";

    private static final String GET_ASPECT_METHOD_SIGNATURE = "(I)Lorg/codehaus/aspectwerkz/aspect/AspectContainer;";

    private static final String SHORT_VALUE_METHOD_NAME = "shortValue";

    private static final String INT_VALUE_METHOD_NAME = "intValue";

    private static final String LONG_VALUE_METHOD_NAME = "longValue";

    private static final String FLOAT_VALUE_METHOD_NAME = "floatValue";

    private static final String DOUBLE_VALUE_METHOD_NAME = "doubleValue";

    private static final String BYTE_VALUE_METHOD_NAME = "byteValue";

    private static final String BOOLEAN_VALUE_METHOD_NAME = "booleanValue";

    private static final String CHAR_VALUE_METHOD_NAME = "charValue";

    private static final String CHAR_VALUE_METHOD_SIGNATURE = "()C";

    private static final String BOOLEAN_VALUE_METHOD_SIGNATURE = "()Z";

    private static final String BYTE_VALUE_METHOD_SIGNATURE = "()B";

    private static final String DOUBLE_VALUE_METHOD_SIGNATURE = "()D";

    private static final String FLOAT_VALUE_METHOD_SIGNATURE = "()F";

    private static final String LONG_VALUE_METHOD_SIGNATURE = "()J";

    private static final String INT_VALUE_METHOD_SIGNATURE = "()I";

    private static final String SHORT_VALUE_METHOD_SIGNATURE = "()S";

    private static final String SHORT_CLASS_INIT_METHOD_SIGNATURE = "(S)V";

    private static final String INTEGER_CLASS_INIT_METHOD_SIGNATURE = "(I)V";

    private static final String LONG_CLASS_INIT_METHOD_SIGNATURE = "(J)V";

    private static final String FLOAT_CLASS_INIT_METHOD_SIGNATURE = "(F)V";

    private static final String DOUBLE_CLASS_INIT_METHOD_SIGNATURE = "(D)V";

    private static final String BYTE_CLASS_INIT_METHOD_SIGNATURE = "(B)V";

    private static final String BOOLEAN_CLASS_INIT_METHOD_SIGNATURE = "(Z)V";

    private static final String CHARACTER_CLASS_INIT_METHOD_SIGNATURE = "(C)V";

    private static final String GET_PER_JVM_ASPECT_METHOD_NAME = "createPerJvmAspect";

    private static final String GET_PER_JVM_ASPECT_METHOD_SIGNATURE = "()Ljava/lang/Object;";

    private static final String GET_PER_CLASS_ASPECT_METHOD_NAME = "createPerClassAspect";

    private static final String GET_PER_CLASS_ASPECT_METHOD_SIGNATURE = "(Ljava/lang/Class;)Ljava/lang/Object;";

    private static final String GET_SIGNATURE_METHOD_NAME = "getSignature";

    private static final String GET_SIGNATURE_METHOD_SIGNATURE = "()Lorg/codehaus/aspectwerkz/joinpoint/Signature;";

    private static final String GET_RTTI_METHOD_NAME = "getRtti";

    private static final String GET_RTTI_METHOD_SIGNATURE = "()Lorg/codehaus/aspectwerkz/joinpoint/Rtti;";

    private static final String PROCEED_METHOD_NAME = "proceed";

    private static final String PROCEED_METHOD_SIGNATURE = "()Ljava/lang/Object;";

    private static final String GET_PARAMETER_VALUES_METHOD_NAME = "getParameterValues";

    private static final String GET_PARAMETER_VALUES_METHOD_SIGNATURE = "()[Ljava/lang/Object;";

    private static final String INVOKE_TARGET_METHOD_EXECUTION_METHOD_NAME = "invokeTargetMethodExecution";

    private static final String INVOKE_TARGET_METHOD_CALL_METHOD_NAME = "invokeTargetMethodCall";

    private static final String INVOKE_TARGET_METHOD_EXECUTION_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;";

    private static final String INVOKE_TARGET_METHOD_CALL_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;";

    private static final String INVOKE_TARGET_CONSTRUCTOR_EXECUTION_METHOD_NAME = "invokeTargetConstructorExecution";

    private static final String INVOKE_TARGET_CONSTRUCTOR_EXECUTION_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;";

    private static final String INVOKE_TARGET_CONSTRUCTOR_CALL_METHOD_NAME = "invokeTargetConstructorCall";

    private static final String INVOKE_TARGET_CONSTRUCTOR_CALL_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;";

    private static final String GET_TARGET_FIELD_METHOD_NAME = "getTargetField";

    private static final String GET_TARGET_FIELD_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;";

    private static final String SET_TARGET_FIELD_METHOD_NAME = "setTargetField";

    private static final String SET_TARGET_FIELD_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)V";

    private static final String METHOD_SIGNATURE_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/MethodSignatureImpl";

    private static final String CONSTRUCTOR_SIGNATURE_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/ConstructorSignatureImpl";

    private static final String FIELD_SIGNATURE_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/FieldSignatureImpl";

    private static final String METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/MethodSignatureImpl;";

    private static final String CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/ConstructorSignatureImpl;";

    private static final String FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/FieldSignatureImpl;";

    private static final String METHOD_RTTI_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/MethodRttiImpl";

    private static final String CONSTRUCTOR_RTTI_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/ConstructorRttiImpl";

    private static final String FIELD_RTTI_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/FieldRttiImpl";

    private static final String METHOD_RTTI_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/MethodRttiImpl;";

    private static final String CONSTRUCTOR_RTTI_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/ConstructorRttiImpl;";

    private static final String FIELD_RTTI_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/FieldRttiImpl;";

    private static final String SET_RETURN_VALUE_METHOD_NAME = "setReturnValue";

    private static final String SET_RETURN_VALUE_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";

    private static final String SET_NEW_INSTANCE_METHOD_NAME = "setNewInstance";

    private static final String SET_NEW_INSTANCE_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";

    private static final String SET_FIELD_VALUE_METHOD_NAME = "setFieldValue";

    private static final String SET_FIELD_VALUE_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";

    private static final String GET_FIELD_VALUE_METHOD_NAME = "getFieldValue";

    private static final String GET_FIELD_VALUE_METHOD_SIGNATURE = "()Ljava/lang/Object;";

    private static final String IS_IN_CFLOW_METOD_NAME = "isInCflow";

    private static final String IS_IN_CFLOW_METOD_SIGNATURE = "()Z";

    private static final String L = "L";

    private static final String I = "I";

    private static final String SEMICOLON = ";";

    private static final String WEAK_REFERENCE_CLASS_SIGNATURE = "Ljava/lang/ref/WeakReference;";

    private static final String WEAK_REFERENCE_CLASS_NAME = "java/lang/ref/WeakReference";

    private static final String WEAK_REFERENCE_GET_METHOD_NAME = "get";

    private static final String WEAK_REFERENCE_GET_METHOD_SIGNATURE = "()Ljava/lang/Object;";

    /**
     * Private constructor to prevent instantiation.
     */
    private JitCompiler() {
    }

    /**
     * Compiles a join point class on the fly that invokes the advice chain and the target join point statically.
     * 
     * @param joinPointHash the join point hash
     * @param joinPointType the join point joinPointType
     * @param pointcutType the pointcut type
     * @param advice a list with the advice
     * @param declaringClass the declaring class
     * @param targetClass the currently executing class
     * @param system the system
     * @param thisInstance
     * @param targetInstance
     * @param hotswapCount
     * @return the JIT compiled join point
     */
    public static JoinPoint compileJoinPoint(
        final int joinPointHash,
        final int joinPointType,
        final PointcutType pointcutType,
        final AdviceIndexInfo[] advice,
        final Class declaringClass,
        final Class targetClass,
        final AspectSystem system,
        final Object thisInstance,
        final Object targetInstance,
        final int hotswapCount) {
        try {
            if (pointcutType.equals(PointcutType.HANDLER)) { // TODO: fix handler pointcuts
                return null;
            }
            AdviceInfo[] aroundAdvice = JoinPointManager.extractAroundAdvices(advice);
            AdviceInfo[] beforeAdvice = JoinPointManager.extractBeforeAdvices(advice);
            AdviceInfo[] afterAdvice = JoinPointManager.extractAfterFinallyAdvices(advice);
            if ((aroundAdvice.length == 0) && (beforeAdvice.length == 0) && (afterAdvice.length == 0)) {
                return null; // no advice => bail out
            }
            RttiInfo rttiInfo = setRttiInfo(
                joinPointType,
                joinPointHash,
                declaringClass,
                system,
                targetInstance,
                targetInstance,
                targetClass);
            StringBuffer buf = new StringBuffer();
            buf.append(JIT_CLASS_PREFIX);
            buf.append(pointcutType.toString());
            buf.append('_');
            buf.append(targetClass.getName());
            buf.append('_');
            buf.append(declaringClass.getName());
            buf.append('_');
            buf.append(new Integer(joinPointHash).toString());
            buf.append('_');
            buf.append(system.getDefiningClassLoader().hashCode());
            buf.append('_');
            buf.append(hotswapCount);
            final String className = buf.toString().replace('.', '_').replace('-', '_');

            // use the loader that loaded the target class
            ClassLoader loader = targetClass.getClassLoader();

            // try to load the class without generating it
            // AW-160, AW-163
            Class joinPointClass = AsmHelper.loadClass(loader, className);

            if (joinPointClass == null) {
                final ClassWriter cw = AsmHelper.newClassWriter(true);

                cw.visit(
                        AsmHelper.JAVA_VERSION,
                        Constants.ACC_PUBLIC + Constants.ACC_SUPER,
                        className,
                        JOIN_POINT_BASE_CLASS_NAME,
                        null, null
                );

                createMemberFields(joinPointType, cw, className);
                if (createInitMethod(joinPointType, cw, className, aroundAdvice, beforeAdvice, afterAdvice)) {
                    return null; // bail out, one of the advice has deployment model that is not
                    // supported, use regular
                    // join point instance
                }
                createGetSignatureMethod(joinPointType, cw, className);
                createGetRttiMethod(joinPointType, cw, className);
                createSetRttiMethod(joinPointType, cw, className);
                createProceedMethod(
                    joinPointType,
                    cw,
                    className,
                    declaringClass,
                    joinPointHash,
                    rttiInfo,
                    aroundAdvice,
                    beforeAdvice,
                    afterAdvice);
                cw.visitEnd();

                // FIXME: should be a VM option
                 AsmHelper.dumpClass("_dump", className, cw);

                // load the generated class
                joinPointClass = AsmHelper.loadClass(loader, cw.toByteArray(), className);
            }

            // create the generated class
            Constructor constructor = joinPointClass.getDeclaredConstructor(new Class[] {
                String.class, int.class, Class.class, Signature.class, Rtti.class, List.class, ExpressionContext.class
            });
            return (JoinPoint) constructor.newInstance(new Object[] {
                "___AW_JIT_COMPILED",
                new Integer(joinPointType),
                declaringClass,
                rttiInfo.signature,
                rttiInfo.rtti,
                rttiInfo.cflowExpressions,
                rttiInfo.expressionContext
            });
        } catch (Throwable e) {
            e.printStackTrace();
            StringBuffer buf = new StringBuffer();
            buf
                    .append("WARNING: could not dynamically create, compile and load a JoinPoint class for join point with hash [");
            buf.append(joinPointHash);
            buf.append("] with target class [");
            buf.append(targetClass);
            buf.append("]: ");
            if (e instanceof InvocationTargetException) {
                buf.append(((InvocationTargetException) e).getTargetException().toString());
            } else {
                buf.append(e.toString());
            }
            java.lang.System.err.println(buf.toString());
            return null; // bail out, no JIT compilation, use regular join point instance
        }
    }

    /**
     * Creates some member fields needed.
     * 
     * @param joinPointType
     * @param cw
     * @param className
     */
    private static void createMemberFields(final int joinPointType, final ClassWriter cw, final String className) {
        cw.visitField(Constants.ACC_PRIVATE, STACKFRAME_FIELD_NAME, I, null, null);
        cw.visitField(Constants.ACC_PRIVATE, SYSTEM_FIELD_NAME, SYSTEM_CLASS_SIGNATURE, null, null);
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                cw.visitField(
                    Constants.ACC_PRIVATE,
                    SIGNATURE_FIELD_NAME,
                    METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE,
                    null,
                    null);
                cw.visitField(Constants.ACC_PRIVATE, RTTI_FIELD_NAME, METHOD_RTTI_IMPL_CLASS_SIGNATURE, null, null);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                cw.visitField(
                    Constants.ACC_PRIVATE,
                    SIGNATURE_FIELD_NAME,
                    CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE,
                    null,
                    null);
                cw
                        .visitField(
                            Constants.ACC_PRIVATE,
                            RTTI_FIELD_NAME,
                            CONSTRUCTOR_RTTI_IMPL_CLASS_SIGNATURE,
                            null,
                            null);
                break;
            case JoinPointType.FIELD_SET:
            case JoinPointType.FIELD_GET:
                cw.visitField(
                    Constants.ACC_PRIVATE,
                    SIGNATURE_FIELD_NAME,
                    FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE,
                    null,
                    null);
                cw.visitField(Constants.ACC_PRIVATE, RTTI_FIELD_NAME, FIELD_RTTI_IMPL_CLASS_SIGNATURE, null, null);
                break;
            case JoinPointType.HANDLER:
                throw new UnsupportedOperationException("handler is not support yet");
            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");
        }
    }

    /**
     * Creates an init method for the JIT join point.
     * 
     * @param joinPointType
     * @param cw
     * @param className
     * @param aroundAdvices
     * @param beforeAdvices
     * @param afterAdvices
     * @return true if the JIT compilation should be skipped
     */
    private static boolean createInitMethod(
        final int joinPointType,
        final ClassWriter cw,
        final String className,
        final AdviceInfo[] aroundAdvices,
        final AdviceInfo[] beforeAdvices,
        final AdviceInfo[] afterAdvices) {
        CodeVisitor cv = cw.visitMethod(
            Constants.ACC_PUBLIC,
            INIT_METHOD_NAME,
            JIT_JOIN_POINT_INIT_METHOD_SIGNATURE,
            null,
            null);
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitVarInsn(Constants.ALOAD, 1);
        cv.visitVarInsn(Constants.ILOAD, 2);
        cv.visitVarInsn(Constants.ALOAD, 3);
        cv.visitVarInsn(Constants.ALOAD, 6);
        cv.visitVarInsn(Constants.ALOAD, 7);
        cv.visitInsn(Constants.ACONST_NULL);
        cv.visitInsn(Constants.ACONST_NULL);
        cv.visitInsn(Constants.ACONST_NULL);
        cv.visitMethodInsn(
            Constants.INVOKESPECIAL,
            JOIN_POINT_BASE_CLASS_NAME,
            INIT_METHOD_NAME,
            JOIN_POINT_BASE_INIT_METHOD_SIGNATURE);

        // init the stack frame field
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitInsn(Constants.ICONST_M1);
        cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);

        // init the signature field
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitVarInsn(Constants.ALOAD, 4);
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                cv.visitTypeInsn(Constants.CHECKCAST, METHOD_SIGNATURE_IMPL_CLASS_NAME);
                cv.visitFieldInsn(
                    Constants.PUTFIELD,
                    className,
                    SIGNATURE_FIELD_NAME,
                    METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                cv.visitTypeInsn(Constants.CHECKCAST, CONSTRUCTOR_SIGNATURE_IMPL_CLASS_NAME);
                cv.visitFieldInsn(
                    Constants.PUTFIELD,
                    className,
                    SIGNATURE_FIELD_NAME,
                    CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.FIELD_SET:
            case JoinPointType.FIELD_GET:
                cv.visitTypeInsn(Constants.CHECKCAST, FIELD_SIGNATURE_IMPL_CLASS_NAME);
                cv.visitFieldInsn(
                    Constants.PUTFIELD,
                    className,
                    SIGNATURE_FIELD_NAME,
                    FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.HANDLER:
                throw new UnsupportedOperationException("handler is not support yet");
            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");
        }

        // init the rtti field
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitVarInsn(Constants.ALOAD, 5);
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                cv.visitTypeInsn(Constants.CHECKCAST, METHOD_RTTI_IMPL_CLASS_NAME);
                cv.visitFieldInsn(Constants.PUTFIELD, className, RTTI_FIELD_NAME, METHOD_RTTI_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                cv.visitTypeInsn(Constants.CHECKCAST, CONSTRUCTOR_RTTI_IMPL_CLASS_NAME);
                cv
                        .visitFieldInsn(
                            Constants.PUTFIELD,
                            className,
                            RTTI_FIELD_NAME,
                            CONSTRUCTOR_RTTI_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.FIELD_SET:
            case JoinPointType.FIELD_GET:
                cv.visitTypeInsn(Constants.CHECKCAST, FIELD_RTTI_IMPL_CLASS_NAME);
                cv.visitFieldInsn(Constants.PUTFIELD, className, RTTI_FIELD_NAME, FIELD_RTTI_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.HANDLER:
                throw new UnsupportedOperationException("handler is not support yet");
            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");
        }

        // init the system field
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitVarInsn(Constants.ALOAD, 3);
        cv.visitMethodInsn(
            Constants.INVOKESTATIC,
            SYSTEM_LOADER_CLASS_NAME,
            GET_SYSTEM_METHOD_NAME,
            GET_SYSTEM_METHOD_NAME_SIGNATURE);
        cv.visitFieldInsn(Constants.PUTFIELD, className, SYSTEM_FIELD_NAME, SYSTEM_CLASS_SIGNATURE);

        // init the aspect fields
        for (int i = 0; i < aroundAdvices.length; i++) {
            if (initAspectField(aroundAdvices[i], cw, AROUND_ADVICE_FIELD_PREFIX + i, cv, className)) {
                return true;
            }
        }
        for (int i = 0; i < beforeAdvices.length; i++) {
            if (initAspectField(beforeAdvices[i], cw, BEFORE_ADVICE_FIELD_PREFIX + i, cv, className)) {
                return true;
            }
        }
        for (int i = 0; i < afterAdvices.length; i++) {
            if (initAspectField(afterAdvices[i], cw, AFTER_ADVICE_FIELD_PREFIX + i, cv, className)) {
                return true;
            }
        }
        cv.visitInsn(Constants.RETURN);
        cv.visitMaxs(0, 0);
        return false;
    }

    /**
     * Create and initialize the aspect field for a specific advice.
     * 
     * @param adviceTuple
     * @param cw
     * @param aspectFieldName
     * @param cv
     * @param className
     */
    private static boolean initAspectField(
        final AdviceInfo adviceTuple,
        final ClassWriter cw,
        final String aspectFieldName,
        final CodeVisitor cv,
        final String className) {
        final CrossCuttingInfo info = adviceTuple.getAspectManager().getAspectContainer(adviceTuple.getAspectIndex())
                .getCrossCuttingInfo();
        final String aspectClassName = info.getAspectClass().getName().replace('.', '/');
        final String aspectClassSignature = L + aspectClassName + SEMICOLON;

        // add the aspect field
        cw.visitField(Constants.ACC_PRIVATE, aspectFieldName, aspectClassSignature, null, null);

        // handle the init in the constructor
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitFieldInsn(Constants.GETFIELD, className, SYSTEM_FIELD_NAME, SYSTEM_CLASS_SIGNATURE);
        cv.visitLdcInsn(adviceTuple.getAspectManager().getUuid());
        cv.visitMethodInsn(
            Constants.INVOKEVIRTUAL,
            SYSTEM_CLASS_NAME,
            GET_ASPECT_MANAGER_METHOD_NAME,
            GET_ASPECT_MANAGER_METHOD_NAME_SIGNATURE);
        cv.visitIntInsn(Constants.BIPUSH, adviceTuple.getAspectIndex());
        cv.visitMethodInsn(
            Constants.INVOKEVIRTUAL,
            ASPECT_MANAGER_CLASS_NAME,
            GET_ASPECT_CONTAINER_METHOD_NAME,
            GET_ASPECT_METHOD_SIGNATURE);
        switch (info.getDeploymentModel()) {
            case DeploymentModel.PER_JVM:
                cv.visitMethodInsn(
                    Constants.INVOKEINTERFACE,
                    ASPECT_CONTAINER_CLASS_NAME,
                    GET_PER_JVM_ASPECT_METHOD_NAME,
                    GET_PER_JVM_ASPECT_METHOD_SIGNATURE);
                break;
            case DeploymentModel.PER_CLASS:
                cv.visitVarInsn(Constants.ALOAD, 0);
                cv.visitFieldInsn(Constants.GETFIELD, className, TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
                cv.visitMethodInsn(
                    Constants.INVOKEINTERFACE,
                    ASPECT_CONTAINER_CLASS_NAME,
                    GET_PER_CLASS_ASPECT_METHOD_NAME,
                    GET_PER_CLASS_ASPECT_METHOD_SIGNATURE);
                break;
            default:
                return true;
        }
        cv.visitTypeInsn(Constants.CHECKCAST, aspectClassName);
        cv.visitFieldInsn(Constants.PUTFIELD, className, aspectFieldName, aspectClassSignature);
        return false;
    }

    /**
     * Creates a new getSignature method.
     * 
     * @param joinPointType
     * @param cw
     * @param className
     */
    private static void createGetSignatureMethod(final int joinPointType, final ClassWriter cw, final String className) {
        CodeVisitor cv = cw.visitMethod(
            Constants.ACC_PUBLIC,
            GET_SIGNATURE_METHOD_NAME,
            GET_SIGNATURE_METHOD_SIGNATURE,
            null,
            null);
        cv.visitVarInsn(Constants.ALOAD, 0);
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                cv.visitFieldInsn(
                    Constants.GETFIELD,
                    className,
                    SIGNATURE_FIELD_NAME,
                    METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                cv.visitFieldInsn(
                    Constants.GETFIELD,
                    className,
                    SIGNATURE_FIELD_NAME,
                    CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.FIELD_SET:
            case JoinPointType.FIELD_GET:
                cv.visitFieldInsn(
                    Constants.GETFIELD,
                    className,
                    SIGNATURE_FIELD_NAME,
                    FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.HANDLER:
                throw new UnsupportedOperationException("handler is not support yet");
            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");
        }
        cv.visitInsn(Constants.ARETURN);
        cv.visitMaxs(0, 0);
    }

    /**
     * Creates a new getRtti method.
     * 
     * @param joinPointType
     * @param cw
     * @param className
     */
    private static void createGetRttiMethod(final int joinPointType, final ClassWriter cw, final String className) {
        CodeVisitor cv = cw.visitMethod(
            Constants.ACC_PUBLIC,
            GET_RTTI_METHOD_NAME,
            GET_RTTI_METHOD_SIGNATURE,
            null,
            null);
        cv.visitVarInsn(Constants.ALOAD, 0);
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                cv.visitFieldInsn(Constants.GETFIELD, className, RTTI_FIELD_NAME, METHOD_RTTI_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                cv
                        .visitFieldInsn(
                            Constants.GETFIELD,
                            className,
                            RTTI_FIELD_NAME,
                            CONSTRUCTOR_RTTI_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.FIELD_SET:
            case JoinPointType.FIELD_GET:
                cv.visitFieldInsn(Constants.GETFIELD, className, RTTI_FIELD_NAME, FIELD_RTTI_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.HANDLER:
                throw new UnsupportedOperationException("handler is not support yet");
            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");
        }
        cv.visitInsn(Constants.ARETURN);
        cv.visitMaxs(0, 0);
    }

    /**
     * Creates a new setRtti method for rtti management
     *
     * @param joinPointType
     * @param cw
     * @param className
     */
    private static void createSetRttiMethod(final int joinPointType, final ClassWriter cw, final String className) {
        CodeVisitor cv = cw.visitMethod(
            Constants.ACC_PUBLIC,
            "setRtti",
            "(Lorg/codehaus/aspectwerkz/joinpoint/Rtti;)V",
            null,
            null);
        cv.visitVarInsn(Constants.ALOAD, 0);//this
        cv.visitVarInsn(Constants.ALOAD, 1);//rtti arg
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                cv.visitTypeInsn(Constants.CHECKCAST, METHOD_RTTI_IMPL_CLASS_NAME);
                cv.visitFieldInsn(Constants.PUTFIELD, className, RTTI_FIELD_NAME, METHOD_RTTI_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                cv.visitTypeInsn(Constants.CHECKCAST, CONSTRUCTOR_RTTI_IMPL_CLASS_NAME);
                cv
                        .visitFieldInsn(
                            Constants.PUTFIELD,
                            className,
                            RTTI_FIELD_NAME,
                            CONSTRUCTOR_RTTI_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.FIELD_SET:
            case JoinPointType.FIELD_GET:
                cv.visitTypeInsn(Constants.CHECKCAST, FIELD_RTTI_IMPL_CLASS_NAME);
                cv.visitFieldInsn(Constants.PUTFIELD, className, RTTI_FIELD_NAME, FIELD_RTTI_IMPL_CLASS_SIGNATURE);
                break;
            case JoinPointType.HANDLER:
                throw new UnsupportedOperationException("handler is not support yet");
            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");
        }
        cv.visitInsn(Constants.RETURN);
        cv.visitMaxs(0, 0);
    }

    /**
     * Create the proceed() method.
     * 
     * @param joinPointType
     * @param cw
     * @param className
     * @param declaringClass
     * @param joinPointHash
     * @param signatureCflowExprStruct
     * @param aroundAdvice
     * @param beforeAdvice
     * @param afterAdvice
     */
    private static void createProceedMethod(
        final int joinPointType,
        final ClassWriter cw,
        final String className,
        final Class declaringClass,
        final int joinPointHash,
        final RttiInfo signatureCflowExprStruct,
        final AdviceInfo[] aroundAdvice,
        final AdviceInfo[] beforeAdvice,
        final AdviceInfo[] afterAdvice) {
        CodeVisitor cv = cw.visitMethod(
            Constants.ACC_PUBLIC | Constants.ACC_FINAL,
            PROCEED_METHOD_NAME,
            PROCEED_METHOD_SIGNATURE,
            new String[] {
                THROWABLE_CLASS_NAME
            },
            null);
        incrementStackFrameCounter(cv, className);
        Labels labels = invokeAdvice(cv, className, aroundAdvice, beforeAdvice, afterAdvice, signatureCflowExprStruct, joinPointType);
        resetStackFrameCounter(cv, className);
        invokeJoinPoint(joinPointType, declaringClass, joinPointHash, cv, className);
        cv.visitInsn(Constants.ARETURN);
        cv.visitLabel(labels.handlerLabel);
        cv.visitVarInsn(Constants.ASTORE, 2);
        cv.visitLabel(labels.endLabel);
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitInsn(Constants.ICONST_M1);
        cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);
        cv.visitVarInsn(Constants.ALOAD, 2);
        cv.visitInsn(Constants.ATHROW);

        // handle the final try-finally clause
        cv.visitTryCatchBlock(labels.startLabel, labels.returnLabels[0], labels.handlerLabel, null);
        for (int i = 1; i < labels.switchCaseLabels.length; i++) {
            Label switchCaseLabel = labels.switchCaseLabels[i];
            Label returnLabel = labels.returnLabels[i];
            cv.visitTryCatchBlock(switchCaseLabel, returnLabel, labels.handlerLabel, null);
        }
        cv.visitTryCatchBlock(labels.handlerLabel, labels.endLabel, labels.handlerLabel, null);
        cv.visitMaxs(0, 0);
    }

    /**
     * Invokes the specific join point.
     * 
     * @param joinPointType
     * @param declaringClass
     * @param joinPointHash
     * @param cv
     * @param className
     */
    private static void invokeJoinPoint(
        final int joinPointType,
        final Class declaringClass,
        final int joinPointHash,
        final CodeVisitor cv,
        final String className) {
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
                invokeMethodExecutionJoinPoint(declaringClass, joinPointHash, cv, joinPointType, className);
                break;
            case JoinPointType.METHOD_CALL:
                invokeMethodCallJoinPoint(declaringClass, joinPointHash, cv, joinPointType, className);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:

                // TODO: BUG - should invoke the wrapper ctor to make sure the it works with
                // execution pc, but it does
                // not work
                //               invokeConstructorCallJoinPoint(
                //                        system, declaringClass, joinPointHash, joinPointType, cv, className
                //                );
                ConstructorTuple constructorTuple = AspectRegistry.getConstructorTuple(declaringClass, joinPointHash);
                if (constructorTuple.getOriginalConstructor().equals(constructorTuple.getWrapperConstructor())) {
                    invokeConstructorCallJoinPoint(declaringClass, joinPointHash, joinPointType, cv, className);
                } else {
                    java.lang.System.err
                            .println("WARNING: When a constructor has both a CALL and EXECUTION join point, only the CALL will be executed. This limitation is due to a bug that has currently not been fixed yet.");
                    invokeConstrutorExecutionJoinPoint(declaringClass, joinPointHash, joinPointType, cv, className);
                }
                break;
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                invokeConstrutorExecutionJoinPoint(declaringClass, joinPointHash, joinPointType, cv, className);
                break;
            case JoinPointType.FIELD_SET:
                invokeSetFieldJoinPoint(cv, className);
                break;
            case JoinPointType.FIELD_GET:
                invokeGetFieldJoinPoint(cv, className);
                break;
            case JoinPointType.HANDLER:
                throw new UnsupportedOperationException("handler is not support yet");
            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");
        }
    }

    /**
     * Invokes a method join point - execution context.
     * 
     * @param declaringClass
     * @param joinPointHash
     * @param cv
     * @param joinPointType
     * @param className
     */
    private static void invokeMethodExecutionJoinPoint(
        final Class declaringClass,
        final int joinPointHash,
        final CodeVisitor cv,
        final int joinPointType,
        final String className) {
        MethodTuple methodTuple = AspectRegistry.getMethodTuple(declaringClass, joinPointHash);
        Method targetMethod = methodTuple.getOriginalMethod();
        String declaringClassName = targetMethod.getDeclaringClass().getName().replace('.', '/');
        String methodName = targetMethod.getName();
        String methodDescriptor = Type.getMethodDescriptor(targetMethod);
        Type[] argTypes = Type.getArgumentTypes(targetMethod);
        if (Modifier.isPublic(targetMethod.getModifiers()) && Modifier.isPublic(declaringClass.getModifiers())) {
            invokeMethod(
                targetMethod,
                cv,
                joinPointType,
                argTypes,
                className,
                declaringClassName,
                methodName,
                methodDescriptor);
        } else {
            invokeMethodExecutionReflectively(cv);
        }
        setReturnValue(targetMethod, cv, className);
    }

    /**
     * Invokes a method join point - call context.
     * 
     * @param declaringClass
     * @param joinPointHash
     * @param cv
     * @param joinPointType
     * @param className
     */
    private static void invokeMethodCallJoinPoint(
        final Class declaringClass,
        final int joinPointHash,
        final CodeVisitor cv,
        final int joinPointType,
        final String className) {
        MethodTuple methodTuple = AspectRegistry.getMethodTuple(declaringClass, joinPointHash);
        Method targetMethod = methodTuple.getWrapperMethod();
        String declaringClassName = targetMethod.getDeclaringClass().getName().replace('.', '/');
        String methodName = targetMethod.getName();
        String methodDescriptor = Type.getMethodDescriptor(targetMethod);
        Type[] argTypes = Type.getArgumentTypes(targetMethod);
        if (Modifier.isPublic(targetMethod.getModifiers()) && Modifier.isPublic(declaringClass.getModifiers())) {
            invokeMethod(
                targetMethod,
                cv,
                joinPointType,
                argTypes,
                className,
                declaringClassName,
                methodName,
                methodDescriptor);
        } else {
            invokeMethodCallReflectively(cv);
        }
        setReturnValue(targetMethod, cv, className);
    }

    /**
     * Invokes a constructor join point.
     * 
     * @param declaringClass
     * @param joinPointHash
     * @param joinPointType
     * @param cv
     * @param className
     */
    private static void invokeConstructorCallJoinPoint(
        final Class declaringClass,
        final int joinPointHash,
        final int joinPointType,
        final CodeVisitor cv,
        final String className) {
        ConstructorTuple constructorTuple = AspectRegistry.getConstructorTuple(declaringClass, joinPointHash);
        Constructor targetConstructor = constructorTuple.getWrapperConstructor();
        String declaringClassName = targetConstructor.getDeclaringClass().getName().replace('.', '/');
        String constructorDescriptor = AsmHelper.getConstructorDescriptor(targetConstructor);
        Signature signature = new ConstructorSignatureImpl(constructorTuple.getDeclaringClass(), constructorTuple);
        Type[] argTypes = AsmHelper.getArgumentTypes(targetConstructor);
        if (Modifier.isPublic(targetConstructor.getModifiers()) && Modifier.isPublic(declaringClass.getModifiers())) {
            invokeConstructorCall(joinPointType, argTypes, cv, className, declaringClassName, constructorDescriptor);
        } else {
            invokeConstructorCallReflectively(cv);
        }
        setNewInstance(cv, className);
    }

    /**
     * Invokes a constructor join point.
     * 
     * @param declaringClass
     * @param joinPointHash
     * @param joinPointType
     * @param cv
     * @param className
     */
    private static void invokeConstrutorExecutionJoinPoint(
        final Class declaringClass,
        final int joinPointHash,
        final int joinPointType,
        final CodeVisitor cv,
        final String className) {
        ConstructorTuple constructorTuple = AspectRegistry.getConstructorTuple(declaringClass, joinPointHash);
        Constructor targetConstructor = constructorTuple.getOriginalConstructor();
        String declaringClassName = targetConstructor.getDeclaringClass().getName().replace('.', '/');
        String constructorDescriptor = AsmHelper.getConstructorDescriptor(targetConstructor);
        Type[] argTypes = AsmHelper.getArgumentTypes(targetConstructor);

        // remove the last argument (the dummy JoinPointManager type)
        Type[] newArgTypes = new Type[argTypes.length - 1];
        for (int i = 0; i < newArgTypes.length; i++) {
            newArgTypes[i] = argTypes[i];
        }
        if (Modifier.isPublic(targetConstructor.getModifiers()) && Modifier.isPublic(declaringClass.getModifiers())) {
            invokeConstructorExecution(
                joinPointType,
                newArgTypes,
                cv,
                className,
                declaringClassName,
                constructorDescriptor);
        } else {
            invokeConstructorExecutionReflectively(cv);
        }
        setNewInstance(cv, className);
    }

    /**
     * Invokes set field.
     * 
     * @param cv
     * @param className
     */
    private static void invokeSetFieldJoinPoint(final CodeVisitor cv, final String className) {
        invokeTargetFieldSet(cv, className);
        setFieldValue(cv, className);
    }

    /**
     * Invokes get field.
     * 
     * @param cv
     * @param className
     */
    private static void invokeGetFieldJoinPoint(final CodeVisitor cv, final String className) {
        invokeTargetFieldGet(cv);
        setFieldValue(cv, className);
    }

    /**
     * Sets the return value.
     * 
     * @param targetMethod
     * @param cv
     * @param className
     */
    private static void setReturnValue(final Method targetMethod, final CodeVisitor cv, final String className) {
        if (Type.getReturnType(targetMethod).getSort() != Type.VOID) {
            cv.visitVarInsn(Constants.ASTORE, 1);
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitFieldInsn(Constants.GETFIELD, className, RTTI_FIELD_NAME, METHOD_RTTI_IMPL_CLASS_SIGNATURE);
            cv.visitVarInsn(Constants.ALOAD, 1);
            cv.visitMethodInsn(
                Constants.INVOKEVIRTUAL,
                METHOD_RTTI_IMPL_CLASS_NAME,
                SET_RETURN_VALUE_METHOD_NAME,
                SET_RETURN_VALUE_METHOD_SIGNATURE);
            cv.visitVarInsn(Constants.ALOAD, 1);
        }
    }

    /**
     * Sets the new instance value.
     * 
     * @param cv
     * @param className
     */
    private static void setNewInstance(final CodeVisitor cv, final String className) {
        cv.visitVarInsn(Constants.ASTORE, 1);
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitFieldInsn(Constants.GETFIELD, className, RTTI_FIELD_NAME, CONSTRUCTOR_RTTI_IMPL_CLASS_SIGNATURE);
        cv.visitVarInsn(Constants.ALOAD, 1);
        cv.visitMethodInsn(
            Constants.INVOKEVIRTUAL,
            CONSTRUCTOR_RTTI_IMPL_CLASS_NAME,
            SET_NEW_INSTANCE_METHOD_NAME,
            SET_NEW_INSTANCE_METHOD_SIGNATURE);
        cv.visitVarInsn(Constants.ALOAD, 1);
    }

    /**
     * Sets the field value.
     * 
     * @param cv
     * @param className
     */
    private static void setFieldValue(final CodeVisitor cv, final String className) {
        cv.visitVarInsn(Constants.ASTORE, 1);
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitFieldInsn(Constants.GETFIELD, className, RTTI_FIELD_NAME, FIELD_RTTI_IMPL_CLASS_SIGNATURE);
        cv.visitVarInsn(Constants.ALOAD, 1);
        cv.visitMethodInsn(
            Constants.INVOKEVIRTUAL,
            FIELD_RTTI_IMPL_CLASS_NAME,
            SET_FIELD_VALUE_METHOD_NAME,
            SET_FIELD_VALUE_METHOD_SIGNATURE);
        cv.visitVarInsn(Constants.ALOAD, 1);
    }

    /**
     * Handles invocation of a method.
     * 
     * @param targetMethod
     * @param cv
     * @param joinPointType
     * @param argTypes
     * @param className
     * @param declaringClassName
     * @param methodName
     * @param methodDescriptor
     */
    private static void invokeMethod(
        final Method targetMethod,
        final CodeVisitor cv,
        final int joinPointType,
        final Type[] argTypes,
        final String className,
        final String declaringClassName,
        final String methodName,
        final String methodDescriptor) {
        // public method => invoke statically
        prepareReturnValueWrapping(targetMethod, cv);
        prepareParameterUnwrapping(joinPointType, argTypes, cv, className);
        if (!Modifier.isStatic(targetMethod.getModifiers())) {
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitMethodInsn(Constants.INVOKESPECIAL, className, "getTarget", "()Ljava/lang/Object;");
            //AW-265
//            cv
//                    .visitFieldInsn(
//                        Constants.GETFIELD,
//                        className,
//                        TARGET_INSTANCE_FIELD_NAME,
//                        WEAK_REFERENCE_CLASS_SIGNATURE);
//            cv.visitMethodInsn(
//                Constants.INVOKEVIRTUAL,
//                WEAK_REFERENCE_CLASS_NAME,
//                WEAK_REFERENCE_GET_METHOD_NAME,
//                WEAK_REFERENCE_GET_METHOD_SIGNATURE);
            cv.visitTypeInsn(Constants.CHECKCAST, declaringClassName);
        }
        unwrapParameters(argTypes, cv);

        // invoke the target method (static or member) statically
        if (Modifier.isStatic(targetMethod.getModifiers())) {
            cv.visitMethodInsn(Constants.INVOKESTATIC, declaringClassName, methodName, methodDescriptor);
        } else if (targetMethod.getDeclaringClass().isInterface()) {
            //AW-253
            cv.visitMethodInsn(Constants.INVOKEINTERFACE, declaringClassName, methodName, methodDescriptor);
        } else {
            cv.visitMethodInsn(Constants.INVOKEVIRTUAL, declaringClassName, methodName, methodDescriptor);
        }
        wrapReturnValue(targetMethod, cv);
    }

    /**
     * Handles invocation of a method reflectively - execution context.
     * 
     * @param cv
     */
    private static void invokeMethodExecutionReflectively(final CodeVisitor cv) {
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
            Constants.INVOKESTATIC,
            JOIN_POINT_BASE_CLASS_NAME,
            INVOKE_TARGET_METHOD_EXECUTION_METHOD_NAME,
            INVOKE_TARGET_METHOD_EXECUTION_METHOD_SIGNATURE);
    }

    /**
     * Handles invocation of a method reflectively - call context.
     * 
     * @param cv
     */
    private static void invokeMethodCallReflectively(final CodeVisitor cv) {
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
            Constants.INVOKESTATIC,
            JOIN_POINT_BASE_CLASS_NAME,
            INVOKE_TARGET_METHOD_CALL_METHOD_NAME,
            INVOKE_TARGET_METHOD_CALL_METHOD_SIGNATURE);
    }

    /**
     * Handles invocation of a constructor - call context.
     * 
     * @param joinPointType
     * @param argTypes
     * @param cv
     * @param className
     * @param declaringClassName
     * @param constructorDescriptor
     */
    private static void invokeConstructorCall(
        final int joinPointType,
        final Type[] argTypes,
        final CodeVisitor cv,
        final String className,
        final String declaringClassName,
        final String constructorDescriptor) {
        // public constructor => invoke statically
        prepareParameterUnwrapping(joinPointType, argTypes, cv, className);
        cv.visitTypeInsn(Constants.NEW, declaringClassName);
        cv.visitInsn(Constants.DUP);
        unwrapParameters(argTypes, cv);
        cv.visitMethodInsn(Constants.INVOKESPECIAL, declaringClassName, INIT_METHOD_NAME, constructorDescriptor);
    }

    /**
     * Handles invocation of a constructor reflectively.
     * 
     * @param cv
     */
    private static void invokeConstructorCallReflectively(final CodeVisitor cv) {
        // constructor is non-public -> invoke using reflection
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
            Constants.INVOKESTATIC,
            JOIN_POINT_BASE_CLASS_NAME,
            INVOKE_TARGET_CONSTRUCTOR_CALL_METHOD_NAME,
            INVOKE_TARGET_CONSTRUCTOR_CALL_METHOD_SIGNATURE);
    }

    /**
     * Handles invocation of a constructor - execution context.
     * 
     * @param joinPointType
     * @param newArgTypes
     * @param cv
     * @param className
     * @param declaringClassName
     * @param constructorDescriptor
     */
    private static void invokeConstructorExecution(
        final int joinPointType,
        final Type[] newArgTypes,
        final CodeVisitor cv,
        final String className,
        final String declaringClassName,
        final String constructorDescriptor) {
        // public constructor => invoke statically
        prepareParameterUnwrapping(joinPointType, newArgTypes, cv, className);
        cv.visitTypeInsn(Constants.NEW, declaringClassName);
        cv.visitInsn(Constants.DUP);
        unwrapParameters(newArgTypes, cv);
        cv.visitInsn(Constants.ACONST_NULL);
        cv.visitMethodInsn(Constants.INVOKESPECIAL, declaringClassName, INIT_METHOD_NAME, constructorDescriptor);
    }

    /**
     * Handles invocation of a constructor reflectively - execution context.
     * 
     * @param cv
     */
    private static void invokeConstructorExecutionReflectively(final CodeVisitor cv) {
        // constructor is non-public -> invoke using reflection
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
            Constants.INVOKESTATIC,
            JOIN_POINT_BASE_CLASS_NAME,
            INVOKE_TARGET_CONSTRUCTOR_EXECUTION_METHOD_NAME,
            INVOKE_TARGET_CONSTRUCTOR_EXECUTION_METHOD_SIGNATURE);
    }

    /**
     * Handles invocation of a field - get context.
     * 
     * @param cv
     */
    private static void invokeTargetFieldGet(final CodeVisitor cv) {
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
            Constants.INVOKESTATIC,
            JOIN_POINT_BASE_CLASS_NAME,
            GET_TARGET_FIELD_METHOD_NAME,
            GET_TARGET_FIELD_METHOD_SIGNATURE);
    }

    /**
     * Handles invocation of a field - set context.
     * 
     * @param cv
     */
    private static void invokeTargetFieldSet(final CodeVisitor cv, String className) {
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
            Constants.INVOKESTATIC,
            JOIN_POINT_BASE_CLASS_NAME,
            SET_TARGET_FIELD_METHOD_NAME,
            SET_TARGET_FIELD_METHOD_SIGNATURE);
        //cv.visitInsn(Constants.ACONST_NULL);
        // use rtti getPVals[0]
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitFieldInsn(Constants.GETFIELD, className, RTTI_FIELD_NAME, FIELD_RTTI_IMPL_CLASS_SIGNATURE);
        //cv.visitVarInsn(Constants.ALOAD, 1);
        cv.visitMethodInsn(
            Constants.INVOKEVIRTUAL,
            FIELD_RTTI_IMPL_CLASS_NAME,
            GET_PARAMETER_VALUES_METHOD_NAME,
            GET_PARAMETER_VALUES_METHOD_SIGNATURE);
        AsmHelper.loadIntegerConstant(cv, 0);
        cv.visitInsn(Constants.AALOAD);

    }

    /**
     * Resets the stack frame counter.
     * 
     * @param cv
     * @param className
     */
    private static void resetStackFrameCounter(final CodeVisitor cv, final String className) {
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitInsn(Constants.ICONST_M1);
        cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);
    }

    /**
     * Handles the incrementation of the stack frame.
     * 
     * @param cv
     * @param className
     */
    private static void incrementStackFrameCounter(final CodeVisitor cv, final String className) {
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitInsn(Constants.DUP);
        cv.visitFieldInsn(Constants.GETFIELD, className, STACKFRAME_FIELD_NAME, I);
        cv.visitInsn(Constants.ICONST_1);
        cv.visitInsn(Constants.IADD);
        cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);
    }

    /**
     * Handles the advice invocations. <p/>Creates a switch clause in which the advice chain is called recursively.
     * <p/>Wraps the switch clause in a try-finally statement in which the finally block resets the stack frame counter.
     * 
     * @param cv
     * @param className
     * @param aroundAdvices
     * @param beforeAdvices
     * @param afterAdvices
     * @param signatureCflowExprStruct
     * @param joinPointType
     * @return the labels needed to implement the last part of the try-finally clause
     */
    private static Labels invokeAdvice(
        final CodeVisitor cv,
        final String className,
        final AdviceInfo[] aroundAdvices,
        final AdviceInfo[] beforeAdvices,
        final AdviceInfo[] afterAdvices,
        final RttiInfo signatureCflowExprStruct,
        final int joinPointType) {
        // creates the labels needed for the switch and try-finally blocks
        int nrOfCases = aroundAdvices.length;
        boolean hasBeforeAfterAdvice = (beforeAdvices.length + afterAdvices.length) > 0;
        if (hasBeforeAfterAdvice) {
            nrOfCases += 1; // one more case
        }
        Label[] switchCaseLabels = new Label[nrOfCases];
        Label[] returnLabels = new Label[nrOfCases];
        int[] caseNumbers = new int[nrOfCases];
        for (int i = 0; i < switchCaseLabels.length; i++) {
            switchCaseLabels[i] = new Label();
            caseNumbers[i] = i;
        }
        for (int i = 0; i < returnLabels.length; i++) {
            returnLabels[i] = new Label();
        }
        Label tryStartLabel = new Label();
        Label defaultCaseLabel = new Label();
        Label gotoLabel = new Label();
        Label handlerLabel = new Label();
        Label endLabel = new Label();
        cv.visitLabel(tryStartLabel);
        if (signatureCflowExprStruct.cflowExpressions.size() > 0) {
            // add cflow check only if we have cflow expressions
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitMethodInsn(Constants.INVOKEVIRTUAL, className, IS_IN_CFLOW_METOD_NAME, IS_IN_CFLOW_METOD_SIGNATURE);
            cv.visitJumpInsn(Constants.IFEQ, defaultCaseLabel);
        }
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitFieldInsn(Constants.GETFIELD, className, STACKFRAME_FIELD_NAME, I);

        // create the switch table
        cv.visitLookupSwitchInsn(defaultCaseLabel, caseNumbers, switchCaseLabels);
        invokeBeforeAfterAdvice(
            hasBeforeAfterAdvice,
            beforeAdvices,
            afterAdvices,
            className,
            cv,
            switchCaseLabels,
            returnLabels,
            joinPointType);
        invokeAroundAdvice(hasBeforeAfterAdvice, aroundAdvices, className, cv, switchCaseLabels, returnLabels, joinPointType);
        cv.visitLabel(defaultCaseLabel);

        // put the labels in a data structure and return them
        Labels labelData = new Labels();
        labelData.switchCaseLabels = switchCaseLabels;
        labelData.returnLabels = returnLabels;
        labelData.startLabel = tryStartLabel;
        labelData.gotoLabel = gotoLabel;
        labelData.handlerLabel = handlerLabel;
        labelData.endLabel = endLabel;
        return labelData;
    }

    /**
     * Invokes before and after advice.
     * 
     * @param hasBeforeAfterAdvice
     * @param beforeAdvices
     * @param afterAdvices
     * @param className
     * @param cv
     * @param switchCaseLabels
     * @param returnLabels
     * @param joinPointType
     */
    private static void invokeBeforeAfterAdvice(
        boolean hasBeforeAfterAdvice,
        final AdviceInfo[] beforeAdvices,
        final AdviceInfo[] afterAdvices,
        final String className,
        final CodeVisitor cv,
        final Label[] switchCaseLabels,
        final Label[] returnLabels,
        final int joinPointType) {
        if (hasBeforeAfterAdvice) {
            cv.visitLabel(switchCaseLabels[0]);

            // add invocations to the before advices
            for (int i = 0; i < beforeAdvices.length; i++) {
                AdviceInfo beforeAdvice = beforeAdvices[i];
                AspectContainer container = beforeAdvice.getAspectManager().getAspectContainer(
                    beforeAdvice.getAspectIndex());
                Method adviceMethod = container.getAdvice(beforeAdvice.getMethodIndex());
                String aspectClassName = container.getCrossCuttingInfo().getAspectClass().getName().replace('.', '/');
                String aspectFieldName = BEFORE_ADVICE_FIELD_PREFIX + i;
                String aspectClassSignature = L + aspectClassName + SEMICOLON;
                // handles advice with signature for args() support
                //      JoinPoint as sole arg or:
                //      this.getRtti().getParametersValues()[<index>], unwrap if primitive type
                int[] argIndexes = beforeAdvice.getMethodToArgIndexes();
                // if no arg or only JoinPoint, we consider for now that we have to push JoinPoint for old advice with JoinPoint as sole arg
                if (isAdviceArgsJoinPointOnly(argIndexes)) {
                    cv.visitVarInsn(Constants.ALOAD, 0);
                    cv.visitFieldInsn(Constants.GETFIELD, className, aspectFieldName, aspectClassSignature);
                    cv.visitVarInsn(Constants.ALOAD, 0);
                } else {
                    Type[] adviceArgTypes = Type.getArgumentTypes(adviceMethod);
                    // this.getRtti().getParameterValues() (array, store in register 2)
                    prepareParameterUnwrapping(joinPointType, adviceArgTypes, cv, className);
                    cv.visitVarInsn(Constants.ALOAD, 0);
                    cv.visitFieldInsn(Constants.GETFIELD, className, aspectFieldName, aspectClassSignature);
                    for (int j = 0; j < argIndexes.length; j++) {
                        int argIndex = argIndexes[j];
                        if (argIndex != -1) {
                            Type argumentType = adviceArgTypes[j];
                            cv.visitVarInsn(Constants.ALOAD, 2);//the param array
                            AsmHelper.loadIntegerConstant(cv, argIndex);//index
                            cv.visitInsn(Constants.AALOAD);
                            unwrapParameter(argumentType, cv);//unwrap
                        } else {
                            // assume JoinPoint - TODO support for static part optimization
                            cv.visitVarInsn(Constants.ALOAD, 0);
                        }
                    }
                }
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    aspectClassName,
                    adviceMethod.getName(),
                    Type.getMethodDescriptor(adviceMethod));
            }

            // add invocation to this.proceed
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitMethodInsn(Constants.INVOKEVIRTUAL, className, PROCEED_METHOD_NAME, PROCEED_METHOD_SIGNATURE);
            cv.visitVarInsn(Constants.ASTORE, 1);

            // add invocations to the after advices
            for (int i = afterAdvices.length - 1; i >= 0; i--) {
                AdviceInfo afterAdvice = afterAdvices[i];
                AspectContainer container = afterAdvice.getAspectManager().getAspectContainer(
                    afterAdvice.getAspectIndex());
                Method adviceMethod = container.getAdvice(afterAdvice.getMethodIndex());
                String aspectClassName = container.getCrossCuttingInfo().getAspectClass().getName().replace('.', '/');
                String aspectFieldName = AFTER_ADVICE_FIELD_PREFIX + i;
                String aspectClassSignature = L + aspectClassName + SEMICOLON;
                // handles advice with signature for args() support
                //      JoinPoint as sole arg or:
                //      this.getRtti().getParametersValues()[<index>], unwrap if primitive type
                int[] argIndexes = afterAdvice.getMethodToArgIndexes();
                // if no arg or only JoinPoint, we consider for now that we have to push JoinPoint for old advice with JoinPoint as sole arg
                if (isAdviceArgsJoinPointOnly(argIndexes)) {
                    cv.visitVarInsn(Constants.ALOAD, 0);
                    cv.visitFieldInsn(Constants.GETFIELD, className, aspectFieldName, aspectClassSignature);
                    cv.visitVarInsn(Constants.ALOAD, 0);
                } else {
                    Type[] adviceArgTypes = Type.getArgumentTypes(adviceMethod);
                    // this.getRtti().getParameterValues() (array, store in register 2)
                    prepareParameterUnwrapping(joinPointType, adviceArgTypes, cv, className);
                    cv.visitVarInsn(Constants.ALOAD, 0);
                    cv.visitFieldInsn(Constants.GETFIELD, className, aspectFieldName, aspectClassSignature);
                    for (int j = 0; j < argIndexes.length; j++) {
                        int argIndex = argIndexes[j];
                        if (argIndex != -1) {
                            Type argumentType = adviceArgTypes[j];
                            cv.visitVarInsn(Constants.ALOAD, 2);//the param array
                            AsmHelper.loadIntegerConstant(cv, argIndex);//index
                            cv.visitInsn(Constants.AALOAD);
                            unwrapParameter(argumentType, cv);//unwrap
                        } else {
                            // assume JoinPoint - TODO support for static part optimization
                            cv.visitVarInsn(Constants.ALOAD, 0);
                        }
                    }
                }
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    aspectClassName,
                    adviceMethod.getName(),
                    Type.getMethodDescriptor(adviceMethod));
            }
            cv.visitLabel(returnLabels[0]);
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitInsn(Constants.ICONST_M1);
            cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);
            cv.visitVarInsn(Constants.ALOAD, 1);
            cv.visitInsn(Constants.ARETURN);
        }
    }

    /**
     * Invokes around advice.
     * 
     * @param hasBeforeAfterAdvice
     * @param aroundAdvices
     * @param className
     * @param cv
     * @param switchCaseLabels
     * @param returnLabels
     * @param joinPointType
     */
    private static void invokeAroundAdvice(
        boolean hasBeforeAfterAdvice,
        final AdviceInfo[] aroundAdvices,
        final String className,
        final CodeVisitor cv,
        final Label[] switchCaseLabels,
        final Label[] returnLabels,
        final int joinPointType) {
        int i = 0;
        int j = 0;
        if (hasBeforeAfterAdvice) {
            j = 1;
        }
        for (; i < aroundAdvices.length; i++, j++) {
            AdviceInfo aroundAdvice = aroundAdvices[i];
            AspectContainer container = aroundAdvice.getAspectManager().getAspectContainer(
                aroundAdvice.getAspectIndex());
            Method adviceMethod = container.getAdvice(aroundAdvice.getMethodIndex());
            String aspectClassName = container.getCrossCuttingInfo().getAspectClass().getName().replace('.', '/');
            String aspectFieldName = AROUND_ADVICE_FIELD_PREFIX + i;
            String aspectClassSignature = L + aspectClassName + SEMICOLON;
            cv.visitLabel(switchCaseLabels[j]);
            // handles advice with signature for args() support
            //      JoinPoint as sole arg or:
            //      this.getRtti().getParametersValues()[<index>], unwrap if primitive type
            int[] argIndexes = aroundAdvice.getMethodToArgIndexes();
            // if no arg or only JoinPoint, we consider for now that we have to push JoinPoint for old advice with JoinPoint as sole arg
            if (isAdviceArgsJoinPointOnly(argIndexes)) {
                cv.visitVarInsn(Constants.ALOAD, 0);
                cv.visitFieldInsn(Constants.GETFIELD, className, aspectFieldName, aspectClassSignature);
                cv.visitVarInsn(Constants.ALOAD, 0);
            } else {
                Type[] adviceArgTypes = Type.getArgumentTypes(adviceMethod);
                // this.getRtti().getParameterValues() (array, store in register 2)
                prepareParameterUnwrapping(joinPointType, adviceArgTypes, cv, className);
                cv.visitVarInsn(Constants.ALOAD, 0);
                cv.visitFieldInsn(Constants.GETFIELD, className, aspectFieldName, aspectClassSignature);
                for (int a = 0; a < argIndexes.length; a++) {
                    int argIndex = argIndexes[a];
                    if (argIndex != -1) {
                        Type argumentType = adviceArgTypes[a];
                        cv.visitVarInsn(Constants.ALOAD, 2);//the param array
                        AsmHelper.loadIntegerConstant(cv, argIndex);//index
                        cv.visitInsn(Constants.AALOAD);
                        unwrapParameter(argumentType, cv);//unwrap
                    } else {
                        // assume JoinPoint - TODO support for static part optimization
                        cv.visitVarInsn(Constants.ALOAD, 0);
                    }
                }
            }
            cv.visitMethodInsn(
                Constants.INVOKEVIRTUAL,
                aspectClassName,
                adviceMethod.getName(),
                Type.getMethodDescriptor(adviceMethod));

            // try-finally management
            cv.visitVarInsn(Constants.ASTORE, 2);
            cv.visitLabel(returnLabels[j]);
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitInsn(Constants.ICONST_M1);
            cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);
            cv.visitVarInsn(Constants.ALOAD, 2);
            cv.visitInsn(Constants.ARETURN);
        }
    }

    /**
     * Prepares the unwrapping of the parameters.
     * 
     * @param joinPointType
     * @param argTypes
     * @param cv
     * @param className
     */
    private static void prepareParameterUnwrapping(
        final int joinPointType,
        final Type[] argTypes,
        final CodeVisitor cv,
        final String className) {
        if (argTypes.length != 0) {
            // handle paramerers
            cv.visitVarInsn(Constants.ALOAD, 0);
            switch (joinPointType) {
                case JoinPointType.METHOD_EXECUTION:
                case JoinPointType.METHOD_CALL:
                    cv.visitFieldInsn(Constants.GETFIELD, className, RTTI_FIELD_NAME, METHOD_RTTI_IMPL_CLASS_SIGNATURE);
                    cv.visitMethodInsn(
                        Constants.INVOKEVIRTUAL,
                        METHOD_RTTI_IMPL_CLASS_NAME,
                        GET_PARAMETER_VALUES_METHOD_NAME,
                        GET_PARAMETER_VALUES_METHOD_SIGNATURE);
                    break;
                case JoinPointType.CONSTRUCTOR_EXECUTION:
                case JoinPointType.CONSTRUCTOR_CALL:
                    cv.visitFieldInsn(
                        Constants.GETFIELD,
                        className,
                        RTTI_FIELD_NAME,
                        CONSTRUCTOR_RTTI_IMPL_CLASS_SIGNATURE);
                    cv.visitMethodInsn(
                        Constants.INVOKEVIRTUAL,
                        CONSTRUCTOR_RTTI_IMPL_CLASS_NAME,
                        GET_PARAMETER_VALUES_METHOD_NAME,
                        GET_PARAMETER_VALUES_METHOD_SIGNATURE);
                    break;
                case JoinPointType.FIELD_GET:
                case JoinPointType.FIELD_SET:
                    cv.visitFieldInsn(Constants.GETFIELD, className, RTTI_FIELD_NAME, FIELD_RTTI_IMPL_CLASS_SIGNATURE);
                    cv.visitMethodInsn(
                        Constants.INVOKEVIRTUAL,
                        FIELD_RTTI_IMPL_CLASS_NAME,
                        GET_PARAMETER_VALUES_METHOD_NAME,
                        GET_PARAMETER_VALUES_METHOD_SIGNATURE);
                    break;
                case JoinPointType.HANDLER:
                    throw new UnsupportedOperationException("handler is not support yet");
                case JoinPointType.STATIC_INITALIZATION:
                    throw new UnsupportedOperationException("static initialization is not support yet");
            }
            cv.visitVarInsn(Constants.ASTORE, 2);
        }
    }

    /**
     * Handle the unwrapping of the parameters.
     * 
     * @param argTypes
     * @param cv
     */
    private static void unwrapParameters(final Type[] argTypes, final CodeVisitor cv) {
        // unwrap the parameters
        for (int f = 0; f < argTypes.length; f++) {
            cv.visitVarInsn(Constants.ALOAD, 2);
            AsmHelper.loadIntegerConstant(cv, f);
            cv.visitInsn(Constants.AALOAD);
            Type argType = argTypes[f];
            unwrapParameter(argType, cv);
        }
    }

    /**
     * Unwrapp a single parameter which is already on the stack.
     * We skip the "null" test (to avoid NPE on longValue() etc) since
     * this method is used only in the RTTI parameters value extraction.
     * TODO: This may lead to issue if advice is setting rtti param to null.
     *
     * @param argType
     * @param cv
     */
    private static void unwrapParameter(final Type argType, final CodeVisitor cv) {
        // unwrap the parameter
        switch (argType.getSort()) {
            case Type.SHORT:
                cv.visitTypeInsn(Constants.CHECKCAST, SHORT_CLASS_NAME);
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    SHORT_CLASS_NAME,
                    SHORT_VALUE_METHOD_NAME,
                    SHORT_VALUE_METHOD_SIGNATURE);
                break;
            case Type.INT:
                cv.visitTypeInsn(Constants.CHECKCAST, INTEGER_CLASS_NAME);
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    INTEGER_CLASS_NAME,
                    INT_VALUE_METHOD_NAME,
                    INT_VALUE_METHOD_SIGNATURE);
                break;
            case Type.LONG:
                cv.visitTypeInsn(Constants.CHECKCAST, LONG_CLASS_NAME);
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    LONG_CLASS_NAME,
                    LONG_VALUE_METHOD_NAME,
                    LONG_VALUE_METHOD_SIGNATURE);
                break;
            case Type.FLOAT:
                cv.visitTypeInsn(Constants.CHECKCAST, FLOAT_CLASS_NAME);
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    FLOAT_CLASS_NAME,
                    FLOAT_VALUE_METHOD_NAME,
                    FLOAT_VALUE_METHOD_SIGNATURE);
                break;
            case Type.DOUBLE:
                cv.visitTypeInsn(Constants.CHECKCAST, DOUBLE_CLASS_NAME);
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    DOUBLE_CLASS_NAME,
                    DOUBLE_VALUE_METHOD_NAME,
                    DOUBLE_VALUE_METHOD_SIGNATURE);
                break;
            case Type.BYTE:
                cv.visitTypeInsn(Constants.CHECKCAST, BYTE_CLASS_NAME);
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    BYTE_CLASS_NAME,
                    BYTE_VALUE_METHOD_NAME,
                    BYTE_VALUE_METHOD_SIGNATURE);
                break;
            case Type.BOOLEAN:
                cv.visitTypeInsn(Constants.CHECKCAST, BOOLEAN_CLASS_NAME);
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    BOOLEAN_CLASS_NAME,
                    BOOLEAN_VALUE_METHOD_NAME,
                    BOOLEAN_VALUE_METHOD_SIGNATURE);
                break;
            case Type.CHAR:
                cv.visitTypeInsn(Constants.CHECKCAST, CHARACTER_CLASS_NAME);
                cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL,
                    CHARACTER_CLASS_NAME,
                    CHAR_VALUE_METHOD_NAME,
                    CHAR_VALUE_METHOD_SIGNATURE);
                break;
            case Type.OBJECT:
                String objectTypeName = argType.getClassName().replace('.', '/');
                cv.visitTypeInsn(Constants.CHECKCAST, objectTypeName);
                break;
            case Type.ARRAY:
                cv.visitTypeInsn(Constants.CHECKCAST, argType.getDescriptor());
                break;
        }
    }

    /**
     * Prepare the return value wrapping.
     * 
     * @param targetMethod
     * @param cv
     */
    private static void prepareReturnValueWrapping(final Method targetMethod, final CodeVisitor cv) {
        switch (Type.getReturnType(targetMethod).getSort()) {
            case Type.SHORT:
                cv.visitTypeInsn(Constants.NEW, SHORT_CLASS_NAME);
                cv.visitInsn(Constants.DUP);
                break;
            case Type.INT:
                cv.visitTypeInsn(Constants.NEW, INTEGER_CLASS_NAME);
                cv.visitInsn(Constants.DUP);
                break;
            case Type.LONG:
                cv.visitTypeInsn(Constants.NEW, LONG_CLASS_NAME);
                cv.visitInsn(Constants.DUP);
                break;
            case Type.FLOAT:
                cv.visitTypeInsn(Constants.NEW, FLOAT_CLASS_NAME);
                cv.visitInsn(Constants.DUP);
                break;
            case Type.DOUBLE:
                cv.visitTypeInsn(Constants.NEW, DOUBLE_CLASS_NAME);
                cv.visitInsn(Constants.DUP);
                break;
            case Type.BYTE:
                cv.visitTypeInsn(Constants.NEW, BYTE_CLASS_NAME);
                cv.visitInsn(Constants.DUP);
                break;
            case Type.BOOLEAN:
                cv.visitTypeInsn(Constants.NEW, BOOLEAN_CLASS_NAME);
                cv.visitInsn(Constants.DUP);
                break;
            case Type.CHAR:
                cv.visitTypeInsn(Constants.NEW, CHARACTER_CLASS_NAME);
                cv.visitInsn(Constants.DUP);
                break;
        }
    }

    /**
     * Handle the return value wrapping.
     * 
     * @param targetMethod
     * @param cv
     */
    private static void wrapReturnValue(final Method targetMethod, final CodeVisitor cv) {
        switch (Type.getReturnType(targetMethod).getSort()) {
            case Type.VOID:
                cv.visitInsn(Constants.ACONST_NULL);
                break;
            case Type.SHORT:
                cv.visitMethodInsn(
                    Constants.INVOKESPECIAL,
                    SHORT_CLASS_NAME,
                    INIT_METHOD_NAME,
                    SHORT_CLASS_INIT_METHOD_SIGNATURE);
                break;
            case Type.INT:
                cv.visitMethodInsn(
                    Constants.INVOKESPECIAL,
                    INTEGER_CLASS_NAME,
                    INIT_METHOD_NAME,
                    INTEGER_CLASS_INIT_METHOD_SIGNATURE);
                break;
            case Type.LONG:
                cv.visitMethodInsn(
                    Constants.INVOKESPECIAL,
                    LONG_CLASS_NAME,
                    INIT_METHOD_NAME,
                    LONG_CLASS_INIT_METHOD_SIGNATURE);
                break;
            case Type.FLOAT:
                cv.visitMethodInsn(
                    Constants.INVOKESPECIAL,
                    FLOAT_CLASS_NAME,
                    INIT_METHOD_NAME,
                    FLOAT_CLASS_INIT_METHOD_SIGNATURE);
                break;
            case Type.DOUBLE:
                cv.visitMethodInsn(
                    Constants.INVOKESPECIAL,
                    DOUBLE_CLASS_NAME,
                    INIT_METHOD_NAME,
                    DOUBLE_CLASS_INIT_METHOD_SIGNATURE);
                break;
            case Type.BYTE:
                cv.visitMethodInsn(
                    Constants.INVOKESPECIAL,
                    BYTE_CLASS_NAME,
                    INIT_METHOD_NAME,
                    BYTE_CLASS_INIT_METHOD_SIGNATURE);
                break;
            case Type.BOOLEAN:
                cv.visitMethodInsn(
                    Constants.INVOKESPECIAL,
                    BOOLEAN_CLASS_NAME,
                    INIT_METHOD_NAME,
                    BOOLEAN_CLASS_INIT_METHOD_SIGNATURE);
                break;
            case Type.CHAR:
                cv.visitMethodInsn(
                    Constants.INVOKESPECIAL,
                    CHARACTER_CLASS_NAME,
                    INIT_METHOD_NAME,
                    CHARACTER_CLASS_INIT_METHOD_SIGNATURE);
                break;
        }
    }

    /**
     * Creates and sets the signature and a list with all the cflow expressions for the join point.
     * 
     * @param joinPointType
     * @param joinPointHash
     * @param declaringClass
     * @param system
     * @param thisInstance
     * @param targetInstance
     * @param targetClass
     * @return static info
     * @TODO HANDLER cflow management needs to be tested for all pointcuts (only verified to work for EXECUTION)
     */
    private static RttiInfo setRttiInfo(
        final int joinPointType,
        final int joinPointHash,
        final Class declaringClass,
        final AspectSystem system,
        final Object thisInstance,
        final Object targetInstance,
        final Class targetClass) {
        RttiInfo tuple = new RttiInfo();
        List cflowExpressionList = new ArrayList();
        AspectManager[] aspectManagers = system.getAspectManagers();
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
                MethodTuple methodTuple = AspectRegistry.getMethodTuple(declaringClass, joinPointHash);
                MethodSignatureImpl methodSignature = new MethodSignatureImpl(
                    methodTuple.getDeclaringClass(),
                    methodTuple);
                tuple.signature = methodSignature;
                tuple.rtti = new MethodRttiImpl(methodSignature, thisInstance, targetInstance);
                MethodInfo methodInfo = JavaMethodInfo.getMethodInfo(methodTuple.getWrapperMethod());
                ClassInfo withinInfo = JavaClassInfo.getClassInfo(targetClass);
                ExpressionContext ctx = new ExpressionContext(PointcutType.EXECUTION, methodInfo, methodInfo);//AVAJ
                for (int i = 0; i < aspectManagers.length; i++) {
                    for (Iterator it = aspectManagers[i].getPointcuts(ctx).iterator(); it.hasNext();) {
                        Pointcut pointcut = (Pointcut) it.next();
                        if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                            cflowExpressionList.add(pointcut.getExpressionInfo().getCflowExpressionRuntime());
                        }
                    }
                }
                tuple.cflowExpressions = cflowExpressionList;
                tuple.expressionContext = ctx;
                break;
            case JoinPointType.METHOD_CALL:
                methodTuple = AspectRegistry.getMethodTuple(declaringClass, joinPointHash);
                methodSignature = new MethodSignatureImpl(methodTuple.getDeclaringClass(), methodTuple);
                tuple.signature = methodSignature;
                tuple.rtti = new MethodRttiImpl(methodSignature, thisInstance, targetInstance);
                methodInfo = JavaMethodInfo.getMethodInfo(methodTuple.getWrapperMethod());
                withinInfo = JavaClassInfo.getClassInfo(targetClass);
                ctx = new ExpressionContext(PointcutType.CALL, methodInfo, withinInfo);
                for (int i = 0; i < aspectManagers.length; i++) {
                    for (Iterator it = aspectManagers[i].getPointcuts(ctx).iterator(); it.hasNext();) {
                        Pointcut pointcut = (Pointcut) it.next();
                        if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                            cflowExpressionList.add(pointcut.getExpressionInfo().getCflowExpressionRuntime());
                        }
                    }
                }
                tuple.cflowExpressions = cflowExpressionList;
                tuple.expressionContext = ctx;
                break;
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                ConstructorTuple constructorTuple = AspectRegistry.getConstructorTuple(declaringClass, joinPointHash);
                ConstructorSignatureImpl constructorSignature = new ConstructorSignatureImpl(constructorTuple
                        .getDeclaringClass(), constructorTuple);
                tuple.signature = constructorSignature;
                tuple.rtti = new ConstructorRttiImpl(constructorSignature, thisInstance, targetInstance);
                ConstructorInfo constructorInfo = JavaConstructorInfo.getConstructorInfo(constructorTuple
                        .getWrapperConstructor());
                withinInfo = JavaClassInfo.getClassInfo(targetClass);
                ctx = new ExpressionContext(PointcutType.EXECUTION, constructorInfo, constructorInfo);//AVAJ
                for (int i = 0; i < aspectManagers.length; i++) {
                    for (Iterator it = aspectManagers[i].getPointcuts(ctx).iterator(); it.hasNext();) {
                        Pointcut pointcut = (Pointcut) it.next();
                        if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                            cflowExpressionList.add(pointcut.getExpressionInfo().getCflowExpressionRuntime());
                        }
                    }
                }
                tuple.cflowExpressions = cflowExpressionList;
                tuple.expressionContext = ctx;
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
                constructorTuple = AspectRegistry.getConstructorTuple(declaringClass, joinPointHash);
                constructorSignature = new ConstructorSignatureImpl(
                    constructorTuple.getDeclaringClass(),
                    constructorTuple);
                tuple.signature = constructorSignature;
                tuple.rtti = new ConstructorRttiImpl(constructorSignature, thisInstance, targetInstance);
                constructorInfo = JavaConstructorInfo.getConstructorInfo(constructorTuple.getWrapperConstructor());
                withinInfo = JavaClassInfo.getClassInfo(targetClass);
                ctx = new ExpressionContext(PointcutType.CALL, constructorInfo, withinInfo);
                for (int i = 0; i < aspectManagers.length; i++) {
                    for (Iterator it = aspectManagers[i].getPointcuts(ctx).iterator(); it.hasNext();) {
                        Pointcut pointcut = (Pointcut) it.next();
                        if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                            cflowExpressionList.add(pointcut.getExpressionInfo().getCflowExpressionRuntime());
                        }
                    }
                }
                tuple.cflowExpressions = cflowExpressionList;
                tuple.expressionContext = ctx;
                break;
            case JoinPointType.FIELD_SET:
                Field field = AspectRegistry.getField(declaringClass, joinPointHash);
                FieldSignatureImpl fieldSignature = new FieldSignatureImpl(field.getDeclaringClass(), field);
                tuple.signature = fieldSignature;
                tuple.rtti = new FieldRttiImpl(fieldSignature, thisInstance, targetInstance);
                FieldInfo fieldInfo = JavaFieldInfo.getFieldInfo(field);
                withinInfo = JavaClassInfo.getClassInfo(targetClass);
                ctx = new ExpressionContext(PointcutType.SET, fieldInfo, withinInfo);//AVAJ
                for (int i = 0; i < aspectManagers.length; i++) {
                    for (Iterator it = aspectManagers[i].getPointcuts(ctx).iterator(); it.hasNext();) {
                        Pointcut pointcut = (Pointcut) it.next();
                        if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                            cflowExpressionList.add(pointcut.getExpressionInfo().getCflowExpressionRuntime());
                        }
                    }
                }
                tuple.cflowExpressions = cflowExpressionList;
                tuple.expressionContext = ctx;
                break;
            case JoinPointType.FIELD_GET:
                field = AspectRegistry.getField(declaringClass, joinPointHash);
                fieldSignature = new FieldSignatureImpl(field.getDeclaringClass(), field);
                tuple.signature = fieldSignature;
                tuple.rtti = new FieldRttiImpl(fieldSignature, thisInstance, targetInstance);
                fieldInfo = JavaFieldInfo.getFieldInfo(field);
                withinInfo = JavaClassInfo.getClassInfo(targetClass);
                ctx = new ExpressionContext(PointcutType.GET, fieldInfo, withinInfo);//AVAJ
                for (int i = 0; i < aspectManagers.length; i++) {
                    for (Iterator it = aspectManagers[i].getPointcuts(ctx).iterator(); it.hasNext();) {
                        Pointcut pointcut = (Pointcut) it.next();
                        if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                            cflowExpressionList.add(pointcut.getExpressionInfo().getCflowExpressionRuntime());
                        }
                    }
                }
                tuple.cflowExpressions = cflowExpressionList;
                tuple.expressionContext = ctx;
                break;
            case JoinPointType.HANDLER:
                CatchClauseSignatureImpl catchClauseSignature = new CatchClauseSignatureImpl(
                    declaringClass,
                    declaringClass,
                    "");
                tuple.signature = catchClauseSignature;
                tuple.rtti = new CatchClauseRttiImpl(catchClauseSignature, thisInstance, targetInstance);
                ClassInfo exceptionClassInfo = JavaClassInfo.getClassInfo(declaringClass);
                withinInfo = JavaClassInfo.getClassInfo(targetClass);//AVAJ within/withincode support ?
                ctx = new ExpressionContext(PointcutType.HANDLER, exceptionClassInfo, withinInfo);
                for (int i = 0; i < aspectManagers.length; i++) {
                    for (Iterator it = aspectManagers[i].getPointcuts(ctx).iterator(); it.hasNext();) {
                        Pointcut pointcut = (Pointcut) it.next();
                        if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                            cflowExpressionList.add(pointcut.getExpressionInfo().getCflowExpressionRuntime());
                        }
                    }
                }
                tuple.cflowExpressions = cflowExpressionList;
                tuple.expressionContext = ctx;
                break;
            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");
        }
        if (tuple.cflowExpressions == null) {
            tuple.cflowExpressions = EMTPTY_ARRAY_LIST;
        }
        return tuple;
    }

    /**
     * Test if the advice has JoinPoint as sole arg or is using args() support.
     *
     * @param methodToArgIndexes
     * @return true if simple advice without args() binding
     */
    private static boolean isAdviceArgsJoinPointOnly(int[] methodToArgIndexes) {
        for (int i = 0; i < methodToArgIndexes.length; i++) {
            int argIndex = methodToArgIndexes[i];
            if (argIndex >= 0) {
                return false;
            }
        }
        return true;
    }



    /**
     * Struct for the labels needed in the switch and try-finally blocks in the proceed method.
     */
    static class Labels {
        public Label[] switchCaseLabels = null;

        public Label[] returnLabels = null;

        public Label startLabel = null;

        public Label gotoLabel = null;

        public Label handlerLabel = null;

        public Label endLabel = null;
    }

    /**
     * Struct for static info.
     */
    public static class RttiInfo {
        public Signature signature = null;

        public Rtti rtti = null;

        public List cflowExpressions = null;

        public ExpressionContext expressionContext = null;
    }
}
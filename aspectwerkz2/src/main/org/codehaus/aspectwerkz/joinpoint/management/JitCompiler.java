/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.*;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.transform.AsmHelper;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldSignatureImpl;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.lang.reflect.*;

/**
 * Runtime (Just-In-Time/JIT) compiler.
 * <p/>
 * Compiles a custom JoinPoint class that invokes all advices in a specific advice chain (at a specific join point) and
 * the target join point statically.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JitCompiler {

    /**
     * @TODO: document JVM option
     * <p/>
     * The path where the JIT class should be dumped to disk. If not speficied then no dump.
     */
//    private static final String JIT_CLASS_DUMP_DIR = "_dump";
    private static final String JIT_CLASS_DUMP_DIR = java.lang.System.getProperty("aspectwerkz.jit.dump.path", null);

    private static final String JIT_CLASS_PREFIX = "org/codehaus/aspectwerkz/jit/___AW_JP_";
    private static final String STACKFRAME_FIELD_NAME = "m_stackFrame";
    private static final String SIGNATURE_FIELD_NAME = "m_signature";
    private static final String SYSTEM_FIELD_NAME = "m_system";
    private static final String TARGET_INSTANCE_FIELD_NAME = "m_targetInstance";
    private static final String TARGET_CLASS_FIELD_NAME = "m_targetClass";
    private static final String AROUND_ADVICE_FIELD_PREFIX = "m_around";

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
    private static final String JOIN_POINT_BASE_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/JoinPointBase";
    private static final String SYSTEM_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/System;";
    private static final String SYSTEM_CLASS_NAME = "org/codehaus/aspectwerkz/System";
    private static final String ASPECT_MANAGER_CLASS_NAME = "org/codehaus/aspectwerkz/aspect/management/AspectManager";
    private static final String ASPECT_CLASS_NAME = "org/codehaus/aspectwerkz/aspect/Aspect";
    private static final String THROWABLE_CLASS_NAME = "java/lang/Throwable";
    private static final String ADVICE_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;";
    private static final String JOIN_POINT_BASE_INIT_METHOD_SIGNATURE = "(Ljava/lang/String;ILjava/lang/Class;Lorg/codehaus/aspectwerkz/joinpoint/management/AroundAdviceExecutor;Lorg/codehaus/aspectwerkz/joinpoint/management/BeforeAdviceExecutor;Lorg/codehaus/aspectwerkz/joinpoint/management/AfterAdviceExecutor;)V";
    private static final String JIT_JOIN_POINT_INIT_METHOD_SIGNATURE = "(Ljava/lang/String;ILjava/lang/Class;Lorg/codehaus/aspectwerkz/joinpoint/Signature;)V";
    private static final String SYSTEM_LOADER_CLASS_NAME = "org/codehaus/aspectwerkz/SystemLoader";
    private static final String INIT_METHOD_NAME = "<init>";
    private static final String GET_SYSTEM_METHOD_NAME = "getSystem";
    private static final String GET_SYSTEM_METHOD_NAME_SIGNATURE = "(Ljava/lang/String;)Lorg/codehaus/aspectwerkz/System;";
    private static final String GET_ASPECT_MANAGER_METHOD_NAME = "getAspectManager";
    private static final String GET_ASPECT_MANAGER_METHOD_NAME_SIGNATURE = "()Lorg/codehaus/aspectwerkz/aspect/management/AspectManager;";
    private static final String GET_ASPECT_METHOD_NAME = "getAspect";
    private static final String GET_ASPECT_METHOD_SIGNATURE = "(I)Lorg/codehaus/aspectwerkz/aspect/Aspect;";
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
    private static final String GET_PER_JVM_ASPECT_METHOD_NAME = "___AW_getPerJvmAspect";
    private static final String GET_PER_JVM_ASPECT_METHOD_SIGNATURE = "()Lorg/codehaus/aspectwerkz/aspect/Aspect;";
    private static final String GET_PER_CLASS_ASPECT_METHOD_NAME = "___AW_getPerClassAspect";
    private static final String GET_PER_CLASS_ASPECT_METHOD_SIGNATURE = "(Ljava/lang/Class;)Lorg/codehaus/aspectwerkz/aspect/Aspect;";
    private static final String GET_SIGNATURE_METHOD_NAME = "getSignature";
    private static final String GET_SIGNATURE_METHOD_SIGNATURE = "()Lorg/codehaus/aspectwerkz/joinpoint/Signature;";
    private static final String PROCEED_METHOD_NAME = "proceed";
    private static final String PROCEED_METHOD_SIGNATURE = "()Ljava/lang/Object;";
    private static final String GET_PARAMETER_VALUES_METHOD_NAME = "getParameterValues";
    private static final String GET_PARAMETER_VALUES_METHOD_SIGNATURE = "()[Ljava/lang/Object;";
    private static final String INVOKE_TARGET_METHOD_METHOD_NAME = "invokeTargetMethod";
    private static final String INVOKE_TARGET_METHOD_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;";
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
    private static final String SET_RETURN_VALUE_METHOD_NAME = "setReturnValue";
    private static final String SET_RETURN_VALUE_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";
    private static final String SET_NEW_INSTANCE_METHOD_NAME = "setNewInstance";
    private static final String SET_NEW_INSTANCE_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";
    private static final String SET_FIELD_VALUE_METHOD_NAME = "setFieldValue";
    private static final String SET_FIELD_VALUE_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";

    private static final String L = "L";
    private static final String I = "I";
    private static final String SEMICOLON = ";";

    /**
     * @param joinPointHash  the join point hash
     * @param joinPointType  the join point joinPointType
     * @param pointcutType   the pointcut type
     * @param advices        a list with the advices
     * @param declaringClass the declaring class
     * @param targetClass    the currently executing class
     * @param uuid           the system UUID
     * @return the JIT compiled join point
     */
    public static JoinPoint compileJoinPoint(
            final int joinPointHash,
            final int joinPointType,
            final PointcutType pointcutType,
            final AdviceContainer[] advices,
            final Class declaringClass,
            final Class targetClass,
            final String uuid) {

        try {
            IndexTuple[] aroundAdvices = JoinPointManager.extractAroundAdvices(advices);
            IndexTuple[] beforeAdvices = JoinPointManager.extractBeforeAdvices(advices);
            IndexTuple[] afterAdvices = JoinPointManager.extractAfterAdvices(advices);

            // TODO: currently if before or after found => bail out
            // TODO: handle before and after advices
            if (beforeAdvices.length != 0) {
                return null;
            }
            if (afterAdvices.length != 0) {
                return null;
            }

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
            buf.append(uuid);
            final String className = buf.toString().replace('.', '_').replace('-', '_');

            System system = SystemLoader.getSystem(uuid);

            ClassWriter cw = new ClassWriter(true);

            createMemberFields(joinPointType, cw, className);

            if (createInitMethod(joinPointType, cw, className, aroundAdvices, beforeAdvices, afterAdvices, system)) {
                return null;  // bail out, one of the advice has deployment model that is not supported, use regular join point instance
            }

            createGetSignatureMethod(joinPointType, cw, className);

            Signature signature =
                    createProceedMethod(
                            joinPointType, cw, className, system, declaringClass, joinPointHash,
                            aroundAdvices, beforeAdvices, afterAdvices
                    );
            cw.visitEnd();

            if (JIT_CLASS_DUMP_DIR != null) {
                AsmHelper.dumpClass(JIT_CLASS_DUMP_DIR, className, cw);
            }

            Class joinPointClass = AsmHelper.loadClass(cw.toByteArray(), className.replace('/', '.'));

            Constructor constructor = joinPointClass.getDeclaredConstructor(
                    new Class[]{
                        String.class, int.class, Class.class, Signature.class
                    }
            );
            return (JoinPoint)constructor.newInstance(
                    new Object[]{uuid, new Integer(joinPointType), declaringClass, signature}
            );
        }
        catch (Throwable e) {
            e.printStackTrace();
            java.lang.System.err.println(
                    "WARNING: could not dynamically create, compile and load a JoinPoint class for join point with hash " +
                    joinPointHash + ": " +
                    e.toString()
            );
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
        cw.visit(
                Constants.ACC_PUBLIC + Constants.ACC_SUPER, className,
                JOIN_POINT_BASE_CLASS_NAME, null, null
        );
        cw.visitField(Constants.ACC_PRIVATE, STACKFRAME_FIELD_NAME, I, null, null);
        cw.visitField(Constants.ACC_PRIVATE, SYSTEM_FIELD_NAME, SYSTEM_CLASS_SIGNATURE, null, null);

        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                cw.visitField(
                        Constants.ACC_PRIVATE, SIGNATURE_FIELD_NAME, METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE, null, null
                );
                break;

            case JoinPointType.CONSTRUCTOR_CALL:
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                cw.visitField(
                        Constants.ACC_PRIVATE, SIGNATURE_FIELD_NAME, CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE, null,
                        null
                );
                break;

            case JoinPointType.FIELD_SET:
            case JoinPointType.FIELD_GET:
                cw.visitField(
                        Constants.ACC_PRIVATE, SIGNATURE_FIELD_NAME, FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE, null, null
                );
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
     * @param system        c
     * @return true if the JIT compilation should be skipped
     */
    private static boolean createInitMethod(
            final int joinPointType,
            final ClassWriter cw,
            final String className,
            final IndexTuple[] aroundAdvices,
            final IndexTuple[] beforeAdvices,
            final IndexTuple[] afterAdvices,
            final System system) {

        CodeVisitor cv =
                cw.visitMethod(
                        Constants.ACC_PUBLIC, INIT_METHOD_NAME, JIT_JOIN_POINT_INIT_METHOD_SIGNATURE, null, null
                );

        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitVarInsn(Constants.ALOAD, 1);
        cv.visitVarInsn(Constants.ILOAD, 2);
        cv.visitVarInsn(Constants.ALOAD, 3);
        cv.visitInsn(Constants.ACONST_NULL);
        cv.visitInsn(Constants.ACONST_NULL);
        cv.visitInsn(Constants.ACONST_NULL);
        cv.visitMethodInsn(
                Constants.INVOKESPECIAL, JOIN_POINT_BASE_CLASS_NAME, INIT_METHOD_NAME,
                JOIN_POINT_BASE_INIT_METHOD_SIGNATURE
        );

        // init stack frame
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitInsn(Constants.ICONST_M1);
        cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);

        // init signature
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitVarInsn(Constants.ALOAD, 4);
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                cv.visitTypeInsn(Constants.CHECKCAST, METHOD_SIGNATURE_IMPL_CLASS_NAME);
                cv.visitFieldInsn(
                        Constants.PUTFIELD, className, SIGNATURE_FIELD_NAME, METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
                );
                break;

            case JoinPointType.CONSTRUCTOR_CALL:
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                cv.visitTypeInsn(Constants.CHECKCAST, CONSTRUCTOR_SIGNATURE_IMPL_CLASS_NAME);
                cv.visitFieldInsn(
                        Constants.PUTFIELD, className, SIGNATURE_FIELD_NAME,
                        CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE
                );
                break;

            case JoinPointType.FIELD_SET:
            case JoinPointType.FIELD_GET:
                cv.visitTypeInsn(Constants.CHECKCAST, FIELD_SIGNATURE_IMPL_CLASS_NAME);
                cv.visitFieldInsn(
                        Constants.PUTFIELD, className, SIGNATURE_FIELD_NAME, FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE
                );
                break;

            case JoinPointType.HANDLER:
                throw new UnsupportedOperationException("handler is not support yet");

            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");
        }

        // init system
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitVarInsn(Constants.ALOAD, 1);
        cv.visitMethodInsn(
                Constants.INVOKESTATIC, SYSTEM_LOADER_CLASS_NAME, GET_SYSTEM_METHOD_NAME,
                GET_SYSTEM_METHOD_NAME_SIGNATURE
        );
        cv.visitFieldInsn(Constants.PUTFIELD, className, SYSTEM_FIELD_NAME, SYSTEM_CLASS_SIGNATURE);

        // init around advice
        for (int i = 0; i < aroundAdvices.length; i++) {
            IndexTuple aroundAdvice = aroundAdvices[i];
            Aspect aspect = system.getAspectManager().getAspect(aroundAdvice.getAspectIndex());
            String aspectClassName = aspect.getClass().getName().replace('.', '/');

            String aspectFieldName = AROUND_ADVICE_FIELD_PREFIX + i;
            String aspectClassSignature = L + aspectClassName + SEMICOLON;

            // add the aspect field
            cw.visitField(Constants.ACC_PRIVATE, aspectFieldName, aspectClassSignature, null, null);

            // handle the init in the constructor
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitFieldInsn(Constants.GETFIELD, className, SYSTEM_FIELD_NAME, SYSTEM_CLASS_SIGNATURE);
            cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL, SYSTEM_CLASS_NAME, GET_ASPECT_MANAGER_METHOD_NAME,
                    GET_ASPECT_MANAGER_METHOD_NAME_SIGNATURE
            );
            cv.visitIntInsn(Constants.BIPUSH, aroundAdvice.getAspectIndex());
            cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL, ASPECT_MANAGER_CLASS_NAME,
                    GET_ASPECT_METHOD_NAME, GET_ASPECT_METHOD_SIGNATURE
            );

            switch (aspect.___AW_getDeploymentModel()) {
                case DeploymentModel.PER_JVM:
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, ASPECT_CLASS_NAME,
                            GET_PER_JVM_ASPECT_METHOD_NAME,
                            GET_PER_JVM_ASPECT_METHOD_SIGNATURE
                    );
                    break;

                case DeploymentModel.PER_CLASS:
                    cv.visitVarInsn(Constants.ALOAD, 0);
                    cv.visitFieldInsn(Constants.GETFIELD, className, TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, ASPECT_CLASS_NAME,
                            GET_PER_CLASS_ASPECT_METHOD_NAME,
                            GET_PER_CLASS_ASPECT_METHOD_SIGNATURE
                    );
                    break;
//                            case DeploymentModel.PER_INSTANCE:
//                                cv.visitVarInsn(Constants.ALOAD, 0);
//                                cv.visitFieldInsn(
//                                        Constants.GETFIELD, className, TARGET_INSTANCE_FIELD_NAME, "Ljava/lang/Object;"
//                                );
//                                cv.visitMethodInsn(
//                                        Constants.INVOKEVIRTUAL, "org/codehaus/aspectwerkz/aspect/Aspect",
//                                        "___AW_getPerInstanceAspect",
//                                        "(Ljava/lang/Object;)Lorg/codehaus/aspectwerkz/aspect/Aspect;"
//                                `);
//                                break;
//                                return null;
//
//                            case DeploymentModel.PER_THREAD:

                default:
                    return true;
            }

            cv.visitTypeInsn(Constants.CHECKCAST, aspectClassName);
            cv.visitFieldInsn(Constants.PUTFIELD, className, aspectFieldName, aspectClassSignature);
        }

        cv.visitInsn(Constants.RETURN);
        cv.visitMaxs(0, 0);

        return false;
    }

    /**
     * Creates a new getSignature method.
     *
     * @param joinPointType
     * @param cw
     * @param className
     */
    private static void createGetSignatureMethod(
            final int joinPointType, final ClassWriter cw, final String className) {
        CodeVisitor cv =
                cw.visitMethod(
                        Constants.ACC_PUBLIC, GET_SIGNATURE_METHOD_NAME, GET_SIGNATURE_METHOD_SIGNATURE, null,
                        null
                );

        cv.visitVarInsn(Constants.ALOAD, 0);

        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                cv.visitFieldInsn(
                        Constants.GETFIELD, className, SIGNATURE_FIELD_NAME,
                        METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
                );
                break;

            case JoinPointType.CONSTRUCTOR_CALL:
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                cv.visitFieldInsn(
                        Constants.GETFIELD, className, SIGNATURE_FIELD_NAME,
                        CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE
                );
                break;

            case JoinPointType.FIELD_SET:
            case JoinPointType.FIELD_GET:
                cv.visitFieldInsn(
                        Constants.GETFIELD, className, SIGNATURE_FIELD_NAME,
                        FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE
                );
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
     * Create the proceed() method.
     *
     * @param joinPointType
     * @param cw
     * @param className
     * @param system
     * @param declaringClass
     * @param joinPointHash
     * @param aroundAdvices
     * @param beforeAdvices
     * @param afterAdvices
     * @return the signature
     */
    private static Signature createProceedMethod(
            final int joinPointType,
            final ClassWriter cw,
            final String className,
            final System system,
            final Class declaringClass,
            final int joinPointHash,
            final IndexTuple[] aroundAdvices,
            final IndexTuple[] beforeAdvices,
            final IndexTuple[] afterAdvices) {

        Signature signature = null;

        CodeVisitor cv = cw.visitMethod(
                Constants.ACC_PUBLIC, PROCEED_METHOD_NAME, PROCEED_METHOD_SIGNATURE, new String[]{
                    THROWABLE_CLASS_NAME
                },
                null
        );

        incrementStackFrameCounter(cv, className);

        LabelData labelData = invokeAroundAdvice(cv, className, aroundAdvices, system);

        resetStackFrameCounter(cv, className);

        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
            case JoinPointType.METHOD_CALL:
                String declaringClassName = null;
                Type[] argTypes = new Type[0];
                MethodTuple methodTuple = system.getAspectManager().getMethodTuple(declaringClass, joinPointHash);
                Method targetMethod = methodTuple.getOriginalMethod();
                declaringClassName = targetMethod.getDeclaringClass().getName().replace('.', '/');
                String methodName = targetMethod.getName();
                String methodDescriptor = Type.getMethodDescriptor(targetMethod);
                signature = new MethodSignatureImpl(methodTuple.getDeclaringClass(), methodTuple);
                argTypes = Type.getArgumentTypes(targetMethod);
                if (Modifier.isPublic(targetMethod.getModifiers())) {
                    invokePublicMethod(
                            targetMethod, cv, joinPointType, argTypes, className,
                            declaringClassName, methodName, methodDescriptor
                    );
                }
                else {
                    invokeNonPublicMethod(cv);
                }
                setReturnValue(targetMethod, cv, className);
                break;

            case JoinPointType.CONSTRUCTOR_CALL:
                ConstructorTuple constructorTuple = system.getAspectManager().getConstructorTuple(
                        declaringClass, joinPointHash
                );
                Constructor targetConstructor = constructorTuple.getWrapperConstructor();
                declaringClassName = targetConstructor.getDeclaringClass().getName().replace('.', '/');
                String constructorDescriptor = AsmHelper.getConstructorDescriptor(targetConstructor);
                signature = new ConstructorSignatureImpl(constructorTuple.getDeclaringClass(), constructorTuple);
                argTypes = AsmHelper.getArgumentTypes(targetConstructor);
                if (Modifier.isPublic(targetConstructor.getModifiers())) {
                    invokePublicConstructorCall(
                            joinPointType, argTypes, cv, className, declaringClassName, constructorDescriptor
                    );
                }
                else {
                    invokeNonPublicConstructorCall(cv);
                }
                setNewInstance(cv, className);
                break;

            case JoinPointType.CONSTRUCTOR_EXECUTION:
                constructorTuple = system.getAspectManager().getConstructorTuple(declaringClass, joinPointHash);
                targetConstructor = constructorTuple.getOriginalConstructor();
                declaringClassName = targetConstructor.getDeclaringClass().getName().replace('.', '/');
                constructorDescriptor = AsmHelper.getConstructorDescriptor(targetConstructor);
                signature = new ConstructorSignatureImpl(constructorTuple.getDeclaringClass(), constructorTuple);
                argTypes = AsmHelper.getArgumentTypes(targetConstructor);
                // remove the last argument (the dummy JoinPointManager type)
                Type[] newArgTypes = new Type[argTypes.length - 1];
                for (int i = 0; i < newArgTypes.length; i++) {
                    newArgTypes[i] = argTypes[i];
                }
                if (Modifier.isPublic(targetConstructor.getModifiers())) {
                    invokePublicConstructorExecution(
                            joinPointType, newArgTypes, cv, className, declaringClassName, constructorDescriptor
                    );
                }
                else {
                    invokeNonPublicConstructorExecution(cv);
                }
                setNewInstance(cv, className);
                break;

            case JoinPointType.FIELD_SET:
                Field setField = system.getAspectManager().getField(declaringClass, joinPointHash);
                signature = new FieldSignatureImpl(setField.getDeclaringClass(), setField);
                invokeTargetFieldSet(cv);
                setFieldValue(cv, className);
                break;

            case JoinPointType.FIELD_GET:
                Field getField = system.getAspectManager().getField(declaringClass, joinPointHash);
                signature = new FieldSignatureImpl(getField.getDeclaringClass(), getField);
                invokeTargetFieldGet(cv);
                setFieldValue(cv, className);
                break;

            case JoinPointType.HANDLER:
                throw new UnsupportedOperationException("handler is not support yet");

            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("static initialization is not support yet");

            default:
                return null;
        }

        cv.visitInsn(Constants.ARETURN);

        // handle the final try-finally nastyness
        cv.visitTryCatchBlock(labelData.startLabel, labelData.returnLabels[0], labelData.handlerLabel, null);
        for (int i = 1; i < labelData.switchCaseLabels.length; i++) {
            Label switchCaseLabel = labelData.switchCaseLabels[i];
            Label returnLabel = labelData.returnLabels[i];
            cv.visitTryCatchBlock(switchCaseLabel, returnLabel, labelData.handlerLabel, null);
        }
        cv.visitTryCatchBlock(labelData.handlerLabel, labelData.endLabel, labelData.handlerLabel, null);

        cv.visitMaxs(0, 0);

        return signature;
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
            cv.visitFieldInsn(
                    Constants.GETFIELD, className, SIGNATURE_FIELD_NAME,
                    METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
            );
            cv.visitVarInsn(Constants.ALOAD, 1);
            cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL, METHOD_SIGNATURE_IMPL_CLASS_NAME,
                    SET_RETURN_VALUE_METHOD_NAME, SET_RETURN_VALUE_METHOD_SIGNATURE
            );
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
        cv.visitFieldInsn(
                Constants.GETFIELD, className, SIGNATURE_FIELD_NAME,
                CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE
        );
        cv.visitVarInsn(Constants.ALOAD, 1);
        cv.visitMethodInsn(
                Constants.INVOKEVIRTUAL, CONSTRUCTOR_SIGNATURE_IMPL_CLASS_NAME,
                SET_NEW_INSTANCE_METHOD_NAME, SET_NEW_INSTANCE_METHOD_SIGNATURE
        );
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
            cv.visitFieldInsn(
                    Constants.GETFIELD, className, SIGNATURE_FIELD_NAME,
                    FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE
            );
            cv.visitVarInsn(Constants.ALOAD, 1);
            cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL, FIELD_SIGNATURE_IMPL_CLASS_NAME,
                    SET_FIELD_VALUE_METHOD_NAME, SET_FIELD_VALUE_METHOD_SIGNATURE
            );
            cv.visitVarInsn(Constants.ALOAD, 1);
    }

    /**
     * Handles invocation of a public method.
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
    private static void invokePublicMethod(
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
            cv.visitFieldInsn(Constants.GETFIELD, className, TARGET_INSTANCE_FIELD_NAME, OBJECT_CLASS_SIGNATURE);
            cv.visitTypeInsn(Constants.CHECKCAST, declaringClassName);
        }

        unwrapParameters(argTypes, cv);

        // invoke the target method (static or member) statically
        if (Modifier.isStatic(targetMethod.getModifiers())) {
            cv.visitMethodInsn(Constants.INVOKESTATIC, declaringClassName, methodName, methodDescriptor);
        }
        else {
            cv.visitMethodInsn(Constants.INVOKEVIRTUAL, declaringClassName, methodName, methodDescriptor);
        }
        wrapReturnValue(targetMethod, cv);
    }

    /**
     * Handles invocation of a non-public method.
     *
     * @param cv
     */
    private static void invokeNonPublicMethod(final CodeVisitor cv) {
        // method is non-public -> invoke using reflection
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
                Constants.INVOKESTATIC,
                JOIN_POINT_BASE_CLASS_NAME,
                INVOKE_TARGET_METHOD_METHOD_NAME,
                INVOKE_TARGET_METHOD_METHOD_SIGNATURE
        );
    }

    /**
     * Handles invocation of a public constructor - call context.
     *
     * @param joinPointType
     * @param argTypes
     * @param cv
     * @param className
     * @param declaringClassName
     * @param constructorDescriptor
     */
    private static void invokePublicConstructorCall(
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
     * Handles invocation of a non-public constructor - call context.
     *
     * @param cv
     */
    private static void invokeNonPublicConstructorCall(final CodeVisitor cv) {
        // constructor is non-public -> invoke using reflection
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
                Constants.INVOKESTATIC,
                JOIN_POINT_BASE_CLASS_NAME,
                INVOKE_TARGET_CONSTRUCTOR_CALL_METHOD_NAME,
                INVOKE_TARGET_CONSTRUCTOR_CALL_METHOD_SIGNATURE
        );
    }

    /**
     * Handles invocation of a public constructor - execution context.
     *
     * @param joinPointType
     * @param newArgTypes
     * @param cv
     * @param className
     * @param declaringClassName
     * @param constructorDescriptor
     */
    private static void invokePublicConstructorExecution(
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
     * Handles invocation of a non-public constructor - execution context.
     *
     * @param cv
     */
    private static void invokeNonPublicConstructorExecution(final CodeVisitor cv) {
        // constructor is non-public -> invoke using reflection
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
                Constants.INVOKESTATIC,
                JOIN_POINT_BASE_CLASS_NAME,
                INVOKE_TARGET_CONSTRUCTOR_EXECUTION_METHOD_NAME,
                INVOKE_TARGET_CONSTRUCTOR_EXECUTION_METHOD_SIGNATURE
        );
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
                GET_TARGET_FIELD_METHOD_SIGNATURE
        );
    }

    /**
     * Handles invocation of a field - set context.
     *
     * @param cv
     */
    private static void invokeTargetFieldSet(final CodeVisitor cv) {
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitMethodInsn(
                Constants.INVOKESTATIC,
                JOIN_POINT_BASE_CLASS_NAME,
                SET_TARGET_FIELD_METHOD_NAME,
                SET_TARGET_FIELD_METHOD_SIGNATURE
        );
        cv.visitInsn(Constants.ACONST_NULL);
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
     * Handles the around advice invocations.
     * <p/>
     * Creates a switch clause in which the around advice chain is called recursively.
     * <p/>
     * Wraps the switch clause in a try-finally statement in which the finally block resets the stack frame counter.
     *
     * @param cv
     * @param className
     * @param aroundAdvices
     * @param system
     * @return the labels needed to implement the last part of the try-finally clause
     */
    private static LabelData invokeAroundAdvice(
            final CodeVisitor cv,
            final String className,
            final IndexTuple[] aroundAdvices,
            final System system) {

        // try-finally management
        Label startLabel = new Label();
        cv.visitLabel(startLabel);

        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitFieldInsn(Constants.GETFIELD, className, STACKFRAME_FIELD_NAME, I);

        // creates the labels needed for the switch and try-finally blocks
        Label[] switchCaseLabels = new Label[aroundAdvices.length];
        Label[] returnLabels = new Label[aroundAdvices.length];
        for (int i = 0; i < switchCaseLabels.length; i++) {
            switchCaseLabels[i] = new Label();
        }
        for (int i = 0; i < returnLabels.length; i++) {
            returnLabels[i] = new Label();
        }
        Label defaultCaseLabel = new Label();
        Label gotoLabel = new Label();
        Label handlerLabel = new Label();
        Label endLabel = new Label();

        // create the switch table
        cv.visitTableSwitchInsn(0, switchCaseLabels.length - 1, defaultCaseLabel, switchCaseLabels);

        // add invocations to the around advices
        for (int i = 0; i < aroundAdvices.length; i++) {
            IndexTuple aroundAdvice = aroundAdvices[i];
            Aspect aspect = system.getAspectManager().getAspect(aroundAdvice.getAspectIndex());
            Method adviceMethod = aspect.___AW_getAdvice(aroundAdvice.getMethodIndex());
            String aspectClassName = aspect.getClass().getName().replace('.', '/');

            String aspectFieldName = AROUND_ADVICE_FIELD_PREFIX + i;
            String aspectClassSignature = L + aspectClassName + SEMICOLON;

            cv.visitLabel(switchCaseLabels[i]);
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitFieldInsn(Constants.GETFIELD, className, aspectFieldName, aspectClassSignature);
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitMethodInsn(
                    Constants.INVOKEVIRTUAL, aspectClassName, adviceMethod.getName(), ADVICE_METHOD_SIGNATURE
            );

            // try-finally management
            cv.visitVarInsn(Constants.ASTORE, 2);
            cv.visitLabel(returnLabels[i]);
            cv.visitVarInsn(Constants.ALOAD, 0);
            cv.visitInsn(Constants.ICONST_M1);
            cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);
            cv.visitVarInsn(Constants.ALOAD, 2);

            cv.visitInsn(Constants.ARETURN);
        }
        cv.visitLabel(defaultCaseLabel);

        // try-finally management
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitInsn(Constants.ICONST_M1);
        cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);
        cv.visitJumpInsn(Constants.GOTO, gotoLabel);
        cv.visitLabel(handlerLabel);
        cv.visitVarInsn(Constants.ASTORE, 3);
        cv.visitLabel(endLabel);
        cv.visitVarInsn(Constants.ALOAD, 0);
        cv.visitInsn(Constants.ICONST_M1);
        cv.visitFieldInsn(Constants.PUTFIELD, className, STACKFRAME_FIELD_NAME, I);
        cv.visitVarInsn(Constants.ALOAD, 3);
        cv.visitInsn(Constants.ATHROW);
        cv.visitLabel(gotoLabel);

        // put the labels in a data structure and return them
        LabelData labelData = new LabelData();
        labelData.switchCaseLabels = switchCaseLabels;
        labelData.returnLabels = returnLabels;
        labelData.startLabel = startLabel;
        labelData.gotoLabel = gotoLabel;
        labelData.handlerLabel = handlerLabel;
        labelData.endLabel = endLabel;
        return labelData;
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
                    cv.visitFieldInsn(
                            Constants.GETFIELD, className, SIGNATURE_FIELD_NAME,
                            METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
                    );
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, METHOD_SIGNATURE_IMPL_CLASS_NAME,
                            GET_PARAMETER_VALUES_METHOD_NAME, GET_PARAMETER_VALUES_METHOD_SIGNATURE
                    );
                    break;

                case JoinPointType.CONSTRUCTOR_EXECUTION:
                case JoinPointType.CONSTRUCTOR_CALL:
                    cv.visitFieldInsn(
                            Constants.GETFIELD, className, SIGNATURE_FIELD_NAME,
                            CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE
                    );
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, CONSTRUCTOR_SIGNATURE_IMPL_CLASS_NAME,
                            GET_PARAMETER_VALUES_METHOD_NAME, GET_PARAMETER_VALUES_METHOD_SIGNATURE
                    );
                    break;

                case JoinPointType.FIELD_GET:
                case JoinPointType.FIELD_SET:
                    cv.visitFieldInsn(
                            Constants.GETFIELD, className, SIGNATURE_FIELD_NAME,
                            FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE
                    );
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, FIELD_SIGNATURE_IMPL_CLASS_NAME,
                            GET_PARAMETER_VALUES_METHOD_NAME, GET_PARAMETER_VALUES_METHOD_SIGNATURE
                    );
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
            AsmHelper.setICONST_X(cv, f);
            cv.visitInsn(Constants.AALOAD);

            Type argType = argTypes[f];
            switch (argType.getSort()) {
                case Type.SHORT:
                    cv.visitTypeInsn(Constants.CHECKCAST, SHORT_CLASS_NAME);
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, SHORT_CLASS_NAME, SHORT_VALUE_METHOD_NAME,
                            SHORT_VALUE_METHOD_SIGNATURE
                    );
                    break;
                case Type.INT:
                    cv.visitTypeInsn(Constants.CHECKCAST, INTEGER_CLASS_NAME);
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, INTEGER_CLASS_NAME, INT_VALUE_METHOD_NAME,
                            INT_VALUE_METHOD_SIGNATURE
                    );
                    break;
                case Type.LONG:
                    cv.visitTypeInsn(Constants.CHECKCAST, LONG_CLASS_NAME);
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, LONG_CLASS_NAME, LONG_VALUE_METHOD_NAME,
                            LONG_VALUE_METHOD_SIGNATURE
                    );
                    break;
                case Type.FLOAT:
                    cv.visitTypeInsn(Constants.CHECKCAST, FLOAT_CLASS_NAME);
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, FLOAT_CLASS_NAME, FLOAT_VALUE_METHOD_NAME,
                            FLOAT_VALUE_METHOD_SIGNATURE
                    );
                    break;
                case Type.DOUBLE:
                    cv.visitTypeInsn(Constants.CHECKCAST, DOUBLE_CLASS_NAME);
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, DOUBLE_CLASS_NAME, DOUBLE_VALUE_METHOD_NAME,
                            DOUBLE_VALUE_METHOD_SIGNATURE
                    );
                    break;
                case Type.BYTE:
                    cv.visitTypeInsn(Constants.CHECKCAST, BYTE_CLASS_NAME);
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, BYTE_CLASS_NAME, BYTE_VALUE_METHOD_NAME,
                            BYTE_VALUE_METHOD_SIGNATURE
                    );
                    break;
                case Type.BOOLEAN:
                    cv.visitTypeInsn(Constants.CHECKCAST, BOOLEAN_CLASS_NAME);
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, BOOLEAN_CLASS_NAME, BOOLEAN_VALUE_METHOD_NAME,
                            BOOLEAN_VALUE_METHOD_SIGNATURE
                    );
                    break;
                case Type.CHAR:
                    cv.visitTypeInsn(Constants.CHECKCAST, CHARACTER_CLASS_NAME);
                    cv.visitMethodInsn(
                            Constants.INVOKEVIRTUAL, CHARACTER_CLASS_NAME, CHAR_VALUE_METHOD_NAME,
                            CHAR_VALUE_METHOD_SIGNATURE
                    );
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
                        Constants.INVOKESPECIAL, SHORT_CLASS_NAME, INIT_METHOD_NAME,
                        SHORT_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.INT:
                cv.visitMethodInsn(
                        Constants.INVOKESPECIAL, INTEGER_CLASS_NAME, INIT_METHOD_NAME,
                        INTEGER_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.LONG:
                cv.visitMethodInsn(
                        Constants.INVOKESPECIAL, LONG_CLASS_NAME, INIT_METHOD_NAME,
                        LONG_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.FLOAT:
                cv.visitMethodInsn(
                        Constants.INVOKESPECIAL, FLOAT_CLASS_NAME, INIT_METHOD_NAME,
                        FLOAT_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.DOUBLE:
                cv.visitMethodInsn(
                        Constants.INVOKESPECIAL, DOUBLE_CLASS_NAME, INIT_METHOD_NAME,
                        DOUBLE_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.BYTE:
                cv.visitMethodInsn(
                        Constants.INVOKESPECIAL, BYTE_CLASS_NAME, INIT_METHOD_NAME,
                        BYTE_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.BOOLEAN:
                cv.visitMethodInsn(
                        Constants.INVOKESPECIAL, BOOLEAN_CLASS_NAME, INIT_METHOD_NAME,
                        BOOLEAN_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.CHAR:
                cv.visitMethodInsn(
                        Constants.INVOKESPECIAL, CHARACTER_CLASS_NAME, INIT_METHOD_NAME,
                        CHARACTER_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
        }
    }

    /**
     * Data structure for the labels needed in the switch and try-finally blocks in the proceed method.
     */
    static class LabelData {
        public Label[] switchCaseLabels = null;
        public Label[] returnLabels = null;
        public Label startLabel = null;
        public Label gotoLabel = null;
        public Label handlerLabel = null;
        public Label endLabel = null;
    }
}


/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Constants;

/**
 * Interface with common constants used in the transformation process.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public interface TransformationConstants extends Constants {
    public static final List EMTPTY_ARRAY_LIST = new ArrayList();

    // prefixes
    public static final String DELIMITER = "$_AW_$";
    public static final String ASPECTWERKZ_PREFIX = "___AW_";
    public static final String WRAPPER_METHOD_PREFIX = "aw$";
    public static final String SYNTHETIC_MEMBER_PREFIX = "aw$";
    public static final String ORIGINAL_METHOD_PREFIX = WRAPPER_METHOD_PREFIX + "original" + DELIMITER;
    public static final String INVOKE_WRAPPER_METHOD_PREFIX = "INVOKE" + DELIMITER;
    public static final String PUTFIELD_WRAPPER_METHOD_PREFIX = "PUTFIELD" + DELIMITER;
    public static final String GETFIELD_WRAPPER_METHOD_PREFIX = "GETFIELD" + DELIMITER;
    public static final String JOIN_POINT_CLASS_SUFFIX = ASPECTWERKZ_PREFIX + "JoinPoint";
    public static final String ASPECTWERKZ_PACKAGE_NAME = "org/codehaus/aspectwerkz";

    // internal fields
    public static final String SERIAL_VERSION_UID_FIELD_NAME = "serialVersionUID";
    public static final String TARGET_CLASS_FIELD_NAME = SYNTHETIC_MEMBER_PREFIX + "clazz";

    // internal methods
    public static final String INIT_JOIN_POINTS_METHOD_NAME = WRAPPER_METHOD_PREFIX + "initJoinPoints";
    public static final String STATIC_INITIALIZATION_METHOD_NAME = WRAPPER_METHOD_PREFIX + "staticinitialization";

    // method and class names
    public static final String INIT_METHOD_NAME = "<init>";
    public static final String CLINIT_METHOD_NAME = "<clinit>";
    public static final String CLASS_LOADER_REFLECT_CLASS_NAME = "java.lang.ClassLoader";
    public static final String CLASS_LOADER_CLASS_NAME = "java/lang/ClassLoader";
    public static final String DEFINE_CLASS_METHOD_NAME = "defineClass";
    public static final String INVOKE_METHOD_NAME = "invoke";
    public static final String FOR_NAME_METHOD_NAME = "forName";
    public static final String LOAD_JOIN_POINT_METHOD_NAME = "loadJoinPoint";
    public static final String MIXINS_CLASS_NAME = "org/codehaus/aspectwerkz/aspect/management/Mixins";
    public static final String MIXIN_OF_METHOD_NAME = "mixinOf";
    public static final String MIXIN_OF_METHOD_PER_CLASS_SIGNATURE = "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;";
    public static final String MIXIN_OF_METHOD_PER_INSTANCE_SIGNATURE = "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;";

    // java types and signatures
    public static final String SHORT_CLASS_NAME = "java/lang/Short";
    public static final String INTEGER_CLASS_NAME = "java/lang/Integer";
    public static final String LONG_CLASS_NAME = "java/lang/Long";
    public static final String FLOAT_CLASS_NAME = "java/lang/Float";
    public static final String DOUBLE_CLASS_NAME = "java/lang/Double";
    public static final String BYTE_CLASS_NAME = "java/lang/Byte";
    public static final String BOOLEAN_CLASS_NAME = "java/lang/Boolean";
    public static final String CHARACTER_CLASS_NAME = "java/lang/Character";
    public static final String OBJECT_CLASS_SIGNATURE = "Ljava/lang/Object;";
    public static final String OBJECT_CLASS_NAME = "java/lang/Object";
    public static final String CLASS_CLASS_SIGNATURE = "Ljava/lang/Class;";
    public static final String CLASS_CLASS = "java/lang/Class";
    public static final String THROWABLE_CLASS_NAME = "java/lang/Throwable";
    public static final String SHORT_VALUE_METHOD_NAME = "shortValue";
    public static final String INT_VALUE_METHOD_NAME = "intValue";
    public static final String LONG_VALUE_METHOD_NAME = "longValue";
    public static final String FLOAT_VALUE_METHOD_NAME = "floatValue";
    public static final String DOUBLE_VALUE_METHOD_NAME = "doubleValue";
    public static final String BYTE_VALUE_METHOD_NAME = "byteValue";
    public static final String BOOLEAN_VALUE_METHOD_NAME = "booleanValue";
    public static final String CHAR_VALUE_METHOD_NAME = "charValue";
    public static final String CHAR_VALUE_METHOD_SIGNATURE = "()C";
    public static final String BOOLEAN_VALUE_METHOD_SIGNATURE = "()Z";
    public static final String BYTE_VALUE_METHOD_SIGNATURE = "()B";
    public static final String DOUBLE_VALUE_METHOD_SIGNATURE = "()D";
    public static final String FLOAT_VALUE_METHOD_SIGNATURE = "()F";
    public static final String LONG_VALUE_METHOD_SIGNATURE = "()J";
    public static final String INT_VALUE_METHOD_SIGNATURE = "()I";
    public static final String SHORT_VALUE_METHOD_SIGNATURE = "()S";
    public static final String SHORT_CLASS_INIT_METHOD_SIGNATURE = "(S)V";
    public static final String INTEGER_CLASS_INIT_METHOD_SIGNATURE = "(I)V";
    public static final String LONG_CLASS_INIT_METHOD_SIGNATURE = "(J)V";
    public static final String FLOAT_CLASS_INIT_METHOD_SIGNATURE = "(F)V";
    public static final String DOUBLE_CLASS_INIT_METHOD_SIGNATURE = "(D)V";
    public static final String BYTE_CLASS_INIT_METHOD_SIGNATURE = "(B)V";
    public static final String BOOLEAN_CLASS_INIT_METHOD_SIGNATURE = "(Z)V";
    public static final String CHARACTER_CLASS_INIT_METHOD_SIGNATURE = "(C)V";
    public static final String NO_PARAMS_RETURN_VOID_METHOD_SIGNATURE = "()V";
    public static final String CLASS_CLASS_GETCLASSLOADER_METHOD_SIGNATURE = "()Ljava/lang/ClassLoader;";
    public static final String L = "L";
    public static final String I = "I";
    public static final String SEMICOLON = ";";

    public static final byte EMPTY_WRAPPER_ATTRIBUTE_VALUE_EMPTY = Byte.MIN_VALUE;
    public static final byte EMPTY_WRAPPER_ATTRIBUTE_VALUE_NOTEMPTY = Byte.MAX_VALUE;

    // optim flags
    public static final boolean OPTIMIZED_JOIN_POINT = true;
    public static final boolean NON_OPTIMIZED_JOIN_POINT = false;

    // static and member field names
    public static final String MIXIN_FIELD_NAME = SYNTHETIC_MEMBER_PREFIX + "MIXIN_";
    public static final String SIGNATURE_FIELD_NAME = "SIGNATURE";
    public static final String META_DATA_FIELD_NAME = "META_DATA";
    public static final String ASPECT_FIELD_PREFIX = "ASPECT_";
    public static final String STACK_FRAME_COUNTER_FIELD_NAME = "STACK_FRAME_COUNTER";
    public static final String INTERCEPTOR_INDEX_FIELD_NAME = "INTERCEPTOR_INDEX";
    public static final String CALLEE_INSTANCE_FIELD_NAME = "CALLEE";
    public static final String CALLER_INSTANCE_FIELD_NAME = "CALLER";
    public static final String ARGUMENT_FIELD = "ARGUMENT_";
    public static final String RETURN_VALUE_FIELD_NAME = "RETURN_VALUE";
    public static final String OPTIMIZED_JOIN_POINT_INSTANCE_FIELD_NAME = "OPTIMIZED_JOIN_POINT";

    public static final String AROUND_INTERCEPTORS_FIELD_NAME = "AROUND_INTERCEPTORS";
    public static final String NR_OF_AROUND_INTERCEPTORS_FIELD_NAME = "NR_OF_AROUND_INTERCEPTORS";
    public static final String BEFORE_INTERCEPTORS_FIELD_NAME = "BEFORE_INTERCEPTORS";
    public static final String NR_OF_BEFORE_INTERCEPTORS_FIELD_NAME = "NR_OF_BEFORE_INTERCEPTORS";
    public static final String AFTER_INTERCEPTORS_FIELD_NAME = "AFTER_INTERCEPTORS";
    public static final String NR_OF_AFTER_INTERCEPTORS_FIELD_NAME = "NR_OF_AFTER_INTERCEPTORS";
    public static final String AFTER_RETURNING_INTERCEPTORS_FIELD_NAME = "AFTER_RETURNING_INTERCEPTORS";
    public static final String NR_OF_AFTER_RETURNING_INTERCEPTORS_FIELD_NAME = "NR_OF_AFTER_RETURNING_INTERCEPTORS";
    public static final String AFTER_THROWING_INTERCEPTORS_FIELD_NAME = "AFTER_THROWING_INTERCEPTORS";
    public static final String NR_OF_AFTER_THROWING_INTERCEPTORS_FIELD_NAME = "NR_OF_AFTER_THROWING_INTERCEPTORS";

    // runtime system signatures and types
    public static final String JOIN_POINT_MANAGER_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/management/JoinPointManager";
    public static final String LOAD_JOIN_POINT_METHOD_SIGNATURE = "(ILjava/lang/Class;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;)V";
    public static final String FOR_NAME_METHOD_SIGNATURE = "(Ljava/lang/String;)Ljava/lang/Class;";
    public static final String METHOD_SIGNATURE_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/MethodSignatureImpl";
    public static final String METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/MethodSignatureImpl;";
    public static final String CONSTRUCTOR_SIGNATURE_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/ConstructorSignatureImpl;";
    public static final String FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/FieldSignatureImpl;";
    public static final String HANDLER_SIGNATURE_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/CatchClauseSignatureImpl;";
    public static final String NEW_METHOD_SIGNATURE_METHOD_SIGNATURE = "(Ljava/lang/Class;I)Lorg/codehaus/aspectwerkz/joinpoint/impl/MethodSignatureImpl;";
    public static final String NEW_CONSTRUCTOR_SIGNATURE_METHOD_SIGNATURE = "(Ljava/lang/Class;I)Lorg/codehaus/aspectwerkz/joinpoint/impl/ConstructorSignatureImpl;";
    public static final String NEW_FIELD_SIGNATURE_METHOD_SIGNATURE = "(Ljava/lang/Class;I)Lorg/codehaus/aspectwerkz/joinpoint/impl/FieldSignatureImpl;";
    public static final String NEW_HANDLER_SIGNATURE_METHOD_SIGNATURE = "(Ljava/lang/Class;)Lorg/codehaus/aspectwerkz/joinpoint/impl/CatchClauseSignatureImpl;";
    public static final String SIGNATURE_FACTORY_CLASS = "org/codehaus/aspectwerkz/joinpoint/management/SignatureFactory";
    public static final String ASPECTS_CLASS_NAME = "org/codehaus/aspectwerkz/aspect/management/Aspects";
    public static final String ASPECT_OF_METHOD_NAME = "aspectOf";
    public static final String ASPECT_OF_PER_JVM_METHOD_SIGNATURE = "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Object;";
    public static final String GETCLASSLOADER_METHOD_NAME = "getClassLoader";
    public static final String ASPECT_OF_PER_CLASS_METHOD_SIGNATURE = "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;";
    public static final String ASPECT_OF_PER_INSTANCE_METHOD_SIGNATURE = "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;";
    public static final String GET_CFLOW_STACK_METHOD_NAME = "getCflowStack";
    public static final String GET_CFLOW_STACK_METHOD_SIGNATURE = "(Ljava/lang/Class;)Lorg/codehaus/aspectwerkz/CflowStack;";
    public static final String GET_SIGNATURE_METHOD_NAME = "getSignature";
    public static final String GET_SIGNATURE_METHOD_SIGNATURE = "()Lorg/codehaus/aspectwerkz/joinpoint/Signature;";
    public static final String GET_RTTI_METHOD_NAME = "getRtti";
    public static final String GET_RTTI_METHOD_SIGNATURE = "()Lorg/codehaus/aspectwerkz/joinpoint/Rtti;";
    public static final String PROCEED_METHOD_NAME = "proceed";
    public static final String PROCEED_METHOD_SIGNATURE = "()Ljava/lang/Object;";
    public static final String COPY_METHOD_NAME = "copy";
    public static final String COPY_METHOD_SIGNATURE = "()Lorg/codehaus/aspectwerkz/joinpoint/StaticJoinPoint;";
    public static final String ADD_META_DATA_METHOD_NAME = "addMetaData";
    public static final String ADD_META_DATA_METHOD_SIGNATURE = "(Ljava/lang/Object;Ljava/lang/Object;)V";
    public static final String MAP_CLASS_SIGNATURE = "Ljava/util/Map;";
    public static final String MAP_CLASS_NAME = "java/util/Map";
    public static final String PUT_METHOD_NAME = "put";
    public static final String PUT_METHOD_SIGNATURE = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    public static final String GET_META_DATA_METHOD_NAME = "getMetaData";
    public static final String GET_TARGET_METHOD_NAME = "getTarget";
    public static final String GET_THIS_METHOD_NAME = "getThis";
    public static final String GET_CALLER_METHOD_NAME = "getCaller";
    public static final String GET_CALLEE_METHOD_NAME = "getCallee";
    public static final String GET_METHOD_NAME = "get";
    public static final String GET_METHOD_SIGNATURE = "(Ljava/lang/Object;)Ljava/lang/Object;";
    public static final String GET_META_DATA_METHOD_SIGNATURE = "(Ljava/lang/Object;)Ljava/lang/Object;";
    public static final String NEW_METHOD_SIGNATURE_METHOD_NAME = "newMethodSignature";
    public static final String NEW_CONSTRUCTOR_SIGNATURE_METHOD_NAME = "newConstructorSignature";
    public static final String NEW_FIELD_SIGNATURE_METHOD_NAME = "newFieldSignature";
    public static final String NEW_CATCH_CLAUSE_SIGNATURE_METHOD_NAME = "newCatchClauseSignature";
    public static final String HASH_MAP_CLASS_NAME = "java/util/HashMap";
    public static final String NO_PARAM_RETURN_VOID_SIGNATURE = "()V";
    public static final String CLASS_NOT_FOUND_EXCEPTION_CLASS_NAME = "java/lang/ClassNotFoundException";
    public static final String GET_CALLER_CLASS_METHOD_NAME = "getCallerClass";
    public static final String GET_CALLER_CLASS_METHOD_SIGNATURE = "()Ljava/lang/Class;";
    public static final String GET_TARGET_CLASS_METHOD_NAME = "getTargetClass";
    public static final String GET_TARGET_CLASS_METHOD_SIGNATURE = "()Ljava/lang/Class;";
    public static final String GET_TYPE_METHOD_NAME = "getType";
    public static final String GET_TYPE_METHOD_SIGNATURE = "()Lorg/codehaus/aspectwerkz/joinpoint/management/JoinPointType;";
    public static final String RESET_METHOD_NAME = "reset";
    public static final String RUNTIME_EXCEPTION_CLASS_NAME = "java/lang/RuntimeException";
    public static final String RUNTIME_EXCEPTION_INIT_METHOD_SIGNATURE = "(Ljava/lang/String;)V";
    public static final String IS_IN_CFLOW_METOD_NAME = "isInCflow";
    public static final String IS_IN_CFLOW_METOD_SIGNATURE = "()Z";
    public static final String IS_IN_CFLOWBELOW_METOD_NAME = "isInCflowBelow";
    public static final String IS_IN_CFLOWBELOW_METOD_SIGNATURE = "()Z";
    public static final String STATIC_JOIN_POINT_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/StaticJoinPoint";
    public static final String STATIC_JOIN_POINT_JAVA_CLASS_NAME = "org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint";
    public static final String JOIN_POINT_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/JoinPoint";
    public static final String JOIN_POINT_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;";
    public static final String JOIN_POINT_JAVA_CLASS_NAME = "org.codehaus.aspectwerkz.joinpoint.JoinPoint";
    public static final String NO_PARAMS_SIGNATURE = "()";

    public static final String METHOD_RTTI_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/MethodRttiImpl;";
    public static final String METHOD_RTTI_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/MethodRttiImpl";
    public static final String METHOD_RTTI_IMPL_INIT_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/impl/MethodSignatureImpl;Ljava/lang/Object;Ljava/lang/Object;)V";
    public static final String CONSTRUCTOR_RTTI_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/ConstructorRttiImpl;";
    public static final String CONSTRUCTOR_RTTI_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/ConstructorRttiImpl";
    public static final String CONSTRUCTOR_RTTI_IMPL_INIT_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/impl/ConstructorSignatureImpl;Ljava/lang/Object;Ljava/lang/Object;)V";
    public static final String FIELD_RTTI_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/FieldRttiImpl;";
    public static final String FIELD_RTTI_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/FieldRttiImpl";
    public static final String FIELD_RTTI_IMPL_INIT_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/impl/FieldSignatureImpl;Ljava/lang/Object;Ljava/lang/Object;)V";
    public static final String HANDLER_RTTI_IMPL_CLASS_SIGNATURE = "Lorg/codehaus/aspectwerkz/joinpoint/impl/CatchClauseRttiImpl;";
    public static final String HANDLER_RTTI_IMPL_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/impl/CatchClauseRttiImpl";
    public static final String HANDLER_RTTI_IMPL_INIT_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/impl/CatchClauseSignatureImpl;Ljava/lang/Object;Ljava/lang/Object;)V";
    public static final String SET_PARAMETER_VALUES_METHOD_NAME = "setParameterValues";
    public static final String SET_PARAMETER_VALUES_METHOD_SIGNATURE = "([Ljava/lang/Object;)V";
    public static final String SET_PARAMETER_VALUE_METHOD_NAME = "setParameterValue";
    public static final String SET_PARAMETER_VALUE_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";
    public static final String SET_FIELD_VALUE_METHOD_NAME = "setFieldValue";
    public static final String SET_FIELD_VALUE_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";
    public static final String SET_RETURN_VALUE_METHOD_NAME = "setReturnValue";
    public static final String SET_RETURN_VALUE_METHOD_SIGNATURE = "(Ljava/lang/Object;)V";

    public static final String HAS_INSTANCE_LEVEL_ASPECT_INTERFACE_NAME = "org/codehaus/aspectwerkz/aspect/management/HasInstanceLevelAspect";
    public static final String INSTANCE_LEVEL_ASPECT_MAP_FIELD_NAME = SYNTHETIC_MEMBER_PREFIX + "instanceLevelAspects";
    public static final String INSTANCE_LEVEL_ASPECT_MAP_FIELD_SIGNATURE = "Ljava/util/Map;";
    public static final String GET_INSTANCE_LEVEL_ASPECT_METHOD_NAME = WRAPPER_METHOD_PREFIX + "getAspect";
    public static final String GET_INSTANCE_LEVEL_ASPECT_METHOD_SIGNATURE = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;";

    public static final String ADVISABLE_CLASS_JAVA_NAME = "org.codehaus.aspectwerkz.intercept.Advisable";
    public static final String ADVISABLE_CLASS_NAME = "org/codehaus/aspectwerkz/intercept/Advisable";
    public static final String INTERCEPT_INVOKE_METHOD_NAME = "invoke";
    public static final String AROUND_ADVICE_INVOKE_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;";
    public static final String BEFORE_ADVICE_INVOKE_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)V";
    public static final String AFTER_ADVICE_INVOKE_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)V";
    public static final String AFTER_RETURNING_ADVICE_INVOKE_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;Ljava/lang/Object;)V";
    public static final String AFTER_THROWING_ADVICE_INVOKE_METHOD_SIGNATURE = "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;Ljava/lang/Throwable;)V";
    public static final String AROUND_ADVICE_CLASS_NAME = "org/codehaus/aspectwerkz/intercept/AroundAdvice";
    public static final String BEFORE_ADVICE_CLASS_NAME = "org/codehaus/aspectwerkz/intercept/BeforeAdvice";
    public static final String AFTER_ADVICE_CLASS_NAME = "org/codehaus/aspectwerkz/intercept/AfterAdvice";
    public static final String AFTER_RETURNING_ADVICE_CLASS_NAME = "org/codehaus/aspectwerkz/intercept/AfterReturningAdvice";
    public static final String AFTER_THROWING_ADVICE_CLASS_NAME = "org/codehaus/aspectwerkz/intercept/AfterThrowingAdvice";
    public static final String AROUND_ADVICE_ARRAY_CLASS_SIGNATURE = "[Lorg/codehaus/aspectwerkz/intercept/AroundAdvice;";
    public static final String BEFORE_ADVICE_ARRAY_CLASS_SIGNATURE = "[Lorg/codehaus/aspectwerkz/intercept/BeforeAdvice;";
    public static final String AFTER_ADVICE_ARRAY_CLASS_SIGNATURE = "[Lorg/codehaus/aspectwerkz/intercept/AfterAdvice;";
    public static final String AFTER_RETURNING_ADVICE_ARRAY_CLASS_SIGNATURE = "[Lorg/codehaus/aspectwerkz/intercept/AfterReturningAdvice;";
    public static final String AFTER_THROWING_ADVICE_ARRAY_CLASS_SIGNATURE = "[Lorg/codehaus/aspectwerkz/intercept/AfterThrowingAdvice;";
    public static final String GET_AROUND_ADVICE_METHOD_NAME = "aw$getAroundAdvice";
    public static final String GET_AROUND_ADVICE_METHOD_SIGNATURE = "(I)[Lorg/codehaus/aspectwerkz/intercept/AroundAdvice;";
    public static final String GET_BEFORE_ADVICE_METHOD_NAME = "aw$getBeforeAdvice";
    public static final String GET_BEFORE_ADVICE_METHOD_SIGNATURE = "(I)[Lorg/codehaus/aspectwerkz/intercept/BeforeAdvice;";
    public static final String GET_AFTER_ADVICE_METHOD_NAME = "aw$getAfterAdvice";
    public static final String GET_AFTER_ADVICE_METHOD_SIGNATURE = "(I)[Lorg/codehaus/aspectwerkz/intercept/AfterAdvice;";
    public static final String GET_AFTER_RETURNING_ADVICE_METHOD_NAME = "aw$getAfterReturningAdvice";
    public static final String GET_AFTER_RETURNING_ADVICE_METHOD_SIGNATURE = "(I)[Lorg/codehaus/aspectwerkz/intercept/AfterReturningAdvice;";
    public static final String GET_AFTER_THROWING_ADVICE_METHOD_NAME = "aw$getAfterThrowingAdvice";
    public static final String GET_AFTER_THROWING_ADVICE_METHOD_SIGNATURE = "(I)[Lorg/codehaus/aspectwerkz/intercept/AfterThrowingAdvice;";

    public static final int MODIFIER_INVOKEINTERFACE = 0x10000000;
    public static final int INDEX_NOTAVAILABLE = -1;

}
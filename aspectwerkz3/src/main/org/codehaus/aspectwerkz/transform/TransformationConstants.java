/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface with common constants used in the transformation process.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas BonŽr </a>
 */
public interface TransformationConstants {
    public static final List EMTPTY_ARRAY_LIST = new ArrayList();

    // prefixes
    public static final String ASPECTWERKZ_PREFIX = "___AW_";
    // FIMXE for 2.0 have only "access$" as prefix
    public static final String WRAPPER_METHOD_PREFIX = ASPECTWERKZ_PREFIX + "access$";//CAUTION: keep AW prefix or fix the AspectRegistry etc
    public static final String DELIMITER = "$_AW_$";
    public static final String STATIC_CLASS_FIELD = ASPECTWERKZ_PREFIX + "clazz";
    public static final String JOIN_POINT_PREFIX = ASPECTWERKZ_PREFIX + "jp";
    public static final String ORIGINAL_METHOD_PREFIX = WRAPPER_METHOD_PREFIX + "original" + DELIMITER;
    public static final String INVOKE_WRAPPER_METHOD_PREFIX = "INVOKE" + DELIMITER;
    public static final String PUTFIELD_WRAPPER_METHOD_PREFIX = "PUTFIELD" + DELIMITER;
    public static final String GETFIELD_WRAPPER_METHOD_PREFIX = "GETFIELD" + DELIMITER;
    public static final String JOIN_POINT_BASE_CLASS_SUFFIX = ASPECTWERKZ_PREFIX + "JoinPointBase";
    public static final String JOIN_POINT_CLASS_SUFFIX = ASPECTWERKZ_PREFIX + "JoinPoint";
    public static final String ASPECTWERKZ_PACKAGE_NAME = "org/codehaus/aspectwerkz";

    // internal fields
    public static final String CROSS_CUTTING_INFO_CLASS_FIELD = ASPECTWERKZ_PREFIX + "crossCuttingInfo";
    public static final String JOIN_POINT_MANAGER_FIELD = ASPECTWERKZ_PREFIX + "joinPointManager";
    public static final String ASPECT_MANAGER_FIELD = ASPECTWERKZ_PREFIX + "aspectManager";
    public static final String TARGET_CLASS_FIELD_NAME = "___AW_Clazz";
    public static final String EMPTY_WRAPPER_ATTRIBUTE = ASPECTWERKZ_PREFIX + "empty";

    // internal methods
    public static final String JOIN_POINTS_INIT_METHOD = ORIGINAL_METHOD_PREFIX + "initJoinPoints";
    public static final String LOAD_JOIN_POINT_METHOD_SIGNATURE = "(ILjava/lang/Class;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIILjava/lang/String;)V";
    public static final String FOR_NAME_METHOD_SIGNATURE = "(Ljava/lang/String;)Ljava/lang/Class;";
    public static final String INIT_JOIN_POINTS_METHOD_NAME = WRAPPER_METHOD_PREFIX + DELIMITER + "initJoinPoints";
    public static final String JOIN_POINT_INDEX_ATTRIBUTE = ASPECTWERKZ_PREFIX + "JoinPointIndex";
    public static final String INLINED_JOIN_POINT_MANAGER_CLASS_NAME = "org/codehaus/aspectwerkz/joinpoint/management/JoinPointManager";

    // method and class names
    public static final String INIT_METHOD_NAME = "<init>";
    public static final String CLINIT_METHOD_NAME = "<clinit>";
    public static final String CLASS_LOADER_REFLECT_CLASS_NAME = "java.lang.ClassLoader";
    public static final String DEFINE_CLASS_METHOD_NAME = "defineClass";
    public static final String INVOKE_METHOD_NAME = "invoke";
    public static final String FOR_NAME_METHOD_NAME = "forName";
    public static final String LOAD_JOIN_POINT_METHOD_NAME = "loadJoinPoint";
    public static final String GET_JOIN_POINT_MANAGER = "getJoinPointManager";
    public static final String GET_ASPECT_MANAGER_METHOD = "getAspectManager";
    public static final String GET_SYSTEM_METHOD = "getSystem";
    public static final String GET_MIXIN_METHOD = "getMixin";
    public static final String INVOKE_MIXIN_METHOD = "invokeMixin";
    public static final String SERIAL_VERSION_UID_FIELD = "serialVersionUID";
    public static final String PROCEED_WITH_EXECUTION_JOIN_POINT_METHOD = "proceedWithExecutionJoinPoint";
    public static final String PROCEED_WITH_CALL_JOIN_POINT_METHOD = "proceedWithCallJoinPoint";
    public static final String PROCEED_WITH_SET_JOIN_POINT_METHOD = "proceedWithSetJoinPoint";
    public static final String PROCEED_WITH_GET_JOIN_POINT_METHOD = "proceedWithGetJoinPoint";
    public static final String PROCEED_WITH_HANDLER_JOIN_POINT_METHOD = "proceedWithHandlerJoinPoint";
    public static final String HANDLER_JOIN_POINT_EXECUTION_METHOD = "proceed";
    public static final String GET_JOIN_POINTS_EXECUTION_METHOD = "getJoinPoints";
    public static final String UUID_EXECUTION_METHOD = "generate";
    public static final String CLASS_LOOKUP_METHOD = "class$";
    public static final String ASPECT_MANAGER_CLASS = "org.codehaus.aspectwerkz.aspect.management.AspectManager";
    public static final String JOIN_POINT_MANAGER_CLASS = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointManager";
    public static final String JOIN_POINT_TYPE_METHOD_EXECUTION = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.METHOD_EXECUTION";
    public static final String JOIN_POINT_TYPE_METHOD_CALL = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.METHOD_CALL";
    public static final String JOIN_POINT_TYPE_CONSTRUCTOR_EXECUTION = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.CONSTRUCTOR_EXECUTION";
    public static final String JOIN_POINT_TYPE_CONSTRUCTOR_CALL = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.CONSTRUCTOR_CALL";
    public static final String JOIN_POINT_TYPE_FIELD_SET = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.FIELD_SET";
    public static final String JOIN_POINT_TYPE_FIELD_GET = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.FIELD_GET";
    public static final String JOIN_POINT_TYPE_HANDLER = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.HANDLER";
    public static final String JOIN_POINT_TYPE_STATIC_INITALIZATION = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.STATIC_INITIALIZATION";
    public static final String SYSTEM_CLASS = "org.codehaus.aspectwerkz.RuntimeSystem";
    public static final String SYSTEM_LOADER_CLASS = "org.codehaus.aspectwerkz.SystemLoader";
    public static final String MIXIN_CLASS = "org.codehaus.aspectwerkz.Mixin";
    public static final String THREAD_LOCAL_CLASS = "org.codehaus.aspectwerkz.util.SerializableThreadLocal";
    public static final String WEAK_REFERENCE_CLASS = "java.lang.ref.WeakReference";
    public static final String CROSS_CUTTING_INFO_CLASS = "org.codehaus.aspectwerkz.CrossCuttingInfo";

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
    public static final String L = "L";
    public static final String I = "I";
    public static final String SEMICOLON = ";";

    public static final byte EMPTY_WRAPPER_ATTRIBUTE_VALUE_EMPTY = Byte.MIN_VALUE;
    public static final byte EMPTY_WRAPPER_ATTRIBUTE_VALUE_NOTEMPTY = Byte.MAX_VALUE;
}
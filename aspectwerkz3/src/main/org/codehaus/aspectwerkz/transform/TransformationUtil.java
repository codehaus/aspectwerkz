/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

/**
 * Contains constants and utility method used by the transformers.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class TransformationUtil {
    public static final String ASPECTWERKZ_PREFIX = "___AW_";

    public static final String DELIMITER = "$_AW_$";

    public static final String CALL_SIDE_DELIMITER = "#";

    public static final String UUID_FIELD = ASPECTWERKZ_PREFIX + "uuid";

    public static final String META_DATA_FIELD = ASPECTWERKZ_PREFIX + "metaData";

    public static final String STATIC_CLASS_FIELD = ASPECTWERKZ_PREFIX + "clazz";

    public static final String JOIN_POINT_PREFIX = ASPECTWERKZ_PREFIX + "jp";

    public static final String ORIGINAL_METHOD_PREFIX = ASPECTWERKZ_PREFIX + DELIMITER;

    public static final String CROSS_CUTTING_INFO_CLASS_FIELD = ASPECTWERKZ_PREFIX + "crossCuttingInfo";

    public static final String WRAPPER_METHOD_PREFIX = "access$";

    public static final String METHOD_WRAPPER_METHOD_PREFIX = ASPECTWERKZ_PREFIX + "methodWrapper" + DELIMITER;

    public static final String PUTFIELD_WRAPPER_METHOD_PREFIX = ASPECTWERKZ_PREFIX + "fieldSetWrapper" + DELIMITER;

    public static final String GETFIELD_WRAPPER_METHOD_PREFIX = ASPECTWERKZ_PREFIX + "fieldGetWrapper" + DELIMITER;

    public static final String JOIN_POINTS_INIT_METHOD = ORIGINAL_METHOD_PREFIX + "initJoinPoints";

    public static final String JOIN_POINT_MANAGER_FIELD = ASPECTWERKZ_PREFIX + "joinPointManager";

    public static final String ASPECT_MANAGER_FIELD = ASPECTWERKZ_PREFIX + "aspectManager";

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

    public static final String SUPER_CALL_WRAPPER_PREFIX = ASPECTWERKZ_PREFIX
                                                           + DELIMITER
                                                           + "super_call_wrapper"
                                                           + DELIMITER;

    public static final String MEMBER_METHOD_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX
                                                                 + DELIMITER
                                                                 + "member_method"
                                                                 + DELIMITER;

    public static final String STATIC_METHOD_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX
                                                                 + DELIMITER
                                                                 + "static_method"
                                                                 + DELIMITER;

    public static final String MEMBER_FIELD_GET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX
                                                                    + DELIMITER
                                                                    + "member_field"
                                                                    + DELIMITER
                                                                    + "get"
                                                                    + DELIMITER;

    public static final String MEMBER_FIELD_SET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX
                                                                    + DELIMITER
                                                                    + "member_field"
                                                                    + DELIMITER
                                                                    + "set"
                                                                    + DELIMITER;

    public static final String STATIC_FIELD_GET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX
                                                                    + DELIMITER
                                                                    + "static_field"
                                                                    + DELIMITER
                                                                    + "get"
                                                                    + DELIMITER;

    public static final String STATIC_FIELD_SET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX
                                                                    + DELIMITER
                                                                    + "static_field"
                                                                    + DELIMITER
                                                                    + "set"
                                                                    + DELIMITER;

    public static final String CALLER_SIDE_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX
                                                               + DELIMITER
                                                               + "caller_side_method"
                                                               + DELIMITER;

    public static final String CONSTRUCTOR_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX
                                                               + DELIMITER
                                                               + "constructor"
                                                               + DELIMITER;

    public static final String FIELD_JOIN_POINT_PRE_EXECUTION_METHOD = "pre";

    public static final String FIELD_JOIN_POINT_POST_EXECUTION_METHOD = "post";

    public static final String CALLER_SIDE_JOIN_POINT_PRE_EXECUTION_METHOD = "pre";

    public static final String CALLER_SIDE_JOIN_POINT_POST_EXECUTION_METHOD = "post";

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

    public static final String MEMBER_METHOD_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.MemberMethodJoinPoint";

    public static final String STATIC_METHOD_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.StaticMethodJoinPoint";

    public static final String MEMBER_FIELD_GET_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.MemberFieldGetJoinPoint";

    public static final String MEMBER_FIELD_SET_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.MemberFieldSetJoinPoint";

    public static final String STATIC_FIELD_GET_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.StaticFieldGetJoinPoint";

    public static final String STATIC_FIELD_SET_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.StaticFieldSetJoinPoint";

    public static final String CALLER_SIDE_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint";

    public static final String CONSTRUCTOR_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.ConstructorJoinPoint";

    public static final String IDENTIFIABLE_INTERFACE = "org.codehaus.aspectwerkz.Identifiable";

    public static final String META_DATA_INTERFACE = "org.codehaus.aspectwerkz.MetaDataEnhanceable";

    public static final String UUID_CLASS = "org.codehaus.aspectwerkz.util.UuidGenerator";

    public static final String CROSS_CUTTING_INFO_CLASS = "org.codehaus.aspectwerkz.CrossCuttingInfo";

    public static final String EMPTY_WRAPPER_ATTRIBUTE = ASPECTWERKZ_PREFIX + "empty";

    public static final byte EMPTY_WRAPPER_ATTRIBUTE_VALUE_EMPTY = Byte.MIN_VALUE;

    public static final byte EMPTY_WRAPPER_ATTRIBUTE_VALUE_NOTEMPTY = Byte.MAX_VALUE;

    public static final String JOIN_POINT_INDEX_ATTRIBUTE = ASPECTWERKZ_PREFIX + "JoinPointIndex";

    public static final String SYSTEM_ATTRIBUTE_CLASS_USE = ASPECTWERKZ_PREFIX + "ClassUseAttribute";

    /**
     * Returns the prefixed method name.
     *
     * @param methodName     the method name
     * @param methodSequence the method sequence
     * @param className      the class name
     * @return the name of the join point
     */
    public static String getPrefixedOriginalMethodName(final String methodName,
                                                       final int methodSequence,
                                                       final String className) {
        final StringBuffer buf = new StringBuffer();
        buf.append(ORIGINAL_METHOD_PREFIX);
        buf.append(methodName);
        buf.append(DELIMITER);
        buf.append(methodSequence);
        buf.append(DELIMITER);
        buf.append(className.replace('.', '_').replace('/', '_'));
        return buf.toString();
    }

    /**
     * Returns the prefixed method name.
     *
     * @param methodName     the method name
     * @param methodSequence the method sequence
     * @param className      the class name
     * @return the name of the join point
     */
    public static String getWrapperMethodName(final String methodName,
                                                    final int methodSequence,
                                                    final String className,
                                                    final String prefix) {
        final StringBuffer buf = new StringBuffer();
        //FIXME: double check me
        // we use the javaC convention for hidden synthetic method
        // is the methodSequence enough ?
        // [ Alex: looks like it will change between each RW since tied to ctx match ]
        buf.append(WRAPPER_METHOD_PREFIX);
        buf.append(methodSequence);
        buf.append("$");
        buf.append(prefix);
        buf.append(methodName);
        buf.append(DELIMITER);
        buf.append(methodSequence);
        buf.append(DELIMITER);
        buf.append(className.replace('.', '_').replace('/', '_'));
        return buf.toString();
    }
}
/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;

import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;

/**
 * Contains constants and utility method used by the transformers.
 *
 * @TODO: remove the constants and methods that are not used when TF impl. is complete
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class TransformationUtil {

    public static final String ASPECTWERKZ_PREFIX = "___AW_";
    public static final String DELIMITER = "$_AW_$";
    public static final String CALL_SIDE_DELIMITER = "#";
    public static final String UUID_FIELD = ASPECTWERKZ_PREFIX + "uuid";
    public static final String META_DATA_FIELD = ASPECTWERKZ_PREFIX + "meta_data";
    public static final String STATIC_CLASS_FIELD = ASPECTWERKZ_PREFIX + "clazz";
    public static final String JOIN_POINT_PREFIX = ASPECTWERKZ_PREFIX + "jp";
    public static final String ORIGINAL_METHOD_PREFIX = ASPECTWERKZ_PREFIX + DELIMITER;

    public static final String WRAPPER_METHOD_PREFIX = ASPECTWERKZ_PREFIX + "wrapper";
    public static final String JOIN_POINT_MANAGER_FIELD = ASPECTWERKZ_PREFIX + "joinPointManager";
    public static final String GET_JOIN_POINT_MANAGER = "getJoinPointManager";
    public static final String PROCEED_WITH_EXECUTION_JOIN_POINT_METHOD = "proceedWithExecutionJoinPoint";
    public static final String PROCEED_WITH_CALL_JOIN_POINT_METHOD = "proceedWithCallJoinPoint";
    public static final String PROCEED_WITH_SET_GET_JOIN_POINT_METHOD = "proceedWithSetGetJoinPoint";
    public static final String PROCEED_WITH_CATCH_CLAUSE_JOIN_POINT_METHOD = "proceedWithCatchClauseJoinPoint";

    public static final String SUPER_CALL_WRAPPER_PREFIX = ASPECTWERKZ_PREFIX + DELIMITER + "super_call_wrapper" + DELIMITER;
    public static final String MEMBER_METHOD_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "member_method" + DELIMITER;
    public static final String STATIC_METHOD_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "static_method" + DELIMITER;
    public static final String MEMBER_FIELD_GET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "member_field" + DELIMITER + "get" + DELIMITER;
    public static final String MEMBER_FIELD_SET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "member_field" + DELIMITER + "set" + DELIMITER;
    public static final String STATIC_FIELD_GET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "static_field" + DELIMITER + "get" + DELIMITER;
    public static final String STATIC_FIELD_SET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "static_field" + DELIMITER + "set" + DELIMITER;
    public static final String CALLER_SIDE_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "caller_side_method" + DELIMITER;
    public static final String CONSTRUCTOR_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "constructor" + DELIMITER;
//
    public static final String FIELD_JOIN_POINT_PRE_EXECUTION_METHOD = "pre";
    public static final String FIELD_JOIN_POINT_POST_EXECUTION_METHOD = "post";
    public static final String CALLER_SIDE_JOIN_POINT_PRE_EXECUTION_METHOD = "pre";
    public static final String CALLER_SIDE_JOIN_POINT_POST_EXECUTION_METHOD = "post";
    public static final String HANDLER_JOIN_POINT_EXECUTION_METHOD = "proceed";
    public static final String GET_JOIN_POINTS_EXECUTION_METHOD = "getJoinPoints";
    public static final String UUID_EXECUTION_METHOD = "generate";
    public static final String GET_UUID_METHOD = ASPECTWERKZ_PREFIX + "getUuid";
    public static final String GET_META_DATA_METHOD = ASPECTWERKZ_PREFIX + "getMetaData";
    public static final String SET_META_DATA_METHOD = ASPECTWERKZ_PREFIX + "addMetaData";
    public static final String CLASS_LOOKUP_METHOD = "class$";

    public static final String JOIN_POINT_MANAGER_CLASS = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointManager";
    public static final String JOIN_POINT_TYPE_METHOD_EXECUTION = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.METHOD_EXECUTION";
    public static final String JOIN_POINT_TYPE_METHOD_CALL = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.METHOD_CALL";
    public static final String JOIN_POINT_TYPE_CONSTRUCTOR_EXECUTION = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.CONSTRUCTOR_EXECUTION";
    public static final String JOIN_POINT_TYPE_CONSTRUCTOR_CALL = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.CONSTRUCTOR_CALL";
    public static final String JOIN_POINT_TYPE_FIELD_SET = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.FIELD_SET";
    public static final String JOIN_POINT_TYPE_FIELD_GET = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.FIELD_GET";
    public static final String JOIN_POINT_TYPE_CATCH_CLAUSE = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.CATCH_CLAUSE";
    public static final String JOIN_POINT_TYPE_STATIC_INITALIZATION = "org.codehaus.aspectwerkz.joinpoint.management.JoinPointType.STATIC_INITALIZATION";

    public static final String SYSTEM_CLASS = "org.codehaus.aspectwerkz.System";
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

    public static final String SERIAL_VERSION_UID_FIELD = "serialVersionUID";
    public static final String RETRIEVE_SYSTEM_METHOD = "getSystem";
    public static final String RETRIEVE_MIXIN_METHOD = "getMixin";
    public static final String INVOKE_MIXIN_METHOD = "___AW_invokeMixin";

    /**
     * Creates a sorted method list of all the public methods in the class and super classes.
     *
     * @param klass the class with the methods
     * @return the sorted method list
     */
    public static List createSortedMethodList(final Class klass) {
        if (klass == null) throw new IllegalArgumentException("class to sort method on can not be null");

        // get all public methods including the inherited methods
        java.lang.reflect.Method[] methods = klass.getMethods();

        List methodList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            java.lang.reflect.Method method = methods[i];
            if (!method.getName().equals("equals") &&
                    !method.getName().equals("hashCode") &&
                    !method.getName().equals("getClass") &&
                    !method.getName().equals("toString") &&
                    !method.getName().equals("wait") &&
                    !method.getName().equals("notify") &&
                    !method.getName().equals("notifyAll") &&
                    !method.getName().startsWith(CLASS_LOOKUP_METHOD) &&
                    !method.getName().startsWith(GET_UUID_METHOD) &&
                    !method.getName().startsWith(GET_META_DATA_METHOD) &&
                    !method.getName().startsWith(SET_META_DATA_METHOD) &&
                    !method.getName().startsWith(ORIGINAL_METHOD_PREFIX) &&
                    !method.getName().startsWith(ASPECTWERKZ_PREFIX)) {
                methodList.add(method);
            }
        }
        Collections.sort(
                methodList,
                MethodComparator.getInstance(MethodComparator.NORMAL_METHOD)
        );
        return methodList;
    }

    /**
     * Creates a sorted method list of all the public methods in the class and super classes.
     *
     * @param klass the class with the methods
     * @return the sorted method list
     */
    public static List createSortedMethodList(final CtClass klass) {
        if (klass == null) throw new IllegalArgumentException("class to sort method on can not be null");

        // get all public methods including the inherited methods
        CtMethod[] methods = klass.getMethods();

        List methodList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            if (!method.getName().equals("equals") &&
                    !method.getName().equals("hashCode") &&
                    !method.getName().equals("getClass") &&
                    !method.getName().equals("toString") &&
                    !method.getName().equals("wait") &&
                    !method.getName().equals("notify") &&
                    !method.getName().equals("notifyAll") &&
                    !method.getName().startsWith(CLASS_LOOKUP_METHOD) &&
                    !method.getName().startsWith(GET_UUID_METHOD) &&
                    !method.getName().startsWith(GET_META_DATA_METHOD) &&
                    !method.getName().startsWith(SET_META_DATA_METHOD) &&
                    !method.getName().startsWith(ORIGINAL_METHOD_PREFIX) &&
                    !method.getName().startsWith(ASPECTWERKZ_PREFIX)) {
                methodList.add(method);
            }
        }
        Collections.sort(
                methodList,
                MethodComparator.getInstance(MethodComparator.NORMAL_METHOD)
        );
        return methodList;
    }

    /**
     * Checks if a class has a certain class as super class, somewhere up in the class hierarchy.
     *
     * @param classMetaData the meta-data for the class to match
     * @param className the name of the super class
     * @return true if we have a match else false
     */
    public static boolean hasSuperClass(final ClassMetaData classMetaData, final String className) {
        if (classMetaData == null || className == null) {
            return false;
        }
        else if (classMetaData.getName().equals(null)) {
            return true;
        }
        else if (className.equals(classMetaData.getName())) {
            return true;
        }
        else {
            return TransformationUtil.hasSuperClass(classMetaData.getSuperClass(), className);
        }
    }

    /**
     * Converts modifiers represented in a string array to an int.
     *
     * @param modifiers the modifiers as strings
     * @return the modifiers as an int
     */
    public static int getModifiersAsInt(final String[] modifiers) {
        int accessFlags = 0;
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i].equals("abstract")) {
                accessFlags |= Modifier.ABSTRACT;
            }
            else if (modifiers[i].equals("final")) {
                accessFlags |= Modifier.FINAL;
            }
            else if (modifiers[i].equals("interface")) {
                accessFlags |= Modifier.INTERFACE;
            }
            else if (modifiers[i].equals("native")) {
                accessFlags |= Modifier.NATIVE;
            }
            else if (modifiers[i].equals("private")) {
                accessFlags |= Modifier.PRIVATE;
            }
            else if (modifiers[i].equals("protected")) {
                accessFlags |= Modifier.PROTECTED;
            }
            else if (modifiers[i].equals("public")) {
                accessFlags |= Modifier.PUBLIC;
            }
            else if (modifiers[i].equals("static")) {
                accessFlags |= Modifier.STATIC;
            }
            else if (modifiers[i].equals("strict")) {
                accessFlags |= Modifier.STRICT;
            }
            else if (modifiers[i].equals("synchronized")) {
                accessFlags |= Modifier.SYNCHRONIZED;
            }
            else if (modifiers[i].equals("transient")) {
                accessFlags |= Modifier.TRANSIENT;
            }
            else if (modifiers[i].equals("volatile")) {
                accessFlags |= Modifier.VOLATILE;
            }
        }
        return accessFlags;
    }

    /**
     * Calculate the hash for a method.
     *
     * @param method the method
     * @return the hash
     */
    public static int calculateHash(final java.lang.reflect.Method method) {
        int hash = 17;
        hash = 37 * hash + method.getName().hashCode();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class type = method.getParameterTypes()[i];
            String name = type.getName();
            int index = name.indexOf("[L");
            if (index >= 0) {
                name = name.substring(index + 2, name.length() - 1) + "[]";
            }
            hash = 37 * hash + name.hashCode();
        }
        return hash;
    }

    /**
     * Calculate the hash for a javassist method.
     *
     * @param method the method
     * @return the hash
     */
    public static int calculateHash(final CtMethod method) throws NotFoundException {
        int hash = 17;
        hash = 37 * hash + method.getName().hashCode();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            CtClass type = method.getParameterTypes()[i];
            hash = 37 * hash + type.getName().hashCode();
        }
        return hash;
    }

    /**
     * Calculate the hash for a constructor.
     *
     * @param constructor the constructor
     * @return the hash
     */
    public static int calculateHash(final Constructor constructor) {
        int hash = 17;
        hash = 37 * hash + constructor.getName().hashCode();
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            Class type = constructor.getParameterTypes()[i];
            String name = type.getName();
            int index = name.indexOf("[L");
            if (index >= 0) {
                name = name.substring(index + 2, name.length() - 1) + "[]";
            }
            hash = 37 * hash + name.hashCode();
        }
        return hash;
    }

    /**
     * Calculate the hash for a javassist constructor.
     *
     * @param constructor the constructor
     * @return the hash
     */
    public static int calculateHash(final CtConstructor constructor) throws NotFoundException {
        int hash = 17;
        hash = 37 * hash + constructor.getName().hashCode();
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            CtClass type = constructor.getParameterTypes()[i];
            hash = 37 * hash + type.getName().hashCode();
        }
        return hash;
    }

    /**
     * Calculate the hash for a field.
     *
     * @param field the field
     * @return the hash
     */
    public static int calculateHash(final Field field) {
        int hash = 17;
        hash = 37 * hash + field.getName().hashCode();
        Class type = field.getType();
        String name = type.getName();
        int index = name.indexOf("[L");
        if (index >= 0) {
            name = name.substring(index + 2, name.length() - 1) + "[]";
        }
        hash = 37 * hash + name.hashCode();
        return hash;
    }

    /**
     * Calculate the hash for a javassist field.
     *
     * @param field the field
     * @return the hash
     */
    public static int calculateHash(final CtField field) throws NotFoundException {
        int hash = 17;
        hash = 37 * hash + field.getName().hashCode();
        hash = 37 * hash + field.getType().getName().hashCode();
        return hash;
    }

    /**
     * Returns the prefixed method name.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @param className the class name
     * @return the name of the join point
     */
    public static String getPrefixedMethodName(final CtMethod method,
                                               final int methodSequence,
                                               final String className) {
        final StringBuffer methodName = new StringBuffer();
        methodName.append(ORIGINAL_METHOD_PREFIX);
        methodName.append(method.getName());
        methodName.append(DELIMITER);
        methodName.append(methodSequence);
        methodName.append(DELIMITER);
        methodName.append(className.replace('.', '_'));
        return methodName.toString();
    }

    /**
     * Returns the prefixed method name.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @param className the class name
     * @return the name of the join point
     */
    public static String getPrefixedMethodName(final Method method,
                                               final int methodSequence,
                                               final String className) {
        final StringBuffer methodName = new StringBuffer();
        methodName.append(ORIGINAL_METHOD_PREFIX);
        methodName.append(method.getName());
        methodName.append(DELIMITER);
        methodName.append(methodSequence);
        methodName.append(DELIMITER);
        methodName.append(className.replace('.', '_'));
        return methodName.toString();
    }
}

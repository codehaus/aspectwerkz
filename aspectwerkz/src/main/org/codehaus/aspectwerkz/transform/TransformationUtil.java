/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.lang.reflect.Array;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.security.MessageDigest;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ReferenceType;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.MethodComparator;

/**
 * Contains constants and utility method used by the transformers.
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
    public static final String ORIGINAL_METHOD_PREFIX = ASPECTWERKZ_PREFIX + "original_method" + DELIMITER;
    public static final String SUPER_CALL_WRAPPER_PREFIX = ASPECTWERKZ_PREFIX + DELIMITER + "super_call_wrapper" + DELIMITER;
    public static final String MEMBER_METHOD_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "member_method" + DELIMITER;
    public static final String STATIC_METHOD_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "static_method" + DELIMITER;
    public static final String MEMBER_FIELD_GET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "member_field" + DELIMITER + "get" + DELIMITER;
    public static final String MEMBER_FIELD_SET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "member_field" + DELIMITER + "set" + DELIMITER;
    public static final String STATIC_FIELD_GET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "static_field" + DELIMITER + "get" + DELIMITER;
    public static final String STATIC_FIELD_SET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "static_field" + DELIMITER + "set" + DELIMITER;
    public static final String CALLER_SIDE_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "caller_side_method" + DELIMITER;
    public static final String CONSTRUCTOR_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "constructor" + DELIMITER;

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

    public static final ObjectType MEMBER_METHOD_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.MemberMethodJoinPoint");
    public static final ObjectType STATIC_METHOD_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.StaticMethodJoinPoint");
    public static final ObjectType MEMBER_FIELD_GET_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.MemberFieldGetJoinPoint");
    public static final ObjectType MEMBER_FIELD_SET_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.MemberFieldSetJoinPoint");
    public static final ObjectType STATIC_FIELD_GET_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.StaticFieldGetJoinPoint");
    public static final ObjectType STATIC_FIELD_SET_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.StaticFieldSetJoinPoint");
    public static final ObjectType CALLER_SIDE_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint");
    public static final ObjectType CONSTRUCTOR_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.ConstructorJoinPoint");
    public static final ObjectType WEAK_REFERENCE_TYPE = new ObjectType("java.lang.ref.WeakReference");;

    /**
     * Converts String access types to BCEL access types.
     *
     * @param modifiers the modifiers as strings
     * @return the BCEL modifiers (int)
     */
    public static int getModifiersAsInt(final String[] modifiers) {
        int accessFlags = 0;
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i].equals("abstract")) {
                accessFlags |= Constants.ACC_ABSTRACT;
            }
            else if (modifiers[i].equals("final")) {
                accessFlags |= Constants.ACC_FINAL;
            }
            else if (modifiers[i].equals("interface")) {
                accessFlags |= Constants.ACC_INTERFACE;
            }
            else if (modifiers[i].equals("native")) {
                accessFlags |= Constants.ACC_NATIVE;
            }
            else if (modifiers[i].equals("private")) {
                accessFlags |= Constants.ACC_PRIVATE;
            }
            else if (modifiers[i].equals("protected")) {
                accessFlags |= Constants.ACC_PROTECTED;
            }
            else if (modifiers[i].equals("public")) {
                accessFlags |= Constants.ACC_PUBLIC;
            }
            else if (modifiers[i].equals("static")) {
                accessFlags |= Constants.ACC_STATIC;
            }
            else if (modifiers[i].equals("strict")) {
                accessFlags |= Constants.ACC_STRICT;
            }
            else if (modifiers[i].equals("super")) {
                accessFlags |= Constants.ACC_SUPER;
            }
            else if (modifiers[i].equals("synchronized")) {
                accessFlags |= Constants.ACC_SYNCHRONIZED;
            }
            else if (modifiers[i].equals("transient")) {
                accessFlags |= Constants.ACC_TRANSIENT;
            }
            else if (modifiers[i].equals("volatile")) {
                accessFlags |= Constants.ACC_VOLATILE;
            }
        }
        return accessFlags;
    }

    /**
     * Converts a type represented as a string to a BCEL type.
     *
     * @param type the type as a string
     * @return the BCEL type
     */
    public static Type getBcelType(final String type) {
        Type bcelReturnType;
        if (type == null) {
            return Type.NULL;
        }
        else if (type.equals("void")) {
            bcelReturnType = Type.VOID;
        }
        else if (type.equals("int")) {
            bcelReturnType = Type.INT;
        }
        else if (type.equals("long")) {
            bcelReturnType = Type.LONG;
        }
        else if (type.equals("short")) {
            bcelReturnType = Type.SHORT;
        }
        else if (type.equals("double")) {
            bcelReturnType = Type.DOUBLE;
        }
        else if (type.equals("float")) {
            bcelReturnType = Type.FLOAT;
        }
        else if (type.equals("char")) {
            bcelReturnType = Type.CHAR;
        }
        else if (type.equals("boolean")) {
            bcelReturnType = Type.BOOLEAN;
        }
        else if (type.equals("byte")) {
            bcelReturnType = Type.BYTE;
        }
        else if (type.endsWith("[]")) {
            int index = type.indexOf('[');
            int dimensions = type.length() - index >> 1; // we need number of dimensions
            bcelReturnType = new ArrayType(type.substring(0, index), dimensions);
        }
        else {
            bcelReturnType = new ObjectType(type);
        }
        return bcelReturnType;
    }

    /**
     * Converts a BCEL type to a class.
     *
     * @param bcelType the BCEL type
     * @return the class
     */
    public static Class convertBcelTypeToClass(final Type bcelType) {
        final String type = bcelType.toString();
        Class klass;
        if (type.equals("void")) {
            klass = null;
        }
        else if (type.equals("long")) {
            klass = long.class;
        }
        else if (type.equals("int")) {
            klass = int.class;
        }
        else if (type.equals("short")) {
            klass = short.class;
        }
        else if (type.equals("double")) {
            klass = double.class;
        }
        else if (type.equals("float")) {
            klass = float.class;
        }
        else if (type.equals("boolean")) {
            klass = boolean.class;
        }
        else if (type.equals("byte")) {
            klass = byte.class;
        }
        else if (type.equals("char")) {
            klass = char.class;
        }
        else if (type.endsWith("[]")) {
            int index = type.indexOf('[');
            int dimension = type.length() - index >> 1; // we need number of dimensions

            try {
                klass = Array.newInstance(
                        ContextClassLoader.loadClass(type.substring(0, index)),
                        new int[dimension]).getClass();
            }
            catch (ClassNotFoundException e) {
                throw new WrappedRuntimeException(e);
            }
        }
        else {
            try {
                klass = ContextClassLoader.loadClass(type);
            }
            catch (ClassNotFoundException e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return klass;
    }

    /**
     * Calculates the serialVerUid for a class.
     *
     * @param cg the class gen
     * @return the uid
     */
    public static long calculateSerialVersionUid(final Context context, final ClassGen cg) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bout);

            JavaClass klass = context.getJavaClass(cg);
            Method[] methods = klass.getMethods();

            // class name.
            String className = klass.getClassName();
            out.writeUTF(className);

            // class modifiers.
            int classMods = klass.getModifiers() &
                    (Constants.ACC_PUBLIC | Constants.ACC_FINAL |
                    Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT);
            // fixes bug in javac
            if ((classMods & Constants.ACC_INTERFACE) != 0) {
                classMods = (methods.length > 0) ?
                        (classMods | Constants.ACC_ABSTRACT) :
                        (classMods & ~Constants.ACC_ABSTRACT);
            }
            out.writeInt(classMods);

            // interfaces.
            JavaClass[] interfaces = klass.getInterfaces();
            if (interfaces != null) {
                String[] interfaceNames = new String[interfaces.length];
                for (int i = 0; i < interfaces.length; i++) {
                    interfaceNames[i] = interfaces[i].getClassName();
                }
                Arrays.sort(interfaceNames);
                for (int i = 0; i < interfaces.length; i++) {
                    out.writeUTF(interfaceNames[i]);
                }
            }
            // fields.
            Field[] fields = klass.getFields();
            if (fields != null) {
                Arrays.sort(fields, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Field field1 = (Field)o1;
                        Field field2 = (Field)o2;
                        return field1.getName().compareTo(field2.getName());
                    }
                });
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    int mods = field.getModifiers();
                    if (((mods & Constants.ACC_PRIVATE) == 0) ||
                            ((mods & (Constants.ACC_STATIC |
                            Constants.ACC_TRANSIENT)) == 0)) {
                        out.writeUTF(field.getName());
                        out.writeInt(mods);
                        out.writeUTF(field.getSignature());
                    }
                }
            }

            // put the regular methods, the constructors and the
            // static intializer in different lists
            List constructorList = new ArrayList();
            List regularMethodList = new ArrayList();
            if (methods != null) {
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method.getName().equals("<clinit>")) {
                        // handle static intiailization.
                        out.writeUTF("<clinit>");
                        out.writeInt(Constants.ACC_STATIC);
                        out.writeUTF("()V");
                    }
                    else if (method.getName().equals("<init>")) {
                        constructorList.add(method);
                    }
                    else {
                        regularMethodList.add(method);
                    }
                }
            }

            // handle constructors.
            Object[] constructors = constructorList.toArray();
            Arrays.sort(constructors, new Comparator() {
                public int compare(Object o1, Object o2) {
                    try {
                        Method c1 = (Method)o1;
                        Method c2 = (Method)o2;
                        return c1.getSignature().compareTo(c2.getSignature());
                    }
                    catch (Exception e) {
                        throw new WrappedRuntimeException(e);
                    }
                }
            });
            for (int i = 0; i < constructors.length; i++) {
                Method constructor = (Method)constructors[i];
                int mods = constructor.getModifiers();
                if ((mods & Constants.ACC_PRIVATE) == 0) {
                    out.writeUTF("<init>");
                    out.writeInt(mods);
                    out.writeUTF(constructor.getSignature().replace('/', '.'));
                }
            }

            // handle regular methods.
            Object[] regularMethods = regularMethodList.toArray();
            Arrays.sort(regularMethods, new Comparator() {
                public int compare(Object o1, Object o2) {
                    try {
                        Method m1 = (Method)o1;
                        Method m2 = (Method)o2;
                        int value = m1.getName().compareTo(m2.getName());
                        if (value == 0) {
                            value = m1.getSignature().compareTo(m2.getSignature());
                        }
                        return value;
                    }
                    catch (Exception e) {
                        throw new WrappedRuntimeException(e);
                    }
                }
            });
            for (int i = 0; i < regularMethods.length; i++) {
                Method method = (Method)regularMethods[i];
                int mods = method.getModifiers();
                if ((mods & Constants.ACC_PRIVATE) == 0) {
                    out.writeUTF(method.getName());
                    out.writeInt(mods);
                    out.writeUTF(method.getSignature().replace('/', '.'));
                }
            }

            // calculate hash.
            out.flush();
            MessageDigest digest = MessageDigest.getInstance("SHA");
            byte[] digested = digest.digest(bout.toByteArray());
            long hash = 0;
            for (int i = Math.min(digested.length, 8) - 1; i >= 0; i--) {
                hash = (hash << 8) | (digested[i] & 0xFF);
            }
            return hash;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Checks if a class is serialiable.
     *
     * The method needs to be context aware since the BCEL call getAllInterfaces() will
     * load all transitively implemented interfaces.
     *
     * @param context the transformation context
     * @param cg the class gen
     * @return boolean
     */
    public static boolean isSerializable(final Context context, final ClassGen cg) {
        boolean isSerializable = false;
        try {
            JavaClass[] allInterfaces = context.getJavaClass(cg).getAllInterfaces();
            for (int i = 0; i < allInterfaces.length; i++) {
                JavaClass anInterface = allInterfaces[i];
                if (anInterface.getClassName().equals("java.io.Serializable")) {
                    isSerializable = true;
                }
            }
        }
        catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
        return isSerializable;
    }

    /**
     * Checks if the class has a serialVersionUID field.
     *
     * @param cg the class gen
     * @return boolean
     */
    public static boolean hasSerialVersionUid(final ClassGen cg) {
        boolean hasSerialVerUid = false;
        Field[] fields = cg.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getName().equals(SERIAL_VERSION_UID_FIELD)) {
                hasSerialVerUid = true;
                break;
            }
        }
        return hasSerialVerUid;
    }

    /**
     * Add the given interface to the given class representation.
     *
     * @param cg ClassGen representation
     * @param interf FQN of the interface
     */
    public static void addInterfaceToClass(final ClassGen cg, final String interf) {

        //@todo review log
        AspectWerkzPreProcessor.log("adding interface to " + cg.getClassName() + ": " + interf);

        //@todo: check for readonly class ??
        if (!Arrays.asList(cg.getInterfaceNames()).contains(interf)) {
            cg.addInterface(interf);
        }
    }

    /**
     * Add the given method implementation to the given class representation.
     *
     * @param cg ClassGen representation
     * @param method method implementation
     */
    public static void addMethod(final ClassGen cg, final Method method) {
        //@todo review log
        AspectWerkzPreProcessor.log("adding method to " + cg.getClassName() + ": " + method.toString());

        //@todo: check for read only ??
        if (cg.containsMethod(method.getName(), method.getSignature()) == null) {
            cg.addMethod(method);
        }
    }

    /**
     * Add the given field implementation to the given class representation.
     *
     * @param cg ClassGen representation
     * @param field field implementation
     */
    public static void addField(final ClassGen cg, final Field field) {
        //@todo review log
        AspectWerkzPreProcessor.log("adding field to " + cg.getClassName() + ": " + field.toString());

        //@todo: check for read only ??
        if (!cg.containsField(field)) {
            cg.addField(field);
        }
    }

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
                    !method.getName().startsWith(ORIGINAL_METHOD_PREFIX)) {
                methodList.add(method);
            }
        }
        Collections.sort(
                methodList,
                MethodComparator.getInstance(MethodComparator.NORMAL_METHOD)
        );
        return methodList;
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.delegation;

import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;

/**
 * Helper class with utility methods for Javassist.
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class JavassistHelper {
    /**
     * Helper method for bogus CtMethod.make in Javassist for static methods
     * 
     * @param returnType
     * @param name
     * @param parameters
     * @param exceptions
     * @param body
     * @param declaring
     * @return new method
     * @throws CannotCompileException
     */
    public static CtMethod makeStatic(
        final CtClass returnType,
        final String name,
        final CtClass[] parameters,
        final CtClass[] exceptions,
        final String body,
        final CtClass declaring) throws CannotCompileException {
        try {
            CtMethod cm = new CtMethod(returnType, name, parameters, declaring);
            cm.setModifiers(cm.getModifiers() | Modifier.STATIC);
            cm.setExceptionTypes(exceptions);
            cm.setBody(body);
            return cm;
        } catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }

    /**
     * Gets the default value for primitive types
     * 
     * @param type
     * @return
     */
    public static String getDefaultPrimitiveValue(CtClass type) {
        if (type == CtClass.booleanType) {
            return "false";
        } else if (type == CtClass.intType) {
            return "0";
        } else if (type == CtClass.longType) {
            return "0L";
        } else if (type == CtClass.floatType) {
            return "0.0f";
        } else if (type == CtClass.shortType) {
            return "(short)0";
        } else if (type == CtClass.byteType) {
            return "(byte)0";
        } else if (type == CtClass.charType) {
            return "'\\u0000'";
        } else if (type == CtClass.doubleType) {
            return "(double)0";
        } else {
            return "null";
        }
    }

    /**
     * Checks if the given Class as already a method methodName Does not take into account the signature
     * 
     * @param klass
     * @param methodName
     * @return true if klass has methodName
     */
    public static boolean hasMethod(CtClass klass, String methodName) {
        try {
            klass.getDeclaredMethod(methodName);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if the given Class as already a ctor with given signature
     * 
     * @param klass
     * @param args
     * @return true if klass has ctor
     */
    public static boolean hasConstructor(CtClass klass, CtClass[] args) {
        try {
            klass.getDeclaredConstructor(args);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if the given Class as already a method methodName Does not take into account the signature
     * 
     * @param klass
     * @param fieldName
     * @return true if klass has methodName
     */
    public static boolean hasField(CtClass klass, String fieldName) {
        try {
            klass.getDeclaredField(fieldName);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if the given Class as already a method methodName Does not take into account the signature
     * 
     * @param klass
     * @param methodName
     * @return true if klass has methodName
     */
    public static boolean hasMethod(CtClass klass, String methodName, CtClass[] args) {
        try {
            klass.getDeclaredMethod(methodName, args);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Swapp bodies of the two given methods of the same declaring class
     * 
     * @param methodA
     * @param methodB
     * @TODO: add support for annotations
     */
    public static void swapBodies(CtMethod methodA, CtMethod methodB) {
        String nameA = methodA.getName();
        int modifiersA = methodA.getModifiers();
        methodA.setName(methodB.getName());
        methodA.setModifiers(methodB.getModifiers());
        methodB.setName(nameA);
        methodB.setModifiers(modifiersA);
    }

    /**
     * Converts a Javassist type signature to a reflect type signature. <p/>Since <b>sucky </b> Javassist does not use
     * the standard.
     * 
     * @param typeName
     * @return @TODO does not support multi dimensional arrays, needs to be fixed
     */
    public static String convertJavassistTypeSignatureToReflectTypeSignature(String typeName) {
        int index = typeName.indexOf("[]");
        if (index >= 0) {
            typeName = typeName.substring(0, index);
            if (typeName.equals("short")) {
                typeName = "[S";
            } else if (typeName.equals("int")) {
                typeName = "[I";
            } else if (typeName.equals("long")) {
                typeName = "[J";
            } else if (typeName.equals("float")) {
                typeName = "[F";
            } else if (typeName.equals("double")) {
                typeName = "[D";
            } else if (typeName.equals("char")) {
                typeName = "[C";
            } else if (typeName.equals("byte")) {
                typeName = "[B";
            } else if (typeName.equals("boolean")) {
                typeName = "[Z";
            } else {
                typeName = "[L" + typeName + ';';
            }
        }
        return typeName;
    }

    /**
     * Checks if a method is marked as an empty wrapper (runtime weaving)
     * 
     * @param method
     * @return true if empty wrapper
     */
    public static boolean isAnnotatedEmpty(CtMethod method) {
        byte[] emptyBytes = method.getAttribute(TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE);
        return ((emptyBytes != null) && (emptyBytes[0] == TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE_VALUE_EMPTY));
    }

    /**
     * Checks if a method is marked as a non empty wrapper (runtime unweaving)
     * 
     * @param method
     * @return true if non empty wrapper
     */
    public static boolean isAnnotatedNotEmpty(CtMethod method) {
        byte[] emptyBytes = method.getAttribute(TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE);
        return ((emptyBytes == null) || (emptyBytes[0] == TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE_VALUE_NOTEMPTY));
    }

    /**
     * Sets a method as beeing an empty wrapper
     * 
     * @param method
     */
    public static void setAnnotatedEmpty(CtMethod method) {
        method.setAttribute(TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE, new byte[] {
            TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE_VALUE_EMPTY
        });
    }

    /**
     * Sets a method as beeing a non empty wrapper
     * 
     * @param method
     */
    public static void setAnnotatedNotEmpty(CtMethod method) {
        method.setAttribute(TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE, new byte[] {
            TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE_VALUE_NOTEMPTY
        });
    }

    /**
     * Creates an empty wrapper method to allow HotSwap without schema change <p/>TODO refactor PrepareTransformer
     * CAUTION: does not check the wrapped method already exists while PrepareTransformer version does
     * 
     * @param ctClass the ClassGen
     * @param originalMethod the current method
     * @param methodSequence the method hash
     * @return the wrapper method
     */
    public static CtMethod createEmptyWrapperMethod(
        final CtClass ctClass,
        final CtMethod originalMethod,
        final int methodSequence) throws NotFoundException, CannotCompileException {
        String wrapperMethodName = TransformationUtil.getPrefixedMethodName(
            originalMethod.getName(),
            methodSequence,
            ctClass.getName().replace('/', '.'));

        // determine the method access flags (should always be set to protected)
        int accessFlags = originalMethod.getModifiers();
        if ((accessFlags & Modifier.PROTECTED) == 0) {
            // set the protected flag
            accessFlags |= Modifier.PROTECTED;
        }
        if ((accessFlags & Modifier.PRIVATE) != 0) {
            // clear the private flag
            accessFlags &= ~Modifier.PRIVATE;
        }
        if ((accessFlags & Modifier.PUBLIC) != 0) {
            // clear the public flag
            accessFlags &= ~Modifier.PUBLIC;
        }

        // add an empty body
        StringBuffer body = new StringBuffer();
        if (originalMethod.getReturnType() == CtClass.voidType) {
            // special handling for void return type leads to cleaner bytecode
            // generation with Javassist
            body.append("{}");
        } else if (!originalMethod.getReturnType().isPrimitive()) {
            body.append("{ return null;}");
        } else {
            body.append("{ return ");
            body.append(JavassistHelper.getDefaultPrimitiveValue(originalMethod.getReturnType()));
            body.append("; }");
        }
        CtMethod method = null;
        if (Modifier.isStatic(originalMethod.getModifiers())) {
            method = JavassistHelper.makeStatic(originalMethod.getReturnType(), wrapperMethodName, originalMethod
                    .getParameterTypes(), originalMethod.getExceptionTypes(), body.toString(), ctClass);
        } else {
            method = CtNewMethod.make(originalMethod.getReturnType(), wrapperMethodName, originalMethod
                    .getParameterTypes(), originalMethod.getExceptionTypes(), body.toString(), ctClass);
            method.setModifiers(accessFlags);
        }
        JavassistHelper.copyCustomAttributes(method, originalMethod);

        // add a method level attribute so that we remember it is an empty
        // method
        JavassistHelper.setAnnotatedEmpty(method);
        return method;
    }

    /**
     * Copy pasted from Javassist since it is a private method
     * 
     * @param clazz
     * @return @throws CannotCompileException
     */
    public static long calculateSerialVerUid(CtClass clazz) throws CannotCompileException {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bout);
            ClassFile classFile = clazz.getClassFile();

            // class name.
            String javaName = javaName(clazz);
            out.writeUTF(javaName);

            // class modifiers.
            out.writeInt(clazz.getModifiers()
                & (Modifier.PUBLIC | Modifier.FINAL | Modifier.INTERFACE | Modifier.ABSTRACT));

            // interfaces.
            String[] interfaces = classFile.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i] = javaName(interfaces[i]);
            }
            Arrays.sort(interfaces);
            for (int i = 0; i < interfaces.length; i++) {
                out.writeUTF(interfaces[i]);
            }

            // fields.
            CtField[] fields = clazz.getDeclaredFields();
            Arrays.sort(fields, new Comparator() {
                public int compare(Object o1, Object o2) {
                    CtField field1 = (CtField) o1;
                    CtField field2 = (CtField) o2;
                    return field1.getName().compareTo(field2.getName());
                }
            });
            for (int i = 0; i < fields.length; i++) {
                CtField field = (CtField) fields[i];
                int mods = field.getModifiers();
                if (((mods & Modifier.PRIVATE) == 0) || ((mods & (Modifier.STATIC | Modifier.TRANSIENT)) == 0)) {
                    out.writeUTF(field.getName());
                    out.writeInt(mods);
                    out.writeUTF(field.getFieldInfo2().getDescriptor());
                }
            }

            // static initializer.
            if (classFile.getStaticInitializer() != null) {
                out.writeUTF("<clinit>");
                out.writeInt(Modifier.STATIC);
                out.writeUTF("()V");
            }

            // constructors.
            CtConstructor[] constructors = clazz.getDeclaredConstructors();
            Arrays.sort(constructors, new Comparator() {
                public int compare(Object o1, Object o2) {
                    CtConstructor c1 = (CtConstructor) o1;
                    CtConstructor c2 = (CtConstructor) o2;
                    return c1.getMethodInfo2().getDescriptor().compareTo(c2.getMethodInfo2().getDescriptor());
                }
            });
            for (int i = 0; i < constructors.length; i++) {
                CtConstructor constructor = constructors[i];
                int mods = constructor.getModifiers();
                if ((mods & Modifier.PRIVATE) == 0) {
                    out.writeUTF("<init>");
                    out.writeInt(mods);
                    out.writeUTF(constructor.getMethodInfo2().getDescriptor().replace('/', '.'));
                }
            }

            // methods.
            CtMethod[] methods = clazz.getDeclaredMethods();
            Arrays.sort(methods, new Comparator() {
                public int compare(Object o1, Object o2) {
                    CtMethod m1 = (CtMethod) o1;
                    CtMethod m2 = (CtMethod) o2;
                    int value = m1.getName().compareTo(m2.getName());
                    if (value == 0) {
                        value = m1.getMethodInfo2().getDescriptor().compareTo(m2.getMethodInfo2().getDescriptor());
                    }
                    return value;
                }
            });
            for (int i = 0; i < methods.length; i++) {
                CtMethod method = methods[i];
                int mods = method.getModifiers();
                if ((mods & Modifier.PRIVATE) == 0) {
                    out.writeUTF(method.getName());
                    out.writeInt(mods);
                    out.writeUTF(method.getMethodInfo2().getDescriptor().replace('/', '.'));
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
        } catch (IOException e) {
            throw new CannotCompileException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new CannotCompileException(e);
        }
    }

    private static String javaName(CtClass clazz) {
        return Descriptor.toJavaName(Descriptor.toJvmName(clazz));
    }

    private static String javaName(String name) {
        return Descriptor.toJavaName(Descriptor.toJvmName(name));
    }

    public static void setSerialVersionUID(CtClass clazz, long serialVerUid) throws CannotCompileException {
        // add field with default value.
        CtField field = new CtField(CtClass.longType, "serialVersionUID", clazz);
        field.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        clazz.addField(field, serialVerUid + "L");
    }

    /**
     * Does the class implement Serializable?
     */
    private static boolean isSerializable(CtClass clazz) throws NotFoundException {
        ClassPool pool = clazz.getClassPool();
        return clazz.subtypeOf(pool.get("java.io.Serializable"));
    }

    public static boolean isSerialVerUidNeeded(CtClass clazz) throws NotFoundException {
        // check for pre-existing field.
        try {
            clazz.getDeclaredField("serialVersionUID");
            return false;
        } catch (NotFoundException e) {
        }

        // check if the class is serializable.
        if (!isSerializable(clazz)) {
            return false;
        }
        return true;
    }

    /**
     * Adds a new <code>AspectManager</code> field to the advised class.
     * 
     * @param ctClass
     * @param definition
     */
    public static void addAspectManagerField(
        final CtClass ctClass,
        final SystemDefinition definition,
        final Context context) throws NotFoundException, CannotCompileException {
        if (!hasField(ctClass, TransformationUtil.ASPECT_MANAGER_FIELD)) {
            CtField field = new CtField(
                ctClass.getClassPool().get(TransformationUtil.ASPECT_MANAGER_CLASS),
                TransformationUtil.ASPECT_MANAGER_FIELD,
                ctClass);
            field.setModifiers(Modifier.STATIC | Modifier.PRIVATE | Modifier.FINAL);
            StringBuffer body = new StringBuffer();
            body.append(TransformationUtil.SYSTEM_LOADER_CLASS);
            body.append("#getSystem(");
            body.append(TransformationUtil.STATIC_CLASS_FIELD);
            body.append('.');
            body.append("getClassLoader())");
            body.append('.');
            body.append(TransformationUtil.GET_ASPECT_MANAGER_METHOD);
            body.append("(\"");
            body.append(definition.getUuid());
            body.append("\");");

            //TODO ALEX AVAOPC
            /*
             * what about having several field to access the AspectManager whose system is introducing methods ? should
             * we have a simpler TF model and hardcode the AspectManager index ?? [problem for undeploy of a system]
             */
            ctClass.addField(field, body.toString());
            context.markAsAdvised();
        }
    }

    /**
     * Creates a new static class field.
     * 
     * @param ctClass the class
     */
    public static void addStaticClassField(final CtClass ctClass, final Context context) throws NotFoundException,
            CannotCompileException {
        if (!hasField(ctClass, TransformationUtil.STATIC_CLASS_FIELD)) {
            CtField field = new CtField(
                ctClass.getClassPool().get("java.lang.Class"),
                TransformationUtil.STATIC_CLASS_FIELD,
                ctClass);
            field.setModifiers(Modifier.STATIC | Modifier.PRIVATE | Modifier.FINAL);
            ctClass.addField(field, "java.lang.Class#forName(\"" + ctClass.getName().replace('/', '.') + "\")");
            context.markAsAdvised();
        }
    }

    /**
     * Adds a new <code>JoinPointManager</code> field to the advised class.
     * 
     * @param ctClass
     * @param definition
     */
    public static void addJoinPointManagerField(
        final CtClass ctClass,
        final SystemDefinition definition,
        final Context context) throws NotFoundException, CannotCompileException {
        if (!hasField(ctClass, TransformationUtil.JOIN_POINT_MANAGER_FIELD)) {
            CtField field = new CtField(
                ctClass.getClassPool().get(TransformationUtil.JOIN_POINT_MANAGER_CLASS),
                TransformationUtil.JOIN_POINT_MANAGER_FIELD,
                ctClass);
            field.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
            StringBuffer body = new StringBuffer();
            body.append(TransformationUtil.JOIN_POINT_MANAGER_CLASS);
            body.append('#');
            body.append(TransformationUtil.GET_JOIN_POINT_MANAGER);
            body.append('(');
            body.append(TransformationUtil.STATIC_CLASS_FIELD);
            body.append(", \"");
            body.append(definition.getUuid());
            body.append("\")");
            ctClass.addField(field, body.toString());
            context.markAsAdvised();
        }
    }

    /**
     * Copies the custom attributes from copyTo class to another.
     * 
     * @param copyTo
     * @param copyFrom
     */
    public static void copyCustomAttributes(final CtMethod copyTo, final CtMethod copyFrom) {
        List attributes = copyFrom.getMethodInfo().getAttributes();
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            AttributeInfo attributeInfo = (AttributeInfo) iterator.next();
            if (attributeInfo.getName().startsWith(AttributeEnhancer.CUSTOM_ATTRIBUTE)) {
                copyTo.setAttribute(attributeInfo.getName(), attributeInfo.get());
                //FIXME bug here
                //System.out.println("JavassistHelper.copyCustomAttributes " + copyFrom.getName() + " to " +
                // copyTo.getName() + " " + attributeInfo.getName());
            }
        }
    }

    /**
     * Calculate the hash for a javassist field.
     * 
     * @param field the field
     * @return the hash
     */
    public static int calculateHash(final CtField field) throws NotFoundException {
        int hash = 17;
        hash = (37 * hash) + field.getName().hashCode();
        String name = convertJavassistTypeSignatureToReflectTypeSignature(field.getType().getName().replace('/', '.'));
        hash = (37 * hash) + name.hashCode();
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
        hash = (37 * hash) + constructor.getName().hashCode();
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            CtClass type = constructor.getParameterTypes()[i];
            String name = convertJavassistTypeSignatureToReflectTypeSignature(type.getName().replace('/', '.'));
            hash = (37 * hash) + name.hashCode();
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
        hash = (37 * hash) + method.getName().hashCode();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            CtClass type = method.getParameterTypes()[i];
            String name = convertJavassistTypeSignatureToReflectTypeSignature(type.getName().replace('/', '.'));
            hash = (37 * hash) + name.hashCode();
        }
        return hash;
    }

    /**
     * Calculate the hash for a class.
     * 
     * @param ctClass the class
     * @return the hash
     */
    public static int calculateHash(final CtClass ctClass) throws NotFoundException {
        //        int hash = 17;
        //        CtMethod[] methods = ctClass.getDeclaredMethods();
        //        for (int i = 0; i < methods.length; i++) {
        //            hash = (37 * hash) + calculateHash(methods[i]);
        //        }
        //        CtConstructor[] constructors = ctClass.getDeclaredConstructors();
        //        for (int i = 0; i < constructors.length; i++) {
        //            hash = (37 * hash) + calculateHash(constructors[i]);
        //        }
        //        CtField[] fields = ctClass.getDeclaredFields();
        //        for (int i = 0; i < fields.length; i++) {
        //            hash = (37 * hash) + calculateHash(fields[i]);
        //        }
        //        return hash;
        return ctClass.getName().hashCode();
    }

    /**
     * Creates a sorted method list of all the public methods in the class and super classes.
     * 
     * @param klass the class with the methods
     * @return the sorted method list
     */
    public static List createSortedMethodList(final CtClass klass) {
        if (klass == null) {
            throw new IllegalArgumentException("class to sort method on can not be null");
        }

        // get all public methods including the inherited methods
        CtMethod[] methods = klass.getDeclaredMethods();
        List methodList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            if (!method.getName().equals("equals")
                && !method.getName().equals("hashCode")
                && !method.getName().equals("getClass")
                && !method.getName().equals("toString")
                && !method.getName().equals("wait")
                && !method.getName().equals("notify")
                && !method.getName().equals("notifyAll")
                && !method.getName().startsWith(TransformationUtil.CLASS_LOOKUP_METHOD)
                && !method.getName().startsWith(TransformationUtil.GET_UUID_METHOD)
                && !method.getName().startsWith(TransformationUtil.GET_META_DATA_METHOD)
                && !method.getName().startsWith(TransformationUtil.SET_META_DATA_METHOD)
                && !method.getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX)
                && !method.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                methodList.add(method);
            }
        }
        Collections.sort(methodList, MethodComparator.getInstance(MethodComparator.NORMAL_METHOD));
        return methodList;
    }

    /**
     * Returrns the join point index for the class.
     * 
     * @param klass
     * @return the index
     */
    public static int getJoinPointIndex(final CtClass klass) {
        byte[] attribute = klass.getAttribute(TransformationUtil.JOIN_POINT_INDEX_ATTRIBUTE);
        if (attribute == null) {
            klass.setAttribute(TransformationUtil.JOIN_POINT_INDEX_ATTRIBUTE, new byte[] {
                new Integer(0).byteValue()
            });
            return 0;
        }
        return new Integer(attribute[0]).intValue();
    }

    /**
     * Sets the join point index for the class.
     * 
     * @param klass
     * @param index
     */
    public static void setJoinPointIndex(final CtClass klass, final int index) {
        klass.setAttribute(TransformationUtil.JOIN_POINT_INDEX_ATTRIBUTE, new byte[] {
            new Integer(index).byteValue()
        });
    }
}
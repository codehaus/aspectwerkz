/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining;

import org.codehaus.aspectwerkz.util.ContextClassLoader;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;
import org.objectweb.asm.Label;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Helper class with utility methods for the ASM library.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AsmHelper implements TransformationConstants {

    public final static ClassInfo INTEGER = JavaClassInfo.getClassInfo(Integer.TYPE);
    public final static ClassInfo VOID = JavaClassInfo.getClassInfo(Void.TYPE);
    public final static ClassInfo BOOLEAN = JavaClassInfo.getClassInfo(Boolean.TYPE);
    public final static ClassInfo BYTE = JavaClassInfo.getClassInfo(Byte.TYPE);
    public final static ClassInfo CHARACTER = JavaClassInfo.getClassInfo(Character.TYPE);
    public final static ClassInfo SHORT = JavaClassInfo.getClassInfo(Short.TYPE);
    public final static ClassInfo DOUBLE = JavaClassInfo.getClassInfo(Double.TYPE);
    public final static ClassInfo FLOAT = JavaClassInfo.getClassInfo(Float.TYPE);
    public final static ClassInfo LONG = JavaClassInfo.getClassInfo(Long.TYPE);

    /**
     * A boolean to check if we have a J2SE 5 support
     */
    public final static boolean IS_JAVA_5;
    public static int JAVA_VERSION = V1_3;

    static {
        Class annotation = null;
        try {
            annotation = Class.forName("java.lang.annotation.Annotation");
            ClassReader cr = new ClassReader("java.lang.annotation.Annotation");
            JAVA_VERSION = V1_5;
        } catch (Throwable e) {
            annotation = null;
        }
        if (annotation == null) {
            IS_JAVA_5 = false;
        } else {
            IS_JAVA_5 = true;
        }
    }

    /**
     * Factory method for ASM ClassWriter and J2SE 5 support
     * See http://www.objectweb.org/wws/arc/asm/2004-08/msg00005.html
     *
     * @param computeMax
     * @return
     */
    public static ClassWriter newClassWriter(boolean computeMax) {
        return new ClassWriter(computeMax, true);
    }

    /**
     * Gets the argument types for a constructor. <p/>Parts of code in this method is taken from the ASM codebase.
     *
     * @param constructor
     * @return the ASM argument types for the constructor
     */
    public static Type[] getArgumentTypes(final Constructor constructor) {
        Class[] classes = constructor.getParameterTypes();
        Type[] types = new Type[classes.length];
        for (int i = classes.length - 1; i >= 0; --i) {
            types[i] = Type.getType(classes[i]);
        }
        return types;
    }

    /**
     * Dumps an ASM class to disk.
     *
     * @param dumpDir
     * @param className
     * @param cw
     * @throws java.io.IOException
     */
    public static void dumpClass(final String dumpDir, final String className, final ClassWriter cw)
            throws IOException {
        File dir = new File(dumpDir + File.separator + className.substring(0, className.lastIndexOf('/')));
        dir.mkdirs();
        String fileName = dumpDir + File.separator + className + ".class";
        if (AspectWerkzPreProcessor.VERBOSE) {
            System.out.println("AW INFO: dumping class " + className + " to " + dumpDir);
        }
        FileOutputStream os = new FileOutputStream(fileName);
        os.write(cw.toByteArray());
        os.close();
    }

    /**
     * Adds a class to a class loader and loads it.
     *
     * @param loader the class loader (if null the context class loader will be used)
     * @param bytes  the bytes for the class
     * @param name   the name of the class
     * @return the class
     */
    public static Class loadClass(ClassLoader loader, final byte[] bytes, final String name) {
        String className = name.replace('/', '.');
        try {
            if (loader == null) {
                loader = ContextClassLoader.getLoader();
            }
            Class klass = loader.loadClass(CLASS_LOADER_REFLECT_CLASS_NAME);
            Method method = klass.getDeclaredMethod(
                    DEFINE_CLASS_METHOD_NAME, new Class[]{
                        String.class, byte[].class, int.class, int.class
                    }
            );

            // TODO: what if we don't have rights to set this method to
            // accessible on this specific CL? Load it in System CL?
            method.setAccessible(true);
            Object[] args = new Object[]{
                className, bytes, new Integer(0), new Integer(bytes.length)
            };
            Class clazz = (Class) method.invoke(loader, args);

            method.setAccessible(false);
            return clazz;

        } catch (InvocationTargetException e) {
            // JIT failovering for Thread concurrency
            // AW-222 (Tomcat and WLS were reported for AW-222)
            if (e.getTargetException() instanceof LinkageError) {
                Class failoverJoinpointClass = loadClass(loader, className);
                if (failoverJoinpointClass != null) {
                    return failoverJoinpointClass;
                }
            }
            throw new WrappedRuntimeException(e);
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Tries to load a class if unsuccessful returns null.
     *
     * @param loader the class loader
     * @param name   the name of the class
     * @return the class
     */
    public static Class loadClass(ClassLoader loader, final String name) {
        String className = name.replace('/', '.');
        try {
            if (loader == null) {
                loader = ContextClassLoader.getLoader();
            }
            // Use Class.forName since loader.loadClass fails on JBoss UCL
            return Class.forName(className, false, loader);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Calculates the method hash. The computation MUST be the same as in ReflectHelper, thus we switch back the names
     * to Java style. Note that for array type, Java.reflect is using "[Lpack.foo;" style unless primitive.
     *
     * @param name
     * @param desc
     * @return
     */
    public static int calculateMethodHash(final String name, final String desc) {
        int hash = 17;
        hash = (37 * hash) + name.replace('/', '.').hashCode();
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        for (int i = 0; i < argumentTypes.length; i++) {
            hash = (37 * hash)
                   + AsmHelper.convertTypeDescToReflectDesc(argumentTypes[i].getDescriptor()).hashCode();
        }
        return hash;
    }

    /**
     * Calculates the constructor hash.
     *
     * @param desc
     * @return
     */
    public static int calculateConstructorHash(final String desc) {
        return AsmHelper.calculateMethodHash(INIT_METHOD_NAME, desc);
    }

    /**
     * Calculates the joinpoint constructor call/execution hash.
     * It depends on the callee class name else we won't be able to distinguish joinpoint on different targets.
     *
     * @param declaringClassName
     * @param desc
     * @return
     */
    public static int calculateConstructorJoinPointHash(final String declaringClassName, final String desc) {
        int hash = 17;
        hash = (37 * hash) + AsmHelper.calculateClassHash("L" + declaringClassName + ";");
        hash = (37 * hash) + AsmHelper.calculateMethodHash(INIT_METHOD_NAME, desc);
        return hash;
    }

    /**
     * Calculates the field hash.
     *
     * @param name
     * @param desc
     * @return
     */
    public static int calculateFieldHash(final String name, final String desc) {
        int hash = 17;
        hash = (37 * hash) + name.hashCode();
        Type type = Type.getType(desc);
        hash = (37 * hash) + AsmHelper.convertTypeDescToReflectDesc(type.getDescriptor()).hashCode();
        return hash;
    }

    /**
     * Calculates the class hash.
     *
     * @param declaringType
     * @return
     */
    public static int calculateClassHash(final String declaringType) {
        return AsmHelper.convertTypeDescToReflectDesc(declaringType).hashCode();
    }

    /**
     * Converts an internal Java array type name ([Lblabla) to the a the format used by the expression matcher
     * (blabla[])
     *
     * @param typeName is type name
     * @return
     */
    public static String convertArrayTypeName(final String typeName) {
        int index = typeName.lastIndexOf('[');
        if (index != -1) {
            StringBuffer arrayType = new StringBuffer();
            if (typeName.endsWith("I")) {
                arrayType.append("int");
            } else if (typeName.endsWith("J")) {
                arrayType.append("long");
            } else if (typeName.endsWith("S")) {
                arrayType.append("short");
            } else if (typeName.endsWith("F")) {
                arrayType.append("float");
            } else if (typeName.endsWith("D")) {
                arrayType.append("double");
            } else if (typeName.endsWith("Z")) {
                arrayType.append("boolean");
            } else if (typeName.endsWith("C")) {
                arrayType.append("char");
            } else if (typeName.endsWith("B")) {
                arrayType.append("byte");
            } else {
                arrayType.append(typeName.substring(index + 2, typeName.length() - 1));
            }
            for (int i = 0; i < (index + 1); i++) {
                arrayType.append("[]");
            }
            return arrayType.toString();
        } else {
            return typeName;
        }
    }

    /**
     * Converts an ASM type descriptor" (I, [I, [Ljava/lang/String;, Ljava/lang/String;) to a Java.reflect one (int, [I,
     * [Ljava.lang.String;, java.lang.String)
     *
     * @param typeDesc
     * @return the Java.reflect string representation
     */
    public static String convertTypeDescToReflectDesc(final String typeDesc) {
        if (typeDesc == null) {
            return null;
        }
        String result = null;
        // change needed for array types only
        if (typeDesc.startsWith("[")) {
            result = typeDesc;
        } else {
            // support for single dimension type
            if (typeDesc.startsWith("L") && typeDesc.endsWith(";")) {
                result = typeDesc.substring(1, typeDesc.length() - 1);
            } else {
                // primitive type, single dimension
                if (typeDesc.equals("I")) {
                    result = "int";
                } else if (typeDesc.equals("J")) {
                    result = "long";
                } else if (typeDesc.equals("S")) {
                    result = "short";
                } else if (typeDesc.equals("F")) {
                    result = "float";
                } else if (typeDesc.equals("D")) {
                    result = "double";
                } else if (typeDesc.equals("Z")) {
                    result = "boolean";
                } else if (typeDesc.equals("C")) {
                    result = "char";
                } else if (typeDesc.equals("B")) {
                    result = "byte";
                } else {
                    throw new RuntimeException("unknown primitive type " + typeDesc);
                }
            }
        }
        return result.replace('/', '.');
    }

    /**
     * Converts a java reflect type desc to ASM type desc.
     *
     * @param desc
     * @return
     */
    public static String convertReflectDescToTypeDesc(final String desc) {
        if (desc == null) {
            return null;
        }
        String typeDesc = desc;
        int dimension = 0;
        char[] arr = desc.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == ']') {
                dimension++;
            }
        }
        typeDesc = desc.substring(0, desc.length() - dimension * 2);
        if (typeDesc.equals("int")) {
            typeDesc = "I";
        } else if (typeDesc.equals("short")) {
            typeDesc = "S";
        } else if (typeDesc.equals("long")) {
            typeDesc = "J";
        } else if (typeDesc.equals("float")) {
            typeDesc = "F";
        } else if (typeDesc.equals("double")) {
            typeDesc = "D";
        } else if (typeDesc.equals("byte")) {
            typeDesc = "B";
        } else if (typeDesc.equals("char")) {
            typeDesc = "C";
        } else if (typeDesc.equals("boolean")) {
            typeDesc = "Z";
        } else {
            typeDesc = 'L' + typeDesc + ';';
        }
        for (int i = 0; i < dimension; i++) {
            typeDesc = '[' + typeDesc;
        }
        return typeDesc.replace('.', '/');
    }

    /**
     * Adds the correct return statement.
     *
     * @param mv
     * @param type
     */
    public static void addReturnStatement(final CodeVisitor mv, final Type type) {
        switch (type.getSort()) {
            case Type.VOID:
                mv.visitInsn(RETURN);
                break;
            case Type.LONG:
                mv.visitInsn(LRETURN);
                break;
            case Type.INT:
                mv.visitInsn(IRETURN);
                break;
            case Type.SHORT:
                mv.visitInsn(IRETURN);
                break;
            case Type.DOUBLE:
                mv.visitInsn(DRETURN);
                break;
            case Type.FLOAT:
                mv.visitInsn(FRETURN);
                break;
            case Type.BYTE:
                mv.visitInsn(IRETURN);
                break;
            case Type.BOOLEAN:
                mv.visitInsn(IRETURN);
                break;
            case Type.CHAR:
                mv.visitInsn(IRETURN);
                break;
            case Type.ARRAY:
                mv.visitInsn(ARETURN);
                break;
            case Type.OBJECT:
                mv.visitInsn(ARETURN);
                break;
        }
    }

    /**
     * Loads argument types.
     *
     * @param mv
     * @param argumentTypes
     */
    public static void loadArgumentTypes(final CodeVisitor mv, final Type[] argumentTypes, final boolean staticMethod) {
        int index;
        if (staticMethod) {
            index = 0;
        } else {
            index = 1;
        }
        for (int i = 0; i < argumentTypes.length; i++) {
            index = loadType(mv, index, argumentTypes[i]);
        }
    }

    /**
     * Loads a type.
     *
     * @param cv
     * @param index
     * @param type
     * @return the incremented index
     */
    public static int loadType(final CodeVisitor cv, int index, final Type type) {
        switch (type.getSort()) {
            case Type.LONG:
                cv.visitVarInsn(LLOAD, index++);
                index++;
                break;
            case Type.INT:
                cv.visitVarInsn(ILOAD, index++);
                break;
            case Type.SHORT:
                cv.visitVarInsn(ILOAD, index++);
                break;
            case Type.DOUBLE:
                cv.visitVarInsn(DLOAD, index++);
                index++;
                break;
            case Type.FLOAT:
                cv.visitVarInsn(FLOAD, index++);
                break;
            case Type.BYTE:
                cv.visitVarInsn(ILOAD, index++);
                break;
            case Type.BOOLEAN:
                cv.visitVarInsn(ILOAD, index++);
                break;
            case Type.CHAR:
                cv.visitVarInsn(ILOAD, index++);
                break;
            case Type.ARRAY:
                cv.visitVarInsn(ALOAD, index++);
                break;
            case Type.OBJECT:
                cv.visitVarInsn(ALOAD, index++);
                break;
        }
        return index;
    }

    /**
     * Stores a type.
     *
     * @param cv
     * @param index
     * @param type
     * @return the incremented index
     */
    public static int storeType(final CodeVisitor cv, int index, final Type type) {
        switch (type.getSort()) {
            case Type.VOID:
                break;
            case Type.LONG:
                cv.visitVarInsn(LSTORE, index++);
                index++;
                break;
            case Type.INT:
                cv.visitVarInsn(ISTORE, index++);
                break;
            case Type.SHORT:
                cv.visitVarInsn(ISTORE, index++);
                break;
            case Type.DOUBLE:
                cv.visitVarInsn(DSTORE, index++);
                index++;
                break;
            case Type.FLOAT:
                cv.visitVarInsn(FSTORE, index++);
                break;
            case Type.BYTE:
                cv.visitVarInsn(ISTORE, index++);
                break;
            case Type.BOOLEAN:
                cv.visitVarInsn(ISTORE, index++);
                break;
            case Type.CHAR:
                cv.visitVarInsn(ISTORE, index++);
                break;
            case Type.ARRAY:
                cv.visitVarInsn(ASTORE, index++);
                break;
            case Type.OBJECT:
                cv.visitVarInsn(ASTORE, index++);
                break;
        }
        return index;
    }

    /**
     * Creates and adds the correct parameter index for integer types.
     *
     * @param cv
     * @param index
     */
    public static void loadIntegerConstant(final CodeVisitor cv, final int index) {
        switch (index) {
            case 0:
                cv.visitInsn(ICONST_0);
                break;
            case 1:
                cv.visitInsn(ICONST_1);
                break;
            case 2:
                cv.visitInsn(ICONST_2);
                break;
            case 3:
                cv.visitInsn(ICONST_3);
                break;
            case 4:
                cv.visitInsn(ICONST_4);
                break;
            case 5:
                cv.visitInsn(ICONST_5);
                break;
            default:
                cv.visitIntInsn(BIPUSH, index);
                break;
        }
    }

    /**
     * Prepares the wrapping or a primitive type.
     *
     * @param cv
     * @param type
     */
    public static void prepareWrappingOfPrimitiveType(final CodeVisitor cv, final Type type) {
        switch (type.getSort()) {
            case Type.SHORT:
                cv.visitTypeInsn(NEW, SHORT_CLASS_NAME);
                cv.visitInsn(DUP);
                break;
            case Type.INT:
                cv.visitTypeInsn(NEW, INTEGER_CLASS_NAME);
                cv.visitInsn(DUP);
                break;
            case Type.LONG:
                cv.visitTypeInsn(NEW, LONG_CLASS_NAME);
                cv.visitInsn(DUP);
                break;
            case Type.FLOAT:
                cv.visitTypeInsn(NEW, FLOAT_CLASS_NAME);
                cv.visitInsn(DUP);
                break;
            case Type.DOUBLE:
                cv.visitTypeInsn(NEW, DOUBLE_CLASS_NAME);
                cv.visitInsn(DUP);
                break;
            case Type.BYTE:
                cv.visitTypeInsn(NEW, BYTE_CLASS_NAME);
                cv.visitInsn(DUP);
                break;
            case Type.BOOLEAN:
                cv.visitTypeInsn(NEW, BOOLEAN_CLASS_NAME);
                cv.visitInsn(DUP);
                break;
            case Type.CHAR:
                cv.visitTypeInsn(NEW, CHARACTER_CLASS_NAME);
                cv.visitInsn(DUP);
                break;
        }
    }

    /**
     * Handles the wrapping of a primitive type.
     *
     * @param cv
     * @param type
     */
    public static void wrapPrimitiveType(final CodeVisitor cv, final Type type) {
        switch (type.getSort()) {
            case Type.VOID:
                cv.visitInsn(ACONST_NULL);
                break;
            case Type.SHORT:
                cv.visitMethodInsn(
                        INVOKESPECIAL,
                        SHORT_CLASS_NAME,
                        INIT_METHOD_NAME,
                        SHORT_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.INT:
                cv.visitMethodInsn(
                        INVOKESPECIAL,
                        INTEGER_CLASS_NAME,
                        INIT_METHOD_NAME,
                        INTEGER_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.LONG:
                cv.visitMethodInsn(INVOKESPECIAL, LONG_CLASS_NAME, INIT_METHOD_NAME, LONG_CLASS_INIT_METHOD_SIGNATURE);
                break;
            case Type.FLOAT:
                cv.visitMethodInsn(
                        INVOKESPECIAL,
                        FLOAT_CLASS_NAME,
                        INIT_METHOD_NAME,
                        FLOAT_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.DOUBLE:
                cv.visitMethodInsn(
                        INVOKESPECIAL,
                        DOUBLE_CLASS_NAME,
                        INIT_METHOD_NAME,
                        DOUBLE_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.BYTE:
                cv.visitMethodInsn(INVOKESPECIAL, BYTE_CLASS_NAME, INIT_METHOD_NAME, BYTE_CLASS_INIT_METHOD_SIGNATURE);
                break;
            case Type.BOOLEAN:
                cv.visitMethodInsn(
                        INVOKESPECIAL,
                        BOOLEAN_CLASS_NAME,
                        INIT_METHOD_NAME,
                        BOOLEAN_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
            case Type.CHAR:
                cv.visitMethodInsn(
                        INVOKESPECIAL,
                        CHARACTER_CLASS_NAME,
                        INIT_METHOD_NAME,
                        CHARACTER_CLASS_INIT_METHOD_SIGNATURE
                );
                break;
        }
    }

    /**
     * Handles the unwrapping of a type, unboxing of primitives and casting to the correct object type.
     * Takes care of null value replaced by default primitive value.
     * <pre>(obj==null)?0L:((Long)obj).longValue();</pre>
     *
     * @param cv
     * @param type
     */
    public static void unwrapType(final CodeVisitor cv, final Type type) {
        // void, object and array type handling
        switch (type.getSort()) {
            case Type.OBJECT:
                String objectTypeName = type.getClassName().replace('.', '/');
                cv.visitTypeInsn(CHECKCAST, objectTypeName);
                return;
            case Type.ARRAY:
                cv.visitTypeInsn(CHECKCAST, type.getDescriptor());
                return;
            case Type.VOID:
                return;
        }
        // primitive type handling
        Label l0If = new Label();
        Label l1End = new Label();
        // if != null
        cv.visitInsn(DUP);
        cv.visitJumpInsn(IFNONNULL, l0If);
        // else, default value
        cv.visitInsn(POP);
        addDefaultValue(cv, type);
        // end
        cv.visitJumpInsn(GOTO, l1End);
        // if body
        cv.visitLabel(l0If);
        switch (type.getSort()) {
            case Type.SHORT:
                cv.visitTypeInsn(CHECKCAST, SHORT_CLASS_NAME);
                cv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        SHORT_CLASS_NAME,
                        SHORT_VALUE_METHOD_NAME,
                        SHORT_VALUE_METHOD_SIGNATURE
                );
                break;
            case Type.INT:
                cv.visitTypeInsn(CHECKCAST, INTEGER_CLASS_NAME);
                cv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        INTEGER_CLASS_NAME,
                        INT_VALUE_METHOD_NAME,
                        INT_VALUE_METHOD_SIGNATURE
                );
                break;
            case Type.LONG:
                cv.visitTypeInsn(CHECKCAST, LONG_CLASS_NAME);
                cv.visitMethodInsn(
                        INVOKEVIRTUAL, LONG_CLASS_NAME, LONG_VALUE_METHOD_NAME, LONG_VALUE_METHOD_SIGNATURE
                );
                break;
            case Type.FLOAT:
                cv.visitTypeInsn(CHECKCAST, FLOAT_CLASS_NAME);
                cv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        FLOAT_CLASS_NAME,
                        FLOAT_VALUE_METHOD_NAME,
                        FLOAT_VALUE_METHOD_SIGNATURE
                );
                break;
            case Type.DOUBLE:
                cv.visitTypeInsn(CHECKCAST, DOUBLE_CLASS_NAME);
                cv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        DOUBLE_CLASS_NAME,
                        DOUBLE_VALUE_METHOD_NAME,
                        DOUBLE_VALUE_METHOD_SIGNATURE
                );
                break;
            case Type.BYTE:
                cv.visitTypeInsn(CHECKCAST, BYTE_CLASS_NAME);
                cv.visitMethodInsn(
                        INVOKEVIRTUAL, BYTE_CLASS_NAME, BYTE_VALUE_METHOD_NAME, BYTE_VALUE_METHOD_SIGNATURE
                );
                break;
            case Type.BOOLEAN:
                cv.visitTypeInsn(CHECKCAST, BOOLEAN_CLASS_NAME);
                cv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        BOOLEAN_CLASS_NAME,
                        BOOLEAN_VALUE_METHOD_NAME,
                        BOOLEAN_VALUE_METHOD_SIGNATURE
                );
                break;
            case Type.CHAR:
                cv.visitTypeInsn(CHECKCAST, CHARACTER_CLASS_NAME);
                cv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        CHARACTER_CLASS_NAME,
                        CHAR_VALUE_METHOD_NAME,
                        CHAR_VALUE_METHOD_SIGNATURE
                );
                break;
        }
        cv.visitLabel(l1End);
    }

    /**
     * Adds the default value for a type.
     *
     * @param cv
     * @param type
     */
    public static void addDefaultValue(final CodeVisitor cv, final Type type) {
        switch (type.getSort()) {
            case Type.OBJECT:
                cv.visitInsn(ACONST_NULL);
                break;
            case Type.ARRAY:
                cv.visitInsn(ACONST_NULL);
                break;
            case Type.INT:
                cv.visitInsn(ICONST_0);
                break;
            case Type.LONG:
                cv.visitInsn(LCONST_0);
                break;
            case Type.SHORT:
                cv.visitInsn(ICONST_0);
                break;
            case Type.FLOAT:
                cv.visitInsn(FCONST_0);
                break;
            case Type.DOUBLE:
                cv.visitInsn(DCONST_0);
                break;
            case Type.BYTE:
                cv.visitInsn(ICONST_0);
                break;
            case Type.BOOLEAN:
                cv.visitInsn(ICONST_0);
                break;
            case Type.CHAR:
                cv.visitInsn(ICONST_0);
                break;
        }
    }

    /**
     * Adds a string and inserts null if the string is null.
     *
     * @param cv
     * @param value
     */
    public static void addNullableString(final CodeVisitor cv, final String value) {
        if (value == null) {
            cv.visitInsn(ACONST_NULL);
        } else {
            cv.visitLdcInsn(value);
        }
    }

    /**
     * Compute the register depth, based on an array of types (long, double = 2 bytes address)
     *
     * @param typesOnStack
     * @return depth of the stack
     */
    public static int getRegisterDepth(final Type[] typesOnStack) {
        int depth = 0;
        for (int i = 0; i < typesOnStack.length; i++) {
            Type type = typesOnStack[i];
            depth++;
            switch (type.getSort()) {
                case Type.LONG:
                    depth++;
                    break;
                case Type.DOUBLE:
                    depth++;
                    break;
            }
        }
        return depth;
    }

    /**
     * Compute the index on the stack of a given argument based on its index in the signature
     *
     * @param typesOnStack
     * @param typeIndex
     * @return
     */
    public static int getRegisterIndexOf(final Type[] typesOnStack, final int typeIndex) {
        int depth = 0;
        for (int i = 0; i < typeIndex; i++) {
            Type type = typesOnStack[i];
            depth++;
            switch (type.getSort()) {
                case Type.LONG:
                    depth++;
                    break;
                case Type.DOUBLE:
                    depth++;
                    break;
            }
        }
        return depth;
    }

    /**
     * Compute the index on the signature of a given argument based on its index as if it was on the stack
     * where the stack would start at the first argument
     *
     * @param typesOnStack
     * @param registerIndex
     * @return
     */
    public static int getTypeIndexOf(final Type[] typesOnStack, final int registerIndex) {
        for (int i = 0; i < typesOnStack.length; i++) {
            int presumedRegisterIndex = getRegisterIndexOf(typesOnStack, i);
            if (registerIndex == presumedRegisterIndex) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the Type is a primitive.
     *
     * @param returnType
     * @return TRUE/FALSE
     */
    public static boolean isPrimitive(final Type returnType) {
        if (returnType.getSort() == Type.INT ||
            returnType.getSort() == Type.SHORT ||
            returnType.getSort() == Type.LONG ||
            returnType.getSort() == Type.FLOAT ||
            returnType.getSort() == Type.DOUBLE ||
            returnType.getSort() == Type.BYTE ||
            returnType.getSort() == Type.CHAR ||
            returnType.getSort() == Type.BOOLEAN ||
            returnType.getSort() == Type.VOID) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Increments the index (takes doubles and longs in to account).
     *
     * @param index
     * @param type
     * @return the incremented index
     */
    public static int incrementIndex(int index, final Type type) {
        if (type.getSort() == Type.LONG || type.getSort() == Type.DOUBLE) {
            index += 2;
        } else {
            index++;
        }
        return index;
    }

    /**
     * Returns the descriptor corresponding to the given method info.
     * Adapted from ASM Type.getMethodDescriptor(<Method>)
     *
     * @param method
     * @return the descriptor of the given method.
     */
    public static String getMethodDescriptor(final MethodInfo method) {
        ClassInfo[] parameters = method.getParameterTypes();
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for (int i = 0; i < parameters.length; ++i) {
            getClassDescriptor(buf, parameters[i]);
        }
        buf.append(')');
        getClassDescriptor(buf, method.getReturnType());
        return buf.toString();
    }

    /**
     * Returns the descriptor corresponding to the given constructor info.
     *
     * @param constructor
     * @return the descriptor of the given constructor.
     */
    public static String getConstructorDescriptor(final ConstructorInfo constructor) {
        ClassInfo[] parameters = constructor.getParameterTypes();
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for (int i = 0; i < parameters.length; ++i) {
            getClassDescriptor(buf, parameters[i]);
        }
        buf.append(')');
        getClassDescriptor(buf, VOID);
        return buf.toString();
    }

    /**
     * Returns the descriptor corresponding to the given field info.
     *
     * @param field
     * @return the descriptor of the given field.
     */
    public static String getFieldDescriptor(final FieldInfo field) {
        return getClassDescriptor(field.getType());
    }

    /**
     * Returns the descriptor corresponding to the given Java type.
     * Adapted from ASM Type.getClassDescriptor(Class)
     * <p/>
     * TODO remove the delegation to private method
     *
     * @param c an object class, a primitive class or an array class.
     * @return the descriptor corresponding to the given class.
     */
    public static String getClassDescriptor(final ClassInfo c) {
        StringBuffer buf = new StringBuffer();
        getClassDescriptor(buf, c);
        return buf.toString();
    }

    /**
     * Appends the descriptor of the given class to the given string buffer.
     * Adapted from ASM Type.getClassDescriptor(StringBuffer, Class)
     *
     * @param buf   the string buffer to which the descriptor must be appended.
     * @param klass the class whose descriptor must be computed.
     */
    private static void getClassDescriptor(final StringBuffer buf, final ClassInfo klass) {
        ClassInfo d = klass;
        while (true) {
            if (d.isPrimitive()) {
                char car;
                if (d.equals(INTEGER)) {
                    car = 'I';
                } else if (d.equals(VOID)) {
                    car = 'V';
                } else if (d.equals(BOOLEAN)) {
                    car = 'Z';
                } else if (d.equals(BYTE)) {
                    car = 'B';
                } else if (d.equals(CHARACTER)) {
                    car = 'C';
                } else if (d.equals(SHORT)) {
                    car = 'S';
                } else if (d.equals(DOUBLE)) {
                    car = 'D';
                } else if (d.equals(FLOAT)) {
                    car = 'F';
                } else if (d.equals(LONG)) {
                    car = 'J';
                } else {
                    throw new Error("should not happen");
                }
                buf.append(car);
                return;
            } else if (d.isArray()) {
                buf.append('[');
                d = d.getComponentType();
            } else {
                buf.append('L');
                String name = d.getName();
                int len = name.length();
                for (int i = 0; i < len; ++i) {
                    char car = name.charAt(i);
                    buf.append(car == '.' ? '/' : car);
                }
                buf.append(';');
                return;
            }
        }
    }

    /**
     * Returns the Java types corresponding to the argument types of the given
     * method.
     * Adapted from ASM getArgumentTypes(Method)
     *
     * @param method a method.
     * @return the Java types corresponding to the argument types of the given
     *         method.
     */
    public static Type[] getArgumentTypes(final MethodInfo method) {
        ClassInfo[] classes = method.getParameterTypes();
        Type[] types = new Type[classes.length];
        for (int i = classes.length - 1; i >= 0; --i) {
            types[i] = getType(classes[i]);
        }
        return types;
    }

    /**
     * Returns the Java type corresponding to the given class.
     * Adapted from ASM getType(Class)
     *
     * @param c a class.
     * @return the Java type corresponding to the given class.
     */
    public static Type getType(final ClassInfo c) {
        if (c.isPrimitive()) {
            if (c.equals(INTEGER)) {
                return Type.INT_TYPE;
            } else if (c.equals(VOID)) {
                return Type.VOID_TYPE;
            } else if (c.equals(BOOLEAN)) {
                return Type.BOOLEAN_TYPE;
            } else if (c.equals(BYTE)) {
                return Type.BYTE_TYPE;
            } else if (c.equals(CHARACTER)) {
                return Type.CHAR_TYPE;
            } else if (c.equals(SHORT)) {
                return Type.SHORT_TYPE;
            } else if (c.equals(DOUBLE)) {
                return Type.DOUBLE_TYPE;
            } else if (c.equals(FLOAT)) {
                return Type.FLOAT_TYPE;
            } else if (c.equals(LONG)) {
                return Type.LONG_TYPE;
            } else {
                throw new Error("should not happen");
            }
        } else {
            return Type.getType(getClassDescriptor(c));
        }
    }

}
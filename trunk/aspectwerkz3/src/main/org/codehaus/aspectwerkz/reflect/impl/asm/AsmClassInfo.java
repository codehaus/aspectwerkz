/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.asm;

import gnu.trove.TIntObjectHashMap;

import org.codehaus.aspectwerkz.annotation.Annotation;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.CustomAttribute;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ClassInfo interface utilizing the ASM bytecode library for the info retriaval.
 *
 * TODO: the name switching between "/" and "." seems fragile (especially at lookup). Do a review.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas BonÈr</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AsmClassInfo implements ClassInfo {

    /**
     * The bytecode for the class.
     */
    private final byte[] m_bytecode;

    /**
     * The class loader.
     */
    private final ClassLoader m_loader;

    /**
     * The ASM type.
     */
    private Type m_type;

    /**
     * The name of the class.
     */
    private String m_name;

    /**
     * The modifiers.
     */
    private int m_modifiers;

    /**
     * Is the class an interface.
     */
    private boolean m_isInterface = false;

    /**
     * Is the class a primitive type.
     */
    private boolean m_isPrimitive = false;

    /**
     * Is the class of type array.
     */
    private boolean m_isArray = false;

    /**
     * A list with the <code>ConstructorInfo</code> instances.
     */
    private final TIntObjectHashMap m_constructors = new TIntObjectHashMap();

    /**
     * A list with the <code>MethodInfo</code> instances.
     */
    private final TIntObjectHashMap m_methods = new TIntObjectHashMap();

    /**
     * A list with the <code>FieldInfo</code> instances.
     */
    private final TIntObjectHashMap m_fields = new TIntObjectHashMap();

    /**
     * A list with the interfaces class names.
     */
    private String[] m_interfaceClassNames = null;

    /**
     * A list with the interfaces.
     */
    private ClassInfo[] m_interfaces = null;

    /**
     * The super class name.
     */
    private String m_superClassName = null;

    /**
     * The super class.
     */
    private ClassInfo m_superClass = null;

    /**
     * The annotations.
     */
    private List m_annotations = new ArrayList();

    /**
     * The component type name if array type.
     */
    private String m_componentTypeName = null;

    /**
     * The component type if array type.
     */
    private ClassInfo m_componentType = null;

    /**
     * The class info repository.
     */
    private final AsmClassInfoRepository m_classInfoRepository;

    /**
     * Creates a new ClassInfo instance. TODO switch access back to private
     * 
     * @param bytecode
     * @param loader
     */
    public AsmClassInfo(final byte[] bytecode, final ClassLoader loader) {
        if (bytecode == null) {
            throw new IllegalArgumentException("bytecode can not be null");
        }
        m_bytecode = bytecode;
        m_loader = loader;
        m_classInfoRepository = AsmClassInfoRepository.getRepository(loader);
        try {
            ClassReader cr = new ClassReader(bytecode);
            ClassWriter cw = new ClassWriter(true);
            ClassInfoClassAdapter visitor = new ClassInfoClassAdapter(cw);
            cr.accept(visitor, false);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Create a ClassInfo based on a component type and a given dimension
     * Due to java.lang.reflect. behavior, the ClassInfo is almost empty. It is not an interface, only subclass
     * of java.lang.Object, no methods, fields, or constructor, no annotation.
     *
     * TODO: not sure it has to be abstract final but it looks like all reflect based are.
     *
     * @param className
     * @param loader
     * @param componentInfo
     * @param dimension
     */
    private AsmClassInfo(String className, ClassLoader loader, ClassInfo componentInfo, int dimension) {
        m_loader = loader;
        m_name = className.replace('/', '.');
        m_classInfoRepository = AsmClassInfoRepository.getRepository(loader);

        m_isArray = true;
        m_componentType = componentInfo;
        m_componentTypeName = componentInfo.getName();
        m_modifiers = componentInfo.getModifiers() | Modifier.ABSTRACT | Modifier.FINAL;
        m_isInterface = false;//as in java.reflect
        m_superClass = JavaClassInfo.getClassInfo(Object.class);
        m_superClassName = m_superClass.getName();
        m_interfaceClassNames = new String[0];
        m_interfaces = new ClassInfo[0];

        m_bytecode = null;
        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Returns the class info for a specific class.
     * 
     * @return the class info
     */
    public static ClassInfo getClassInfo(final byte[] bytecode, final ClassLoader loader) {
        String className = AsmClassInfo.retrieveClassNameFromBytecode(bytecode);
        AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
        ClassInfo classInfo = repository.getClassInfo(className);
        if (classInfo == null) {
            classInfo = new AsmClassInfo(bytecode, loader);
        }
        return classInfo;
    }

    /**
     * Returns the class info for a specific class.
     * 
     * @return the class info
     */
    public static ClassInfo getClassInfo(final InputStream stream, final ClassLoader loader) {
        try {
            ClassReader cr = new ClassReader(stream);
            ClassWriter cw = new ClassWriter(true);
            ClassNameRetrievalClassAdapter visitor = new ClassNameRetrievalClassAdapter(cw);
            cr.accept(visitor, false);
            String className = visitor.getClassName();
            AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
            ClassInfo classInfo = repository.getClassInfo(className);
            if (classInfo == null) {
                classInfo = new AsmClassInfo(cw.toByteArray(), loader);
            }
            return classInfo;
        } catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Retrieves the class name from the bytecode of a class.
     * 
     * @param bytecode
     * @return the class name
     */
    public static String retrieveClassNameFromBytecode(final byte[] bytecode) {
        ClassReader cr = new ClassReader(bytecode);
        ClassWriter cw = new ClassWriter(true);
        ClassNameRetrievalClassAdapter visitor = new ClassNameRetrievalClassAdapter(cw);
        cr.accept(visitor, false);
        return visitor.getClassName();
    }

    /**
     * Creates a ClassInfo based on the stream retrieved from the class loader through <code>getResourceAsStream</code>.
     * 
     * @param className
     * @param loader
     */
    public static ClassInfo createClassInfoFromStream(String className, final ClassLoader loader) {
        className = className.replace('.', '/');

        // compute array type dimension if any
        int componentTypeIndex = className.indexOf('[');
        String componentName = className;
        int dimension = 1;
        if (componentTypeIndex > 0) {
            componentName = className.substring(0, componentTypeIndex);
            dimension = 1 + (int) (className.length() - componentTypeIndex)/2;
        }

        // primitive type
        if (componentName.indexOf('/') < 0) {
            // it might be one
            Class primitiveClass = AsmClassInfo.getPrimitiveClass(componentName);
            if (primitiveClass != null) {
                if (dimension <= 1) {
                    return JavaClassInfo.getClassInfo(primitiveClass);
                } else {
                    Class arrayClass = Array.newInstance(primitiveClass, dimension).getClass();
                    return JavaClassInfo.getClassInfo(arrayClass);
                }
            }
        }

        // non primitive type
        InputStream componentClassAsStream = loader.getResourceAsStream(componentName + ".class");
        if (componentClassAsStream == null) {
            throw new RuntimeException("could not load class [" + componentName + "] as a resource in loader ["
                    + loader + "]");
        }
        ClassInfo componentInfo = AsmClassInfo.getClassInfo(componentClassAsStream, loader);
        if (dimension <= 1) {
            return componentInfo;
        } else {
            return AsmClassInfo.getArrayClassInfo(className, loader, componentInfo, dimension);
        }
    }

    /**
     * Checks if the class is a of a primitive type, if so create and return the class for the type else return null.
     * 
     * @param className
     * @return the class for the primitive type or null
     */
    public static Class getPrimitiveClass(final String className) {
        if (className.equals("void")) {
            return void.class;
        } else if (className.equals("long")) {
            return long.class;
        } else if (className.equals("int")) {
            return int.class;
        } else if (className.equals("short")) {
            return short.class;
        } else if (className.equals("double")) {
            return double.class;
        } else if (className.equals("float")) {
            return float.class;
        } else if (className.equals("byte")) {
            return byte.class;
        } else if (className.equals("boolean")) {
            return boolean.class;
        } else if (className.equals("char")) {
            return char.class;
        } else {
            return null;
        }
    }

    /**
     * Returns the bytecode for the class.
     * 
     * @return Returns the bytecode.
     */
    public byte[] getBytecode() {
        return m_bytecode;
    }

    /**
     * Returns the annotations infos.
     * 
     * @return the annotations infos
     */
    public List getAnnotations() {
        return m_annotations;
    }

    /**
     * Returns the name of the class.
     * 
     * @return the name of the class
     */
    public String getName() {
        return m_name.replace('/', '.');
    }

    /**
     * Returns the class modifiers.
     * 
     * @return the class modifiers
     */
    public int getModifiers() {
        return m_modifiers;
    }

    /**
     * Returns a constructor info by its hash.
     * 
     * @param hash
     * @return
     */
    public ConstructorInfo getConstructor(final int hash) {
        return (ConstructorInfo) m_constructors.get(hash);
    }

    /**
     * Returns a list with all the constructors info.
     * 
     * @return the constructors info
     */
    public ConstructorInfo[] getConstructors() {
        Object[] values = m_constructors.getValues();
        ConstructorInfo[] methodInfos = new ConstructorInfo[values.length];
        for (int i = 0; i < values.length; i++) {
            methodInfos[i] = (ConstructorInfo) values[i];
        }
        return methodInfos;
    }

    /**
     * Returns a method info by its hash.
     * 
     * @param hash
     * @return
     */
    public MethodInfo getMethod(final int hash) {
        return (MethodInfo) m_methods.get(hash);
    }

    /**
     * Returns a list with all the methods info.
     * 
     * @return the methods info
     */
    public MethodInfo[] getMethods() {
        Object[] values = m_methods.getValues();
        MethodInfo[] methodInfos = new MethodInfo[values.length];
        for (int i = 0; i < values.length; i++) {
            methodInfos[i] = (MethodInfo) values[i];
        }
        return methodInfos;
    }

    /**
     * Returns a field info by its hash.
     * 
     * @param hash
     * @return
     */
    public FieldInfo getField(final int hash) {
        return (FieldInfo) m_fields.get(hash);
    }

    /**
     * Returns a list with all the field info.
     * 
     * @return the field info
     */
    public FieldInfo[] getFields() {
        Object[] values = m_fields.getValues();
        FieldInfo[] fieldInfos = new FieldInfo[values.length];
        for (int i = 0; i < values.length; i++) {
            fieldInfos[i] = (FieldInfo) values[i];
        }
        return fieldInfos;
    }

    /**
     * Returns the interfaces.
     * 
     * @return the interfaces
     */
    public ClassInfo[] getInterfaces() {
        if (m_interfaces == null) {
            m_interfaces = new ClassInfo[m_interfaceClassNames.length];
            for (int i = 0; i < m_interfaceClassNames.length; i++) {
                m_interfaces[i] = AsmClassInfo.createClassInfoFromStream(m_interfaceClassNames[i], m_loader);
            }
        }
        return m_interfaces;
    }

    /**
     * Returns the super class.
     * 
     * @return the super class
     */
    public ClassInfo getSuperClass() {
        if (m_superClass == null) {
            m_superClass = AsmClassInfo.createClassInfoFromStream(m_superClassName, m_loader);
        }
        return m_superClass;
    }

    /**
     * Returns the component type if array type else null.
     * 
     * @return the component type
     */
    public ClassInfo getComponentType() {
        if (isArray() && (m_componentTypeName == null)) {
            m_componentType = AsmClassInfo.createClassInfoFromStream(m_componentTypeName, m_loader);
        }
        return m_componentType;
    }

    /**
     * Is the class an interface.
     * 
     * @return
     */
    public boolean isInterface() {
        return m_isInterface;
    }

    /**
     * Is the class a primitive type.
     * 
     * @return
     */
    public boolean isPrimitive() {
        return m_isPrimitive;
    }

    /**
     * Is the class an array type.
     * 
     * @return
     */
    public boolean isArray() {
        return m_isArray;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassInfo)) {
            return false;
        }
        ClassInfo classInfo = (ClassInfo) o;
        return m_name.equals(classInfo.getName().toString());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return m_name.hashCode();
    }

    /**
     * Create a ClassInfo based on a component type and a given dimension
     *
     * @param className
     * @param loader
     * @param componentClassInfo
     * @param dimension
     * @return
     */
    public static ClassInfo getArrayClassInfo(String className, ClassLoader loader, ClassInfo componentClassInfo, int dimension) {
        if (dimension <= 1) {
            return componentClassInfo;
        }
        ClassInfo info = new AsmClassInfo(className, loader, componentClassInfo, dimension);
        return info;
    }


    /**
     * ASM bytecode visitor that retrieves the class name from the bytecode.
     * 
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    public static class ClassNameRetrievalClassAdapter extends ClassAdapter {

        private String m_className;

        public ClassNameRetrievalClassAdapter(final ClassVisitor visitor) {
            super(visitor);
        }

        public void visit(final int access, final String name, final String superName, final String[] interfaces,
                final String sourceFile) {
            m_className = name.replace('/', '.');
            super.visit(access, name, superName, interfaces, sourceFile);
        }

        /**
         * @return Returns the className.
         */
        public String getClassName() {
            return m_className;
        }
    }

    /**
     * ASM bytecode visitor that gathers info about the class.
     * 
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    class ClassInfoClassAdapter extends ClassAdapter {

        public ClassInfoClassAdapter(final ClassVisitor visitor) {
            super(visitor);
        }

        public void visit(final int access, final String name, final String superName, final String[] interfaces,
                final String sourceFile) {
            m_name = name;
            m_modifiers = access;
            m_superClassName = superName;
            m_interfaceClassNames = interfaces;

            // FIXME this algo for array types does most likely NOT WORK (since I assume that ASM is handling arrays
            // using the internal desriptor format)

            if (m_name.endsWith("[]")) {
                m_isArray = true;
                int index = m_name.indexOf('[');
                m_componentTypeName = m_name.substring(0, index);
            } else if (m_name.equals("long") || m_name.equals("int") || m_name.equals("short")
                    || m_name.equals("double") || m_name.equals("float") || m_name.equals("byte")
                    || m_name.equals("boolean") || m_name.equals("char")) {
                m_isPrimitive = true;
            }
            super.visit(access, name, superName, interfaces, sourceFile);
        }

        public void visitField(final int access, final String name, final String desc, final String value,
                final Attribute attrs) {
            final FieldStruct struct = new FieldStruct();
            struct.modifiers = access;
            struct.name = name;
            struct.desc = desc;
            struct.value = value;
            struct.attrs = attrs;
            AsmFieldInfo fieldInfo = new AsmFieldInfo(struct, m_name, m_loader);
            m_fields.put(AsmHelper.calculateHash(struct), fieldInfo);
            super.visitField(access, name, desc, value, attrs);
        }

        public CodeVisitor visitMethod(final int access, final String name, final String desc,
                final String[] exceptions, final Attribute attrs) {
            final MethodStruct struct = new MethodStruct();
            struct.modifiers = access;
            struct.name = name;
            struct.desc = desc;
            struct.exceptions = exceptions;
            struct.attrs = attrs;
            if (name.equals("<clinit>")) {
                // skip <clinit>
            } else if (name.equals("<init>")) {
                AsmConstructorInfo methodInfo = new AsmConstructorInfo(struct, m_name, m_loader);
                m_constructors.put(AsmHelper.calculateHash(struct), methodInfo);
            } else {
                AsmMethodInfo methodInfo = new AsmMethodInfo(struct, m_name, m_loader);
                m_methods.put(AsmHelper.calculateHash(struct), methodInfo);
            }
            return cv.visitMethod(access, name, desc, exceptions, attrs);
        }

        public void visitAttribute(final Attribute attrs) {
            if (attrs == null) {
                return;
            }
            if (attrs instanceof CustomAttribute) {
                CustomAttribute customAttribute = (CustomAttribute) attrs;
                byte[] bytes = customAttribute.getBytes();
                try {
                    m_annotations.add((Annotation) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject());
                } catch (Exception e) {
                    System.err.println("WARNING: could not deserialize annotation");
                }
            }

            // bring on the next attribute
            visitAttribute(attrs.next);
        }
    }
}
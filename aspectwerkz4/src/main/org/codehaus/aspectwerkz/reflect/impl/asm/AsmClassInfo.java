/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.asm;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntArrayList;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAnnotationHelper;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.attrs.Annotation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Implementation of the ClassInfo interface utilizing the ASM bytecode library for the info retriaval.
 * <p/>
 * Annotations are lazily gathered, unless required to visit them at the same time as we visit methods and fields.
 * <p/>
 * This implementation guarantees that the method, fields and constructors can be retrieved in the same order as they were in the bytecode
 * (it can depends of the compiler and might not be the order of the source code - f.e. IBM compiler)
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AsmClassInfo implements ClassInfo {

    protected final static String[] EMPTY_STRING_ARRAY = new String[0];

    protected final static List EMPTY_LIST = new ArrayList();

    private final static Attribute[] NO_ATTRIBUTES = new Attribute[0];

    /**
     * The class loader wrapped in a weak ref.
     */
    private final WeakReference m_loaderRef;

    /**
     * The name of the class.
     */
    private String m_name;

    /**
     * The signature of the class.
     */
    private String m_signature;

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
     * Flag for the static initializer method.
     */
    private boolean m_hasStaticInitializer = false;

    /**
     * A list with the <code>ConstructorInfo</code> instances.
     * When visiting the bytecode, we keep track of the order of the visit.
     * The first time the getConstructors() gets called, we build an array and then reuse it directly.
     */
    private final TIntObjectHashMap m_constructors = new TIntObjectHashMap();
    private TIntArrayList m_sortedConstructorHashes = new TIntArrayList();
    private ConstructorInfo[] m_constructorsLazy = null;


    /**
     * A list with the <code>MethodInfo</code> instances.
     * When visiting the bytecode, we keep track of the order of the visit.
     * The first time the getMethods() gets called, we build an array and then reuse it directly.
     */
    private final TIntObjectHashMap m_methods = new TIntObjectHashMap();
    private TIntArrayList m_sortedMethodHashes = new TIntArrayList();
    private MethodInfo[] m_methodsLazy = null;

    /**
     * A list with the <code>FieldInfo</code> instances.
     * When visiting the bytecode, we keep track of the order of the visit.
     * The first time the getFields() gets called, we build an array and then reuse it directly.
     */
    private final TIntObjectHashMap m_fields = new TIntObjectHashMap();
    private TIntArrayList m_sortedFieldHashes = new TIntArrayList();
    private FieldInfo[] m_fieldsLazy = null;

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
     * Lazily populated.
     */
    private List m_annotations = null;

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
     * Creates a new ClassInfo instance.
     *
     * @param bytecode
     * @param loader
     */
    AsmClassInfo(final byte[] bytecode, final ClassLoader loader, boolean lazyAttributes) {
        if (bytecode == null) {
            throw new IllegalArgumentException("bytecode can not be null");
        }
        m_loaderRef = new WeakReference(loader);
        m_classInfoRepository = AsmClassInfoRepository.getRepository(loader);
        try {
            ClassReader cr = new ClassReader(bytecode);
            ClassInfoClassAdapter visitor = new ClassInfoClassAdapter(lazyAttributes);
            cr.accept(visitor, lazyAttributes ? NO_ATTRIBUTES : AsmAnnotationHelper.ANNOTATIONS_ATTRIBUTES, false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Creates a new ClassInfo instance.
     *
     * @param resourceStream
     * @param loader
     */
    AsmClassInfo(final InputStream resourceStream, final ClassLoader loader) {
        if (resourceStream == null) {
            throw new IllegalArgumentException("resource stream can not be null");
        }
        m_loaderRef = new WeakReference(loader);
        m_classInfoRepository = AsmClassInfoRepository.getRepository(loader);
        try {
            ClassReader cr = new ClassReader(resourceStream);
            ClassInfoClassAdapter visitor = new ClassInfoClassAdapter(true);
            cr.accept(visitor, NO_ATTRIBUTES, false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Create a ClassInfo based on a component type and a given dimension Due to java.lang.reflect. behavior, the
     * ClassInfo is almost empty. It is not an interface, only subclass of java.lang.Object, no methods, fields, or
     * constructor, no annotation.
     *
     * @param className
     * @param loader
     * @param componentInfo
     * @param dimension
     */
    AsmClassInfo(final String className,
                 final ClassLoader loader,
                 final ClassInfo componentInfo,
                 final int dimension) { // TODO dimension param is not used
        m_loaderRef = new WeakReference(loader);
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
        m_signature = AsmHelper.getClassDescriptor(this);
        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Returns a completely new class info for a specific class. Does not cache.
     *
     * @param bytecode
     * @param loader
     * @return the class info
     */
    public static ClassInfo newClassInfo(final byte[] bytecode, final ClassLoader loader) {
        final String className = AsmClassInfo.retrieveClassNameFromBytecode(bytecode);
        AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
        repository.removeClassInfo(className);
        return new AsmClassInfo(bytecode, loader, true);
    }

    /**
     * Returns the class info for a specific class.
     *
     * @param className
     * @param loader
     * @return the class info
     */
    public static ClassInfo getClassInfo(final String className, final ClassLoader loader) {
        final String javaClassName = getJavaClassName(className);
        AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
        ClassInfo classInfo = repository.getClassInfo(javaClassName);
        if (classInfo == null) {
            classInfo = createClassInfoFromStream(javaClassName, loader, true);
        }
        return classInfo;
    }

    /**
     * Returns the class info for a specific class.
     *
     * @param bytecode
     * @param loader
     * @return the class info
     */
    public static ClassInfo getClassInfo(final byte[] bytecode, final ClassLoader loader) {
        final String className = AsmClassInfo.retrieveClassNameFromBytecode(bytecode);
        AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
        ClassInfo classInfo = repository.getClassInfo(className);
        if (classInfo == null) {
            classInfo = new AsmClassInfo(bytecode, loader, true);
        }
        return classInfo;
    }

    /**
     * Returns the class info for a specific class.
     *
     * @param stream
     * @param loader
     * @return the class info
     */
    public static ClassInfo getClassInfo(final InputStream stream, final ClassLoader loader) {
        try {
            ClassReader cr = new ClassReader(stream);
            // keep a copy of the bytecode, since me way want to "reuse the stream"
            byte[] bytes = cr.b;
            ClassNameRetrievalClassAdapter visitor = new ClassNameRetrievalClassAdapter();
            cr.accept(visitor, NO_ATTRIBUTES, true);
            final String className = visitor.getClassName();
            AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
            ClassInfo classInfo = repository.getClassInfo(className);
            if (classInfo == null) {
                classInfo = new AsmClassInfo(bytes, loader, true);
            }
            return classInfo;
        } catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the class info for a specific class.
     *
     * @param stream
     * @param loader
     * @param lazyAttributes
     * @return the class info
     */
    public static ClassInfo getClassInfo(final InputStream stream, final ClassLoader loader, boolean lazyAttributes) {
        if (lazyAttributes) {
            return getClassInfo(stream, loader);
        }
        try {
            ClassReader cr = new ClassReader(stream);
            // keep a copy of the bytecode, since me way want to "reuse the stream"
            byte[] bytes = cr.b;
            ClassNameRetrievalClassAdapter visitor = new ClassNameRetrievalClassAdapter();
            cr.accept(visitor, NO_ATTRIBUTES, true);
            String className = visitor.getClassName();
            AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
            ClassInfo classInfo = repository.getClassInfo(className);
            if (classInfo == null) {
                classInfo = new AsmClassInfo(bytes, loader, lazyAttributes);
            }
            return classInfo;
        } catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Marks the class as dirty (since it has been modified and needs to be rebuild).
     *
     * @param className
     */
    public static void markDirty(final String className, final ClassLoader loader) {
        AsmClassInfoRepository.getRepository(loader).removeClassInfo(className);
    }

    /**
     * Retrieves the class name from the bytecode of a class.
     *
     * @param bytecode
     * @return the class name
     */
    public static String retrieveClassNameFromBytecode(final byte[] bytecode) {
        ClassReader cr = new ClassReader(bytecode);
        ClassNameRetrievalClassAdapter visitor = new ClassNameRetrievalClassAdapter();
        cr.accept(visitor, NO_ATTRIBUTES, true);
        return visitor.getClassName();
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
     * Returns the annotations infos.
     *
     * @return the annotations infos
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            if (isPrimitive() || isArray()) {
                m_annotations = EMPTY_LIST;
            } else {
                try {
                    InputStream in = null;
                    ClassReader cr = null;
                    try {
                        in =
                        ((ClassLoader) m_loaderRef.get()).getResourceAsStream(m_name.replace('.', '/') + ".class");
                        cr = new ClassReader(in);
                    } finally {
                        try {
                            in.close();
                        } catch (Exception e) {
                            ;
                        }
                    }
                    List annotations = new ArrayList();
                    cr.accept(
                            new AsmAnnotationHelper.ClassAnnotationExtractor(
                                    annotations, (ClassLoader) m_loaderRef.get()
                            ),
                            AsmAnnotationHelper.ANNOTATIONS_ATTRIBUTES,
                            true
                    );
                    m_annotations = annotations;
                } catch (IOException e) {
                    // unlikely to occur since ClassInfo relies on getResourceAsStream
                    System.err.println(
                            "AW::WARNING  could not load " + m_name + " as a resource to retrieve annotations"
                    );
                    m_annotations = EMPTY_LIST;
                }
            }
        }
        return m_annotations;
    }

    /**
     * Returns the name of the class.
     *
     * @return the name of the class
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the signature for the class.
     *
     * @return the signature for the class
     */
    public String getSignature() {
        return m_signature;
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
     * Checks if the class has a static initalizer.
     *
     * @return
     */
    public boolean hasStaticInitializer() {
        return m_hasStaticInitializer;
    }

    /**
     * Returns a constructor info by its hash.
     *
     * @param hash
     * @return
     */
    public ConstructorInfo getConstructor(final int hash) {
        ConstructorInfo constructorInfo = (ConstructorInfo) m_constructors.get(hash);
        if (constructorInfo == null) {
            // lookup in the class hierarchy
            ClassInfo superClassInfo = getSuperclass();
            while (superClassInfo != null) {
                constructorInfo = superClassInfo.getConstructor(hash);
                if (constructorInfo == null) {
                    // go up in the hierarchy
                    superClassInfo = superClassInfo.getSuperclass();
                } else {
                    break;
                }
            }
        }
        return constructorInfo;
    }

    /**
     * Returns a list with all the constructors info.
     *
     * @return the constructors info
     */
    public ConstructorInfo[] getConstructors() {
        if (m_constructorsLazy == null) {
            ConstructorInfo[] constructorInfos = new ConstructorInfo[m_sortedConstructorHashes.size()];
            for (int i = 0; i < m_sortedConstructorHashes.size(); i++) {
                constructorInfos[i] = (ConstructorInfo) m_constructors.get(m_sortedConstructorHashes.get(i));
            }
            m_constructorsLazy = constructorInfos;
        }
        return m_constructorsLazy;
    }

    /**
     * Returns a method info by its hash.
     *
     * @param hash
     * @return
     */
    public MethodInfo getMethod(final int hash) {
        MethodInfo methodInfo = (MethodInfo) m_methods.get(hash);
        if (methodInfo == null) {
            // lookup in the class hierarchy
            ClassInfo superClassInfo = getSuperclass();
            while (superClassInfo != null) {
                methodInfo = superClassInfo.getMethod(hash);
                if (methodInfo == null) {
                    // go up in the hierarchy
                    superClassInfo = superClassInfo.getSuperclass();
                } else {
                    break;
                }
            }
        }
        return methodInfo;
    }

    /**
     * Returns a list with all the methods info.
     *
     * @return the methods info
     */
    public MethodInfo[] getMethods() {
        if (m_methodsLazy == null) {
            MethodInfo[] methodInfos = new MethodInfo[m_sortedMethodHashes.size()];
            for (int i = 0; i < m_sortedMethodHashes.size(); i++) {
                methodInfos[i] = (MethodInfo) m_methods.get(m_sortedMethodHashes.get(i));
            }
            m_methodsLazy = methodInfos;
        }
        return m_methodsLazy;
    }

    /**
     * Returns a field info by its hash.
     *
     * @param hash
     * @return
     */
    public FieldInfo getField(final int hash) {
        FieldInfo fieldInfo = (FieldInfo) m_fields.get(hash);
        if (fieldInfo == null) {
            // lookup in the class hierarchy
            ClassInfo superClassInfo = getSuperclass();
            while (superClassInfo != null) {
                fieldInfo = superClassInfo.getField(hash);
                if (fieldInfo == null) {
                    // go up in the hierarchy
                    superClassInfo = superClassInfo.getSuperclass();
                } else {
                    break;
                }
            }
        }
        return fieldInfo;
    }

    /**
     * Returns a list with all the field info.
     *
     * @return the field info
     */
    public FieldInfo[] getFields() {
        if (m_fieldsLazy == null) {
            FieldInfo[] fieldInfos = new FieldInfo[m_sortedFieldHashes.size()];
            for (int i = 0; i < m_sortedFieldHashes.size(); i++) {
                fieldInfos[i] = (FieldInfo) m_fields.get(m_sortedFieldHashes.get(i));
            }
            m_fieldsLazy = fieldInfos;
        }
        return m_fieldsLazy;
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
                m_interfaces[i] = AsmClassInfo.getClassInfo(m_interfaceClassNames[i], (ClassLoader) m_loaderRef.get());
            }
        }
        return m_interfaces;
    }

    /**
     * Returns the super class.
     *
     * @return the super class
     */
    public ClassInfo getSuperclass() {
        if (m_superClass == null && m_superClassName != null) {
            m_superClass = AsmClassInfo.getClassInfo(m_superClassName, (ClassLoader) m_loaderRef.get());
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
            m_componentType = AsmClassInfo.getClassInfo(m_componentTypeName, (ClassLoader) m_loaderRef.get());
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
        return m_name.equals(classInfo.getName());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return m_name.hashCode();
    }

    public String toString() {
        return m_name;
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
    public static ClassInfo getArrayClassInfo(final String className,
                                              final ClassLoader loader,
                                              final ClassInfo componentClassInfo,
                                              final int dimension) {
        if (dimension <= 1) {
            return componentClassInfo;
        }
        return new AsmClassInfo(className, loader, componentClassInfo, dimension);
    }

    /**
     * Creates a ClassInfo based on the stream retrieved from the class loader through
     * <code>getResourceAsStream</code>.
     *
     * @param name
     * @param loader
     * @param lazyAttributes
     */
    private static ClassInfo createClassInfoFromStream(final String name,
                                                       final ClassLoader loader,
                                                       boolean lazyAttributes) {
        final String className = name.replace('.', '/');

        // compute array type dimension if any
        int componentTypeIndex = className.indexOf('[');

        String componentName = className;
        int dimension = 1;
        if (componentTypeIndex > 0) {
            componentName = className.substring(0, componentTypeIndex);
            dimension = 1 + (className.length() - componentTypeIndex) / 2;
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
        InputStream componentClassAsStream = null;
        if (loader != null) {
            componentClassAsStream = loader.getResourceAsStream(componentName + ".class");
        } else {
            // boot class loader, fall back to system classloader that will see it anyway
            componentClassAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream(componentName + ".class");
        }
        if (componentClassAsStream == null) {
            new RuntimeException(
                    "could not load class ["
                    + componentName
                    + "] as a resource in loader ["
                    + loader
                    + "]"
            ).printStackTrace();
            return new ClassInfo.NullClassInfo();
        }
        ClassInfo componentInfo = null;
        try {
            componentInfo = AsmClassInfo.getClassInfo(componentClassAsStream, loader, lazyAttributes);
        } finally {
            try {
                componentClassAsStream.close();
            } catch (Exception e) {
                ;
            }
        }

        if (dimension <= 1) {
            return componentInfo;
        } else {
            return AsmClassInfo.getArrayClassInfo(className, loader, componentInfo, dimension);
        }
    }

    /**
     * Creates and returns a new annotation info build up from the Java5 annotation.
     *
     * @param annotation the ASM annotation abstractiono
     * @param loader     the class loader that has loaded the proxy class to use
     * @return the annotation info
     */
    public static AnnotationInfo getAnnotationInfo(final Annotation annotation, final ClassLoader loader) {
        String annotationName = annotation.type.substring(1, annotation.type.length() - 1).replace('/', '.');
        final org.codehaus.aspectwerkz.annotation.Annotation proxy = AsmAnnotationHelper.getAnnotationProxy(
                annotation,
                loader
        );
        return new AnnotationInfo(annotationName, proxy);
    }

    /**
     * Creates a string with the annotation key value pairs.
     *
     * @param annotation
     * @return the string
     */
    private static String createAnnotationKeyValueString(final Annotation annotation) {
        List elementValues = annotation.elementValues;
        StringBuffer annotationValues = new StringBuffer();
        if (elementValues.size() != 0) {
            int i = 0;
            for (Iterator iterator = elementValues.iterator(); iterator.hasNext();) {
                Object[] keyValuePair = (Object[]) iterator.next();
                annotationValues.append((String) keyValuePair[0]);
                annotationValues.append('=');
                annotationValues.append(keyValuePair[1].toString());
                if (i < elementValues.size() - 1) {
                    annotationValues.append(',');
                }
            }
        }
        return annotationValues.toString();
    }

    /**
     * ASM bytecode visitor that retrieves the class name from the bytecode.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
     */
    public static class ClassNameRetrievalClassAdapter extends AsmAnnotationHelper.NullClassAdapter {

        private String m_className;

        public void visit(final int version,
                          final int access,
                          final String name,
                          final String superName,
                          final String[] interfaces,
                          final String sourceFile) {
            m_className = name.replace('/', '.');
        }

        public String getClassName() {
            return m_className;
        }
    }

    /**
     * ASM bytecode visitor that gathers info about the class.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
     */
    private class ClassInfoClassAdapter extends AsmAnnotationHelper.NullClassAdapter {

        public boolean m_lazyAttributes = true;

        public ClassInfoClassAdapter(boolean lazyAttributes) {
            m_lazyAttributes = lazyAttributes;
        }

        public void visit(final int version,
                          final int access,
                          final String name,
                          final String superName,
                          final String[] interfaces,
                          final String sourceFile) {

            m_name = name.replace('/', '.');
            m_modifiers = access;
            m_isInterface = Modifier.isInterface(m_modifiers);
            // special case for java.lang.Object, which does not extend anything
            m_superClassName = superName == null ? null : superName.replace('/', '.');
            m_interfaceClassNames = new String[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                m_interfaceClassNames[i] = interfaces[i].replace('/', '.');
            }
            // FIXME this algo for array types does most likely NOT WORK (since
            // I assume that ASM is handling arrays
            // using the internal desriptor format '[L' and the algo is using '[]')
            if (m_name.endsWith("[]")) {
                m_isArray = true;
                int index = m_name.indexOf('[');
                m_componentTypeName = m_name.substring(0, index);
            } else if (m_name.equals("long")
                       || m_name.equals("int")
                       || m_name.equals("short")
                       || m_name.equals("double")
                       || m_name.equals("float")
                       || m_name.equals("byte")
                       || m_name.equals("boolean")
                       || m_name.equals("char")) {
                m_isPrimitive = true;
            }
        }

        public void visitAttribute(final Attribute attribute) {
            // attributes
            if (!m_lazyAttributes) {
                List annotations = new ArrayList();
                annotations =
                AsmAnnotationHelper.extractAnnotations(annotations, attribute, (ClassLoader) m_loaderRef.get());
                m_annotations = annotations;
            }
        }

        public void visitField(final int access,
                               final String name,
                               final String desc,
                               final Object value,
                               final Attribute attrs) {
            final FieldStruct struct = new FieldStruct();
            struct.modifiers = access;
            struct.name = name;
            struct.desc = desc;
            struct.value = value;
            AsmFieldInfo fieldInfo = new AsmFieldInfo(struct, m_name, (ClassLoader) m_loaderRef.get());
            // attributes
            if (!m_lazyAttributes) {
                List annotations = new ArrayList();
                annotations =
                AsmAnnotationHelper.extractAnnotations(annotations, attrs, (ClassLoader) m_loaderRef.get());
                fieldInfo.m_annotations = annotations;
            }
            int hash = AsmHelper.calculateFieldHash(name, desc);
            m_fields.put(hash, fieldInfo);
            m_sortedFieldHashes.add(hash);
        }

        public CodeVisitor visitMethod(final int access,
                                       final String name,
                                       final String desc,
                                       final String[] exceptions,
                                       final Attribute attrs) {
            final MethodStruct struct = new MethodStruct();
            struct.modifiers = access;
            struct.name = name;
            struct.desc = desc;
            struct.exceptions = exceptions;
            int hash = AsmHelper.calculateMethodHash(name, desc);
            // the methodInfo that should be updated when we will visit the method parameter names info if needed.
            AsmMethodInfo methodInfo = null;
            if (name.equals(TransformationConstants.CLINIT_METHOD_NAME)) {
                m_hasStaticInitializer = true;
            } else {
                AsmMemberInfo memberInfo = null;
                if (name.equals(TransformationConstants.INIT_METHOD_NAME)) {
                    memberInfo = new AsmConstructorInfo(struct, m_name, (ClassLoader) m_loaderRef.get());
                    m_constructors.put(hash, memberInfo);
                    m_sortedConstructorHashes.add(hash);
                } else {
                    memberInfo = new AsmMethodInfo(struct, m_name, (ClassLoader) m_loaderRef.get());
                    m_methods.put(hash, memberInfo);
                    m_sortedMethodHashes.add(hash);
                    methodInfo = (AsmMethodInfo) memberInfo;
                }
                // attributes
                if (!m_lazyAttributes) {
                    List annotations = new ArrayList();
                    annotations =
                    AsmAnnotationHelper.extractAnnotations(annotations, attrs, (ClassLoader) m_loaderRef.get());
                    memberInfo.m_annotations = annotations;
                }
            }
            if (methodInfo != null) {
                // visit the method to access the parameter names as required to support Aspect with bindings
                // TODO: should we make this optional - similar to m_lazyAttributes ?
                Type[] parameterTypes = Type.getArgumentTypes(desc);
                if (parameterTypes.length > 0) {
                    CodeVisitor methodParameterNamesVisitor = new MethodParameterNamesCodeAdapter(
                            Modifier.isStatic(access),
                            parameterTypes.length, methodInfo
                    );
                    return methodParameterNamesVisitor;
                } else {
                    methodInfo.m_parameterNames = EMPTY_STRING_ARRAY;
                }
            }
            return AsmAnnotationHelper.NullCodeAdapter.NULL_CODE_ADAPTER;
        }

        public void visitEnd() {
            m_signature = AsmHelper.getClassDescriptor(AsmClassInfo.this);
        }
    }

    /**
     * Extracts method parameter names as they appear in the source code from debug infos
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    static class MethodParameterNamesCodeAdapter extends AsmAnnotationHelper.NullCodeAdapter {
        private final boolean m_isStatic;
        private final int m_parameterCount;
        private int m_parameterFound = 0;
        private int m_parameterIndex = 0;
        private AsmMethodInfo m_methodInfo;

        public MethodParameterNamesCodeAdapter(boolean isStatic, int parameterCount, AsmMethodInfo methodInfo) {
            m_isStatic = isStatic;
            m_parameterCount = parameterCount;
            m_methodInfo = methodInfo;
            m_methodInfo.m_parameterNames = new String[m_parameterCount];
        }

        public void visitLocalVariable(String name, String desc, Label start, Label end, int index) {
            if (m_parameterFound == 0 && !m_isStatic) {
                m_parameterFound++;// skip "this"
            } else {
                m_parameterFound++;
                if (m_parameterIndex < m_methodInfo.m_parameterNames.length) {
                    m_methodInfo.m_parameterNames[m_parameterIndex++] = name;
                } else {
                    ;// skip code block locals
                }
            }
        }
    }

    /**
     * Converts the class name from VM type class name to Java type class name.
     *
     * @param className the VM type class name
     * @return the Java type class name
     */
    private static String getJavaClassName(final String className) {
        String javaClassName;
        if (className.startsWith("[")) {
            javaClassName = Type.getType(className).getClassName();
        } else {
            javaClassName = className.replace('/', '.');
        }
        return javaClassName;
    }
}
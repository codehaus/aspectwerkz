/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.asm;

import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;

import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.codehaus.aspectwerkz.definition.DescriptorUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.expression.QDoxParser;
import org.codehaus.aspectwerkz.reflect.TypeConverter;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.attrs.RuntimeInvisibleAnnotations;
import org.objectweb.asm.attrs.Attributes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Enhances classes with custom attributes using the ASM library.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AsmAttributeEnhancer implements AttributeEnhancer {
    /**
     * The class reader.
     */
    private ClassReader m_reader = null;

    /**
     * The name of the class file.
     */
    private String m_classFileName = null;

    /**
     * The class name.
     */
    private String m_className = null;

    /**
     * Compiled class class loader
     */
    private URLClassLoader m_loader = null;

    /**
     * The class attributes.
     */
    private List m_classAttributes = new ArrayList();

    /**
     * The constructor attributes.
     */
    private List m_constructorAttributes = new ArrayList();

    /**
     * The method attributes.
     */
    private List m_methodAttributes = new ArrayList();

    /**
     * The field attributes.
     */
    private List m_fieldAttributes = new ArrayList();

    /**
     * Initializes the attribute enhancer. Must always be called before use.
     *
     * @param className the class name
     * @param classPath the class path
     * @return true if the class was succefully loaded, false otherwise
     */
    public boolean initialize(final String className, final URL[] classPath) {
        try {
            m_className = className;
            m_loader = new URLClassLoader(classPath);
            m_classFileName = className.replace('.', '/') + ".class";
            InputStream classAsStream = m_loader.getResourceAsStream(m_classFileName);
            if (classAsStream == null) {
                return false;
            }
            // setup the ASM stuff in init, but only parse at write time
            try {
                m_reader = new ClassReader(classAsStream);
            } catch (Exception e) {
                throw new ClassNotFoundException(m_className, e);
            } finally {
                classAsStream.close();//AW-296
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return true;
    }

    /**
     * Inserts an attribute on class level.
     *
     * @param attribute the attribute
     */
    public void insertClassAttribute(final Object attribute) {
        if (m_reader == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        final byte[] serializedAttribute = serialize(attribute);
        m_classAttributes.add(serializedAttribute);
    }

    /**
     * Inserts an attribute on field level.
     *
     * @param field     the QDox java field
     * @param attribute the attribute
     */
    public void insertFieldAttribute(final JavaField field, final Object attribute) {
        if (m_reader == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        final byte[] serializedAttribute = serialize(attribute);
        m_fieldAttributes.add(new FieldAttributeInfo(field, serializedAttribute));
    }

    /**
     * Inserts an attribute on method level.
     *
     * @param method    the QDox java method
     * @param attribute the attribute
     */
    public void insertMethodAttribute(final JavaMethod method, final Object attribute) {
        if (m_reader == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        final String[] methodParamTypes = new String[method.getParameters().length];
        for (int i = 0; i < methodParamTypes.length; i++) {
            methodParamTypes[i] = TypeConverter.convertTypeToJava(method.getParameters()[i].getType());
        }
        final byte[] serializedAttribute = serialize(attribute);
        m_methodAttributes.add(new MethodAttributeInfo(method, serializedAttribute));
    }

    /**
     * Inserts an attribute on constructor level.
     *
     * @param constructor the QDox java method
     * @param attribute   the attribute
     */
    public void insertConstructorAttribute(final JavaMethod constructor, final Object attribute) {
        if (m_reader == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        final String[] methodParamTypes = new String[constructor.getParameters().length];
        for (int i = 0; i < methodParamTypes.length; i++) {
            methodParamTypes[i] = TypeConverter.convertTypeToJava(constructor.getParameters()[i].getType());
        }
        final byte[] serializedAttribute = serialize(attribute);
        m_constructorAttributes.add(new MethodAttributeInfo(constructor, serializedAttribute));
    }

    /**
     * Writes the enhanced class to file.
     *
     * @param destDir the destination directory
     */
    public void write(final String destDir) {
        if (m_reader == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        try {
            // parse the bytecode
            ClassWriter writer = AsmHelper.newClassWriter(true);
            m_reader.accept(new AttributeClassAdapter(writer), Attributes.getDefaultAttributes(), false);

            // write the bytecode to disk
            String path = destDir + File.separator + m_classFileName;
            File file = new File(path);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                // directory does not exist create all directories in the path
                if (!parentFile.mkdirs()) {
                    throw new RuntimeException(
                            "could not create dir structure needed to write file "
                            + path
                            + " to disk"
                    );
                }
            }
            FileOutputStream os = new FileOutputStream(destDir + File.separator + m_classFileName);
            os.write(writer.toByteArray());
            os.close();
        } catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Serializes the attribute to byte array.
     *
     * @param attribute the attribute
     * @return the attribute as a byte array
     */
    public static byte[] serialize(final Object attribute) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(attribute);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Return the first interfaces implemented by a level in the class hierarchy (bottom top)
     *
     * @return nearest superclass (including itself) implemented interfaces
     */
    public String[] getNearestInterfacesInHierarchy(final String innerClassName) {
        if (m_loader == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        try {
            Class innerClass = Class.forName(innerClassName, false, m_loader);
            return getNearestInterfacesInHierarchy(innerClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("could not load mixin for mixin implicit interface: " + e.toString());
        } catch (NoClassDefFoundError er) {
            // raised if extends / implements dependancies not found
            throw new RuntimeException(
                    "could not find dependency for mixin implicit interface: "
                    + innerClassName
                    + " due to: "
                    + er.toString()
            );
        }
    }

    /**
     * Return the first interfaces implemented by a level in the class hierarchy (bottom top)
     *
     * @return nearest superclass (including itself) implemented interfaces starting from root
     */
    private String[] getNearestInterfacesInHierarchy(final Class root) {
        if (root == null) {
            return new String[]{};
        }
        Class[] implementedClasses = root.getInterfaces();
        String[] interfaces = null;
        if (implementedClasses.length == 0) {
            interfaces = getNearestInterfacesInHierarchy(root.getSuperclass());
        } else {
            interfaces = new String[implementedClasses.length];
            for (int i = 0; i < implementedClasses.length; i++) {
                interfaces[i] = implementedClasses[i].getName();
            }
        }
        return interfaces;
    }

    /**
     * Base class for the attribute adapter visitors.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private class AttributeClassAdapter extends ClassAdapter {
        private static final String INIT_METHOD_NAME = "<init>";

        private boolean classLevelAnnotationDone = false;

        public AttributeClassAdapter(final ClassVisitor cv) {
            super(cv);
        }

        public void visitField(final int access,
                               final String name,
                               final String desc,
                               final Object value,
                               final Attribute attrs) {

            RuntimeInvisibleAnnotations invisible = CustomAttributeHelper.linkRuntimeInvisibleAnnotations(attrs);
            for (Iterator it = m_fieldAttributes.iterator(); it.hasNext();) {
                FieldAttributeInfo struct = (FieldAttributeInfo) it.next();
                if (name.equals(struct.field.getName())) {
                    invisible.annotations.add(CustomAttributeHelper.createCustomAnnotation(struct.attribute));
                }
            }
            if (invisible.annotations.size() == 0) {
                invisible = null;
            }
            super.visitField(access, name, desc, value, (attrs != null) ? attrs : invisible);
        }

        public CodeVisitor visitMethod(final int access,
                                       final String name,
                                       final String desc,
                                       final String[] exceptions,
                                       final Attribute attrs) {

            RuntimeInvisibleAnnotations invisible = CustomAttributeHelper.linkRuntimeInvisibleAnnotations(attrs);
            if (!name.equals(INIT_METHOD_NAME)) {
                for (Iterator it = m_methodAttributes.iterator(); it.hasNext();) {
                    MethodAttributeInfo struct = (MethodAttributeInfo) it.next();
                    JavaMethod method = struct.method;
                    String[] parameters = QDoxParser.getJavaMethodParametersAsStringArray(method);
                    if (name.equals(method.getName()) && Arrays.equals(parameters, DescriptorUtil.getParameters(desc))) {
                        invisible.annotations.add(CustomAttributeHelper.createCustomAnnotation(struct.attribute));
                    }
                }
            } else {
                for (Iterator it = m_constructorAttributes.iterator(); it.hasNext();) {
                    MethodAttributeInfo struct = (MethodAttributeInfo) it.next();
                    JavaMethod method = struct.method;
                    String[] parameters = QDoxParser.getJavaMethodParametersAsStringArray(method);
                    if (name.equals(INIT_METHOD_NAME) && Arrays.equals(parameters, DescriptorUtil.getParameters(desc))) {
                        invisible.annotations.add(CustomAttributeHelper.createCustomAnnotation(struct.attribute));
                    }
                }
            }
            if (invisible.annotations.size() == 0) {
                invisible = null;
            }
            return cv.visitMethod(access, name, desc, exceptions, (attrs != null) ? attrs : invisible);
        }

        public void visitAttribute(Attribute attrs) {
            classLevelAnnotationDone = true;
            RuntimeInvisibleAnnotations invisible = CustomAttributeHelper.linkRuntimeInvisibleAnnotations(attrs);
            for (Iterator it = m_classAttributes.iterator(); it.hasNext();) {
                byte[] bytes = (byte[]) it.next();
                invisible.annotations.add(CustomAttributeHelper.createCustomAnnotation(bytes));
            }
            if (invisible.annotations.size() == 0) {
                invisible = null;
            }
            super.visitAttribute((attrs != null) ? attrs : invisible);
        }

        public void visitEnd() {
            if (!classLevelAnnotationDone) {
                classLevelAnnotationDone = true;
                RuntimeInvisibleAnnotations invisible = CustomAttributeHelper.linkRuntimeInvisibleAnnotations(null);
                for (Iterator it = m_classAttributes.iterator(); it.hasNext();) {
                    byte[] bytes = (byte[]) it.next();
                    invisible.annotations.add(CustomAttributeHelper.createCustomAnnotation(bytes));
                }
                if (invisible.annotations.size() > 0) {
                    super.visitAttribute(invisible);
                }
                super.visitEnd();
            }
        }
    }

    /**
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private static class FieldAttributeInfo {
        public final byte[] attribute;
        public final JavaField field;

        public FieldAttributeInfo(final JavaField field, final byte[] attribute) {
            this.field = field;
            this.attribute = attribute;
        }
    }

    /**
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private static class MethodAttributeInfo {
        public final byte[] attribute;
        public final JavaMethod method;

        public MethodAttributeInfo(final JavaMethod method, final byte[] attribute) {
            this.method = method;
            this.attribute = attribute;
        }
    }
}
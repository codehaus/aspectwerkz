/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.asm;

import org.codehaus.aspectwerkz.definition.DescriptorUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.apache.xmlbeans.impl.jam.JField;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JParameter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * Enhances classes with attributes using the ASM library.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AsmAttributeEnhancer implements AttributeEnhancer {
    /**
     * The class reader.
     */
    private ClassReader m_reader = null;

    /**
     * The class writer.
     */
    private ClassWriter m_writer = null;

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
     * Initializes the attribute enhancer. Must always be called before use.
     *
     * @param className the class name
     * @param classPath the class path
     * @return true if the class was succefully loaded, false otherwise
     */
    public boolean initialize(final String className, final String classPath, boolean isInner) {
        try {
            m_className = className;
            URL[] urls = new URL[] { new File(classPath).toURL() };
            m_loader = new URLClassLoader(urls);
            m_classFileName = className.replace('.', '/') + ".class";
            InputStream classAsStream = m_loader.getResourceAsStream(m_classFileName);
            if (classAsStream == null) {
                return false;
            }
            try {
                m_reader = new ClassReader(classAsStream);
                m_writer = new ClassWriter(true);
                m_reader.accept(m_writer, false);
            } catch (Exception e) {
                throw new ClassNotFoundException(m_className, e);
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
        if (m_writer == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        final byte[] serializedAttribute = serialize(attribute);
        m_reader.accept(new AttributeClassAdapter(m_writer, serializedAttribute) {
                public void visit(final int access, final String name, final String superName,
                                  final String[] interfaces, final String sourceFile) {
                    visitAttribute(new CustomAttribute(serializedAttribute));
                    super.visit(access, name, superName, interfaces, sourceFile);
                }
            }, false);
    }

    /**
     * Inserts an attribute on field level.
     *
     * @param field     the field
     * @param attribute the attribute
     */
    public void insertFieldAttribute(final JField field, final Object attribute) {
        if (m_writer == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        final byte[] serializedAttribute = serialize(attribute);
        m_reader.accept(new AttributeClassAdapter(m_writer, serializedAttribute) {
                public void visitField(final int access, final String name, final String desc, final Object value,
                                       final Attribute attrs) {
                    if (name.equals(field.getSimpleName())) {
                        cv.visitField(access, name, desc, value, new CustomAttribute(serializedAttribute));
                    }
                    super.visitField(access, name, desc, value, attrs);
                }
            }, false);
    }

    /**
     * Inserts an attribute on method level.
     *
     * @param method    the method
     * @param attribute the attribute
     */
    public void insertMethodAttribute(final JMethod method, final Object attribute) {
        if (m_writer == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        final String[] methodParamTypes = new String[method.getParameters().length];
        JParameter[] parameters = method.getParameters();
         for (int i = 0; i < methodParamTypes.length; i++) {
            JParameter parameter = parameters[i];
            methodParamTypes[i] = parameter.getType().getQualifiedName();
//            methodParamTypes[i] = TypeConverter.convertTypeToJava(parameter.getType());
        }
        final byte[] serializedAttribute = serialize(attribute);
        m_reader.accept(new AttributeClassAdapter(m_writer, serializedAttribute) {
                public CodeVisitor visitMethod(final int access, final String name, final String desc,
                                               final String[] exceptions, final Attribute attrs) {
                    if (name.equals(method.getSimpleName())
                        && Arrays.equals(methodParamTypes, DescriptorUtil.getParameters(desc))) {
                        cv.visitMethod(access, name, desc, exceptions, new CustomAttribute(serializedAttribute));
                    }
                    return super.visitMethod(access, name, desc, exceptions, attrs);
                }
            }, false);
    }

    /**
     * Writes the enhanced class to file.
     *
     * @param destDir the destination directory
     */
    public void write(final String destDir) {
        try {
            String path = destDir + File.separator + m_classFileName;
            File file = new File(path);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                // directory does not exist create all directories in the path
                if (!parentFile.mkdirs()) {
                    throw new RuntimeException("could not create dir structure needed to write file " + path
                                               + " to disk");
                }
            }
            FileOutputStream os = new FileOutputStream(destDir + File.separator + m_classFileName);
            os.write(m_writer.toByteArray());
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
            throw new RuntimeException("could not find dependency for mixin implicit interface: " + innerClassName
                                       + " due to: " + er.toString());
        }
    }

    /**
     * Return the first interfaces implemented by a level in the class hierarchy (bottom top)
     *
     * @return nearest superclass (including itself) ' implemented interfaces starting from root
     */
    private String[] getNearestInterfacesInHierarchy(final Class root) {
        if (root == null) {
            return new String[] {  };
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
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
     */
    private static class AttributeClassAdapter extends ClassAdapter {
        /**
         * The serialized attribute.
         */
        protected byte[] m_serializedAttribute;

        /**
         * Creates a new adapter.
         *
         * @param cv                  the class visitor
         * @param serializedAttribute the serialized attribute
         */
        public AttributeClassAdapter(final ClassVisitor cv, final byte[] serializedAttribute) {
            super(cv);
            m_serializedAttribute = serializedAttribute;
        }

        public void visit(int i, String s, String s1, String[] strings, String s2) {
            super.visit(i, s, s1, strings, s2);
        }

        public void visitInnerClass(String s, String s1, String s2, int i) {
            super.visitInnerClass(s, s1, s2, i);
        }

        public void visitField(int i, String s, String s1, Object o, Attribute attribute) {
            super.visitField(i, s, s1, o, attribute);
        }

        public CodeVisitor visitMethod(int i, String s, String s1, String[] strings, Attribute attribute) {
            return super.visitMethod(i, s, s1, strings, attribute);
        }

        public void visitAttribute(Attribute attribute) {
            super.visitAttribute(attribute);
        }

        public void visitEnd() {
            super.visitEnd();
        }
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.bcel;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.xmlbeans.impl.jam.JField;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.codehaus.aspectwerkz.annotation.AnnotationC;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.codehaus.aspectwerkz.definition.DescriptorUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * Enhances classes with attributes.
 * <p/>
 * Implementation based on BCEL.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class BcelAttributeEnhancer implements AttributeEnhancer {
    /**
     * The BCEL java class.
     */
    private JavaClass m_javaClass = null;

    /**
     * The BCEL class gen.
     */
    private ClassGen m_classGen = null;

    /**
     * The BCEL constant pool gen.
     */
    private ConstantPoolGen m_constantPoolGen = null;

    /**
     * Compiled class class loader
     */
    private URLClassLoader m_loader = null;

    /**
     * Initializes the attribute enhancer. Must always be called before use.
     *
     * @param className the class name
     * @param classPath the class path
     * @param isInner is the class an inner class
     * @return true if the class was succefully loaded, false otherwise
     */
    public boolean initialize(final String className, final String classPath, final boolean isInner) {
        try {
            URL[] urls = new URL[] { new File(classPath).toURL() };
            m_loader = new URLClassLoader(urls);
            String classFileName = className.replace('.', '/') + ".class";
            if (isInner) {
                classFileName = AnnotationC.convertToJavaStyleInnerClassFileName(classFileName);
            }
            InputStream classAsStream = m_loader.getResourceAsStream(classFileName);
            if (classAsStream == null) {
                // TODO: this is an ugly bug fix due to JAM bug with inner class parsing
                int lastSlash = classFileName.lastIndexOf('/');
                classFileName = classFileName.substring(0, lastSlash) + '$'
                                + classFileName.substring(lastSlash + 1, classFileName.length());
                classAsStream = m_loader.getResourceAsStream(classFileName);

                //                System.err.println("WARNING: " + className + " can not be found on classpath");
                //                return false;
            }
            ClassParser classParser = new ClassParser(classAsStream, className);
            m_javaClass = classParser.parse();
            m_constantPoolGen = new ConstantPoolGen(m_javaClass.getConstantPool());
            m_classGen = new ClassGen(m_javaClass);
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
        if (m_classGen == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        byte[] serializedAttribute = serialize(attribute);
        Attribute attr = new Unknown(m_constantPoolGen.addUtf8("Custom"), serializedAttribute.length,
                                     serializedAttribute, m_constantPoolGen.getConstantPool());
        m_classGen.addAttribute(attr);
    }

    /**
     * Inserts an attribute on field level.
     *
     * @param field     the QDox java field
     * @param attribute the attribute
     */
    public void insertFieldAttribute(final JField field, final Object attribute) {
        if (m_classGen == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        byte[] serializedAttribute = serialize(attribute);
        Field[] classfileField = m_classGen.getFields();
        for (int i = 0; i < classfileField.length; i++) {
            if (classfileField[i].getName().equals(field.getSimpleName())) {
                FieldGen fieldGen = new FieldGen(classfileField[i], m_constantPoolGen);
                Attribute attr = new Unknown(m_constantPoolGen.addUtf8("Custom"), serializedAttribute.length,
                                             serializedAttribute, m_constantPoolGen.getConstantPool());
                fieldGen.addAttribute(attr);
                Field newField = fieldGen.getField();
                m_classGen.replaceField(classfileField[i], newField);
            }
        }
    }

    /**
     * Inserts an attribute on method level.
     *
     * @param method    the QDox java method
     * @param attribute the attribute
     */
    public void insertMethodAttribute(final JMethod method, final Object attribute) {
        if (m_classGen == null) {
            throw new IllegalStateException("annotation enhancer is not initialized");
        }
        byte[] serializedAttribute = serialize(attribute);
        JParameter[] parameters = method.getParameters();
        String[] methodParamTypes = new String[parameters.length];
        for (int i = 0; i < methodParamTypes.length; i++) {
            JParameter parameter = parameters[i];
            methodParamTypes[i] = parameter.getType().getQualifiedName();

            //            methodParamTypes[i] = TypeConverter.convertTypeToJava(parameter.getType());
        }
        Method[] classfileMethod = m_classGen.getMethods();
        for (int i = 0; i < classfileMethod.length; i++) {
            if (classfileMethod[i].getName().equals(method.getSimpleName())) {
                if (Arrays.equals(methodParamTypes, DescriptorUtil.getParameters(classfileMethod[i].getSignature()))) {
                    MethodGen methodGen = new MethodGen(classfileMethod[i], m_javaClass.getClassName(),
                                                        m_constantPoolGen);
                    Attribute attr = new Unknown(m_constantPoolGen.addUtf8("Custom"), serializedAttribute.length,
                                                 serializedAttribute, m_constantPoolGen.getConstantPool());
                    methodGen.addAttribute(attr);
                    Method newMethod = methodGen.getMethod();
                    m_classGen.replaceMethod(classfileMethod[i], newMethod);
                }
            }
        }
    }

    /**
     * Writes the enhanced class to file.
     *
     * @param destDir the destination directory
     */
    public void write(final String destDir) {
        try {
            m_classGen.setConstantPool(m_constantPoolGen);
            JavaClass klass = m_classGen.getJavaClass();
            String path = destDir + "/" + klass.getClassName().replace('.', '/') + ".class";
            File file = new File(path);
            File parentFile = file.getParentFile();
            boolean exists = parentFile.exists();
            if (!exists) {
                // directory does not exist create all directories in the path
                boolean success = parentFile.mkdirs();
                if (!success) {
                    throw new RuntimeException("could not create dir structure needed to write file " + path
                                               + " to disk");
                }
            }
            FileOutputStream fout = new FileOutputStream(path);
            DataOutputStream out = new DataOutputStream(fout);
            klass.dump(out);
            fout.close();
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
     * @return nearest superclass (including itself) ' implemented interfaces
     */
    public String[] getNearestInterfacesInHierarchy(final String innerClassName) {
        if (m_loader == null) {
            throw new IllegalStateException("attribute enhancer is not initialized");
        }
        try {
            Class innerClass = Class.forName(innerClassName, false, m_loader);
            return getNearestInterfacesInHierarchy(innerClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("could not load mixin for mixin implicit interface [" + innerClassName + "]: "
                                       + e.toString());
        } catch (NoClassDefFoundError er) {
            throw new RuntimeException("could not find dependency for mixin implicit interface [" + innerClassName
                                       + "]: " + er.toString());
        }
    }

    /**
     * Return the first interfaces implemented by a level in the class hierarchy (bottom top)
     *
     * @return nearest superclass (including itself) ' implemented interfaces starting from root
     */
    private String[] getNearestInterfacesInHierarchy(Class root) {
        if (root == null) {
            return new String[] {  };
        }
        Class[] implementedClasses = root.getInterfaces();
        String[] interfaces;
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
}

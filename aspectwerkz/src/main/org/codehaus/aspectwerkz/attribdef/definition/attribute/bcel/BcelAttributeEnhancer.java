/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute.bcel;

import java.util.Arrays;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.DataOutputStream;

import com.thoughtworks.qdox.model.JavaMethod;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AttributeEnhancer;
import org.codehaus.aspectwerkz.attribdef.definition.DescriptorUtil;
import org.codehaus.aspectwerkz.metadata.TypeConverter;

/**
 * Enhances aspect classes with attributes.
 * Implementation based on BCEL.
 *
 * Based on code from the Attrib4j project by Mark Pollack and Ted Neward (http://attrib4j.sourceforge.net/).
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
     * Initializes the attribute enhancer.
     * Must always be called before use.
     *
     * @param className the class name
     * @param classPath the class path
     * @return true if the class was succefully loaded, false otherwise
     */
    public boolean initialize(final String className, final String classPath) {
        try {
            URL[] urls = new URL[]{new File(classPath).toURL()};
            URLClassLoader loader = new URLClassLoader(urls);

            String classFileName = className.replace('.', '/') + ".class";
            InputStream classAsStream = loader.getResourceAsStream(classFileName);

            if (classAsStream == null) return false;
            ClassParser classParser = new ClassParser(classAsStream, className);
            m_javaClass = classParser.parse();

            m_constantPoolGen = new ConstantPoolGen(m_javaClass.getConstantPool());
            m_classGen = new ClassGen(m_javaClass);
        }
        catch (Exception e) {
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
        if (m_classGen == null) throw new IllegalStateException("attribute enhancer is not initialized");
        byte[] serializedAttribute = serialize(attribute);
        Attribute attr = new Unknown(
                m_constantPoolGen.addUtf8("Custom"),
                serializedAttribute.length,
                serializedAttribute,
                m_constantPoolGen.getConstantPool()
        );
        m_classGen.addAttribute(attr);
    }

    /**
     * Inserts an attribute on field level.
     *
     * @param field the field name
     * @param attribute the attribute
     */
    public void insertFieldAttribute(final String field, final Object attribute) {
        if (m_classGen == null) throw new IllegalStateException("attribute enhancer is not initialized");
        byte[] serializedAttribute = serialize(attribute);
        Field[] classfileField = m_classGen.getFields();
        for (int i = 0; i < classfileField.length; i++) {
            if (classfileField[i].getName().equals(field)) {
                FieldGen fieldGen = new FieldGen(classfileField[i], m_constantPoolGen);
                Attribute attr = new Unknown(
                        m_constantPoolGen.addUtf8("Custom"),
                        serializedAttribute.length,
                        serializedAttribute,
                        m_constantPoolGen.getConstantPool()
                );
                fieldGen.addAttribute(attr);
                Field newField = fieldGen.getField();
                m_classGen.replaceField(classfileField[i], newField);
            }
        }
    }

    /**
     * Inserts an attribute on method level.
     *
     * @param method the QDox java method
     * @param attribute the attribute
     */
    public void insertMethodAttribute(final JavaMethod method, final Object attribute) {
        if (m_classGen == null) throw new IllegalStateException("attribute enhancer is not initialized");

        byte[] serializedAttribute = serialize(attribute);

        String[] methodParamTypes = new String[method.getParameters().length];
        for (int i = 0; i < methodParamTypes.length; i++) {
            methodParamTypes[i] = TypeConverter.convertTypeToJava(method.getParameters()[i].getType());
        }
        System.out.println("method.getName() = " + method.getName());

        Method[] classfileMethod = m_classGen.getMethods();
        for (int i = 0; i < classfileMethod.length; i++) {
            if (classfileMethod[i].getName().equals(method.getName())) {
                if (Arrays.equals(
                        methodParamTypes,
                        DescriptorUtil.convertToJavaFormat(classfileMethod[i].getSignature()))
                ) {
                    MethodGen methodGen = new MethodGen(
                            classfileMethod[i],
                            m_javaClass.getClassName(),
                            m_constantPoolGen
                    );

                    Attribute attr = new Unknown(
                            m_constantPoolGen.addUtf8("Custom"),
                            serializedAttribute.length,
                            serializedAttribute,
                            m_constantPoolGen.getConstantPool()
                    );
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
                    throw new RuntimeException("could not create dir structure needed to write file " + path + " to disk");
                }
            }

            FileOutputStream fout = new FileOutputStream(path);
            DataOutputStream out = new DataOutputStream(fout);
            klass.dump(out);
            fout.close();
        }
        catch (IOException e) {
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
        }
        catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}

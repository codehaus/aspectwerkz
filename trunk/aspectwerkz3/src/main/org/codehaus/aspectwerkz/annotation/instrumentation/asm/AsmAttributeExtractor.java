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
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extracts attributes from the class bytecode using the ASM library.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AsmAttributeExtractor implements AttributeExtractor {
    /**
     * The class reader.
     */
    private ClassReader m_reader = null;

    /**
     * The class writer.
     */
    private ClassWriter m_writer = null;

    /**
     * Open the classfile and parse it in to the BCEL library.
     *
     * @param className the class name to load.
     * @param loader    the classloader to use to get the inputstream of the .class file.
     */
    public void initialize(final String className, final ClassLoader loader) {
        String classFileName = className.replace('.', '/') + ".class";
        try {
            InputStream classStream = loader.getResourceAsStream(classFileName);
            m_reader = new ClassReader(classStream);
            m_writer = new ClassWriter(false);
        } catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the class attributes.
     *
     * @return the class attributes
     */
    public Object[] getClassAttributes() {
        if (m_reader == null) {
            throw new IllegalStateException("attribute extractor is not initialized");
        }
        final List classAttributes = new ArrayList();
        m_reader.accept(new ClassAdapter(m_writer) {
                public void visitAttribute(final Attribute attribute) {
                    if (attribute instanceof CustomAttribute) {
                        CustomAttribute customAttribute = (CustomAttribute)attribute;
                        byte[] bytes = customAttribute.getBytes();
                        try {
                            classAttributes.add(new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject());
                        } catch (Exception e) {
                            //TODO AVAOSD jp index offlined deployed make it breaks
                            // since Unkonw attr not wrapped in Attr
                            // SKIP throw new WrappedRuntimeException(e);
                        }
                    }
                }
            }, new Attribute[] { new CustomAttribute(null) }, false);
        return classAttributes.toArray();
    }

    /**
     * Return all the attributes associated with a method that have a particular method signature.
     *
     * @param methodName       The name of the method.
     * @param methodParamTypes An array of parameter types as given by the reflection api.
     * @return the method attributes.
     */
    public Object[] getMethodAttributes(final String methodName, final String[] methodParamTypes) {
        if (m_reader == null) {
            throw new IllegalStateException("attribute extractor is not initialized");
        }
        final List methodAttributes = new ArrayList();
        m_reader.accept(new ClassAdapter(m_writer) {
                public CodeVisitor visitMethod(final int access, final String name, final String desc,
                                               final String[] exceptions, final Attribute attribute) {
                    if (name.equals(methodName) && Arrays.equals(methodParamTypes, DescriptorUtil.getParameters(desc))) {
                        if (attribute instanceof CustomAttribute) {
                            CustomAttribute customAttribute = (CustomAttribute)attribute;
                            byte[] bytes = customAttribute.getBytes();
                            try {
                                methodAttributes.add(new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject());
                            } catch (Exception e) {
                                throw new WrappedRuntimeException(e);
                            }
                        }
                    }
                    return null;
                }
            }, new Attribute[] { new CustomAttribute(null) }, false);
        return methodAttributes.toArray();
    }

    /**
     * Return all the attributes associated with a field.
     *
     * @param fieldName The name of the field.
     * @return the field attributes.
     */
    public Object[] getFieldAttributes(final String fieldName) {
        if (m_reader == null) {
            throw new IllegalStateException("attribute extractor is not initialized");
        }
        final List fieldAttributes = new ArrayList();
        m_reader.accept(new ClassAdapter(m_writer) {
                public void visitField(final int access, final String name, final String desc, final Object value,
                                       final Attribute attribute) {
                    if (name.equals(fieldName)) {
                        if (attribute instanceof CustomAttribute) {
                            CustomAttribute customAttribute = (CustomAttribute)attribute;
                            byte[] bytes = customAttribute.getBytes();
                            try {
                                fieldAttributes.add(new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject());
                            } catch (Exception e) {
                                throw new WrappedRuntimeException(e);
                            }
                        }
                    }
                }
            }, new Attribute[] { new CustomAttribute(null) }, false);
        return fieldAttributes.toArray();
    }
}

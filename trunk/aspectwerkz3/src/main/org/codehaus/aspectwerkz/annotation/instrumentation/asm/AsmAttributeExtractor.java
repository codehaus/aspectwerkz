/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.asm;

import org.codehaus.aspectwerkz.UnbrokenObjectInputStream;
import org.codehaus.aspectwerkz.util.Base64;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.definition.DescriptorUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.attrs.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.lang.reflect.Method;

/**
 * Extracts attributes from the class bytecode using the ASM library.
 * <p/>
 * Note: this class is parsing the bytecode at every single call. It does not cache anything
 * and should be used with caution.
 * <br/>For faster access, consider using ClassInfo.
 * <br/>We are using it when registering an aspect, to avoid any side effect that would lead to loading
 * a related class (f.e. outer class when aspect is inner class).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AsmAttributeExtractor /*implements AttributeExtractor*/ {
//
//    private static final String INIT_METHOD_NAME = "<init>";
//
//    /**
//     * The class reader.
//     */
//    private ClassReader m_reader = null;
//
//    private ClassLoader m_loader;///TODO weak ?
//
//    /**
//     * Open the classfile and parse it in to the ASM library.
//     *
//     * @param className the class name to load.
//     * @param loader    the classloader to use to get the inputstream of the .class file.
//     * @return true if correctly initialized
//     */
//    public boolean initialize(final String className, final ClassLoader loader) {
//        String classFileName = className.replace('.', '/') + ".class";
//        try {
//            InputStream classStream = loader.getResourceAsStream(classFileName);
//            if (classStream != null) {
//                m_reader = new ClassReader(classStream);
//                m_loader = loader;
//            } else {
//                return false;
//            }
//        } catch (IOException e) {
//            //TODO - is that good ?
//            throw new WrappedRuntimeException(e);
//        }
//        return true;
//    }
//
//    /**
//     * Returns the class attributes.
//     *
//     * @return the class attributes
//     */
//    public Object[] getClassAttributes() {
//        if (m_reader == null) {
//            throw new IllegalStateException("attribute extractor is not initialized");
//        }
//        final List classAttributes = new ArrayList();
//        m_reader.accept(
//                new AsmAnnotationHelper.ClassAnnotationExtractor(classAttributes, m_loader),
//                AsmAnnotationHelper.ANNOTATIONS_ATTRIBUTES,
//                true
//        );
//        return classAttributes.toArray();
//    }
//
//    public Object[] getMethodAttributes(final Method method) {
//        return getMethodAttributes(method.getName(), Type.getMethodDescriptor(method));
//    }
//
//    public Object[] getMethodAttributes(final String methodName, String[] types) {
//       throw new RuntimeException("TODO");
//    }
//
//    /**
//     * Return all the attributes associated with a method that have a particular method signature.
//     *
//     * @param methodName       The name of the method.
//     * @param desc An array of parameter types as given by the reflection api.
//     * @return the method attributes.
//     */
//    public Object[] getMethodAttributes(final String methodName, final String desc) {
//        if (m_reader == null) {
//            throw new IllegalStateException("attribute extractor is not initialized");
//        }
//        final List methodAttributes = new ArrayList();
//        m_reader.accept(
//                new AsmAnnotationHelper.MethodAnnotationExtractor(methodAttributes, methodName, desc, m_loader),
//                AsmAnnotationHelper.ANNOTATIONS_ATTRIBUTES,
//                true
//        );
//        return methodAttributes.toArray();
//    }
//
//    /**
//     * Return all the attributes associated with a constructor that have a particular method signature.
//     *
//     * @param constructorParamTypes An array of parameter types as given by the reflection api.
//     * @return the constructor attributes.
//     */
//    public Object[] getConstructorAttributes(final String[] constructorParamTypes) {
//        if (m_reader == null) {
//            throw new IllegalStateException("attribute extractor is not initialized");
//        }
//        return getMethodAttributes(INIT_METHOD_NAME, constructorParamTypes);
//    }
//
//    /**
//     * Return all the attributes associated with a field.
//     *
//     * @param fieldName The name of the field.
//     * @return the field attributes.
//     */
//    public Object[] getFieldAttributes(final String fieldName) {
//        if (m_reader == null) {
//            throw new IllegalStateException("attribute extractor is not initialized");
//        }
//        final List fieldAttributes = new ArrayList();
//        m_reader.accept(
//                new AsmAnnotationHelper.FieldAnnotationExtractor(fieldAttributes, fieldName, m_loader),
//                AsmAnnotationHelper.ANNOTATIONS_ATTRIBUTES,
//                true
//        );
//        return fieldAttributes.toArray();
//    }
//


}
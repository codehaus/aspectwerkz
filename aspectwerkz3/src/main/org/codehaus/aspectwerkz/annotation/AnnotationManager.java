/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.util.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parses and retrieves annotations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationManager {
    /**
     * The JavaDoc parser.
     */
    private final JavaDocBuilder m_parser = new JavaDocBuilder();

    /**
     * Map with the registered annotations mapped to their proxy classes.
     */
    private final Map m_registeredAnnotations = new HashMap();

    /**
     * Adds a source tree to the builder.
     *
     * @param srcDirs the source trees
     */
    public void addSourceTrees(final String[] srcDirs) {
        for (int i = 0; i < srcDirs.length; i++) {
            m_parser.addSourceTree(new File(srcDirs[i]));
        }
    }

    /**
     * Register an annotation together with its proxy implementation.
     *
     * @param proxyClass     the proxy class
     * @param annotationName the name of the annotation
     */
    public void registerAnnotationProxy(final Class proxyClass, final String annotationName) {
        m_registeredAnnotations.put(annotationName, proxyClass);
    }

    /**
     * Returns all classes.
     *
     * @return an array with all classes
     */
    public JavaClass[] getAllClasses() {
        Collection classes = m_parser.getClassLibrary().all();
        Collection javaClasses = new ArrayList();
        String className;
        for (Iterator it = classes.iterator(); it.hasNext();) {
            className = (String)it.next();
            if ("java.lang.Object".equals(className)) {
                continue;
            }
            JavaClass clazz = m_parser.getClassByName(className);
            javaClasses.add(clazz);
        }
        return (JavaClass[])javaClasses.toArray(new JavaClass[]{});
    }

    /**
     * Returns the annotations with a specific name for a specific class.
     *
     * @param name
     * @param clazz
     * @return an array with the annotations
     */
    public Annotation[] getAnnotations(final String name, final JavaClass clazz) {
        DocletTag[] tags = clazz.getTags();
        List annotations = new ArrayList();
        for (int i = 0; i < tags.length; i++) {
            DocletTag tag = tags[i];
            String tagName = tag.getName().trim();
            String value = Strings.removeFormattingCharacters(tag.getValue().trim());
            if (name.equals(tagName) && m_registeredAnnotations.containsKey(tagName)) {
                Class proxyClass = (Class)m_registeredAnnotations.get(tagName);
                Annotation annotation;
                try {
                    annotation = (Annotation)proxyClass.newInstance();
                } catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
                if (annotation.isTyped()) {
                    StringBuffer buf = new StringBuffer();
                    buf.append('@');
                    buf.append(name);
                    buf.append('(');
                    buf.append(value);
                    buf.append(')');
                    annotation.setValue(buf.toString());
                } else {
                    annotation.setValue(value);
                }
                annotation.setName(name);
                annotations.add(annotation);
            }
        }
        return (Annotation[])annotations.toArray(new Annotation[]{});
    }

    /**
     * Returns the annotations with a specific name for a specific method.
     *
     * @param name
     * @param method
     * @return an array with the annotations
     */
    public Annotation[] getAnnotations(final String name, final JavaMethod method) {
        DocletTag[] tags = method.getTags();
        List annotations = new ArrayList();
        for (int i = 0; i < tags.length; i++) {
            DocletTag tag = tags[i];
            String tagName = tag.getName().trim();
            String value = Strings.removeFormattingCharacters(tag.getValue().trim());
            if (name.equals(tagName) && m_registeredAnnotations.containsKey(tagName)) {
                Class proxyClass = (Class)m_registeredAnnotations.get(tagName);
                Annotation annotation;
                try {
                    annotation = (Annotation)proxyClass.newInstance();
                } catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
                System.out.println("value: " + value);
                System.out.println("name: " + annotation.getName());
                System.out.println("istyped: " + annotation.isTyped());
                if (annotation.isTyped()) {
                    StringBuffer buf = new StringBuffer();
                    buf.append('@');
                    buf.append(name);
                    buf.append('(');
                    buf.append(value);
                    buf.append(')');
                    annotation.setValue(buf.toString());
                } else {
                    annotation.setValue(value);
                }
                annotation.setName(name);
                annotations.add(annotation);
            }
        }
        return (Annotation[])annotations.toArray(new Annotation[]{});
    }

    /**
     * Returns the annotations with a specific name for a specific field.
     *
     * @param name
     * @param field
     * @return an array with the annotations
     */
    public Annotation[] getAnnotations(final String name, final JavaField field) {
        DocletTag[] tags = field.getTags();
        List annotations = new ArrayList();
        for (int i = 0; i < tags.length; i++) {
            DocletTag tag = tags[i];
            String tagName = tag.getName().trim();
            String value = Strings.removeFormattingCharacters(tag.getValue().trim());
            if (name.equals(tagName) && m_registeredAnnotations.containsKey(tagName)) {
                Class proxyClass = (Class)m_registeredAnnotations.get(tagName);
                Annotation annotation;
                try {
                    annotation = (Annotation)proxyClass.newInstance();
                } catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
                if (annotation.isTyped()) {
                    StringBuffer buf = new StringBuffer();
                    buf.append('@');
                    buf.append(name);
                    buf.append('(');
                    buf.append(value);
                    buf.append(')');
                    annotation.setValue(buf.toString());
                } else {
                    annotation.setValue(value);
                }
                annotation.setName(name);
                annotations.add(annotation);
            }
        }
        return (Annotation[])annotations.toArray(new Annotation[]{});
    }
}

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
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.annotation.expression.AnnotationVisitor;
import org.codehaus.aspectwerkz.annotation.expression.ast.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Parses and retrieves annotations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AnnotationManager {

    private static final String JAVA_LANG_OBJECT_CLASS_NAME = "java.lang.Object";

    /**
     * The JavaDoc parser.
     */
    private final JavaDocBuilder m_parser = new JavaDocBuilder();

    /**
     * Map with the registered annotations mapped to their interface implementation classes.
     */
    private final Map m_registeredAnnotations = new HashMap();

    /**
     * Constructs a new annotation manager and had the given ClassLoader to the
     * search path
     *
     * @param loader
     */
    public AnnotationManager(ClassLoader loader) {
        m_parser.getClassLibrary().addClassLoader(loader);
    }

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
     * Adds a source file.
     *
     * @param srcFile the source file
     */
    public void addSource(final String srcFile) {
        try {
            m_parser.addSource(new File(srcFile));
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
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
            className = (String) it.next();
            if (JAVA_LANG_OBJECT_CLASS_NAME.equals(className)) {
                continue;
            }
            JavaClass clazz = m_parser.getClassByName(className);
            javaClasses.add(clazz);
        }
        return (JavaClass[]) javaClasses.toArray(new JavaClass[]{});
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
            RawAnnotation rawAnnotation = getRawAnnotation(name, tag);
            if (rawAnnotation != null) {
                annotations.add(instantiateAnnotation(rawAnnotation));
            }
        }
        return (Annotation[]) annotations.toArray(new Annotation[]{});
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
            RawAnnotation rawAnnotation = getRawAnnotation(name, tag);
            if (rawAnnotation != null) {
                annotations.add(instantiateAnnotation(rawAnnotation));
            }
        }
        return (Annotation[]) annotations.toArray(new Annotation[]{});
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
            RawAnnotation rawAnnotation = getRawAnnotation(name, tag);
            if (rawAnnotation != null) {
                annotations.add(instantiateAnnotation(rawAnnotation));
            }
        }
        return (Annotation[]) annotations.toArray(new Annotation[]{});
    }


    /**
     * Instantiate the given annotation based on its name, and initialize it by passing the given value (may be parsed
     * or not, depends on type/untyped)
     *
     * @param rawAnnotation
     * @return
     */
    private Annotation instantiateAnnotation(final RawAnnotation rawAnnotation) {
        final Class proxyClass = (Class) m_registeredAnnotations.get(rawAnnotation.name);

        if (!proxyClass.isInterface()) {
            throw new RuntimeException("Annotation class is not defined as an interface for " + rawAnnotation.name
                + ". Use of AspectWerkz 1.x Annotation proxies is not anymore supported.");
        }

        try {
            InvocationHandler handler = new Java14AnnotationInvocationHander(
                    proxyClass, rawAnnotation.name, rawAnnotation.value
            );
            Object annotationProxy = Proxy.newProxyInstance(
                    proxyClass.getClassLoader(), new Class[]{Annotation.class, proxyClass}, handler
            );
            return (Annotation) annotationProxy;
        } catch (Throwable e) {
            throw new DefinitionException(
                    "Unable to parse annotation @" + rawAnnotation.name + '(' +
                    " " + rawAnnotation.value + ')', e
            );
        }
    }

    /**
     * Extract the raw information (name + unparsed value without optional parenthesis) from a Qdox doclet Note:
     * StringBuffer.append(null<string>) sucks and produce "null" string..
     * Note: when using untyped annotation, then the first space character(s) in the value part will be
     * resumed to only one space (untyped     type -> untyped type), due to QDox doclet handling.
     *
     * @param annotationName
     * @param tag
     * @return RawAnnotation or null if not found
     */
    private RawAnnotation getRawAnnotation(String annotationName, DocletTag tag) {
        String asIs = tag.getName() + " " + tag.getValue();
        asIs = asIs.trim();
        Strings.removeFormattingCharacters(asIs);

        // filter out if annotationName cannot be found
        if (!asIs.startsWith(annotationName)) {
            return null;
        }

        String name = null;
        String value = null;

        // try untyped split
        if (asIs.indexOf(' ') > 0) {
            name = asIs.substring(0, asIs.indexOf(' '));
        }
        if (annotationName.equals(name)) {
            // untyped
            value = asIs.substring(asIs.indexOf(' ') + 1, asIs.length());
            if (!value.startsWith("\"") && !value.endsWith("\"")) {
                //value = "\""+Strings.replaceSubString(value, "\"", "\\" + "\"")+"\"";
                //value = "\""+value+"\"";
            }
            if (value.startsWith("(") && value.endsWith(")")) {
                if (value.length() > 2) {
                    value = value.substring(1, value.length()-1);
                } else {
                    value = "";
                }
            }
        } else {
            // try typed split
            if (asIs.indexOf('(') > 0) {
                name = asIs.substring(0, asIs.indexOf('('));
            }
            if (annotationName.equals(name)) {
                value = asIs.substring(asIs.indexOf('(') + 1, asIs.length());
                if (value.endsWith(")")) {
                    if (value.length() > 1) {
                        value = value.substring(0, value.length() - 1);
                        if (value.startsWith("\"") && value.endsWith("\"")) {
//                            if (value.length() > 2) {
//                                value = value.substring(1, value.length() - 1);
//                            }
                        }
                    } else {
                        value = "";
                    }
                }
            } else if (annotationName.equals(asIs)) {
                value = "";
            }
        }

        // found one
        if (value != null) {
            RawAnnotation annotation = new RawAnnotation();
            annotation.name = annotationName;
            annotation.value = value;
            return annotation;
        } else {
            return null;
        }
    }

    /**
     * Raw info about an annotation: Do(foo) ==> Do + foo [unless untyped then ==> Do(foo) + null Do foo  ==> Do + foo
     * etc
     */
    private static class RawAnnotation implements Serializable {
        String name;
        String value;
    }
}
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private Annotation instantiateAnnotation(RawAnnotation rawAnnotation) {
        Class proxyClass = (Class) m_registeredAnnotations.get(rawAnnotation.name);
        Annotation annotation;
        try {
            annotation = (Annotation) proxyClass.newInstance();
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        annotation.setName(rawAnnotation.name);
        annotation.initialize(rawAnnotation.name, (rawAnnotation.value == null) ? "" : rawAnnotation.value);
        return annotation;
    }

    /**
     * Extrac the raw information (name + unparsed value without optional parenthesis) from a Qdox doclet Note:
     * StringBuffer.append(null<string>) sucks and produce "null" string..
     *
     * @param annotationName
     * @param tag
     * @return RawAnnotation or null if not found
     */
    private RawAnnotation getRawAnnotation(String annotationName, DocletTag tag) {
        String tagName = tag.getName().trim();

        // early filtering
        if (!tagName.startsWith(annotationName)) {
            return null;
        }

        // check first for untyped annotations
        if (m_registeredAnnotations.containsKey(annotationName)) {
            Class proxyClass = (Class) m_registeredAnnotations.get(annotationName);
            if (UntypedAnnotationProxy.class.isAssignableFrom(proxyClass)) {
                // we do have an untyped annotation
                // does it match
                if (tagName.equals(annotationName)) {
                    RawAnnotation rawAnnotation = new RawAnnotation();
                    rawAnnotation.name = annotationName;
                    rawAnnotation.value = Strings.removeFormattingCharacters(tag.getValue().trim());
                    return rawAnnotation;
                } else {
                    // complains - @Untyped(hello) nor @Untyped("hello") are allowed.
                    throw new RuntimeException("Cannot instantiate UntypedAnnotation of name: " + tagName);
                }
            }
        }

        // handles case where there is no space between the @Annotation and the annotation values (x=1)
        // In such a case Qdox, see one single annotation whose name is the first part up to the first space
        // character
        String rawValue = null;
        if (tagName.indexOf('(') > 0) {//@Void(), @Do(x = 3), @Do(x=3)
            rawValue = tagName.substring(tagName.indexOf('(') + 1).trim();//), x, x=3)
            tagName = tagName.substring(0, tagName.indexOf('(')).trim();//Void, Do
            if (rawValue.endsWith(")")) {
                if (rawValue.length() > 1) {
                    rawValue = rawValue.substring(0, rawValue.length() - 1);
                } else {
                    rawValue = null;
                }
            }
        }
        String rawEndValue = Strings.removeFormattingCharacters(tag.getValue().trim());
        if (rawEndValue.endsWith(")")) {
            if (rawEndValue.length() > 1) {
                rawEndValue = rawEndValue.substring(0, rawEndValue.length() - 1);
            } else {
                rawEndValue = null;
            }
        }
        StringBuffer raw = new StringBuffer();
        if (rawValue != null) {
            raw.append(rawValue);
        }
        if (rawEndValue != null) {
            raw.append(rawEndValue);
        }

        // exact filtering
        if (tagName.equals(annotationName) && m_registeredAnnotations.containsKey(tagName)) {
            RawAnnotation rawAnnotation = new RawAnnotation();
            rawAnnotation.name = annotationName;
            rawAnnotation.value = raw.toString();
            return rawAnnotation;
        }

        // no match
        return null;
    }

    /**
     * Raw info about an annotation: Do(foo) ==> Do + foo [unless untyped then ==> Do(foo) + null Do foo  ==> Do + foo
     * etc
     */
    private static class RawAnnotation {
        String name;
        String value;
    }
}
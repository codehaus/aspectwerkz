/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.apache.xmlbeans.impl.jam.*;

import java.io.File;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationC {

    private static final String ANNOTATION_ASPECT = "Aspect";
    private static final String ANNOTATION_AROUND = "Around";
    private static final String ANNOTATION_BEFORE = "Before";
    private static final String ANNOTATION_AFTER = "After";
    private static final String ANNOTATION_EXPRESSION = "Expression";
    private static final String ANNOTATION_IMPLEMENTS = "Implements";
    private static final String ANNOTATION_INTRODUCE = "Introduce";

    public static void main(String[] args) throws Exception {
        JamServiceFactory factory = JamServiceFactory.getInstance();
        JamServiceParams params = factory.createServiceParams();
        registerSystemAnnotations(params);
        registerUserDefinedAnnotations(params);
        includeSourceTree(params);
        JClass[] classes = factory.createService(params).getAllClasses();
        for (int i = 0; i < classes.length; i++) {
            JClass clazz = classes[i];
            handleClassAnnotations(clazz);
            JMethod[] methods = clazz.getDeclaredMethods();
            for (int j = 0; j < methods.length; j++) {
                handleMethodAnnotations(methods[j]);
            }
            JField[] fields = clazz.getDeclaredFields();
            for (int j = 0; j < fields.length; j++) {
                handleFieldAnnotations(fields[j]);
            }
            JClass[] innerClasses = clazz.getClasses();
            for (int j = 0; j < innerClasses.length; j++) {
                handleInnerClassAnnotations(innerClasses[j]);
            }
        }
    }

    private static void includeSourceTree(final JamServiceParams params) {
//        params.includeSourceFile(new File("c:/src/aspectwerkz3/src/samples/examples/introduction/IntroductionAspect.java"));
        params.includeSourceFile(new File("c:/src/aspectwerkz3/src/samples/examples/caching/CachingAspect.java"));
    }

    private static void handleClassAnnotations(JClass clazz) {
        JAnnotation ann = clazz.getAnnotation(ANNOTATION_ASPECT);
        if (ann != null) {
            AspectProxy aspectProxy = (AspectProxy)ann.getProxy();
            // handle proxy
        }
    }

    private static void handleMethodAnnotations(JMethod method) {
        JAnnotation aroundAnnotation = method.getAnnotation(ANNOTATION_AROUND);
        if (aroundAnnotation != null) {
            AroundProxy aroundProxy = (AroundProxy)aroundAnnotation.getProxy();
            if (aroundProxy != null) {
                // handle proxy
            }
        }
        JAnnotation beforeAnnotation = method.getAnnotation(ANNOTATION_BEFORE);
        if (beforeAnnotation != null) {
            BeforeProxy beforeProxy = (BeforeProxy)beforeAnnotation.getProxy();
            if (beforeProxy != null) {
                // handle proxy
            }
        }
        JAnnotation afterAnnotation = method.getAnnotation(ANNOTATION_AFTER);
        if (afterAnnotation != null) {
            AfterProxy afterProxy = (AfterProxy)afterAnnotation.getProxy();
            if (afterProxy != null) {
                // handle proxy
            }
        }
    }

    private static void handleFieldAnnotations(JField field) {
        JAnnotation expressionAnnotation = field.getAnnotation(ANNOTATION_EXPRESSION);
        if (expressionAnnotation != null) {
            ExpressionProxy expressionProxy = (ExpressionProxy)expressionAnnotation.getProxy();
            if (expressionProxy != null) {
                // handle proxy
            }
        }
        JAnnotation implementsAnnotation = field.getAnnotation(ANNOTATION_IMPLEMENTS);
        if (implementsAnnotation != null) {
            ImplementsProxy implementsProxy = (ImplementsProxy)implementsAnnotation.getProxy();
            if (implementsProxy != null) {
                // handle proxy
            }
        }
    }

    private static void handleInnerClassAnnotations(JClass innerClass) {
        JAnnotation introduceAnnotation = innerClass.getAnnotation(ANNOTATION_INTRODUCE);
        if (introduceAnnotation != null) {
            IntroduceProxy introduceProxy = (IntroduceProxy)introduceAnnotation.getProxy();
            if (introduceProxy != null) {
            }
        }
    }

    private static void registerSystemAnnotations(final JamServiceParams params) {
        params.registerAnnotationProxy(AspectProxy.class, ANNOTATION_ASPECT);
        params.registerAnnotationProxy(AroundProxy.class, ANNOTATION_AROUND);
        params.registerAnnotationProxy(BeforeProxy.class, ANNOTATION_BEFORE);
        params.registerAnnotationProxy(AfterProxy.class, ANNOTATION_AFTER);
        params.registerAnnotationProxy(ExpressionProxy.class, ANNOTATION_EXPRESSION);
        params.registerAnnotationProxy(ImplementsProxy.class, ANNOTATION_IMPLEMENTS);
        params.registerAnnotationProxy(IntroduceProxy.class, ANNOTATION_INTRODUCE);
    }

    private static void registerUserDefinedAnnotations(final JamServiceParams params) {
        // parse XML and register all use def. annotations
    }
}

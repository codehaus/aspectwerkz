/************1**************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionParserHelper;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.transform.ReflectHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Extracts the aspects annotations from the class files and creates a meta-data representation of them.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AspectAnnotationParser {
    /**
     * Parse the attributes and create and return a meta-data representation of them.
     * 
     * @param klass the class to extract attributes from
     * @param aspectDef the aspect definition
     * @param definition the aspectwerkz definition
     */
    public void parse(final Class klass, final AspectDefinition aspectDef, final SystemDefinition definition) {
        if (klass == null) {
            throw new IllegalArgumentException("class to parse can not be null");
        }
        AspectAnnotationProxy aspectAnnotation = (AspectAnnotationProxy) Annotations.getAnnotation(
            AnnotationC.ANNOTATION_ASPECT,
            klass);
        if (aspectAnnotation == null) {
            // fall back on using the class name as aspect name and let the deployment model be
            // perJVM
            aspectAnnotation = new AspectAnnotationProxy();
            aspectAnnotation.setName(klass.getName());
        }

        // attribute settings override the xml settings
        aspectDef.setDeploymentModel(aspectAnnotation.deploymentModel());
        String className = klass.getName();
        String aspectName = klass.getName(); // currently the same as the classname
        parseFieldAttributes(klass, aspectDef);
        parseMethodAttributes(klass, className, aspectName, aspectDef);
        parseClassAttributes(klass, aspectDef);
    }

    /**
     * Parses the field attributes and creates a meta-data representation of them.
     * 
     * @param klass the class to extract attributes from
     * @param aspectDef the aspect definition
     */
    private void parseFieldAttributes(final Class klass, AspectDefinition aspectDef) {
        if (aspectDef == null) {
            throw new IllegalArgumentException("aspect definition can not be null");
        }
        if (klass == null) {
            return;
        }
        Field[] fieldList = klass.getDeclaredFields();
        for (int i = 0; i < fieldList.length; i++) {
            Field field = fieldList[i];
            List expressionAnnotations = Annotations.getAnnotations(AnnotationC.ANNOTATION_EXPRESSION, field);
            for (Iterator iterator = expressionAnnotations.iterator(); iterator.hasNext();) {
                ExpressionAnnotationProxy annotation = (ExpressionAnnotationProxy) iterator.next();
                if (annotation != null) {
                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(
                        field.getName(),
                        annotation.expression(),
                        aspectDef);
                }
            }
            List implementsAnnotations = Annotations.getAnnotations(AnnotationC.ANNOTATION_IMPLEMENTS, field);
            for (Iterator iterator = implementsAnnotations.iterator(); iterator.hasNext();) {
                ImplementsAnnotationProxy annotation = (ImplementsAnnotationProxy) iterator.next();
                if (annotation != null) {
                    DefinitionParserHelper.createAndAddInterfaceIntroductionDefToAspectDef(
                        annotation.expression(),
                        field.getName(),
                        field.getType().getName(),
                        aspectDef);
                }
            }
        }

        // recursive call, next iteration based on super class
        parseFieldAttributes(klass.getSuperclass(), aspectDef);
    }

    /**
     * Parses the method attributes and creates a meta-data representation of them.
     * 
     * @param klass the class
     * @param aspectClassName the aspect class name
     * @param aspectName the aspect name
     * @param aspectDef the aspect definition
     */
    private void parseMethodAttributes(
        final Class klass,
        final String aspectClassName,
        final String aspectName,
        final AspectDefinition aspectDef) {

        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        if (aspectClassName == null) {
            throw new IllegalArgumentException("aspect class name can not be null");
        }
        if (aspectName == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }
        if (aspectDef == null) {
            throw new IllegalArgumentException("aspect definition can not be null");
        }
        
        List methodList = ReflectHelper.createSortedMethodList(klass);

        // iterate first on all method to lookup @Expression Pointcut annotations so that they can be resolved
        int methodIndex = 0;
        for (Iterator it = methodList.iterator(); it.hasNext(); methodIndex++) {
            Method method = (Method) it.next();

            // Pointcut with signature
            List expressionAnnotations = Annotations.getAnnotations(AnnotationC.ANNOTATION_EXPRESSION, method);
            for (Iterator iterator = expressionAnnotations.iterator(); iterator.hasNext();) {
                ExpressionAnnotationProxy annotation = (ExpressionAnnotationProxy) iterator.next();
                if (annotation != null) {

                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(AspectAnnotationParser
                            .getMethodPointcutCallSignature(method, annotation), annotation.expression(), aspectDef);
                }
            }
        }

        // iterate on other annotations
        methodIndex = 0;
        for (Iterator it = methodList.iterator(); it.hasNext(); methodIndex++) {
            Method method = (Method) it.next();

            // create the advice name out of the class and method name, <classname>.<methodname>
            List aroundAnnotations = Annotations.getAnnotations(AnnotationC.ANNOTATION_AROUND, method);
            for (Iterator iterator = aroundAnnotations.iterator(); iterator.hasNext();) {
                AroundAnnotationProxy aroundAnnotation = (AroundAnnotationProxy) iterator.next();
                if (aroundAnnotation != null) {
                    DefinitionParserHelper.createAndAddAroundAdviceDefToAspectDef(
                        aroundAnnotation.pointcut(),
                        AspectAnnotationParser.getMethodPointcutCallSignature(method, aroundAnnotation),
                        aspectName,
                        aspectClassName,
                        method,
                        methodIndex,
                        aspectDef);
                }
            }
            List beforeAnnotations = Annotations.getAnnotations(AnnotationC.ANNOTATION_BEFORE, method);
            for (Iterator iterator = beforeAnnotations.iterator(); iterator.hasNext();) {
                BeforeAnnotationProxy beforeAnnotation = (BeforeAnnotationProxy) iterator.next();
                if (beforeAnnotation != null) {
                    DefinitionParserHelper.createAndAddBeforeAdviceDefToAspectDef(
                        beforeAnnotation.pointcut(),
                        AspectAnnotationParser.getMethodPointcutCallSignature(method, beforeAnnotation),
                        aspectName,
                        aspectClassName,
                        method,
                        methodIndex,
                        aspectDef);
                }
            }
            List afterAnnotations = Annotations.getAnnotations(AnnotationC.ANNOTATION_AFTER, method);
            for (Iterator iterator = afterAnnotations.iterator(); iterator.hasNext();) {
                AfterAnnotationProxy afterAnnotation = (AfterAnnotationProxy) iterator.next();
                if (afterAnnotation != null) {
                    DefinitionParserHelper.createAndAddAfterAdviceDefToAspectDef(
                        afterAnnotation.pointcut(),
                        AspectAnnotationParser.getMethodPointcutCallSignature(method, afterAnnotation),
                        aspectName,
                        aspectClassName,
                        method,
                        methodIndex,
                        aspectDef);
                }
            }
        }
    }

    /**
     * Looks for "@Introduce IntroduceAttribute" defined at aspect inner class level
     * 
     * @param klass of aspect
     * @param aspectDef
     */
    private void parseClassAttributes(final Class klass, AspectDefinition aspectDef) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        List annotations = Annotations.getAnnotations(AnnotationC.ANNOTATION_INTRODUCE, klass);
        for (Iterator iterator = annotations.iterator(); iterator.hasNext();) {
            IntroduceAnnotationProxy annotation = (IntroduceAnnotationProxy) iterator.next();
            if (annotation != null) {
                Class mixin;
                try {
                    mixin = klass.getClassLoader().loadClass(annotation.innerClassName());
                } catch (ClassNotFoundException e) {
                    throw new WrappedRuntimeException(e);
                }
                DefinitionParserHelper.createAndAddIntroductionDefToAspectDef(
                    mixin,
                    annotation.expression(),
                    annotation.deploymentModel(),
                    aspectDef);
            }
        }
    }

    /**
     * Returns the call signature of a Pointcut or advice with signature methodName(paramType paramName, ...) [we ignore
     * the return type] If there is no parameters, the call signature is not "name()" but just "name"
     * 
     * @param method
     * @return annotationProxy that contains the ordered map of call parameters
     */
    private static String getMethodPointcutCallSignature(Method method, ParameterizedAnnotationProxy annotationProxy) {
        StringBuffer buffer = new StringBuffer(method.getName());
        if (annotationProxy.getArgumentNames().size() > 0) {
            buffer.append("(");
            for (Iterator it = annotationProxy.getArgumentNames().iterator(); it.hasNext();) {
                String parameter = (String) it.next();
                buffer.append(annotationProxy.getArgumentType(parameter));
                buffer.append(" ").append(parameter);
                if (it.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        }
        return buffer.toString();
    }

}
/************1**************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionParserHelper;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Extracts the aspects annotations from the class files and creates a meta-data representation of them.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AspectAnnotationParser implements AnnotationParser {
    /**
     * Parse the attributes and create and return a meta-data representation of them.
     *
     * @param klass      the class to extract attributes from
     * @param aspectDef  the aspect definition
     * @param definition the aspectwerkz definition
     */
    public void parse(final Class klass, final AspectDefinition aspectDef, final SystemDefinition definition) {
        if (klass == null) {
            throw new IllegalArgumentException("class to parse can not be null");
        }
        AspectAnnotationProxy aspectAnnotation = (AspectAnnotationProxy)Annotations.getAnnotation(
                AnnotationC.ANNOTATION_ASPECT, klass
        );
        if (aspectAnnotation == null) {
            // fall back on using the class name as aspect name and let the deployment model be perJVM
            aspectAnnotation = new AspectAnnotationProxy();
            aspectAnnotation.setname(klass.getName());
            aspectAnnotation.setvalue("perJVM");
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
     * @param klass     the class to extract attributes from
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

        // parse the pointcuts
        for (int i = 0; i < fieldList.length; i++) {
            Field field = fieldList[i];
            ExpressionAnnotationProxy expressionAnnotation = (ExpressionAnnotationProxy)Annotations.getAnnotation(
                    AnnotationC.ANNOTATION_EXPRESSION, field
            );
            if (expressionAnnotation != null) {
                DefinitionParserHelper.createAndAddPointcutDefToAspectDef(
                        field.getName(),
                        expressionAnnotation.expression(), aspectDef
                );
            }
            IntroduceAnnotationProxy introduceAnnotation = (IntroduceAnnotationProxy)Annotations.getAnnotation(
                    AnnotationC.ANNOTATION_INTRODUCE, field
            );
            if (introduceAnnotation != null) {
                DefinitionParserHelper.createAndAddInterfaceIntroductionDefToAspectDef(
                        introduceAnnotation.expression(),
                        field.getName(),
                        field.getType().getName(),
                        aspectDef
                );
            }
        }

        // recursive call, next iteration based on super class
        parseFieldAttributes(klass.getSuperclass(), aspectDef);
    }

    /**
     * Parses the method attributes and creates a meta-data representation of them.
     *
     * @param klass           the class
     * @param aspectClassName the aspect class name
     * @param aspectName      the aspect name
     * @param aspectDef       the aspect definition
     */
    private void parseMethodAttributes(
            final Class klass, final String aspectClassName, final String aspectName,
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
        List methodList = TransformationUtil.createSortedMethodList(klass);

        // parse the advices and introductions
        int methodIndex = 0;
        for (Iterator it = methodList.iterator(); it.hasNext(); methodIndex++) {
            Method method = (Method)it.next();

            // create the advice name out of the class and method name, <classname>.<methodname>
            String adviceName = aspectClassName + '.' + method.getName();
            AroundAnnotationProxy aroundAnnotation = (AroundAnnotationProxy)Annotations.getAnnotation(
                    AnnotationC.ANNOTATION_AROUND, method
            );
            if (aroundAnnotation != null) {
                DefinitionParserHelper.createAndAddAroundAdviceDefToAspectDef(
                        aroundAnnotation.pointcut(), adviceName, aspectName,
                        aspectClassName, method, methodIndex,
                        aspectDef
                );
            }
            BeforeAnnotationProxy beforeAnnotation = (BeforeAnnotationProxy)Annotations.getAnnotation(
                    AnnotationC.ANNOTATION_BEFORE, method
            );
            if (beforeAnnotation != null) {
                DefinitionParserHelper.createAndAddBeforeAdviceDefToAspectDef(
                        beforeAnnotation.pointcut(), adviceName, aspectName,
                        aspectClassName, method, methodIndex,
                        aspectDef
                );
            }
            AfterAnnotationProxy afterAnnotation = (AfterAnnotationProxy)Annotations.getAnnotation(
                    AnnotationC.ANNOTATION_AFTER, method
            );

            if (afterAnnotation != null) {
                DefinitionParserHelper.createAndAddAfterAdviceDefToAspectDef(
                        afterAnnotation.pointcut(), adviceName, aspectName,
                        aspectClassName, method, methodIndex,
                        aspectDef
                );
            }
        }
    }

    /**
     * Looks for @Introduce IntroduceAttribute defined at aspect inner class level
     *
     * @TODO: IMPLEMENT DAMMIT
     *
     * @param klass     of aspect
     * @param aspectDef
     */
    private void parseClassAttributes(final Class klass, AspectDefinition aspectDef) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        IntroduceAnnotationProxy introduceAnnotation = (IntroduceAnnotationProxy)Annotations.getAnnotation(
                AnnotationC.ANNOTATION_INTRODUCE, klass
        );
//        Class mixin;
//        try {
//            mixin = klass.getClassLoader().loadClass(introduceAnnotation.());
//        } catch (ClassNotFoundException e) {
//            throw new WrappedRuntimeException(e);
//        }
//        DefinitionParserHelper.createAndAddIntroductionDefToAspectDef(
//                mixin, introduceAttr.getExpression(),
//                introduceAttr.getDeploymentModel(),
//                aspectDef
//        );
    }
}

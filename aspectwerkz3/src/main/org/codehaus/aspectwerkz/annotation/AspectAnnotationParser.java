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
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.DeploymentScope;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAnnotations;
import org.codehaus.aspectwerkz.transform.inlining.spi.AspectModel;
import org.codehaus.aspectwerkz.transform.inlining.spi.AspectModelManager;

import java.util.Iterator;
import java.util.List;

/**
 * Extracts the aspects annotations from the class files and creates a meta-data representation of them.
 * <br/>
 * Note: we are not using reflection to loop over fields, etc, so that we do not trigger nested loading, which could be
 * potential target classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AspectAnnotationParser {

    /**
     * Singleton is enough.
     */
    private final static AspectAnnotationParser INSTANCE = new AspectAnnotationParser();

    /**
     * Private constructor to enforce singleton
     */
    private AspectAnnotationParser() {
    }

    /**
     * Parse the attributes and create and return a meta-data representation of them.
     *
     * @param classInfo the class to extract attributes from
     * @param aspectDef the aspect definition
     * @param loader
     */
    public static void parse(final ClassInfo classInfo, final AspectDefinition aspectDef, final ClassLoader loader) {
        INSTANCE.doParse(classInfo, aspectDef, loader);

        // load the different aspect model and let them define their aspects
        for (Iterator it = AspectModelManager.getModels().iterator(); it.hasNext();) {
            ((AspectModel) it.next()).defineAspect(classInfo, aspectDef, loader);
        }
    }

    /**
     * Parse the attributes and create and return a meta-data representation of them.
     *
     * @param classInfo the class to extract attributes from
     * @param aspectDef the aspect definition
     * @param loader
     */
    private void doParse(final ClassInfo classInfo, final AspectDefinition aspectDef, final ClassLoader loader) {
        if (classInfo == null) {
            throw new IllegalArgumentException("class to parse can not be null");
        }

        AspectAnnotationProxy aspectAnnotation = (AspectAnnotationProxy) AsmAnnotations.getAnnotation(
                AnnotationC.ANNOTATION_ASPECT,
                classInfo
        );
        if (aspectAnnotation == null) {
            // fall back on using the class name as aspect name and let the deployment model be
            // perJVM
            aspectAnnotation = new AspectAnnotationProxy();
            aspectAnnotation.setAspectName(classInfo.getName());
        }

        // attribute settings override the xml settings
        aspectDef.setDeploymentModel(aspectAnnotation.deploymentModel());
        String className = classInfo.getName();
        String aspectName = aspectAnnotation.aspectName();
        parseFieldAttributes(classInfo, aspectDef);
        parseMethodAttributes(classInfo, className, aspectName, aspectDef);
        parseClassAttributes(classInfo, aspectDef, loader);
    }

    /**
     * Parses the field attributes and creates a meta-data representation of them.
     *
     * @param classInfo the class to extract attributes from
     * @param aspectDef the aspect definition
     */
    private void parseFieldAttributes(final ClassInfo classInfo, final AspectDefinition aspectDef) {
        if (aspectDef == null) {
            throw new IllegalArgumentException("aspect definition can not be null");
        }
        if (classInfo == null) {
            return;
        }

        FieldInfo[] fieldList = classInfo.getFields();
        for (int i = 0; i < fieldList.length; i++) {
            FieldInfo field = fieldList[i];
            for (Iterator iterator = field.getAnnotations().iterator(); iterator.hasNext();) {
                AnnotationInfo annotationInfo = (AnnotationInfo) iterator.next();
                if (annotationInfo.getAnnotation() == null) {
                    continue;
                }
                if (AnnotationC.ANNOTATION_EXPRESSION.equals(annotationInfo.getName())) {
                    if (field.getType().getName().equals(DeploymentScope.class.getName())) {
                        DefinitionParserHelper.createAndAddDeploymentScopeDef(
                                field.getName(),
                                ((ExpressionAnnotationProxy) annotationInfo.getAnnotation()).expression(),
                                aspectDef.getSystemDefinition()
                        );
                    } else {
                        DefinitionParserHelper.createAndAddPointcutDefToAspectDef(
                                field.getName(),
                                ((ExpressionAnnotationProxy) annotationInfo.getAnnotation()).expression(),
                                aspectDef
                        );
                    }
                } else if (AnnotationC.ANNOTATION_IMPLEMENTS.equals(annotationInfo.getName())) {
                    DefinitionParserHelper.createAndAddInterfaceIntroductionDefToAspectDef(
                            ((ImplementsAnnotationProxy) annotationInfo.getAnnotation()).expression(),
                            field.getName(),
                            field.getType().getName(),
                            aspectDef
                    );
                }
            }
        }

        // recursive call, next iteration based on super class
        parseFieldAttributes(classInfo.getSuperclass(), aspectDef);
    }

    /**
     * Parses the method attributes and creates a meta-data representation of them.
     *
     * @param classInfo       the class
     * @param aspectClassName the aspect class name
     * @param aspectName      the aspect name
     * @param aspectDef       the aspect definition
     */
    private void parseMethodAttributes(final ClassInfo classInfo,
                                       final String aspectClassName,
                                       final String aspectName,
                                       final AspectDefinition aspectDef) {
        if (classInfo == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        if (aspectClassName == null) {
            throw new IllegalArgumentException("aspect class name can not be null");
        }
        if (aspectName == null) {
            throw new IllegalArgumentException("aspect name can not be null " + aspectClassName);
        }
        if (aspectDef == null) {
            throw new IllegalArgumentException("aspect definition can not be null");
        }
        // get complete method list (includes inherited ones)
        List methodList = ClassInfoHelper.createMethodList(classInfo);

        // iterate first on all method to lookup @Expression Pointcut annotations so that they can be resolved
        parsePointcutAttributes(methodList, aspectDef);

        // iterate on the advice annotations
        for (Iterator it = methodList.iterator(); it.hasNext();) {
            MethodInfo method = (MethodInfo) it.next();
            try {
                // create the advice name out of the class and method name, <classname>.<methodname>
                parseAroundAttributes(method, aspectName, aspectClassName, aspectDef);
                parseBeforeAttributes(method, aspectName, aspectClassName, aspectDef);
                parseAfterAttributes(method, aspectName, aspectClassName, aspectDef);
            } catch (DefinitionException e) {
                System.err.println("WARNING: unable to register advice: " + e.getMessage());
                // TODO AV - better handling of reg issue (f.e. skip the whole aspect, in DocumentParser, based on DefinitionE
            }
        }
    }

    /**
     * Parses the method pointcut attributes.
     *
     * @param methodList
     * @param aspectDef
     */
    private void parsePointcutAttributes(final List methodList, final AspectDefinition aspectDef) {
        for (Iterator it = methodList.iterator(); it.hasNext();) {
            MethodInfo method = (MethodInfo) it.next();

            // Pointcut with signature
            List expressionAnnotations = AsmAnnotations.getAnnotations(AnnotationC.ANNOTATION_EXPRESSION, method);
            for (Iterator iterator = expressionAnnotations.iterator(); iterator.hasNext();) {
                ExpressionAnnotationProxy annotation = (ExpressionAnnotationProxy) iterator.next();
                if (annotation != null) {

                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(
                            AspectAnnotationParser
                            .getMethodPointcutCallSignature(method.getName(), annotation),
                            annotation.expression(), aspectDef
                    );
                }
            }
        }
    }

    /**
     * Parses the around attributes.
     *
     * @param method
     * @param aspectName
     * @param aspectClassName
     * @param aspectDef
     */
    private void parseAroundAttributes(final MethodInfo method,
                                       final String aspectName,
                                       final String aspectClassName,
                                       final AspectDefinition aspectDef) {
        List aroundAnnotations = AsmAnnotations.getAnnotations(AnnotationC.ANNOTATION_AROUND, method);
        for (Iterator iterator = aroundAnnotations.iterator(); iterator.hasNext();) {
            AroundAnnotationProxy aroundAnnotation = (AroundAnnotationProxy) iterator.next();
            if (aroundAnnotation != null) {
                final String expression = aroundAnnotation.pointcut();
                final String adviceName = AspectAnnotationParser.getMethodPointcutCallSignature(
                        method.getName(), aroundAnnotation
                );
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        adviceName,
                        aroundAnnotation.getType(),
                        expression,
                        null,
                        aspectName,
                        aspectClassName,
                        method,
                        aspectDef
                );
                aspectDef.addAroundAdviceDefinition(adviceDef);
            }
        }
    }

    /**
     * Parses the before attributes.
     *
     * @param method
     * @param aspectName
     * @param aspectClassName
     * @param aspectDef
     */
    private void parseBeforeAttributes(final MethodInfo method,
                                       final String aspectName,
                                       final String aspectClassName,
                                       final AspectDefinition aspectDef) {
        List beforeAnnotations = AsmAnnotations.getAnnotations(AnnotationC.ANNOTATION_BEFORE, method);
        for (Iterator iterator = beforeAnnotations.iterator(); iterator.hasNext();) {
            BeforeAnnotationProxy beforeAnnotation = (BeforeAnnotationProxy) iterator.next();
            if (beforeAnnotation != null) {
                final String expression = beforeAnnotation.pointcut();
                final String adviceName = AspectAnnotationParser.getMethodPointcutCallSignature(
                        method.getName(), beforeAnnotation
                );
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        adviceName,
                        beforeAnnotation.getType(),
                        expression,
                        null,
                        aspectName,
                        aspectClassName,
                        method,
                        aspectDef
                );
                aspectDef.addBeforeAdviceDefinition(adviceDef);
            }
        }
    }

    /**
     * Parses the after attributes.
     *
     * @param method
     * @param aspectName
     * @param aspectClassName
     * @param aspectDef
     */
    private void parseAfterAttributes(final MethodInfo method,
                                      final String aspectName,
                                      final String aspectClassName,
                                      final AspectDefinition aspectDef) {
        List afterAnnotations = AsmAnnotations.getAnnotations(AnnotationC.ANNOTATION_AFTER, method);
        for (Iterator iterator = afterAnnotations.iterator(); iterator.hasNext();) {
            AfterAnnotationProxy afterAnnotation = (AfterAnnotationProxy) iterator.next();
            if (afterAnnotation != null) {
                final String expression = afterAnnotation.pointcut();
                final String adviceName = AspectAnnotationParser.getMethodPointcutCallSignature(
                        method.getName(), afterAnnotation
                );
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        adviceName,
                        afterAnnotation.getType(),
                        expression,
                        afterAnnotation.getSpecialArgumentType(),
                        aspectName,
                        aspectClassName,
                        method,
                        aspectDef
                );
                aspectDef.addAfterAdviceDefinition(adviceDef);
            }
        }
    }

    /**
     * Looks for "@Introduce IntroduceAttribute" defined at aspect inner class level
     *
     * @param classInfo of aspect
     * @param aspectDef
     * @param loader
     */
    private void parseClassAttributes(final ClassInfo classInfo,
                                      final AspectDefinition aspectDef,
                                      final ClassLoader loader) {
        if (classInfo == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        List annotations = AsmAnnotations.getAnnotations(AnnotationC.ANNOTATION_INTRODUCE, classInfo);
        for (Iterator iterator = annotations.iterator(); iterator.hasNext();) {
            IntroduceAnnotationProxy annotation = (IntroduceAnnotationProxy) iterator.next();
            if (annotation != null) {
                ClassInfo mixin;
                try {
                    mixin = AsmClassInfo.getClassInfo(annotation.innerClassName(), loader);
                } catch (Exception e) {
                    // TODO - we actually have a runtime exception already there.
                    throw new WrappedRuntimeException(e);
                }
                DefinitionParserHelper.createAndAddIntroductionDefToAspectDef(
                        mixin,
                        annotation.expression(),
                        annotation.deploymentModel(),
                        aspectDef
                );
            }
        }
    }

    /**
     * Returns the call signature of a Pointcut or advice with signature methodName(paramType paramName, ...) [we ignore
     * the return type] If there is no parameters, the call signature is not "name()" but just "name"
     *
     * @param methodName
     * @return annotationProxy that contains the ordered map of call parameters
     */
    private static String getMethodPointcutCallSignature(final String methodName,
                                                         final ParameterizedAnnotationProxy annotationProxy) {
        StringBuffer buffer = new StringBuffer(methodName);
        if (annotationProxy.getArgumentNames().size() > 0) {
            buffer.append('(');
            for (Iterator it = annotationProxy.getArgumentNames().iterator(); it.hasNext();) {
                String parameter = (String) it.next();
                buffer.append(annotationProxy.getArgumentType(parameter));
                buffer.append(' ').append(parameter);
                if (it.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(')');
        }
        return buffer.toString();
    }
}
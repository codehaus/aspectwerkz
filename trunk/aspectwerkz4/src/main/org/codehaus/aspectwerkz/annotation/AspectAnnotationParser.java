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
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAnnotations;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.aspect.AdviceType;

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
     * The sole instance.
     */
    private final static AspectAnnotationParser INSTANCE = new AspectAnnotationParser();

    /**
     * Private constructor to prevent subclassing.
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

        Aspect aspectAnnotation = (Aspect) AsmAnnotations.getAnnotation(
                AOPAnnotationConstants.ANNOTATION_ASPECT(),
                classInfo
        );
        //TODO review 1.5 annotation - depl model should be an ENUM or an int CONST (for 1.4 compat)
        String aspectName = classInfo.getName();
        String deploymentModel = DeploymentModel.getDeploymentModelAsString(DeploymentModel.PER_JVM);
        ;
        if (aspectAnnotation != null) {
            if (aspectAnnotation.value() != null) {
                //@Aspect(perJVM)
                deploymentModel = aspectAnnotation.value();
            } else {
                if (aspectAnnotation.name() != null) {
                    //@Aspect(name=..)
                    aspectName = aspectAnnotation.name();
                }
                if (aspectAnnotation.deploymentModel() != null) {
                    //@Aspect(deploymentModel=..)
                    deploymentModel = aspectAnnotation.deploymentModel();
                }
            }
        }

        // attribute settings override the xml settings
        aspectDef.setDeploymentModel(deploymentModel);
        String className = classInfo.getName();
        parseFieldAttributes(classInfo, aspectDef);
        parseMethodAttributes(classInfo, className, aspectName, aspectDef);
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
                if (AOPAnnotationConstants.ANNOTATION_EXPRESSION().equals(annotationInfo.getName())) {
                    if (field.getType().getName().equals(DeploymentScope.class.getName())) {
                        DefinitionParserHelper.createAndAddDeploymentScopeDef(
                                field.getName(),
                                ((Expression) annotationInfo.getAnnotation()).value(),
                                aspectDef.getSystemDefinition()
                        );
                    } else {
                        DefinitionParserHelper.createAndAddPointcutDefToAspectDef(
                                field.getName(),
                                ((Expression) annotationInfo.getAnnotation()).value(),
                                aspectDef
                        );
                    }
                } else if (AOPAnnotationConstants.ANNOTATION_INTRODUCE().equals(annotationInfo.getName())) {
                    DefinitionParserHelper.createAndAddInterfaceIntroductionDefToAspectDef(
                            ((Introduce) annotationInfo.getAnnotation()).value(),
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
            List expressionAnnotations = AsmAnnotations.getAnnotations(
                    AOPAnnotationConstants.ANNOTATION_EXPRESSION(), method
            );
            for (Iterator iterator = expressionAnnotations.iterator(); iterator.hasNext();) {
                Expression annotation = (Expression) iterator.next();
                if (annotation != null) {
                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(
                            getAdviceNameAsInSource(method),
                            annotation.value(), aspectDef
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
        List aroundAnnotations = AsmAnnotations.getAnnotations(AOPAnnotationConstants.ANNOTATION_AROUND(), method);
        for (Iterator iterator = aroundAnnotations.iterator(); iterator.hasNext();) {
            Around aroundAnnotation = (Around) iterator.next();
            if (aroundAnnotation != null) {
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        getAdviceNameAsInSource(method),
                        AdviceType.AROUND,
                        aroundAnnotation.value(),
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
        List beforeAnnotations = AsmAnnotations.getAnnotations(AOPAnnotationConstants.ANNOTATION_BEFORE(), method);
        for (Iterator iterator = beforeAnnotations.iterator(); iterator.hasNext();) {
            Before beforeAnnotation = (Before) iterator.next();
            if (beforeAnnotation != null) {
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        getAdviceNameAsInSource(method),
                        AdviceType.BEFORE,
                        beforeAnnotation.value(),
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
        List afterAnnotations = AsmAnnotations.getAnnotations(AOPAnnotationConstants.ANNOTATION_AFTER(), method);
        for (Iterator iterator = afterAnnotations.iterator(); iterator.hasNext();) {
            After annotation = (After) iterator.next();
            if (annotation != null) {
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        getAdviceNameAsInSource(method),
                        AdviceType.AFTER,
                        annotation.value(),
                        null,
                        aspectName,
                        aspectClassName,
                        method,
                        aspectDef
                );
                aspectDef.addAfterAdviceDefinition(adviceDef);
            }
        }
        afterAnnotations = AsmAnnotations.getAnnotations(AOPAnnotationConstants.ANNOTATION_AFTER_RETURNING(), method);
        for (Iterator iterator = afterAnnotations.iterator(); iterator.hasNext();) {
            AfterReturning annotation = (AfterReturning) iterator.next();
            if (annotation != null) {
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        getAdviceNameAsInSource(method),
                        AdviceType.AFTER_RETURNING,
                        getExpressionElseValue(annotation.value(), annotation.pointcut()),
                        annotation.type(),
                        aspectName,
                        aspectClassName,
                        method,
                        aspectDef
                );
                aspectDef.addAfterAdviceDefinition(adviceDef);
            }
        }
        afterAnnotations = AsmAnnotations.getAnnotations(AOPAnnotationConstants.ANNOTATION_AFTER_THROWING(), method);
        for (Iterator iterator = afterAnnotations.iterator(); iterator.hasNext();) {
            AfterThrowing annotation = (AfterThrowing) iterator.next();
            if (annotation != null) {
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        getAdviceNameAsInSource(method),
                        AdviceType.AFTER_THROWING,
                        getExpressionElseValue(annotation.value(), annotation.pointcut()),
                        annotation.type(),
                        aspectName,
                        aspectClassName,
                        method,
                        aspectDef
                );
                aspectDef.addAfterAdviceDefinition(adviceDef);
            }
        }
        afterAnnotations = AsmAnnotations.getAnnotations(AOPAnnotationConstants.ANNOTATION_AFTER_FINALLY(), method);
        for (Iterator iterator = afterAnnotations.iterator(); iterator.hasNext();) {
            AfterFinally annotation = (AfterFinally) iterator.next();
            if (annotation != null) {
                AdviceDefinition adviceDef = DefinitionParserHelper.createAdviceDefinition(
                        getAdviceNameAsInSource(method),
                        AdviceType.AFTER_FINALLY,
                        annotation.value(),
                        null,
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
     * Returns the call signature of a Pointcut or advice with signature methodName(paramType paramName, ...) [we ignore
     * the return type] If there is no parameters, the call signature is not "name()" but just "name"
     *
     * @param methodInfo
     * @return string representation (see javavadoc)
     */
    private static String getAdviceNameAsInSource(final MethodInfo methodInfo) {
        StringBuffer buffer = new StringBuffer(methodInfo.getName());
        if (methodInfo.getParameterNames() == null
            || methodInfo.getParameterNames().length != methodInfo.getParameterTypes().length
            || (methodInfo.getParameterNames().length > 0 && methodInfo.getParameterNames()[0] == null)) {
            throw new DefinitionException(
                    "Could not access source information for method " + methodInfo.getDeclaringType().getName() + "." +
                    methodInfo.getName() +
                    methodInfo.getSignature() +
                    ". Compile aspects with javac -g."
            );
        }
        if (methodInfo.getParameterNames().length > 0) {
            buffer.append('(');
            for (int i = 0; i < methodInfo.getParameterNames().length; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                String parameterName = methodInfo.getParameterNames()[i];
                buffer.append(methodInfo.getParameterTypes()[i].getName());
                buffer.append(' ').append(parameterName);
            }
            buffer.append(')');
        }
        return buffer.toString();
    }

    /**
     * Handles specific syntax for @AfterXXX annotation, where we can write it using the default "value" element
     * or instead specify the pointcut using "pointcut", and optionally a "type" element.
     *
     * @param value
     * @param pointcut
     * @return the one of value or expression which is not null. Both cannot be specified at the same time
     */
    public static String getExpressionElseValue(String value, String pointcut) {
        if (!Strings.isNullOrEmpty(pointcut)) {
            return pointcut;
        } else if (!Strings.isNullOrEmpty(value)) {
            return value;
        } else {
            throw new DefinitionException("neither expression nor value had a valid value");
        }
    }

}
/************1**************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionParserHelper;
import org.codehaus.aspectwerkz.definition.attribute.AspectAttribute;
import org.codehaus.aspectwerkz.definition.attribute.AroundAttribute;
import org.codehaus.aspectwerkz.definition.attribute.AfterAttribute;
import org.codehaus.aspectwerkz.definition.attribute.BeforeAttribute;
import org.codehaus.aspectwerkz.definition.attribute.IntroduceAttribute;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Extracts the aspects attributes from the class files and creates a meta-data representation of them.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AspectAttributeParser implements AttributeParser {

    /**
     * Parse the attributes and create and return a meta-data representation of them.
     *
     * @param klass      the class to extract attributes from
     * @param aspectDef  the aspect definition
     * @param definition the aspectwerkz definition
     */
    public void parse(final Class klass,
                      final AspectDefinition aspectDef,
                      final SystemDefinition definition) {
        if (klass == null) throw new IllegalArgumentException("class to parse can not be null");

        AspectAttribute aspectAttr = getAspectAttribute(klass);

        // attribute settings override the xml settings
        aspectDef.setDeploymentModel(aspectAttr.getDeploymentModel());

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
        if (aspectDef == null) throw new IllegalArgumentException("aspect definition can not be null");
        if (klass == null) return;
        if (klass.getName().equals(Aspect.class.getName())) return;

        Field[] fieldList = klass.getDeclaredFields();
        // parse the pointcuts
        for (int i = 0; i < fieldList.length; i++) {
            Field field = fieldList[i];
            Object[] fieldAttributes = Attributes.getAttributes(field);
            for (int j = 0; j < fieldAttributes.length; j++) {
                Object fieldAttr = fieldAttributes[j];

                if (fieldAttr instanceof ExecutionAttribute) {
                    ExecutionAttribute attribute = ((ExecutionAttribute) fieldAttr);
                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(field.getName(),
                            PointcutType.EXECUTION,
                            attribute.getExpression(),
                            aspectDef);
                    break;
                } else if (fieldAttr instanceof CallAttribute) {
                    CallAttribute attribute = ((CallAttribute) fieldAttr);
                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(field.getName(),
                            PointcutType.CALL,
                            attribute.getExpression(),
                            aspectDef);
                    break;
                } else if (fieldAttr instanceof ClassAttribute) {
                    ClassAttribute attribute = ((ClassAttribute) fieldAttr);
                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(field.getName(),
                            PointcutType.CLASS,
                            attribute.getExpression(),
                            aspectDef);
                    break;
                } else if (fieldAttr instanceof SetAttribute) {
                    SetAttribute attribute = ((SetAttribute) fieldAttr);
                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(field.getName(),
                            PointcutType.SET,
                            attribute.getExpression(),
                            aspectDef);
                    break;
                } else if (fieldAttr instanceof GetAttribute) {
                    GetAttribute attribute = ((GetAttribute) fieldAttr);
                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(field.getName(),
                            PointcutType.GET,
                            attribute.getExpression(),
                            aspectDef);
                    break;
                } else if (fieldAttr instanceof CFlowAttribute) {
                    CFlowAttribute attribute = ((CFlowAttribute) fieldAttr);
                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(field.getName(),
                            PointcutType.CFLOW,
                            attribute.getExpression(),
                            aspectDef);
                    // TODO: ALEX CFLOW
                    DefinitionParserHelper.createAndAddPointcutDefToAspectDef(field.getName() + "-System",
                            PointcutType.CALL,
                            attribute.getExpression(),
                            aspectDef);
                    break;
                } else if (fieldAttr instanceof ImplementsAttribute) {
                    ImplementsAttribute attribute = ((ImplementsAttribute) fieldAttr);
                    DefinitionParserHelper.createAndAddInterfaceIntroductionDefToAspectDef(attribute.getExpression(),
                            field.getName(),
                            field.getType().getName(),
                            aspectDef);
                    break;
                }
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
    private void parseMethodAttributes(final Class klass,
                                       final String aspectClassName,
                                       final String aspectName,
                                       final AspectDefinition aspectDef) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");
        if (aspectClassName == null) throw new IllegalArgumentException("aspect class name can not be null");
        if (aspectName == null) throw new IllegalArgumentException("aspect name can not be null");
        if (aspectDef == null) throw new IllegalArgumentException("aspect definition can not be null");

        List methodList = TransformationUtil.createSortedMethodList(klass);

        // parse the advices and introductions
        int methodIndex = 0;

        for (Iterator it = methodList.iterator(); it.hasNext(); methodIndex++) {
            Method method = (Method) it.next();
            // create the advice name out of the class and method name, <classname>.<methodname>
            String adviceName = aspectClassName + '.' + method.getName();
            Object[] methodAttributes = Attributes.getAttributes(method);
            for (int j = 0; j < methodAttributes.length; j++) {
                Object methodAttr = methodAttributes[j];

                if (methodAttr instanceof AroundAttribute) {
                    AroundAttribute aroundAttr = (AroundAttribute) methodAttr;
                    String name = aroundAttr.getName();
                    // custom name overrides the default one
                    if (name != null) {
                        adviceName = name;
                    }
                    String expression = aroundAttr.getExpression();
                    DefinitionParserHelper.createAndAddAroundAdviceDefToAspectDef(expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef);
                } else if (methodAttr instanceof BeforeAttribute) {
                    BeforeAttribute beforeAttr = (BeforeAttribute) methodAttr;
                    String name = beforeAttr.getName();
                    // custom name overrides the default one
                    if (name != null) {
                        adviceName = name;
                    }
                    String expression = beforeAttr.getExpression();
                    DefinitionParserHelper.createAndAddBeforeAdviceDefToAspectDef(expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef);
                } else if (methodAttr instanceof AfterAttribute) {
                    AfterAttribute afterAttr = (AfterAttribute) methodAttr;
                    String name = afterAttr.getName();
                    // custom name overrides the default one
                    if (name != null) {
                        adviceName = name;
                    }
                    String expression = afterAttr.getExpression();
                    DefinitionParserHelper.createAndAddAfterAdviceDefToAspectDef(expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef);
                }
            }
        }
    }

    /**
     * Retrieves the aspect attributes.
     *
     * @param klass the aspect class
     * @return the aspect attributes
     */
    private AspectAttribute getAspectAttribute(final Class klass) {
        AspectAttribute aspectAttr = null;
        Object[] classAttributes = Attributes.getAttributes(klass);
        for (int i = 0; i < classAttributes.length; i++) {
            Object classAttr = classAttributes[i];
            if (classAttr instanceof AspectAttribute) {
                aspectAttr = (AspectAttribute) classAttr;
                break;
            }
        }
        if (aspectAttr == null) {
            // fall back on using the class name as aspect name and let the deployment model be perJVM
            aspectAttr = new AspectAttribute(klass.getName(), DeploymentModel.PER_JVM);
        }
        return aspectAttr;
    }

    /**
     * Looks for @Introduce IntroduceAttribute defined at aspect inner class level
     *
     * @param klass     of aspect
     * @param aspectDef
     */
    private void parseClassAttributes(final Class klass, AspectDefinition aspectDef) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");

        Object[] classAttributes = Attributes.getAttributes(klass);
        for (int i = 0; i < classAttributes.length; i++) {
            IntroduceAttribute introduceAttr = null;
            if (classAttributes[i] instanceof IntroduceAttribute) {
                introduceAttr = (IntroduceAttribute) classAttributes[i];

                Class mixin = null;
                try {
                    mixin = klass.getClassLoader().loadClass(introduceAttr.getInnerClassName());
                } catch (ClassNotFoundException e) {
                    throw new WrappedRuntimeException(e);
                }
                Method[] methods = (Method[]) TransformationUtil.createSortedMethodList(mixin).toArray(new Method[]{});//gatherMixinSortedMethods(mixin, introduceAttr.getIntroducedInterfaceNames());
                DefinitionParserHelper.createAndAddIntroductionDefToAspectDef(introduceAttr.getExpression(),
                        introduceAttr.getInnerClassName(),
                        introduceAttr.getIntroducedInterfaceNames(),
                        methods,
                        introduceAttr.getDeploymentModel(),
                        aspectDef);
            }
        }
    }
}

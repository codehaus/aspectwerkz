/************1**************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AspectAttributeParser;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AspectAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AroundAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AfterAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.BeforeAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.IntroduceAttribute;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.MethodComparator;

/**
 * Extracts the aspects attributes from the class files and creates a meta-data representation of them.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class DefaultAspectAttributeParser extends AspectAttributeParser {

    /**
     * Parse the attributes and create and return a meta-data representation of them.
     *
     * @param klass the class to extract attributes from
     * @return the aspect meta-data
     */
    public AspectDefinition parse(final Class klass) {
        if (klass == null) throw new IllegalArgumentException("class to parse can not be null");

        AspectAttribute aspectAttr = getAspectAttribute(klass);
        String className = klass.getName();
        String aspectName = aspectAttr.getName();
        if (aspectName == null) {
            aspectName = className; // if no name is specified use the full class name of the aspect as name
        }
        // create the aspect definition
        AspectDefinition aspectDef = new AspectDefinition(
                aspectName,
                className,
                aspectAttr.getDeploymentModel()
        );

        parseFieldAttributes(klass, aspectDef);
        parseMethodAttributes(klass, className, aspectName, aspectDef);
        parseClassAttributes(klass, aspectDef);

        return aspectDef;
    }

    /**
     * Parses the field attributes and creates a meta-data representation of them.
     *
     * @param klass the class to extract attributes from
     * @param aspectDef the aspect definition
     */
    private void parseFieldAttributes(final Class klass, AspectDefinition aspectDef) {
        if (aspectDef == null) throw new IllegalArgumentException("aspect definition can not be null");
        if (klass == null) return;
        if (klass.getName().equals("org.codehaus.aspectwerkz.aspect.Aspect")) return;

        Field[] fieldList = klass.getDeclaredFields();
        // parse the pointcuts
        for (int i = 0; i < fieldList.length; i++) {
            Field field = fieldList[i];
            Object[] fieldAttributes = Attributes.getAttributes(field);
            for (int j = 0; j < fieldAttributes.length; j++) {
                Object fieldAttr = fieldAttributes[j];
                if (fieldAttr instanceof ExecutionAttribute) {
                    ExecutionAttribute attribute = ((ExecutionAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            field.getName(),
                            Pointcut.EXECUTION,
                            attribute.getExpression(),
                            aspectDef
                    );
                    break;
                }
                else if (fieldAttr instanceof CallAttribute) {
                    CallAttribute attribute = ((CallAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            field.getName(),
                            Pointcut.CALL,
                            attribute.getExpression(),
                            aspectDef
                    );
                    break;
                }
                else if (fieldAttr instanceof ClassAttribute) {
                    ClassAttribute attribute = ((ClassAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            field.getName(),
                            Pointcut.CLASS,
                            attribute.getExpression(),
                            aspectDef
                    );
                    break;
                }
                else if (fieldAttr instanceof SetAttribute) {
                    SetAttribute attribute = ((SetAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            field.getName(),
                            Pointcut.SET,
                            attribute.getExpression(),
                            aspectDef
                    );
                    break;
                }
                else if (fieldAttr instanceof GetAttribute) {
                    GetAttribute attribute = ((GetAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            field.getName(),
                            Pointcut.GET,
                            attribute.getExpression(),
                            aspectDef
                    );
                    break;
                }
                else if (fieldAttr instanceof ThrowsAttribute) {
                    ThrowsAttribute attribute = ((ThrowsAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            field.getName(),
                            Pointcut.THROWS,
                            attribute.getExpression(),
                            aspectDef
                    );
                    break;
                }
                else if (fieldAttr instanceof CFlowAttribute) {
                    CFlowAttribute attribute = ((CFlowAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            field.getName(),
                            Pointcut.CFLOW,
                            attribute.getExpression(),
                            aspectDef
                    );
                    break;
                }
                else if (fieldAttr instanceof ImplementsAttribute) {
                    ImplementsAttribute attribute = ((ImplementsAttribute)fieldAttr);
                    createAndAddInterfaceIntroductionDefToAspectDef(
                            attribute.getExpression(),
                            field.getName(),
                            field.getType().getName(),
                            aspectDef
                    );
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
     * @param klass the class
     * @param aspectClassName the aspect class name
     * @param aspectName the aspect name
     * @param aspectDef the aspect definition
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
            Method method = (Method)it.next();
            // create the advice name out of the class and method name, <classname>.<methodname>
            String adviceName = aspectClassName + '.' + method.getName();
            Object[] methodAttributes = Attributes.getAttributes(method);
            for (int j = 0; j < methodAttributes.length; j++) {
                Object methodAttr = methodAttributes[j];

                if (methodAttr instanceof AroundAttribute) {
                    AroundAttribute aroundAttr = (AroundAttribute)methodAttr;
                    String name = aroundAttr.getName();
                    if (name != null) {
                        adviceName = name;
                    }
                    String expression = aroundAttr.getExpression();
                    createAndAddAroundAdviceDefToAspectDef(
                            expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef
                    );
                }
                else if (methodAttr instanceof BeforeAttribute) {
                    BeforeAttribute beforeAttr = (BeforeAttribute)methodAttr;
                    String name = beforeAttr.getName();
                    if (name != null) {
                        adviceName = name;
                    }
                    String expression = beforeAttr.getExpression();
                    createAndAddBeforeAdviceDefToAspectDef(
                            expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef
                    );
                }
                else if (methodAttr instanceof AfterAttribute) {
                    AfterAttribute afterAttr = (AfterAttribute)methodAttr;
                    String name = afterAttr.getName();
                    String expression = afterAttr.getExpression();
                    if (name != null) {
                        adviceName = name;
                    }
                    createAndAddAfterAdviceDefToAspectDef(
                            expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef
                    );
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
                aspectAttr = (AspectAttribute)classAttr;
                break;
            }
        }
        if (aspectAttr == null) {
            throw new DefinitionException("aspect [" + klass.getName() + "] is not properly defined. (Is the aspect compiled with AspectC?)");
        }
        return aspectAttr;
    }

    /**
     * Looks for @Introduce IntroduceAttribute defined at aspect class level
     *
     * @param klass of aspect
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
                Method[] methods = mixin.getDeclaredMethods();//TODO is this enough (mixin impl inheritance)?
                Arrays.sort(methods, MethodComparator.getInstance(MethodComparator.NORMAL_METHOD));

                createAndAddIntroductionDefToAspectDef(
                        introduceAttr.getExpression(),
                        introduceAttr.getInnerClassName(),
                        introduceAttr.getIntroducedInterfaceNames(),
                        methods,
                        aspectDef
                );
            }
        }


    }
}

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

import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AspectAttributeParser;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AspectAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AroundAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AfterAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.BeforeAttribute;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.IntroductionAttribute;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 * Extracts the aspects attributes from the class files and creates a meta-data representation of them.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
        String aspectClassName = klass.getName();
        String aspectName = aspectClassName; // TODO: allow customized name, spec. in the attributes, CAUTION: will affect f.e. 'm_definition.getAspectIndexByName' in AddImplementationTransformer

        // create the aspect definition
        AspectDefinition aspectDef = new AspectDefinition(
                aspectName,
                aspectClassName,
                aspectAttr.getDeploymentModel()
        );

        parseFieldAttributes(klass, aspectDef);
        parseMethodAttributes(klass, aspectClassName, aspectName, aspectDef);

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
            String adviceName = aspectClassName + '.' + method.getName(); // TODO: allow a custom name, spec. in the attributes
            Object[] methodAttributes = Attributes.getAttributes(method);
            for (int j = 0; j < methodAttributes.length; j++) {
                Object methodAttr = methodAttributes[j];

                if (methodAttr instanceof AroundAttribute) {
                    String expression = ((AroundAttribute)methodAttr).getExpression();
                    createAndAddAroundAdviceDefToAspectDef(
                            expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef
                    );
                    break;
                }
                else if (methodAttr instanceof BeforeAttribute) {
                    String expression = ((BeforeAttribute)methodAttr).getExpression();
                    createAndAddBeforeAdviceDefToAspectDef(
                            expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef
                    );
                    break;
                }
                else if (methodAttr instanceof AfterAttribute) {
                    String expression = ((AfterAttribute)methodAttr).getExpression();
                    createAndAddAfterAdviceDefToAspectDef(
                            expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef
                    );
                    break;
                }
                else if (methodAttr instanceof IntroductionAttribute) {
                    String expression = ((IntroductionAttribute)methodAttr).getExpression();
                    createAndAddIntroductionDefToAspectDef(
                            expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef
                    );
                    break;
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
}

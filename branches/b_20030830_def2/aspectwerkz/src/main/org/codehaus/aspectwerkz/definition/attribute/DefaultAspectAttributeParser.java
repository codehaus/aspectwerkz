/************1**************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.attribute.AspectAttributeParser;
import org.codehaus.aspectwerkz.definition.attribute.AspectAttribute;
import org.codehaus.aspectwerkz.definition.attribute.AroundAdviceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.PostAdviceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.PreAdviceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.IntroductionAttribute;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * Extracts the aspects attributes from the class files and creates a meta-data representation of them.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
        String aspectName = aspectClassName; // TODO: allow customized name, spec. in the attributes

        // create the aspect definition
        AspectDefinition aspectDef = new AspectDefinition(
                aspectName,
                aspectClassName,
                aspectAttr.getDeploymentModel()
        );

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
                            Pointcut.EXECUTION,
                            attribute.getExpression(),
                            aspectDef,
                            field
                    );
                    break;
                }
                else if (fieldAttr instanceof CallAttribute) {
                    CallAttribute attribute = ((CallAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            Pointcut.CALL,
                            attribute.getExpression(),
                            aspectDef,
                            field
                    );
                    break;
                }
                else if (fieldAttr instanceof ClassAttribute) {
                    ClassAttribute attribute = ((ClassAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            Pointcut.CLASS,
                            attribute.getExpression(),
                            aspectDef,
                            field
                    );
                    break;
                }
                else if (fieldAttr instanceof SetAttribute) {
                    SetAttribute attribute = ((SetAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            Pointcut.SET,
                            attribute.getExpression(),
                            aspectDef,
                            field
                    );
                    break;
                }
                else if (fieldAttr instanceof GetAttribute) {
                    GetAttribute attribute = ((GetAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            Pointcut.GET,
                            attribute.getExpression(),
                            aspectDef,
                            field
                    );
                    break;
                }
                else if (fieldAttr instanceof ThrowsAttribute) {
                    ThrowsAttribute attribute = ((ThrowsAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            Pointcut.THROWS,
                            attribute.getExpression(),
                            aspectDef,
                            field
                    );
                    break;
                }
                else if (fieldAttr instanceof CFlowAttribute) {
                    CFlowAttribute attribute = ((CFlowAttribute)fieldAttr);
                    createAndAddPointcutDefToAspectDef(
                            Pointcut.CFLOW,
                            attribute.getExpression(),
                            aspectDef,
                            field
                    );
                    break;
                }
            }
        }

        List methodList = TransformationUtil.createSortedMethodList(klass);

        // parse the advices and introductions
        int methodIndex = 0;
        for (Iterator it = methodList.iterator(); it.hasNext(); methodIndex++) {
            Method method = (Method)it.next();
            String adviceName = aspectClassName + '.' + method.getName(); // TODO: allow a custom name, spec. in the attributes
            Object[] methodAttributes = Attributes.getAttributes(method);
            for (int j = 0; j < methodAttributes.length; j++) {
                Object methodAttr = methodAttributes[j];
                if (methodAttr instanceof AroundAdviceAttribute) {
                    String expression = ((AroundAdviceAttribute)methodAttr).getExpression();
                    createAndAddAroundAdviceDefToAspectDef(
                            expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef
                    );
                    break;
                }
                else if (methodAttr instanceof PreAdviceAttribute) {
                    String expression = ((PreAdviceAttribute)methodAttr).getExpression();
                    createAndAddPreAdviceDefToAspectDef(
                            expression, adviceName, aspectName,
                            aspectClassName, method, methodIndex, aspectDef
                    );
                    break;
                }
                else if (methodAttr instanceof PostAdviceAttribute) {
                    String expression = ((PostAdviceAttribute)methodAttr).getExpression();
                    createAndAddPostAdviceDefToAspectDef(
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
        if (aspectDef == null) {
            throw new DefinitionException("aspect [" + aspectName + "] is not properly defined (check the attributes)");
        }
        return aspectDef;
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
            throw new DefinitionException("aspect [" + klass.getName() + "] is not properly defined (check the attributes)");
        }
        return aspectAttr;
    }
}

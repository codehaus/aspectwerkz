/************1**************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import attrib4j.Attribute;
import attrib4j.Attributes;

import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.AspectAttributeParser;
import org.codehaus.aspectwerkz.definition.attribute.AspectAttribute;
import org.codehaus.aspectwerkz.definition.attribute.AroundAdviceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.PostAdviceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.PreAdviceAttribute;
import org.codehaus.aspectwerkz.definition.attribute.PointcutAttribute;
import org.codehaus.aspectwerkz.definition.attribute.IntroductionAttribute;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 * Attributes parser based on attrib4j.
 * Extracts the aspects attributes and creates a meta-data representation of them.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Attrib4jAspectAttributeParser extends AspectAttributeParser {

    /**
     * Parse the attributes and create and return a meta-data representation of them.
     *
     * @param klass the class to extract attributes from
     * @return the aspect meta-data
     */
    public AspectDefinition parse(final Class klass) {

        AspectAttribute aspectAttr = getAspectAttribute(klass);

        String aspectClassName = klass.getName();
        String aspectName = aspectClassName; // TODO: allow customized name, spec. in the attributes

        // create the aspect definition
        AspectDefinition aspectDef = new AspectDefinition(
                aspectName,
                aspectClassName,
                aspectAttr.getDeploymentModel()
        );

        List methodList = TransformationUtil.createSortedMethodList(klass);

        // handle the pointcuts
        for (Iterator it = methodList.iterator(); it.hasNext(); ) {
            Method method = (Method)it.next();
            Attribute[] methodAttributes = Attributes.getAttributes(method);
            for (int j = 0; j < methodAttributes.length; j++) {
                Attribute methodAttr = methodAttributes[j];
                if (methodAttr instanceof PointcutAttribute) {
                    String expression = ((PointcutAttribute)methodAttr).getExpression();
                    createAndAddPointcutDefToAspectDef(expression, aspectDef, method);
                    break;
                }
            }
        }

        // handle the advices and introductions
        int methodIndex = 0;
        for (Iterator it = methodList.iterator(); it.hasNext(); methodIndex++) {
            Method method = (Method)it.next();
            String adviceName = aspectClassName + '.' + method.getName(); // TODO: allow a custom name, spec. in the attributes
            Attribute[] methodAttributes = Attributes.getAttributes(method);
            for (int j = 0; j < methodAttributes.length; j++) {
                Attribute methodAttr = methodAttributes[j];
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
        Attribute[] classAttributes = Attributes.getAttributes(klass);
        for (int i = 0; i < classAttributes.length; i++) {
            Attribute classAttr = classAttributes[i];
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

    /**
     * Main for testing.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            AspectAttributeParser parser = new Attrib4jAspectAttributeParser();
            AspectDefinition aspectMetaData = parser.parse(Class.forName(args[0]));
            System.out.println("-- Pointcuts --");
            for (Iterator it = aspectMetaData.getPointcuts().iterator(); it.hasNext();) {
                PointcutDefinition pointcutMetaData = (PointcutDefinition)it.next();
                System.out.println("pointcutMetaData.getName() = " + pointcutMetaData.getName());
                System.out.println("pointcutMetaData.getExpression() = " + pointcutMetaData.getExpression());
            }
            System.out.println("-- AroundAdvices --");
            for (Iterator it = aspectMetaData.getAroundAdvices().iterator(); it.hasNext();) {
                AdviceDefinition adviceMetaData = (AdviceDefinition)it.next();
                System.out.println("adviceMetaData.getExpression() = " + adviceMetaData.getPointcut());
                System.out.println("adviceMetaData.getMethod() = " + adviceMetaData.getMethod());
            }
            System.out.println("-- PreAdvices --");
            for (Iterator it = aspectMetaData.getPreAdvices().iterator(); it.hasNext();) {
                AdviceDefinition adviceMetaData = (AdviceDefinition)it.next();
                System.out.println("adviceMetaData.getExpression() = " + adviceMetaData.getPointcut());
                System.out.println("adviceMetaData.getMethod() = " + adviceMetaData.getMethod());
            }
            System.out.println("-- PostAdvices --");
            for (Iterator it = aspectMetaData.getPostAdvices().iterator(); it.hasNext();) {
                AdviceDefinition adviceMetaData = (AdviceDefinition)it.next();
                System.out.println("adviceMetaData.getExpression() = " + adviceMetaData.getPointcut());
                System.out.println("adviceMetaData.getMethod() = " + adviceMetaData.getMethod());
            }
            System.out.println("-- Introductions --");
            for (Iterator it = aspectMetaData.getIntroductions().iterator(); it.hasNext();) {
                IntroductionDefinition introductionMetaData = (IntroductionDefinition)it.next();
                System.out.println("introductionMetaData.getExpression() = " + introductionMetaData.getPointcut());
                System.out.println("introductionMetaData.getMethod() = " + introductionMetaData.getMethod());
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

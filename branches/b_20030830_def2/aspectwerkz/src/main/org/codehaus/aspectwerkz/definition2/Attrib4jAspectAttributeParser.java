/************1**************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition2;

import java.lang.reflect.Method;
import java.util.Iterator;

import attrib4j.Attribute;
import attrib4j.Attributes;

import org.codehaus.aspectwerkz.definition2.AspectDefinition2;
import org.codehaus.aspectwerkz.definition2.AdviceDefinition2;
import org.codehaus.aspectwerkz.definition2.AspectAttributeParser;
import org.codehaus.aspectwerkz.definition2.attribute.AspectAttribute;
import org.codehaus.aspectwerkz.definition2.attribute.AroundAdviceAttribute;
import org.codehaus.aspectwerkz.definition2.attribute.PostAdviceAttribute;
import org.codehaus.aspectwerkz.definition2.attribute.PreAdviceAttribute;
import org.codehaus.aspectwerkz.definition2.attribute.PointcutAttribute;
import org.codehaus.aspectwerkz.definition2.attribute.IntroductionAttribute;

/**
 * Extracts the aspects attributes and creates a meta-data representation of them.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Attrib4jAspectAttributeParser implements AspectAttributeParser {

    /**
     * Parse the attributes and create and return a meta-data representation of them.
     *
     * @param klass the class to extract attributes from
     * @return the aspect meta-data
     */
    public AspectDefinition2 parse(final Class klass) {
        // get the class level attributes.
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
            return null; // no aspect => return
        }

        // create the aspect meta-data
        AspectDefinition2 aspectMetaData = new AspectDefinition2(
                klass.getName(), // TODO: allow customized name
                klass,
                aspectAttr.getDeploymentModel()
        );

        // get the method level attributes
        Method[] methods = klass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            String adviceName = aspectMetaData.getKlass().getName() + '.' + methods[i].getName(); // TODO: allow a custom name

            Attribute[] methodAttributes = Attributes.getAttributes(methods[i]);

            for (int j = 0; j < methodAttributes.length; j++) {
                Attribute methodAttr = methodAttributes[j];

                if (methodAttr instanceof PointcutAttribute) {
                    aspectMetaData.addPointcut(
                            new PointcutDefinition2(
                                    ((PointcutAttribute)methodAttr).getExpression(),
                                    methods[i]
                            ));
                    break;
                }
                else if (methodAttr instanceof AroundAdviceAttribute) {
                    aspectMetaData.addAroundAdvice(
                            new AdviceDefinition2(
                                    adviceName,
                                    ((AroundAdviceAttribute)methodAttr).getPointcut(),
                                    methods[i],
                                    aspectMetaData.getDeploymentModel()
                            ));
                    break;
                }
                else if (methodAttr instanceof PreAdviceAttribute) {
                    aspectMetaData.addPreAdvice(
                            new AdviceDefinition2(
                                    adviceName,
                                    ((PreAdviceAttribute)methodAttr).getPointcut(),
                                    methods[i],
                                    aspectMetaData.getDeploymentModel()
                            ));
                    break;
                }
                else if (methodAttr instanceof PostAdviceAttribute) {
                    aspectMetaData.addPostAdvice(
                            new AdviceDefinition2(
                                    adviceName,
                                    ((PostAdviceAttribute)methodAttr).getPointcut(),
                                    methods[i],
                                    aspectMetaData.getDeploymentModel()
                            ));
                    break;
                }
                else if (methodAttr instanceof IntroductionAttribute) {
                    aspectMetaData.addIntroduction(
                            new IntroductionDefinition2(
                                    adviceName,
                                    ((IntroductionAttribute)methodAttr).getPointcut(),
                                    methods[i],
                                    aspectMetaData.getDeploymentModel()
                            ));
                    break;
                }
            }
        }
        return aspectMetaData;
    }

    /**
     * Main for testing.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            AspectAttributeParser parser = new Attrib4jAspectAttributeParser();
            AspectDefinition2 aspectMetaData = parser.parse(Class.forName(args[0]));
            System.out.println("-- Pointcuts --");
            for (Iterator it = aspectMetaData.getPointcuts().iterator(); it.hasNext();) {
                PointcutDefinition2 pointcutMetaData = (PointcutDefinition2)it.next();
                System.out.println("pointcutMetaData.getName() = " + pointcutMetaData.getName());
                System.out.println("pointcutMetaData.getExpression() = " + pointcutMetaData.getExpression());
            }
            System.out.println("-- AroundAdvices --");
            for (Iterator it = aspectMetaData.getAroundAdvices().iterator(); it.hasNext();) {
                AdviceDefinition2 adviceMetaData = (AdviceDefinition2)it.next();
                System.out.println("adviceMetaData.getExpression() = " + adviceMetaData.getPointcut());
                System.out.println("adviceMetaData.getMethod() = " + adviceMetaData.getMethod());
            }
            System.out.println("-- PreAdvices --");
            for (Iterator it = aspectMetaData.getPreAdvices().iterator(); it.hasNext();) {
                AdviceDefinition2 adviceMetaData = (AdviceDefinition2)it.next();
                System.out.println("adviceMetaData.getExpression() = " + adviceMetaData.getPointcut());
                System.out.println("adviceMetaData.getMethod() = " + adviceMetaData.getMethod());
            }
            System.out.println("-- PostAdvices --");
            for (Iterator it = aspectMetaData.getPostAdvices().iterator(); it.hasNext();) {
                AdviceDefinition2 adviceMetaData = (AdviceDefinition2)it.next();
                System.out.println("adviceMetaData.getExpression() = " + adviceMetaData.getPointcut());
                System.out.println("adviceMetaData.getMethod() = " + adviceMetaData.getMethod());
            }
            System.out.println("-- Introductions --");
            for (Iterator it = aspectMetaData.getIntroductions().iterator(); it.hasNext();) {
                IntroductionDefinition2 introductionMetaData = (IntroductionDefinition2)it.next();
                System.out.println("introductionMetaData.getExpression() = " + introductionMetaData.getPointcut());
                System.out.println("introductionMetaData.getMethod() = " + introductionMetaData.getMethod());
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.expression.ExpressionNamespace;

import java.lang.reflect.Method;

import java.util.Iterator;

/**
 * Helper class for the attribute and the XML definition parsers.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DefinitionParserHelper
{
    /**
     * Creates and add pointcut definition to aspect definition.
     *
     * @param name
     * @param expression
     * @param aspectDef
     */
    public static void createAndAddPointcutDefToAspectDef(final String name,
        final String expression, final AspectDefinition aspectDef)
    {
        PointcutDefinition pointcutDef = new PointcutDefinition();

        pointcutDef.setName(name);
        pointcutDef.setExpression(expression);
        aspectDef.addPointcut(pointcutDef);

        String aspectName = aspectDef.getName();

        ExpressionNamespace.getNamespace(aspectName).addExpressionInfo(name,
            new ExpressionInfo(expression, aspectName));
    }

    /**
     * Creates and add around advice definition to aspect definition.
     *
     * @param expression
     * @param adviceName
     * @param aspectName
     * @param aspectClassName
     * @param method
     * @param methodIndex
     * @param aspectDef
     */
    public static void createAndAddAroundAdviceDefToAspectDef(
        final String expression, final String adviceName,
        final String aspectName, final String aspectClassName,
        final Method method, final int methodIndex,
        final AspectDefinition aspectDef)
    {
        try
        {
            AdviceDefinition adviceDef = createAdviceDefinition(adviceName,
                    AdviceDefinition.AROUND_ADVICE, aspectName,
                    aspectClassName, expression, method, methodIndex, aspectDef);

            aspectDef.addAroundAdvice(adviceDef);
        }
        catch (DefinitionException e)
        {
            // see AW-152.
            System.err.println(
                "AspectWerkz - <WARN> unable to register advice " + aspectName
                + "." + adviceName + " at \"" + expression + "\": "
                + e.getMessage());

            // go on silently
            // TODO AV - better handling of reg issue (f.e. skip the whole aspect, in DocumentParser, based on DefinitionE
        }
    }

    /**
     * Creates and add pre advice definition to aspect definition.
     *
     * @param expression
     * @param adviceName
     * @param aspectName
     * @param aspectClassName
     * @param method
     * @param methodIndex
     * @param aspectDef
     */
    public static void createAndAddBeforeAdviceDefToAspectDef(
        final String expression, final String adviceName,
        final String aspectName, final String aspectClassName,
        final Method method, final int methodIndex,
        final AspectDefinition aspectDef)
    {
        try
        {
            AdviceDefinition adviceDef = createAdviceDefinition(adviceName,
                    AdviceDefinition.BEFORE_ADVICE, aspectName,
                    aspectClassName, expression, method, methodIndex, aspectDef);

            aspectDef.addBeforeAdvice(adviceDef);
        }
        catch (DefinitionException e)
        {
            // see AW-152.
            System.err.println(
                "AspectWerkz - <WARN> unable to register advice " + aspectName
                + "." + adviceName + " at \"" + expression + "\": "
                + e.getMessage());

            // go on silently
            // TODO AV - better handling of reg issue (f.e. skip the whole aspect, in DocumentParser, based on DefinitionE
        }
    }

    /**
     * Creates and add post advice definition to aspect definition.
     *
     * @param expression
     * @param adviceName
     * @param aspectName
     * @param aspectClassName
     * @param method
     * @param methodIndex
     * @param aspectDef
     */
    public static void createAndAddAfterAdviceDefToAspectDef(
        final String expression, final String adviceName,
        final String aspectName, final String aspectClassName,
        final Method method, final int methodIndex,
        final AspectDefinition aspectDef)
    {
        try
        {
            AdviceDefinition adviceDef = createAdviceDefinition(adviceName,
                    AdviceDefinition.AFTER_ADVICE, aspectName, aspectClassName,
                    expression, method, methodIndex, aspectDef);

            aspectDef.addAfterAdvice(adviceDef);
        }
        catch (DefinitionException e)
        {
            // see AW-152.
            System.err.println(
                "AspectWerkz - <WARN> unable to register advice " + aspectName
                + "." + adviceName + " at \"" + expression + "\": "
                + e.getMessage());

            // go on silently
            // TODO AV - better handling of reg issue (f.e. skip the whole aspect, in DocumentParser, based on DefinitionE
        }
    }

    /**
     * Creates and add introduction definition to aspect definition.
     *
     * @param expression
     * @param introductionName
     * @param introducedInterfaceNames
     * @param introducedMethods
     * @param deploymentModel
     * @param aspectDef
     */
    public static void createAndAddIntroductionDefToAspectDef(
        final String expression, final String introductionName,
        final String[] introducedInterfaceNames,
        final Method[] introducedMethods, final String deploymentModel,
        final AspectDefinition aspectDef)
    {
        IntroductionDefinition introDef = createIntroductionDefinition(introductionName,
                expression, introducedInterfaceNames, introducedMethods,
                deploymentModel, aspectDef);

        // check doublons - TODO change ArrayList to HashMap since NAME is a key
        IntroductionDefinition doublon = null;

        for (Iterator intros = aspectDef.getIntroductions().iterator();
            intros.hasNext();)
        {
            IntroductionDefinition intro = (IntroductionDefinition) intros.next();

            if (intro.getName().equals(introDef.getName()))
            {
                doublon = intro;
                intro.addExpressionInfos(introDef.getExpressionInfos());

                break;
            }
        }

        if (doublon == null)
        {
            aspectDef.addIntroduction(introDef);
        }
    }

    /**
     * Creates and add interface introduction definition to aspect definition.
     *
     * @param expression
     * @param introductionName
     * @param interfaceClassName
     * @param aspectDef
     */
    public static void createAndAddInterfaceIntroductionDefToAspectDef(
        final String expression, final String introductionName,
        final String interfaceClassName, final AspectDefinition aspectDef)
    {
        InterfaceIntroductionDefinition introDef = createInterfaceIntroductionDefinition(introductionName,
                expression, interfaceClassName, aspectDef);

        aspectDef.addInterfaceIntroduction(introDef);
    }

    /**
     * Creates a new advice definition.
     *
     * @param adviceName      the advice name
     * @param adviceType      the advice type
     * @param aspectName      the aspect name
     * @param aspectClassName the aspect class name
     * @param expression      the pointcut expression
     * @param method          the advice method
     * @param methodIndex     the advice method index
     * @param aspectDef       the aspect definition
     * @return the new advice definition
     */
    public static AdviceDefinition createAdviceDefinition(
        final String adviceName, final String adviceType,
        final String aspectName, final String aspectClassName,
        final String expression, final Method method, final int methodIndex,
        final AspectDefinition aspectDef)
    {
        ExpressionInfo expressionInfo = new ExpressionInfo(expression,
                aspectName);
        final AdviceDefinition adviceDef = new AdviceDefinition(adviceName,
                adviceType, aspectName, aspectClassName, expressionInfo,
                method, methodIndex, aspectDef);

        return adviceDef;
    }

    /**
     * Creates an introduction definition.
     *
     * @param introductionName
     * @param expression
     * @param introducedInterfaceNames
     * @param introducedMethods
     * @param deploymentModel
     * @param aspectDef
     * @return
     */
    public static IntroductionDefinition createIntroductionDefinition(
        final String introductionName, final String expression,
        final String[] introducedInterfaceNames,
        final Method[] introducedMethods, final String deploymentModel,
        final AspectDefinition aspectDef)
    {
        String aspectName = aspectDef.getName();
        ExpressionInfo expressionInfo = new ExpressionInfo(expression,
                aspectName);

        ExpressionNamespace.getNamespace(aspectName).addExpressionInfo(expression,
            expressionInfo);

        final IntroductionDefinition introDef = new IntroductionDefinition(introductionName,
                expressionInfo, introducedInterfaceNames, introducedMethods,
                deploymentModel);

        return introDef;
    }

    /**
     * Creates a new interface introduction definition.
     *
     * @param introductionName   the introduction name
     * @param expression         the pointcut expression
     * @param interfaceClassName the class name of the interface
     * @param aspectDef          the aspect definition
     * @return the new introduction definition
     */
    public static InterfaceIntroductionDefinition createInterfaceIntroductionDefinition(
        final String introductionName, final String expression,
        final String interfaceClassName, final AspectDefinition aspectDef)
    {
        String aspectName = aspectDef.getName();
        ExpressionInfo expressionInfo = new ExpressionInfo(expression,
                aspectName);

        ExpressionNamespace.getNamespace(aspectName).addExpressionInfo(expression,
            expressionInfo);

        final InterfaceIntroductionDefinition introDef = new InterfaceIntroductionDefinition(introductionName,
                expressionInfo, interfaceClassName);

        return introDef;
    }
}

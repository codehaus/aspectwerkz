/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.aspect.AdviceType;

import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * Helper class for the attribute and the XML definition parsers.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class DefinitionParserHelper {
    public static final String EXPR_PREFIX = "AW_";

    /**
     * Creates and add pointcut definition to aspect definition.
     *
     * @param name
     * @param expression
     * @param aspectDef
     */
    public static void createAndAddPointcutDefToAspectDef(final String name,
                                                          final String expression,
                                                          final AspectDefinition aspectDef) {
        PointcutDefinition pointcutDef = new PointcutDefinition(expression);
        aspectDef.addPointcutDefinition(pointcutDef);

        // name can be the "pcName(paramType paramName)"
        // extract the parameter name to type map
        // and register the pointcut using its name
        //TODO: support for same pc name and different signature
        String pointcutName = name;
        String pointcutCallSignature = null;
        if (name.indexOf("(") > 0) {
            pointcutName = name.substring(0, name.indexOf("("));
            pointcutCallSignature = name.substring(name.indexOf("(") + 1, name.lastIndexOf(")"));
        }

        // do a lookup first to avoid infinite recursion when:
        // <pointcut name="pc" ...> [will be registered as pc]
        // <advice bind-to="pc" ...> [will be registered as pc and should not override previous one !]
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(aspectDef.getQualifiedName());
        ExpressionInfo info = namespace.getExpressionInfoOrNull(pointcutName);
        if (info == null) {
            info = new ExpressionInfo(expression, aspectDef.getQualifiedName());
            // extract the pointcut signature map
            if (pointcutCallSignature != null) {
                String[] parameters = Strings.splitString(pointcutCallSignature, ",");
                for (int i = 0; i < parameters.length; i++) {
                    String[] parameterInfo = Strings.splitString(
                            Strings.replaceSubString(
                                    parameters[i].trim(),
                                    "  ",
                                    " "
                            ), " "
                    );
                    info.addArgument(parameterInfo[1], parameterInfo[0]);
                }
            }
        }
        ExpressionNamespace.getNamespace(aspectDef.getQualifiedName()).addExpressionInfo(pointcutName, info);
    }

    /**
     * Creates and add introduction definition to aspect definition.
     *
     * @param mixinClass
     * @param expression
     * @param deploymentModel
     * @param aspectDef
     */
    public static void createAndAddIntroductionDefToAspectDef(final Class mixinClass,
                                                              final String expression,
                                                              final String deploymentModel,
                                                              final AspectDefinition aspectDef) {
        IntroductionDefinition introDef = createIntroductionDefinition(
                mixinClass,
                expression,
                deploymentModel,
                aspectDef
        );

        // check doublons - TODO change ArrayList to HashMap since NAME is a key
        IntroductionDefinition doublon = null;
        for (Iterator intros = aspectDef.getIntroductionDefinitions().iterator(); intros.hasNext();) {
            IntroductionDefinition intro = (IntroductionDefinition) intros.next();
            if (intro.getName().equals(introDef.getName())) {
                doublon = intro;
                intro.addExpressionInfos(introDef.getExpressionInfos());
                break;
            }
        }
        if (doublon == null) {
            aspectDef.addIntroductionDefinition(introDef);
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
    public static void createAndAddInterfaceIntroductionDefToAspectDef(final String expression,
                                                                       final String introductionName,
                                                                       final String interfaceClassName,
                                                                       final AspectDefinition aspectDef) {
        InterfaceIntroductionDefinition introDef = createInterfaceIntroductionDefinition(
                introductionName,
                expression,
                interfaceClassName,
                aspectDef
        );
        aspectDef.addInterfaceIntroductionDefinition(introDef);
    }

    /**
     * Creates a new advice definition.
     *
     * @param adviceName          the advice name
     * @param adviceType          the advice type
     * @param expression          the advice expression
     * @param specialArgumentType the arg
     * @param aspectName          the aspect name
     * @param aspectClassName     the aspect class name
     * @param method              the advice method
     * @param aspectDef           the aspect definition
     * @return the new advice definition
     */
    public static AdviceDefinition createAdviceDefinition(final String adviceName,
                                                          final AdviceType adviceType,
                                                          final String expression,
                                                          final String specialArgumentType,
                                                          final String aspectName,
                                                          final String aspectClassName,
                                                          final Method method,
                                                          final AspectDefinition aspectDef) {
        ExpressionInfo expressionInfo = new ExpressionInfo(
                expression,
                aspectDef.getQualifiedName()
        );

        // support for pointcut signature
        String adviceCallSignature = null;
        if (adviceName.indexOf('(') > 0) {
            adviceCallSignature = adviceName.substring(adviceName.indexOf('(') + 1, adviceName.lastIndexOf(')'));
            String[] parameters = Strings.splitString(adviceCallSignature, ",");
            for (int i = 0; i < parameters.length; i++) {
                String[] parameterInfo = Strings.splitString(
                        Strings.replaceSubString(parameters[i].trim(), "  ", " "),
                        " "
                );
                expressionInfo.addArgument(parameterInfo[1], parameterInfo[0]);
            }
        }

        final AdviceDefinition adviceDef = new AdviceDefinition(
                adviceName,
                adviceType,
                specialArgumentType,
                aspectName,
                aspectClassName,
                expressionInfo,
                method,
                aspectDef
        );
        return adviceDef;
    }

    /**
     * Creates an introduction definition.
     *
     * @param mixinClass
     * @param expression
     * @param deploymentModel
     * @param aspectDef
     * @return
     */
    public static IntroductionDefinition createIntroductionDefinition(final Class mixinClass,
                                                                      final String expression,
                                                                      final String deploymentModel,
                                                                      final AspectDefinition aspectDef) {
        ExpressionInfo expressionInfo = new ExpressionInfo(expression, aspectDef.getQualifiedName());

        // auto-name the pointcut which is anonymous for introduction
        ExpressionNamespace.getNamespace(aspectDef.getQualifiedName()).addExpressionInfo(
                EXPR_PREFIX + expression.hashCode(),
                expressionInfo
        );
        final IntroductionDefinition introDef = new IntroductionDefinition(
                mixinClass, expressionInfo, deploymentModel
        );
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
    public static InterfaceIntroductionDefinition createInterfaceIntroductionDefinition(final String introductionName,
                                                                                        final String expression,
                                                                                        final String interfaceClassName,
                                                                                        final AspectDefinition aspectDef) {
        ExpressionInfo expressionInfo = new ExpressionInfo(expression, aspectDef.getQualifiedName());

        // auto-name the pointcut which is anonymous for introduction
        ExpressionNamespace.getNamespace(aspectDef.getQualifiedName()).addExpressionInfo(
                EXPR_PREFIX + expression.hashCode(),
                expressionInfo
        );
        final InterfaceIntroductionDefinition introDef = new InterfaceIntroductionDefinition(
                introductionName,
                expressionInfo,
                interfaceClassName
        );
        return introDef;
    }

}
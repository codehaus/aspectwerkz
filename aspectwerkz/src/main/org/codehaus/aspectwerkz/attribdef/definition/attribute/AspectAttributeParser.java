/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.expression.ExpressionTemplate;
import org.codehaus.aspectwerkz.attribdef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.InterfaceIntroductionDefinition;

/**
 * Abstract base class for all the aspect attribute parsers to extend.
 * <p/>Extracts the aspects attributes and creates a meta-data representation of them.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AspectAttributeParser {

    /**
     * Parse the attributes and create and return a meta-data representation of them.
     *
     * @param klass the class to extract attributes from
     * @return the aspect meta-data
     */
    public abstract AspectDefinition parse(final Class klass);

    /**
     * Creates and add pointcut definition to aspect definition.
     *
     * @param name
     * @param type
     * @param expression
     * @param aspectDef
     */
    protected void createAndAddPointcutDefToAspectDef(final String name,
                                                      final PointcutType type,
                                                      final String expression,
                                                      final AspectDefinition aspectDef) {
        PointcutDefinition pointcutDef = new PointcutDefinition();
        pointcutDef.setName(name);
        pointcutDef.setType(type);
        pointcutDef.setExpression(expression);
        aspectDef.addPointcut(pointcutDef);

        // create and add a new expression template
        ExpressionTemplate expressionTemplate = Expression.createExpressionTemplate(
                aspectDef.getName(), expression, "", name, type
        );
        Expression.registerExpressionTemplate(expressionTemplate);
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
    protected void createAndAddAroundAdviceDefToAspectDef(final String expression,
                                                          final String adviceName,
                                                          final String aspectName,
                                                          final String aspectClassName,
                                                          final Method method,
                                                          final int methodIndex,
                                                          final AspectDefinition aspectDef) {
        AdviceDefinition adviceDef = createAdviceDefinition(
                adviceName, aspectName, aspectClassName,
                expression, method, methodIndex, aspectDef
        );
        aspectDef.addAroundAdvice(adviceDef);
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
    protected void createAndAddBeforeAdviceDefToAspectDef(final String expression,
                                                          final String adviceName,
                                                          final String aspectName,
                                                          final String aspectClassName,
                                                          final Method method,
                                                          final int methodIndex,
                                                          final AspectDefinition aspectDef) {
        AdviceDefinition adviceDef = createAdviceDefinition(
                adviceName, aspectName, aspectClassName,
                expression, method, methodIndex, aspectDef
        );
        aspectDef.addBeforeAdvice(adviceDef);
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
    protected void createAndAddAfterAdviceDefToAspectDef(final String expression,
                                                         final String adviceName,
                                                         final String aspectName,
                                                         final String aspectClassName,
                                                         final Method method,
                                                         final int methodIndex,
                                                         final AspectDefinition aspectDef) {
        AdviceDefinition adviceDef = createAdviceDefinition(
                adviceName, aspectName, aspectClassName,
                expression, method, methodIndex, aspectDef
        );
        aspectDef.addAfterAdvice(adviceDef);
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
    protected void createAndAddIntroductionDefToAspectDef(final String expression,
                                                          final String introductionName,
                                                          final String[] introducedInterfaceNames,
                                                          final Method[] introducedMethods,
                                                          final String deploymentModel,
                                                          final AspectDefinition aspectDef) {
        IntroductionDefinition introDef = createIntroductionDefinition(
                introductionName,
                expression,
                introducedInterfaceNames,
                introducedMethods,
                deploymentModel,
                aspectDef);
        aspectDef.addIntroduction(introDef);
    }

    /**
     * Creates and add interface introduction definition to aspect definition.
     *
     * @param expression
     * @param introductionName
     * @param interfaceClassName
     * @param aspectDef
     */
    protected void createAndAddInterfaceIntroductionDefToAspectDef(final String expression,
                                                                   final String introductionName,
                                                                   final String interfaceClassName,
                                                                   final AspectDefinition aspectDef) {
        InterfaceIntroductionDefinition introDef = createInterfaceIntroductionDefinition(
                introductionName, expression, interfaceClassName, aspectDef
        );
        aspectDef.addInterfaceIntroduction(introDef);
    }

    /**
     * Creates a new advice definition.
     *
     * @param adviceName the advice name
     * @param aspectName the aspect name
     * @param aspectClassName the aspect class name
     * @param expression the pointcut expression
     * @param method the advice method
     * @param methodIndex the advice method index
     * @param aspectDef the aspect definition
     * @return the new advice definition
     */
    protected AdviceDefinition createAdviceDefinition(final String adviceName,
                                                      final String aspectName,
                                                      final String aspectClassName,
                                                      final String expression,
                                                      final Method method,
                                                      final int methodIndex,
                                                      final AspectDefinition aspectDef) {

        Expression expr = Expression.createRootExpression(
                aspectDef.getName(), expression
        );
        final AdviceDefinition adviceDef = new AdviceDefinition(
                adviceName, aspectName, aspectClassName,
                expr, method, methodIndex, aspectDef
        );
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
    protected IntroductionDefinition createIntroductionDefinition(final String introductionName,
                                                                  final String expression,
                                                                  final String[] introducedInterfaceNames,
                                                                  final Method[] introducedMethods,
                                                                  final String deploymentModel,
                                                                  final AspectDefinition aspectDef) {
        Expression expr = Expression.createRootExpression(
                aspectDef.getName(), expression, PointcutType.CLASS
        );
        final IntroductionDefinition introDef = new IntroductionDefinition(
                introductionName, expr, introducedInterfaceNames, introducedMethods, deploymentModel
        );
        return introDef;
    }

    /**
     * Creates a new interface introduction definition.
     *
     * @param introductionName the introduction name
     * @param expression the pointcut expression
     * @param interfaceClassName the class name of the interface
     * @param aspectDef the aspect definition
     * @return the new introduction definition
     */
    protected InterfaceIntroductionDefinition createInterfaceIntroductionDefinition(
            final String introductionName,
            final String expression,
            final String interfaceClassName,
            final AspectDefinition aspectDef) {

        Expression expr = Expression.createRootExpression(
                aspectDef.getName(), expression, PointcutType.CLASS
        );
        final InterfaceIntroductionDefinition introDef = new InterfaceIntroductionDefinition(
                introductionName, expr, interfaceClassName
        );
        return introDef;
    }
}

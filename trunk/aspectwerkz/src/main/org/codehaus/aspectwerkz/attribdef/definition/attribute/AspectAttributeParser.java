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

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.*;

/**
 * Abstract base class for all the aspect attribute parsers to extend.
 * Extracts the aspects attributes and creates a meta-data representation of them.
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
                                                      final String type,
                                                      final String expression,
                                                      final AspectDefinition aspectDef) {
        aspectDef.addPointcut(new PointcutDefinitionImpl(name, type, expression, aspectDef));
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
                adviceName, aspectName, aspectClassName, expression,
                method, methodIndex, aspectDef
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
                adviceName, aspectName, aspectClassName, expression,
                method, methodIndex, aspectDef
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
                adviceName, aspectName, aspectClassName, expression,
                method, methodIndex, aspectDef
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
     * @param aspectDef
     */
    protected void createAndAddIntroductionDefToAspectDef(final String expression,
                                                          final String introductionName,
                                                          final String[] introducedInterfaceNames,
                                                          final Method[] introducedMethods,
                                                          final AspectDefinition aspectDef) {
        IntroductionDefinition introDef = createIntroductionDefinition(
                introductionName,
                expression,
                introducedInterfaceNames,
                introducedMethods,
                aspectDef);
        aspectDef.addInterfaceIntroduction(introDef);
    }

    /**
     * Creates an introduction definition
     *
     * @param introductionName
     * @param expression
     * @param introducedInterfaceNames
     * @param introducedMethods
     * @param aspectDef
     * @return
     */
    private IntroductionDefinition createIntroductionDefinition(
            String introductionName, String expression,
            String[] introducedInterfaceNames, Method[] introducedMethods,
            AspectDefinition aspectDef) {

        final IntroductionDefinition introDef = new IntroductionDefinition(
                introductionName, expression, introducedInterfaceNames, introducedMethods

        );

        try {
            final PointcutDefinition pointcutDef = new PointcutDefinitionImpl(
                    introductionName, PointcutDefinition.CLASS, expression, aspectDef
            );

            final IntroductionWeavingRule introductionWeavingRule = new IntroductionWeavingRule();
            introductionWeavingRule.setExpression(introductionName);

            aspectDef.addPointcut(pointcutDef);
            addPointcutPattern(introductionWeavingRule, pointcutDef);

            introDef.setWeavingRule(introductionWeavingRule);
        }
        catch (Exception e) {
            throw new DefinitionException("definition for introduction [" + introDef.getName() + "] in aspect [" + aspectDef.getName() + "] is not valid: " + e.getMessage());
        }
        return introDef;
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
        IntroductionDefinition introDef = createInterfaceIntroductionDefinition(
                introductionName, expression, interfaceClassName, aspectDef);
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

        final AdviceDefinition adviceDef = new AdviceDefinition(
                adviceName, aspectName, aspectClassName,
                expression, method, methodIndex, aspectDef
        );
        try {
            final AdviceWeavingRule weavingRule = new AdviceWeavingRule(expression);

            for (Iterator it2 = adviceDef.getPointcutRefs().iterator(); it2.hasNext();) {
                String pointcutName = (String)it2.next();
                PointcutDefinition pointcutDef = aspectDef.getPointcutDef(pointcutName);

                // if cflow pointcut set the cflow expression
                if (pointcutDef.isCFlowPointcut()) {
                    weavingRule.setCFlowExpression(pointcutDef.getName());
                }
                else {
                    // set the pointcut type
                    weavingRule.setPointcutType(pointcutDef.getType());
                }

                addPointcutPattern(weavingRule, pointcutDef);
            }
            adviceDef.setWeavingRule(weavingRule);
        }
        catch (Exception e) {
            throw new DefinitionException("definition for advice [" + adviceDef.getName() + "] in aspect [" + aspectDef.getName() + "] is not valid: " + e.getMessage());
        }
        return adviceDef;
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
    protected IntroductionDefinition createInterfaceIntroductionDefinition(
            final String introductionName,
            final String expression,
            final String interfaceClassName,
            final AspectDefinition aspectDef) {

        final IntroductionDefinition introDef = new IntroductionDefinition(
                introductionName, expression, interfaceClassName
        );

        try {
            final PointcutDefinition pointcutDef = new PointcutDefinitionImpl(
                    introductionName, PointcutDefinition.CLASS, expression, aspectDef
            );

            final IntroductionWeavingRule introductionWeavingRule = new IntroductionWeavingRule();
            introductionWeavingRule.setExpression(introductionName);

            aspectDef.addPointcut(pointcutDef);
            addPointcutPattern(introductionWeavingRule, pointcutDef);

//            for (Iterator it2 = introDef.getPointcutRefs().iterator(); it2.hasNext();) {
//                String pointcutName = (String)it2.next();
//                PointcutDefinition pointcutDef = aspectDef.getPointcutDef(pointcutName);
//                addPointcutPattern(introductionWeavingRule, pointcutDef);
//            }
            introDef.setWeavingRule(introductionWeavingRule);
        }
        catch (Exception e) {
            throw new DefinitionException("definition for introduction [" + introDef.getName() + "] in aspect [" + aspectDef.getName() + "] is not valid: " + e.getMessage());
        }
        return introDef;
    }

    /**
     * Adds a pointcut pattern to the weaving rule.
     *
     * @param adviceWeavingRule the weaving rule                               x
     * @param pointcutDef the pointcut definition
     */
    private static void addPointcutPattern(final AdviceWeavingRule adviceWeavingRule,
                                           final PointcutDefinition pointcutDef) {
        if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.METHOD)) {
            adviceWeavingRule.addMethodPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
            adviceWeavingRule.addSetFieldPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.GET_FIELD)) {
            adviceWeavingRule.addGetFieldPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.THROWS)) {
            adviceWeavingRule.addThrowsPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
            adviceWeavingRule.addCallerSidePointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CFLOW)) {
            adviceWeavingRule.addCallerSidePointcutPattern(pointcutDef);
        }
    }

    /**
     * Adds a pointcut pattern to the weaving rule.
     *
     * @param introWeavingRule the weaving rule
     * @param pointcutDef the pointcut definition
     */
    private static void addPointcutPattern(final IntroductionWeavingRule introWeavingRule,
                                           final PointcutDefinition pointcutDef) {
        if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CLASS)) {
            introWeavingRule.addClassPointcutPattern(pointcutDef);
        }
    }
}

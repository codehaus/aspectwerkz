/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.delegation;

import java.util.List;
import java.util.Iterator;

import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.transform.inlining.AspectModelManager;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.annotation.AspectAnnotationParser;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.expression.ExpressionVisitor;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public abstract class AbstractAdviceDelegator implements AdviceDelegator {

    protected final Object ASPECT_INSTANCE;

    protected AdviceDefinition m_adviceDef;

    /**
     * @param aspect
     * @param adviceName
     */
    public AbstractAdviceDelegator(final Object aspect, final String adviceName) {
        ASPECT_INSTANCE = aspect;
        final ClassLoader deployLoader = getClass().getClassLoader();
        final ClassLoader aspectLoader = aspect.getClass().getClassLoader();
        final Class aspectClass = aspect.getClass();
        final ClassInfo aspectClassInfo = JavaClassInfo.getClassInfo(aspectClass);
        final String aspectName = aspectClass.getName();

        final SystemDefinition systemDef = SystemDefinitionContainer.getVirtualDefinitionFor(deployLoader);
        AspectDefinition aspectDef = systemDef.getAspectDefinition(aspectName);
        if (aspectDef == null) {
            aspectDef = new AspectDefinition(aspectName, aspectClassInfo, systemDef);
            AspectModelManager.defineAspect(aspectClassInfo, aspectDef, aspectLoader);
            AspectAnnotationParser.parse(aspectClassInfo, aspectDef, aspectLoader);
            systemDef.addAspectOverwriteIfExists(aspectDef);
        }
        List adviceDefs = aspectDef.getAdviceDefinitions();
        for (Iterator it = adviceDefs.iterator(); it.hasNext();) {
            AdviceDefinition adviceDef = (AdviceDefinition) it.next();
            if (adviceDef.getName().equals(adviceName)) {
                m_adviceDef = adviceDef;
                break;
            }
        }
        if (m_adviceDef == null) {
            throw new DefinitionException(
                    "no advice with name [" + adviceName + "] found in aspect [" + aspectName + ']'
            );
        }
    }

    /**
     * @return
     */
    public Object getAspect() {
        return ASPECT_INSTANCE;
    }

    /**
     * @return
     */
    public AdviceDefinition getAdviceDefinition() {
        return m_adviceDef;
    }

    /**
     * @param advisable
     */
    public void register(final Advisable advisable) {
        ClassInfo classInfo = JavaClassInfo.getClassInfo(advisable.getClass());
        ExpressionVisitor expression = m_adviceDef.getExpressionInfo().getExpression();
        handleConstructors(classInfo.getConstructors(), expression, advisable);
        handleMethods(classInfo.getMethods(), expression, advisable);
        handleFields(classInfo.getFields(), expression, advisable);
    }

    /**
     * @param constructors
     * @param expression
     * @param advisable
     */
    private void handleConstructors(final ConstructorInfo[] constructors,
                                    final ExpressionVisitor expression,
                                    final Advisable advisable) {
        for (int i = 0; i < constructors.length; i++) {
            ConstructorInfo constructor = constructors[i];
            if (expression.match(new ExpressionContext(PointcutType.EXECUTION, constructor, null))) {
                int joinPointIndex = AsmHelper.calculateConstructorHash(constructor.getSignature());
                if (this instanceof AroundAdviceDelegator) {
                    AroundAdviceDelegator aroundAdviceDelegator = (AroundAdviceDelegator) this;
                    AroundAdviceDelegator[] delegators;
                    AroundAdviceDelegator[] oldDelegators = advisable.aw$getAroundAdviceDelegators(joinPointIndex);
                    if (oldDelegators != null) {
                        delegators = new AroundAdviceDelegator[oldDelegators.length + 1];
                        System.arraycopy(oldDelegators, 0, delegators, 0, oldDelegators.length);
                        delegators[delegators.length - 1] = aroundAdviceDelegator;
                    } else {
                        delegators = new AroundAdviceDelegator[]{aroundAdviceDelegator};
                    }
                    advisable.aw$setAroundAdviceDelegators(joinPointIndex, delegators);
                }
            }
        }
    }

    /**
     * @param methods
     * @param expression
     * @param advisable
     */
    private void handleMethods(final MethodInfo[] methods,
                               final ExpressionVisitor expression,
                               final Advisable advisable) {
        for (int i = 0; i < methods.length; i++) {
            MethodInfo method = methods[i];
            if (expression.match(new ExpressionContext(PointcutType.EXECUTION, method, null))) {
                int joinPointIndex = AsmHelper.calculateMethodHash(method.getName(), method.getSignature());
                if (this instanceof AroundAdviceDelegator) {
                    AroundAdviceDelegator aroundAdviceDelegator = (AroundAdviceDelegator) this;
                    AroundAdviceDelegator[] delegators;
                    AroundAdviceDelegator[] oldDelegators = advisable.aw$getAroundAdviceDelegators(joinPointIndex);
                    if (oldDelegators != null) {
                        delegators = new AroundAdviceDelegator[oldDelegators.length + 1];
                        System.arraycopy(oldDelegators, 0, delegators, 0, oldDelegators.length);
                        delegators[delegators.length - 1] = aroundAdviceDelegator;
                    } else {
                        delegators = new AroundAdviceDelegator[]{aroundAdviceDelegator};
                    }
                    advisable.aw$setAroundAdviceDelegators(joinPointIndex, delegators);
                }
            }
        }
    }

    /**
     * @param fields
     * @param expression
     * @param advisable
     */
    private void handleFields(final FieldInfo[] fields,
                              final ExpressionVisitor expression,
                              final Advisable advisable) {
        for (int i = 0; i < fields.length; i++) {
            FieldInfo field = fields[i];
            if (expression.match(new ExpressionContext(PointcutType.EXECUTION, field, null))) {
                int joinPointIndex = AsmHelper.calculateFieldHash(field.getName(), field.getSignature());
                if (this instanceof AroundAdviceDelegator) {
                    AroundAdviceDelegator aroundAdviceDelegator = (AroundAdviceDelegator) this;
                    AroundAdviceDelegator[] delegators;
                    AroundAdviceDelegator[] oldDelegators = advisable.aw$getAroundAdviceDelegators(joinPointIndex);
                    if (oldDelegators != null) {
                        delegators = new AroundAdviceDelegator[oldDelegators.length + 1];
                        System.arraycopy(oldDelegators, 0, delegators, 0, oldDelegators.length);
                        delegators[delegators.length - 1] = aroundAdviceDelegator;
                    } else {
                        delegators = new AroundAdviceDelegator[]{aroundAdviceDelegator};
                    }
                    advisable.aw$setAroundAdviceDelegators(joinPointIndex, delegators);
                }
            }
        }
    }
}

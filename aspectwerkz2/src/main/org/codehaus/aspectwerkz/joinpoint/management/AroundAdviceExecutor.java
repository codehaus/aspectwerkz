/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.joinpoint.FieldSignature;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class AroundAdviceExecutor implements AdviceExecutor {

    /**
     * The index of the current advice.
     */
    private int m_currentAdviceIndex = -1;

    /**
     * The advices indexes.
     */
    private final IndexTuple[] m_adviceIndexes;

    /**
     * The cflow expressions.
     */
    private final List m_cflowExpressions;

    /**
     * The aspect system.
     */
    private final System m_system;

    /**
     * The aspect manager.
     */
    private final AspectManager m_aspectManager;

    /**
     * The join point type.
     */
    private final int m_joinPointType;

    /**
     * Should the executor check the control flow at each invocation?
     */
    private final boolean m_checkCflow;

    /**
     *
     * @param adviceIndexes
     * @param cflowExpressions
     * @param system
     */
    public AroundAdviceExecutor(final IndexTuple[] adviceIndexes,
                                final List cflowExpressions,
                                final System system,
                                final int joinPointType) {
        m_adviceIndexes = adviceIndexes;
        m_cflowExpressions = cflowExpressions;
        m_system = system;
        m_aspectManager = m_system.getAspectManager();
        m_joinPointType = joinPointType;
        m_checkCflow = cflowExpressions.size() != 0;
    }

    /**
     * Executes its advices one by one. After the last advice has been executed, the original method is invoked.
     *
     * @param joinPoint    the current join point
     * @return             the result from the next advice in the chain or the invocation of the target method
     */
    public Object proceed(final JoinPoint joinPoint) throws Throwable {
        if (m_checkCflow) {
            boolean isInCFlow = false;
            for (Iterator it = m_cflowExpressions.iterator(); it.hasNext();) {
                Expression cflowExpression = (Expression)it.next();
                if (m_system.isInControlFlowOf(cflowExpression)) {
                    isInCFlow = true;
                    break;
                }
            }
            if (!isInCFlow) {
                return invokeTargetMethod(joinPoint);
            }
        }

        Object result = null;
        if (m_currentAdviceIndex == m_adviceIndexes.length - 1) {
            m_currentAdviceIndex = - 1;
            try {
                switch (m_joinPointType) {
                    case JoinPointType.FIELD_SET:
                        setTargetField(joinPoint);
                        break;
                    case JoinPointType.FIELD_GET:
                        result = getTargetField(joinPoint);
                        break;
                    default:
                        result = invokeTargetMethod(joinPoint);
                        break;
                }
            }
            finally {
                m_currentAdviceIndex = m_adviceIndexes.length - 1;
            }
        }
        else {
            m_currentAdviceIndex++;
            try {
                IndexTuple index = m_adviceIndexes[m_currentAdviceIndex];
                result = m_aspectManager.getAspect(index.getAspectIndex()).
                        ___AW_invokeAdvice(index.getMethodIndex(), joinPoint);
            }
            finally {
                m_currentAdviceIndex--;
            }
        }
        return result;
    }

    /**
     * Checks if the executor has any advices.
     *
     * @return true if it has advices
     */
    public boolean hasAdvices() {
        return m_adviceIndexes.length != 0;
    }

    /**
     * Creates a deep copy of the advice executor.
     *
     * @return a deep copy of the intance
     */
    public AdviceExecutor newInstance() {
        return new AroundAdviceExecutor(m_adviceIndexes, m_cflowExpressions, m_system, m_joinPointType);
    }

    /**
     * Invokes the origignal method.
     *
     * @param joinPoint the join point instance
     * @return the result from the method invocation
     * @throws Throwable the exception from the original method
     */
    public Object invokeTargetMethod(final JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] parameterValues = signature.getParameterValues();
        Object targetInstance = joinPoint.getTargetInstance();
        try {
            return targetMethod.invoke(targetInstance, parameterValues);
        }
        catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getTargetException());
        }
    }

    /**
     * Sets the target field.
     *
     * @param joinPoint the join point instance
     * @throws Throwable the exception from the original method
     */
    public void setTargetField(final JoinPoint joinPoint) throws Throwable {
        FieldSignature signature = (FieldSignature)joinPoint.getSignature();
        Field targetField = signature.getField();
        Object fieldValue = signature.getFieldValue();
        Object targetInstance = joinPoint.getTargetInstance();
        targetField.set(targetInstance, fieldValue);
    }

    /**
     * Gets the target field.
     *
     * @param joinPoint the join point instance
     * @return the target field
     * @throws Throwable the exception from the original method
     */
    public Object getTargetField(final JoinPoint joinPoint) throws Throwable {
        FieldSignature signature = (FieldSignature)joinPoint.getSignature();
        Field targetField = signature.getField();
        Object targetInstance = joinPoint.getTargetInstance();
        return targetField.get(targetInstance);
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.util.Iterator;
import java.util.List;

import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Handles the execution of the around advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
class AroundAdviceExecutor {

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
     * @param adviceIndexes
     * @param cflowExpressions
     * @param system
     */
    public AroundAdviceExecutor(
            final IndexTuple[] adviceIndexes,
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
     * @param joinPoint the current join point
     * @return the result from the next advice in the chain or the invocation of the target method
     */
    public Object proceed(final JoinPoint joinPoint) throws Throwable {
        Object result = null;
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
                return invokeJoinPoint(joinPoint);
            }
        }
        if (m_currentAdviceIndex == m_adviceIndexes.length - 1) {
            m_currentAdviceIndex = -1;
            try {
                result = invokeJoinPoint(joinPoint);
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

//    /**
//     * Creates a deep copy of the advice executor.
//     *
//     * @return a deep copy of the intance
//     */
//    public AroundAdviceExecutor newInstance() {
//        return new AroundAdviceExecutor(m_adviceIndexes, m_cflowExpressions, m_system, m_joinPointType);
//    }

    /**
     * Invoke the join point.
     *
     * @param joinPoint the join point instance
     * @return the result from the invocation
     * @throws Throwable
     */
    private Object invokeJoinPoint(final JoinPoint joinPoint) throws Throwable {
        Object result = null;
        switch (m_joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
                result = JoinPointBase.invokeTargetMethod(joinPoint);
                break;
            case JoinPointType.METHOD_CALL:
                result = JoinPointBase.invokeTargetMethod(joinPoint);
                break;
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                result = JoinPointBase.invokeTargetConstructorExecution(joinPoint);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
                result = JoinPointBase.invokeTargetConstructorCall(joinPoint);
                break;
            case JoinPointType.FIELD_SET:
                JoinPointBase.setTargetField(joinPoint);
                break;
            case JoinPointType.FIELD_GET:
                result = JoinPointBase.getTargetField(joinPoint);
                break;
        }
        return result;
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.util.List;
import java.util.Iterator;

import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class BeforeAdviceExecutor implements AdviceExecutor {

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
     * Should the executor check the control flow at each invocation?
     */
    private final boolean m_checkCflow;

    /**
     *
     * @param adviceIndexes
     * @param cflowExpressions
     * @param system
     */
    public BeforeAdviceExecutor(final IndexTuple[] adviceIndexes,
                                final List cflowExpressions,
                                final System system) {
        m_adviceIndexes = adviceIndexes;
        m_cflowExpressions = cflowExpressions;
        m_system = system;
        m_checkCflow = cflowExpressions.size() != 0;
    }

    /**
     * Executes its advices one by one. After the last advice has been executed, the original method is invoked.
     *
     * @param joinPoint    the current join point
     * @return             null
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
                return null;
            }
        }
        for (int i = 0, j = m_adviceIndexes.length; i < j; i++) {
            IndexTuple index = m_adviceIndexes[i];
            int aspectIndex = index.getAspectIndex();
            int methodIndex = index.getMethodIndex();
            m_system.getAspectManager().getAspect(aspectIndex).
                    ___AW_invokeAdvice(methodIndex, joinPoint);
        }
        return null;
    }

    /**
     * Creates a deep copy of the advice executor.
     *
     * @return a deep copy of the intance
     */
    public AdviceExecutor deepCopy() {
        return new BeforeAdviceExecutor(m_adviceIndexes, m_cflowExpressions, m_system);
    }
}

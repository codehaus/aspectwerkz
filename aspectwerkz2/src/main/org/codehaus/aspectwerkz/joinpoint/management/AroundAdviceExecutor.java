/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
     * The aspect system.
     */
    private final System m_system;

    /**
     *
     * @param adviceIndexes
     * @param system
     */
    public AroundAdviceExecutor(final IndexTuple[] adviceIndexes, final System system) {
        m_adviceIndexes = adviceIndexes;
        m_system = system;
    }

    /**
     * Executes its advices one by one. After the last advice has been executed, the original method is invoked.
     *
     * @param joinPoint    the current join point
     * @return             the result from the next advice in the chain or the invocation of the target method
     */
    public Object proceed(final JoinPoint joinPoint) throws Throwable {
        m_currentAdviceIndex++;
        if (m_currentAdviceIndex < m_adviceIndexes.length) {
            IndexTuple index = m_adviceIndexes[m_currentAdviceIndex];
            int aspectIndex = index.getAspectIndex();
            int methodIndex = index.getMethodIndex();
            Object result = m_system.getAspectManager().getAspect(aspectIndex).
                    ___AW_invokeAdvice(methodIndex, joinPoint);
            return result;
        }
        else {
            return invokeTargetMethod(joinPoint);
        }
    }

    /**
     * Creates a deep copy of the advice executor.
     *
     * @return a deep copy of the intance
     */
    public AdviceExecutor deepCopy() {
        return new AroundAdviceExecutor(m_adviceIndexes, m_system);
    }

    /**
     * Invokes the origignal method.
     *
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
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.expression.CflowExpressionVisitor;
import org.codehaus.aspectwerkz.joinpoint.ConstructorSignature;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorRttiImpl;

/**
 * Abstraction of a constructor join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
class ConstructorJoinPoint extends JoinPointBase {
    private final ConstructorSignature m_signature;
    private final ConstructorRttiImpl m_rtti;

    /**
     * Creates a new constructor join point.
     *
     * @param type
     * @param targetClass
     * @param signature
     * @param rtti
     * @param cflowExpressions
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public ConstructorJoinPoint(final int type, final Class targetClass, final Signature signature, final Rtti rtti,
                                final CflowExpressionVisitor[] cflowExpressions,
                                final AroundAdviceExecutor aroundAdviceExecutor,
                                final BeforeAdviceExecutor beforeAdviceExecutor,
                                final AfterAdviceExecutor afterAdviceExecutor) {
        super(type, targetClass, cflowExpressions, aroundAdviceExecutor, beforeAdviceExecutor, afterAdviceExecutor);
        m_signature = (ConstructorSignature)signature;
        m_rtti = (ConstructorRttiImpl)rtti;
    }

    /**
     * Walks through the pointcuts and invokes all its advices. When the last advice of the last pointcut has been
     * invoked, the original method is invoked. Is called recursively.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
        final Object result = m_aroundAdviceExecutor.proceed(this);
        m_rtti.setNewInstance(result);
        return result;
    }

    /**
     * Returns the signature for the join point.
     *
     * @return the signature
     */
    public Signature getSignature() {
        return m_signature;
    }

    /**
     * Returns the RTTI for the join point.
     *
     * @return the RTTI
     */
    public Rtti getRtti() {
        return m_rtti;
    }

    /**
     * Returns a string representation of the join point.
     *
     * @return a string representation
     * @TODO: implement toString to something meaningful
     */
    public String toString() {
        return super.toString();
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.joinpoint.ConstructorSignature;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorRttiImpl;

/**
 * Abstraction of a constructor join point.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
class ConstructorJoinPoint extends JoinPointBase {
    private final ConstructorSignature m_signature;

    private transient ConstructorRttiImpl m_rtti;

    /**
     * Creates a new constructor join point.
     * 
     * @param type
     * @param targetClass
     * @param signature
     * @param rtti
     * @param joinPointMetaData
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public ConstructorJoinPoint(final int type,
                                final Class targetClass,
                                final Signature signature,
                                final Rtti rtti,
                                final JoinPointMetaData joinPointMetaData,
                                final AroundAdviceExecutor aroundAdviceExecutor,
                                final BeforeAdviceExecutor beforeAdviceExecutor,
                                final AfterAdviceExecutor afterAdviceExecutor) {
        super(type, targetClass, joinPointMetaData, aroundAdviceExecutor, beforeAdviceExecutor, afterAdviceExecutor);
        m_signature = (ConstructorSignature) signature;
        m_rtti = (ConstructorRttiImpl) rtti;
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
        this.getRtti().cloneFor(result, this.getRtti().getThis());//target is assigned at this point as well
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

    public Object[] extractArguments(int[] methodToArgIndexes) {
        // special handling for XML defined aspect, the old way, where we assume (JoinPoint) is sole arg
        if (methodToArgIndexes.length <= 0) {
            return new Object[]{this};
        }

        Object[] args = new Object[methodToArgIndexes.length];
        for (int i = 0; i < args.length; i++) {
            int argIndex = methodToArgIndexes[i];
            if (argIndex != -1) {
                args[i] = m_rtti.getParameterValues()[argIndex];
            } else {
                // assume for now -1 is JoinPoint - TODO: evolve for staticJP
                args[i] = this;
            }
        }
        return args;
    }

    /**
     * Allows to pass the RTTI to the JP. The JPBase implementation delegates getTarget to the RTTI.
     * Since in 1.0 engine, JP are cached and shared, while the RTTI is not, we need to set the RTTI (AW-265).
     * This method MUST not be called by the user.
     *
     * @param rtti
     */
    public void setRtti(Rtti rtti) {
        m_rtti = (ConstructorRttiImpl)rtti;
    }

}
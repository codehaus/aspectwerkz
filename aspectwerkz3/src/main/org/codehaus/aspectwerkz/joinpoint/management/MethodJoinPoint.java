/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodRttiImpl;
import java.util.List;
import java.io.ObjectInputStream;

/**
 * Abstraction of a method join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
class MethodJoinPoint extends JoinPointBase {

    private MethodSignature m_signature;
    private MethodRttiImpl m_rtti;

    /**
     * Creates a new join point.
     *
     * @param uuid
     * @param type
     * @param targetClass
     * @param signature
     * @param rtti
     * @param cflowExpressions
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public MethodJoinPoint(final String uuid, final int type, final Class targetClass, final Signature signature,
                           final Rtti rtti, final List cflowExpressions,
                           final AroundAdviceExecutor aroundAdviceExecutor,
                           final BeforeAdviceExecutor beforeAdviceExecutor,
                           final AfterAdviceExecutor afterAdviceExecutor) {
        super(uuid, type, targetClass, cflowExpressions, aroundAdviceExecutor, beforeAdviceExecutor, afterAdviceExecutor);
        m_signature = (MethodSignature)signature;
        m_rtti = (MethodRttiImpl)rtti;
    }

    /**
     * Walks through the pointcuts and invokes all its advices. When the last advice of the last pointcut has been
     * invoked, the original method is invoked. Is called recursively.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
        Object result = m_aroundAdviceExecutor.proceed(this);

        m_rtti.setReturnValue(result);

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
     * Returns a string representation of the join puoint.
     *
     * @return a string representation
     * @TODO: implement toString to something meaningful
     */
    public String toString() {
        return super.toString();
    }

//    /**
//     * Provides custom deserialization.
//     *
//     * @param stream the object input stream containing the serialized object
//     * @throws Exception in case of failure
//     */
//    private void readObject(final ObjectInputStream stream) throws Exception {
//        ObjectInputStream.GetField fields = stream.readFields();
//        m_rtti = (MethodRttiImpl)fields.get("m_rtti", null);
//        m_signature = (MethodSignature)fields.get("m_signature", null);
//    }
}
/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.advice;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Is invoked at the join points where a specific exception is thrown out of a method.
 * <p/>
 * Supports four different deployment models:
 *     PER_JVM, PER_CLASS, PER_INSTANCE A PER_THREAD.<br/>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization A object creation is needed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public abstract class ThrowsAdvice extends AbstractAdvice {

    /**
     * Sets the class of the class.
     */
    public ThrowsAdvice() {
        super();
    }

    /**
     * Callback method.
     * To be implemented by the user.
     *
     * @param joinPoint the join point the advice is executing at
     */
    public abstract void execute(final JoinPoint joinPoint) throws Throwable;

    /**
     * Executes the current advice A then redirects to the next advice in the
     * chain.
     * Callback method for the framework.
     *
     * @param joinPoint the join point the advice is currently executing at
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object doExecute(final JoinPoint joinPoint) throws Throwable {
        switch (m_deploymentModel) {
            case DeploymentModel.PER_JVM:
                execute(joinPoint);
                break;

            case DeploymentModel.PER_CLASS:
                ((ThrowsAdvice)getPerClassAdvice(joinPoint)).execute(joinPoint);
                break;

            case DeploymentModel.PER_INSTANCE:
                ((ThrowsAdvice)getPerInstanceAdvice(joinPoint)).execute(joinPoint);
                break;

            case DeploymentModel.PER_THREAD:
                ((ThrowsAdvice)getPerThreadAdvice()).execute(joinPoint);
                break;

            default:
                break;
        }
        return joinPoint.proceed();
    }
}

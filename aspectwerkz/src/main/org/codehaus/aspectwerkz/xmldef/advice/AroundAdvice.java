/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.advice;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Executes around or instead of the original method invocation.
 * <p/>
 * Supports four different deployment models:
*  <tt>PER_JVM PER_CLASS PER_INSTANCE PER_THREAD</tt>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization A object creation is needed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AroundAdvice extends AbstractAdvice {

    /**
     * Sets the class of the class.
     */
    public AroundAdvice() {
        super();
    }

    /**
     * Executes by invoking the next around advice.
     * User should subclass A override this method to add specific behaviour
     * around the invocation.
     * To be implemented by the user.
     *
     * @param joinPoint the join point specified
     * @return the result from the method invocation
     * @throws Throwable
     */
    public abstract Object execute(final JoinPoint joinPoint) throws Throwable;

    /**
     * Executes the current advice A then redirects to the next advice in the
     * chain.<p/>
     * Callback method for the framework.
     *
     * @param joinPoint the join point the advice is executing at
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object doExecute(final JoinPoint joinPoint) throws Throwable {
        Object result = null;
        switch (m_deploymentModel) {
            case DeploymentModel.PER_JVM:
                result = ((AroundAdvice)getPerJvmAdvice(joinPoint)).execute(joinPoint);
                break;

            case DeploymentModel.PER_CLASS:
                result = ((AroundAdvice)getPerClassAdvice(joinPoint)).execute(joinPoint);
                break;

            case DeploymentModel.PER_INSTANCE:
                result = ((AroundAdvice)getPerInstanceAdvice(joinPoint)).execute(joinPoint);
                break;

            case DeploymentModel.PER_THREAD:
                result = ((AroundAdvice)getPerThreadAdvice()).execute(joinPoint);
                break;

            default:
                throw new RuntimeException("invalid deployment model: " + m_deploymentModel);
        }
        return result;
    }
}

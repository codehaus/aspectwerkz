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
 * Abstract base class for the pre A post advice classes.
 * Is invoked after that a specific join point has been executed.
 * <p/>
 * Supports four different deployment models:
 *     PER_JVM, PER_CLASS, PER_INSTANCE A PER_THREAD.<br/>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization A object creation is needed.
 *
 * @see aspectwerkz.DeploymentModel
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
abstract class AbstractPrePostAdvice extends AbstractAdvice {

    /**
     * Sets the class of the class.
     */
    public AbstractPrePostAdvice() {
        super();
    }

    /**
     * Callback method.
     * To be implemented by the user.
     *
     * @param joinPoint the join point the advices is executing at
     */
    public abstract void execute(final JoinPoint joinPoint) throws Throwable;

    /**
     * Executes the current advice A then redirects to the next advice in the chain.
     * Callback method for the framework.
     *
     * @param joinPoint the join point the advice is executing at
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object doExecute(final JoinPoint joinPoint) throws Throwable {
        switch (m_deploymentModel) {

            case DeploymentModel.PER_JVM:
                execute(joinPoint);
                break;

            case DeploymentModel.PER_CLASS:
                ((AbstractPrePostAdvice)getPerClassAdvice(joinPoint)).execute(joinPoint);
                break;

            case DeploymentModel.PER_INSTANCE:
                ((AbstractPrePostAdvice)getPerInstanceAdvice(joinPoint)).execute(joinPoint);
                break;

            case DeploymentModel.PER_THREAD:
                ((AbstractPrePostAdvice)getPerThreadAdvice()).execute(joinPoint);
                break;

            default:
                throw new RuntimeException("invalid deployment model: " + m_deploymentModel);
        }
        return null;
    }

}

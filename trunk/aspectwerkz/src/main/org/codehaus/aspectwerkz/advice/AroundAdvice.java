/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.advice;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Executes around or instead of the original method invocation.
 * <p/>
 * Supports four different deployment models:
 *     PER_JVM, PER_CLASS, PER_INSTANCE and PER_THREAD.<br/>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization and object creation is needed.
 *
 * @see aspectwerkz.DeploymentModel
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AroundAdvice.java,v 1.3 2003-06-30 15:55:25 jboner Exp $
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
     * User should subclass and override this method to add specific behaviour
     * around the invocation.
     * To be implemented by the user.
     *
     * @param joinPoint the join point specified
     * @return the result from the method invocation
     * @throws Throwable
     */
    public abstract Object execute(final JoinPoint joinPoint) throws Throwable;

    /**
     * Executes the current advice and then redirects to the next advice in the
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

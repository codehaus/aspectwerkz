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

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Default implementation of the around advice.
 * <p/>
 * Only to be used as a container for pre and post advices.
 * <p/>
 * Executes it's pre and post advices and delegates in between
 * direcly to the next around advice in the chain.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: DefaultAroundAdvice.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class DefaultAroundAdvice extends AroundAdvice {

    /**
     * Creates a new default around advice.
     */
    public DefaultAroundAdvice() {
        super();
    }

    /**
     * Delegates directly to the next advice in the chain.
     *
     * @param joinPoint the join point for the pointcut
     * @return the result from the method invocation
     * @throws Throwable
     */
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}

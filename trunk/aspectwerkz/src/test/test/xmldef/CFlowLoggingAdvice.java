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
package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: CFlowLoggingAdvice.java,v 1.1 2003-06-30 15:56:51 jboner Exp $
 */
public class CFlowLoggingAdvice extends AroundAdvice {

    public CFlowLoggingAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;

        System.out.println("jp.getMethodName() = " + jp.getMethodName());

        ((Loggable)jp.getTargetObject()).log("advice-before ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetObject()).log("advice-after ");
        return result;
    }
}

/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
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
package examples.logging;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * This advice implements a simple logging service.<br/>
 *
 * It logs the entry and exit of the methods that are picked out
 * by the pointcuts mapped to this advice.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: LoggingAdvice.java,v 1.4 2003-06-17 15:50:57 jboner Exp $
 *
 * @advice-def name=log
 *             deployment-model=perJVM
 *             attribute=log
 * @advice-param advice-ref=log
 *               name=param
 *               value=value
 */
public class LoggingAdvice extends AroundAdvice {

    public LoggingAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        System.out.println("param to advice = " + getParameter("param"));
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        System.out.println("--> " + jp.getTargetClass().getName() + "::" + jp.getMethodName());
        final Object result = joinPoint.proceed();
        System.out.println("<-- " + jp.getTargetClass().getName() + "::" + jp.getMethodName());
        return result;
    }
}

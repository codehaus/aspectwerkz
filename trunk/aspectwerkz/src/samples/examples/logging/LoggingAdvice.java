/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
 * It logs the entry A exit of the methods that are picked out
 * by the pointcuts mapped to this advice.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: LoggingAdvice.java,v 1.10 2003-07-08 18:49:46 jboner Exp $
 *
 * @aspectwerkz.advice-def name=log
 *                         deployment-model=perJVM
 *                         attribute=log
 * @aspectwerkz.advice-param advice-ref=log
 *                           name=param
 *                           value=value
 */
public class LoggingAdvice extends AroundAdvice {

    private int m_level = 0;

    public LoggingAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
//        System.out.println("param to advice = " + getParameter("param"));
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        indent();
        System.out.println("--> " + jp.getTargetClass().getName() + "::" + jp.getMethodName());
        m_level++;
        final Object result = joinPoint.proceed();
        m_level--;
        indent();
        System.out.println("<-- " + jp.getTargetClass().getName() + "::" + jp.getMethodName());
        return result;
    }

    private void indent() {
        for (int i = 0; i < m_level; i++) {
            System.out.print("  ");
        }
    }
}

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
package examples.persistence;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @advice-def name=counter3
 *             deployment-model=perJVM
 *             persistent=true
 *             attribute=counter3
 * @advice-param advice-ref=counter3
 *               name=param1
 *               value=value1
 * @advice-param advice-ref=counter3
 *               name=param2
 *               value=value2
 */
public class InvocationCounterAdvice extends AroundAdvice {

    private int m_counter = 0;

    public InvocationCounterAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
//        System.out.println("parameter 1= " + getParameter("param1"));
//        System.out.println("parameter 2 = " + getParameter("param2"));
        m_counter++;
        System.out.println("advice - persistent int field: "+ m_counter);
        return joinPoint.proceed();
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.bindingsyntax;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class TestAspect {
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        String last = AdviceBindingTest.flow;
        AdviceBindingTest.flow += " ";

        //System.out.println(AdviceBindingTest.flow + " -> Advice_1");
        Object r = joinPoint.proceed();

        //System.out.println(AdviceBindingTest.flow + " <- Advice_1");
        AdviceBindingTest.flow = last;
        return '1' + (String) r;
    }

    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        String last = AdviceBindingTest.flow;
        AdviceBindingTest.flow += " ";

        //System.out.println(AdviceBindingTest.flow + " -> Advice_2");
        Object r = joinPoint.proceed();

        //System.out.println(AdviceBindingTest.flow + " <- Advice_2");
        AdviceBindingTest.flow = last;
        return '2' + (String) r;
    }
}
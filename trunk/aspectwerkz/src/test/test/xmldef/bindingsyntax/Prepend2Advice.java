/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.bindingsyntax;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class Prepend2Advice extends AroundAdvice {

    public Object execute(JoinPoint jp) throws Throwable {
        String last = AdviceBindingTest.flow;
        AdviceBindingTest.flow+=" ";
        //System.out.println(AdviceBindingTest.flow + " -> Advice_2");
        Object r = jp.proceed();
        //System.out.println(AdviceBindingTest.flow + " <- Advice_2");
        AdviceBindingTest.flow = last;
        return "2"+(String)r;
    }
}

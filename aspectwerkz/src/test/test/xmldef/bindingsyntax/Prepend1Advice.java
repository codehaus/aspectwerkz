/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.bindingsyntax;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class Prepend1Advice extends AroundAdvice {

    public Object execute(JoinPoint jp) throws Throwable {
        String last = AdviceBindingTest.flow;
        AdviceBindingTest.flow+=" ";
        //System.out.println(AdviceBindingTest.flow + " -> Advice_1");
        Object r = jp.proceed();
        //System.out.println(AdviceBindingTest.flow + " <- Advice_1");
        AdviceBindingTest.flow = last;
        return "1"+(String)r;
    }
}

package test;

import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
public class MyPreAdvice3 extends PreAdvice {
    public MyPreAdvice3() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        FieldAdviceTest.log("pre2 ");
        CallerSideAdviceTest.log("pre2 ");
    }
}

package test;

import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
public class MyPostAdvice2 extends PostAdvice {
    public MyPostAdvice2() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        FieldAdviceTest.log("post1 ");
        CallerSideAdviceTest.log("post1 ");
    }
}

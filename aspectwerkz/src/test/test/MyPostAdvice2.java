/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import org.codehaus.aspectwerkz.xmldef.advice.PostAdvice;
import org.codehaus.aspectwerkz.xmldef.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class MyPostAdvice2 extends PostAdvice {
    public MyPostAdvice2() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        FieldAdviceTest.log("post1 ");
        CallerSideAdviceTest.log("post1 ");
    }
}

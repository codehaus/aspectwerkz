/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.staticfield;

import org.codehaus.aspectwerkz.xmldef.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;

/**
 * Test case for AW-92
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MyPostAdvice1 extends PostAdvice {

    public MyPostAdvice1() {
        super();
    }

    public void execute(final JoinPoint joinPoint) throws Throwable {
        FieldJoinPoint fjp = (FieldJoinPoint)joinPoint;

        CollectionFieldTest.s_log += "MyPostAdvice1 ";
    }
}

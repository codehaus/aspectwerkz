/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
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
public class MyPostAdvice2 extends PostAdvice {

    public MyPostAdvice2() {
        super();
    }

    public void execute(final JoinPoint joinPoint) throws Throwable {
        FieldJoinPoint fjp = (FieldJoinPoint)joinPoint;

        //todo redo check runtime type

        CollectionFieldTest.s_log += "MyPostAdvice2 ";
    }
}

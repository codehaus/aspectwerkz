/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.staticfield;

import org.codehaus.aspectwerkz.xmldef.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MemberFieldSetJoinPoint;

/**
 * Test case for AW-92
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MyPreAdvice2 extends PreAdvice {

    public MyPreAdvice2() {
        super();
    }

    public void execute(final JoinPoint joinPoint) throws Throwable {
        FieldJoinPoint fjp = (FieldJoinPoint)joinPoint;

        // if member field not handled correctly but handled as a static field
        // this will throw a class cast exception
        MemberFieldSetJoinPoint mfjp = (MemberFieldSetJoinPoint)fjp;

        CollectionFieldTest.s_log += "MyPreAdvice2 ";
    }
}

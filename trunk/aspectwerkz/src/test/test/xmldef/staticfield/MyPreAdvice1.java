/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.staticfield;

import org.codehaus.aspectwerkz.xmldef.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.StaticFieldSetJoinPoint;

/**
 * Test case for AW-92
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MyPreAdvice1 extends PreAdvice {

    public MyPreAdvice1() {
        super();
    }

    public void execute(final JoinPoint joinPoint) throws Throwable {
        FieldJoinPoint fjp = (FieldJoinPoint)joinPoint;

        // if static field not handled correctly but handled as a member field
        // this will throw a class cast exception
        StaticFieldSetJoinPoint sgfjp = (StaticFieldSetJoinPoint)fjp;

        CollectionFieldTest.s_log += "MyPreAdvice1 ";
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import org.codehaus.aspectwerkz.xmldef.advice.ThrowsAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.ThrowsJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MyThrowsAdvice1 extends ThrowsAdvice {

    public MyThrowsAdvice1() {
        super();
    }

    public void execute(final JoinPoint joinPoint) throws Throwable {
        // Needs to cast to the correct join point.
        // A bit tedious but optimizes the performance since I otherwise need to perform a cast at EVERY invocation
        throw new test.xmldef.TestException(((ThrowsJoinPoint)joinPoint).getMessage());
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.handler;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.CatchClauseRtti;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class HandlerTestAspect {
    public void before(final JoinPoint joinPoint) throws Throwable {
        HandlerTest.log("pre ");
        // AW-276 access the rtti
        Throwable t = (Throwable) ((CatchClauseRtti) joinPoint.getRtti()).getParameterValue();
        if (t == null) {
            TestCase.fail("handler join point has invalid rttit");
        }
    }
}
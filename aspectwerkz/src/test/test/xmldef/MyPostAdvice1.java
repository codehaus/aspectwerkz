/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import org.codehaus.aspectwerkz.xmldef.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class MyPostAdvice1 extends PostAdvice {
    public MyPostAdvice1() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
    }
}

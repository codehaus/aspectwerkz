/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyPreAdvice { //extends PreAdvice {
    public MyPreAdvice() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        System.out.println("MyPreAdvice called");
    }
}

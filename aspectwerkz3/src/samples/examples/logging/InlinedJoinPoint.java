/***************************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved. *
 * http://aspectwerkz.codehaus.org *
 * ---------------------------------------------------------------------------------- * The software
 * in this package is published under the terms of the LGPL license * a copy of which has been
 * included with this distribution in the license.txt file. *
 **************************************************************************************************/
package examples.logging;

public class InlinedJoinPoint extends InlinedJoinPointBase {

    private Target m_target;

    private Target m_this;

    private int m_stackFrame = -1;

    private int m_i;

  public static final int invoke(int i, Target targetInstance) throws Throwable {
        InlinedJoinPoint joinPoint = new InlinedJoinPoint();
//        joinPoint.m_target = targetInstance;
        joinPoint.m_i = i;
        return joinPoint.proceed();
    }

    public final int proceed() throws Throwable {
        return m_target.toLog1(m_i);
    }
}
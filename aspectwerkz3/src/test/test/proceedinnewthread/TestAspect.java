/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.proceedinnewthread;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class TestAspect {

    public Object advice1(final JoinPoint jp) throws Throwable {
        new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            ProceedTest.LOG += "advice1Pre ";
                            jp.proceed();
                            ProceedTest.LOG += "advice1Post ";
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        ).start();
        return null;
    }

    public Object advice2(final JoinPoint jp) throws Throwable {
        ProceedTest.LOG += "advice2Pre ";
        jp.proceed();
        ProceedTest.LOG += "advice2Post ";
        return null;
    }

    public Object advice3(final JoinPoint jp) throws Throwable {
        ProceedTest.LOG += "advice3Pre ";
        jp.proceed();
        ProceedTest.LOG += "advice3Post ";
        return null;
    }
}

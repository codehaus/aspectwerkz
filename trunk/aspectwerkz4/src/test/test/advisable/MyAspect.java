/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.advisable;

import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class MyAspect {

    /**
     * @Around execution(* *..*.adviseWithAround(..))
     */
    public Object around1(StaticJoinPoint jp) throws Throwable {
        AdvisableTest.log("around1_pre ");
        Object result = jp.proceed();
        AdvisableTest.log("around1_post ");
        return result;
    }

    /**
     * @Around execution(* *..*.adviseWithAroundStack(..))
     */
    public Object around2(StaticJoinPoint jp) throws Throwable {
        AdvisableTest.log("around2_pre ");
        Object result = jp.proceed();
        AdvisableTest.log("around2_post ");
        return result;
    }

    /**
     * @Around execution(* *..*.adviseWithAroundStack(..))
     */
    public Object around3(StaticJoinPoint jp) throws Throwable {
        AdvisableTest.log("around3_pre ");
        Object result = jp.proceed();
        AdvisableTest.log("around3_post ");
        return result;
    }

    public void before() {
        AdvisableTest.log("before ");
    }
}

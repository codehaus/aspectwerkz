/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class ArgAspect {

    /**
     * @Before methodsToLogPC(ai, as)
     */
    public void beforeWithArgs(final JoinPoint joinPoint, int ai, String as) throws Throwable {
        System.out.println("== ==> ArgAspect.beforeWithArgs " + joinPoint + ", " + ai + ", " + as);
    }

    /**
     * @After methodsToLogPC(ai, as)
     */
    public void afterWithArgs(final JoinPoint joinPoint, int ai, String as) throws Throwable {
        System.out.println("== ==> ArgAspect.afterWithArgs " + joinPoint + ", " + ai + ", " + as);
    }

    /**
     * @Before methodsToLogPC(ai, as)
     */
    public void beforeWithArgs2(final JoinPoint joinPoint, String as, int ai) throws Throwable {
        System.out.println("== ==> ArgAspect.beforeWithArgs2 " + joinPoint + ", " + as + ", " + ai);
    }

    /**
     * @After methodsToLogPC(ai, as)
     */
    public void afterWithArgs2(final JoinPoint joinPoint, String as, int ai) throws Throwable {
        System.out.println("== ==> ArgAspect.afterWithArgs2 " + joinPoint + ", " + as + ", " + ai);
    }

    /**
     * Before methodsToLogPC2(sarr)
     */
    public void beforeWithArgs3(final JoinPoint joinPoint, String[] sarr) throws Throwable {
        System.out.println("== ==> ArgAspect.beforeWithArgs3 " + joinPoint + ", " + sarr);
    }

    /**
     * After methodsToLogPC2(sarr)
     */
    public void afterWithArgs3(final JoinPoint joinPoint, String[] sarr) throws Throwable {
        System.out.println("== ==> ArgAspect.afterWithArgs3 " + joinPoint + ", " + sarr);
    }

    /**
     * @Expression execution(* ..ArgLoggingTarget.toLog*(..)) && args(int, s, i)
     */
    Pointcut methodsToLogPC(int i, String s) {
        return null;
    }

    /**
     * Expression execution(* ..ArgLoggingTarget.toLog*(..)) && args(int, sarr)
     */
//    Pointcut methodsToLogPC2(int i, String[] sarr) {
//        return null;
//    }

    //TODO if not abstract, then must be "void"
    //TODO: decide - should we ignore abstract marked pc annotation ??
    //FOR NOW: grab em all, ignore return type, and abstract or not.
    //void methodsToLogPC(String s) {};
    // in fact could be ANY method...

    //TODO anonymous one
    //@Before execution(* *..*(..)) && args(s, String)
    //void beforeAdviceWithArgs(String s) { . .. the advice body }

    /**
     * Expression execution(* *..*(..)) && args(s, String)
     */
    //abstract Pointcut methodsToLogPC(int s);
    //TODO: do we allow that ? AJ does not (pc name must be unique in AJ no matter sig)
    //NOTE: AJ supports args(bindedName, Type), where Type acts at the matching level
    //but not at the advice level
    //we cannot do that since we don't have the bindedName in the matching advice sig
    //unless we do some mixed syntax: args(String, int s). s is named, String is unamed
    //thus advice would be (int s)
    //DO it later.

    //methodsToLog(String s) {};
}
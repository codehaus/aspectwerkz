/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.annotation.Annotation;
import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class ArgAspect {

    /**
     * @Before pc1(ai, as)
     */
    public void before1(final JoinPoint joinPoint, int ai, String[] as) throws Throwable {
        MethodSignature sig = (MethodSignature)joinPoint.getSignature();
        Annotation a = sig.getAnnotation("Annotation");
        System.out.println("== ==> ArgAspect.before " + joinPoint + ", " + ai + ", " + as);
    }

    /**
     * @After pc1(ai, as)
     */
    public void after1(final JoinPoint joinPoint, int ai, String[] as) throws Throwable {
        System.out.println("== ==> ArgAspect.after " + joinPoint + ", " + ai + ", " + as);
    }

    /**
     * Before pc1(ai, as)
     */
    public void before2(final JoinPoint joinPoint, String[] as, int ai) throws Throwable {
        System.out.println("== ==> ArgAspect.before2 " + joinPoint + ", " + as + ", " + ai);
    }

    /**
     * After pc1(ai, as)
     */
    public void after2(final JoinPoint joinPoint, String[] as, int ai) throws Throwable {
        System.out.println("== ==> ArgAspect.after2 " + joinPoint + ", " + as + ", " + ai);
    }

    /**
     * Before pc2(sarr)
     */
    public void before3(final JoinPoint joinPoint, String[] sarr) throws Throwable {
        System.out.println("== ==> ArgAspect.before3 " + joinPoint + ", " + sarr);
    }

    /**
     * After pc2(sarr)
     */
    public void after3(final JoinPoint joinPoint, String[] sarr) throws Throwable {
        System.out.println("== ==> ArgAspect.after3 " + joinPoint + ", " + sarr);
    }

    /**
     * @Expression execution(* ..ArgLoggingTarget.toLog*(..)) && args(int, s, i)
     */
    Pointcut pc1(int i, String[] s) {
        return null;
    }

    /**
     * Expression execution(* ..ArgLoggingTarget.toLog*(..)) && args(int, sarr)
     */
    Pointcut pc2(int i, String[] sarr) {
        return null;
    }

    //TODO if not abstract, then must be "void"
    //TODO: decide - should we ignore abstract marked pc annotation ??
    //FOR NOW: grab em all, ignore return type, and abstract or not.
    //void pc1(String s) {};
    // in fact could be ANY method...

    //TODO anonymous one
    //@Before execution(* *..*(..)) && args(s, String)
    //void beforeAdvice(String s) { . .. the advice body }

    /**
     * Expression execution(* *..*(..)) && args(s, String)
     */
    //abstract Pointcut pc1(int s);
    //TODO: do we allow that ? AJ does not (pc name must be unique in AJ no matter sig)
    //NOTE: AJ supports args(bindedName, Type), where Type acts at the matching level
    //but not at the advice level
    //we cannot do that since we don't have the bindedName in the matching advice sig
    //unless we do some mixed syntax: args(String, int s). s is named, String is unamed
    //thus advice would be (int s)
    //DO it later.

    //methodsToLog(String s) {};
}
     /**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.annotation.Annotation;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class ArgAspect {

    private int m_level = 0;

    /**
     * @Around pc1(ai, as)
     */
    public Object around1(final JoinPoint joinPoint, int ai, String as) throws Throwable {
        indent();
        m_level++;
        System.out.println(" ==> around1 -- pre " + ai + ", " + as);
        Object result = joinPoint.proceed();
        m_level--;
        indent();
        System.out.println(" ==> around1 -- post " + ai + ", " + as);
        return result;
    }

    /**
     * @Before pc1(ai, as)
     */
    public void before1(final JoinPoint joinPoint, int ai, String as) throws Throwable {
        indent();
        m_level++;
        System.out.println(" ==> before1: " + ai + ", " + as);
    }

    /**
     * @After pc1(ai, as)
     */
    public void after1(final JoinPoint joinPoint, int ai, String as) throws Throwable {
        m_level--;
        indent();
        System.out.println(" ==> after1: " + ai + ", " + as);
    }

    /**
     * @Before pc1(ai, as)
     */
    public void before2(final JoinPoint joinPoint, String as, int ai) throws Throwable {
        indent();
        m_level++;
        System.out.println(" ==> before2: " + as + ", " + ai);
    }

    /**
     * @After pc1(ai, as)
     */
    public void after2(final JoinPoint joinPoint, String as, int ai) throws Throwable {
        m_level--;
        indent();
        System.out.println(" ==> after2: " + as + ", " + ai);
    }

    /**
     * @Around pc2(sarr)
     */
    public Object around3(final JoinPoint joinPoint, String[] sarr) throws Throwable {
        indent();
        m_level++;
        System.out.println("==> around3 -- pre " + sarr);
        Object result = joinPoint.proceed();
        m_level--;
        indent();
        System.out.println("==> around3 -- post " + sarr);
        return result;
    }

    /**
     * @Before pc2(sarr)
     */
    public void before3(final JoinPoint joinPoint, String[] sarr) throws Throwable {
        indent();
        m_level++;
        System.out.println("==> before3: " + sarr);
    }

    /**
     * @After pc2(sarr)
     */
    public void after3(final JoinPoint joinPoint, String[] sarr) throws Throwable {
        m_level--;
        indent();
        System.out.println("==> after3: " + sarr);
    }

    /**
     * @Expression execution(* ..ArgLoggingTarget.toLog*(..)) && args(int, s, i)
     */
    Pointcut pc1(int i, String s) {
        return null;
    }

    /**
     * @Expression execution(* ..ArgLoggingTarget.toLog*(..)) && args(int, sarr)
     */
    Pointcut pc2(String[] sarr) {
        return null;
    }
    
    /**
     * @Expression execution(* ..ArgLoggingTarget.toLog*(..))
     */
    Pointcut pc3() {
        return null;
    }
    
    // FIXME - Validate: 
    // 1. Use of PC (@Before pc1(..)) has the correct param list to the PC (match the signature)
    // 2. All param names in the PC signature are defined in an args(..) construct

    
    
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

    private void indent() {
        for (int i = 0; i < m_level; i++) {
            System.out.print("  ");
        }
    }
}
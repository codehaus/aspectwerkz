/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aspect;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldSignature;
import test.FieldAdviceTest;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perJVM
 */
public class FieldTestAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @Set * test.FieldAdviceTest.m_setFieldPreAdvice*
     */
    Pointcut pc1;
    /**
     * @Set int test.FieldAdviceTest.m_setFieldPreAdvi*
     */
    Pointcut pc2;
    /**
     * @Set * test.FieldAdviceTest.m_setFie*dPostAdviced
     */
    Pointcut pc3;
    /**
     * @Set * test.FieldAdviceTest.m_se*FieldPostAdviced
     */
    Pointcut pc4;
    /**
     * @Set * test.FieldAdviceTest.m_setFieldPrePostAdviced
     */
    Pointcut pc5;

    /**
     * @Get * test.FieldAdviceTest.m_getFieldPreAdvic*
     */
    Pointcut pc6;
    /**
     * @Get * test.FieldAdviceTest.m_getFieldPreAdvice*
     */
    Pointcut pc7;
    /**
     * @Get * test.FieldAdviceTest.m_getFieldPostAdviced
     */
    Pointcut pc8;
    /**
     * @Get * test.FieldAdviceTest.m_getFieldPrePostAdviced
     */
    Pointcut pc9;

    /**
     * @Set * test.FieldAdviceTest.s_setStaticFieldPreAdvic*
     */
    Pointcut pc10;
    /**
     * @Set * test.FieldAdviceTest.s_setStaticFieldPreAdvice*
     */
    Pointcut pc11;
    /**
     * @Set * test.FieldAdviceTest.s_setStaticFieldPostAdviced
     */
    Pointcut pc12;
    /**
     * @Set * test.FieldAdviceTest.s_setStaticFieldPrePostAdviced
     */
    Pointcut pc13;

    /**
     * @Get * test.FieldAdviceTest.s_getStaticFieldPreAdvice*
     */
    Pointcut pc14;
    /**
     * @Get * test.FieldAdviceTest.s_getStaticFieldPreAdvic*
     */
    Pointcut pc15;
    /**
     * @Get * test.FieldAdviceTest.s_getStaticFieldPostAdviced
     */
    Pointcut pc16;
    /**
     * @Get * test.FieldAdviceTest.s_getStaticFieldPrePostAdviced
     */
    Pointcut pc17;

    /**
     * @Set * test.FieldAdviceTest.m_setFieldAroundAdviced
     */
    Pointcut pc18;
    /**
     * @Set * test.FieldAdviceTest.s_setStaticFieldAroundAdviced
     */
    Pointcut pc19;
    /**
     * @Get * test.FieldAdviceTest.m_getFieldAroundAdviced
     */
    Pointcut pc20;
    /**
     * @Get * test.FieldAdviceTest.s_getStaticFieldAroundAdviced
     */
    Pointcut pc21;

    /**
     * @Set * test.FieldAdviceTest.m_setFieldAroundAdviced*WithNullAdvice
     */
    Pointcut pc22;
    /**
     * @Get * test.FieldAdviceTest.m_getFieldAroundAdvicedWithNullAdvice
     */
    Pointcut pc23;

    /**
     * @Set * test.FieldAdviceTest.m_setFieldAroundAdvicedObjectWithAPI
     */
    Pointcut pc24;
    /**
     * @Set * test.FieldAdviceTest.m_setFieldAroundAdvicedWithAPI
     */
    Pointcut pc25;

    // ============ Advices ============

    /**
     * @Before pc2 || pc5 || pc10 || pc13
     * @Before pc6 || pc9 || pc14 || pc17
     */
    public void preAdvice1(final JoinPoint joinPoint) throws Throwable {
        FieldAdviceTest.log("pre1 ");
    }

    /**
     * @Before pc1 || pc5 || pc11 || pc13
     * @Before pc7 || pc9 || pc15 || pc17
     */
    public void preAdvice2(final JoinPoint joinPoint) throws Throwable {
        FieldAdviceTest.log("pre2 ");
    }

    /**
     * @After pc4 || pc5 || pc12 || pc13
     * @After pc8 || pc9 || pc16 || pc17
     */
    public void postAdvice1(final JoinPoint joinPoint) throws Throwable {
        FieldAdviceTest.log("post1 ");
    }

    /**
     * @After pc3 || pc5 || pc12 || pc13
     * @After pc8 || pc9 || pc16 || pc17
     */
    public void postAdvice2(final JoinPoint joinPoint) throws Throwable {
        FieldAdviceTest.log("post2 ");
    }

    /**
     * @Around pc18 || pc19
     * @Around pc20 || pc21
     */
    public Object around(final JoinPoint joinPoint) throws Throwable {
        FieldAdviceTest.log("before ");
        final Object result = joinPoint.proceed();
        FieldAdviceTest.log("after ");
        return result;
    }

    /**
     * @Around pc22
     * @Around pc23
     */
    public Object aroundNullAdvice(final JoinPoint joinPoint) throws Throwable {
        FieldAdviceTest.log("before ");
        final Object result = joinPoint.proceed();
        FieldAdviceTest.log("after ");
        return null;
    }

    /**
     * @Around pc24
     */
    public Object aroundAdviceAltering(final JoinPoint joinPoint) throws Throwable {
        FieldAdviceTest.log("before ");
        FieldSignature signature = (FieldSignature)joinPoint.getSignature();
        signature.setFieldValue(new String("byAdvice"));
        joinPoint.proceed();
        FieldAdviceTest.log("after ");
        return null;
    }

    /**
     * @Around pc25
     */
    public Object aroundAdviceAlteringPrimitive(final JoinPoint joinPoint) throws Throwable {
        FieldAdviceTest.log("before ");
        FieldSignature signature = (FieldSignature)joinPoint.getSignature();
        signature.setFieldValue(new Integer(3));
        joinPoint.proceed();
        FieldAdviceTest.log("after ");
        return null;
    }

}

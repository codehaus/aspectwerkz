/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.attribdef.aspect;

import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import test.attribdef.FieldAdviceTest;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class FieldTestAspect extends Aspect {

    // ============ Pointcuts ============

    /** @Set * test.attribdef.FieldAdviceTest.m_setFieldPreAdvice* */
    Pointcut pc1;
    /** @Set int test.attribdef.FieldAdviceTest.m_setFieldPreAdvi* */
    Pointcut pc2;
    /** @Set * test.attribdef.FieldAdviceTest.m_setFie*dPostAdviced */
    Pointcut pc3;
    /** @Set * test.attribdef.FieldAdviceTest.m_se*FieldPostAdviced */
    Pointcut pc4;
    /** @Set * test.attribdef.FieldAdviceTest.m_setFieldPrePostAdviced */
    Pointcut pc5;
    /** @Get * test.attribdef.FieldAdviceTest.m_getFieldPreAdvic* */
    Pointcut pc6;
    /** @Get * test.attribdef.FieldAdviceTest.m_getFieldPreAdvice* */
    Pointcut pc7;
    /** @Get * test.attribdef.FieldAdviceTest.m_getFieldPostAdviced */
    Pointcut pc8;
    /** @Get * test.attribdef.FieldAdviceTest.m_getFieldPrePostAdviced */
    Pointcut pc9;
    /** @Set * test.attribdef.FieldAdviceTest.s_setStaticFieldPreAdvic* */
    Pointcut pc10;
    /** @Set * test.attribdef.FieldAdviceTest.s_setStaticFieldPreAdvice* */
    Pointcut pc11;
    /** @Set * test.attribdef.FieldAdviceTest.s_setStaticFieldPostAdviced */
    Pointcut pc12;
    /** @Set * test.attribdef.FieldAdviceTest.s_setStaticFieldPrePostAdviced */
    Pointcut pc13;
    /** @Get * test.attribdef.FieldAdviceTest.s_getStaticFieldPreAdvice* */
    Pointcut pc14;
    /** @Get * test.attribdef.FieldAdviceTest.s_getStaticFieldPreAdvic* */
    Pointcut pc15;
    /** @Get * test.attribdef.FieldAdviceTest.s_getStaticFieldPostAdviced */
    Pointcut pc16;
    /** @Get * test.attribdef.FieldAdviceTest.s_getStaticFieldPrePostAdviced */
    Pointcut pc17;

    // ============ Advices ============

    /**
     * @Before pc2 || pc5 || pc6 || pc9 || pc10 || pc13 || pc14 || pc17
     */
    public void preAdvice1(final JoinPoint joinPoint) {
        FieldAdviceTest.log("pre1 ");
    }

    /**
     * @Before pc1 || pc5 || pc7 || pc11 || pc13 || pc15 || pc9 || pc17
     */
    public void preAdvice2(final JoinPoint joinPoint) {
        FieldAdviceTest.log("pre2 ");
    }

    /**
     * @After pc4 || pc5 || pc8 || pc9 || pc12 || pc13 || pc16 || pc17
     */
    public void postAdvice1(final JoinPoint joinPoint) {
        FieldAdviceTest.log("post1 ");
    }

    /**
     * @After pc3 || pc5 || pc8 || pc9 || pc12 || pc13 || pc16 || pc17
     */
    public void postAdvice2(final JoinPoint joinPoint) {
        FieldAdviceTest.log("post2 ");
    }
}

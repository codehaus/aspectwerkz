/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.staticfield;

import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TestAspect extends Aspect {

    /** @Set * test.xmldef.staticfield.StaticFieldAdviceTest.s_field* */
     Pointcut pcSet;
    /** @Set * test.xmldef.staticfield.StaticFieldAdviceTest.m_field* */
     Pointcut pcSetMember;
    /** @Set * test.xmldef.staticfield.CollectionFieldTest.s_field */
     Pointcut pcSetColl;
    /** @Set * test.xmldef.staticfield.CollectionFieldTest.m_field */
     Pointcut pcSetMemberColl;
    /** @Get * test.xmldef.staticfield.CollectionFieldTest.s_field */
     Pointcut pcGetColl;
    /** @Get * test.xmldef.staticfield.CollectionFieldTest.m_field */
     Pointcut pcGetMemberColl;

    /**
     * @Before pcSet
     */
    public void preStaticField(final JoinPoint joinPoint) throws Throwable {
        CollectionFieldTest.s_log += "MyPreAdvice1 ";
    }

    /**
     * @Before pcSetMember
     */
    public void preMemberField1(final JoinPoint joinPoint) throws Throwable {
        CollectionFieldTest.s_log += "MyPreAdvice2 ";
    }

    /**
     * @Before pcSetColl
     */
    public void preStaticField2(final JoinPoint joinPoint) throws Throwable {
        CollectionFieldTest.s_log += "MyPreAdvice1 ";
    }

    /**
     * @Before pcSetMemberColl
     */
    public void preMemberField2(final JoinPoint joinPoint) throws Throwable {
        CollectionFieldTest.s_log += "MyPreAdvice2 ";
    }

    /**
     * @After pcGetColl
     */
    public void postStaticField(final JoinPoint joinPoint) throws Throwable {
        CollectionFieldTest.s_log += "MyPostAdvice1 ";
    }

    /**
     * @After pcGetMemberColl
     */
    public void postMemberField(final JoinPoint joinPoint) throws Throwable {
        CollectionFieldTest.s_log += "MyPostAdvice2 ";
    }

}

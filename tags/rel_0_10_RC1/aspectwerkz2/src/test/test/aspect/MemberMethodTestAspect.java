/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aspect;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import test.Loggable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perJVM
 */
public class MemberMethodTestAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.get*(..))
     */
    Pointcut pc1;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.*Param**(..))
     */
    Pointcut pc2;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.testThrowException(..))
     */
    Pointcut pc3;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.methodAdvicedMethod())
     */
    Pointcut pc4;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.meth*AdvicedMethod())
     */
    Pointcut pc5;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.method*icedMethodNewThread(..))
     */
    Pointcut pc6;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.method*dvicedMethodNewThread(..))
     */
    Pointcut pc7;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.multipleMethodAdvicedMethod(..))
     */
    Pointcut pc8;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.multipleChainedMethodAdvicedMethod(..))
     */
    Pointcut pc9;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.joinPointMetaData(..))
     */
    Pointcut pc10;
    /**
     * @Expression execution(void test.MemberMethodAdviceTest.passingParameterToAdviceMethod(..))
     */
    Pointcut pc11;
    /**
     * @Expression execution(void test.MemberMethodAdviceTest.multiplePointcutsMethod(..))
     */
    Pointcut pc12;
    /**
     * @Expression execution(void test.MemberMethodAdviceTest.multiplePointcutsMethod(..))
     */
    Pointcut pc13;
    /**
     * @Expression execution(* test.MemberMethodAdviceTest.takesArrayAsArgument(String[]))
     */
    Pointcut pc14;
    /**
     * @Expression execution(long test.MemberMethodAdviceTest.getPrimitiveAndNullFromAdvice())
     */
    Pointcut pc15;
    /**
     * @Expression execution(void test.MemberMethodAdviceTest.beforeAdvicedMethod())
     */
    Pointcut pc16;
    /**
     * @Expression execution(void test.MemberMethodAdviceTest.afterAdvicedMethod())
     */
    Pointcut pc17;
    /**
     * @Expression execution(void test.MemberMethodAdviceTest.beforeAfterAdvicedMethod())
     */
    Pointcut pc18;
    /**
     * @Expression execution(void test.MemberMethodAdviceTest.beforeAroundAfterAdvicedMethod())
     */
    Pointcut pc19;

    // ============ Advices ============

    /**
     * @Around pc1 || pc2 || pc3 || pc4 || pc14 || pc9
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * @Around pc5 || pc8 || pc9 || pc12 || pc19
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around pc8 || pc9 || pc13 || pc19
     */
    public Object advice3(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after2 ");
        return result;
    }

    /**
     * @Around pc10
     */
    public Object advice4(final JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        String metadata =
                joinPoint.getTargetClass().getName() +
                signature.getMethod().getName() +
                joinPoint.getTargetInstance().hashCode() +
                signature.getParameterValues()[0] +
                signature.getParameterTypes()[0].getName() +
                signature.getReturnType().getName() +
                signature.getReturnValue();
        return metadata;
    }

    /**
     * @Around pc6 || pc7
     */
    public Object advice5(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after ");
        return result;
    }

    /**
     * @Around pc15
     */
    public Object advice6(final JoinPoint joinPoint) throws Throwable {
        return null;
    }

    /**
     * @Before pc16 || pc18 || pc19
     */
    public void before(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("pre ");
    }

    /**
     * @After pc17 || pc18 || pc19
     */
    public void after(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("post ");
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aspect;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import test.StaticMethodAdviceTest;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perJVM
 */
public class StaticMethodTestAspect {

    // ============ Pointcuts ============

    /**
     * @Expression execution(* test.StaticMethodAdviceTest.get*(..))
     */
    Pointcut pc1;
    /**
     * @Expression execution(* test.StaticMethodAdviceTest.*Param*(..))
     */
    Pointcut pc2;
    /**
     * @Expression execution(void test.StaticMethodAdviceTest.methodAdvicedMethod(..))
     */
    Pointcut pc4;
    /**
     * @Expression execution(* test.StaticMethodAdviceTest.methodAdvicedMethod(..))
     */
    Pointcut pc5;
    /**
     * @Expression execution(* test.StaticMethodAdviceTest.methodAdvicedMethodNewThread(..))
     */
    Pointcut pc6;
    /**
     * @Expression execution(* test.StaticMethodAdviceTest.multipleMethodAdvicedMethod(..))
     */
    Pointcut pc7;
    /**
     * @Expression execution(* test.StaticMethodAdviceTest.multipleChainedMethodAdvicedMethod(..))
     */
    Pointcut pc8;
    /**
     * @Expression execution(* test.StaticMethodAdviceTest.joinPointMetaData(..))
     */
    Pointcut pc9;
    /**
     * @Expression execution(void test.StaticMethodAdviceTest.multiplePointcutsMethod(..))
     */
    Pointcut pc10;
    /**
     * @Expression execution(void test.StaticMethodAdviceTest.multiplePointcutsMethod(..))
     */
    Pointcut pc11;
    /**
     * @Expression execution(* test.StaticMethodAdviceTest.takesArrayAsArgument(String[]))
     */
    Pointcut pc12;
    /**
     * @Expression execution(long test.StaticMethodAdviceTest.getPrimitiveAndNullFromAdvice())
     */
    Pointcut pc13;

    // ============ Advices ============

    /**
     * @Around pc1 || pc2 || pc5 || pc8 || pc12
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * @Around pc4 || pc7 || pc8 || pc10
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        ((StaticMethodAdviceTest)joinPoint.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((StaticMethodAdviceTest)joinPoint.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around pc7 || pc8 || pc11
     */
    public Object advice3(final JoinPoint joinPoint) throws Throwable {
        ((StaticMethodAdviceTest)joinPoint.getTargetInstance()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((StaticMethodAdviceTest)joinPoint.getTargetInstance()).log("after2 ");
        return result;
    }

    /**
     * @Around pc9
     */
    public Object advice4(final JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        String metadata =
                joinPoint.getTargetClass().getName() +
                signature.getMethod().getName() +
                signature.getParameterValues()[0] +
                signature.getParameterTypes()[0].getName() +
                signature.getReturnType().getName() +
                signature.getReturnValue();
        return metadata;
    }

    /**
     * @Around pc6
     */
    public Object advice5(final JoinPoint joinPoint) throws Throwable {
        ((StaticMethodAdviceTest)joinPoint.getTargetInstance()).log("before ");
        final Object result = joinPoint.proceed();
        ((StaticMethodAdviceTest)joinPoint.getTargetInstance()).log("after ");
        return result;
    }

    /**
     * @Around pc13
     */
    public Object advice7(final JoinPoint joinPoint) throws Throwable {
        return null;
    }
}

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
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.ThrowsJoinPoint;
import test.attribdef.StaticMethodAdviceTest;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class StaticMethodTestAspect extends Aspect {

    // ============ Pointcuts ============

    /** @Execution * test.attribdef.StaticMethodAdviceTest.get*(..) */
    Pointcut pc1;
    /** @Execution * test.attribdef.StaticMethodAdviceTest.*Param*(..) */
    Pointcut pc2;
    /** @Throws * test.attribdef.StaticMethodAdviceTest.exceptionThrower(..)#java.lang.UnsupportedOperationException */
    Pointcut pc3;
    /** @Execution void test.attribdef.StaticMethodAdviceTest.methodAdvicedMethod(..) */
    Pointcut pc4;
    /** @Execution * test.attribdef.StaticMethodAdviceTest.methodAdvicedMethod(..) */
    Pointcut pc5;
    /** @Execution * test.attribdef.StaticMethodAdviceTest.methodAdvicedMethodNewThread(..) */
    Pointcut pc6;
    /** @Execution * test.attribdef.StaticMethodAdviceTest.multipleMethodAdvicedMethod(..) */
    Pointcut pc7;
    /** @Execution * test.attribdef.StaticMethodAdviceTest.multipleChainedMethodAdvicedMethod(..) */
    Pointcut pc8;
    /** @Execution * test.attribdef.StaticMethodAdviceTest.joinPointMetaData(..) */
    Pointcut pc9;
    /** @Execution void test.attribdef.StaticMethodAdviceTest.multiplePointcutsMethod(..) */
    Pointcut pc10;
    /** @Execution void test.attribdef.StaticMethodAdviceTest.multiplePointcutsMethod(..) */
    Pointcut pc11;
    /** @Execution * test.attribdef.StaticMethodAdviceTest.takesArrayAsArgument(String[]) */
    Pointcut pc12;

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
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((StaticMethodAdviceTest)jp.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((StaticMethodAdviceTest)jp.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around pc7 || pc8 || pc11
     */
    public Object advice3(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((StaticMethodAdviceTest)jp.getTargetInstance()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((StaticMethodAdviceTest)jp.getTargetInstance()).log("after2 ");
        return result;
    }

    /**
     * @Around pc9
     */
    public Object advice4(final JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        String metadata =
                jp.getTargetClass().getName() +
                jp.getMethod().getName() +
                jp.getParameters()[0] +
                jp.getParameterTypes()[0].getName() +
                jp.getReturnType().getName() +
                jp.getResult();
        return metadata;
    }

    /**
     * @Around pc6
     */
    public Object advice5(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((StaticMethodAdviceTest)jp.getTargetInstance()).log("before ");
        final Object result = joinPoint.proceedInNewThread();
        ((StaticMethodAdviceTest)jp.getTargetInstance()).log("after ");
        return result;
    }


    /**
     * @Around pc3
     */
    public Object advice6(final JoinPoint joinPoint) throws Throwable {
        return new Object();
    }
}

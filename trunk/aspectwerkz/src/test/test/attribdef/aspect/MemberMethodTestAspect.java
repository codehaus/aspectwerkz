/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.attribdef.aspect;

import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import test.attribdef.Loggable;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MemberMethodTestAspect extends Aspect {

    // ============ Pointcuts ============

    /** @Execution * test.attribdef.MemberMethodAdviceTest.get*(..) */
    Pointcut pc1;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.*Param**(..) */
    Pointcut pc2;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.testThrowException(..) */
    Pointcut pc3;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.methodAdvicedMethod(..) */
    Pointcut pc4;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.meth*AdvicedMethod(..) */
    Pointcut pc5;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.method*icedMethodNewThread(..) */
    Pointcut pc6;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.method*dvicedMethodNewThread(..) */
    Pointcut pc7;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.multipleMethodAdvicedMethod(..) */
    Pointcut pc8;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.multipleChainedMethodAdvicedMethod(..) */
    Pointcut pc9;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.joinPointMetaData(..) */
    Pointcut pc10;
    /** @Execution void test.attribdef.MemberMethodAdviceTest.passingParameterToAdviceMethod(..) */
    Pointcut pc11;
    /** @Execution void test.attribdef.MemberMethodAdviceTest.multiplePointcutsMethod(..) */
    Pointcut pc12;
    /** @Execution void test.attribdef.MemberMethodAdviceTest.multiplePointcutsMethod(..) */
    Pointcut pc13;
    /** @Execution * test.attribdef.MemberMethodAdviceTest.takesArrayAsArgument(String[]) */
    Pointcut pc14;
    /** @Execution long test.attribdef.MemberMethodAdviceTest.getPrimitiveAndNullFromAdvice() */
    Pointcut pc15;

    // ============ Advices ============

    /**
     * @Around pc1 || pc2 || pc3 || pc4 || pc14 || pc9
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * @Around pc5 || pc8 || pc9 || pc12
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around pc8 || pc9 || pc13
     */
    public Object advice3(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetInstance()).log("after2 ");
        return result;
    }

    /**
     * @Around pc10
     */
    public Object advice4(final JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        String metadata =
                jp.getTargetClass().getName() +
                jp.getMethod().getName() +
                jp.getTargetInstance().hashCode() +
                jp.getParameters()[0] +
                jp.getParameterTypes()[0].getName() +
                jp.getReturnType().getName() +
                jp.getResult();
        return metadata;
    }

    /**
     * @Around pc6 || pc7
     */
    public Object advice5(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("before ");
        final Object result = joinPoint.proceedInNewThread();
        ((Loggable)jp.getTargetInstance()).log("after ");
        return result;
    }

    /**
     * @Around pc15
     */
    public Object advice6(final JoinPoint joinPoint) throws Throwable {
        return null;
    }

    /**
     * Around
     */
//    public Object advice7(final JoinPoint joinPoint) throws Throwable {
//        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
//        ((Loggable)jp.getTargetInstance()).log("# ");
//        return joinPoint.proceed();
//    }
}

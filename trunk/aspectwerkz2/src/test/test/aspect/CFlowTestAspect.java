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
import test.Loggable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perJVM
 */
public class CFlowTestAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @CFlow * test.CFlowTest.step1()
     */
    Pointcut pc1;
    /**
     * @CFlow * test.CFlowTest.step1_A()
     */
    Pointcut pc1_A;
    /**
     * @CFlow * test.CFlowTest.step1_B()
     */
    Pointcut pc1_B;
    /**
     * @Execution * test.CFlowTest.step2()
     */
    Pointcut pc2;
    /**
     * @Execution * test.CFlowTest.step2_B()
     */
    Pointcut pc2_B;

    // ============ Advices ============

    /**
     * @Around pc2 AND pc1
     */
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("advice-before ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("advice-after ");
        return result;
    }

    /**
     * @Around pc2_B AND pc1_B AND pc1_A
     */
    public Object execute2(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("advice-before2 ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("advice-after2 ");
        return result;
    }

}

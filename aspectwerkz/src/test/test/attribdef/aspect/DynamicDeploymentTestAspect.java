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
import test.attribdef.Loggable;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DynamicDeploymentTestAspect extends Aspect {

    // ============ Pointcuts ============

    /** @Execution * test.attribdef.DynamicDeploymentTest.reorderAdvicesTestMethod(..) */
    Pointcut pc1;
    /** @Execution * test.attribdef.DynamicDeploymentTest.removeAdviceTestMethod(..) */
    Pointcut pc2;
    /** @Execution * test.attribdef.DynamicDeploymentTest.addAdviceTestMethod(..) */
    Pointcut pc3;
    /** @Execution * test.attribdef.DynamicDeploymentTest.createTransientAdviceTestMethod(..) */
    Pointcut pc4;
    /** @Execution * test.attribdef.DynamicDeploymentTest.createPersistentAdviceTestMethod(..) */
    Pointcut pc5;

    // ============ Advices ============

    /**
     * @Around pc1 || pc2 || pc3
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around pc1 || pc4 || pc5
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetInstance()).log("after2 ");
        return result;
    }
}

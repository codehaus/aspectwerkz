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
 * @TODO: need nested pointcuts, need to be able to specify one singe pointcut name for the advice to be able to easily refer to it when modifying the advices at runtime. this the handle is the pointcut expression bound to the advice and this handle then need to be simplified (one single name that can be reused).
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
    /** @Execution * test.attribdef.DynamicDeploymentTest.createAspectTestMethod(..) */
    Pointcut pc4;

    // ============ Advices ============

    /**
     * @Around pc1
     * @Around pc2
     * @Around pc3
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around pc1 name=advice2
     * @Around pc2 name=advice2
     * @Around pc4 name=advice2
    */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetInstance()).log("after2 ");
        return result;
    }
}

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
public class CFlowTestAspect extends Aspect {

    // ============ Pointcuts ============

    /** @CFlow * test.attribdef.CFlowTest.step1() */
    Pointcut pc1;
    /** @Execution * test.attribdef.CFlowTest.step2() */
    Pointcut pc2;

    // ============ Advices ============

    /**
     * @Around pc1 && pc2
     */
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("advice-before ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetInstance()).log("advice-after ");
        return result;
    }
}

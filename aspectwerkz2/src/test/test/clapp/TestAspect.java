/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.clapp;

import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class TestAspect extends Aspect {

    /**
     * @Execution * test.xmldef.clapp.Target.callme(..)
     */
    Pointcut pc1;

    /**
     * @Around pc1
     */
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        Integer result = (Integer)joinPoint.proceed();
        return new Integer(-1 * result.intValue());
    }
}
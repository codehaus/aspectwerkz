/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.interfacesubtypebug;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class TestAspect {
    // ============ Pointcuts ============

    /**
     * @Expression execution(* test.interfacesubtypebug.Intf#.*())
     */
    Pointcut interfacePC;

    // ============ Advices ============

    /**
     * @Around interfacePC
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        InterfaceSubtypeBug.LOG += "interface ";
        Object result = joinPoint.proceed();
        InterfaceSubtypeBug.LOG += "interface ";
        return result;
    }
}

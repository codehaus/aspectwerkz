/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.Pointcut;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @Aspect perJVM
 */
public class LoggingAspect extends AbstractLoggingAspect {

    public LoggingAspect() {
        super();
    }
    
    // ============ Pointcuts ============

    // AW-152: see XML - TODO move in a TestCase
//    /**
//     * @Expression execution(* examples.logging.Target.toLog1(..))
//     */
//    Pointcut methodsToLog1;

    /**
     * @Expression execution(* examples.logging.Target.toLog2(..))
     */
    Pointcut methodsToLog2;

    /**
     * @Expression execution(* examples.logging.Target.toLog3(..))
     */
    Pointcut methodsToLog3;

    /**
     * @Expression get(int examples.logging.Target.m_*)
     */
    Pointcut logGet;

    /**
     * @Expression set(int examples.logging.Target.m_*)
     */
    Pointcut logSet;
}

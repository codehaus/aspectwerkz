/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.CrossCuttingInfo;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect
 */
public class LoggingAspect extends AbstractLoggingAspect {

    public LoggingAspect(CrossCuttingInfo info) {
        super(info);
//        System.out.println("\t\tLoggingAspect UUID: " + info.getUuid());
//        System.out.println("I AM PROTO " + info.isPrototype());
    }
    
    // ============ Pointcuts ============

    // AW-152: see XML - TODO move in a TestCase
    // AV - eW - temp restored
    /**
     * @Expression execution(@log * examples.logging.*.*(..))
     */
    Pointcut methodsToLog;

    /**
     * @Expression get(@log * examples.logging.*.*)
     */
    Pointcut logGet;

    /**
     * @Expression set(@log * examples.logging.*.*)
     */
    Pointcut logSet;
}

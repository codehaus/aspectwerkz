/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.PreparedPointcut;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class LoggingAspect extends AbstractLoggingAspect {

    // ============ Pointcuts ============

    /**
     * @Expression execution(* examples.logging.Target.toLog*(..))
     */
    Pointcut methodsToLog() {
        return null;
    };
}
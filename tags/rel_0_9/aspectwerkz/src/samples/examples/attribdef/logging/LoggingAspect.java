/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.attribdef.logging;

import org.codehaus.aspectwerkz.attribdef.Pointcut;

/**
 * @Aspect perJVM name=LoggingAspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class LoggingAspect extends AbstractLoggingAspect {

    // ============ Pointcuts ============

    /**
     * @Execution * examples.attribdef.logging.Target.toLog1(..)
     */
    Pointcut methodsToLog1;

    /**
     * @Execution * examples.attribdef.logging.Target.toLog2(..)
     */
    Pointcut methodsToLog2;

    /**
     * @Execution * examples.attribdef.logging.Target.toLog3(..)
     */
    Pointcut methodsToLog3;

    /**
     * @Get int examples.attribdef.logging.Target.m_*
     */
    Pointcut logGet;

    /**
     * @Set int examples.attribdef.logging.Target.m_*
     */
    Pointcut logSet;
}

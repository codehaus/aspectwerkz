/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;

/**
 * This aspect shows how to implement logging modules using Log4j, 1.4 Logger etc.
 * (currently showing the use of 1.4 Logger API).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class LoggerIdiom {

    /**
     * @Around("methodToLog && target(loggable)")
     */
    public Object log(StaticJoinPoint jp, Loggable loggable) throws Throwable {
        loggable.getLog().log(Level.ALL, "entering " + jp.getSignature());
        Object result = jp.proceed();
        loggable.getLog().log(Level.ALL, "exiting " + jp.getSignature());
        return result;
    }

    /**
     * @Mixin(
     *      pointcut="loggableClasses",
     *      deploymentModel="perClass"
     * )
     */
    public static class LoggableImpl implements Loggable {

        private final Logger LOG;

        public LoggableImpl(Class targetClass) {
            LOG = Logger.getLogger(targetClass.getName());
        }

        public Logger getLog() {
            return LOG;
        }
    }

    public static interface Loggable {
        Logger getLog();
    }
}


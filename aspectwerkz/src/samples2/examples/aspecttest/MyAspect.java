/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.aspecttest;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @Aspect("perJVM")
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MyAspect {

    // ==== pointcuts ====

    /**
     * @Pointcut("expression")
     * Pointcut("**..*.set*(..)")
     */
    void setMethods() {}

    /**
     * @Pointcut("expression")
     * Pointcut("**..*.set*(..)")
     */
    void getMethods() {}

    /**
     * @Pointcut("expression")
     * Pointcut("getMethods() && setMethods()")
     */
    void methodsToLog() {}

    /**
     * @Pointcut("domain.*")
     */
    void domainObjects() {}

    // ==== advices ====

    /**
     * @AroundAdvice("methodsToLog()")
     */
    public Object logMethod(JoinPoint joinPoint) throws Throwable {
        // do stuff
        return joinPoint.proceed();
    }

    /**
     * @PreAdvice("setMethods()")
     */
    public void logEntry(JoinPoint joinPoint) throws Throwable {
        // do stuff
    }

    // ==== introductions ====

    String m_name;

    /**
     * @Introduction("domainObjects()")
     */
    public String getName() {
        return m_name;
    }

    /**
     * @Introduction("domainObjects()")
     */
    public void setName(String name) {
        m_name = name;
    }
}

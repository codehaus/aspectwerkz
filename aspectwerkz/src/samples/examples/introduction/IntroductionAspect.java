/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.introduction;

import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionAspect extends AbstractIntroductionAspect {

    // ============ Pointcuts ============

    /**
     * @Class examples.introduction.Target
     */
    Pointcut classes;

    /**
     * @Introduction classes
     */
    public String sayHello2() {
        return "Hello World! Hello World!";
    }
}

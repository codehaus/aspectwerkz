/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.introduction;

import org.codehaus.aspectwerkz.aspect.AbstractAspect;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionAspect extends AbstractAspect {

    // ============ Pointcuts ============

    /**
     * @Class examples.introduction.Target
     */
    Pointcut classes;

    // ============ Introductions ============

    /**
     * @Implements classes
     */
     examples.introduction.Mixin mixin;

    /**
     * @Introduction classes
     */
    public String sayHello() {
        return "Hello World!";
    }
}

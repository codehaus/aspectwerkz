/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.attribdef.introduction;

import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;

/**
 * @Aspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractIntroductionAspect extends Aspect {

    /**
     * @Implements classes
     */
     Mixin mixin;

    /**
     * @Introduction classes
     */
    public String sayHello1() {
        System.out.println("target instance: " + ___AW_getTargetInstance());
        return "Hello World!";
    }
}

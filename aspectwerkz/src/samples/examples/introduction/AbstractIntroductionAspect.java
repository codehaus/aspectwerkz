/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.introduction;

import org.codehaus.aspectwerkz.aspect.AbstractAspect;

/**
 * @Aspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class AbstractIntroductionAspect extends AbstractAspect {

    /**
     * @Implements classes
     */
     Mixin mixin;

    /**
     * @Introduction classes
     */
    public String sayHello1() {
        return "Hello World!";
    }
}

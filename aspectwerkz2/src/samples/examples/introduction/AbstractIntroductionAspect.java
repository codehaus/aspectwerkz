/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.introduction;

import org.codehaus.aspectwerkz.CrossCutting;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @Aspect
 */
public abstract class AbstractIntroductionAspect {

    /**
     * The Introduce doclet is not necessary here. This aspect provides a half completed mixin impl (abstract one)
     *
     * @Introduce TO_BE_DEFINED
     */
    public abstract class MyImpl implements Mixin {
        public String sayHello1() {
            System.out.println("aspect target class: " + ((CrossCutting)this).getCrossCuttingInfo().getTargetClass());
            return "Hello World!";
        }
    }
}

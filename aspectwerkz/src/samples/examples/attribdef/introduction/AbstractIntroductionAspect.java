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

//    /**
//     * @Implements examples.attribdef.introduction.Target
//     */
//     Mixin mixinInterface;

    /**
     * TODO: is this a good abstraction ?
     * Shall we allow for defining pointcuts thru @Implements ?
     * What if the abstract aspect provide all the mixin impl ?
     *
     * @Introduce TO_BE_DEFINED
     */
    public abstract class MyImpl implements Mixin {
        public String sayHello1() {
            System.out.println("target instance: " + ___AW_getTargetInstance());
            return "Hello World!";
        }
    }
}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.attribdef.introduction;

/**
 * @Aspect perInstance
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionAspect extends AbstractIntroductionAspect {

    // ============ Pointcuts ============

    /**
     * @Introduce examples.attribdef.introduction.Target
     */
    public class MyConcreteImpl extends MyImpl {
        public String sayHello2() {
            System.out.println("target instance: " + ___AW_getTargetInstance());
            return "Hello World! Hello World!";
        }
    }
}

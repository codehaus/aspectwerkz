/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.introduction;

import org.codehaus.aspectwerkz.CrossCuttingInfo;


/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perClass
 */
public class IntroductionAspect extends AbstractIntroductionAspect {

    // ============ Pointcuts ============

    /**
     * @Introduce class(examples.introduction.Target) deploymentModel=perInstance
     */
    public class MyConcreteImpl extends MyImpl {
        public String sayHello2() {
            CrossCuttingInfo info = CrossCuttingInfo.getInfo("samples", this);
            System.out.println("mixin target class: " + info.getMixinTargetClass(this.getClass().getName(), this));
            System.out.println("mixin target instance: " + info.getMixinTargetInstance(this.getClass().getName(), this));
            return "Hello World! Hello World!";
        }
    }
}

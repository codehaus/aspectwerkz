/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.mixindeployment;

import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * The aspect mixin is deployed as perInstance
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @Aspect perJVM
 */
public class IntroductionDeploymentAspect extends Aspect {

    /**
     * @Class test.mixindeployment.IntroductionDeploymentTest$TargetA
     */
    Pointcut a;

    /**
     * @Class test.mixindeployment.IntroductionDeploymentTest$TargetB
     */
    Pointcut b;

    /**
     * Set to match
     *
     * @Introduce a || b deploymentModel=perInstance
     */
    public class MarkerImpl implements Marker {

        public Object getTargetInstance() {
            return ___AW_getMixinTargetInstance(this.getClass().getName(), this);
        }

        public Class getTargetClass() {
            return ___AW_getMixinTargetClass(this.getClass().getName(), this);
        }
    }


    /**
     * @Introduce test.mixindeployment.IntroductionDeploymentTest$TargetC deploymentModel=perClass
     */
    public class AnotherMarkerImpl implements Marker {

        public Object getTargetInstance() {
            // will return null
            return ___AW_getMixinTargetInstance(this.getClass().getName(), this);
        }

        public Class getTargetClass() {
            return ___AW_getMixinTargetClass(this.getClass().getName(), this);
        }
    }

}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.mixindeployment;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.CrossCuttingInfo;

/**
 * The aspect mixin is deployed as perInstance
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @Aspect perJVM
 */
public class IntroductionDeploymentAspect {

    /**
     * @Expression class(test.mixindeployment.IntroductionDeploymentTest$TargetA)
     */
    Pointcut a;

    /**
     * @Expression class(test.mixindeployment.IntroductionDeploymentTest$TargetB)
     */
    Pointcut b;

    /**
     * Set to match
     *
     * @Introduce a || b deploymentModel=perInstance
     */
    public class MarkerImpl implements Marker {

        public Object getTargetInstance() {
            CrossCuttingInfo info = CrossCuttingInfo.getInfo("tests", IntroductionDeploymentAspect.this);
            return info.getMixinTargetInstance(this.getClass().getName(), this);
        }

        public Class getTargetClass() {
            CrossCuttingInfo info = CrossCuttingInfo.getInfo("tests", IntroductionDeploymentAspect.this);
            return info.getMixinTargetClass(this.getClass().getName(), this);
        }
    }

    /**
     * Note: explicit class(..) pointcut is needed
     *
     * @Introduce class(test.mixindeployment.IntroductionDeploymentTest$TargetC) deploymentModel=perClass
     */
    public class AnotherMarkerImpl implements Marker {

        public Object getTargetInstance() {
            // will return null
            CrossCuttingInfo info = CrossCuttingInfo.getInfo("tests", IntroductionDeploymentAspect.this);
            return info.getMixinTargetInstance(this.getClass().getName(), this);
        }

        public Class getTargetClass() {
            CrossCuttingInfo info = CrossCuttingInfo.getInfo("tests", IntroductionDeploymentAspect.this);
            return info.getMixinTargetClass(this.getClass().getName(), this);
        }
    }
}

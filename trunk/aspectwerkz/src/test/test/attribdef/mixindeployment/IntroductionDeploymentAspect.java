/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.attribdef.mixindeployment;

import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.attribdef.Pointcut;

/**
 * The aspect mixin is deployed as perInstance
 *
 * @Aspect perJVM
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class IntroductionDeploymentAspect extends Aspect {

    /** @Class test.attribdef.mixindeployment.IntroductionDeploymentTest$TargetA */
    Pointcut a;

    /** @Class test.attribdef.mixindeployment.IntroductionDeploymentTest$TargetB */
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
     * @Introduce test.attribdef.mixindeployment.IntroductionDeploymentTest$TargetC deploymentModel=perClass
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

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.mixindeployment;

import org.codehaus.aspectwerkz.CrossCuttingInfo;

/**
 * The aspect mixin is deployed as perInstance
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @Aspect perJVM
 */
public class IntroductionDeploymentAspect {
    /**
     * Set to parse
     *
     * @Introduce within(test.mixindeployment.IntroductionDeploymentTest$TargetA) ||
     * within(test.mixindeployment.IntroductionDeploymentTest$TargetB) deploymentModel=perInstance
     */
    public static class MarkerImpl implements Marker {
        /**
         * The cross-cutting info.
         */
        private final CrossCuttingInfo m_info;

        /**
         * We are interested in cross-cutting info, therefore we have added a constructor that takes a cross-cutting
         * infor instance as its only parameter.
         *
         * @param info the cross-cutting info
         */
        public MarkerImpl(final CrossCuttingInfo info) {
            m_info = info;
        }

        public Object getTargetInstance() {
            return m_info.getMixinTargetInstance(this.getClass().getName(), this);
        }

        public Class getTargetClass() {
            return m_info.getMixinTargetClass(this.getClass().getName(), this);
        }
    }

    /**
     * Note: explicit within(..) pointcut is needed
     *
     * @Introduce within(test.mixindeployment.IntroductionDeploymentTest$TargetC) deploymentModel=perClass
     */
    public static class AnotherMarkerImpl implements Marker {
        /**
         * The cross-cutting info.
         */
        private final CrossCuttingInfo m_info;

        /**
         * We are interested in cross-cutting info, therefore we have added a constructor that takes a cross-cutting
         * infor instance as its only parameter.
         *
         * @param info the cross-cutting info
         */
        public AnotherMarkerImpl(final CrossCuttingInfo info) {
            m_info = info;
        }

        public Object getTargetInstance() {
            // will return null
            return m_info.getMixinTargetInstance(this.getClass().getName(), this);
        }

        public Class getTargetClass() {
            return m_info.getMixinTargetClass(this.getClass().getName(), this);
        }
    }
}

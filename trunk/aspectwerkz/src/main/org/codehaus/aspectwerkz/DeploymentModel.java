/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Enum containing the different deployment model types.
 * Used to be type-safe but that added to much overhead (0.00004 ms/call)
 * compared to the current implementation.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class DeploymentModel {

    public static final int PER_JVM = 0;
    public static final int PER_CLASS = 1;
    public static final int PER_INSTANCE = 2;
    public static final int PER_THREAD = 3;

    /**
     * Converts the deployment model from string to int type.
     *
     * @param type the string type
     * @return the matched deployment type
     */
    public static int getDeploymentModelAsInt(final String type) {
        if (type == null || type.equals(AspectWerkzDefinition.PER_JVM)) {
            return PER_JVM;
        }
        else if (type.equals(AspectWerkzDefinition.PER_CLASS)) {
            return PER_CLASS;
        }
        else if (type.equals(AspectWerkzDefinition.PER_INSTANCE)) {
            return PER_INSTANCE;
        }
        else if (type.equals(AspectWerkzDefinition.PER_THREAD)) {
            return PER_THREAD;
        }
        else {
            throw new RuntimeException("invalid deployment model: " + type);
        }
    }

    /**
     * Converts the deployment model from int to string type.
     *
     * @param type the int type
     * @return the string type
     */
    public static String getDeploymentModelAsString(final int type) {
        final String deploymentModel;
        switch (type) {
            case PER_JVM:
                deploymentModel = AspectWerkzDefinition.PER_JVM;
                break;
            case PER_CLASS:
                deploymentModel = AspectWerkzDefinition.PER_CLASS;
                break;
            case PER_INSTANCE:
                deploymentModel = AspectWerkzDefinition.PER_INSTANCE;
                break;
            case PER_THREAD:
                deploymentModel = AspectWerkzDefinition.PER_THREAD;
                break;
            default:
                throw new IllegalArgumentException("no such deployment model type");
        }
        return deploymentModel;
    }

    /**
     * Check mixin deployment model is compatible with aspect' ones
     * Supported models are:
     * Mixin			Aspect
     * perJVM			perJVM
     * perClass		    perJVM,perClass
     * perInstance		perJVM,perClass,perInstance
     * perThread        perThread
     *
     * @param mixinModel
     * @param aspectModel
     * @return true if compatible
     */
    public static boolean isMixinDeploymentModelCompatible(int mixinModel, int aspectModel) {
        //note: implementation depends on constant values
        if (mixinModel == PER_THREAD) {
            return (aspectModel==PER_THREAD);
        } else {
            return (mixinModel >= aspectModel);
        }
    }
}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.introduction;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Mixin that is applied to all Introductions. Enables the retrieval of
 * meta-data for the Introduction.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface HasMetaData {

    /**
     * The name of the mixin.
     */
    public static final String NAME = "org/codehaus/aspectwerkz/introduction/HasMetaData";

    /**
     * The full interface name for the mixin..
     */
    public static final String INTERFACE_CLASS = "org.codehaus.aspectwerkz.introduction.HasMetaData";

    /**
     * The full implementation name for the mixin..
     */
    public static final String IMPLEMENTATION_CLASS = "org.codehaus.aspectwerkz.introduction.HasMetaDataImpl";

    /**
     * The deployment model for the mixin.
     */
    public static final String DEPLOYMENT_MODEL = AspectWerkzDefinition.PER_INSTANCE;

    /**
     * Returns the deployment model for the introduction/advice
     *
     * @return the deployment model
     */
    int ___AW_getDeploymentModel();

    /**
     * Sets the deployment model for the advice.
     *
     * @param deploymentModel the deployment model
     */
    void ___AW_setDeploymentModel(int deploymentModel);

    /**
     * Returns the target object for the introduction/advice.
     * Returns null if the target object is deployed as perInstance.
     *
     * @return the target object
     */
    Object ___AW_getTargetObject();

    /**
     * Sets the target object for the introduction/advice.
     *
     * @param targetObject the target object
     */
    void ___AW_setTargetObject(Object targetObject);

    /**
     * Returns the target class for the introduction/advice.
     *
     * @return the target class
     */
    Class ___AW_getTargetClass();

    /**
     * Sets the target class for the introduction/advice.
     *
     * @param targetClass the target class
     */
    void ___AW_setTargetClass(Class targetClass);
}

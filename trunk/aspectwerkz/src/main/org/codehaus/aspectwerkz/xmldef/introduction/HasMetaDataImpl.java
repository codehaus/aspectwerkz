/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.introduction;

/**
 * Mixin that is applied to all Introductions. Enables the retrieval of
 * meta-data for the Introduction.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class HasMetaDataImpl implements HasMetaData {

    /**
     * The target object for the introduction that this mixin is applied to.
     */
    private Object m_targetObject;

    /**
     * The target class for the introduction that this mixin is applied to.
     */
    private Class m_targetClass;

    /**
     * The deployment model for the introduction that this mixin is applied to.
     */
    private int m_deploymentModel;

    /**
     * Returns the deployment model for the introduction/advice that this mixin is applied to.
     *
     * @return the deployment model
     */
    public int ___AW_getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Sets the deployment model for the introduction that this mixin is applied to.
     *
     * @param deploymentModel the deployment model
     */
    public void ___AW_setDeploymentModel(final int deploymentModel) {
        m_deploymentModel = deploymentModel;
    }

    /**
     * Returns the target object for the introduction that this mixin is applied to.
     * Returns null if the introduction that this mixin is applied to is deployed as perInstance.
     *
     * @return the target object
     */
    public Object ___AW_getTargetObject() {
        return m_targetObject;
    }

    /**
     * Sets the target object for the introduction that this mixin is applied to.
     *
     * @param targetObject the target object
     */
    public void ___AW_setTargetObject(final Object targetObject) {
        m_targetObject = targetObject;
    }

    /**
     * Returns the target class for the introduction that this mixin is applied to.
     *
     * @return the target class
     */
    public Class ___AW_getTargetClass() {
        return m_targetClass;
    }

    /**
     * Sets the target class for the introduction that this mixin is applied to.
     *
     * @param targetClass the target class
     */
    public void ___AW_setTargetClass(final Class targetClass) {
        m_targetClass = targetClass;
    }
}

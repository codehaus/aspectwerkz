/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.introduction;

/**
 * Mixin that is applied to all Introductions. Enables the retrieval of
 * meta-data for the Introduction.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: HasMetaDataImpl.java,v 1.1 2003-07-09 11:31:43 jboner Exp $
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

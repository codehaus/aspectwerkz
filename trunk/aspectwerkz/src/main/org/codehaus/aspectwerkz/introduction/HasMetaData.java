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

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Mixin that is applied to all Introductions. Enables the retrieval of
 * meta-data for the Introduction.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: HasMetaData.java,v 1.1 2003-07-09 11:31:43 jboner Exp $
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

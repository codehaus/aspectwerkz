/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.io.Serializable;

/**
 * Holds the introduction definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionDefinition implements Serializable {

    private String m_name;
    private String m_interface;
    private String m_implementation;
    private String m_deploymentModel;
    private String m_isPersistent;
    private String m_attribute = "";

    /**
     * Returns the name or the introduction.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name or the introduction.
     *
     * @param name the name
     */
    public void setName(final String name) {
        m_name = name.trim();
    }

    /**
     * Returns the class name of the interface.
     *
     * @return the class name
     */
    public String getInterface() {
        return m_interface;
    }

    /**
     * Sets the class name or the interface.
     *
     * @param anInterface the class name
     */
    public void setInterface(final String anInterface) {
        m_interface = anInterface.trim();
    }

    /**
     * Returns the class name or the implementation.
     *
     * @return the class name
     */
    public String getImplementation() {
        return m_implementation;
    }

    /**
     * Sets the class name of the implementation.
     *
     * @param implementation the class name
     */
    public void setImplementation(final String implementation) {
        m_implementation = implementation.trim();
    }

    /**
     * Returns the the deployment model for the advice
     *
     * @return the deployment model
     */
    public String getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Sets the deployment model for the advice.
     *
     * @param deploymentModel the deployment model
     */
    public void setDeploymentModel(final String deploymentModel) {
        m_deploymentModel = deploymentModel.trim();
    }

    /**
     * Sets the persistent attribute.
     *
     * @param isPersistent the persistent attribute
     */
    public void setIsPersistent(final String isPersistent) {
        m_isPersistent = isPersistent;
    }

    /**
     * Gets the persistent attribute.
     *
     * @return the persistent attribute
     */
    public String getIsPersistent() {
        return m_isPersistent;
    }

    /**
     * Returns the attribute.
     *
     * @return the attribute
     */
    public String getAttribute() {
        return m_attribute;
    }

    /**
     * Sets the attribute.
     *
     * @param attribute the attribute
     */
    public void setAttribute(final String attribute) {
        m_attribute = attribute;
    }

    /**
     * Checks if the introduction is persistent.
     *
     * @return true if introduction is persistent
     */
    public boolean isPersistent() {
        if (m_isPersistent != null &&
                (m_isPersistent.equalsIgnoreCase("true") ||
                m_isPersistent.equalsIgnoreCase("yes"))) {
            return true;
        }
        else {
            return false;
        }
    }
}

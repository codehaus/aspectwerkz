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
package org.codehaus.aspectwerkz.definition;

import java.io.Serializable;

/**
 * Holds the introduction definition.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: IntroductionDefinition.java,v 1.1.1.1 2003-05-11 15:13:54 jboner Exp $
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

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
import java.util.Map;

import gnu.trove.THashMap;

/**
 * Holds the advice definition.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: AdviceDefinition.java,v 1.1.1.1 2003-05-11 15:13:42 jboner Exp $
 */
public class AdviceDefinition implements Serializable {

    private String m_name;
    private String m_advice;
    private String m_deploymentModel;
    private String m_isPersistent;
    private String m_attribute = "";
    private Map m_parameters = new THashMap();

    /**
     * Returns the name of the advice.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the advice.
     *
     * @param name the name
     */
    public void setName(final String name) {
        m_name = name.trim();
    }

    /**
     * Returns the class name of the advice.
     *
     * @return the class name of the advice
     */
    public String getClassName() {
        return m_advice;
    }

    /**
     * Sets the class name of the advice.
     *
     * @param advice the class name of the advice
     */
    public void setAdvice(final String advice) {
        m_advice = advice.trim();
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
     * Gets the persistent attribute.
     *
     * @return the persistent attribute
     */
    public String getIsPersistent() {
        return m_isPersistent;
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

    /**
     * Adds a new parameter to the advice.
     *
     * @param name the name of the parameter
     * @param value the value for the parameter
     */
    public void addParameter(final String name, final String value) {
        m_parameters.put(name, value);
    }

    /**
     * Returns the parameters as a Map.
     *
     * @return the parameters
     */
    public Map getParameters() {
        return m_parameters;
    }
}

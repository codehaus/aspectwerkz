/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition2;

import java.lang.reflect.Method;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Holds the meta-data for the introductions.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionDefinition2 implements Serializable {

    /**
     * The name of the advice.
     */
    private String m_name;

    /**
     * The pointcut for the advice.
     */
    private final String m_pointcut;

    /**
     * The method for the advice.
     */
    private final Method m_method;

    /**
     * The deployment model.
     */
    private String m_deploymentModel;

    /**
     * The attribute for the advice.
     */
    private String m_attribute = "";

    /**
     * Creates a new introduction meta-data instance.
     *
     * @param name the name of the pointcut
     * @param pointcut the pointcut
     * @param method the method
     * @param deploymentModel the deployment model
     */
    public IntroductionDefinition2(final String name,
                                   final String pointcut,
                                   final Method method,
                                   final String deploymentModel) {
        m_name = name;
        m_pointcut = pointcut;
        m_method = method;
        m_deploymentModel = deploymentModel;
    }

    /**
     * Returns the name of the advice.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the pointcut.
     *
     * @return the pointcut
     */
    public String getPointcut() {
        return m_pointcut;
    }

    /**
     * Returns the method.
     *
     * @return the method
     */
    public Method getMethod() {
        return m_method;
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
}

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
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.util.Strings;

/**
 * Holds the meta-data for the advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviceDefinition2 implements Serializable {

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
     * The pointcut definition references.
     */
    private List m_pointcutRefs = null;

    /**
     * Creates a new advice meta-data instance.
     *
     * @param name the name of the pointcut
     * @param pointcut the pointcut
     * @param method the method
     * @param deploymentModel the deployment model
     */
    public AdviceDefinition2(final String name,
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

    /**
     * Returns a list with the pointcut references.
     *
     * @return the pointcut references
     */
    public List getPointcutRefs() {
        if (m_pointcutRefs != null) {
            return m_pointcutRefs;
        }
        String expression = Strings.replaceSubString(m_pointcut, "&&", "");
        expression = Strings.replaceSubString(expression, "||", "");
        expression = Strings.replaceSubString(expression, "!", "");
        expression = Strings.replaceSubString(expression, "(", "");
        expression = Strings.replaceSubString(expression, ")", "");

        m_pointcutRefs = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(expression, " ");
        while (tokenizer.hasMoreTokens()) {
            String pointcutRef = tokenizer.nextToken();
            m_pointcutRefs.add(pointcutRef);
        }
        return m_pointcutRefs;
    }
}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.util.Strings;

/**
 * Holds the meta-data for the advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviceDefinition {

    /**
     * The name of the advice.
     */
    private String m_name;

    /**
     * The aspect class name.
     */
    private final String m_aspectClassName;

    /**
     * The aspect name.
     */
    private final String m_aspectName;

    /**
     * The pointcut for the advice.
     */
    private final String m_pointcut;

    /**
     * The method for the advice.
     */
    private final Method m_method;

    /**
     * Index for the method for this advice.
     */
    private final int m_methodIndex;

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
     * The advice weaving rule.
     */
    private AdviceWeavingRule m_weavingRule;

    /**
     * Creates a new advice meta-data instance.
     *
     * @param name the name of the pointcut
     * @param aspectName the name of the aspect
     * @param aspectClassName the class name of the aspect
     * @param pointcut the pointcut
     * @param method the method
     * @param methodIndex the method index
     * @param deploymentModel the deployment model
     */
    public AdviceDefinition(final String name,
                            final String aspectName,
                            final String aspectClassName,
                            final String pointcut,
                            final Method method,
                            final int methodIndex,
                            final String deploymentModel) {
        if (name == null) throw new IllegalArgumentException("name can not be null");
        if (aspectName == null) throw new IllegalArgumentException("aspect name can not be null");
        if (aspectClassName == null) throw new IllegalArgumentException("class name can not be null");
        if (pointcut == null) throw new IllegalArgumentException("pointcut can not be null");
        if (method == null) throw new IllegalArgumentException("method can not be null");
        if (methodIndex < 0) throw new IllegalArgumentException("method index is not valid");
        if (deploymentModel == null) throw new IllegalArgumentException("deployment model can not be null");
        m_name = name;
        m_aspectName = aspectName;
        m_aspectClassName = aspectClassName;
        m_pointcut = pointcut;
        m_method = method;
        m_methodIndex = methodIndex;
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
     * Sets the name of the advice.
     *
     * @param name the name
     */
    public void setName(final String name) {
        m_name = name.trim();
    }

    /**
     * Returns the class name.
     *
     * @return the class name
     */
    public String getAspectClassName() {
        return m_aspectClassName;
    }

    /**
     * Returns the aspect name.
     *
     * @return the aspect name
     */
    public String getAspectName() {
        return m_aspectName;
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
     * Returns the method index for the introduction method.
     *
     * @return the method index
     */
    public int getMethodIndex() {
        return m_methodIndex;
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

    /**
     * Returns the weaving rule.
     *
     * @return the weaving rule
     */
    public AdviceWeavingRule getWeavingRule() {
        return m_weavingRule;
    }

    /**
     * Sets the weaving rule.
     *
     * @param weavingRule the weaving rule
     */
    public void setWeavingRule(final AdviceWeavingRule weavingRule) {
        m_weavingRule = weavingRule;
    }
}

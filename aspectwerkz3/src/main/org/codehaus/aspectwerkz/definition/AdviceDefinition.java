/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import java.lang.reflect.Method;

/**
 * Holds the meta-data for the advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviceDefinition {
    public static final String AROUND_ADVICE = "AROUND_ADVICE";
    public static final String BEFORE_ADVICE = "BEFORE_ADVICE";
    public static final String AFTER_ADVICE = "AFTER_ADVICE";

    /**
     * The name of the advice.
     */
    private String m_name;

    /**
     * The type of the advice.
     */
    private String m_type;

    /**
     * The aspect class name.
     */
    private final String m_aspectClassName;

    /**
     * The aspect name.
     */
    private final String m_aspectName;

    /**
     * The pointcut expression.
     */
    private ExpressionInfo m_expressionInfo;

    /**
     * The method for the advice.
     */
    private final Method m_method;

    /**
     * Index for the method for this advice.
     */
    private final int m_methodIndex;

    /**
     * The attribute for the advice.
     */
    private String m_attribute = "";

    /**
     * The aspect definition holding this advice definition.
     */
    private AspectDefinition m_aspectDefinition;

    /**
     * Creates a new advice meta-data instance.
     *
     * @param name            the name of the expressionInfo
     * @param type            the type of the advice
     * @param aspectName      the name of the aspect
     * @param aspectClassName the class name of the aspect
     * @param expressionInfo  the expressionInfo
     * @param method          the method
     * @param methodIndex     the method index
     */
    public AdviceDefinition(final String name, final String type, final String aspectName,
                            final String aspectClassName, final ExpressionInfo expressionInfo, final Method method,
                            final int methodIndex, final AspectDefinition aspectDef) {
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }

        if (!type.equals(AROUND_ADVICE) && !type.equals(BEFORE_ADVICE) && !type.equals(AFTER_ADVICE)) {
            throw new IllegalArgumentException("illegal advice type");
        }

        if (aspectName == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }

        if (aspectClassName == null) {
            throw new IllegalArgumentException("class name can not be null");
        }

        if (expressionInfo == null) {
            throw new IllegalArgumentException("expressionInfo can not be null");
        }

        if (method == null) {
            throw new IllegalArgumentException("method can not be null");
        }

        if (methodIndex < 0) {
            throw new IllegalArgumentException("method index is not valid");
        }

        if (aspectDef == null) {
            throw new IllegalArgumentException("aspect definition can not be null");
        }

        m_name = name;
        m_type = type;
        m_aspectName = aspectName;
        m_aspectClassName = aspectClassName;
        m_expressionInfo = expressionInfo;
        m_method = method;
        m_methodIndex = methodIndex;
        m_aspectDefinition = aspectDef;
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
     * Returns the expression.
     *
     * @return the expression
     */
    public ExpressionInfo getExpressionInfo() {
        return m_expressionInfo;
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
        return m_aspectDefinition.getDeploymentModel();
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
     * Deep copy of the definition.
     *
     * @param expressionInfo
     * @return
     */
    public AdviceDefinition copyAt(final ExpressionInfo expressionInfo) {
        return new AdviceDefinition(getName(), getType(), getAspectName(), getAspectClassName(), expressionInfo,
                                    getMethod(), getMethodIndex(), m_aspectDefinition);
    }

    /**
     * Returns the advice type, one of: AdviceDefinition.AROUND_ADVICE, AdviceDefinition.BEFORE_ADVICE or
     * AdviceDefinition.AFTER_ADVICE
     *
     * @return the advice type
     */
    public String getType() {
        return m_type;
    }
}

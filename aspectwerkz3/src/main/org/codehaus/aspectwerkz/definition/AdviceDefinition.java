/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.aspect.AdviceType;

import java.lang.reflect.Method;

/**
 * Holds the meta-data for the advices.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AdviceDefinition {

    /**
     * The name of the advice.
     * It is the advice method name and optionnaly the call signature.
     * e.g. advice or advice() or advice(JoinPoint jp) or myadvice(JoinPoint myJp , java.lang.String foo) ...
     */
    private String m_name;

    /**
     * The type of the advice.
     */
    private AdviceType m_type;

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
     * The attribute for the advice.
     */
    private String m_attribute = "";

    /**
     * The aspect definition holding this advice definition.
     */
    private AspectDefinition m_aspectDefinition;

    /**
     * The special arg type, such as returning(TYPE) or throwing(TYPE).
     */
    private String m_specialArgumentType;

    /**
     * Creates a new advice meta-data instance.
     * 
     * @param name the name of the expressionInfo
     * @param type the type of the advice
     * @param specialArgumentType the special arg type, such as returning(TYPE) or throwing(TYPE)
     * @param aspectName the name of the aspect
     * @param aspectClassName the class name of the aspect
     * @param expressionInfo the expressionInfo
     * @param method the method
     */
    public AdviceDefinition(final String name,
                            final AdviceType type,
                            final String specialArgumentType,
                            final String aspectName,
                            final String aspectClassName,
                            final ExpressionInfo expressionInfo,
                            final Method method,
                            final AspectDefinition aspectDef) {
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        if (type == null) {
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
        if (aspectDef == null) {
            throw new IllegalArgumentException("aspect definition can not be null");
        }
        m_name = name;
        m_type = type;
        m_specialArgumentType = specialArgumentType;
        m_aspectName = aspectName;
        m_aspectClassName = aspectClassName;
        m_expressionInfo = expressionInfo;
        m_method = method;
        m_aspectDefinition = aspectDef;
    }

    /**
     * Returns the advice type.
     *
     * @return the advice type
     */
    public AdviceType getType() {
        return m_type;
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
     * Returns the special arg type, such as returning(TYPE) or throwing(TYPE).
     *
     * @return
     */
    public String getSpecialArgumentType() {
        return m_specialArgumentType;
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
        return new AdviceDefinition(
            getName(),
            getType(),
            getSpecialArgumentType(),
            getAspectName(),
            getAspectClassName(),
            expressionInfo,
            getMethod(),
            m_aspectDefinition);
    }
}
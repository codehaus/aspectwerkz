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
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.util.Strings;

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
    private final MethodInfo m_method;

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
     * TODO only use this method and make ctor private?
     * <p/>
     * Creates a new advice definition.
     *
     * @param adviceName          the advice name
     * @param adviceType          the advice type
     * @param expression          the advice expression
     * @param specialArgumentType the arg
     * @param aspectName          the aspect name
     * @param aspectClassName     the aspect class name
     * @param method              the advice method
     * @param aspectDef           the aspect definition
     * @return the new advice definition
     */
    public static AdviceDefinition newInstance(final String adviceName,
                                               final AdviceType adviceType,
                                               final String expression,
                                               final String specialArgumentType,
                                               final String aspectName,
                                               final String aspectClassName,
                                               final MethodInfo method,
                                               final AspectDefinition aspectDef) {
        ExpressionInfo expressionInfo = new ExpressionInfo(
                expression,
                aspectDef.getQualifiedName()
        );

        // support for pointcut signature
        String adviceCallSignature = null;
        if (adviceName.indexOf('(') > 0) {
            adviceCallSignature = adviceName.substring(adviceName.indexOf('(') + 1, adviceName.lastIndexOf(')'));
            String[] parameters = Strings.splitString(adviceCallSignature, ",");
            for (int i = 0; i < parameters.length; i++) {
                String[] parameterInfo = Strings.splitString(
                        Strings.replaceSubString(parameters[i].trim(), "  ", " "),
                        " "
                );
                expressionInfo.addArgument(parameterInfo[1], parameterInfo[0]);
            }
        }

        return new AdviceDefinition(
                adviceName,
                adviceType,
                specialArgumentType,
                aspectName,
                aspectClassName,
                expressionInfo,
                method,
                aspectDef
        );
    }

    /**
     * Creates a new advice meta-data instance.
     *
     * @param name                the name of the expressionInfo
     * @param type                the type of the advice
     * @param specialArgumentType the special arg type, such as returning(TYPE) or throwing(TYPE)
     * @param aspectName          the name of the aspect
     * @param aspectClassName     the class name of the aspect
     * @param expressionInfo      the expressionInfo
     * @param methodInfo          the methodInfo
     */
    public AdviceDefinition(final String name,
                            final AdviceType type,
                            final String specialArgumentType,
                            final String aspectName,
                            final String aspectClassName,
                            final ExpressionInfo expressionInfo,
                            final MethodInfo methodInfo,
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
        if (methodInfo == null) {
            throw new IllegalArgumentException("methodInfo can not be null");
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
        m_method = methodInfo;
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
     * Returns the fully qualified name for the advice
     *
     * @return the fully qualified name
     */
    public String getQualifiedName() {
        return m_aspectDefinition.getQualifiedName() + '.' + m_name;
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
     * <p/>
     * TODO should return NULL object if null
     *
     * @return the expression
     */
    public ExpressionInfo getExpressionInfo() {
        return m_expressionInfo;
    }

    /**
     * Sets the expression info.
     *
     * @param newExpression the new expression info
     */
    public void setExpressionInfo(final ExpressionInfo newExpression) {
        m_expressionInfo = newExpression;
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
    public MethodInfo getMethodInfo() {
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
     * Returns the definition for the aspect that defines this advice.
     *
     * @return the aspect definition
     */
    public AspectDefinition getAspectDefinition() {
        return m_aspectDefinition;
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
                getMethodInfo(),
                m_aspectDefinition
        );
    }
}
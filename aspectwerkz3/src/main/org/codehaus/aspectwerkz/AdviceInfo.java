/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.objectweb.asm.Type;

import java.io.Serializable;

/**
 * Contains advice info, like indexes describing the aspect and a method (advice or introduced),
 * aspect manager etc.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AdviceInfo implements Serializable {

    public final static AdviceInfo[] EMPTY_ADVICE_INFO_ARRAY = new AdviceInfo[0];

    // -- some magic index used in the m_methodToArgIndexes[] so that we know what to bind except advised target args
    public final static int JOINPOINT_ARG = -1;
    public final static int STATIC_JOINPOINT_ARG = -2;
    public final static int TARGET_ARG = -3;
    public final static int THIS_ARG = -4;
    public final static int RTTI_ARG = -5;

    /**
     * The method name.
     */
    private String m_methodName;

    /**
     * The method sig.
     */
    private String m_methodSignature;

    /**
     * The method's parameter types.
     */
    private Type[] m_methodParameterTypes;

    /**
     * The advice name
     * <adviceMethodName>[(... call signature)]
     */
    private final String m_name;

    /**
     * The aspect class name where this advice is defined.
     */
    private String m_aspectClassName;

    /**
     * The aspect qualified name - <uuid>/<aspectNickName or FQNclassName>
     */
    private String m_aspectQualifiedName;

    /**
     * The aspect deployment model
     */
    private int m_aspectDeploymentModel;

    /**
     * The advice method arg index mapped to the advisED target arg index.
     * If the value is greater or equal to 0, it is an args binding. Else, it is a magic index
     * (see constants JOINPOINT_ARG, STATIC_JOINPOINT_ARG, THIS_ARG, TARGET_ARG) 
     *
     */
    private int[] m_methodToArgIndexes;

    /**
     * The "special" argument for the advice.
     */
    private String m_specialArgumentType;

    /**
     * The advice type.
     */
    private AdviceType m_type;

    private boolean m_targetWithRuntimeCheck;

    private ExpressionInfo m_expressionInfo;

    private ExpressionContext m_expressionContext;

    /**
     * Creates a new advice info.
     *
     * @param aspectQualifiedName
     * @param aspectClassName
     * @param aspectDeploymentModel
     * @param methodName
     * @param methodSignature
     * @param methodParameterTypes
     * @param type                  the advice type
     * @param specialArgumentType   the special arg type
     * @param adviceName            full qualified advice method name (aspectFQN/advice(call sig))
     * @param targetWithRuntimeCheck true if a runtime check is needed based on target instance
     * @param expressionInfo
     * @param expressionContext
     */
    public AdviceInfo(final String aspectQualifiedName,
                      final String aspectClassName,
                      final int aspectDeploymentModel,
                      final String methodName,
                      final String methodSignature,
                      final Type[] methodParameterTypes,
                      final AdviceType type,
                      final String specialArgumentType,
                      final String adviceName,
                      final boolean targetWithRuntimeCheck,
                      final ExpressionInfo expressionInfo,
                      final ExpressionContext expressionContext) {
        m_aspectQualifiedName = aspectQualifiedName;
        m_aspectClassName = aspectClassName;
        m_aspectDeploymentModel = aspectDeploymentModel;
        m_methodName = methodName;
        m_methodSignature = methodSignature;
        m_methodParameterTypes = methodParameterTypes;
        m_type = type;
        m_specialArgumentType = AsmHelper.convertReflectDescToTypeDesc(specialArgumentType);
        m_name = adviceName;
        m_targetWithRuntimeCheck = targetWithRuntimeCheck;
        m_expressionInfo = expressionInfo;
        m_expressionContext = expressionContext;
    }

    /**
     * Return the method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return m_methodName;
    }

    /**
     * Return the method signature.
     *
     * @return the method signature
     */
    public String getMethodSignature() {
        return m_methodSignature;
    }

    /**
     * Return the method name.
     *
     * @return the method name
     */
    public Type[] getMethodParameterTypes() {
        return m_methodParameterTypes;
    }

    /**
     * Returns the aspect qualified name.
     *
     * @return the aspect qualified name
     */
    public String getAspectQualifiedName() {
        return m_aspectQualifiedName;
    }

    /**
     * Returns the aspect FQN className.
     *
     * @return the aspect class name
     */
    public String getAspectClassName() {
        return m_aspectClassName;
    }

    /**
     * Returns the aspect deployment model
     *
     * @return
     */
    public int getAspectDeploymentModel() {
        return m_aspectDeploymentModel;
    }

    /**
     * Returns the name of the advice.
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the advice method to target method arg mapping A value of -1 means "not mapped"
     *
     * @param map
     */
    public void setMethodToArgIndexes(final int[] map) {
        m_methodToArgIndexes = map;
    }

    /**
     * Returns the advice method to target method arg index mapping.
     *
     * @return the indexes
     */
    public int[] getMethodToArgIndexes() {
        return m_methodToArgIndexes;
    }

    /**
     * Returns the special argument type.
     *
     * @return
     */
    public String getSpecialArgumentType() {
        return m_specialArgumentType;
    }

    /**
     * Returns the advice type.
     *
     * @return
     */
    public AdviceType getType() {
        return m_type;
    }

    public boolean hasTargetWithRuntimeCheck() {
        return m_targetWithRuntimeCheck;
    }

    public ExpressionInfo getExpressionInfo() {
        return m_expressionInfo;
    }

    public ExpressionContext getExpressionContext() {
        return m_expressionContext;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("AdviceInfo[");
        sb.append(m_type).append(',');
        sb.append(m_aspectQualifiedName).append(',');
        sb.append(m_name).append(',');
        sb.append(m_methodName).append(',');
        sb.append(m_methodSignature).append(',');
        sb.append(m_methodParameterTypes).append(',');
        sb.append(m_specialArgumentType).append(']');
        sb.append(hashCode());
        return sb.toString();
    }

}
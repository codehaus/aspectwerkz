/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Contains advice info, like indexes describing the aspect and a method (advice or introduced),
 * aspect manager etc.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AdviceInfo implements Serializable {

    public final static AdviceInfo[] ADVICE_INFO_ARRAY = new AdviceInfo[0];

    /**
     * The advice method.
     * FIXME REMOVE
     */
    private Method m_method;

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
     * The advice method arg index mapped to the target method arg index
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

    /**
     * Creates a new advice info.
     *
     * @param aspectQualifiedName
     * @param aspectClassName
     * @param aspectDeploymentModel
     * @param method         the advice method
     * @param type                the advice type
     * @param specialArgumentType the special arg type
     * @param adviceName full qualified advice method name (aspectFQN/advice(call sig))
     */
    public AdviceInfo(final String aspectQualifiedName,
                      final String aspectClassName,
                      final int aspectDeploymentModel,
                      final Method method,
                      final AdviceType type,
                      final String specialArgumentType,
                      final String adviceName) {
        m_aspectQualifiedName = aspectQualifiedName;
        m_aspectClassName = aspectClassName;
        m_aspectDeploymentModel = aspectDeploymentModel;
        m_method = method;
        m_type = type;
        m_specialArgumentType = AsmHelper.convertReflectDescToTypeDesc(specialArgumentType);
        m_name = adviceName;
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
     * Return the method.
     *
     * @return the method
     */
    public Method getMethod() {
        return m_method;
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

    public String toString() {
        StringBuffer sb = new StringBuffer("AdviceInfo[");
        sb.append(m_type).append(',');
        sb.append(m_name).append(',');
        sb.append(m_method.getName()).append(',');
        sb.append(m_specialArgumentType).append(']');
        sb.append(hashCode());
        return sb.toString();
    }
}
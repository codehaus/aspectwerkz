/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;

/**
 * Context for the expression AST evaluation.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ExpressionContext {

    /**
     * The type of the pointcut to evaluate.
     */
    private final PointcutType m_pointcutType;

    /**
     * The NameSpace in which to evaluate
     */
    private final ExpressionNamespace m_namespace;

    /**
     * The ClassMetaData on which to evaluate
     */
    private final ClassMetaData m_classMetaData;

    /**
     * The MemberMetaData on which to evaluate
     */
    private final MemberMetaData m_memberMetaData;

    /**
     * The exception FQN on which to evaluate Used with THROWS typed expression
     */
    private final String m_exceptionType;

    /**
     * Creates a new context.
     *
     * @param pointcutType the pointcut type
     */
    public ExpressionContext(final PointcutType pointcutType,
                             ExpressionNamespace namespace,
                             ClassMetaData classMetaData,
                             MemberMetaData memberMetaData,
                             String exceptionType) {
        m_pointcutType = pointcutType;
        m_namespace = namespace;
        m_classMetaData = classMetaData;
        m_memberMetaData = memberMetaData;
        m_exceptionType = exceptionType;
    }

    public PointcutType getPointcutType() {
        return m_pointcutType;
    }

    public ClassMetaData getClassMetaData() {
        return m_classMetaData;
    }

    public String getExceptionType() {
        return m_exceptionType;
    }

    public MemberMetaData getMemberMetaData() {
        return m_memberMetaData;
    }

    public ExpressionNamespace getNamespace() {
        return m_namespace;
    }
}

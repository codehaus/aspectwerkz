/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression.visitor;

import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;

import java.util.Set;

/**
 * Context for cflowEvaluateVisitor
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CflowExpressionContext {

    private ExpressionNamespace m_namespace;

    private Set m_classNameMethodMetaDataTuples;

    public CflowExpressionContext(ExpressionNamespace namespace, Set classNameMethodMetaDataTuples) {
        m_namespace = namespace;
        m_classNameMethodMetaDataTuples = classNameMethodMetaDataTuples;
    }

    public ExpressionNamespace getNamespace() {
        return m_namespace;
    }

    public Set getClassNameMethodMetaDataTuples() {
        return m_classNameMethodMetaDataTuples;
    }


}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression.visitor;

import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;

import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class TypeVisitorContext {

    private ExpressionNamespace m_namespace;

    private Set m_types;


    public TypeVisitorContext(ExpressionNamespace m_namespace) {
        this.m_namespace = m_namespace;
        m_types = new HashSet();
    }

    public ExpressionNamespace getNamespace() {
        return m_namespace;
    }

    public void setNamespace(ExpressionNamespace namespace) {
        m_namespace = namespace;
    }

    public Set getTypes() {
        return m_types;
    }

    public void addTypes(PointcutType type) {
        m_types.add(type);
    }


}

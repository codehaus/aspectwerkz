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
import org.codehaus.aspectwerkz.definition.expression.CflowExpression;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CflowIdentifierLookupVisitorContext {

    private ExpressionNamespace m_namespace;

    private Set m_names;

    private List m_anonymous;

    public CflowIdentifierLookupVisitorContext(ExpressionNamespace m_namespace) {
        this.m_namespace = m_namespace;
        m_names = new HashSet();
        m_anonymous = new ArrayList();
    }

    public ExpressionNamespace getNamespace() {
        return m_namespace;
    }

    public void setNamespace(ExpressionNamespace namespace) {
        m_namespace = namespace;
    }

    public Set getNames() {
        return m_names;
    }

    public void addName(String name) {
        m_names.add(name);
    }

    public void addNames(Set names) {
        m_names.addAll(names);
    }

    public void addAnonymous(CflowExpression expression) {
        m_anonymous.add(expression);
    }

    public List getAnonymous() {
        return m_anonymous;
    }
}

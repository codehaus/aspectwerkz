/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.util.SequencedHashMap;

import java.util.Map;
import java.util.Set;

/**
 * The 'Expression' annotation proxy.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur </a>
 */
public class ExpressionAnnotationProxy extends UntypedAnnotationProxy implements ParameterizedAnnotationProxy {
    String m_expression;

    private final Map m_argsTypeByName = new SequencedHashMap();

    public String expression() {
        return m_expression;
    }

    public void setValue(final String value) {
        m_expression = value;
    }

    public void addArgument(String argName, String className) {
        m_argsTypeByName.put(argName, className);
    }

    public Set getArgumentNames() {
        return m_argsTypeByName.keySet();
    }

    public String getArgumentType(String parameterName) {
        return (String) m_argsTypeByName.get(parameterName);
    }

}
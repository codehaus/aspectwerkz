/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.aspect.AdviceType;

import java.util.Map;
import java.util.Set;

/**
 * The advice annotation proxy base.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur </a>
 */
public class AdviceAnnotationProxyBase extends UntypedAnnotationProxy implements ParameterizedAnnotationProxy {

    protected String m_pointcut;
    protected AdviceType m_type;

    protected final Map m_argsTypeByName = new SequencedHashMap();

    public String pointcut() {
        return m_pointcut;
    }

    public void setValue(final String value) {
        m_pointcut = value;
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

    public AdviceType getType() {
        return m_type;
    }
}
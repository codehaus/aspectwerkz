/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.util.Strings;

/**
 * The 'Introduce' annotation proxy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroduceAnnotationProxy extends AnnotationProxyBase {
    /**
     * The expression for the introduction.
     */
    private String m_expression;

    /**
     * The FQN of the inner class for default introduction impl.
     */
    private String m_innerClassName;

    /**
     * The FQN of interface implemented by the inner class.
     */
    private String[] m_introducedInterfaces;

    /**
     * Deployment model for the mixin
     */
    private String m_deploymentModel;

    public String expression() {
        return m_expression;
    }

    public String deploymentModel() {
        return m_deploymentModel;
    }

    public void setvalue(String value) {
        m_expression = Strings.removeFormattingCharacters(value);
    }

    public void setdeploymentModel(final String deploymentModel) {
        m_deploymentModel = deploymentModel;
    }

    public String[] introducedInterfaces() {
        return m_introducedInterfaces;
    }

    public void setIntroducedInterfaces(String[] introducedInterfaceNames) {
        m_introducedInterfaces = introducedInterfaceNames;
    }

    public String innerClassName() {
        return m_innerClassName;
    }

    public void setInnerClassName(String innerClassName) {
        m_innerClassName = innerClassName;
    }
}

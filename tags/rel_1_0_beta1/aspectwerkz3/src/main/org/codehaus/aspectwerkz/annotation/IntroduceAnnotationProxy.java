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
public class IntroduceAnnotationProxy extends UntypedAnnotationProxy {
    private String m_expression;
    private String m_innerClassName;
    private String[] m_introducedInterfaces;
    private String m_deploymentModel;

    public String expression() {
        return m_expression;
    }

    public String deploymentModel() {
        return m_deploymentModel;
    }

    public void setValue(final String value) {
        String[] parts = Strings.splitString(value, " ");
        StringBuffer expression = new StringBuffer();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int equals = part.indexOf('=');
            if (equals > 0) {
                String name = part.substring(0, equals);
                String param = part.substring(equals + 1, part.length());
                if (name.equalsIgnoreCase("deploymentModel")) {
                    m_deploymentModel = param;
                }
            } else {
                expression.append(part);
            }
        }
        m_expression = expression.toString();
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

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.apache.xmlbeans.impl.jam.annotation.TypedAnnotationProxyBase;
import java.io.Serializable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class IntroduceProxy extends TypedAnnotationProxyBase implements Serializable {
    private String m_expression;
    private String m_deploymentModel;

    public String expression() {
        return m_expression;
    }

    public String deploymentModel() {
        return m_deploymentModel;
    }

    public void setvalue(String value) {
        m_expression = value;
    }

    public void setDeploymentModel(final String deploymentModel) {
        m_deploymentModel = deploymentModel;
    }
}

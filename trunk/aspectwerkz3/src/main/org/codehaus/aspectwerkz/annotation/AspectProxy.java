/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.apache.xmlbeans.impl.jam.annotation.TypedAnnotationProxyBase;

import java.io.Serializable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectProxy extends TypedAnnotationProxyBase implements Serializable {
    private String m_name;
    private String m_deploymentModel;

    public String name() {
        return m_name;
    }

    public String deploymentModel() {
        return m_deploymentModel;
    }

    public void setvalue(String value) {
        m_deploymentModel = value;
    }

    public void setName(final String name) {
        m_name = name;
    }
}

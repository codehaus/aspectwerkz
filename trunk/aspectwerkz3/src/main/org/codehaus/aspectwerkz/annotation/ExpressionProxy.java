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
public class ExpressionProxy extends TypedAnnotationProxyBase implements Serializable {
    String m_expression;

    public String expression() {
        return m_expression;
    }

    public void setvalue(String value) {
        m_expression = value;
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.apache.xmlbeans.impl.jam.annotation.TypedAnnotationProxyBase;
import org.codehaus.aspectwerkz.util.Strings;

import java.io.Serializable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviceAnnotationProxyBase extends TypedAnnotationProxyBase implements Serializable {
    String m_name;
    String m_pointcut;

    public String name() {
        return m_name;
    }

    public String pointcut() {
        return m_pointcut;
    }

    public void setvalue(String value) {
        m_pointcut = Strings.removeFormattingCharacters(value);
    }

    public void setname(String name) {
        m_name = name;
    }
}

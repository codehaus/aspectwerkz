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
public class AdviceProxyBase extends TypedAnnotationProxyBase implements Serializable {
    String m_name;
    String m_pointcut;

    public String name() {
        return m_name;
    }

    public String pointcut() {
        return m_pointcut;
    }

    public void setvalue(String value) {
        System.out.println("value = " + value);
        m_pointcut = value;
    }

    public void setname(String name) {
        m_name = name;
    }
}

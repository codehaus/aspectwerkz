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
 * Untyped annotation proxy.
 * <p/>
 * To be used with JavDoc-style, pure string based, one value only type of annotations
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class UntypedAnnotationProxy extends AnnotationProxyBase {
    String m_value;

    public String value() {
        return m_value;
    }

    public void setvalue(String value) {
        m_value = Strings.removeFormattingCharacters(value);
    }
}

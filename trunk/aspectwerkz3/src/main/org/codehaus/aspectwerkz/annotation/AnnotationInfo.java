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
public class AnnotationInfo implements Serializable {

    private final String m_name;
    private final TypedAnnotationProxyBase m_annotation;

    public AnnotationInfo(final String name, final TypedAnnotationProxyBase annotation) {
        m_name = name;
        m_annotation = annotation;
    }

    public String getName() {
        return m_name;
    }

    public TypedAnnotationProxyBase getAnnotation() {
        return m_annotation;
    }
}

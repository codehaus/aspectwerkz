/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Holds the annotation proxy instance and the name of the annotation.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class AnnotationInfo implements Serializable {
    private final String m_name;

    private final Annotation m_annotation;

    public AnnotationInfo(final String name, final Annotation annotation) {
        m_name = name;
        m_annotation = annotation;
    }

    public String getName() {
        return m_name;
    }

    public Annotation getAnnotation() {
        return m_annotation;
    }

}
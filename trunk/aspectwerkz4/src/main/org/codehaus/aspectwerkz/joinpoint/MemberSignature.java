/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

import java.util.List;

import org.codehaus.aspectwerkz.annotation.Annotation;

/**
 * Interface for the member signatures (method, constructor and field).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface MemberSignature extends Signature {

    /**
     * Return the annotation with a specific name.
     *
     * @param annotationName the annotation name
     * @return the annotation or null
     */
    Annotation getAnnotation(String annotationName);

    /**
     * Return a list with the annotations with a specific name.
     *
     * @param annotationName the annotation name
     * @return the annotations in a list (can be empty)
     */
    List getAnnotations(String annotationName);

    /**
     * Return all the annotations <p/>Each annotation is wrapped in
     * {@link org.codehaus.aspectwerkz.annotation.AnnotationInfo}instance.
     *
     * @return a list with the annotations
     */
    List getAnnotationInfos();
}
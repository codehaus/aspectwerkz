/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

import java.util.List;

import org.codehaus.backport175.reader.Annotation;

/**
 * Interface for the member signatures (method, constructor and field).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public interface MemberSignature extends Signature {

    /**
     * Return the given annotation if any.
     *
     * @param annotationClass the annotation class
     * @return the annotation or null
     */
    Annotation getAnnotation(Class annotationClass);

    /**
     * Return all the annotations.
     *
     * @return annotations
     */
    Annotation[] getAnnotations();
}
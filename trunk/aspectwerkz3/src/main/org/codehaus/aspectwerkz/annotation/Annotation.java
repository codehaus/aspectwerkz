/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

/**
 * Marker interface for all annotation proxy implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface Annotation {
    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName();

    /**
     * Sets the name of the annotation, the '@[name]'.
     *
     * @param name
     */
    void setName(String name);

    /**
     * Sets the full value of the annotation (including possible named parameters etc.).
     *
     * @param value
     */
    void setValue(String value);

    /**
     * Checks if the annotation is typed or not.
     *
     * @return boolean
     */
    boolean isTyped();
}

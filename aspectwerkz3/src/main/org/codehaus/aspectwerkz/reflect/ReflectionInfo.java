/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect;

import java.io.Serializable;
import java.util.List;

/**
 * Base interface for the reflection info hierarchy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public interface ReflectionInfo extends Serializable {
    /**
     * Returns the name of the class.
     *
     * @return the name of the class
     */
    String getName();

    /**
     * Returns the class modifiers.
     *
     * @return the class modifiers
     */
    int getModifiers();

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    List getAnnotations();

    /**
     * Adds an attribute.
     *
     * @param attribute the attribute
     */
    void addAnnotation(Object attribute);
}

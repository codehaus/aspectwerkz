/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;

import java.io.Serializable;
import java.util.List;

/**
 * Base interface for the metadata hierarchy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public interface MetaData extends Serializable {

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    List getAttributes();

    /**
     * Adds an attribute.
     *
     * @param attribute the attribute
     */
    void addAttribute(CustomAttribute attribute);
}

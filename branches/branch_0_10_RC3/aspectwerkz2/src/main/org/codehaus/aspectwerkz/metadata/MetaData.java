/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Base interface for the metadata hierarchy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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

    static class NullMetaData implements MetaData {
        protected final static List EMPTY_LIST = new ArrayList(0);
        protected final static String[] EMPTY_STRING_ARRAY = new String[0];

        public List getAttributes() {return EMPTY_LIST;}
        public void addAttribute(CustomAttribute attribute) {;}
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.io.Serializable;
import java.util.Collection;

/**
 * A list of all the possible target classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ClassList implements Serializable {

    /**
     * List with all the possible target classes.
     */
    private Collection m_classes;

    /**
     * Returns the classes.
     *
     * @return the classes
     */
    public Collection getClasses() {
        return m_classes;
    }

    /**
     * Appends a new list of classes to the old one.
     *
     * @param classes the classes to append
     */
    public void setClasses(final Collection classes) {
        m_classes = classes;
    }
}

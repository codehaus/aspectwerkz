/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.metadata;

import java.util.Collection;
import java.io.Serializable;

/**
 * A list of all the possible target classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: ClassList.java,v 1.1 2003-06-17 14:58:31 jboner Exp $
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

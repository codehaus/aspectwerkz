/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR and PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.transform;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Keeps track of which classes have been transformed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: TransformedClassSet.java,v 1.2 2003-07-09 05:21:28 jboner Exp $
 */
public class TransformedClassSet {

    /**
     * and set with the transformed classes.
     */
    private final Set m_transformedClassSet = new HashSet();

    /**
     * The sole instance.
     */
    private final static TransformedClassSet s_soleInstance = new TransformedClassSet();

    /**
     * Check if a specific class has been transformed.
     *
     * @param className the name of the class
     * @return boolean
     */
    public static boolean contains(final String className) {
        return getInstance().getClasses().contains(className);
    }

    /**
     * Returns an iterator for the transformed classes.
     *
     * @return an iterator
     */
    public static Iterator iterator() {
        return getInstance().getClasses().iterator();
    }

    /**
     * Adds a newly transformed class.
     *
     * @param className the name of the class
     */
    public static void add(final String className) {
        if (getInstance().getClasses().contains(className)) {
            return;
        }
        getInstance().getClasses().add(className);
    }

    /**
     * Returns the transformed class set.
     *
     * @return the transformed class set
     */
    private Set getClasses() {
        return m_transformedClassSet;
    }

    /**
     * Returns the sole instance.
     *
     * @return the sole instance
     */
    private static TransformedClassSet getInstance() {
        return s_soleInstance;
    }

    /**
     * Private constructor.
     */
    private TransformedClassSet() {
    }
}


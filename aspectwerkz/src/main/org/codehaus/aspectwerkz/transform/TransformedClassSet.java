/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Keeps track of which classes have been transformed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TransformedClassSet {

    /**
     * A set with the transformed classes.
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


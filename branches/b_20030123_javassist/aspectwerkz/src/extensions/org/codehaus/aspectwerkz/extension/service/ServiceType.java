/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.service;

/**
 * An type-safe enum that represents all the services available.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ServiceType {

    /**
     * The persistence manager type.
     */
    public static final ServiceType PERSISTENCE_MANAGER =
            new ServiceType("PERSISTENCE_MANAGER");

    private final String m_name;

    /**
     * Creates a new service type.
     *
     * @param name the name of the type
     */
    private ServiceType(final String name) {
        m_name = name;
    }

    /**
     * Returns the name of the type.
     *
     * @return the name of the type
     */
    public String toString() {
        return m_name;
    }
}

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
package org.codehaus.aspectwerkz.extension.service;

/**
 * An type-safe enum that represents all the services available.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bon�r</a>
 * @version $Id: ServiceType.java,v 1.1.1.1 2003-05-11 15:13:22 jboner Exp $
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

/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Holds meta-data for an interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: InterfaceMetaData.java,v 1.1.2.2 2003-07-22 16:20:09 avasseur Exp $
 */
public class InterfaceMetaData implements MetaData, Serializable {

    /**
     * The name of the class.
     */
    private String m_name;

    /**
     * A list with the interfaces.
     */
    private List m_interfaces = new ArrayList();

    /**
     * Returns the name of the class.
     *
     * @return the name of the class
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the class.
     *
     * @param name the name of the class
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Returns the interfaces.
     *
     * @return the interfaces
     */
    public List getInterfaces() {
        return m_interfaces;
    }

    /**
     * Sets the interfaces.
     *
     * @param interfaces the interfaces
     */
    public void setInterfaces(final List interfaces) {
        m_interfaces = interfaces;
    }
}


/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.List;
import java.util.ArrayList;

/**
 * Holds meta-data for an interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class InterfaceMetaData implements MetaData {

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


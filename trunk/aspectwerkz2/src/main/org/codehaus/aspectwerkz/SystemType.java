/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

/**
 * Type-safe enum with the system types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SystemType {

    public static final SystemType XML_DEF = new SystemType("XML_DEF");
    public static final SystemType ATTRIB_DEF = new SystemType("ATTRIB_DEF");

    private final String m_name;

    private SystemType(final String name) {
        m_name = name;
    }

    public String toString() {
        return m_name;
    }
}

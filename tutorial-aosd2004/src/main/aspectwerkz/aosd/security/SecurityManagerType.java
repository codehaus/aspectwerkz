/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.security;

/**
 * Typesafe enum for the security manager types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SecurityManagerType {

    /**
     * The JAAS security manager type.
     */
    public static final SecurityManagerType JAAS = new SecurityManagerType("JAAS");

    /**
     * The name of the type.
     */
    private final String m_name;

    /**
     * Returns the string representation of the type.
     *
     * @return the string representation
     */
    public String toString() {
        return m_name;
    }

    /**
     * Creates a new security manager type enum.
     *
     * @param name the name of the type
     */
    private SecurityManagerType(final String name) {
        m_name = name;
    }
}

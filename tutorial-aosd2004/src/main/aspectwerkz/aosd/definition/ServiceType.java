/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.definition;

/**
 * An enumeration class that represents all the services available to the user.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ServiceType {

    public static final ServiceType SECURITY_MANAGER = new ServiceType("SECURITY_MANAGER");
    public static final ServiceType TRANSACTION_MANAGER = new ServiceType("TRANSACTION_MANAGER");

    private final String m_name;

    /**
     * Returns the name of the type.
     *
     * @return the name of the type
     */
    public String toString() {
        return m_name;
    }

    /**
     * Creates a new service type.
     *
     * @param name the name of the type
     */
    private ServiceType(final String name) {
        m_name = name;
    }
}

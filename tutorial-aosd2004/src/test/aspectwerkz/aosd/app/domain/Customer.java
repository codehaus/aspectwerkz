/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.app.domain;

import aspectwerkz.aosd.app.domain.Address;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Customer {

    private String m_firstName;
    private String m_lastName;
    private Address m_address;

    public Customer(String firstname, String lastname) {
        m_firstName = firstname;
        m_lastName = lastname;
    }

    public void setFirstName(String firstName) {
        m_firstName = firstName;
    }

    public String getFirstName() {
        return m_firstName;
    }

    public void setLastName(String lastName) {
        m_lastName = lastName;
    }

    public String getLastName() {
        return m_lastName;
    }

    public void setAddress(final Address address) {
        m_address = address;
    }

    public void doSomethingIllegal() {
        throw new RuntimeException("Forced exception");
    }

    public String getKey() {
        return m_firstName + m_lastName;
    }
}


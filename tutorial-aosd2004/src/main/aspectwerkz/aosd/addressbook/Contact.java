/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.addressbook;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Contact implements Serializable {

    public static final Contact NULL = new NullContact();

    private final String m_firstName;
    private final String m_lastName;
    private final List m_emailAddresses = new ArrayList();

    public Contact(final String firstName, final String lastName) {
        m_firstName = firstName;
        m_lastName = lastName;
    }

    public String getFirstName() {
        return m_firstName;
    }

    public String getLastName() {
        return m_lastName;
    }

    public List getEmailAddresses() {
        return m_emailAddresses;
    }

    public void addEmailAddress(final String emailAddress) {
        m_emailAddresses.add(emailAddress);
    }

    public boolean isNull() {
        return this instanceof NullContact;
    }

    public String toString() {
        return m_firstName + "|" + m_lastName + "|" + m_emailAddresses.size() + " emails";
    }

    public String getId() {
        return m_firstName + "." + m_lastName;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;

        final Contact contact = (Contact) o;

        if (!m_firstName.equals(contact.m_firstName)) return false;
        if (!m_lastName.equals(contact.m_lastName)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = m_firstName.hashCode();
        result = 29 * result + m_lastName.hashCode();
        return result;
    }

    /**
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
     */
    private static class NullContact extends Contact {
        public NullContact() {
            super("", "");
        }
    }

}

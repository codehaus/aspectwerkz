/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.addressbook;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AddressBook implements Serializable {

    private final Set m_contacts = new HashSet();

    void addContact(final Contact contact) {
        m_contacts.add(contact);
    }

    public Set getContacts() {
        return m_contacts;
    }

    public Contact findContact(final String firstName, final String lastName) {
        for (Iterator it = m_contacts.iterator(); it.hasNext();) {
            Contact contact = (Contact)it.next();
            if (contact.getFirstName().equalsIgnoreCase(firstName)
                    && contact.getLastName().equalsIgnoreCase(lastName)) {
                return contact;
            }
        }
        return Contact.NULL;
    }

    public Contact findContact(final String id) {
        for (Iterator it = m_contacts.iterator(); it.hasNext();) {
            Contact contact = (Contact)it.next();
            if (contact.getId().equalsIgnoreCase(id)) {
                return contact;
            }
        }
        return Contact.NULL;
    }

    void removeContacts(Set contacts) {
        for (Iterator it = contacts.iterator(); it.hasNext();) {
            m_contacts.remove(it.next());
        }
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.addressbook;

import java.util.Set;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AddressBookManagerImpl implements AddressBookManager {

    public Contact addContact(AddressBook addressBook, String firstName, String lastName, String email) {
        Contact contact = new Contact(firstName, lastName);
        contact.addEmailAddress(email);
        addressBook.addContact(contact);
        return contact;
    }

    public void removeContacts(AddressBook addressBook, Set contacts) {
         addressBook.removeContacts(contacts);
    }



}

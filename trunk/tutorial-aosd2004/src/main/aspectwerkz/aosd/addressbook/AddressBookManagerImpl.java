/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.addressbook;

import aspectwerkz.aosd.persistence.jisp.JispPersistenceManager;

import java.util.Set;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AddressBookManagerImpl implements AddressBookManager {

    public AddressBook newAddressBook(String owner) {
        //TODO could we have an around advice to fetch from JISP instead ?
        AddressBook addressBook = (AddressBook)JispPersistenceManager.getInstance().retrieve(AddressBook.class, owner);
        if (addressBook == null) {
            System.out.println("created ADB for owner " + owner);
            addressBook = new AddressBook(owner);// AOP persist that
        }
        return addressBook;
    }

    public Contact newContact(final String firstName, final String lastName) {
        return new Contact(firstName, lastName);
    }

    public Contact addContact(AddressBook addressBook, String firstName, String lastName, String email) {
        Contact contact = newContact(firstName, lastName);
        contact.addEmailAddress(email);
        addressBook.addContact(contact);
        return contact;
    }

    public void removeContacts(AddressBook addressBook, Set contacts) {
         addressBook.removeContacts(contacts);
    }



}

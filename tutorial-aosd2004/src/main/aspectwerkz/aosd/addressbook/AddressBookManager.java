/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.addressbook;

import java.util.Set;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AddressBookManager {

    public AddressBook newAddressBook(String owner);

    public Contact newContact(final String firstName, final String lastName);

    public Contact addContact(AddressBook addressBook, String firstName, String lastName, String email);

    public void removeContacts(AddressBook addressBook, Set contacts);

}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.user;

import aspectwerkz.aosd.addressbook.AddressBookManager;
import aspectwerkz.aosd.addressbook.AddressBookManagerImpl;

/**
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Registry {

    /**
     * Returns the UserManager.
     *
     * @return the user manager
     */
    public static UserManager getUserManager() {
        return new UserManagerImpl();
    }

    public static AddressBookManager getAddressBookManager() {
        return new AddressBookManagerImpl();
   }
}

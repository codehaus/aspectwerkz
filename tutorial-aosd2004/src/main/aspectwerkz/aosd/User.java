/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd;

import java.io.Serializable;

import aspectwerkz.aosd.addressbook.AddressBook;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class User implements Serializable {

    private final String m_username;
    private final String m_password;
    private final AddressBook m_addressBook = new AddressBook();

    public User(final String username, final String password) {
        m_username = username;
        m_password = password;
    }

    public String getUsername() {
        return m_username;
    }

    public String getPassword() {
        return m_password;
    }

    public AddressBook getAddressBook() {
        return m_addressBook;
    }

    public String getKey() {
        return m_username + m_password;
    }

    public boolean isNull() {
        return this instanceof NullUser;
    }

    /**
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
     */
    private static class NullUser extends User {
        public NullUser() {
            super("", "");
        }
    }
}

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

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AddressBook {

    private final List m_entries = new ArrayList();

    public void addEntry(final Entry entry) {
        m_entries.add(entry);
    }

    public List getEntries() {
        return m_entries;
    }

    public Entry findEntryForUser(final String firstName, final String lastName) {
        for (Iterator it = m_entries.iterator(); it.hasNext();) {
            Entry entry = (Entry)it.next();
            if (entry.getFirstName().equalsIgnoreCase(firstName)
                    && entry.getLastName().equalsIgnoreCase(lastName)) {
                return entry;
            }
        }
        return Entry.NULL;
    }
}

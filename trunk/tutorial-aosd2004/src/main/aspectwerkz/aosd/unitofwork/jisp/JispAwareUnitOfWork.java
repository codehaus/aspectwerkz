/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork.jisp;

import java.util.Iterator;

import aspectwerkz.aosd.unitofwork.UnitOfWork;
import aspectwerkz.aosd.unitofwork.ObjectBackup;
import aspectwerkz.aosd.persistence.PersistenceManager;
import aspectwerkz.aosd.persistence.jisp.JispPersistenceManager;
import aspectwerkz.aosd.addressbook.AddressBook;

/**
 * A transaction implementation that persists all persistable objects on commit.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JispAwareUnitOfWork extends UnitOfWork {

    /**
     * The persistence manager.
     */
    PersistenceManager s_persistenceManager = JispPersistenceManager.getInstance();

    /**
     * Creates a new UnitOfWork.
     */
    public JispAwareUnitOfWork() {
        super();
    }

    /**
     * Implements the template method <code>doCommit</code> in the {@link aspectwerkz.aosd.unitofwork.jisp.JispAwareUnitOfWork}
     * base class, will be called by the framework on commit.
     * <p/>
     * Commits the tx and stores all the persistent objects.
     */
    public void doCommit() {
        for (Iterator it = m_dirtyObjects.values().iterator(); it.hasNext();) {
            ObjectBackup backup = (ObjectBackup)it.next();
            //System.out.println("storing " + backup + " " + backup.getReference());
            //if (backup.getReference().getClass().getName().equals(AddressBook.class.getName()))//TODO remove this check
                s_persistenceManager.store(backup.getReference());
        }
    }
}


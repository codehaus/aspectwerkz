/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork;

import java.util.Iterator;
import java.util.Set;

/**
 * A transaction implementation that persists all persistable objects on commit.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PersistableUnitOfWork extends UnitOfWork {

    /**
     * Commits the tx and stores all the persistent objects.
     */
    public void commit() {
        storePersistableObjects();
    }

    /**
     * Stores all the persistable objects that have been modified
     * in the transaction.
     */
    protected void storePersistableObjects() {
        for (Iterator it = m_dirtyObjects.values().iterator(); it.hasNext();) {
            ObjectBackup backup = (ObjectBackup)it.next();
            Object ref = backup.getReference();
            Object obj = backup.getObject();
            Set dirtyFields = backup.getDirtyFields();

            // store the dirty fields (or the whole instance) in the persistent storage
        }
    }

    /**
     * Private constructor.
     */
    protected PersistableUnitOfWork() {
        super();
    }
}


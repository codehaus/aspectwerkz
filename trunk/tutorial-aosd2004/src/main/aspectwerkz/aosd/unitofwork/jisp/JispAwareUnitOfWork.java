/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork.jisp;

import java.util.Iterator;
import java.io.Serializable;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import aspectwerkz.aosd.unitofwork.UnitOfWork;
import aspectwerkz.aosd.unitofwork.ObjectBackup;
import aspectwerkz.aosd.persistence.PersistenceManager;
import aspectwerkz.aosd.persistence.PersistenceManagerException;
import aspectwerkz.aosd.persistence.jisp.JispPersistenceManager;
import aspectwerkz.aosd.definition.JispDefinition;
import aspectwerkz.aosd.User;

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

        // TODO: move definition of persistence manager to application startup
        JispDefinition definition = new JispDefinition();
        definition.setName("aosd2004");
        definition.setCreateDbOnStartup(false);
        JispDefinition.PersistentObjectDefinition objectDef = new JispDefinition.PersistentObjectDefinition();
        objectDef.setClassname(User.class.getName());
        JispDefinition.PersistentObjectDefinition.Index index = new JispDefinition.PersistentObjectDefinition.Index();
        index.setName("User");
        index.setKeyMethod("getKey");
        objectDef.addIndex(index);
        definition.addPersistentObjectDefinition(objectDef);
        JispDefinition.BTreeIndexDefinition btreeIndex = new JispDefinition.BTreeIndexDefinition();
        btreeIndex.setName("string.btree");
        btreeIndex.setKeyType("com.coyotegulch.jisp.StringKey32");
        btreeIndex.setOrder(23);
        definition.addBtreeIndex(btreeIndex);
        s_persistenceManager.initialize(Thread.currentThread().getContextClassLoader(), definition);
    }

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
            try {
                s_persistenceManager.store((Serializable)backup.getReference());
            }
            catch (PersistenceManagerException e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }
}


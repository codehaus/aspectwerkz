/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork;

import aspectwerkz.aosd.transaction.TransactionContext;

/**
 * Transactional is an mixin, which means that it is an interface with method implementations added to it.
 * <p/>All domain objects that want to be persistable should enhanced by this mixin.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface Transactional {

    /**
     * Stores the persistable object in persistent storage.
     */
    void create();

    /**
     * Removes the persistable object from persistent storage.
     */
    void remove();

    /**
     * Clones the persistable object.
     *
     * @return a clone of the persistable object
     */
    Object clone();

    /**
     * Checks if the persistable object exists in the persistent storage.
     *
     * @return true if the object exists in the persistent storage
     */
     boolean exists();

    /**
     * Marks the persistable object as dirty.
     * <p/>Is handled automatically by the framework but this method exists
     * for the users to be able to do it explicitly.
     */
    void markDirty();

    /**
     * Marks the current UnitOfWork and attached transaction as restoreObject only.
     */
    void setRollbackOnly();

    /**
     * Returns the current UnitOfWork.
     *
     * @return the current UnitOfWork
     */
    UnitOfWork getUnitOfWork();

    /**
     * Returns the current transaction.
     *
     * @return the current transaction
     */
    TransactionContext getTransaction();
}


/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence;

import java.util.List;

import org.codehaus.aspectwerkz.extension.service.Service;

/**
 * An interface that all persistence managers should implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface PersistenceManager extends Service {

    /**
     * Stores/Updates an object in the database.
     * If the object already exists in the database it makes an update.
     *
     * @param obj the object to store/update
     * @throws org.codehaus.aspectwerkz.extension.persistence.PersistenceManagerException
     */

    void store(final Object obj) throws PersistenceManagerException;

    /**
     * Finds an object from the database by its key.
     *
     * @param klass the class of the object to find
     * @param key the key of the object to find
     * @return the object
     * @throws org.codehaus.aspectwerkz.extension.persistence.PersistenceManagerException
     */
    Object retrieve(final Class klass, final Object key) throws PersistenceManagerException;

    /**
     * Removes an object from the database.
     *
     * @param obj the object to remove
     * @throws org.codehaus.aspectwerkz.extension.persistence.PersistenceManagerException
     */
    void remove(final Object obj) throws PersistenceManagerException;

    /**
     * Retrieves all objects within a specific range.
     *
     * @param klass the class of the object to find
     * @param from index to start range from
     * @param to index to end range at
     * @return a list with all the objects within the range
     * @throws org.codehaus.aspectwerkz.extension.persistence.PersistenceManagerException
     */
    List retrieveAllInRange(final Class klass, final Object from, final Object to)
            throws PersistenceManagerException;
}


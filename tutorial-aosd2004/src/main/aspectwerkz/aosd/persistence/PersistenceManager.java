/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.persistence;

import java.util.Collection;
import java.io.Serializable;

import aspectwerkz.aosd.definition.Definition;

/**
 * An interface that all persistence managers should implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface PersistenceManager {

    /**
     * Initializes the persistence manager.
     * Creates the database and the indexes (BTree and Hash indexes).
     *
     * @param definition the persistence definition
     * @param loader the classloader to use
     */
    void initialize(ClassLoader loader, Definition definition);

    /**
     * Stores/Updates an object in the database.
     * If the object already exists in the database it makes an update.
     *
     * @param obj the object to store/update
     * @throws aspectwerkz.aosd.persistence.PersistenceManagerException
     */

    void store(Serializable obj) throws PersistenceManagerException;

    /**
     * Finds an object from the database by its key.
     *
     * @param klass the class of the object to find
     * @param key the key of the object to find
     * @return the object
     * @throws aspectwerkz.aosd.persistence.PersistenceManagerException
     */
    Object retrieve(Class klass, Object key) throws PersistenceManagerException;

    /**
     * Removes an object from the database.
     *
     * @param obj the object to remove
     * @throws aspectwerkz.aosd.persistence.PersistenceManagerException
     */
    void remove(Serializable obj) throws PersistenceManagerException;

    /**
     * Retrieves all objects within a specific range.
     *
     * @param klass the class of the object to find
     * @param from index to start range from
     * @param to index to end range at
     * @return a collection with all the objects within the range
     * @throws aspectwerkz.aosd.persistence.PersistenceManagerException
     */
    Collection retrieveAllInRange(Class klass, Object from, Object to) throws PersistenceManagerException;
}


/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence;

import java.util.List;

import org.codehaus.aspectwerkz.extension.persistence.definition.PersistenceDefinition;

/**
 * Base class for all persistence manager implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractPersistenceManager implements PersistenceManager {

    /**
     * The class loader to use.
     */
    protected ClassLoader m_loader;

    /**
     * The persistence definition.
     */
    protected PersistenceDefinition m_definition;

    /**
     * Marks the manager as initialized.
     */
    protected boolean m_initialized = false;

    /**
     * Stores/Updates an object in the database.
     * If the object already exists in the database it makes an update.
     *
     * @param obj the object to store/update
     */
    public abstract void store(final Object obj);

    /**
     * Finds an object from the database by its key.
     *
     * @param klass the class of the object to find
     * @param key the key of the object to find
     * @return the object
     */
    public abstract Object retrieve(final Class klass, final Object key);

    /**
     * Retrieves all objects within a specific range.
     *
     * @param klass the class of the object to find
     * @param from index to start range from
     * @param to index to end range at
     * @return a collection with all the objects within the range
     */
    public abstract List retrieveAllInRange(final Class klass,
                                            final Object from,
                                            final Object to);

    /**
     * Removes an object from the database.
     *
     * @param obj the object to remove
     */
    public abstract void remove(final Object obj);

    /**
     * Checks if the service has been initialized.
     *
     * @return boolean
     */
    protected boolean notInitialized() {
        return !m_initialized;
    }
}

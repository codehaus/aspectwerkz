/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension;

import java.util.List;

import org.codehaus.aspectwerkz.extension.service.ServiceType;
import org.codehaus.aspectwerkz.extension.service.ServiceManager;
import org.codehaus.aspectwerkz.extension.persistence.PersistenceManager;
import org.codehaus.aspectwerkz.extension.persistence.PersistenceManagerException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * The registry is simply a gateway to the services available to the client.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Registry {

    /**
     * The sole Registry instance.
     */
    private static Registry s_soleInstance = new Registry();

    /**
     * The persistence manager.
     */
    private PersistenceManager m_persistenceManager;

    /**
     * Finds a persistent object by its index.
     *
     * @param klass the class of the object
     * @param index the index for the record
     * @return the object
     */
    public static Object findPersistentObjectByIndex(final Class klass,
                                                     final Object index) {
        Object obj = null;
        try {
            obj = getInstance().m_persistenceManager.retrieve(klass, index);
        }
        catch (PersistenceManagerException e) {
            throw new WrappedRuntimeException(e);
        }
        return obj;
    }

    /**
     * Finds a persistent object by its index.
     *
     * @param klass the class of the object
     * @param startIndex the index start index for the sequence
     * @param endIndex the index end index for the sequence
     * @return the objects
     */
    public static List findPersistentObjectsWithinRange(final Class klass,
                                                        final Object startIndex,
                                                        final Object endIndex) {
        List objects = null;
        try {
            objects = getInstance().m_persistenceManager.
                    retrieveAllInRange(klass, startIndex, endIndex);
        }
        catch (PersistenceManagerException e) {
            throw new WrappedRuntimeException(e);
        }
        return objects;
    }

    /**
     * Removes an persistent object from the database.
     *
     * @param object the object to remove
     */
    public static void removePersistentObject(final Object object) {
        try {
            getInstance().m_persistenceManager.remove(object);
        }
        catch (PersistenceManagerException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the sole instance.
     *
     * @return the instance
     */
    private static Registry getInstance() {
        return s_soleInstance;
    }

    /**
     * Creates a new registry A initializes the services.
     */
    private Registry() {
        m_persistenceManager = (PersistenceManager)ServiceManager.
                getService(ServiceType.PERSISTENCE_MANAGER);
    }
}

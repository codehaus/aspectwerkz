/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: Registry.java,v 1.1.1.1 2003-05-11 15:13:09 jboner Exp $
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
     * Creates a new registry and initializes the services.
     */
    private Registry() {
        m_persistenceManager = (PersistenceManager)ServiceManager.
                getService(ServiceType.PERSISTENCE_MANAGER);
    }
}

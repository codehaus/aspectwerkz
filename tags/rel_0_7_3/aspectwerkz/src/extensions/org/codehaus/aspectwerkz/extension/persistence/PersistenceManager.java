/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
package org.codehaus.aspectwerkz.extension.persistence;

import java.util.List;

import org.codehaus.aspectwerkz.extension.service.Service;

/**
 * An interface that all persistence managers should implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: PersistenceManager.java,v 1.4 2003-07-07 08:09:25 jboner Exp $
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


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
package org.codehaus.aspectwerkz.extension.persistence;

import java.util.List;

import org.codehaus.aspectwerkz.extension.persistence.definition.PersistenceDefinition;

/**
 * Base class for all persistence manager implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AbstractPersistenceManager.java,v 1.2 2003-06-09 07:04:12 jboner Exp $
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
     * Advises the all the persistent classes.
     *
     * @param definition the persistence definition
     */
    protected void advisePersistentObjects(final PersistenceDefinition definition) {
        // register the DirtyFieldCheckAdvice
//        AspectWerkz.register(
//                DirtyFieldCheckAdvice.NAME, new DirtyFieldCheckAdvice());

//        List persistentObjects = definition.getPersistentObjects();
//        for (Iterator it = persistentObjects.iterator(); it.hasNext();) {
//            PersistentObjectDefinition def = (PersistentObjectDefinition)it.next();
//
//            AspectWerkz.createAspect(def.getClassName()).
//                    createSetFieldPointcut(DirtyFieldCheckAdvice.PATTERN).
//                    addPostAdvice(DirtyFieldCheckAdvice.NAME);
//        }
    }

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

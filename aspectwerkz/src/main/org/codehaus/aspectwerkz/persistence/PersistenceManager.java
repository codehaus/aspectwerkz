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
package org.codehaus.aspectwerkz.persistence;

/**
 * Interface that all persistence managers must implement.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: PersistenceManager.java,v 1.1.1.1 2003-05-11 15:14:37 jboner Exp $
 */
public interface PersistenceManager {

    /**
     * Initializes the persistence manager.
     */
    void initialize();

    /**
     * Registers a new aspect component (advice or introduction) as persistent.
     *
     * @param aspectComponentUuid the uuid of the aspect component to register
     */
    void register(final String aspectComponentUuid);

    /**
     * Creates an new object in the database.
     *
     * @param obj the object to create
     * @param index the index for the object
     * @param deploymentModel the deployment model for the object
     */
    void create(final Object obj,
                final Object index,
                final int deploymentModel);

    /**
     * Updates an object in the database.
     *
     * @param modifiedField the modified field to update
     * @param index the index for the object
     * @param aspectComponentUuid the uuid of the aspect component holding the introduction
     * @param deploymentModel the deployment model for the object
     */
    void update(final ModifiedField modifiedField,
                final Object index,
                final Object aspectComponentUuid,
                final int deploymentModel);

    /**
     * Finds an object from the database by its index.
     *
     * @param aspectComponentUuid the uuid of the aspect component
     * @param index the index
     * @param deploymentModel the deployment model for the object
     * @return the object
     */
    Object retrieve(final Object aspectComponentUuid,
                    final Object index,
                    final int deploymentModel);

    /**
     * Removes all instances of an object for a specific aspect component.
     *
     * @param aspectComponentUuid the aspect component uuid
     * @param deploymentModel the deployement model of the object to remove
     */
    void removeAll(final Object aspectComponentUuid,
                   final int deploymentModel);
}


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
package org.codehaus.aspectwerkz.extension.persistence.definition;

import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 * Holds the definition of the persistence concern.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: PersistenceDefinition.java,v 1.1.1.1 2003-05-11 15:13:15 jboner Exp $
 */
public class PersistenceDefinition implements Definition {

    private final List m_indexes = new ArrayList();
    private final List m_persistenceManagers = new ArrayList();
    private final List m_persistentObjects = new ArrayList();

    public List getIndexes() {
        return m_indexes;
    }

    public void addIndex(final IndexDefinition index) {
        m_indexes.add(index);
    }

    public List getPersistenceManagers() {
        return m_persistenceManagers;
    }

    public void addPersistenceManager(final PersistenceManagerDefinition pm) {
        m_persistenceManagers.add(pm);
    }

    public List getPersistentObjects() {
        return m_persistentObjects;
    }

    public void addPersistentObject(
            final PersistentObjectDefinition persistentObject) {
        m_persistentObjects.add(persistentObject);
    }
}

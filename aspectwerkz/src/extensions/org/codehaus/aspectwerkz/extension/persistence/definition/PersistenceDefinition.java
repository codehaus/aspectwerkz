/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence.definition;

import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 * Holds the definition of the persistence concern.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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

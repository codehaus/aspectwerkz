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
package org.codehaus.aspectwerkz.extension.persistence.jisp;

import java.lang.reflect.Method;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;

import com.coyotegulch.jisp.KeyObject;
import com.coyotegulch.jisp.ObjectIndex;
import com.coyotegulch.jisp.BTreeIndex;
import com.coyotegulch.jisp.BTreeObjectIterator;
import com.coyotegulch.jisp.IndexedObjectDatabase;

import org.codehaus.aspectwerkz.extension.persistence.definition.PersistenceDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.IndexDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.PersistenceManagerDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.PersistentObjectDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.IndexRefDefinition;
import org.codehaus.aspectwerkz.extension.persistence.AbstractPersistenceManager;
import org.codehaus.aspectwerkz.extension.persistence.PersistenceManager;
import org.codehaus.aspectwerkz.extension.persistence.Persistable;
import org.codehaus.aspectwerkz.extension.persistence.Index;
import org.codehaus.aspectwerkz.extension.definition.Definition;
import org.codehaus.aspectwerkz.extension.objectfactory.ObjectFactory;
import org.codehaus.aspectwerkz.extension.objectfactory.DefaultObjectFactory;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * An implementation of the PersistenceManager interface using JISP.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: JispPersistenceManager.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class JispPersistenceManager
        extends AbstractPersistenceManager implements Serializable {

    public static final boolean CREATE = true;
    public static final boolean LOAD = false;
    public static final int INDEX_ORDER = 23;

    /**
     * The sole instance of the persistence manager.
     */
    protected final static PersistenceManager s_soleInstance =
            new JispPersistenceManager();

    /**
     * The JISP database.
     */
    protected IndexedObjectDatabase m_database;

    /**
     * A read-write lock to ensure ACID:ity.
     */
    protected ReadWriteLock m_readWriteLock = new WriterPreferenceReadWriteLock();
//    protected final ReadWriteLock m_readWriteLock = new FIFOReadWriteLock();

    /**
     * Holds the indexes for each persistent object.
     */
    protected final Map m_indexes = new HashMap();

    /**
     * Maps each index name to a the JISP object index.
     */
    protected final Map m_jispIndexes = new HashMap();

    /**
     * Maps each index name to a the JISP object index type.
     */
    protected final Map m_jispIndexTypes = new HashMap();

    /**
     * A list with the definitions for all the indexes added to
     * this persistence manager.
     */
    protected List m_indexDefinitions = new ArrayList();

    /**
     * The name of the database
     */
    protected String m_databaseName;

    /**
     * The full URL for the database
     */
    protected String m_databaseUrl = null;

    /**
     * The name of the database directory.
     */
    protected String m_databaseDir = null;

    /**
     * The name of the index directory.
     */
    protected String m_indexDir = null;

    /**
     * Marks if a new database should be created each time the service starts.
     */
    protected boolean m_createNewDbOnStartup = false;

    /**
     * Returns the one and only instance of the JispPersistenceManager.
     * Singleton.
     *
     * @return the instance
     */
    public static PersistenceManager getInstance() {
        return s_soleInstance;
    }

    /**
     * Initializes the persistence manager.
     * Creates the database and the indexes (BTree and Hash indexes).
     *
     * @param loader the classloader to use
     * @param definition the persistence definition
     */
    public synchronized void initialize(final ClassLoader loader,
                                        final Definition definition) {
        if (m_initialized) return;
        if (definition == null) throw new IllegalArgumentException("definition can not be null");

        m_loader = loader;
        m_definition = (PersistenceDefinition)definition;

        advisePersistentObjects(m_definition);

        PersistenceManagerDefinition persistenceManagerDef = null;
        for (Iterator it = m_definition.getPersistenceManagers().iterator(); it.hasNext();) {
            PersistenceManagerDefinition def = (PersistenceManagerDefinition)it.next();
            if (def.getClassName().equals(getClass().getName())) {
                persistenceManagerDef = def;
                break;
            }
        }
        if (persistenceManagerDef == null) throw new RuntimeException("no definition specified for persistence manager: " + getClass().getName());

        m_indexDefinitions = getIndexes(persistenceManagerDef);
        if (m_indexDefinitions.size() == 0) throw new RuntimeException("no indexes specified for persistence manager");

        final Properties properties = persistenceManagerDef.getProperties();
        m_databaseDir = properties.getProperty(
                PersistenceManagerDefinition.DATABASE_DIR);
        m_databaseName = properties.getProperty(
                PersistenceManagerDefinition.DATABASE_NAME);
        m_indexDir = properties.getProperty(
                PersistenceManagerDefinition.INDEX_DIR);

        final StringBuffer buf = new StringBuffer();
        buf.append(m_databaseDir);
        buf.append(File.separator);
        buf.append(m_databaseName);
        m_databaseUrl = buf.toString();

        final File db = new File(m_databaseUrl);

        if (m_createNewDbOnStartup || !db.exists()) {
            createDatabaseDir(m_databaseDir);
            createDatabase(db, m_indexDefinitions);
        }
        else {
            loadDatabase(m_indexDefinitions);
        }

        loadDomainObjectConfigurations(m_definition);

        Collections.unmodifiableMap(m_indexes);
        Collections.unmodifiableMap(m_jispIndexes);
        Collections.unmodifiableMap(m_jispIndexTypes);

        m_initialized = true;
    }

    /**
     * Stores/Updates an object in the database.
     * If the object already exists in the database it makes an update.
     *
     * @param obj the object to store/update
     */
    public void store(final Object obj) {
        if (notInitialized()) throw new IllegalStateException("persistence manager is not initialized");
        if (obj == null) throw new IllegalArgumentException("object to store can not be null");
        if (!(obj instanceof Persistable)) throw new IllegalArgumentException("object to store does not implement Persistable");

        Class klass = obj.getClass();
        List keys = (List)m_indexes.get(klass.getName());
        KeyObject[] keyArray = new KeyObject[m_indexDefinitions.size()];

        int i = 0;
        for (Iterator it = keys.iterator(); it.hasNext(); i++) {
            try {
                final Index index = (Index)it.next();

                final Method method = index.getMethod();
                final Class indexFieldType = index.getFieldType();
                final Class indexType = (Class)m_jispIndexTypes.get(index.getIndexName());

                final Object indexFieldValue = method.invoke(obj, new Object[]{});

                ObjectFactory factory = new DefaultObjectFactory(
                        indexType,
                        new Class[]{indexFieldType},
                        new Object[]{indexFieldValue});

                keyArray[i] = (KeyObject)factory.newInstance();
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }

        // fill up the key array with null keys if needed
        for (; i < m_indexDefinitions.size(); i++) {
            keyArray[i] = keyArray[0].makeNullKey();
        }

        try {
            m_readWriteLock.writeLock().acquire();
            try {
                m_database.write(keyArray, (Persistable)obj);
            }
            finally {
                m_readWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            notifyAll();
            Thread.currentThread().interrupt();
            throw new WrappedRuntimeException(e);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Finds an object from the database by its key.
     *
     * @param klass the class of the object to find
     * @param key the key of the object to find
     * @return the object
     */
    public Object retrieve(final Class klass, final Object key) {
        if (notInitialized()) throw new IllegalStateException("persistence manager is not initialized");
        if (klass == null || key == null) throw new IllegalArgumentException("class or index can not be null");

        final List indexes = (List)m_indexes.get(klass.getName());
        String indexName = null;
        for (Iterator it = indexes.iterator(); it.hasNext();) {
            Index index = (Index)it.next();
            if (index.getFieldType() == key.getClass()) {
                indexName = index.getIndexName();
            }
        }

        if (indexName == null) throw new RuntimeException("no such index for class specified");

        final ObjectIndex index = (ObjectIndex)m_jispIndexes.get(indexName);
        final Class indexType = (Class)m_jispIndexTypes.get(indexName);

        Object obj = null;
        try {
            ObjectFactory factory = new DefaultObjectFactory(
                    indexType, new Class[]{key.getClass()}, new Object[]{key});

            m_readWriteLock.readLock().acquire();
            try {
                obj = m_database.read(
                        (KeyObject)factory.newInstance(), index, m_loader);
            }
            finally {
                m_readWriteLock.readLock().release();
            }
        }
        catch (InterruptedException e) {
            notifyAll();
            Thread.currentThread().interrupt();
            throw new WrappedRuntimeException(e);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return obj;
    }

    /**
     * Retrieves all objects within a specific range.
     *
     * @param klass the class of the object to find
     * @param from index to start range from
     * @param to index to end range at
     * @return a collection with all the objects within the range
     */
    public List retrieveAllInRange(final Class klass,
                                   final Object from,
                                   final Object to) {
        if (notInitialized()) throw new IllegalStateException("persistence manager is not initialized");
        if (!(from instanceof Comparable) || !(to instanceof Comparable))
            throw new RuntimeException("from and to index must implement the Comparable interface");
        if (!from.getClass().equals(to.getClass()))
            throw new RuntimeException("from and to index must be of the same type");

        final List col = new ArrayList();
        final List keys = (List)m_indexes.get(klass.getName());

        String indexNameFrom = null;
        String indexNameTo = null;
        Method keyMethodTo = null;

        for (Iterator it = keys.iterator(); it.hasNext();) {
            Index index = (Index)it.next();

            if (index.getFieldType() == from.getClass()) {
                indexNameFrom = index.getIndexName();
            }
            if (index.getFieldType() == to.getClass()) {
                indexNameTo = index.getIndexName();
                keyMethodTo = index.getMethod();
            }
        }
        if (indexNameFrom == null || indexNameTo == null) {
            throw new RuntimeException("no such index for class specified");
        }

        final ObjectIndex indexFrom = (ObjectIndex)m_jispIndexes.get(indexNameFrom);
        final ObjectIndex indexTo = (ObjectIndex)m_jispIndexes.get(indexNameTo);
        final Class indexTypeFrom = (Class)m_jispIndexTypes.get(indexNameFrom);
        final Class indexTypeTo = (Class)m_jispIndexTypes.get(indexNameTo);

        if (!(indexFrom instanceof BTreeIndex) || !(indexTo instanceof BTreeIndex)) {
            throw new RuntimeException("from and to index must both be of type BTreeIndex");
        }
        if (!indexTypeFrom.equals(indexTypeTo)) {
            throw new RuntimeException("from and to index must be of the same index type");
        }

        try {
            ObjectFactory fromFactory = new DefaultObjectFactory(
                    indexTypeFrom,
                    new Class[]{from.getClass()},
                    new Object[]{from});

            m_readWriteLock.readLock().acquire();
            try {
                final BTreeObjectIterator iterator =
                        m_database.createIterator((BTreeIndex)indexFrom);

                if (!iterator.moveTo((KeyObject)fromFactory.newInstance())) {
                    throw new RuntimeException("record " + from + " does not exist");
                }
                do {
                    final Object item = iterator.getObject();
                    if (item != null) {
                        col.add(item);
                    }
                    else {
                        break;
                    }
                    final Object keyFieldValue =
                            keyMethodTo.invoke(item, new Object[]{});
                    if (((Comparable)keyFieldValue).compareTo(to) == 0) {
                        break;
                    }
                }
                while (iterator.moveNext());
            }
            finally {
                m_readWriteLock.readLock().release();
            }
        }
        catch (InterruptedException e) {
            notifyAll();
            Thread.currentThread().interrupt();
            throw new WrappedRuntimeException(e);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return col;
    }

    /**
     * Removes an object from the database.
     *
     * @param obj the object to remove
     */
    public void remove(final Object obj) {
        if (notInitialized()) throw new IllegalStateException("persistence manager is not initialized");
        if (obj == null) throw new IllegalArgumentException("object to remove can not be null");
        if (!(obj instanceof Persistable)) throw new IllegalArgumentException("object to remove does not implement Persistable");

        final Class klass = obj.getClass();

        final List keys = getKeysForPersistentObject(klass);
        final KeyObject[] keyArray = new KeyObject[keys.size()];

        int i = 0;
        for (Iterator it = keys.iterator(); it.hasNext(); i++) {
            try {
                final Index index = (Index)it.next();

                final Method method = index.getMethod();
                final Class indexFieldType = index.getFieldType();
                final Class indexType =
                        (Class)m_jispIndexTypes.get(index.getIndexName());

                final Object indexFieldValue = method.invoke(obj, new Object[]{});

                ObjectFactory factory = new DefaultObjectFactory(
                        indexType,
                        new Class[]{indexFieldType},
                        new Object[]{indexFieldValue});

                keyArray[i] = (KeyObject)factory.newInstance();
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        try {
            m_readWriteLock.writeLock().acquire();
            try {
                m_database.remove(keyArray);
            }
            finally {
                m_readWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            notifyAll();
            Thread.currentThread().interrupt();
            throw new WrappedRuntimeException(e);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns all the index definitions for this persistence manager.
     *
     * @param persistenceManagerDef the persistence definition
     * @return a list with all the index definition
     */
    protected List getIndexes(PersistenceManagerDefinition persistenceManagerDef) {
        final List btreeIndexes = new ArrayList();
        final List indexRefDefinitions = persistenceManagerDef.getIndexRefs();
        for (Iterator it = indexRefDefinitions.iterator(); it.hasNext();) {
            String indexName = ((IndexRefDefinition)it.next()).getName();
            List indexDefinitions = m_definition.getIndexes();
            for (Iterator it2 = indexDefinitions.iterator(); it2.hasNext();) {
                IndexDefinition indexDefinition = (IndexDefinition)it2.next();
                if (indexDefinition.getName().equals(indexName)) {
                    btreeIndexes.add(indexDefinition);
                }
            }
        }
        return btreeIndexes;
    }

    /**
     * Gets the domain object definition from the server definition.
     *
     * @param def the persistence definition
     */
    protected void loadDomainObjectConfigurations(final PersistenceDefinition def) {
        if (def == null) throw new IllegalArgumentException("persistence definition can not be null");

        final List persistentObjectDefs = def.getPersistentObjects();

        for (Iterator it1 = persistentObjectDefs.iterator(); it1.hasNext();) {
            final PersistentObjectDefinition persistentObjectDef =
                    (PersistentObjectDefinition)it1.next();

            final List indexRefs = persistentObjectDef.getIndexRefs();
            final String persistentObjectTypeName = persistentObjectDef.getClassName();
            if (indexRefs.size() == 0) throw new RuntimeException("no indexes specified for persistent object: " + persistentObjectTypeName);

            Class persistentObjectType = null;
            try {
                persistentObjectType = m_loader.loadClass(persistentObjectTypeName);
            }
            catch (ClassNotFoundException e) {
                continue;
            }

            m_indexes.put(persistentObjectTypeName, new ArrayList());

            for (Iterator it2 = indexRefs.iterator(); it2.hasNext();) {
                IndexRefDefinition index = (IndexRefDefinition)it2.next();

                final String indexName = index.getName();
                final String indexMethodName = index.getMethod();
                try {
                    final Method method = persistentObjectType.getDeclaredMethod(
                            indexMethodName, new Class[]{});

                    Class indexFieldType = method.getReturnType();

                    if (indexFieldType.getName().equals(long.class.getName())) {
                        indexFieldType = Long.class;
                    }
                    else if (indexFieldType.getName().equals(int.class.getName())) {
                        indexFieldType = Integer.class;
                    }
                    else if (indexFieldType.getName().equals(double.class.getName())) {
                        indexFieldType = Double.class;
                    }
                    else if (indexFieldType.getName().equals(float.class.getName())) {
                        indexFieldType = Float.class;
                    }
                    else if (indexFieldType.getName().equals(short.class.getName())) {
                        indexFieldType = Short.class;
                    }
                    else if (indexFieldType.getName().equals(boolean.class.getName())) {
                        throw new RuntimeException("index field can not be boolean");
                    }
                    ((List)m_indexes.get(persistentObjectTypeName)).add(
                            new Index(method, indexFieldType, indexName));
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
    }

    /**
     * Creates a new jisp database.
     *
     * @param btreeIndexes
     */
    protected void createDatabase(final File db, final List btreeIndexes) {
        if (db == null || btreeIndexes == null) throw new IllegalArgumentException("db or index definition can not be null");

        try {
            if (db.exists()) {
                db.delete();
            }
            m_database = new IndexedObjectDatabase(
                    m_databaseUrl, CREATE, m_loader);

            final List indexes = createBtreeIndexes(btreeIndexes);
            for (Iterator it = indexes.iterator(); it.hasNext();) {
                m_database.attachIndex((BTreeIndex)it.next());
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Loads the jisp database.
     *
     * @param btreeIndexes
     */
    protected void loadDatabase(final List btreeIndexes) {
        if (btreeIndexes == null) throw new IllegalArgumentException("index definition can not be null");

        try {
            m_database = new IndexedObjectDatabase(m_databaseUrl, LOAD, m_loader);

            final List indexes = loadBtreeIndexes(btreeIndexes);
            for (Iterator it = indexes.iterator(); it.hasNext();) {
                m_database.attachIndex((BTreeIndex)it.next());
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Creates the btree indexes.
     *
     * @param btreeIndexes
     * @return the indexes
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.IOException
     */
    protected List createBtreeIndexes(final List btreeIndexes)
            throws ClassNotFoundException, IOException {
        if (btreeIndexes == null) throw new IllegalArgumentException("index configurations can not be null");

        final List indexes = new ArrayList();
        for (Iterator it = btreeIndexes.iterator(); it.hasNext();) {

            final IndexDefinition indexDef = (IndexDefinition)it.next();
            final String jispIndexType = getJispIndexType(indexDef.getType());

            ObjectFactory factory = new DefaultObjectFactory(jispIndexType);

            final StringBuffer indexFile = new StringBuffer();
            indexFile.append(m_indexDir);
            indexFile.append(File.separator);
            indexFile.append(indexDef.getName());

            File killit = new File(indexFile.toString());
            if (killit.exists()) killit.delete();

            final BTreeIndex index = new BTreeIndex(
                    indexFile.toString(),
                    INDEX_ORDER,
                    (KeyObject)factory.newInstance(),
                    false, m_loader);

            indexes.add(index);

            m_jispIndexes.put(indexDef.getName(), index);

            final Class indexType = m_loader.loadClass(
                    getJispIndexType(indexDef.getType()));

            m_jispIndexTypes.put(indexDef.getName(), indexType);
        }
        return indexes;
    }

    /**
     * Loads the btree indexes.
     *
     * @param btreeIndexes
     * @return the indexes
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.IOException
     */
    protected List loadBtreeIndexes(final List btreeIndexes)
            throws ClassNotFoundException, IOException {
        if (btreeIndexes == null) throw new IllegalArgumentException("index configurations can not be null");

        final List indexes = new ArrayList();

        for (Iterator it = btreeIndexes.iterator(); it.hasNext();) {

            final IndexDefinition indexDef = (IndexDefinition)it.next();

            final StringBuffer indexFile = new StringBuffer();
            indexFile.append(m_databaseDir);
            indexFile.append(File.separator);
            indexFile.append(indexDef.getName());

            final BTreeIndex index = new BTreeIndex(indexFile.toString(), m_loader);
            indexes.add(index);

            m_jispIndexes.put(indexDef.getName(), index);

            final Class indexType = m_loader.loadClass(
                    getJispIndexType(indexDef.getType()));

            m_jispIndexTypes.put(indexDef.getName(), indexType);
        }
        return indexes;
    }

    /**
     * Returns the mapped JISP index type.
     *
     * @todo support more JISP index types than String and Long
     *
     * @param type the index type
     * @return the JISP index type
     */
    protected String getJispIndexType(final String type) {
        final String indexType;
        if (type.equals("java.lang.String")) {
            indexType = "com.coyotegulch.jisp.StringKey32";
        }
        else if (type.equals("java.lang.Long")) {
            indexType = "org.codehaus.aspectwerkz.extension.persistence.jisp.LongKey";
        }
        else {
            throw new RuntimeException(type + " is not a valid JISP index type");
        }
        return indexType;
    }

    /**
     * Returns the keys to the object.
     *
     * @param klass the class of the object of interest
     * @return the keys
     */
    protected List getKeysForPersistentObject(final Class klass) {
        return (List)m_indexes.get(klass.getName());
    }

    /**
     * Create the database dir (if it does not exist).
     *
     * @param databaseDir the meta-data dir
     */
    protected static void createDatabaseDir(final String databaseDir) {
        File dir = new File(databaseDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("could not create directory " + databaseDir);
            }
        }
    }

    /**
     *  Non-public constructor to prevent instantiability.
     */
    protected JispPersistenceManager() {
    }
}




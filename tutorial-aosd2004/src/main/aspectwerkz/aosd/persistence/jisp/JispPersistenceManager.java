/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.persistence.jisp;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import com.coyotegulch.jisp.KeyObject;
import com.coyotegulch.jisp.ObjectIndex;
import com.coyotegulch.jisp.HashIndex;
import com.coyotegulch.jisp.BTreeIndex;
import com.coyotegulch.jisp.BTreeObjectIterator;
import com.coyotegulch.jisp.IndexedObjectDatabase;

import aspectwerkz.aosd.persistence.IndexInfo;
import aspectwerkz.aosd.persistence.PersistenceManager;
import aspectwerkz.aosd.persistence.ObjectFactory;
import aspectwerkz.aosd.definition.Definition;
import aspectwerkz.aosd.definition.JispDefinition;

/**
 * An implementation of the PersistenceManager interface using JISP.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JispPersistenceManager implements PersistenceManager, Serializable {

    private static final JispPersistenceManager s_soleInstance = new JispPersistenceManager();
    private static final boolean CREATE = true;
    private static final boolean NO_CREATE = false;

    private IndexedObjectDatabase m_database = null;
    private ClassLoader m_loader = null;
    private boolean m_initialized = false;

    private final ReadWriteLock m_rw = new WriterPreferenceReadWriteLock();

    private final Map m_keys = new HashMap();
    private final Map m_indexes = new HashMap();
    private final Map m_indexTypes = new HashMap();

    private String JISP_DB = "./jisp.db";
    private String m_dbPath = null;
    private boolean m_createDbOnStartup;

    /**
     * Returns the one and only instance of the JispPersistenceManager.
     * Singleton.
     *
     * @return the instance
     */
    public static JispPersistenceManager getInstance() {
        return s_soleInstance;
    }

    /**
     * Initializes the persistence manager.
     * Creates the database and the indexes (BTree and Hash indexes).
     *
     * @param definition the persistence definition
     * @param loader the classloader to use
     */
    public synchronized void initialize(final ClassLoader loader, final Definition definition) {
        if (m_initialized) return;
        if (definition == null) throw new IllegalArgumentException("definition can not be null");

        JispDefinition jispDefinition = (JispDefinition)definition;

        m_loader = loader;
        m_dbPath = jispDefinition.getDbPath() + File.separator;
        m_createDbOnStartup = jispDefinition.getCreateDbOnStartup();

        Collection btreeIndexes = jispDefinition.getBtreeIndexes();
        Collection hashIndexes = jispDefinition.getHashIndexes();

        JISP_DB = m_dbPath + jispDefinition.getName();
        File db = new File(JISP_DB);

        if (m_createDbOnStartup || !db.exists()) {
            createDb(db, btreeIndexes, hashIndexes);
        }
        else {
            loadDb(btreeIndexes, hashIndexes);
        }
        loadDomainObjectConfigurations(jispDefinition);

        Collections.unmodifiableMap(m_keys);
        Collections.unmodifiableMap(m_indexes);
        Collections.unmodifiableMap(m_indexTypes);

        m_initialized = true;
    }

    /**
     * Stores/Updates an object in the database.
     * If the object already exists in the database it makes an update.
     *
     * @param obj the object to store/update
     */
    public void store(final Object obj) {
        if (notInitialized()) throw new IllegalStateException("jisp persistence manager is not initialized");
        if (obj == null) throw new IllegalArgumentException("object to store can not be null");
        if (!(obj instanceof Serializable)) throw new IllegalArgumentException("object to store must be serializable");
        Class klass = obj.getClass();
        Collection keys = (Collection)m_keys.get(klass.getName());
        KeyObject[] keyArray = new KeyObject[keys.size()];

        int i = 0;
        for (Iterator it = keys.iterator(); it.hasNext(); i++) {
            try {
                IndexInfo keyInfo = (IndexInfo)it.next();

                Method method = keyInfo.getMethod();
                Class keyFieldType = keyInfo.getFieldType();
                Class indexType = (Class)m_indexTypes.get(keyInfo.getIndexName());

                Object keyFieldValue = method.invoke(obj, new Object[]{});

                Constructor constructor = indexType.getConstructor(new Class[]{keyFieldType});
                keyArray[i] = (KeyObject)constructor.newInstance(new Object[]{keyFieldValue});
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        try {
            m_rw.writeLock().acquire();
            try {
                m_database.write(keyArray, (Serializable)obj);
            }
            finally {
                m_rw.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            notifyAll();
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
        if (notInitialized()) throw new IllegalStateException("jisp persistence manager is not initialized");
        if (klass == null || key == null) throw new IllegalArgumentException("klass or index can not be null");

        System.out.println("retrieve for " + klass);//TODO REMOVE

        Collection keys = (Collection)m_keys.get(klass.getName());
        String indexName = null;
        if (keys == null) {
            throw new IllegalArgumentException("klass is not persistent: " + klass.getName());
        }
        for (Iterator it = keys.iterator(); it.hasNext();) {
            IndexInfo keyInfo = (IndexInfo)it.next();
            if (keyInfo.getFieldType() == key.getClass()) {
                indexName = keyInfo.getIndexName();
            }
        }

        if (indexName == null) throw new RuntimeException("no such index for class specified");

        ObjectIndex index = (ObjectIndex)m_indexes.get(indexName);
        Class indexType = (Class)m_indexTypes.get(indexName);

        Object obj = null;
        try {
            Constructor constructor = indexType.getConstructor(new Class[]{key.getClass()});
            KeyObject keyObject = (KeyObject)constructor.newInstance(new Object[]{key});

            m_rw.readLock().acquire();
            try {
                obj = m_database.read(keyObject, index, m_loader);
            }
            finally {
                m_rw.readLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            notifyAll();
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
    public Collection retrieveAllInRange(final Class klass, final Object from, final Object to) {
        if (notInitialized()) throw new IllegalStateException("jisp persistence manager is not initialized");
        if (!(from instanceof Comparable) || !(to instanceof Comparable))
            throw new RuntimeException("from and to index must implement the Comparable interface");
        if (!from.getClass().equals(to.getClass()))
            throw new RuntimeException("from and to index must be of the same type");

        Collection col = new ArrayList();
        Collection keys = (Collection)m_keys.get(klass.getName());

        String indexNameFrom = null;
        String indexNameTo = null;
        Method keyMethodTo = null;

        for (Iterator it = keys.iterator(); it.hasNext();) {
            IndexInfo keyInfo = (IndexInfo)it.next();

            if (keyInfo.getFieldType() == from.getClass()) {
                indexNameFrom = keyInfo.getIndexName();
            }
            if (keyInfo.getFieldType() == to.getClass()) {
                indexNameTo = keyInfo.getIndexName();
                keyMethodTo = keyInfo.getMethod();
            }
        }
        if (indexNameFrom == null || indexNameTo == null)
            throw new RuntimeException("no such index for class specified");

        ObjectIndex indexFrom = (ObjectIndex)m_indexes.get(indexNameFrom);
        ObjectIndex indexTo = (ObjectIndex)m_indexes.get(indexNameTo);
        Class indexTypeFrom = (Class)m_indexTypes.get(indexNameFrom);
        Class indexTypeTo = (Class)m_indexTypes.get(indexNameTo);

        if (!(indexFrom instanceof BTreeIndex) || !(indexTo instanceof BTreeIndex))
            throw new RuntimeException("from and to index must both have a BTree index");
        if (!indexTypeFrom.equals(indexTypeTo))
            throw new RuntimeException("from and to index must be of the same index type");

        try {
            Constructor constructor = indexTypeFrom.getConstructor(new Class[]{from.getClass()});
            KeyObject keyObject = (KeyObject)constructor.newInstance(new Object[]{from});

            m_rw.readLock().acquire();
            try {
                BTreeObjectIterator iterator = m_database.createIterator((BTreeIndex)indexFrom);
                if (!iterator.moveTo(keyObject))
                    throw new RuntimeException("record " + from + " does not exist");

                do {
                    Object item = iterator.getObject();
                    if (item != null) {
                        col.add(item);
                    }
                    else {
                        break;
                    }

                    Object keyFieldValue = keyMethodTo.invoke(item, new Object[]{});
                    if (((Comparable)keyFieldValue).compareTo(to) == 0) break;
                }
                while (iterator.moveNext());
            }
            finally {
                m_rw.readLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            notifyAll();
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
        if (notInitialized()) throw new IllegalStateException("jisp persistence manager is not initialized");
        if (obj == null) throw new IllegalArgumentException("object to remove can not be null");
        if (!(obj instanceof Serializable)) throw new IllegalArgumentException("object to remove must be serializable");

        Class klass = obj.getClass();

        Collection keys = getKeysForPersistentObject(klass);
        KeyObject[] keyArray = new KeyObject[keys.size()];

        int i = 0;
        for (Iterator it = keys.iterator(); it.hasNext(); i++) {
            try {
                IndexInfo keyInfo = (IndexInfo)it.next();

                Method method = keyInfo.getMethod();
                Class keyFieldType = keyInfo.getFieldType();
                Class indexType = (Class)m_indexTypes.get(keyInfo.getIndexName());

                Object keyFieldValue = method.invoke(obj, new Object[]{});

                Constructor constructor = indexType.getConstructor(new Class[]{keyFieldType});
                keyArray[i] = (KeyObject)constructor.newInstance(new Object[]{keyFieldValue});
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        try {
            m_rw.writeLock().acquire();
            try {
                m_database.remove(keyArray);

            }
            finally {
                m_rw.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            notifyAll();
            throw new WrappedRuntimeException(e);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Optimizes the hash index.
     *
     * @param indexName
     */
    public void optimizeHashIndex(final String indexName) {
        if (notInitialized()) throw new IllegalStateException("jisp persistence manager is not initialized");
        if (indexName == null) throw new IllegalArgumentException("index name can not be null");

        try {
            HashIndex index = (HashIndex)m_indexes.get(indexName);
            index.optimize();
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Gets the domain object definition from the server definition.
     *
     * @param def the persistence definition
     */
    private void loadDomainObjectConfigurations(final JispDefinition def) {
        if (def == null) throw new IllegalArgumentException("persistence definition can not be null");

        Collection definitions = def.getPersistentObjectDefinitions();

        for (Iterator it1 = definitions.iterator(); it1.hasNext();) {
            JispDefinition.PersistentObjectDefinition definition =
                    (JispDefinition.PersistentObjectDefinition)it1.next();

            Collection indexes = definition.getIndexes();
            String persistentObjectTypeName = definition.getClassname();
            Class persistentObjectType = null;
            try {
                persistentObjectType = m_loader.loadClass(persistentObjectTypeName);
            }
            catch (ClassNotFoundException e) {
                continue;
            }

            m_keys.put(persistentObjectTypeName, new ArrayList());

            for (Iterator it2 = indexes.iterator(); it2.hasNext();) {
                JispDefinition.PersistentObjectDefinition.Index index =
                        (JispDefinition.PersistentObjectDefinition.Index)it2.next();

                final String indexName = index.getName();
                String keyMethodName = index.getKeyMethod();

                try {
                    Method method = persistentObjectType.getDeclaredMethod(keyMethodName, new Class[]{});
                    Class keyFieldType = method.getReturnType();

                    if (keyFieldType == long.class) {
                        keyFieldType = Long.class;
                    }
                    else if (keyFieldType == int.class) {
                        keyFieldType = Integer.class;
                    }
                    else if (keyFieldType == double.class) {
                        keyFieldType = Double.class;
                    }
                    else if (keyFieldType == float.class) {
                        keyFieldType = Float.class;
                    }
                    else if (keyFieldType == short.class) {
                        keyFieldType = Short.class;
                    }
                    else if (keyFieldType == boolean.class) {
                        throw new RuntimeException("index field can not be boolean");
                    }
                    ((Collection)m_keys.get(persistentObjectTypeName)).add(
                            new IndexInfo(method, keyFieldType, indexName)
                    );
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
     * @param hashIndexes
     */
    private void createDb(final File db, final Collection btreeIndexes, final Collection hashIndexes) {
        if (db == null || btreeIndexes == null || hashIndexes == null)
            throw new IllegalArgumentException("db or index configurations can not be null");

        try {
            if (db.exists()) db.delete();

            m_database = new IndexedObjectDatabase(JISP_DB, CREATE, m_loader);

            Collection indexes = createBtreeIndexes(btreeIndexes);
            for (Iterator it = indexes.iterator(); it.hasNext();) {
                m_database.attachIndex((BTreeIndex)it.next());
            }

            indexes = createHashIndexes(hashIndexes);
            for (Iterator it = indexes.iterator(); it.hasNext();) {
                m_database.attachIndex((HashIndex)it.next());
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
     * @param hashIndexes
     */
    private void loadDb(final Collection btreeIndexes, final Collection hashIndexes) {
        if (btreeIndexes == null || hashIndexes == null)
            throw new IllegalArgumentException("index configurations can not be null");

        try {
            m_database = new IndexedObjectDatabase(JISP_DB, NO_CREATE, m_loader);

            Collection indexes = loadBtreeIndexes(btreeIndexes);
            for (Iterator it = indexes.iterator(); it.hasNext();) {
                m_database.attachIndex((BTreeIndex)it.next());
            }

            indexes = loadHashIndexes(hashIndexes);
            for (Iterator it = indexes.iterator(); it.hasNext();) {
                m_database.attachIndex((HashIndex)it.next());
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
    private Collection createBtreeIndexes(final Collection btreeIndexes)
            throws ClassNotFoundException, IOException {
        if (btreeIndexes == null) throw new IllegalArgumentException("index configurations can not be null");

        Collection indexes = new ArrayList();
        for (Iterator it = btreeIndexes.iterator(); it.hasNext();) {

            JispDefinition.BTreeIndexDefinition indexDef = (JispDefinition.BTreeIndexDefinition)it.next();

            ObjectFactory factory = new ObjectFactory(indexDef.getKeyType());

            String indexFile = m_dbPath + indexDef.getName();

            File killit = new File(indexFile);
            if (killit.exists()) killit.delete();

            BTreeIndex index = new BTreeIndex(
                    indexFile, indexDef.getOrder(),
                    (KeyObject)factory.newInstance(),
                    false, m_loader
            );

            indexes.add(index);

            m_indexes.put(indexDef.getName(), index);
            final Class indexType = m_loader.loadClass(indexDef.getKeyType());
            m_indexTypes.put(indexDef.getName(), indexType);
        }
        return indexes;
    }

    /**
     * Create the hash indexes.
     *
     * @param hashIndexes
     * @return the indexes
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.IOException
     */
    private Collection createHashIndexes(final Collection hashIndexes)
            throws ClassNotFoundException, IOException {
        if (hashIndexes == null) throw new IllegalArgumentException("index configurations can not be null");

        Collection indexes = new ArrayList();

        for (Iterator it = hashIndexes.iterator(); it.hasNext();) {

            JispDefinition.HashIndexDefinition indexDef = (JispDefinition.HashIndexDefinition)it.next();

            ObjectFactory factory = new ObjectFactory(indexDef.getKeyType());
            String indexFile = m_dbPath + indexDef.getName();

            File killit = new File(indexFile);
            if (killit.exists()) killit.delete();

            HashIndex index = new HashIndex(
                    indexFile, indexDef.getBuckets(),
                    indexDef.getDbSize(),
                    (KeyObject)factory.newInstance(),
                    m_loader
            );

            indexes.add(index);

            m_indexes.put(indexDef.getName(), index);
            final Class indexType = m_loader.loadClass(indexDef.getKeyType());
            m_indexTypes.put(indexDef.getName(), indexType);
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
    private Collection loadBtreeIndexes(final Collection btreeIndexes)
            throws ClassNotFoundException, IOException {
        if (btreeIndexes == null) throw new IllegalArgumentException("index configurations can not be null");

        Collection indexes = new ArrayList();

        for (Iterator it = btreeIndexes.iterator(); it.hasNext();) {

            JispDefinition.BTreeIndexDefinition indexDef = (JispDefinition.BTreeIndexDefinition)it.next();
            String indexFile = m_dbPath + indexDef.getName();

            BTreeIndex index = new BTreeIndex(indexFile, m_loader);
            indexes.add(index);

            m_indexes.put(indexDef.getName(), index);
            final Class indexType = m_loader.loadClass(indexDef.getKeyType());
            m_indexTypes.put(indexDef.getName(), indexType);
        }
        return indexes;
    }

    /**
     * Loads the hash indexes.
     *
     * @param hashIndexes
     * @return the indexes
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.IOException
     */
    private Collection loadHashIndexes(final Collection hashIndexes)
            throws ClassNotFoundException, IOException {
        if (hashIndexes == null) throw new IllegalArgumentException("index configurations can not be null");

        Collection indexes = new ArrayList();

        for (Iterator it = hashIndexes.iterator(); it.hasNext();) {

            JispDefinition.HashIndexDefinition indexDef = (JispDefinition.HashIndexDefinition)it.next();
            String indexFile = m_dbPath + indexDef.getName();

            HashIndex index = new HashIndex(indexFile, m_loader);
            indexes.add(index);

            m_indexes.put(indexDef.getName(), index);
            final Class indexType = m_loader.loadClass(indexDef.getKeyType());
            m_indexTypes.put(indexDef.getName(), indexType);
        }
        return indexes;
    }

    /**
     * Returns the Keys to the object.
     *
     * @param klass the class of the object of interest
     * @return the keys
     */
    private Collection getKeysForPersistentObject(final Class klass) {
        return (Collection)m_keys.get(klass.getName());
    }

    /**
     * Checks if the service has been initialized.
     *
     * @return boolean
     */
    private boolean notInitialized() {
        return !m_initialized;
    }

    /**
     *  Non public constructor to prevent instantiability.
     */
    private JispPersistenceManager() {
    }
}

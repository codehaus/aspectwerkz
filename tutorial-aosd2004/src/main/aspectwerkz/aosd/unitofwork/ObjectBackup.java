package aspectwerkz.aosd.unitofwork;

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.HashSet;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Holds a backup to the instance in transaction to be able to make a restoreObject.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ObjectBackup {

    /**
     * The class for the backed up instance
     */
    private final Class m_class;
    private final byte[] m_backup;
    private final Object m_reference;
    private final Set m_dirtyFields = new HashSet();
//    private final Set m_collections = new HashSet();

    /**
     * Creates a new <code>ObjectBackup</code> instance and wrappes the instance passed to it.
     *
     * @param obj the instance to make backup on.
     * @return the instance wrapped in an <code>ObjectBackup</code> instance
     */
    public static ObjectBackup newInstance(final Object obj) {
        return new ObjectBackup(obj);
    }

    /**
     * Gets the backed up object.
     *
     * @return the backed up object
     */
    public Object getObject() {
        Object obj;
        try {
            ByteArrayInputStream istream = new ByteArrayInputStream(m_backup);
            ObjectInputStream s = new ObjectInputStream(istream);
            obj = s.readObject();
            istream.close();
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return obj;
    }

    /**
     * Gets the type of the backed up object.
     *
     * @return the type of the backed up object
     */
    public Class getType() {
        return m_class;
    }

    /**
     * Gets a reference to the backed up object.
     *
     * @return the reference to the backed up object
     */
    public Object getReference() {
        return m_reference;
    }

    /**
     * Returns a list with the names of the fields that are dirty.
     *
     * @return the names of the fields that are dirty
     */
    public Set getDirtyFields() {
        return m_dirtyFields;
    }

    /**
     * Registers a new dirty field.
     *
     * @param fieldName the name of the dirty field
     */
    public void registerDirtyField(final String fieldName) {
        if (!m_dirtyFields.contains(fieldName)) {
            m_dirtyFields.add(fieldName);
        }
    }

    /**
     * Creates a new ObjectBackup instance.
     *
     * @param obj the instance to make a backup of
     */
    private ObjectBackup(final Object obj) {
        m_reference = obj;
        m_class = obj.getClass();
        try {
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(ostream);
            stream.writeObject(obj);
            stream.flush();
            m_backup = ostream.toByteArray();
            ostream.close();
        }
        catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Add a collection that have been modified in the transaction.
     *
     * @param collection the collection
     *
     public void addCollection(final Object collection) {
     System.out.println("Adding collection");
     byte[] backup;
     try {
     ByteArrayOutputStream ostream = new ByteArrayOutputStream();
     ObjectOutputStream stream = new ObjectOutputStream(ostream);

     stream.writeObject(collection);
     stream.flush();

     backup = new byte[ostream.size()];
     backup = ostream.toByteArray();

     ostream.close();

     } catch (IOException e) {
     throw new RuntimeException("object is not serializable, i.e. not a collection");
     }
     System.out.println("Backup up the collection");

     m_collections.add(new CollectionStorage(collection, backup));
     }
     */
    /**
     * Gets the backed up collections.
     *
     * @return the collections
     *
     public Set getCollections() {
     return m_collections;
     }
     */

    /**
     * Stores a reference:backup tuple for the collections.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
     */
    final static class CollectionStorage {

        private final Object m_reference;
        private final byte[] m_backup;

        /**
         * Creates a new CollectionStorage instance.
         *
         * @param reference the object reference
         * @param backup the backup as a byte array
         */
        public CollectionStorage(final Object reference, final byte[] backup) {
            m_reference = reference;
            m_backup = backup;
        }

        /**
         * Gets the reference.
         *
         * @return the reference
         */
        public Object getReference() {
            return m_reference;
        }

        /**
         * Gets the backup as a byte array.
         *
         * @return the backup
         */
        public byte[] getBackup() {
            return m_backup;
        }

        /**
         * Gets the backup marshalled into an object.
         *
         * @return the backup
         */
        public Object getBackupAsObject() {
            Object obj;
            try {
                ByteArrayInputStream istream = new ByteArrayInputStream(m_backup);
                ObjectInputStream s = new ObjectInputStream(istream);
                obj = s.readObject();
                istream.close();
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
            return obj;
        }
    }
}

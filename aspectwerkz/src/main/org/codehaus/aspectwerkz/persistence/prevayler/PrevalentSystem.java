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
package org.codehaus.aspectwerkz.persistence.prevayler;

import java.util.Map;
import java.util.Iterator;
import java.lang.reflect.Field;
import java.io.ObjectInputStream;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;

import gnu.trove.THashMap;

import org.prevayler.util.clock.ClockedSystem;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.persistence.ModifiedField;
import org.codehaus.aspectwerkz.MetaDataEnhanceable;
import org.codehaus.aspectwerkz.MetaDataKeys;

/**
 * Implements a prevaylent system for a specific persistent aspect component.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: PrevalentSystem.java,v 1.1.1.1 2003-05-11 15:14:40 jboner Exp $
 */
public class PrevalentSystem extends ClockedSystem {

    /**
     * The serial version uid for the class.
     */
    private static final long serialVersionUID = 4225771898959304651L;

    /**
     * Read-Write lock for the per JVM storage.
     */
    protected transient ReadWriteLock m_perJvmReadWriteLock =
            new WriterPreferenceReadWriteLock();

    /**
     * Read-Write lock for the per class storage.
     */
    protected transient ReadWriteLock m_perClassReadWriteLock =
            new WriterPreferenceReadWriteLock();

    /**
     * Read-Write lock for the per instance storage.
     */
    protected transient ReadWriteLock m_perInstanceReadWriteLock =
            new WriterPreferenceReadWriteLock();

    /**
     * Stores the per JVM instance.
     */
    protected Object m_perJvm;

    /**
     * Stores all the per class instances.
     */
    protected Map m_perClass = new THashMap();

    /**
     * Stores all the per instance instances.
     */
    protected Map m_perInstance = new THashMap();

    /**
     * Instantiates the prevalent system.
     */
    public PrevalentSystem() {
    }

    /**
     * Creates an object in the database.
     *
     * @param obj the object to create
     */
    public void createPerJvm(final Object obj) {
        if (obj == null) throw new IllegalArgumentException("object to create can not be null");
        try {
            m_perJvmReadWriteLock.writeLock().acquire();
            try {
                m_perJvm = obj;
            }
            finally {
                m_perJvmReadWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not create object in database: " + e.getMessage());
        }
    }

    /**
     * Creates an object in the database.
     *
     * @param obj the object to create
     * @param index the index for the object
     */
    public void createPerClass(final Object obj, final Object index) {
        if (obj == null) throw new IllegalArgumentException("object to create can not be null");
        if (index == null) throw new IllegalArgumentException("index can not be null");
        try {
            m_perClassReadWriteLock.writeLock().acquire();
            try {
                m_perClass.put(index, obj);
            }
            finally {
                m_perClassReadWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not create object in database: " + e.getMessage());
        }
    }

    /**
     * Creates a new object in the database.
     *
     * @param obj the object to create
     * @param index the index for the object
     */
    public void createPerInstance(final Object obj, final Object index) {
        if (obj == null) throw new IllegalArgumentException("object to create can not be null");
        if (index == null) throw new IllegalArgumentException("index can not be null");
        try {
            m_perInstanceReadWriteLock.writeLock().acquire();
            try {
                m_perInstance.put(index, obj);
            }
            finally {
                m_perInstanceReadWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not create object in database: " + e.getMessage());
        }
    }

    /**
     * Updates an object in the database.
     *
     * @param modifiedField the modified field
     */
    public void updatePerJvm(final ModifiedField modifiedField) {
        if (modifiedField == null) throw new IllegalArgumentException("modified field can not be null");
        try {
            m_perJvmReadWriteLock.writeLock().acquire();
            try {
                Object obj = m_perJvm;
                Field field = obj.getClass().
                        getDeclaredField(modifiedField.getName());
                field.setAccessible(true);
                field.set(obj, modifiedField.getValue());
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
            finally {
                m_perJvmReadWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not create object in database: " + e.getMessage());
        }
    }

    /**
     * Updates an object in the database.
     *
     * @param modifiedField the modified field
     * @param index the index for the object
     */
    public void updatePerClass(final ModifiedField modifiedField,
                               final Object index) {
        if (modifiedField == null) throw new IllegalArgumentException("modified field can not be null");
        if (index == null) throw new IllegalArgumentException("index can not be null");
        try {
            m_perClassReadWriteLock.writeLock().acquire();
            try {
                Object obj = m_perClass.get(index);

                Field field = obj.getClass().
                        getDeclaredField(modifiedField.getName());
                field.setAccessible(true);
                field.set(obj, modifiedField.getValue());
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
            finally {
                m_perClassReadWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not create object in database: " + e.getMessage());
        }
    }

    /**
     * Updates an object in the database.
     *
     * @param modifiedField the modified field
     * @param index the index for the object
     */
    public void updatePerInstance(final ModifiedField modifiedField,
                                  final Object index) {
        if (modifiedField == null) throw new IllegalArgumentException("modified field can not be null");
        if (index == null) throw new IllegalArgumentException("index can not be null");
        try {
            m_perInstanceReadWriteLock.writeLock().acquire();
            try {
                Object obj = m_perInstance.get(index);
                try {
                    Field field = obj.getClass().
                            getDeclaredField(modifiedField.getName());
                    field.setAccessible(true);
                    field.set(obj, modifiedField.getValue());
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
            finally {
                m_perInstanceReadWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not create object in database: " + e.getMessage());
        }
    }

    /**
     * Retrieve an object from the database by its index.
     *
     * @return the object
     */
    public Object retrievePerJvm() {
        Object obj;
        try {
            m_perJvmReadWriteLock.readLock().acquire();
            try {
                obj = m_perJvm;
            }
            finally {
                m_perJvmReadWriteLock.readLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not retrieve object from database: " + e.getMessage());
        }
        return obj;
    }

    /**
     * Retrieve an object from the database by its index.
     *
     * @param index the index for the object to retrieve
     * @return the object
     */
    public Object retrievePerClass(final Object index) {
        if (index == null) throw new IllegalArgumentException("index can not be null");
        Object obj;
        try {
            m_perClassReadWriteLock.readLock().acquire();
            try {
                obj = m_perClass.get(index);
            }
            finally {
                m_perClassReadWriteLock.readLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not retrieve object from database: " + e.getMessage());
        }
        return obj;
    }

    /**
     * Retrieve an object from the database by its index.
     *
     * @param index the index for the object to retrieve
     * @return the object
     */
    public Object retrievePerInstance(final Object index) {
        if (index == null) throw new IllegalArgumentException("index can not be null");
        Object obj;
        try {
            m_perInstanceReadWriteLock.readLock().acquire();
            try {
                obj = m_perInstance.get(index);
            }
            finally {
                m_perInstanceReadWriteLock.readLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not retrieve object from database: " + e.getMessage());
        }
        return obj;
    }

    /**
     * Removes all object housed by a specific aspect component.
     *
     * @param aspectComponentUuid the UUID for the aspect component
     */
    public void removeAllPerJvm(final Object aspectComponentUuid) {
        if (aspectComponentUuid == null) throw new IllegalArgumentException("aspect component UUID can not be null");
        try {
            m_perJvmReadWriteLock.writeLock().acquire();
            try {
                m_perJvm = null;
            }
            finally {
                m_perJvmReadWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not remove object from database: " + e.getMessage());
        }
    }

    /**
     * Removes all object housed by a specific aspect component.
     *
     * @param aspectComponentUuid the UUID for the aspect component
     */
    public void removeAllPerClass(final Object aspectComponentUuid) {
        if (aspectComponentUuid == null) throw new IllegalArgumentException("aspect component UUID can not be null");
        try {
            m_perClassReadWriteLock.writeLock().acquire();
            try {
                for (Iterator it = m_perClass.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry)it.next();
                    MetaDataEnhanceable obj = (MetaDataEnhanceable)entry.getValue();
                    if (obj.___hidden$getMetaData(MetaDataKeys.ASPECT_COMPONENT_UUID).
                            equals(aspectComponentUuid)) {
                        m_perClass.remove(entry.getKey());
                    }
                }
            }
            finally {
                m_perClassReadWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not remove object from database: " + e.getMessage());
        }
    }

    /**
     * Removes all object housed by a specific aspect component.
     *
     * @param aspectComponentUuid the UUID for the aspect component
     */
    public void removeAllPerInstance(final Object aspectComponentUuid) {
        if (aspectComponentUuid == null) throw new IllegalArgumentException("aspect component UUID can not be null");
        try {
            m_perInstanceReadWriteLock.writeLock().acquire();
            try {
                for (Iterator it = m_perInstance.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry)it.next();
                    MetaDataEnhanceable obj = (MetaDataEnhanceable)entry.getValue();
                    if (obj.___hidden$getMetaData(MetaDataKeys.ASPECT_COMPONENT_UUID).
                            equals(aspectComponentUuid)) {
                        m_perInstance.remove(entry.getKey());
                    }
                }
            }
            finally {
                m_perInstanceReadWriteLock.writeLock().release();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().notifyAll();
            Thread.currentThread().interrupt();
            throw new RuntimeException("could not remove object from database: " + e.getMessage());
        }
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();

        m_perJvm = fields.get("m_perJvm", null);
        m_perClass = (Map)fields.get("m_perClass", null);
        m_perInstance = (Map)fields.get("m_perInstance", null);

        m_perJvmReadWriteLock = new WriterPreferenceReadWriteLock();
        m_perClassReadWriteLock = new WriterPreferenceReadWriteLock();
        m_perInstanceReadWriteLock = new WriterPreferenceReadWriteLock();
    }
}


/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * This is a default <tt>UnitOfWork</tt> implementation that can work as a basis for customized implementations.
 * It is functional by itself but does only perform transaction handling in RAM.
 * <p/>
 * Can be extended to hook in for example JTA or JDBC transactions like in {@link aspectwerkz.aosd.unitofwork.jta.JtaAwareUnitOfWork}
 * (which can also be extended if JTA transaction awareness is wanted) and transparent persistence upon commit.
 * <p/>
 * Provides a set of callback methods that the subclass can choose to override to add additional behaviour at certain
 * points. The callback methods for transaction handling are <tt>commit</tt>, <tt>preCommit</tt>, <tt>postCommit</tt>,
 * <tt>rollback</tt> and <tt>restoreObject</tt>. Here can for example the <tt>commit</tt> method be used to store the
 * objects in the <tt>UnitOfWork</tt> in the persistence storage.
 * <p/>
 * For example:
 * <pre>
 * public void commit() {
 *     insertNew();
 *     updateDirty();
 *     deleteRemoved();
 * }
 * </pre>
 * These method should the loop over the <tt>m_newObjects</tt>, <tt>m_dirtyObjects</tt> and <tt>m_removedObjects</tt>
 * and make modifications in the persistent storage accordingly.
 * <p/>
 * Two other important callback methods are <tt>addToIdMap</tt> and <tt>removeFromIdMap</tt> these should be used
  * to handle addition and removal of objects from an <i>ID Map</i>. These methods are very useful to override since
 * they are the only way to garuantee uniquness in the system. This requires of course that the <i>ID Maps</i> are
 * updated when object are <b>read</b> in into the system as well.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class UnitOfWork {

    /**
     * The current thread-local UnitOfWork.
     */
    protected static final ThreadLocal s_unitOfWork = new ThreadLocal();

    /**
     * Defines the type of UnitOfWork.
     */
    protected static UnitOfWorkType m_unitOfWorkType = UnitOfWorkType.DEFAULT; // TODO make configurable

    /**
     * Backup of all the instances that have participated in the UnitOfWork that has fields that are dirty.
     */
    protected final Map m_dirtyObjects = new HashMap();

    /**
     * List with all the new objects to be stored in persistent storage.
     */
    protected final List m_newObjects = new ArrayList();

    /**
     * List with all the removed objects to be removed from persistent storage.
     */
    protected final List m_removedObjects = new ArrayList();

    /**
     * A flag marking the UnitOfWork as doomed.
     */
    protected boolean m_isRollbackOnly = false;

    /**
     * The parent to this UnitOfWork.
     * <p/>
     * Used to implement nested UnitOfWork transactions, e.g. REQUIRES_NEW transactions.
     */
    protected UnitOfWork m_parent = null;

    /**
     * Marks the manager as initialized.
     */
    protected boolean m_initialized = false;

    /**
     * Returns the current UnitOfWork, if non-existing; null
     *
     * @return the current UnitOfWork
     */
    public static UnitOfWork getCurrent() {
        return (UnitOfWork)s_unitOfWork.get();
    }

    /**
     * Creates a new nested UnitOfWork.
     *
     * @TODO to be implemented
     *
     * @return the new nested UnitOfWork
     */
//    public static UnitOfWork getNestedUnitOfWork() {
//        final UnitOfWork oldUnitOfWork = (UnitOfWork)s_unitOfWork.get();
//        final UnitOfWork newUnitOfWork = UnitOfWorkFactory.newInstance(m_unitOfWorkType);
//        if (oldUnitOfWork != null) {
//            newUnitOfWork.setParent(oldUnitOfWork);
//        }
//        newUnitOfWork.doBegin();
//        s_unitOfWork.set(newUnitOfWork);
//        return newUnitOfWork;
//    }

    /**
     * Checks if the control flow is in a UnitOfWork.
     *
     * @return boolean
     */
    public static boolean isInUnitOfWork() {
        return s_unitOfWork.get() != null;
    }

    /**
     * Sets the UnitOfWork to rollback only only, e.g. marks it as doomed.
     */
    public static void setRollbackOnly() {
        if (isInUnitOfWork()) {
            getCurrent().m_isRollbackOnly = true;
        }
    }

    /**
     * Sets the threads current UnitOfWork.
     *
     * @param unitOfWork the UnitOfWork
     */
    static void setUnitOfWork(final UnitOfWork unitOfWork) {
        s_unitOfWork.set(unitOfWork);
    }

    /**
     * Disposes the UnitOfWork specified. Sets the parent UnitOfWork to the current UnitOfWork if exists.
     */
    public static void dispose() {
        if (!isInUnitOfWork()) return;
        UnitOfWork unitOfWork = getCurrent();

        unitOfWork.m_dirtyObjects.clear();
        unitOfWork.m_newObjects.clear();
        unitOfWork.m_removedObjects.clear();
        unitOfWork.doDispose();

        // replace the current TX with its parent TX
        UnitOfWork parent = unitOfWork.getParent();
        if (parent != null) {
            setUnitOfWork(parent);
        }

        unitOfWork = null;
        s_unitOfWork.set(null);
    }

    /**
     * Checks if the UnitOfWork is set to rollback only
     *
     * @return boolean
     */
    public boolean isRollbackOnly() {
        return m_isRollbackOnly;
    }

    /**
     * Template method. To be overridden by subclass.
     * <p/>
     * Adds an object to the ID map.
     *
     * @param obj the object to add to the ID map
     */
    public void addToIdMap(final Object obj) {
    }

    /**
     * Template method. To be overridden by subclass.
     * <p/>
     * Removes an object from the ID map.
     *
     * @param obj the object to remove from the ID map
     */
    public void removeFromIdMap(final Object obj) {
    }

    /**
     * Rolls back the current UnitOfWork.
     */
    public void rollback() {
        restoreModifiedObjects();
        doRollback();
    }

    /**
     * Template method. To be overridden by subclass.
     * <p/>
     * Is invoked when the UnitOfWork is started.
     */
    public void doBegin() {
    }

    /**
     * Template method. To be overridden by subclass.
     * <p/>
     * Is invoked when the UnitOfWork is committed.
     */
    public void doCommit() {
    }

    /**
     * Template method. To be overridden by subclass.
     * <p/>
     * Is invoked right before the UnitOfWork is committed.
     */
    public void doPreCommit() {
    }

    /**
     * Template method. To be overridden by subclass.
     * <p/>
     * Is invoked right after the UnitOfWork is committed.
     */
    public void doPostCommit() {
    }

    /**
     * Template method. To be overridden by subclass.
     * <p/>
     * Is invoked when the UnitOfWork is aborted.
     */
    public void doRollback() {
    }

    /**
     * Template method. To be overridden by subclass.
     * <p/>
     * Is invoked when the UnitOfWork is aborted.
     */
    public void doDispose() {
    }

    /**
     * Registers the object as newly created, to be created in persistent storage.
     *
     * @param obj the object to add as new
     */
    static void registerNew(final Object obj) {
        if (obj == null) throw new UnitOfWorkException("can not register a null object as new");
        if (isInUnitOfWork()) {
            UnitOfWork unitOfWork = getCurrent();
            if (unitOfWork.m_dirtyObjects.containsKey(obj)) throw new UnitOfWorkException("can not register object as new: object already registered as dirty");
            if (unitOfWork.m_removedObjects.contains(obj)) throw new UnitOfWorkException("can not register object as new: object already registered as removed");
            if (unitOfWork.m_newObjects.contains(obj)) throw new UnitOfWorkException("can not register object as new: object already registered as new");
            unitOfWork.m_newObjects.add(obj);
            unitOfWork.addToIdMap(obj);
        }
    }

    /**
     * Registers the object as removed , to be removed from persistent storage.
     *
     * @param obj the object to add as removed
     */
    static void registerRemoved(final Object obj) {
        if (obj == null) throw new UnitOfWorkException("can not register a null object as removed");
        if (isInUnitOfWork()) {
            UnitOfWork unitOfWork = getCurrent();
            if (unitOfWork.m_newObjects.remove(obj)) return;
            unitOfWork.m_dirtyObjects.remove(obj);
            if (!unitOfWork.m_removedObjects.contains(obj)) {
                unitOfWork.m_removedObjects.add(obj);
                unitOfWork.removeFromIdMap(obj);
            }
        }
    }

    /**
     * Registers the object as dirty, to be updated in persistent storage.
     *
     * @param obj the object to register as dirty
     * @param fieldName the name of the dirty field
     */
    static void registerDirty(final Object obj, final String fieldName) {
        if (isInUnitOfWork()) {
            UnitOfWork unitOfWork = getCurrent();
            if (unitOfWork.m_removedObjects.contains(obj)) throw new UnitOfWorkException("can not makr a removed object as dirty");
            unitOfWork.storeBackup(obj, fieldName);
        }
    }

    /**
     * Registers the object as dirty, to be updated in persistent storage.
     *
     * @param obj the object to register as dirty
     */
    static void registerDirty(final Object obj) {
        if (isInUnitOfWork()) {
            UnitOfWork unitOfWork = getCurrent();
            if (unitOfWork.m_removedObjects.contains(obj)) throw new UnitOfWorkException("can not makr a removed object as dirty");
            if (!unitOfWork.m_dirtyObjects.containsKey(obj)
                    && !unitOfWork.m_newObjects.contains(obj)) {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    unitOfWork.storeBackup(obj, fields[i].getName());
                }
            }
        }
    }

    /**
     * Registers the object as clean.
     *
     * @todo write tests
     *
     * @param obj the object to register as clean
     */
    static void registerClean(final Object obj) {
        if (isInUnitOfWork()) {
            UnitOfWork unitOfWork = getCurrent();
            unitOfWork.addToIdMap(obj);
        }
    }

    /**
     * Checks if the object exists in persistent storage.
     *
     * @todo write tests
     *
     * @param obj the object to check for existance
     */
    static boolean exists(final Object obj) {
        return true;
    }

    /**
     * Creates and starts a new UnitOfWork, if a UnitOfWork exists
     * then it returns the current one.
     *
     * @return the new UnitOfWork
     */
    static UnitOfWork begin() {
        UnitOfWork unitOfWork = (UnitOfWork)s_unitOfWork.get();
        if (unitOfWork != null) {
            return unitOfWork;
        }
        unitOfWork = UnitOfWorkFactory.newInstance(m_unitOfWorkType);
        unitOfWork.doBegin();
        s_unitOfWork.set(unitOfWork);
        return unitOfWork;
    }

    /**
     * Commits the UnitOfWork.
     */
    void commit() {
        doPreCommit();
        doCommit();
        doPostCommit();
    }

    /**
     * Returns the backup of a particular object.
     *
     * @param obj the object of interest
     * @return the modified Object
     */
    ObjectBackup getBackup(final Object obj) {
        if (obj == null) throw new IllegalArgumentException("object can not be null");
        ObjectBackup backup = (ObjectBackup)m_dirtyObjects.get(obj);
        return backup;
    }

    /**
     * Returns the parent of the transaction context.
     *
     * @return the parent transaction context
     */
    UnitOfWork getParent() {
        return m_parent;
    }

    /**
     * Sets the parent of the UnitOfWork.
     *
     * @param parentUnitOfWork the parent of the current UnitOfWork
     */
    void setParent(final UnitOfWork parentUnitOfWork) {
        if (parentUnitOfWork == null) throw new IllegalArgumentException("parent UnitOfWork can not be null");
        m_parent = parentUnitOfWork;
    }

    /**
     * Checks if the UnitOfWork passed to the method is an ancestor to the current UnitOfWork.
     *
     * @param unitOfWork the possible ancestor
     * @return boolean
     */
    boolean isAncestor(final UnitOfWork unitOfWork) {
        UnitOfWork parentUnitOfWork = getParent();
        while (parentUnitOfWork != null) {
            if (unitOfWork.equals(parentUnitOfWork)) {
                return true;
            }
            parentUnitOfWork = parentUnitOfWork.getParent();
        }
        return false;
    }

    /**
     * Makes a backup of the object about to be modified.
     * Makes the backup of a specific instance only once.
     *
     * @param targetInstance the Object about to be modified
     * @param fieldName the name of the dirty field
     */
    protected void storeBackup(final Object targetInstance, final String fieldName) {
        if (targetInstance == null) throw new IllegalArgumentException("object can not be null");
        if (fieldName == null) throw new IllegalArgumentException("field name can not be null");
        ObjectBackup objBackup;
        if (m_dirtyObjects.containsKey(targetInstance)) {
            objBackup = (ObjectBackup)m_dirtyObjects.get(targetInstance);
        }
        else {
            objBackup = ObjectBackup.newInstance(targetInstance);
            m_dirtyObjects.put(targetInstance, objBackup);
        }
        objBackup.registerDirtyField(fieldName);
    }

    /**
     * Restores all the objects modified in the current UnitOfWork
     * by calling restoreObject on every object that has participated
     * in the UnitOfWork, it then clears the backup storage.
     */
    protected void restoreModifiedObjects() {
        for (Iterator it = m_dirtyObjects.values().iterator(); it.hasNext();) {
            restoreObject((ObjectBackup)it.next());
        }
        m_dirtyObjects.clear();
    }

    /**
     * Returns to the object to its old state.
     *
     * @param backup the backed up object
     */
    protected void restoreObject(final ObjectBackup backup) {
        if (backup == null) return;
        try {
            Object obj = backup.getObject();
            Object targetInstance = backup.getReference();

            Field[] fields = targetInstance.getClass().getDeclaredFields();
            for (int i = 0, j = fields.length; i < j; i++) {

                Field field = fields[i];
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(obj);
                field.set(targetInstance, value);
            }
        }
        catch (Exception e) {
            throw new UnitOfWorkException("could not roll back UnitOfWork due to: " + e);
        }
    }

    /**
     * Private constructor.
     */
    protected UnitOfWork() {
    }
}

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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.io.File;

import gnu.trove.THashMap;

import org.prevayler.Prevayler;
import org.prevayler.util.clock.ClockActor;
import org.prevayler.util.clock.ClockedSystem;
import org.prevayler.implementation.SnapshotPrevayler;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.MetaDataKeys;
import org.codehaus.aspectwerkz.MetaDataEnhanceable;
import org.codehaus.aspectwerkz.persistence.ModifiedField;
import org.codehaus.aspectwerkz.persistence.PersistenceManager;
import org.codehaus.aspectwerkz.persistence.prevayler.CreateTransaction;
import org.codehaus.aspectwerkz.persistence.prevayler.PrevalentSystem;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Implements the <code>PersistenceManager</code> interface using
 * <code>Prevayler</code> as backend.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: PrevaylerPersistenceManager.java,v 1.1 2003-06-17 16:23:24 jboner Exp $
 */
public class PrevaylerPersistenceManager implements PersistenceManager {

    /**
     * Holds the path to the transactions log directory.
     */
    public static final String TX_LOG_DIR = System.getProperty(
            "aspectwerkz.persistence.txLogDir", "_txLogs") + File.separator;

    /**
     * A flag deciding to run the snapshot scheduler thread or not.
     */
    public static final String RUN_SNAPSHOT_THREAD = System.getProperty(
            "aspectwerkz.persistence.snapshot.thread", "true");

    /**
     * A flag deciding to run the clock actor thread or not.
     */
    public static final String RUN_CLOCKACTOR_THREAD = System.getProperty(
            "aspectwerkz.persistence.clockactor.thread", "true");

    /**
     * Stores all the prevayler instances.
     */
    protected final Map m_prevaylers = new THashMap();

    /**
     * Marks the persistence managers as initalized or not.
     */
    protected boolean m_initialized = false;

    /**
     * Handles the snapshots.
     */
    protected SnapshotScheduler m_snapshotScheduler;

    /**
     * Initializes the persistence manager.
     */
    public synchronized void initialize() {
        if (m_initialized) return;
        m_initialized = true;
    }

    /**
     * Registers a new aspect component as persistent.
     *
     * @param aspectComponentUuid the uuid of the aspect component to register
     */
    public void register(final String aspectComponentUuid) {
        try {
            synchronized (m_prevaylers) {

                final Prevayler prevayler = new SnapshotPrevayler(
                        new PrevalentSystem(),
                        TX_LOG_DIR + aspectComponentUuid);

                m_prevaylers.put(aspectComponentUuid, prevayler);

                startClockActorDaemon(prevayler);

                startSnapshotMakerDaemon(aspectComponentUuid);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Creates an new object in the database.
     *
     * @param obj the object to create
     * @param index the index for the object
     * @param deploymentModel the deployment model for the object
     */
    public void create(final Object obj,
                       final Object index,
                       final int deploymentModel) {
        if (notInitialized()) throw new IllegalStateException("persistence manager is not initialized");
        if (obj == null) throw new IllegalArgumentException("object to create can not be null");

        try {
            String aspectComponentUuid = (String)((MetaDataEnhanceable)obj).
                    ___hidden$getMetaData(MetaDataKeys.ASPECT_COMPONENT_UUID);

            ((Prevayler)m_prevaylers.get(aspectComponentUuid)).execute(
                    new CreateTransaction(obj, index, deploymentModel));
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }

    }

    /**
     * Updates an object in the database.
     *
     * @param modifiedField the modified field to update
     * @param index the index for the object
     * @param aspectComponentUuid the uuid of the aspect component holding the introduction
     * @param deploymentModel the deployment model for the object
     */
    public void update(final ModifiedField modifiedField,
                       final Object index,
                       final Object aspectComponentUuid,
                       final int deploymentModel) {
        if (notInitialized()) throw new IllegalStateException("persistence manager is not initialized");
        if (modifiedField == null) throw new IllegalArgumentException("modified field can not be null");
        if (aspectComponentUuid == null) throw new IllegalArgumentException("aspect component uuid can not be null");

        try {
            ((Prevayler)m_prevaylers.get(aspectComponentUuid)).execute(
                    new UpdateTransaction(modifiedField, index, deploymentModel));
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Finds an object from the database by its index.
     *
     * @param aspectComponentUuid the uuid of the aspect component
     * @param index the index (can be null for the perJVM deployment model)
     * @param deploymentModel the deployment model for the object
     * @return the object
     */
    public Object retrieve(final Object aspectComponentUuid,
                           final Object index,
                           final int deploymentModel) {
        if (notInitialized()) throw new IllegalStateException("persistence manager is not initialized");
        if (aspectComponentUuid == null) throw new IllegalArgumentException("container uuid can not be null");

        Object obj = null;
        Prevayler prevayler;
        try {
            switch (deploymentModel) {

                case DeploymentModel.PER_JVM:
                    prevayler = (Prevayler)m_prevaylers.get(aspectComponentUuid);
                    obj = ((PrevalentSystem)prevayler.prevalentSystem()).
                            retrievePerJvm();
                    break;

                case DeploymentModel.PER_CLASS:
                    prevayler = (Prevayler)m_prevaylers.get(aspectComponentUuid);
                    obj = ((PrevalentSystem)prevayler.prevalentSystem()).
                            retrievePerClass(index);
                    break;

                case DeploymentModel.PER_INSTANCE:
                    prevayler = (Prevayler)m_prevaylers.get(aspectComponentUuid);

                    obj = ((PrevalentSystem)prevayler.prevalentSystem()).
                            retrievePerInstance(index);
                    break;

                default:
                    throw new IllegalArgumentException("invalid deployment model type");
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return obj;
    }

    /**
     * Removes all instances of an object for a spefific aspect component.
     *
     * @param aspectComponentUuid the aspect component UUID
     * @param deploymentModel the deployement model of the object to remove
     */
    public void removeAll(final Object aspectComponentUuid,
                          final int deploymentModel) {
        if (notInitialized()) throw new IllegalStateException("persistence manager is not initialized");
        if (aspectComponentUuid == null) throw new IllegalArgumentException("container uuid can not be null");

        try {
            ((Prevayler)m_prevaylers.get(aspectComponentUuid)).execute(
                    new RemoveAllTransaction(aspectComponentUuid, deploymentModel));
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Shuts down the prevayler persistence manager system.
     */
    public void shutdown() {
        if (notInitialized()) throw new IllegalStateException("persistence manager is not initialized");
        if (RUN_SNAPSHOT_THREAD.equals("true")) {
            m_snapshotScheduler.cancel();
        }
    }

    /**
     * Returns the current time in the system.
     * All time related tasks MUST use this method to get the time.
     * Otherwise the tasks are NOT deterministic and the system
     * might end up in unconsistent state.
     *
     * @param aspectComponentUuid the uuid of the aspect component
     * @return the current time in the system
     */
    public Date getCurrentTime(final String aspectComponentUuid) {
        if (notInitialized()) throw new IllegalStateException("prevayler persistence manager is not initialized");
        return ((ClockedSystem)((Prevayler)m_prevaylers.get(aspectComponentUuid)).
                prevalentSystem()).clock().time();
    }

    /**
     * Checks if the persistence manager has been initialized.
     *
     * @return true if the persistence manager is not initialized
     */
    public boolean notInitialized() {
        return !m_initialized;
    }

    /**
     * Starts up the clock actor daemon thread. Which keeps track of the time
     * in the prevalent system.
     *
     * @param prevayler the prevayler instance
     */
    protected void startClockActorDaemon(final Prevayler prevayler) {
        if (RUN_CLOCKACTOR_THREAD.equals("false")) return;
        new ClockActor(prevayler);
    }

    /**
     * Starts the snapshot maker daemon.
     *
     * @todo start time and interval should be possible to configure
     *
     * @param aspectComponentUuid the uuid of the aspect component
     */
    protected void startSnapshotMakerDaemon(final String aspectComponentUuid) {
        if (RUN_SNAPSHOT_THREAD.equals("false")) return;

        // make snapshots each night, starting next night at 03:00.00.000.
        GregorianCalendar firstSnapshot = new GregorianCalendar();
        firstSnapshot.add(Calendar.DATE, 1);
        firstSnapshot.set(Calendar.HOUR_OF_DAY, 3);
        firstSnapshot.set(Calendar.MINUTE, 0);
        firstSnapshot.set(Calendar.SECOND, 0);
        firstSnapshot.set(Calendar.MILLISECOND, 0);

        m_snapshotScheduler = new SnapshotScheduler(
                (Prevayler)m_prevaylers.get(aspectComponentUuid),
                firstSnapshot.getTime(),
                1000 * 60 * 60 * 24,
                new SnapshotListenerImpl());
    }

    /**
     * Implements the snapshot scheduler listener interface.
     * Provides callback methods for specific events during
     * snapshots.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
     * @version $Id: PrevaylerPersistenceManager.java,v 1.1 2003-06-17 16:23:24 jboner Exp $
     */
    private static class SnapshotListenerImpl implements SnapshotScheduler.Listener {

        public void snapshotStarted(final Prevayler prevayler,
                                    final long prevaylerDate,
                                    final long systemDate) {
        }

        public void snapshotTaken(final Prevayler prevayler,
                                  final long prevaylerDate,
                                  final long systemDate) {
        }

        public void snapshotException(final Prevayler prevayler,
                                      final Exception exception,
                                      final long prevaylerDate,
                                      final long systemDate) {
        }

        public void snapshotError(final Prevayler prevayler,
                                  final Error error,
                                  final long prevaylerDate,
                                  final long systemDate) {
        }

        public void snapshotShutdown(final Prevayler prevayler,
                                     final long prevaylerDate,
                                     final long systemDate) {
        }
    }
}

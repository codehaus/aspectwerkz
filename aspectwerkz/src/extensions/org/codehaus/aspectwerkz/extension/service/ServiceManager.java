/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.service;

import java.lang.reflect.Method;

import java.io.File;
import java.util.List;
import java.util.Iterator;

import org.codehaus.aspectwerkz.extension.definition.Definition;
import org.codehaus.aspectwerkz.extension.persistence.definition.PersistenceDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.PersistenceManagerDefinition;
import org.codehaus.aspectwerkz.extension.persistence.PersistenceDefinitionParser;
import org.codehaus.aspectwerkz.extension.persistence.PersistenceManager;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Manages all the services in the system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ServiceManager {

    /**
     * The persistence definition file.
     */
    public static final String PERSISTENCE_DEFINITION =
            System.getProperty("aspectwerkz.extension.persistence.definition.file", null);

    /**
     * The sole instance of the service manager.
     */
    protected static final ServiceManager s_soleInstance = new ServiceManager();

    /**
     * The class loader to use.
     */
    protected ClassLoader m_loader = null;

    /**
     * Marks the manager as running.
     */
    protected static boolean m_isRunning = false;

    /**
     * The current persistence manager.
     */
    protected PersistenceManager m_persistenceManager;

    /**
     * Starts up all the services.
     */
    public static void start() {
        synchronized (getInstance()) {
            if (getInstance().isRunning()) return;
            getInstance().startPersistenceManager();
        }
    }

    /**
     * Stops all the services.
     */
    public static void stop() {
        synchronized (getInstance()) {
            if (getInstance().isRunning()) {
                // stop the services
            }
        }
    }

    /**
     * Returns the service specified.
     *
     * @param type the type of the service
     * @return the service
     */
    public static Service getService(final ServiceType type) {
        if (type.equals(ServiceType.PERSISTENCE_MANAGER)) {
            return getInstance().m_persistenceManager;
        }
        else {
            throw new RuntimeException("no such service: " + type.toString());
        }
    }

    /**
     * Checks if all the services has started.
     *
     * @return boolean
     */
    public static boolean isRunning() {
        return getInstance().m_isRunning;
    }

    /**
     * Starts up the persistence manager.
     */
    protected void startPersistenceManager() {
        PersistenceDefinition persistenceDefinition = PersistenceDefinitionParser.
                parse(new File(PERSISTENCE_DEFINITION));

        List persistenceManagers = persistenceDefinition.getPersistenceManagers();
        for (Iterator it = persistenceManagers.iterator(); it.hasNext();) {
            PersistenceManagerDefinition manager = (PersistenceManagerDefinition)it.next();
            if (manager.isActive()) {
                m_persistenceManager = (PersistenceManager)startService(
                        m_loader, manager.getClassName(), persistenceDefinition);
                break;
            }
        }
    }

    /**
     * Starts up a service dynamically.
     *
     * @param loader the class loader
     * @param className the class name for the service
     * @param definition the service definition
     */
    protected static Service startService(final ClassLoader loader,
                                          final String className,
                                          final Definition definition) {
        try {
            Class klass = loader.loadClass(className);
            Method getInstance = klass.getMethod("getInstance", new Class[]{});
            Object service = getInstance.invoke(null, new Object[]{});
            Method initialize = klass.getMethod(
                    "initialize", new Class[]{ClassLoader.class, Definition.class});
            initialize.invoke(service, new Object[]{loader, definition});
            return (Service)service;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the one A only instance of the service manager.
     *
     * @return the instance
     */
    protected static ServiceManager getInstance() {
        return s_soleInstance;
    }

    /**
     * Private constructor.
     */
    protected ServiceManager() {
        m_loader = Thread.currentThread().getContextClassLoader();
    }
}



/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.connectivity.Invoker;
import org.codehaus.aspectwerkz.connectivity.RemoteProxy;
import org.codehaus.aspectwerkz.connectivity.RemoteProxyServer;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.metadata.ClassNameMethodMetaDataTuple;

/**
 * Represents the aspect runtime system. Manages the different parts of the runtime system and provides and API for the
 * use to access and manage the system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public final class System {

    /**
     * The UUID of the single system if only one definition is used.
     */
    public static final String DEFAULT_SYSTEM = "default";

    /**
     * The path to the definition file.
     */
    private static final boolean START_REMOTE_PROXY_SERVER = "true".equals(java.lang.System.getProperty("aspectwerkz.remote.server.run", "false"));

    /**
     * The aspect manager.
     */
    private final AspectManager m_aspectManager;

    /**
     * Holds a list of the cflow join points passed by the control flow of the current thread.
     */
    private final ThreadLocal m_controlFlowLog = new ThreadLocal();

    /**
     * The remote proxy server instance.
     */
    private RemoteProxyServer m_remoteProxyServer = null;

    /**
     * Should NEVER be invoked by the user. Use <code>SystemLoader.getSystem(uuid)</code> to retrieve the system.
     * <p/>
     * Creates a new AspectWerkz system instance.
     * <p/>
     * Sets the UUID for the system.
     *
     * @param uuid       the UUID for the system
     * @param definition the definition for the system
     */
    System(final String uuid, final SystemDefinition definition) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (definition == null) throw new IllegalArgumentException("definition can not be null");

        m_aspectManager = new AspectManager(uuid, definition);

        if (START_REMOTE_PROXY_SERVER) {
            startRemoteProxyServer();
        }
    }

    /**
     * Initializes the system. The initialization needs to be separated fromt he construction of the manager, and is
     * triggered by the runtime system
     */
    public void initialize() {
        m_aspectManager.initialize();
    }

    /**
     * Returns the aspect manager for this system.
     *
     * @return the aspect manager
     */
    public AspectManager getAspectManager() {
        return m_aspectManager;
    }

    /**
     * Registers entering of a control flow join point.
     *
     * @param metaData the classname:methodMetaData metaData
     */
    public void enteringControlFlow(final ClassNameMethodMetaDataTuple metaData) {
        if (metaData == null) throw new IllegalArgumentException("classname:methodMetaData tuple can not be null");

        Set cflowSet = (Set)m_controlFlowLog.get();
        if (cflowSet == null) {
            cflowSet = new HashSet();
        }
        cflowSet.add(metaData);
        m_controlFlowLog.set(cflowSet);
    }

    /**
     * Registers exiting from a control flow join point.
     *
     * @param metaData the classname:methodMetaData metaData
     */
    public void exitingControlFlow(final ClassNameMethodMetaDataTuple metaData) {
        if (metaData == null) throw new IllegalArgumentException("classname:methodMetaData tuple can not be null");

        Set cflowSet = (Set)m_controlFlowLog.get();
        if (cflowSet == null) {
            return;
        }
        cflowSet.remove(metaData);
        m_controlFlowLog.set(cflowSet);
    }

    /**
     * Checks if we are in the control flow of a specific cflow pointcut.
     *
     * @param cflowExpression the cflow expression
     * @return boolean
     */
    public boolean isInControlFlowOf(final Expression cflowExpression) {
        if (cflowExpression == null) throw new IllegalArgumentException("cflowExpression can not be null");

        Set cflowSet = (Set)m_controlFlowLog.get();
        if (cflowSet == null || cflowSet.isEmpty()) {
            return false;
        }
        else {
            for (Iterator it = cflowSet.iterator(); it.hasNext();) {
                ClassNameMethodMetaDataTuple tuple = (ClassNameMethodMetaDataTuple)it.next();
                if (cflowExpression.match(tuple.getClassMetaData(), tuple.getMethodMetaData())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Starts up the remote proxy server.
     */
    private void startRemoteProxyServer() {
        Invoker invoker = getInvoker();
        m_remoteProxyServer = new RemoteProxyServer(ContextClassLoader.getLoader(), invoker);
        m_remoteProxyServer.start();
    }

    /**
     * Returns the Invoker instance to use.
     *
     * @return the Invoker
     */
    private Invoker getInvoker() {
        Invoker invoker = null;
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(java.lang.System.getProperty("aspectwerkz.resource.bundle")));
            String className = properties.getProperty("remote.server.invoker.classname");
            invoker = (Invoker)ContextClassLoader.getLoader().loadClass(className).newInstance();
        }
        catch (Exception e) {
            invoker = getDefaultInvoker();
        }
        return invoker;
    }

    /**
     * Returns the default Invoker.
     *
     * @return the default invoker
     */
    private Invoker getDefaultInvoker() {
        return new Invoker() {
            public Object invoke(final String handle,
                                 final String methodName,
                                 final Class[] paramTypes,
                                 final Object[] args,
                                 final Object context) {
                Object result = null;
                try {
                    final Object instance = RemoteProxy.getWrappedInstance(handle);
                    final Method method = instance.getClass().getMethod(methodName, paramTypes);
                    result = method.invoke(instance, args);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
                return result;
            }
        };
    }
}

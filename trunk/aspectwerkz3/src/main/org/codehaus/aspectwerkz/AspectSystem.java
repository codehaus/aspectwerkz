/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.connectivity.Invoker;
import org.codehaus.aspectwerkz.connectivity.RemoteProxy;
import org.codehaus.aspectwerkz.connectivity.RemoteProxyServer;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.expression.CflowExpressionVisitor;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Represents the aspect runtime system.<br/> Manages the different parts of the runtime system and provides and API to
 * access and manage the system.<br/>
 * <p/>
 * There is an AspectSystem per ClassLoader. An AspectSystem is aware of the classloader hierarchy and reflects it by
 * gathering the AspectManager, which represents a single &lt;system ..&gt; entry.
 * <p/>
 * When an instance of an AspectSystem is created (perClassLoader), it checks for existence of previous AspectManager
 * defined in parent ClassLoader. AspectManager are shared among AspectSystem as shown below:<br/> </p>
 * <pre>
 *          [0d, 1d, 2d]  (3 SystemDefs, all defined in this classloader)
 *                  /   \
 * [0r, 1r, 2r, 3d]      \  (3 reused, one more defined)
 *                     [0r, 1r, 2r, 3d]  (one more defined, not the same)
 * </pre>
 * </p> This composition strategy allow to avoid global static repository, but is tight to following ClassLoader parent
 * hierarchy. </p> If an AspectManager is added at runtime, it should be added in the whole child hierarchy. TODO </p>
 * <p/>
 * TODO: caution when addding a new SystemDefinition in between. TODO: move the remote proxy elsewhere unless defining
 * classloader is needed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public final class AspectSystem {
    /**
     * The path to the remote proxy server config file.
     */
    private static final boolean START_REMOTE_PROXY_SERVER = "true".equals(java.lang.System.getProperty("aspectwerkz.remote.server.run",
                                                                                                        "false"));

    /**
     * ClassLoader defining this AspectSystem
     */
    private final ClassLoader m_classLoader;

    /**
     * The aspect managers in the order of the hierarchy
     */
    private AspectManager[] m_aspectManagers;

    /**
     * Holds a list of the cflow join points passed by the control flow of the current thread.
     *
     * @TODO: I think we need to use a static TL - need test coverage
     */
    private final ThreadLocal m_cflowStack = new ThreadLocal();

    /**
     * The remote proxy server instance.
     */
    private RemoteProxyServer m_remoteProxyServer = null;

    /**
     * Should NEVER be invoked by the user. Use <code>SystemLoader.getSystem(uuid)</code> to retrieve the system.
     * <p/>
     * Creates a new AspectWerkz AOPC system instance.
     * <p/>
     *
     * @param loader      the classloader defining the system
     * @param definitions the ordered SystemDefinitions for the system (whole hierarchy)
     */
    AspectSystem(ClassLoader loader, final List definitions) {
        m_classLoader = loader;
        m_aspectManagers = new AspectManager[definitions.size()];

        // assert uuid are unique in the ClassLoader hierarchy
        assertUuidUniqueWithinHierarchy(definitions);

        // copy the AspectManagers from the parent ClassLoader AspectSystem
        if (loader.getParent() != null) {
            AspectManager[] parentAspectManagers = SystemLoader.getSystem(loader.getParent()).getAspectManagers();
            System.arraycopy(parentAspectManagers, 0, m_aspectManagers, 0, parentAspectManagers.length);
        }

        // note: we should be able to go directly to the correct index instead of this loop and check
        for (int i = 0; i < m_aspectManagers.length; i++) {
            SystemDefinition def = (SystemDefinition)definitions.get(i);
            String uuid = def.getUuid();

            // check if the SystemDefinition comes from a parent AspectSystem before adding it
            AspectManager aspectManager = null;
            try {
                aspectManager = getAspectManager(uuid);
            } catch (DefinitionException e) {
                ;
            }

            if (aspectManager == null) {
                // new def defined in THIS CL and not a parent one
                aspectManager = new AspectManager(this, def);
                System.out.println("Created AspectManager = " + uuid + ": " + aspectManager);
            } else {
                System.out.println("Reused AspectManager = " + uuid + ": " + aspectManager);
                continue;
            }
            m_aspectManagers[i] = aspectManager;
        }

        if (START_REMOTE_PROXY_SERVER) {
            startRemoteProxyServer();
        }
    }

    /**
     * Returns the classloader which defines this AspectSystem
     *
     * @return the classloader which defines this AspectSystem
     */
    public ClassLoader getDefiningClassLoader() {
        return m_classLoader;
    }

    /**
     * Returns an AspectManager by its index. The index are stable when the ClassLoader hierarchy is crossed from top to
     * bottom
     *
     * @param aspectManagerIndex
     * @return AspectManager, or throw an IndexOutOfBoundException
     */
    public AspectManager getAspectManager(int aspectManagerIndex) {
        return m_aspectManagers[aspectManagerIndex];
    }

    /**
     * Returns an AspectManager by its uuid
     *
     * @param uuid
     * @return AspectManager
     * @throws DefinitionException (runtime exception) if not found
     */
    public AspectManager getAspectManager(final String uuid) {
        // Note: uuid is assumed to be unique within an AspectSystem
        for (int i = 0; i < m_aspectManagers.length; i++) {
            AspectManager aspectManager = m_aspectManagers[i];

            // the null check makes sense only in the flow of <init>
            if ((aspectManager != null) && aspectManager.getUuid().equals(uuid)) {
                return aspectManager;
            }
        }
        throw new DefinitionException("No such AspectManager " + uuid + " in " + m_classLoader);
    }

    /**
     * Initializes the system. The initialization needs to be separated from the construction of the manager, and is
     * triggered by the runtime system
     */
    public void initialize() {
        for (int i = 0; i < m_aspectManagers.length; i++) {
            m_aspectManagers[i].initialize();
        }
    }

    /**
     * Returns the aspect managers for this system.
     *
     * @return the aspect managers
     */
    public AspectManager[] getAspectManagers() {
        return m_aspectManagers;
    }

    /**
     * Registers entering of a control flow join point.
     *
     * @param pointcutType the pointcut type
     * @param methodInfo   the method info
     * @param withinInfo   the within info
     */
    public void enteringControlFlow(final PointcutType pointcutType, final MethodInfo methodInfo,
                                    final ClassInfo withinInfo) {
        if (pointcutType == null) {
            throw new IllegalArgumentException("pointcut type can not be null");
        }
        if (methodInfo == null) {
            throw new IllegalArgumentException("method info can not be null");
        }
        Set cflowSet = (Set)m_cflowStack.get();
        if (cflowSet == null) {
            cflowSet = new HashSet();
        }
        cflowSet.add(new ExpressionContext(pointcutType, methodInfo, withinInfo));
        m_cflowStack.set(cflowSet);
    }

    /**
     * Registers exiting from a control flow join point.
     *
     * @param pointcutType the pointcut type
     * @param methodInfo   the method info
     * @param withinInfo   the within info
     */
    public void exitingControlFlow(final PointcutType pointcutType, final MethodInfo methodInfo,
                                   final ClassInfo withinInfo) {
        if (pointcutType == null) {
            throw new IllegalArgumentException("pointcut type can not be null");
        }
        if (methodInfo == null) {
            throw new IllegalArgumentException("method info can not be null");
        }
        Set cflowSet = (Set)m_cflowStack.get();
        if (cflowSet == null) {
            return;
        }
        cflowSet.remove(new ExpressionContext(pointcutType, methodInfo, withinInfo));
        m_cflowStack.set(cflowSet);
    }

    /**
     * Checks if we are in the control flow of a join point picked out by a specific pointcut expression.
     *
     * @param expression the cflow expression
     * @return boolean
     */
    public boolean isInControlFlowOf(final CflowExpressionVisitor expression) {
        if (expression == null) {
            throw new IllegalArgumentException("expression can not be null");
        }
        if (!expression.hasCflowPointcut()) {
            return false;
        }
        Set cflowSet = (Set)m_cflowStack.get();
        if (cflowSet == null) {
            cflowSet = new HashSet();
        }
        for (Iterator it = cflowSet.iterator(); it.hasNext();) {
            ExpressionContext ctx = (ExpressionContext)it.next();
            if (expression.match(ctx)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts up the remote proxy server.
     *
     * @TODO: option to shut down in a nice way?
     */
    private void startRemoteProxyServer() {
        m_remoteProxyServer = new RemoteProxyServer(ContextClassLoader.getLoader(), getInvoker());
        m_remoteProxyServer.start();
    }

    /**
     * Returns the Invoker instance to use.
     *
     * @return the Invoker
     */
    private Invoker getInvoker() {
        Invoker invoker;
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(java.lang.System.getProperty("aspectwerkz.resource.bundle")));
            String className = properties.getProperty("remote.server.invoker.classname");
            invoker = (Invoker)ContextClassLoader.getLoader().loadClass(className).newInstance();
        } catch (Exception e) {
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
                public Object invoke(final String handle, final String methodName, final Class[] paramTypes,
                                     final Object[] args, final Object context) {
                    Object result;
                    try {
                        final Object instance = RemoteProxy.getWrappedInstance(handle);
                        final Method method = instance.getClass().getMethod(methodName, paramTypes);
                        result = method.invoke(instance, args);
                    } catch (Exception e) {
                        throw new WrappedRuntimeException(e);
                    }
                    return result;
                }
            };
    }

    /**
     * Checks uuid unicity within the list. Throw a DefinitionException on failure.
     * @TODO AVAOPC algo is crapped, check earlier and avoid exception but do a WARN (in SysDefContainer)
     *
     * @param definitions
     */
    private static void assertUuidUniqueWithinHierarchy(final List definitions) {
        for (int i = 0; i < definitions.size(); i++) {
            SystemDefinition systemDefinition = (SystemDefinition)definitions.get(i);
            for (int j = 0; j < definitions.size(); j++) {
                if (j == i) {
                    continue;
                }
                SystemDefinition systemDefinition2 = (SystemDefinition)definitions.get(j);
                if (systemDefinition2.getUuid().equals(systemDefinition.getUuid())) {
                    throw new DefinitionException("UUID is not unique within hierarchy: " + systemDefinition.getUuid());
                }
            }
        }
    }

    /**
     * Propagates the aspect managers.
     *
     * @param block
     * @param blockSizeBefore
     */
    public void propagateAspectManagers(final AspectManager[] block, final int blockSizeBefore) {
        AspectManager[] newAspectManagers = new AspectManager[m_aspectManagers.length
                                            + (block.length - blockSizeBefore)];
        System.arraycopy(block, 0, newAspectManagers, 0, block.length);
        if (blockSizeBefore < m_aspectManagers.length) {
            System.arraycopy(m_aspectManagers, blockSizeBefore, newAspectManagers, block.length + 1,
                             m_aspectManagers.length - blockSizeBefore);
        }
        m_aspectManagers = newAspectManagers;
    }
}

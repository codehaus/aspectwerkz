/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import gnu.trove.TIntObjectHashMap;
import org.codehaus.aspectwerkz.connectivity.Invoker;
import org.codehaus.aspectwerkz.connectivity.RemoteProxy;
import org.codehaus.aspectwerkz.connectivity.RemoteProxyServer;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.expression.CflowExpressionVisitorRuntime;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.aspect.management.Aspects;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

/**
 * Represents the aspect runtime system. <br/>Manages the different parts of the runtime system and provides and API to
 * access and manage the system. <br/><p/>There is an AspectSystem per ClassLoader. An AspectSystem is aware of the
 * classloader hierarchy and reflects it by gathering the AspectManager, which represents a single &lt;system ..&gt;
 * entry. <p/>When an instance of an AspectSystem is created (perClassLoader), it checks for existence of previous
 * AspectManager defined in parent ClassLoader. AspectManager are shared among AspectSystem as shown below: <br/> </p>
 * <p/>
 * <pre>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 *              [0d, 1d, 2d]  (3 SystemDefs, all defined in this classloader)
 *                      /   \
 *     [0r, 1r, 2r, 3d]      \  (3 reused, one more defined)
 *                         [0r, 1r, 2r, 3d]  (one more defined, not the same)
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * </pre>
 * <p/>
 * </p> This composition strategy allow to avoid global static repository, but is tight to following ClassLoader parent
 * hierarchy. </p> If an AspectManager is added at runtime, it should be added in the whole child hierarchy. TODO </p>
 * <p/>TODO: caution when addding a new SystemDefinition in between. TODO: move the remote proxy elsewhere unless
 * defining classloader is needed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public final class AspectSystem {
    /**
     * The path to the remote proxy server config file.
     */
    private static final boolean START_REMOTE_PROXY_SERVER = "true".equals(
            java.lang.System.getProperty(
                    "aspectwerkz.remote.server.run",
                    "false"
            )
    );

    /**
     * ClassLoader defining this AspectSystem
     */
    private final ClassLoader m_classLoader;

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
     * <p/>Creates a new AspectWerkz AOPC system instance. <p/>
     *
     * @param loader      the classloader defining the system
     * @param definitions the ordered SystemDefinitions for the system (whole hierarchy)
     */
    AspectSystem(final ClassLoader loader, final List definitions) {
        m_classLoader = loader;

        // assert uuid are unique in the ClassLoader hierarchy
        assertUuidUniqueWithinHierarchy(definitions);

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
     * FIXME XXX needed? used? remove?
     *
     * Initializes the system. The initialization needs to be separated from the construction of the manager, and is
     * triggered by the runtime system
     */
    public void initialize() {
        Aspects.initialize(m_classLoader);
    }

    /**
     * Registers entering of a control flow join point.
     *
     * @param pointcutType the pointcut type
     * @param methodInfo   the method info
     * @param withinInfo   the within info
     */
    public void enteringControlFlow(
            final PointcutType pointcutType,
            final MethodInfo methodInfo,
            final ClassInfo withinInfo) {
        if (pointcutType == null) {
            throw new IllegalArgumentException("pointcut type can not be null");
        }
        if (methodInfo == null) {
            throw new IllegalArgumentException("method info can not be null");
        }
        TIntObjectHashMap cflows = (TIntObjectHashMap)m_cflowStack.get();
        if (cflows == null) {
            cflows = new TIntObjectHashMap();
        }
        ExpressionContext expressionContext = new ExpressionContext(pointcutType, methodInfo, withinInfo);
        cflows.put(expressionContext.hashCode(), expressionContext);
        m_cflowStack.set(cflows);
    }

    /**
     * Registers exiting from a control flow join point.
     *
     * @param pointcutType the pointcut type
     * @param methodInfo   the method info
     * @param withinInfo   the within info
     */
    public void exitingControlFlow(
            final PointcutType pointcutType,
            final MethodInfo methodInfo,
            final ClassInfo withinInfo) {
        if (pointcutType == null) {
            throw new IllegalArgumentException("pointcut type can not be null");
        }
        if (methodInfo == null) {
            throw new IllegalArgumentException("method info can not be null");
        }
        TIntObjectHashMap cflows = (TIntObjectHashMap)m_cflowStack.get();
        if (cflows == null) {
            return;
        }
        ExpressionContext ctx = new ExpressionContext(pointcutType, methodInfo, withinInfo);
        cflows.remove(ctx.hashCode());
        m_cflowStack.set(cflows);
    }

    /**
     * Checks if we are in the control flow of a join point picked out by a specific pointcut expression.
     *
     * @param expression        the cflow expression runtime visitor
     * @param expressionContext the join point expression context whose pointcut contains cflows sub expression(s)
     * @return boolean
     */
    public boolean isInControlFlowOf(
            final CflowExpressionVisitorRuntime expression, ExpressionContext expressionContext) {
        if (expression == null) {
            throw new IllegalArgumentException("expression can not be null");
        }
        TIntObjectHashMap cflows = (TIntObjectHashMap)m_cflowStack.get();
        if (cflows == null) {
            // we still need to evaluate the expression to handle "NOT cflow"
            cflows = new TIntObjectHashMap();
        }
        if (expression.matchCflowStack(cflows.getValues(), expressionContext)) {
            return true;
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
            public Object invoke(
                    final String handle,
                    final String methodName,
                    final Class[] paramTypes,
                    final Object[] args,
                    final Object context) {
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
     *
     * @param definitions
     * @TODO AVAOPC algo is crapped, check earlier and avoid exception but do a WARN (in SysDefContainer)
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
                    throw new DefinitionException(
                            "UUID is not unique within hierarchy: " + systemDefinition.getUuid()
                    );
                }
            }
        }
    }
}
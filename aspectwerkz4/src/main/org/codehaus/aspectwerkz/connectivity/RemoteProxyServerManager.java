/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.connectivity;

import java.util.Properties;
import java.io.FileInputStream;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.util.ContextClassLoader;

/**
 * Manages the remote proxy server.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class RemoteProxyServerManager {

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
     * The sole instance.
     */
    private static final RemoteProxyServerManager INSTANCE = new RemoteProxyServerManager();

    /**
     * The remote proxy server instance.
     */
    private RemoteProxyServer m_remoteProxyServer = null;

    /**
     * Returns the sole instance.
     *
     * @return the sole instance
     */
    public static RemoteProxyServerManager getInstance() {
        return INSTANCE;
    }

    /**
     * Starts up the remote proxy server.
     */
    public void start() {
        if (START_REMOTE_PROXY_SERVER) {
            m_remoteProxyServer = new RemoteProxyServer(ContextClassLoader.getLoader(), getInvoker());
            m_remoteProxyServer.start();
        }
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
            invoker = (Invoker) ContextClassLoader.getLoader().loadClass(className).newInstance();
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
            public Object invoke(final String handle,
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
     * Private constructor.
     */
    private RemoteProxyServerManager() {

    }
}

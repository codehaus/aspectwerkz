/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.connectivity;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.connectivity.RemoteProxyServer;
import org.codehaus.aspectwerkz.connectivity.Invoker;
import org.codehaus.aspectwerkz.connectivity.RemoteProxy;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Server {

    protected RemoteProxyServer m_proxyServer = null;

    /**
     * Starts up the remote proxy server.
     */
    protected void startRemoteProxyServer() {
        m_proxyServer = new RemoteProxyServer(
                Thread.currentThread().getContextClassLoader(),
                new Invoker() {
                    public Object invoke(final String uuid,
                                         final String methodName,
                                         final Class[] paramTypes,
                                         final Object[] args,
                                         final Object context) {
                        Object result;
                        try {
                            System.out.println("uuid = " + uuid);
                            System.out.println("methodName = " + methodName);
                            final Object instance = RemoteProxy.getWrappedInstance(uuid);
                            System.out.println("instance = " + instance);
                            final Method method = instance.getClass().
                                    getMethod(methodName, paramTypes);
                            System.out.println("method = " + method);
                            result = method.invoke(instance, args);
                            System.out.println("result = " + result);
                        }
                        catch (Exception e) {
                            throw new WrappedRuntimeException(e);
                        }
                        return result;
                    }
                });

        System.out.println("starting server...");
        m_proxyServer.start();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startRemoteProxyServer();
    }
}

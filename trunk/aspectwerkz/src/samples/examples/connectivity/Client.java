/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.connectivity;

import org.codehaus.aspectwerkz.connectivity.RemoteProxy;
import examples.introduction.Mixin;

/**
 * Creates a Mixin impl (introduction example) remotely using the remote proxy impl.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Client {

    public static void main(String[] args) {
        run();
    }

    private static void run() {
        RemoteProxy proxy1 = RemoteProxy.createClientProxy(
                new String[]{"examples.introduction.Mixin"},
                "examples.introduction.MixinImpl",
                "localhost",
                7777
        );
        RemoteProxy proxy2 = RemoteProxy.createClientProxy(
                new String[]{"examples.introduction.Mixin"},
                "examples.introduction.MixinImpl",
                "localhost",
                7777
        );

        Mixin mixin1 = (Mixin)proxy1.getInstance(); // retrieves the proxy
        Mixin mixin2 = (Mixin)proxy2.getInstance(); // retrieves the proxy

        System.out.println("Mixin1 says: " + mixin1.sayHello());
        System.out.println("Mixin2 says: " + mixin2.sayHello());

        proxy1.close(); // always call close()
        proxy2.close(); // always call close()
    }
}

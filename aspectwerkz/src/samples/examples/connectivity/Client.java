/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.connectivity;

import org.codehaus.aspectwerkz.connectivity.RemoteProxy;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Client {
    public static void main(String[] args) {
        RemoteProxy proxy = RemoteProxy.createClientProxy(
                new String[]{
                    "examples.connectivity.Test1",
                    "examples.connectivity.Test2"
                },
                "examples.connectivity.Target",
                "localhost",
                7777
        );

        Test1 test1 = (Test1)proxy.getProxy();
        System.out.println("Message: " + test1.test1());

        Test2 test2 = (Test2)proxy.getProxy();
        System.out.println("Message: " + test2.test2());

        proxy.close();
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.connectivity;

import org.codehaus.aspectwerkz.connectivity.RemoteProxy;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class Test1Impl implements Test1 {

    public String test1() {
        return "test 1";
    }

    public RemoteProxy getTest1() {
        Test2 test2 = (Test2)new Target();
        RemoteProxy proxy = RemoteProxy.createServerProxy(test2, "localhost", 7777);
        return proxy;
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.xmldef.synchronization;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import examples.util.concurrent.AsynchronousManager;
import examples.util.definition.ThreadPoolDefinition;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Target {

    public void toSynchronize() {
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // need to test the method in a concurrent environment,
        // A therefore the use of a thread pool
        ThreadPoolDefinition def = new ThreadPoolDefinition();
        def.setBounded(true);
        def.setMaxSize(10);
        def.setMinSize(2);
        def.setInitSize(2);
        def.setKeepAliveTime(6000);
        def.setWaitWhenBlocked(true);
        AsynchronousManager.getInstance().initialize(def);

        final Target target = new Target();
        for (int i = 0; i < 2; i++) {
            AsynchronousManager.getInstance().execute(new Runnable() {
                public void run() {
                    try {
                        target.toSynchronize();
                    }
                    catch (Throwable e) {
                        throw new WrappedRuntimeException(e);
                    }
                }
            });
        }
    }
}

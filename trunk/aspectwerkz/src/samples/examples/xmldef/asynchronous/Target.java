/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.asynchronous;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Target {

    public void toRunAsynchronously() {
        System.out.println("Thread: " + Thread.currentThread() + " - do some work...");
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Thread: " + Thread.currentThread() + " - work done");
    }

    public static void main(String[] args) {
        System.out.println("Thread: " + Thread.currentThread() + " - starting up 5 worker threads...");
        Target target = new Target();
        for (int i = 0; i < 5; i++) {
            target.toRunAsynchronously();
        }
        System.out.println("Thread: " + Thread.currentThread() + " - waiting for the worker threads...");
        Thread.yield();
    }
}

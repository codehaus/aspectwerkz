/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.xmldef.transparentpersistence;

import org.codehaus.aspectwerkz.extension.service.ServiceManager;
import org.codehaus.aspectwerkz.extension.Registry;

/**
 * Demonstrates the transparent persistence code.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 */
public class Client {
    public static void main(String[] args) {

        try {
            // NOTE: only needs to be called once, must be called somewhere
            // starts up the services in the system
            ServiceManager.start();

            Client.printStartMessage();
            for (int i = 0; i < 500; i++) {
                // try to find the object we want
                Object obj = Registry.findPersistentObjectByIndex(Counter.class, "uuid");

                Counter counter;
                if (obj != null) {
                    // in db; cast it
                    counter = (Counter)obj;
                }
                else {
                    // not in db; create it
                    counter = new Counter();
                }
                System.out.println("counter: " + counter.getCounter());

                // NOTE: the counter gets transparently stored when incremented
                counter.increment();
            }
            Client.printEndMessage();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printStartMessage() throws Exception {
		System.out.println("\nNote: The objects are actually read from and stored in the database between each message.\n");
        System.out.println("Sleeping for 5 seconds...");
        Thread.sleep(5 * 1000);
	}

    private static void printEndMessage() throws Exception {
		System.out.println("\nIf you run the example again, you will see that everything is persisted.\n");
	}
}

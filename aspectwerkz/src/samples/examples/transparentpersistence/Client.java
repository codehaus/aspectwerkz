/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package examples.transparentpersistence;

import org.codehaus.aspectwerkz.extension.service.ServiceManager;
import org.codehaus.aspectwerkz.extension.Registry;

/**
 * Demonstrates the transparent persistence code.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: Client.java,v 1.1.1.1 2003-05-11 15:15:38 jboner Exp $
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

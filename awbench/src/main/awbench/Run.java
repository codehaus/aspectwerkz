/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Contains run data
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Run {

    public static int ITERATIONS = 2000000;

    public static List suite = new ArrayList();

    public final String name;

    long startupTime;

    long endTime;

    public Run(String name) {
        suite.add(this);
        this.name = name;
        startupTime = System.currentTimeMillis();
    }

    public void end() {
        endTime = System.currentTimeMillis();
    }

    public long nanoPerIteration() {
        float nano = (endTime - startupTime) * 1000 * 1000 / ITERATIONS;
        return (long) (nano);
    }

    public static void flush() {
        suite = new ArrayList();
    }

    public static void report() {
        System.out.println("|-------------------------------------------------------------------------------");
        System.out.println("| Nanosecond (E-9) / iteration                              Label");
        System.out.println("|--------------------------------------------------------------------------------");
        for (Iterator iterator = suite.iterator(); iterator.hasNext();) {
            Run run = (Run) iterator.next();
            System.out.print("|  " + run.nanoPerIteration() + "      " + run.name);
            System.out.println(" (measured in " + ITERATIONS + " iterations)");
            System.out.println("|--------------------------------------------------------------------------------");
        }
    }

}

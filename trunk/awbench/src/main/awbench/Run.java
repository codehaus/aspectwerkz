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

    /** advice should update this counter so that we can check proper execution and report N/A */
    public static long ADVICE_HIT = 0;

    public static int ITERATIONS = 2000000;//10000;

    public static List suite = new ArrayList();

    public final String name;

    long startupTime;

    long endTime;

    long adviceHit;


    public Run(String name) {
        suite.add(this);
        this.name = name;
        ADVICE_HIT = 0;
        adviceHit = 0;
        startupTime = System.currentTimeMillis();
    }

    public void end() {
        adviceHit = ADVICE_HIT;
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
            //TODO add adviceHit check
            System.out.print("|  " + run.nanoPerIteration() + "      ");
            if (run.adviceHit <= 0) {
                System.out.print("[NOT ADVISED]       ");
            }
            System.out.print(run.adviceHit + "   " + run.name);
            System.out.println(" (measured in " + ITERATIONS + " iterations)");
            System.out.println("|--------------------------------------------------------------------------------");
        }
        System.out.println("| Notes: JP = reflective access to the contextual information");
        System.out.println("|             (JoinPoint, thisJoinPoint, MethodInvocation)");
        System.out.println("| Notes: SJP = statically compiled access to the contextual information");
        System.out.println("|             (StaticJoinPoint, thisJoinPointStaticPart - only available in AW and AJ");

        csvReport();
    }

    public static void csvReport() {
        System.out.print("CSV : ");
        for (Iterator iterator = suite.iterator(); iterator.hasNext();) {
            Run run = (Run) iterator.next();
            if (run.adviceHit <= 0) {
                System.out.print("");
            } else {
                System.out.print(run.nanoPerIteration());
            }
            System.out.print(";");
        }
    }

}

/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.async;

import examples.async.AsyncAspect.Async;
import examples.async.AsyncAspect.Service;

@Service
public class Math {

    @Async(timeout = 5)
    public void add(int a, int b) {
        System.out.printf(
                "[ %s ] %d + %d = %d\n",
                Thread.currentThread().getName(),
                a, b, (a + b)
        );
    }

    @Async(timeout = 5)
    public void substract(int a, int b) {
        System.out.printf(
                "[ %s ] %d - %d = %d\n",
                Thread.currentThread().getName(),
                a, b, (a - b)
        );
    }

    public static void main(String args[]) throws Throwable {
        Math math = new Math();
        System.out.println("\n================ Async sample =================");

        math.add(5, 4);
        math.add(1, 5);
        math.add(2, 6);
        math.add(4, 4);
        math.add(8, 4);

        math.substract(7, 4);
        math.substract(3, 5);
        math.substract(1, 6);
        math.substract(4, 4);
        math.substract(8, 4);
    }
}

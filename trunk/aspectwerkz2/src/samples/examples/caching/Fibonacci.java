/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.caching;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Fibonacci {

    // naive implementation of fibonacci, resulting in a lot
    // of redundant calculations of the same values.
    public static int fib(int n) {
        if (n < 2) {
            System.err.println(n + ".");
            return 1;
        }
        else {
            System.err.print(n + ",");
            return fib(n - 1) + fib(n - 2);
        }
    }

    public static void main(String[] args) {
        int f = fib(10);
        System.err.println("Fib(10) = " + f);
    }
}


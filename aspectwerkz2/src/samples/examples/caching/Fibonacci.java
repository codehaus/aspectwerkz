/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.caching;

import java.util.HashMap;

import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;

/**
 * Sample that calculates fibonacci number naively, uses an inner aspect to cache redundant calculations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
        System.err.println("fib(10) = " + fib(10));
    }

    public static class FibonacciCacheAspect extends Aspect {

        private HashMap m_cache = new HashMap();

        /**
         * @Execution int *..Fibonacci.fib(int)
         */
        Pointcut fibs;

        /**
         * @Around fibs
         */
        public Object cache(final JoinPoint joinPoint) throws Throwable {
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            Integer parameter = (Integer)signature.getParameterValues()[0];
            Integer cachedValue = (Integer)m_cache.get(parameter);
            if (cachedValue == null) {
                Object newValue = joinPoint.proceed(); // not found => calculate
                m_cache.put(parameter, newValue);
                return newValue;
            }
            else {
                System.out.println("using cache: " + cachedValue);
                return cachedValue; // return cached value
            }
        }
    }
}


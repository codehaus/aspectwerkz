/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.caching;

import java.util.HashMap;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @Aspect perInstance
 */
public class FibonacciCacheAspect extends Aspect {

    private HashMap m_cache = new HashMap();

    /**
     * @Expression execution(int *..Fibonacci.fib(int))
     */
    Pointcut fibs;

    /**
     * @Around fibs
     */
    public Object cache(final JoinPoint joinPoint) throws Throwable {
        System.out.println("FibonacciCacheAspect.cache");
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
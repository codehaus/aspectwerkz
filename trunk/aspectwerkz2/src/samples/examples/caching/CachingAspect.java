/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.caching;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perInstance
 */
public class CachingAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @Call examples.caching.*->int examples.caching.Pi.getPiDecimal(int)
     */
    Pointcut invocationCount;

    /**
     * @Execution int examples.caching.Pi.getPiDecimal(int)
     */
    Pointcut methodsToCache;

    // ============ Advices ============

    /**
     * @Around invocationCount
     */
    public Object invocationCounter(final JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        CacheStatistics.addMethodInvocation(
                signature.getName(),
                signature.getParameterTypes()
        );
        return joinPoint.proceed();
    }

    /**
     * @Around methodsToCache
     */
    public Object cache(final JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();

        final Long hash = new Long(calculateHash(signature));
        final Object cachedResult = m_cache.get(hash);

        if (cachedResult != null) {
            System.out.println("using cache");
            CacheStatistics.addCacheInvocation(signature.getName(), signature.getParameterTypes());
            System.out.println("parameter: timeout = " + ___AW_getParameter("timeout"));
            return cachedResult;
        }
        final Object result = joinPoint.proceed();

        m_cache.put(hash, result);
        return result;
    }

    // ============ Utility methods ============

    private long calculateHash(final MethodSignature signature) {
        int result = 17;
        result = 37 * result + signature.getName().hashCode();
        Object[] parameters = signature.getParameterValues();
        for (int i = 0, j = parameters.length; i < j; i++) {
            result = 37 * result + parameters[i].hashCode();
        }
        return result;
    }

    protected Map m_cache = new HashMap();
}

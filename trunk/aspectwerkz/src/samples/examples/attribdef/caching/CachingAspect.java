/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.attribdef.caching;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint;

/**
 * @Aspect perInstance
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CachingAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @Call examples.attribdef.caching.*->int examples.attribdef.caching.Pi.getPiDecimal(int)
     */
    Pointcut invocationCount;

    /**
     * @Execution int examples.attribdef.caching.Pi.getPiDecimal(int)
     */
    Pointcut methodsToCache;

    // ============ Advices ============

    /**
     * @Before invocationCount
     */
    public void invocationCounter(final JoinPoint joinPoint) throws Throwable {
        CallerSideJoinPoint jp = (CallerSideJoinPoint)joinPoint;
        CacheStatistics.addMethodInvocation(
                jp.getCalleeMethodName(),
                jp.getCalleeMethodParameterTypes());
        joinPoint.proceed();
    }

    /**
     * @Around methodsToCache
     */
    public Object cache(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;

        final Long hash = new Long(calculateHash(jp));
        final Object cachedResult = m_cache.get(hash);

        if (cachedResult != null) {
            System.out.println("using cache");
            CacheStatistics.addCacheInvocation(jp.getMethodName(), jp.getParameterTypes());
            System.out.println("parameter: timeout = " + ___AW_getParameter("timeout"));
            return cachedResult;
        }
        final Object result = joinPoint.proceed();

        m_cache.put(hash, result);
        return result;
    }

    // ============ Utility methods ============

    private long calculateHash(final MethodJoinPoint jp) {
        int result = 17;
        result = 37 * result + jp.getMethodName().hashCode();
        Object[] parameters = jp.getParameters();
        for (int i = 0, j = parameters.length; i < j; i++) {
            result = 37 * result + parameters[i].hashCode();
        }
        return result;
    }

    protected Map m_cache = new HashMap();
}

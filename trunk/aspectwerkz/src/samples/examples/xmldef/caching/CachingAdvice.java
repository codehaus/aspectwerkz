/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.xmldef.caching;

import java.util.Map;
import java.util.HashMap;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * This advice implements a simple caching service.<br/>
 *
 * It caches the results from the method invocations that are picked out
 * by the pointcuts mapped to this advice.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CachingAdvice extends AroundAdvice {

//    public static final int NR_OF_BUCKETS = 1000;
//    protected Map m_cache = new StaticBucketMap(NR_OF_BUCKETS);
    protected Map m_cache = new HashMap();

    public CachingAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;

        final Long hash = new Long(calculateHash(jp));
        final Object cachedResult = m_cache.get(hash);

        if (cachedResult != null) {
            System.out.println("using cache");
            CacheStatistics.addCacheInvocation(jp.getMethodName(), jp.getParameterTypes());
            return cachedResult;
        }

        final Object result = joinPoint.proceed();

        m_cache.put(hash, result);
        return result;
    }

    private long calculateHash(final MethodJoinPoint jp) {
        int result = 17;
        result = 37 * result + jp.getMethodName().hashCode();
        Object[] parameters = jp.getParameters();
        for (int i = 0, j = parameters.length; i < j; i++) {
            result = 37 * result + parameters[i].hashCode();
        }
        return result;
    }
}

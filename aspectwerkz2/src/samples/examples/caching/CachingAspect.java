/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.caching;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;
import org.codehaus.aspectwerkz.CrossCutting;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @Aspect perInstance
 */
public class CachingAspect {

    /**
     * @Before call(examples.caching.*->int examples.caching.Pi.getPiDecimal(int))
     */
    public void invocationCounter(final JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        CacheStatistics.addMethodInvocation(
                signature.getName(),
                signature.getParameterTypes()
        );
    }

    /**
     * @Around execution(int examples.caching.Pi.getPiDecimal(int))
     */
    public Object cache(final JoinPoint joinPoint) throws Throwable {
        MethodRtti rtti = (MethodRtti)joinPoint.getRtti();

        final Long hash = new Long(calculateHash(rtti));
        final Object cachedResult = m_cache.get(hash);

        if (cachedResult != null) {
            System.out.println("using cache");
            CacheStatistics.addCacheInvocation(rtti.getName(), rtti.getParameterTypes());
            System.out.println("parameter: timeout = " + ((CrossCutting)this).getCrossCuttingInfo().getParameter("timeout"));
            return cachedResult;
        }
        final Object result = joinPoint.proceed();

        m_cache.put(hash, result);
        return result;
    }

    // ============ Utility methods ============

    private long calculateHash(final MethodRtti rtti) {
        int result = 17;
        result = 37 * result + rtti.getName().hashCode();
        Object[] parameters = rtti.getParameterValues();
        for (int i = 0, j = parameters.length; i < j; i++) {
            result = 37 * result + parameters[i].hashCode();
        }
        return result;
    }

    protected Map m_cache = new HashMap();
}

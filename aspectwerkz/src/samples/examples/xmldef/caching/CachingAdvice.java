/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package examples.caching;

import java.util.Map;
import java.util.HashMap;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * This advice implements a simple caching service.<br/>
 *
 * It caches the results from the method invocations that are picked out
 * by the pointcuts mapped to this advice.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: CachingAdvice.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
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

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.attribdef.caching;

import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CacheStatistics {

    private static Map m_methodInvocations = Collections.synchronizedMap(new HashMap());
    private static Map m_cacheInvocations = Collections.synchronizedMap(new HashMap());

    public static void addMethodInvocation(final String methodName,
                                           final Class[] parameterTypes) {
        Long hash = calculateHash(methodName, parameterTypes);

        if (!m_methodInvocations.containsKey(hash)) {
            m_methodInvocations.put(hash, new Integer(0));
        }
        int counter = ((Integer)m_methodInvocations.get(hash)).intValue();
        counter++;
        m_methodInvocations.put(hash, new Integer(counter));
    }

    public static void addCacheInvocation(final String methodName,
                                          final Class[] parameterTypes) {
        Long hash = calculateHash(methodName, parameterTypes);

        if (!m_cacheInvocations.containsKey(hash)) {
            m_cacheInvocations.put(hash, new Integer(0));
        }
        int counter = ((Integer)m_cacheInvocations.get(hash)).intValue();
        counter++;
        m_cacheInvocations.put(hash, new Integer(counter));
    }

    public static int getNrOfMethodInvocationsFor(final String methodName,
                                                  final Class[] parameterTypes) {
        return ((Integer)m_methodInvocations.get(
                calculateHash(methodName, parameterTypes))).intValue();
    }

    public static int getNrOfCacheInvocationsFor(final String methodName,
                                                 final Class[] parameterTypes) {
        return ((Integer)m_cacheInvocations.get(
                calculateHash(methodName, parameterTypes))).intValue();
    }

    private static Long calculateHash(final String methodName,
                                      final Class[] parameterTypes) {
        int result = 17;
        result = 37 * result + methodName.hashCode();
        for (int i = 0, j = parameterTypes.length; i < j; i++) {
            result = 37 * result + parameterTypes[i].hashCode();
        }
        return new Long(result);
    }
}

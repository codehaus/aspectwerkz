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

import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

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

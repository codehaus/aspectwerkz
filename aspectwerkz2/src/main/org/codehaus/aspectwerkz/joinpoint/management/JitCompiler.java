/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import java.util.Map;

/**
 * JIT (Just-In-Time) compiler. Compiles a custom JoinPoint class that invokes all advices in a specific advice chain
 * (at a specific join point) statically.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JitCompiler {

    /**
     * 1) Grab info about the join point, which advices/aspects it uses etc. throught the JoinPointRegistry 2) Use
     * javassist to create a JoinPoint instance that calls the advices statically.
     *
     * @param joinPointHash
     * @param adviceMap
     * @return the join point
     */
    public static JoinPoint compileJoinPoint(final long joinPointHash, final Map adviceMap) {
        return null;
    }
}


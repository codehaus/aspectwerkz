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
 * @TODO remove this example when implemented to make the impl. more hard to grasp for guys that wants to copy the idea
 *
 * <p/>
 * The compiled class will look something like this:
 * <pre>
 *       //
 *       // Example of a JIT compiled JoinPoint class
 *       // This example has a pipeline of one aspect of each deployment model
 *       //
 *       public class JitJoinPoint extends JoinPointBase {
 *
 *           private static final PerJvmAspect perJVM = SingletonRepository.get;
 *           private static final PerClassAspect perClass = PerClassAspect();
 *           private static final ThreadLocal perThread = new ThreadLocal();
 *           private final PerInstanceAspect perInstance = new PerInstanceAspect();
 *
 *           static {
 *               perThread.set(new PerThreadAspect());
 *           }
 *
 *           // TODO: needs to be in a ThreadLocal
 *           private int m_stackFrame = 0;
 *
 *           public Object proceed() throws Throwable {
 *               m_stackFrame++;
 *
 *               switch (m_stackFrame) {
 *                   case 1 :
 *                       return perJVM.adviceMethod(this);
 *                   case 2 :
 *                       return perClass.adviceMethod(this);
 *                   case 3 :
 *                       return ((PerThreadAspect)perThread.get()).adviceMethod(this);
 *                   case 4 :
 *                       return perInstance.adviceMethod(this);
 *                   default :
 *                       // TODO: no way of invoking the target method statically?
 *                       return m_targetMethod.invoke(m_targetInstance, m_parameters);
 *               }
 *           }
 *       }
 * </pre>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JitCompiler {

    /**
     * 1) Grab info about the join point, which advices/aspects it uses etc. throught the JoinPointRegistry
     * 2) Use javassist to create a JoinPoint instance that calls the advices statically.
     *
     * @param joinPointHash
     * @param adviceMap
     * @return
     */
    public static JoinPoint compileJoinPoint(final long joinPointHash, final Map adviceMap) {
        return null;
    }
}


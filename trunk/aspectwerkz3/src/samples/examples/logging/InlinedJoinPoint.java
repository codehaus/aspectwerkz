/***************************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved. *
 * http://aspectwerkz.codehaus.org *
 * ---------------------------------------------------------------------------------- * The software
 * in this package is published under the terms of the LGPL license * a copy of which has been
 * included with this distribution in the license.txt file. *
 **************************************************************************************************/
package examples.logging;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Signature;

public class InlinedJoinPoint extends InlinedJoinPointBase {

    private static final Signature SIGNATURE;

    private static LoggingAspect ASPECT1;

    private static LoggingAspect ASPECT2;

    private static final Map META_DATA = new HashMap();

    static {
        SIGNATURE = null;
        ASPECT1 = (LoggingAspect) SYSTEM.getAspectManager("ID_1").getAspectContainer(8)
                .createPerJvmAspect();
        ASPECT2 = (LoggingAspect) SYSTEM.getAspectManager("ID_2").getAspectContainer(4)
                .createPerClassAspect(TARGET_CLASS);
    }

    private Target m_target;

    private Target m_this;

    private int m_stackFrame = -1;

    private int m_i;

    public static final int invoke(int i, Target targetInstance) throws Throwable {
        InlinedJoinPoint joinPoint = new InlinedJoinPoint();
        joinPoint.m_target = targetInstance;
        joinPoint.m_i = i;
        Object returnValue = joinPoint.proceed();
        return ((Integer)returnValue).intValue();
    }

    public final Object proceed() throws Throwable {
        m_stackFrame++;
        try {
            // add 'before' advice
            ASPECT1.logEntry(null);
            // add cflow
//            switch (m_stackFrame) {
//                case 0:
//
//                    // if an advice returns Object then autoboxing should occur else use real type
//                    return ASPECT1.logMethod(null);
//                case 1:
//
//                    // can pass in the signature and/or the RTTI instance to the advice
//                    return ASPECT2.advice2(null);
//                default:
//
//                    // invoke target method directly
//
//                    // if we have a:
//                    //      CALL: then invoke a wrapper method for the method call
//                    //      SET or GET: then invoke a wrapper method for the field access/modification
//                    //      these needs to be added to the target class to be able to use private fields
//                    // etc.
//                    return m_target.toLog1(m_i);

            // check for return type here for 'after returning ...'
//            }

            return new Integer(m_target.toLog1(m_i));

//        } catch (Throwable throwable) {
//            // add 'after throwing ...' advice
//            throw throwable;

        } finally {
            // add 'after' and 'after finally' advice
            ASPECT1.logExit(null);
            // add cflow
            m_stackFrame--;
        }
    }
    public Signature getSignature()
    {
        return SIGNATURE;
    }

    public void addMetaData(Object obj, Object obj1)
    {
        META_DATA.put(obj, obj1);
    }

    public Object getMetaData(Object obj)
    {
        return META_DATA.get(obj);
    }

    public Target getTarget()
    {
        return m_target;
    }

    public Target getThis()
    {
        return m_this;
    }
}
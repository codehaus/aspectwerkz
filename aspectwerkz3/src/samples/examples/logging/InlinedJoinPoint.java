/***********************************************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved. * http://aspectwerkz.codehaus.org *
 * ---------------------------------------------------------------------------------- * The software in this package is
 * published under the terms of the LGPL license * a copy of which has been included with this distribution in the
 * license.txt file. *
 **********************************************************************************************************************/
package examples.logging;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.management.SignatureFactory;
import org.codehaus.aspectwerkz.transform.ReflectHelper;

public class InlinedJoinPoint extends InlinedJoinPointBase {

    private static final Signature SIGNATURE;

    private static LoggingAspect ASPECT1;

    private static LoggingAspect ASPECT2;

    private static final Map META_DATA = new HashMap();

    static {
        SIGNATURE = SignatureFactory.newMethodSignature(TARGET_CLASS, -2091835264);
        ASPECT1 = (LoggingAspect) SYSTEM.getAspectManager("samples").getAspectContainer(4).createPerJvmAspect();
        ASPECT2 = (LoggingAspect) SYSTEM.getAspectManager("samples").getAspectContainer(4).createPerClassAspect(
            TARGET_CLASS);
    }

    private Target m_target;

    private Target m_this;

    // TODO performance penalty in CTOR
    private MethodRttiImpl m_rtti = new MethodRttiImpl((MethodSignatureImpl)SIGNATURE, m_this, m_target);

    private int m_stackFrame = -1;

    private int m_i;

    private String m_s;

    public static final int invoke(int i, Target callerInstance, Target calleeInstance) throws Throwable {
        InlinedJoinPoint joinPoint = new InlinedJoinPoint();
        joinPoint.m_this = callerInstance;
        joinPoint.m_target = calleeInstance;
        joinPoint.m_i = i;
        //TODO joinPoint.m_rtti.setParameterValues(new Object[]{.....});//performance penalty
        // add cflow
        Object returnValue = joinPoint.proceed();
        // add cflow
        //TODO joinPoint.m_rtti.setReturnValue(returnValue);
        return ((Integer) returnValue).intValue();
    }

    public final Object proceed() throws Throwable {
        m_stackFrame++;
        try {
            switch (m_stackFrame) {
                case 0:

                    // if an advice returns Object then autoboxing should occur else use real type
                    return ASPECT1.logMethod(null);
                case 1:

                    // can pass in the signature and/or the RTTI instance to the advice
                    return ASPECT2.logMethod(null);
                default:

                    // invoke target method directly

                    // if we have a:
                    //      CALL: then invoke a wrapper method for the method call
                    //      SET or GET: then invoke a wrapper method for the field access/modification
                    //      these needs to be added to the target class to be able to use private fields
                    // etc.
                    return new Integer(m_target.toLog1(m_i));
            }
        } finally {
            m_stackFrame--;
        }
    }

    public Signature getSignature() {
        return SIGNATURE;
    }

    public void addMetaData(Object obj, Object obj1) {
        META_DATA.put(obj, obj1);
    }

    public Object getMetaData(Object obj) {
        return META_DATA.get(obj);
    }

    public Object/* Target */getTarget() {
        return m_target;
    }

    public Target getThis() {
        return m_this;
    }

    public Rtti getRtti() {
        return m_rtti;
    }

}


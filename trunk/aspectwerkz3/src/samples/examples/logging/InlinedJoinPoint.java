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

import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.management.SignatureFactory;

//import org.codehaus.aspectwerkz.joinpoint.Signature;
//import org.codehaus.aspectwerkz.joinpoint.management.SignatureFactory;
//import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;
//import examples.logging.LoggingAspect;
//import examples.logging.Target;
//
//import java.util.Map;
//import java.util.HashMap;

public class InlinedJoinPoint extends InlinedJoinPointBase {

    //    static class Dummmy {
    //        public static int invoke(long l, int i, short s, double d, float f, byte b, boolean bool,
    // char c, Object o, String[] arr) {
    //            return 0;
    //        }
    //    }
    //
    //    public int toAdvise(long l, int i, short s, double d, float f, byte b, boolean bool, char c,
    // Object o, String[] arr) {
    //        return Dummmy.invoke(l, i, s, d, f, b, bool, c, o, arr);
    //    }

    //    private static final Signature SIGNATURE;
    //    private static LoggingAspect ASPECT1;
    //    private static LoggingAspect ASPECT2;
    //    private static final Map META_DATA = new HashMap();

    private Target m_target;

    private Target m_this;

    private int m_stackFrame = -1;

    private int m_i;

    //    static {
    //        SIGNATURE = SignatureFactory.newMethodSignature(TARGET_CLASS, 123456789);
    //        ASPECT1 = (LoggingAspect)SYSTEM.getAspectManager("ID_1").
    //                getAspectContainer(8).createPerJvmAspect();
    //        ASPECT2 = (LoggingAspect)SYSTEM.getAspectManager("ID_2").
    //                getAspectContainer(4).createPerClassAspect(TARGET_CLASS);
    //    }

//    public static final int invoke(int i, Target targetInstance) throws Throwable {
//        InlinedJoinPoint joinPoint = new InlinedJoinPoint();
//        joinPoint.m_target = targetInstance;
//        joinPoint.m_i = i;
//        return joinPoint.proceed();
//    }

    public final int proceed() throws Throwable {
        return m_target.toLog1();
    }


    public static void main(String[] args) {
        InlinedJoinPoint jp = new InlinedJoinPoint();
        jp.test();
    }

    private void test() {
        InlinedJoinPoint.invoke(1, null);
    }

    public static final void invoke(int i, InlinedJoinPoint point) {
        
    }
        
        
        //        m_stackFrame++;
    //        try {
    //            ASPECT1.logEntry(null);
    //            switch (m_stackFrame) {
    //                case 0:
    //                case 1:
    //                default:
    //                    return m_target.getCounter();
    //            }
    //        } catch (Throwable throwable) {
    //            throw throwable;
    //        } finally {
    //            ASPECT1.logExit(null);
    //            m_stackFrame--;
    //        }
    //    }

    //    public Signature getSignature() {
    //        return SIGNATURE;
    //    }
    //
    //    public void addMetaData(Object key, Object value) {
    //        META_DATA.put(key, value);
    //    }
    //
    //    public Object getMetaData(Object key) {
    //        return META_DATA.get(key);
    //    }
    //
    //    public Target getTarget() {
    //        return m_target;
    //    }
    //
    //    public Target getThis() {
    //        return m_this;
    //    }

    /***********************************************************************************************
     * // signature private int m_stackFrame = -1; private int m_int; private String m_string;
     * private float m_float; // target instance private InlineExample m_target; public static final
     * int invoke(int i, String s, float f, InlineExample targetInstance) throws Throwable {
     * __AW_Inlined_Execution_methodToAdvise joinPoint = new
     * __AW_Inlined_Execution_methodToAdvise(); joinPoint.m_int = i; joinPoint.m_string = s;
     * joinPoint.m_float = f; joinPoint.m_target = targetInstance; return joinPoint.proceed(); }
     * public final int proceed() throws Throwable { m_stackFrame++; try { // add 'before' advice //
     * add cflow switch (m_stackFrame) { case 0: // if an advice returns Object then autoboxing
     * should occur else use real type // return ASPECT1.advice1(this); case 1: // can pass in the
     * signature and/or the RTTI instance to the advice // return ASPECT2.advice2(m_int, m_float,
     * SIGNATURE, this); default: // invoke target method directly // if we have a: // CALL: then
     * invoke a wrapper method for the method call // SET or GET: then invoke a wrapper method for
     * the field access/modification // these needs to be added to the target class to be able to
     * use private fields etc. return m_target.___AW_methodToAdvise(m_int, m_string, m_float); //
     * check for return type here for 'after returning ...' } } catch (Throwable throwable) { // add
     * 'after throwing ...' advice throw throwable; } finally { // add 'after' and 'after finally'
     * advice // add cflow m_stackFrame--; } }
     **********************************************************************************************/
}
/***********************************************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved. * http://aspectwerkz.codehaus.org *
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

    private int m_stackFrame = -1;

    private int m_i;

    private String m_s;

    public static final int invoke(int i, Target targetInstance) throws Throwable {
        InlinedJoinPoint joinPoint = new InlinedJoinPoint();
        joinPoint.m_target = targetInstance;
        joinPoint.m_i = i;
        // add cflow
        Object returnValue = joinPoint.proceed();
        // add cflow
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
}


/**
cv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "proceed", "()Ljava/lang/Object;", new String[] { "java/lang/Throwable" }, null);

  INC

Label l0 = new Label();
cv.visitLabel(l0);
cv.visitVarInsn(ALOAD, 0);
cv.visitFieldInsn(GETFIELD, "examples/logging/InlinedJoinPoint", "m_stackFrame", "I");
Label l1 = new Label();
Label l2 = new Label();
Label l3 = new Label();
cv.visitLookupSwitchInsn(l3, new int[] { 0, 1 }, new Label[] { l1, l2 });
cv.visitLabel(l1);

  ASPECT1
cv.visitFieldInsn(GETSTATIC, "examples/logging/InlinedJoinPoint", "ASPECT1", "Lexamples/logging/LoggingAspect;");
cv.visitInsn(ACONST_NULL);
cv.visitMethodInsn(INVOKEVIRTUAL, "examples/logging/LoggingAspect", "logMethod", "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;");
cv.visitVarInsn(ASTORE, 1);

Label l4 = new Label();
cv.visitLabel(l4);

  DEC
cv.visitVarInsn(ALOAD, 1);
cv.visitInsn(ARETURN);

cv.visitLabel(l2);

  ASPECT2
cv.visitFieldInsn(GETSTATIC, "examples/logging/InlinedJoinPoint", "ASPECT2", "Lexamples/logging/LoggingAspect;");
cv.visitInsn(ACONST_NULL);
cv.visitMethodInsn(INVOKEVIRTUAL, "examples/logging/LoggingAspect", "logMethod", "(Lorg/codehaus/aspectwerkz/joinpoint/JoinPoint;)Ljava/lang/Object;");
cv.visitVarInsn(ASTORE, 1);

Label l5 = new Label();
cv.visitLabel(l5);

  DEC
cv.visitVarInsn(ALOAD, 1);
cv.visitInsn(ARETURN);

cv.visitLabel(l3);

  JP

Label l6 = new Label();
cv.visitLabel(l6);

  DEC
cv.visitVarInsn(ALOAD, 1);
cv.visitInsn(ARETURN);

Label l7 = new Label();
cv.visitLabel(l7);
cv.visitVarInsn(ASTORE, 2);

Label l8 = new Label();
cv.visitLabel(l8);

  DEC
cv.visitVarInsn(ALOAD, 2);
cv.visitInsn(ATHROW);

cv.visitTryCatchBlock(l0, l4, l7, null);
cv.visitTryCatchBlock(l2, l5, l7, null);
cv.visitTryCatchBlock(l3, l6, l7, null);
cv.visitTryCatchBlock(l7, l8, l7, null);
cv.visitMaxs(3, 3);
}
*/
/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import org.objectweb.asm.*;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;

import java.util.Iterator;

/**
 * A ClassAdapter that take care of all weaved class and add the glue between the class and its JIT dependencies.
 * <p/>
 * Adds a 'private static final Class aw$clazz' field a 'private static void ___AW_$_AW_$initJoinPoints()' method
 * and patches the 'clinit' method.
 * <p/>
 * If the class has been made advisable, we also add a ___AW_$_AW_$emittedJoinPoints fields that gets populated.
 *
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * TODO: for multi weaving, we could go on in adding several AW initJoinPoints_xxWeaveCount method, but then cannot be
 * done with RW
 */
public class JoinPointInitVisitor extends ClassAdapter implements TransformationConstants {

    private final ContextImpl m_ctx;
    private boolean m_hasClinitMethod = false;
    private boolean m_hasInitJoinPointsMethod = false;
    private boolean m_hasClassField = false;
    private boolean m_hasEmittedJoinPointsField = false;

    /**
     * Creates a new instance.
     *
     * @param cv
     * @param ctx
     */
    public JoinPointInitVisitor(final ClassVisitor cv, final Context ctx) {
        super(cv);
        m_ctx = (ContextImpl) ctx;
    }

    /**
     * Visits the methods. If the AW joinPointsInit method is found, remember that, it means we are in a multi-weaving
     * scheme. Patch the 'clinit' method if already present.
     *
     * TODO: multi-weaving will lead to several invocation of AW initJoinPoints and several assigment of __AW_Clazz in the patched clinit which slows down a bit the load time
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, String, String, String, String[])
     */
    public MethodVisitor visitMethod(final int access,
                                   final String name,
                                   final String desc,
                                   final String signature,
                                   final String[] exceptions) {

        if (CLINIT_METHOD_NAME.equals(name)) {
            m_hasClinitMethod = true;
            // at the beginning of the existing <clinit> method
            //      ___AWClazz = Class.forName("TargetClassName");
            //      ___AW_$_AW_$initJoinPoints();
            MethodVisitor ca = new InsertBeforeClinitCodeAdapter(cv.visitMethod(access, name, desc, signature, exceptions));
            ca.visitMaxs(0, 0);
            return ca;

        } else if (INIT_JOIN_POINTS_METHOD_NAME.equals(name)) {
            m_hasInitJoinPointsMethod = true;
            // add the gathered JIT dependencies for multi-weaving support
            MethodVisitor ca = new InsertBeforeInitJoinPointsCodeAdapter(
                    cv.visitMethod(access, name, desc, signature, exceptions)
            );
            ca.visitMaxs(0, 0);
            return ca;
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    /**
     * Remember if we have already the static class field for multi-weaving scheme.
     *
     * @param access
     * @param name
     * @param desc
     * @param signature
     * @param value
     */
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (TARGET_CLASS_FIELD_NAME.equals(name)) {
            m_hasClassField = true;
        } else if (EMITTED_JOINPOINTS_FIELD_NAME.equals(name)) {
            m_hasEmittedJoinPointsField = true;
        }
        return super.visitField(access, name, desc, signature, value);
    }

    /**
     * Finalize the visit. Add static class field if needed, add initJoinPoints method if needed, add <clinit>if
     * needed.
     */
    public void visitEnd() {
        if (!m_ctx.isAdvised()) {
            super.visitEnd();
            return;
        }

        if (!m_hasClassField) {
            // create field
            //      private final static Class aw$clazz = Class.forName("TargetClassName");
            cv.visitField(
                    ACC_PRIVATE + ACC_FINAL + ACC_STATIC + ACC_SYNTHETIC,
                    TARGET_CLASS_FIELD_NAME,
                    CLASS_CLASS_SIGNATURE,
                    null,
                    null
            );
        }

        if (!m_hasEmittedJoinPointsField && m_ctx.isMadeAdvisable()) {
            // create field
            //      private final static Class aw$emittedJoinPoints that will host a Trove int Object map
            cv.visitField(
                    ACC_PRIVATE + ACC_FINAL + ACC_STATIC + ACC_SYNTHETIC,
                    EMITTED_JOINPOINTS_FIELD_NAME,
                    "Lgnu/trove/TIntObjectHashMap;",
                    null,
                    null
            );
        }

        if (!m_hasClinitMethod) {
            MethodVisitor ca = new InsertBeforeClinitCodeAdapter(
                    cv.visitMethod(
                            ACC_STATIC,
                            CLINIT_METHOD_NAME,
                            NO_PARAM_RETURN_VOID_SIGNATURE,
                            null,
                            null
                    )
            );
            ca.visitInsn(RETURN);
            ca.visitMaxs(0, 0);
        }

        if (!m_hasInitJoinPointsMethod) {
            MethodVisitor mv = new InsertBeforeInitJoinPointsCodeAdapter(
                    cv.visitMethod(
                            ACC_PRIVATE + ACC_FINAL + ACC_STATIC + ACC_SYNTHETIC,
                            INIT_JOIN_POINTS_METHOD_NAME,
                            NO_PARAM_RETURN_VOID_SIGNATURE,
                            null,
                            null
                    )
            );
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
        }

        cv.visitEnd();
    }

    /**
     * Handles the method body of the <clinit>method.
     *
     * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
     */
    public class InsertBeforeClinitCodeAdapter extends MethodAdapter {

        public InsertBeforeClinitCodeAdapter(MethodVisitor ca) {
            super(ca);
            if (!m_hasClassField) {
                mv.visitLdcInsn(m_ctx.getClassName().replace('/', '.'));
                mv.visitMethodInsn(INVOKESTATIC, CLASS_CLASS, FOR_NAME_METHOD_NAME, FOR_NAME_METHOD_SIGNATURE);
                mv.visitFieldInsn(PUTSTATIC, m_ctx.getClassName(), TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
            }
            if (!m_hasEmittedJoinPointsField && m_ctx.isMadeAdvisable()) {
                // aw$emittedJoinPoints = new TIntObjectHashMap()
                mv.visitTypeInsn(NEW, "gnu/trove/TIntObjectHashMap");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "gnu/trove/TIntObjectHashMap", "<init>", "()V");
                mv.visitFieldInsn(PUTSTATIC, m_ctx.getClassName(), EMITTED_JOINPOINTS_FIELD_NAME, "Lgnu/trove/TIntObjectHashMap;");
            }
            if (!m_hasClassField) {
                mv.visitMethodInsn(
                        INVOKESTATIC,
                        m_ctx.getClassName(),
                        INIT_JOIN_POINTS_METHOD_NAME,
                        NO_PARAM_RETURN_VOID_SIGNATURE
                );
            }
        }
    }

    /**
     * Handles the method body of the AW initJoinPoints method.
     *
     * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
     */
    public class InsertBeforeInitJoinPointsCodeAdapter extends MethodAdapter {

        public InsertBeforeInitJoinPointsCodeAdapter(MethodVisitor ca) {
            super(ca);

            // loop over emitted jp and insert call to "JoinPointManager.loadJoinPoint(...)"
            // add calls to aw$emittedJoinPoints.put(.. new EmittedJoinPoint) if needed.
            for (Iterator iterator = m_ctx.getEmittedJoinPoints().iterator(); iterator.hasNext();) {

                EmittedJoinPoint jp = (EmittedJoinPoint) iterator.next();
                mv.visitLdcInsn(new Integer(jp.getJoinPointType()));

                mv.visitFieldInsn(GETSTATIC, m_ctx.getClassName(), TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
                mv.visitLdcInsn(jp.getCallerMethodName());
                mv.visitLdcInsn(jp.getCallerMethodDesc());
                mv.visitLdcInsn(new Integer(jp.getCallerMethodModifiers()));

                mv.visitLdcInsn(jp.getCalleeClassName());
                mv.visitLdcInsn(jp.getCalleeMemberName());
                mv.visitLdcInsn(jp.getCalleeMemberDesc());
                mv.visitLdcInsn(new Integer(jp.getCalleeMemberModifiers()));

                mv.visitLdcInsn(new Integer(jp.getJoinPointHash()));
                mv.visitLdcInsn(jp.getJoinPointClassName());
                mv.visitMethodInsn(
                        INVOKESTATIC,
                        JOIN_POINT_MANAGER_CLASS_NAME,
                        LOAD_JOIN_POINT_METHOD_NAME,
                        LOAD_JOIN_POINT_METHOD_SIGNATURE
                );

                if (m_ctx.isMadeAdvisable()) {
                    // trove map
                    mv.visitFieldInsn(GETSTATIC, m_ctx.getClassName(), EMITTED_JOINPOINTS_FIELD_NAME, "Lgnu/trove/TIntObjectHashMap;");
                    // trove map key
                    mv.visitLdcInsn(new Integer(jp.getJoinPointClassName().hashCode()));


                    mv.visitTypeInsn(NEW, "org/codehaus/aspectwerkz/transform/inlining/EmittedJoinPoint");
                    mv.visitInsn(DUP);

                    mv.visitLdcInsn(new Integer(jp.getJoinPointType()));

                    mv.visitLdcInsn(m_ctx.getClassName());
                    mv.visitLdcInsn(jp.getCallerMethodName());
                    mv.visitLdcInsn(jp.getCallerMethodDesc());
                    mv.visitLdcInsn(new Integer(jp.getCallerMethodModifiers()));

                    mv.visitLdcInsn(jp.getCalleeClassName());
                    mv.visitLdcInsn(jp.getCalleeMemberName());
                    mv.visitLdcInsn(jp.getCalleeMemberDesc());
                    mv.visitLdcInsn(new Integer(jp.getCalleeMemberModifiers()));

                    mv.visitLdcInsn(new Integer(jp.getJoinPointHash()));
                    mv.visitLdcInsn(jp.getJoinPointClassName());

                    mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/aspectwerkz/transform/inlining/EmittedJoinPoint", "<init>",
                            "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;)V"
                            );
                    mv.visitMethodInsn(
                            INVOKEVIRTUAL,
                            "gnu/trove/TIntObjectHashMap",
                            "put",
                            "(ILjava/lang/Object;)Ljava/lang/Object;"
                    );
                }
            }
        }
    }

}
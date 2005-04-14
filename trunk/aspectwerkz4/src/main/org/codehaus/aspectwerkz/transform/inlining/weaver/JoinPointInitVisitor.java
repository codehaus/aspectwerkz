/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.CodeAdapter;
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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
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
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String[],
            *      org.objectweb.asm.Attribute)
     */
    public CodeVisitor visitMethod(final int access,
                                   final String name,
                                   final String desc,
                                   final String[] exceptions,
                                   final Attribute attrs) {

        if (CLINIT_METHOD_NAME.equals(name)) {
            m_hasClinitMethod = true;
            // at the beginning of the existing <clinit> method
            //      ___AWClazz = Class.forName("TargetClassName");
            //      ___AW_$_AW_$initJoinPoints();
            CodeVisitor ca = new InsertBeforeClinitCodeAdapter(cv.visitMethod(access, name, desc, exceptions, attrs));
            ca.visitMaxs(0, 0);
            return ca;

        } else if (INIT_JOIN_POINTS_METHOD_NAME.equals(name)) {
            m_hasInitJoinPointsMethod = true;
            // add the gathered JIT dependencies for multi-weaving support
            CodeVisitor ca = new InsertBeforeInitJoinPointsCodeAdapter(
                    cv.visitMethod(access, name, desc, exceptions, attrs)
            );
            ca.visitMaxs(0, 0);
            return ca;
        } else {
            return super.visitMethod(access, name, desc, exceptions, attrs);
        }
    }

    /**
     * Remember if we have already the static class field for multi-weaving scheme.
     *
     * @param access
     * @param name
     * @param desc
     * @param value
     * @param attrs
     */
    public void visitField(int access, String name, String desc, Object value, Attribute attrs) {
        if (TARGET_CLASS_FIELD_NAME.equals(name)) {
            m_hasClassField = true;
        } else if (EMITTED_JOINPOINTS_FIELD_NAME.equals(name)) {
            m_hasEmittedJoinPointsField = true;
        }
        super.visitField(access, name, desc, value, attrs);
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
            CodeVisitor ca = new InsertBeforeClinitCodeAdapter(
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
            CodeVisitor mv = new InsertBeforeInitJoinPointsCodeAdapter(
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
    public class InsertBeforeClinitCodeAdapter extends CodeAdapter {

        public InsertBeforeClinitCodeAdapter(CodeVisitor ca) {
            super(ca);
            if (!m_hasClassField) {
                cv.visitLdcInsn(m_ctx.getClassName().replace('/', '.'));
                cv.visitMethodInsn(INVOKESTATIC, CLASS_CLASS, FOR_NAME_METHOD_NAME, FOR_NAME_METHOD_SIGNATURE);
                cv.visitFieldInsn(PUTSTATIC, m_ctx.getClassName(), TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
            }
            if (!m_hasEmittedJoinPointsField && m_ctx.isMadeAdvisable()) {
                // aw$emittedJoinPoints = new TIntObjectHashMap()
                cv.visitTypeInsn(NEW, "gnu/trove/TIntObjectHashMap");
                cv.visitInsn(DUP);
                cv.visitMethodInsn(INVOKESPECIAL, "gnu/trove/TIntObjectHashMap", "<init>", "()V");
                cv.visitFieldInsn(PUTSTATIC, m_ctx.getClassName(), EMITTED_JOINPOINTS_FIELD_NAME, "Lgnu/trove/TIntObjectHashMap;");
            }
            if (!m_hasClassField) {
                cv.visitMethodInsn(
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
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    public class InsertBeforeInitJoinPointsCodeAdapter extends CodeAdapter {

        public InsertBeforeInitJoinPointsCodeAdapter(CodeVisitor ca) {
            super(ca);

            // loop over emitted jp and insert call to "JoinPointManager.loadJoinPoint(...)"
            // add calls to aw$emittedJoinPoints.put(.. new EmittedJoinPoint) if needed.
            for (Iterator iterator = m_ctx.getEmittedJoinPoints().iterator(); iterator.hasNext();) {

                EmittedJoinPoint jp = (EmittedJoinPoint) iterator.next();
                cv.visitLdcInsn(new Integer(jp.getJoinPointType()));

                cv.visitFieldInsn(GETSTATIC, m_ctx.getClassName(), TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
                cv.visitLdcInsn(jp.getCallerMethodName());
                cv.visitLdcInsn(jp.getCallerMethodDesc());
                cv.visitLdcInsn(new Integer(jp.getCallerMethodModifiers()));

                cv.visitLdcInsn(jp.getCalleeClassName());
                cv.visitLdcInsn(jp.getCalleeMemberName());
                cv.visitLdcInsn(jp.getCalleeMemberDesc());
                cv.visitLdcInsn(new Integer(jp.getCalleeMemberModifiers()));

                cv.visitLdcInsn(new Integer(jp.getJoinPointHash()));
                cv.visitLdcInsn(jp.getJoinPointClassName());
                cv.visitMethodInsn(
                        INVOKESTATIC,
                        JOIN_POINT_MANAGER_CLASS_NAME,
                        LOAD_JOIN_POINT_METHOD_NAME,
                        LOAD_JOIN_POINT_METHOD_SIGNATURE
                );

                if (m_ctx.isMadeAdvisable()) {
                    // trove map
                    cv.visitFieldInsn(GETSTATIC, m_ctx.getClassName(), EMITTED_JOINPOINTS_FIELD_NAME, "Lgnu/trove/TIntObjectHashMap;");
                    // trove map key
                    cv.visitLdcInsn(new Integer(jp.getJoinPointClassName().hashCode()));


                    cv.visitTypeInsn(NEW, "org/codehaus/aspectwerkz/transform/inlining/EmittedJoinPoint");
                    cv.visitInsn(DUP);

                    cv.visitLdcInsn(new Integer(jp.getJoinPointType()));

                    cv.visitLdcInsn(m_ctx.getClassName());
                    cv.visitLdcInsn(jp.getCallerMethodName());
                    cv.visitLdcInsn(jp.getCallerMethodDesc());
                    cv.visitLdcInsn(new Integer(jp.getCallerMethodModifiers()));

                    cv.visitLdcInsn(jp.getCalleeClassName());
                    cv.visitLdcInsn(jp.getCalleeMemberName());
                    cv.visitLdcInsn(jp.getCalleeMemberDesc());
                    cv.visitLdcInsn(new Integer(jp.getCalleeMemberModifiers()));

                    cv.visitLdcInsn(new Integer(jp.getJoinPointHash()));
                    cv.visitLdcInsn(jp.getJoinPointClassName());

                    cv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/aspectwerkz/transform/inlining/EmittedJoinPoint", "<init>",
                            "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;)V"
                            );
                    cv.visitMethodInsn(
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
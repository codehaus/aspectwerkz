/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import java.util.Iterator;
import java.util.Set;

import org.objectweb.asm.*;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.reflect.ClassInfo;

/**
 * Adds an instance level aspect management to the target class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class InstanceLevelAspectVisitor extends ClassAdapter implements TransformationConstants {

    private final ContextImpl m_ctx;
    private final ClassInfo m_classInfo;
    private boolean m_isAdvised = false;

    /**
     * Creates a new add interface class adapter.
     *
     * @param cv
     * @param classInfo
     * @param ctx
     */
    public InstanceLevelAspectVisitor(final ClassVisitor cv,
                                      final ClassInfo classInfo,
                                      final Context ctx) {
        super(cv);
        m_classInfo = classInfo;
        m_ctx = (ContextImpl) ctx;
    }

    /**
     * Visits the class.
     *
     * @param access
     * @param name
     * @param superName
     * @param interfaces
     * @param sourceFile
     */
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String superName,
                      final String[] interfaces,
                      final String sourceFile) {
        for (int i = 0; i < interfaces.length; i++) {
            String anInterface = interfaces[i];
            if (anInterface.equals(HAS_INSTANCE_LEVEL_ASPECT_INTERFACE_NAME)) {
                super.visit(version, access, name, superName, interfaces, sourceFile);
                return;
            }
        }
        String[] newInterfaceArray = new String[interfaces.length + 1];
        for (int i = 0; i < interfaces.length; i++) {
            newInterfaceArray[i] = interfaces[i];
        }
        newInterfaceArray[interfaces.length] = HAS_INSTANCE_LEVEL_ASPECT_INTERFACE_NAME;

        // add the interface
        super.visit(version, access, name, superName, newInterfaceArray, sourceFile);

        // add the field with the aspect instance map
        addAspectMapField();

        // add the getAspect(..) method
        addGetAspectMethod(name);
    }

    /**
     * Appends mixin instantiation to the clinit method and/or init method.
     *
     * @param access
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     * @return
     */
    public CodeVisitor visitMethod(final int access,
                                   final String name,
                                   final String desc,
                                   final String[] exceptions,
                                   final Attribute attrs) {
        if (m_isAdvised) {
            if (name.equals(INIT_METHOD_NAME)) {
                CodeVisitor mv = new AppendToInitMethodCodeAdapter(
                        cv.visitMethod(access, name, desc, exceptions, attrs)
                );
                mv.visitMaxs(0, 0);
                return mv;
            }
        }
        return cv.visitMethod(access, name, desc, exceptions, attrs);
    }

    /**
     * Adds the aspect map field to the target class.
     */
    private void addAspectMapField() {
        super.visitField(
                ACC_PRIVATE + ACC_SYNTHETIC + ACC_FINAL,
                INSTANCE_LEVEL_ASPECT_MAP_FIELD_NAME,
                INSTANCE_LEVEL_ASPECT_MAP_FIELD_SIGNATURE,
                null, null
        );
    }

    /**
     * Adds the getAspect(..) method to the target class.
     *
     * @param name the class name of the target class
     */
    private void addGetAspectMethod(final String name) {
        CodeVisitor cv = super.visitMethod(
                ACC_PUBLIC + ACC_SYNTHETIC,
                GET_INSTANCE_LEVEL_ASPECT_METHOD_NAME,
                GET_INSTANCE_LEVEL_ASPECT_METHOD_SIGNATURE,
                null, null
        );
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(
                GETFIELD,
                name,
                INSTANCE_LEVEL_ASPECT_MAP_FIELD_NAME,
                INSTANCE_LEVEL_ASPECT_MAP_FIELD_SIGNATURE
        );
        cv.visitVarInsn(ALOAD, 2);
        cv.visitMethodInsn(INVOKEINTERFACE, MAP_CLASS_NAME, GET_METHOD_NAME, GET_METHOD_SIGNATURE);
        cv.visitVarInsn(ASTORE, 3);
        cv.visitVarInsn(ALOAD, 3);
        Label ifNullNotLabel = new Label();
        cv.visitJumpInsn(IFNONNULL, ifNullNotLabel);
        cv.visitVarInsn(ALOAD, 1);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitMethodInsn(
                INVOKESTATIC,
                ASPECTS_CLASS_NAME,
                ASPECT_OF_METHOD_NAME,
                ASPECT_OF_PER_INSTANCE_METHOD_SIGNATURE
        );
        cv.visitVarInsn(ASTORE, 3);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(
                GETFIELD,
                name,
                INSTANCE_LEVEL_ASPECT_MAP_FIELD_NAME,
                INSTANCE_LEVEL_ASPECT_MAP_FIELD_SIGNATURE
        );
        cv.visitVarInsn(ALOAD, 2);
        cv.visitVarInsn(ALOAD, 3);
        cv.visitMethodInsn(INVOKEINTERFACE, MAP_CLASS_NAME, PUT_METHOD_NAME, PUT_METHOD_SIGNATURE);
        cv.visitInsn(POP);
        cv.visitLabel(ifNullNotLabel);
        cv.visitVarInsn(ALOAD, 3);
        cv.visitInsn(ARETURN);
        cv.visitMaxs(0, 0);

        m_ctx.markAsAdvised();
        m_isAdvised = true;
    }

    /**
     * Adds initialization of aspect map field to end of the init method.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private class AppendToInitMethodCodeAdapter extends CodeAdapter {

        /**
         * Flag to track if we have visited the this() or super()
         * method invocation in the visited constructor
         */
        private boolean m_done = false;

        public AppendToInitMethodCodeAdapter(final CodeVisitor ca) {
            super(ca);
        }

        /**
         * Inserts the init of the aspect field right after the call to super(..) of this(..).
         *
         * @param opcode
         * @param owner
         * @param name
         * @param desc
         */
        public void visitMethodInsn(int opcode,
                                    String owner,
                                    String name,
                                    String desc) {

            if (opcode == INVOKESPECIAL) {
                m_done = true;
                cv.visitMethodInsn(opcode, owner, name, desc);

                // initialize aspect map field
                cv.visitVarInsn(ALOAD, 0);
                cv.visitTypeInsn(NEW, HASH_MAP_CLASS_NAME);
                cv.visitInsn(DUP);
                cv.visitMethodInsn(
                        INVOKESPECIAL,
                        HASH_MAP_CLASS_NAME,
                        INIT_METHOD_NAME,
                        NO_PARAM_RETURN_VOID_SIGNATURE
                );
                cv.visitFieldInsn(
                        PUTFIELD,
                        m_classInfo.getName().replace('.', '/'),
                        INSTANCE_LEVEL_ASPECT_MAP_FIELD_NAME,
                        INSTANCE_LEVEL_ASPECT_MAP_FIELD_SIGNATURE
                );
            } else {
                cv.visitMethodInsn(opcode, owner, name, desc);
            }
        }
    }
}
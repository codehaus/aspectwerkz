/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.compiler;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;

import java.lang.reflect.Modifier;

/**
 * A compiler that compiles/generates a class that represents a specific join point, a class which invokes the advices
 * and the target join point statically.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur </a>
 */
public class MethodCallJoinPointCompiler extends AbstractJoinPointCompiler {

    /**
     * Creates a new join point compiler instance.
     *
     * @param model
     */
    MethodCallJoinPointCompiler(final CompilationInfo.Model model) {
        super(model);
    }

    /**
     * Creates fields common for all join point classes.
     */
    protected void createFieldsCommonToAllJoinPoints() {
        m_cw.visitField(
                ACC_PRIVATE + ACC_STATIC,
                TARGET_CLASS_FIELD_NAME,
                CLASS_CLASS_SIGNATURE,
                null,
                null
        );
        m_cw.visitField(
                ACC_PRIVATE + ACC_STATIC,
                SIGNATURE_FIELD_NAME,
                METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE,
                null,
                null
        );
        m_cw.visitField(ACC_PRIVATE + ACC_STATIC, META_DATA_FIELD_NAME, MAP_CLASS_SIGNATURE, null, null);
        m_cw.visitField(
                ACC_PRIVATE + ACC_STATIC,
                OPTIMIZED_JOIN_POINT_INSTANCE_FIELD_NAME,
                L + m_joinPointClassName + SEMICOLON,
                null, null
        );
        m_cw.visitField(ACC_PRIVATE, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature, null, null);
        m_cw.visitField(ACC_PRIVATE, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature, null, null);
        m_cw.visitField(ACC_PRIVATE, MethodCallJoinPointCompiler.STACK_FRAME_COUNTER_FIELD_NAME, I, null, null);
    }

    /**
     * Creates join point specific fields.
     */
    protected void createJoinPointSpecificFields() {
        String[] fieldNames = null;
        // create the method argument fields
        Type[] argumentTypes = Type.getArgumentTypes(m_calleeMemberDesc);
        fieldNames = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            Type argumentType = argumentTypes[i];
            String fieldName = ARGUMENT_FIELD + i;
            fieldNames[i] = fieldName;
            m_cw.visitField(ACC_PRIVATE, fieldName, argumentType.getDescriptor(), null, null);
        }
        m_fieldNames = fieldNames;
    }

    /**
     * Creates the signature for the join point.
     * <p/>
     * FIXME signature field should NOT be of type Signature but of the specific type (update all refs as well)
     *
     * @param cv
     */
    protected void createSignature(final CodeVisitor cv) {
        cv.visitFieldInsn(GETSTATIC, m_joinPointClassName, TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
        cv.visitLdcInsn(new Integer(m_joinPointHash));


        cv.visitMethodInsn(
                INVOKESTATIC,
                SIGNATURE_FACTORY_CLASS,
                NEW_METHOD_SIGNATURE_METHOD_NAME,
                NEW_METHOD_SIGNATURE_METHOD_SIGNATURE
        );
        cv.visitFieldInsn(
                PUTSTATIC, m_joinPointClassName, SIGNATURE_FIELD_NAME, METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
        );

    }

    /**
     * Optimized implementation that does not retrieve the parameters from the join point instance but is passed
     * directly to the method from the input parameters in the 'invoke' method. Can only be used if no around advice
     * exists.
     *
     * @param cv
     * @param argStartIndex index on stack of first target method arg (0 or 1, depends of static target or not)
     */
    protected void createInlinedJoinPointInvocation(final CodeVisitor cv, final boolean isOptimizedJoinPoint,
                                                    final int argStartIndex, final int joinPointIndex) {

        // load the target instance (arg0 else not available for static target)
        if (!Modifier.isStatic(m_calleeMemberModifiers)) {
            cv.visitVarInsn(ALOAD, 0);
        }

        String joinPointName = null; // can be prefixed

        loadArgumentMemberFields(cv, argStartIndex);

        // call the package protected wrapper method if target method is not public
        if (!Modifier.isPublic(m_calleeMemberModifiers)) {
            joinPointName = TransformationUtil.getWrapperMethodName(
                    m_calleeMemberName,
                    m_calleeMemberDesc,
                    m_calleeClassName,
                    INVOKE_WRAPPER_METHOD_PREFIX
            );
        } else {
            joinPointName = m_calleeMemberName;
        }
        // FIXME - pbly broken if we are using a wrapper method - refactor this if / else
        if (Modifier.isStatic(m_calleeMemberModifiers)) {
            cv.visitMethodInsn(INVOKESTATIC, m_calleeClassName, joinPointName, m_calleeMemberDesc);
        } else if (isInvokeInterface(m_calleeMemberModifiers)) {
            // AW-253
            cv.visitMethodInsn(INVOKEINTERFACE, m_calleeClassName, joinPointName, m_calleeMemberDesc);
        } else {
            cv.visitMethodInsn(INVOKEVIRTUAL, m_calleeClassName, joinPointName, m_calleeMemberDesc);
        }
    }

    /**
     * Creates a call to the target join point, the parameter(s) to the join point are retrieved from the invocation
     * local join point instance.
     *
     * @param cv
     */
    protected void createJoinPointInvocation(final CodeVisitor cv) {

        // load the target instance member field unless calleeMember is static
        if (!Modifier.isStatic(m_calleeMemberModifiers)) {
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
        }

        String joinPointName;
        loadArguments(cv);
        if (!Modifier.isPublic(m_calleeMemberModifiers)) {
            joinPointName = TransformationUtil.getWrapperMethodName(
                    m_calleeMemberName,
                    m_calleeMemberDesc,
                    m_calleeClassName,
                    INVOKE_WRAPPER_METHOD_PREFIX
            );
        } else {
            joinPointName = m_calleeMemberName;
        }
        // FIXME - pbly broken if we are using a wrapper method - refactor this if / else
        if (Modifier.isStatic(m_calleeMemberModifiers)) {
            cv.visitMethodInsn(INVOKESTATIC, m_calleeClassName, joinPointName, m_calleeMemberDesc);
        } else if (isInvokeInterface(m_calleeMemberModifiers)) {
            // AW-253
            cv.visitMethodInsn(INVOKEINTERFACE, m_calleeClassName, joinPointName, m_calleeMemberDesc);
        } else {
            cv.visitMethodInsn(INVOKEVIRTUAL, m_calleeClassName, joinPointName, m_calleeMemberDesc);
        }
    }

    /**
     * Returns the join points return type.
     *
     * @return
     */
    protected Type getJoinPointReturnType() {
        return Type.getReturnType(m_calleeMemberDesc);
    }

    /**
     * Returns the join points argument type(s).
     *
     * @return
     */
    protected Type[] getJoinPointArgumentTypes() {
        return Type.getArgumentTypes(m_calleeMemberDesc);
    }

    /**
     * Creates the getRtti method
     */
    protected void createGetRttiMethod() {
        CodeVisitor cv = m_cw.visitMethod(ACC_PUBLIC, GET_RTTI_METHOD_NAME, GET_RTTI_METHOD_SIGNATURE, null, null);

        // new MethodRttiImpl( .. )
        cv.visitTypeInsn(NEW, METHOD_RTTI_IMPL_CLASS_NAME);
        cv.visitInsn(DUP);
        cv.visitFieldInsn(
                GETSTATIC, m_joinPointClassName, SIGNATURE_FIELD_NAME, METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
        );
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
        cv.visitMethodInsn(
                INVOKESPECIAL, METHOD_RTTI_IMPL_CLASS_NAME, INIT_METHOD_NAME, METHOD_RTTI_IMPL_INIT_SIGNATURE
        );

        // set the arguments
        cv.visitInsn(DUP);
        createParametersArrayAt(cv, 1);
        cv.visitVarInsn(ALOAD, 1);
        cv.visitMethodInsn(
                INVOKEVIRTUAL, METHOD_RTTI_IMPL_CLASS_NAME, SET_PARAMETER_VALUES_METHOD_NAME,
                SET_PARAMETER_VALUES_METHOD_SIGNATURE
        );

        // set the Returned instance
        if (m_returnType.getSort() != Type.VOID) {
            cv.visitInsn(DUP);
            if (AsmHelper.isPrimitive(m_returnType)) {
                AsmHelper.prepareWrappingOfPrimitiveType(cv, m_returnType);
                cv.visitVarInsn(ALOAD, 0);
                cv.visitFieldInsn(
                        GETFIELD, m_joinPointClassName, RETURN_VALUE_FIELD_NAME, m_returnType.getDescriptor()
                );
                AsmHelper.wrapPrimitiveType(cv, m_returnType);
            } else {
                cv.visitVarInsn(ALOAD, 0);
                cv.visitFieldInsn(
                        GETFIELD, m_joinPointClassName, RETURN_VALUE_FIELD_NAME, m_returnType.getDescriptor()
                );
            }
            cv.visitMethodInsn(
                    INVOKEVIRTUAL, METHOD_RTTI_IMPL_CLASS_NAME, SET_RETURN_VALUE_METHOD_NAME,
                    SET_RETURN_VALUE_METHOD_SIGNATURE
            );
        }

        cv.visitInsn(ARETURN);
        cv.visitMaxs(0, 0);
    }

    /**
     * Creates the getSignature method.
     */
    protected void createGetSignatureMethod() {
        CodeVisitor cv = m_cw.visitMethod(
                ACC_PUBLIC,
                GET_SIGNATURE_METHOD_NAME,
                GET_SIGNATURE_METHOD_SIGNATURE,
                null,
                null
        );

        cv.visitFieldInsn(
                GETSTATIC, m_joinPointClassName,
                SIGNATURE_FIELD_NAME, METHOD_SIGNATURE_IMPL_CLASS_SIGNATURE
        );
        cv.visitInsn(ARETURN);
        cv.visitMaxs(0, 0);
    }

    /**
     * See AW-253
     * For INVOKE INTERFACE, we are using a custom modifier to track it down in order to avoid to do the
     * hierarchy resolution ourself here
     */
    private boolean isInvokeInterface(int modifier) {
        return (modifier & MODIFIER_INVOKEINTERFACE) != 0;
    }
}
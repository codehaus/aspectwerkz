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
import org.codehaus.aspectwerkz.transform.inlining.compiler.AbstractJoinPointCompiler;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;

import java.lang.reflect.Modifier;

/**
 * A compiler that compiles/generates a class that represents a specific join point, a class which invokes the advices
 * and the target join point statically.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur </a>
 */
public class FieldSetJoinPointCompiler extends AbstractJoinPointCompiler {

    /**
     * Creates a new join point compiler instance.
     *
     * @param model
     */
    FieldSetJoinPointCompiler(final CompilationInfo.Model model) {
        super(model);
    }

    /**
     * Creates join point specific fields.
     */
    protected void createJoinPointSpecificFields() {
        String[] fieldNames = null;
        // create the field argument field
        Type fieldType = Type.getType(m_calleeMemberDesc);
        fieldNames = new String[1];
        String fieldName = ARGUMENT_FIELD + 0;
        fieldNames[0] = fieldName;
        m_cw.visitField(ACC_PRIVATE, fieldName, fieldType.getDescriptor(), null, null);
        m_fieldNames = fieldNames;

        m_cw.visitField(
                ACC_PRIVATE + ACC_STATIC,
                SIGNATURE_FIELD_NAME,
                FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE,
                null,
                null
        );
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
                NEW_FIELD_SIGNATURE_METHOD_NAME,
                NEW_FIELD_SIGNATURE_METHOD_SIGNATURE
        );
        cv.visitFieldInsn(PUTSTATIC, m_joinPointClassName, SIGNATURE_FIELD_NAME, FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE);
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
        joinPointName = m_calleeMemberName;
        joinPointName = TransformationUtil.getWrapperMethodName(
                joinPointName,
                m_calleeMemberDesc,
                m_calleeClassName,
                PUTFIELD_WRAPPER_METHOD_PREFIX
        );
        StringBuffer sig = new StringBuffer();
        sig.append('(');
        sig.append(m_calleeMemberDesc);
        sig.append(')');
        sig.append('V');
        if (Modifier.isStatic(m_calleeMemberModifiers)) {
            cv.visitMethodInsn(INVOKESTATIC, m_calleeClassName, joinPointName, sig.toString());
        } else {
            cv.visitMethodInsn(INVOKEVIRTUAL, m_calleeClassName, joinPointName, sig.toString());
        }
        AsmHelper.addDefaultValue(cv, m_argumentTypes[0]);
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

        loadArguments(cv);
        String joinPointName = TransformationUtil.getWrapperMethodName(
                m_calleeMemberName,
                m_calleeMemberDesc,
                m_calleeClassName,
                PUTFIELD_WRAPPER_METHOD_PREFIX
        );
        StringBuffer putFieldWrapperDesc = new StringBuffer();
        putFieldWrapperDesc.append('(');
        putFieldWrapperDesc.append(m_calleeMemberDesc);
        putFieldWrapperDesc.append(')');
        putFieldWrapperDesc.append('V');
        if (Modifier.isStatic(m_calleeMemberModifiers)) {
            cv.visitMethodInsn(INVOKESTATIC, m_calleeClassName, joinPointName, putFieldWrapperDesc.toString());
        } else {
            cv.visitMethodInsn(INVOKEVIRTUAL, m_calleeClassName, joinPointName, putFieldWrapperDesc.toString());
        }
        AsmHelper.addDefaultValue(cv, m_argumentTypes[0]);
    }

    /**
     * Returns the join points return type.
     *
     * @return
     */
    protected Type getJoinPointReturnType() {
        return Type.getType(m_calleeMemberDesc);
    }

    /**
     * Returns the join points argument type(s).
     *
     * @return
     */
    protected Type[] getJoinPointArgumentTypes() {
        return new Type[]{Type.getType(m_calleeMemberDesc)};
    }

    /**
     * Creates the getRtti method
     */
    protected void createGetRttiMethod() {
        CodeVisitor cv = m_cw.visitMethod(ACC_PUBLIC, GET_RTTI_METHOD_NAME, GET_RTTI_METHOD_SIGNATURE, null, null);

        // new FieldRttiImpl( .. )
        cv.visitTypeInsn(NEW, FIELD_RTTI_IMPL_CLASS_NAME);
        cv.visitInsn(DUP);
        cv.visitFieldInsn(GETSTATIC, m_joinPointClassName, SIGNATURE_FIELD_NAME, FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
        cv.visitMethodInsn(
                INVOKESPECIAL, FIELD_RTTI_IMPL_CLASS_NAME, INIT_METHOD_NAME, FIELD_RTTI_IMPL_INIT_SIGNATURE
        );

        // set the value
        cv.visitInsn(DUP);
        if (AsmHelper.isPrimitive(m_returnType)) {
            AsmHelper.prepareWrappingOfPrimitiveType(cv, m_returnType);
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, RETURN_VALUE_FIELD_NAME, m_returnType.getDescriptor());
            AsmHelper.wrapPrimitiveType(cv, m_returnType);
        } else {
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, RETURN_VALUE_FIELD_NAME, m_returnType.getDescriptor());
        }
        cv.visitMethodInsn(
                INVOKEVIRTUAL, FIELD_RTTI_IMPL_CLASS_NAME, SET_RETURN_VALUE_METHOD_NAME,
                SET_RETURN_VALUE_METHOD_SIGNATURE
        );

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
                SIGNATURE_FIELD_NAME, FIELD_SIGNATURE_IMPL_CLASS_SIGNATURE
        );
        cv.visitInsn(ARETURN);
        cv.visitMaxs(0, 0);
    }
}
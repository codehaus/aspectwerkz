/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;

/**
 * Visitors that are not writing any bytecode and using a Null ClassVisitor / Code Visitor as a target instead.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AsmNullAdapter {

    /**
     * A NullClassAdapter that does nothing.
     * Can be used to speed up ASM and avoid unecessary bytecode writing thru a regular ClassWriter when this is not
     * needed (read only purpose).
     */
    public static class NullClassAdapter implements ClassVisitor {

        public final static ClassVisitor NULL_CLASS_ADAPTER = new NullClassAdapter();

        public void visit(int i, int i1, String s, String s1, String[] strings, String s2) {
        }

        public void visitInnerClass(String s, String s1, String s2, int i) {
        }

        public void visitField(int i, String s, String s1, Object o, Attribute attribute) {
        }

        public CodeVisitor visitMethod(int i, String s, String s1, String[] strings, Attribute attribute) {
            return NullCodeAdapter.NULL_CODE_ADAPTER;
        }

        public void visitAttribute(Attribute attribute) {
        }

        public void visitEnd() {
        }
    }

    /**
     * A NullCodeAdapter that does nothing.
     * Can be used to speed up ASM and avoid unecessary bytecode writing thru a regular CodeWriter when this is not
     * needed (read only purpose)
     */
    public static class NullCodeAdapter implements CodeVisitor {

        public final static CodeVisitor NULL_CODE_ADAPTER = new NullCodeAdapter();

        public void visitInsn(int opcode) {
        }

        public void visitIntInsn(int opcode, int operand) {
        }

        public void visitVarInsn(int opcode, int var) {
        }

        public void visitTypeInsn(int opcode, String desc) {
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        }

        public void visitJumpInsn(int opcode, Label label) {
        }

        public void visitLabel(Label label) {
        }

        public void visitLdcInsn(Object cst) {
        }

        public void visitIincInsn(int var, int increment) {
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt, Label labels[]) {
        }

        public void visitLookupSwitchInsn(Label dflt, int keys[], Label labels[]) {
        }

        public void visitMultiANewArrayInsn(String desc, int dims) {
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        }

        public void visitMaxs(int maxStack, int maxLocals) {
        }

        public void visitLocalVariable(String name, String desc, Label start, Label end, int index) {
        }

        public void visitLineNumber(int line, Label start) {
        }

        public void visitAttribute(Attribute attr) {
        }
    }
}

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
import org.objectweb.asm.Label;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.Context;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class LabelToLineNumberVisitor extends ClassAdapter {

    private ContextImpl m_ctx;

    public LabelToLineNumberVisitor(ClassVisitor cv, Context ctx) {
        super(cv);
        m_ctx = (ContextImpl)ctx;
    }

    public CodeVisitor visitMethod(int i, String s, String s1, String[] strings, Attribute attribute) {
        return new CodeAdapter(super.visitMethod(i, s, s1, strings, attribute)) {
            public void visitLineNumber(int i, Label label) {
                super.visitLineNumber(i, label);
                m_ctx.addLineNumberInfo(label, i);
            }
        };
    }
}

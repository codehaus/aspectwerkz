/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.asm;

import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @TODO: document
 */
public class CustomAttribute extends Attribute {
    private final byte[] m_bytes;

    public CustomAttribute(final byte[] bytes) {
        super(AttributeEnhancer.CUSTOM_ATTRIBUTE);
        m_bytes = bytes;
    }

    public CustomAttribute(final byte[] bytes, final Attribute next) {
        super(AttributeEnhancer.CUSTOM_ATTRIBUTE);
        m_bytes = bytes;
        this.next = next;
    }

    public byte[] getBytes() {
        return m_bytes;
    }

    protected Attribute read(
            final ClassReader cr, final int off, final int len, final char[] buf, final int codeOff,
            final Label[] labels) {
        byte[] bytes = new byte[len];
        int index = off;
        for (int i = 0; i < len; i++, index++) {
            bytes[i] = cr.b[index];
        }
        return new CustomAttribute(bytes);
    }

    protected ByteVector write(
            final ClassWriter cw, final byte[] code, final int len, final int maxStack,
            final int maxLocals) {
        return new ByteVector().putByteArray(m_bytes, 0, m_bytes.length);
    }
}

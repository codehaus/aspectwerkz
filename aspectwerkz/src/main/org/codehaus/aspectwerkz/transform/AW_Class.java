/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.io.IOException;
import java.io.ByteArrayInputStream;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;

/**
 * The AspectWerkz class concept.
 * <p/>
 * Contains informations and data about the class being transformed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AW_Class {

    /**
     * The name of the class.
     */
    private final String m_name;

    /**
     * The BCEL class gen.
     */
    private final ClassGen m_classGen;

    /**
     * Creates a new class.
     *
     * @param name the name
     * @param bytecode the byte code
     * @throws IOException
     * @throws ClassFormatException
     */
    public AW_Class(final String name, final byte[] bytecode) throws IOException, ClassFormatException {
        m_name = name;
        m_classGen = fromByte(bytecode);
    }

    /**
     * Returns the name of the class.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the BCEL class gen for the class.
     *
     * @return the class gen
     */
    public ClassGen getClassGen() {
        return m_classGen;
    }

    /**
     * Returns the byte code for the class.
     * @return
     */
    public byte[] getBytecode() {
        return m_classGen.getJavaClass().getBytes();
    }

    /**
     * Transforms byte code to a BCEL class gen.
     *
     * @param bytecode the byte code
     * @return the BCEL class gen
     * @throws IOException
     * @throws ClassFormatException
     */
    public static ClassGen fromByte(final byte[] bytecode) throws IOException, ClassFormatException {
        ClassParser parser = new ClassParser(new ByteArrayInputStream(bytecode), "<generated>");
        return new ClassGen(parser.parse());
    }
}

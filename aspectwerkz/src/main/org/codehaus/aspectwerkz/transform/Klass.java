/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
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
public class Klass {

    /**
     * The name of the class.
     */
    private final String m_name;

    /**
     * The BCEL class gen.
     */
    private final ClassGen m_classGen;

    /**
     * The BCEL initial class gen to calculate serial ver uid based on initial bytecode
     * Lazily initialized
     */
    private ClassGen m_initialClassGen;

    /**
     * The initial bytecode of the class
     */
    private final byte[] m_initialBytecode;

    /**
     * Creates a new class.
     *
     * @param name the name
     * @param bytecode the byte code
     * @throws IOException
     * @throws ClassFormatException
     */
    public Klass(final String name, final byte[] bytecode) throws IOException, ClassFormatException {
        m_name = name;
        m_classGen = fromByte(bytecode);
        m_initialBytecode = bytecode;
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
     * Returns the BCEL initial class gen for the class.
     *
     * @throws IOException
     * @return the initial class gen
     */
    public ClassGen getInitialClassGen() throws IOException {
        if (m_initialClassGen==null)
            m_initialClassGen = fromByte(m_initialBytecode);
        return m_initialClassGen;
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

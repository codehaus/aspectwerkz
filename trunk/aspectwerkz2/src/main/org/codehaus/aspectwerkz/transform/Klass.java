/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.io.IOException;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * The AspectWerkz class concept.
 * <p/>
 * Contains informations and data about the class being transformed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Klass {

    /**
     * The name of the class.
     */
    private final String m_name;

    /**
     * The Javassist class gen.
     */
    private final CtClass m_ctClass;

    /**
     * The Javassist initial class gen to calculate serial ver uid based on initial bytecode Lazily initialized
     */
    private CtClass m_initialCtClass;

    /**
     * The initial bytecode of the class
     */
    private final byte[] m_initialBytecode;

    /**
     * Creates a new class.
     *
     * @param name     the name
     * @param bytecode the byte code
     */
    public Klass(final String name, final byte[] bytecode, final ClassLoader loader) {
        m_name = name.replace('/', '.');
        m_ctClass = fromByte(name, bytecode, loader);
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
     * Returns the Javassist class gen for the class.
     *
     * @return the class gen
     */
    public CtClass getCtClass() {
        return m_ctClass;
    }

    /**
     * Returns the Javassist initial class gen for the class.
     *
     * @return the initial class gen
     * @throws IOException
     */
    public CtClass getInitialCtClass() throws IOException {
        if (m_initialCtClass == null) {
            m_initialCtClass = fromByte(m_name, m_initialBytecode, null);//TODO BREAK
        }
        return m_initialCtClass;
    }

    /**
     * Returns the byte code for the class.
     *
     * @return
     */
    public byte[] getBytecode() {
        try {
            return m_ctClass.toBytecode();
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Transforms byte code to a Javassist class gen.
     *
     * @param bytecode the byte code
     * @return the Javassist class gen
     */
    public static CtClass fromByte(final String name, final byte[] bytecode, final ClassLoader loader) {
        try {
            ClassPool cp = new ClassPool(null);
            cp.insertClassPath(new ByteArrayClassPath(name, bytecode));
            cp.appendClassPath(new LoaderClassPath(loader));
            return cp.get(name);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.delegation;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

/**
 * The AspectWerkz class concept. <p/>Contains informations and data about the class being transformed.
 * 
 * @TODO: contains javassist specific stuff, refactor out and use an abstraction
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class Klass {
    /**
     * The name of the class.
     */
    private final String m_name;

    /**
     * The classloader defining the class
     */
    private final ClassLoader m_loader;

    /**
     * The Javassist CtClass, lazyly loaded.
     */
    private CtClass m_ctClass;

    /**
     * The Javassist initial class gen to calculate serial ver uid based on initial bytecode Lazily initialized
     */
    private CtClass m_initialCtClass;

    /**
     * The initial bytecode of the class
     */
    private final byte[] m_initialBytecode;

    /**
     * The join point index.
     */
    private int m_joinPointIndex = -1;

    /**
     * Creates a new class.
     * 
     * @param name the name
     * @param bytecode the byte code
     */
    public Klass(final String name, final byte[] bytecode, final ClassLoader loader) {
        m_name = name.replace('/', '.');
        m_loader = loader;
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
        if (m_ctClass == null) {
            m_ctClass = fromByte(m_name, m_initialBytecode, m_loader);
        }
        return m_ctClass;
    }

    /**
     * Returns the Javassist initial class gen for the class.
     * 
     * @return the initial class gen
     */
    public CtClass getInitialCtClass() {
        if (m_initialCtClass == null) {
            m_initialCtClass = fromByte(m_name, m_initialBytecode, m_loader);
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
            return getCtClass().toBytecode();
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the current join point index.
     * 
     * @return
     */
    public int getJoinPointIndex() {
        if (m_joinPointIndex != -1) {
            return m_joinPointIndex;
        } else {
            m_joinPointIndex = JavassistHelper.getJoinPointIndex(getCtClass());
            return m_joinPointIndex;
        }
    }

    /**
     * Increments the join point index.
     */
    public void incrementJoinPointIndex() {
        getJoinPointIndex();
        m_joinPointIndex++;
    }

    /**
     * Flushes the index count.
     */
    public void flushJoinPointIndex() {
        if (m_joinPointIndex != -1) {
            JavassistHelper.setJoinPointIndex(getCtClass(), m_joinPointIndex);
        }
    }
}
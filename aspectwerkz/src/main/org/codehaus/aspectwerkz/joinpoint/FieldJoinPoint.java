/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.joinpoint;

import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Type;
import org.codehaus.aspectwerkz.definition.metadata.FieldMetaData;

/**
 * Matches well defined point of execution in the program where a field is set
 * or accessed. Stores meta data from the join point. I.e. a reference to
 * original object and method, name and type of the field etc. Handles the
 * invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bon�r</a>
 * @version $Id: FieldJoinPoint.java,v 1.1.1.1 2003-05-11 15:14:25 jboner Exp $
 */
public abstract class FieldJoinPoint implements JoinPoint {

    /**
     * The serial version uid for the class.
     */
    private static final long serialVersionUID = 6188620044093181600L;

    /**
     * The signature for the field.
     */
    protected final String m_signature;

    /**
     * The name of the field.
     */
    protected final String m_fieldName;

    /**
     * The type of the field.
     */
    protected final Type m_fieldType;

    /**
     * The name of the type of the field.
     */
    protected final String m_typeName;

    /**
     * The pre advices applied to the join point.
     */
    protected int[] m_preAdvices = new int[0];

    /**
     * The post advices applied to the join point.
     */
    protected int[] m_postAdvices = new int[0];

    /**
     * Marks the join point as initialized.
     */
    protected boolean m_initialized = false;

    /**
     * Meta-data for the field.
     */
    protected FieldMetaData m_metadata;

    /**
     * Creates a new MemberFieldGetJoinPoint object.
     *
     * @param signature the field signature
     */
    public FieldJoinPoint(final String signature) {
        if (signature == null) throw new IllegalArgumentException("signature can not be null");

        AspectWerkz.initialize();

        m_signature = signature;

        final StringTokenizer tokenizer = new StringTokenizer(signature, " ");
        m_typeName = tokenizer.nextToken();
        m_fieldName = tokenizer.nextToken();

        if (m_signature.startsWith("long")) {
            m_fieldType = Type.LONG;
        }
        else if (m_signature.startsWith("int")) {
            m_fieldType = Type.INT;
        }
        else if (m_signature.startsWith("short")) {
            m_fieldType = Type.SHORT;
        }
        else if (m_signature.startsWith("double")) {
            m_fieldType = Type.DOUBLE;
        }
        else if (m_signature.startsWith("float")) {
            m_fieldType = Type.FLOAT;
        }
        else if (m_signature.startsWith("byte")) {
            m_fieldType = Type.BYTE;
        }
        else if (m_signature.startsWith("boolean")) {
            m_fieldType = Type.BOOLEAN;
        }
        else if (m_signature.startsWith("char")) {
            m_fieldType = Type.CHAR;
        }
        else {
            m_fieldType = Type.OBJECT;
        }

        createMetaData();
    }

    /**
     * Does not do anything.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceedInNewThread() throws Throwable {
        return null;
    }

    /**
     * Does not do anything.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
        return null;
    }

    /**
     * Loads the advices for this pointcut.
     * To be over-ridden.
     */
    protected abstract void loadAdvices();

    /**
     * Invokes the next pre advice in the chain until it reaches the end.
     */
    public void pre() throws Throwable {
        if (!m_initialized) {
            loadAdvices();
        }
        for (int i = 0, j = m_preAdvices.length; i < j; i++) {
            try {
                AspectWerkz.getAdvice(m_preAdvices[i]).doExecute(this);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(
                        createAdvicesNotCorrectlyMappedMessage());
            }
        }
    }

    /**
     * Invokes the next post advice in the chain until it reaches the end.
     */
    public void post() throws Throwable {
        if (!m_initialized) {
            loadAdvices();
        }
        for (int i = m_postAdvices.length - 1; i >= 0; i--) {
            try {
                AspectWerkz.getAdvice(m_postAdvices[i]).doExecute(this);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(
                        createAdvicesNotCorrectlyMappedMessage());
            }
        }
    }

    /**
     * Returns the target object.
     *
     * @return the target object
     */
    public abstract Object getTargetObject();

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    public abstract Class getTargetClass();

    /**
     * Returns the field type as a Type object.
     *
     * @return the field type constant
     * @see aspectwerkz.Type
     */
    public Type getFieldType() {
        return m_fieldType;
    }

    /**
     * Returns the name of the field type.
     *
     * @return the name of the field type
     */
    public String getFieldTypeName() {
        return m_typeName;
    }

    /**
     * Returns the signature of the field.
     *
     * @return the signature
     */
    public String getSignature() {
        return m_signature;
    }

    /**
     * Returns the field name.
     *
     * @return the field name
     */
    public String getFieldName() {
        return m_fieldName;
    }

    /**
     * Creates a meta-data for the field for this joinpoint.
     */
    public void createMetaData() {
        m_metadata = new FieldMetaData();
        m_metadata.setName(m_fieldName);
        m_metadata.setType(m_typeName);
    }

    /**
     * Creates an advices not correctly mapped message.
     *
     * @return the message
     */
    protected String createAdvicesNotCorrectlyMappedMessage() {
        StringBuffer cause = new StringBuffer();
        cause.append("advices for ");
        cause.append(getTargetClass().getName());
        cause.append("#");
        cause.append(getFieldName());
        cause.append(" are not correctly mapped");
        return cause.toString();
    }
}

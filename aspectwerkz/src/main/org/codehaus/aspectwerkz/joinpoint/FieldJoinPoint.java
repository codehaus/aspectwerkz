/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
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
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Type;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;

/**
 * Matches well defined point of execution in the program where a field is set
 * or accessed. Stores meta data from the join point. I.e. a reference to
 * original object and method, name and type of the field etc. Handles the
 * invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: FieldJoinPoint.java,v 1.6 2003-06-26 19:27:17 jboner Exp $
 */
public abstract class FieldJoinPoint implements JoinPoint {

    /**
     * The AspectWerkz system for this join point.
     */
    protected transient AspectWerkz m_system;

    /**
     * The serial version uid for the class.
     * @todo recalculate
     */
    private static final long serialVersionUID = 6188620044093181600L;

    /**
     * The signature for the field.
     */
    protected String m_signature;

    /**
     * The name of the field.
     */
    protected String m_fieldName;

    /**
     * The type of the field.
     */
    protected Type m_fieldType;

    /**
     * The name of the type of the field.
     */
    protected String m_typeName;

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
     * The UUID for the AspectWerkz system to use.
     */
    protected String m_uuid;

    /**
     * Creates a new MemberFieldGetJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param signature the field signature
     */
    public FieldJoinPoint(final String uuid, final String signature) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (signature == null) throw new IllegalArgumentException("signature can not be null");

        m_system = AspectWerkz.getSystem(uuid);
        m_system.initialize();

        m_uuid = uuid;
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
                m_system.getAdvice(m_preAdvices[i]).doExecute(this);
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
                m_system.getAdvice(m_postAdvices[i]).doExecute(this);
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
        m_metadata = ReflectionMetaDataMaker.
                createFieldMetaData(m_fieldName, m_typeName);
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

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();

        m_uuid = (String)fields.get("m_uuid", null);
        m_signature = (String)fields.get("m_signature", null);
        m_typeName = (String)fields.get("m_typeName", null);
        m_fieldName = (String)fields.get("m_fieldName", null);
        m_fieldType = (Type)fields.get("m_fieldType", null);

        m_preAdvices = (int[])fields.get("m_preAdvices", null);
        m_postAdvices = (int[])fields.get("m_postAdvices", null);
        m_metadata = (FieldMetaData)fields.get("m_metadata", null);
        m_initialized = fields.get("m_initialized", false);

        m_system = AspectWerkz.getSystem(m_uuid);
        m_system.initialize();
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

import org.codehaus.aspectwerkz.attribdef.definition.attribute.Attribute;

import java.io.ObjectInputStream;

/**
 * Abstract base class for the joint point implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractJoinPoint implements JoinPoint {

    /**
     * The runtime attribute for the join point.
     */
    protected Attribute m_attribute;

    /**
     * Sets the attribute for the join point.
     *
     * @param attribute the attribute
     */
//    public void setAttribute(final Attribute attribute) {
//        m_attribute = attribute;
//    }

    /**
     * Returns the attribute for the join point.
     * <p/>Loads the attribute lazily upon request.
     *
     * @return the attribute
     */
    public Attribute getAttribute() {
        return m_attribute;
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_attribute = (Attribute)fields.get("m_attribute", null);
    }
}

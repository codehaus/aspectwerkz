/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Untyped annotation proxy.
 * <p/>
 * To be used with JavDoc-style, pure string based, one value only type of annotations
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class UntypedAnnotationProxy implements Annotation, Serializable {
    /**
     * The full value of the annotation.
     */
    protected String m_value = "";

    /**
     * The name of the annotation.
     */
    protected String m_name;

    /**
     * Returns the value.
     *
     * @return the value
     */
    public String getValue() {
        return m_value;
    }

    /**
     * Returns the name.
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the annotation, the '@[name]'.
     *
     * @param name
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Sets the full value of the annotation (including possible named parameters etc.).
     *
     * @param value
     */
    public void setValue(final String value) {
        m_value = value;
    }

    /**
     * Checks if the annotation is typed or not.
     *
     * @return boolean
     */
    public boolean isTyped() {
        return false;
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_value = (String)fields.get("m_value", null);
        m_name = (String)fields.get("m_name", null);
    }
}

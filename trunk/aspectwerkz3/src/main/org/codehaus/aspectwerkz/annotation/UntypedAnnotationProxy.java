/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.annotation.TypedAnnotationProxyBase;
import org.apache.xmlbeans.impl.jam.internal.elements.AnnotationValueImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.codehaus.aspectwerkz.util.Strings;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Untyped annotation proxy.
 * <p/>
 * To be used with JavDoc-style, pure string based, one value only type of annotations
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class UntypedAnnotationProxy extends TypedAnnotationProxyBase implements Annotation, Serializable {
    private String m_value = "";
    private transient JAnnotationValue[] m_singleValueArray = null;

    public String value() {
        return m_value;
    }

    public void setvalue(String value) {
        m_value = Strings.removeFormattingCharacters(value);
    }

    public JAnnotationValue[] getValues() {
        if (m_singleValueArray == null) {
            ElementContext elementContext = (ElementContext)mContext;
            JClass jClassType = elementContext.getClassLoader().loadClass("java.lang.String");
            m_singleValueArray = new JAnnotationValue[] {
                                     new AnnotationValueImpl(elementContext, "value", m_value, jClassType)
                                 };
        }
        return m_singleValueArray;
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
        m_singleValueArray = null;
    }
}

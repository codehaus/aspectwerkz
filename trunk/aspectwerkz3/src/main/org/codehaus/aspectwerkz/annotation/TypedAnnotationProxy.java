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
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.internal.elements.AnnotationValueImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.codehaus.aspectwerkz.annotation.expression.AnnotationVisitor;
import org.codehaus.aspectwerkz.annotation.expression.ast.AnnotationParser;
import org.codehaus.aspectwerkz.annotation.expression.ast.ParseException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class TypedAnnotationProxy extends AnnotationProxy implements Annotation, Serializable {
    /**
     * The one and only annotation parser.
     */
    private static AnnotationParser s_parser = new AnnotationParser(System.in);

    /**
     * @TODO: do we need a readObject() method that builds up this list after unmarshalling?
     */
    protected transient List m_values = null;

    public JAnnotationValue[] getValues() {
        if (m_values == null) {
            return new JAnnotationValue[0];
        }
        JAnnotationValue[] out = new JAnnotationValue[m_values.size()];
        m_values.toArray(out);
        return out;
    }

    public void setValue(String name, Object value, JClass type) {
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value can not be null");
        }
        String annotation = (String)value;
        try {
            AnnotationVisitor.parse(this, s_parser.parse(annotation));
        } catch (ParseException e) {
            System.err.println("could not parse annotation: " + annotation);
        }
    }

    /**
     * @TODO: needed to be called from visitor?
     */
    private void addTypedAnnotationValue(Class valueType, String name, Object value) {
        // hang onto it in case they ask for it later with getValues
        if (m_values == null) {
            m_values = new ArrayList();
        }
        ElementContext elementContext = (ElementContext)mContext;
        JClass jClassType = elementContext.getClassLoader().loadClass(valueType.getName());
        m_values.add(new AnnotationValueImpl(elementContext, name, value, jClassType));
    }
}

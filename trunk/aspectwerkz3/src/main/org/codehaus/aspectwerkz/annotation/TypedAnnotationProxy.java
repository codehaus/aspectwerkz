/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.annotation.expression.AnnotationVisitor;
import org.codehaus.aspectwerkz.annotation.expression.ast.AnnotationParser;
import org.codehaus.aspectwerkz.annotation.expression.ast.ParseException;

import java.io.Serializable;

/**
 * The base class for the typed annotation proxies.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class TypedAnnotationProxy implements Annotation, Serializable {
    /**
     * The one and only annotation parser.
     */
    protected static AnnotationParser s_parser = new AnnotationParser(System.in);

    /**
     * The name of the annotation.
     */
    protected String m_name;

    /**
     * Returns the name.
     *
     * @return the name
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
        if (value == null) {
            throw new IllegalArgumentException("value can not be null");
        }
        System.out.println("value: " + value);
        try {
            AnnotationVisitor.parse(this, s_parser.parse(value));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("could not parse annotation [" + m_name + " " + value + "]");
        }
    }

    /**
     * Checks if the annotation is typed or not.
     *
     * @return boolean
     */
    public boolean isTyped() {
        return true;
    }
}

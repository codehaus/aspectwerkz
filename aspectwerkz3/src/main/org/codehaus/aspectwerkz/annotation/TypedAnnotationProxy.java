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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public abstract class TypedAnnotationProxy implements Annotation, Serializable {
    /**
     * The one and only annotation parser.
     */
    protected static final AnnotationParser s_parser = new AnnotationParser(System.in);

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
     * Sets the full value of the annotation (including possible named parameters etc.)
     * as @Foo(x=3 ...)
     */
    public void initialize(final String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        StringBuffer representation = new StringBuffer("@");
        representation.append(name).append('(');
        if (value!=null) {
            representation.append(value);
        }
        representation.append(')');

        try {
            AnnotationVisitor.parse(this, s_parser.parse(representation.toString()));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("could not parse annotation [" + m_name + " " + representation.toString() + "]");
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
/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

/**
 * A custom annotation-like to host AnnotationDefault attribute that host annotation defaulted values
 *
 * Note: Java 5 does not handles this as an annotation but as an attribute so this information
 * will be visible in ASMClassInfo as an annotation but it is not a real one (fe won't extend Java 5 Annotation etc)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public interface AnnotationDefault {

    public final static String NAME = AnnotationDefault.class.getName().replace('/', '.');

    /**
     * The default value of the annotation element marked with the AnnotationDefault attribute
     * Note: for Class it will be an instance of asm.Type
     *
     * @return
     */
    public Object value();

    /**
     * Annotation implementation, since we will not use our Java 5 dynamic proxy based architecture for it
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    static class AnnotationDefaultImpl implements AnnotationDefault, Annotation {
        private final Object m_value;

        public AnnotationDefaultImpl(Object value) {
            m_value = value;
        }

        public Object value() {
            return m_value;
        }

        public Class annotationType() {
            return AnnotationDefault.class;
        }
    }
}

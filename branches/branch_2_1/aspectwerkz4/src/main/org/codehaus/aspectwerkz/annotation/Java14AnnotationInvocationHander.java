/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.annotation.expression.AnnotationVisitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A Java 1.3 / 1.4 strongly typed Annotation handler.
 * This proxy handler gets serialized alongside the annotationInfo within the AnnotationC compiled class.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Java14AnnotationInvocationHander implements InvocationHandler, Serializable {

    //TODO calculate
    private static final long serialVersionUID = 1L;

    private String m_annotationClassName;
    private String m_rawAnnotationName;//nickname f.e. @Before in 1.4
    private String m_rawAnnotationValue;
    private final boolean m_isUntyped;
    private final Map m_elements = new HashMap();

    /**
     * Constructor that will trigger the parsing if required
     *
     * @param annotationInterface
     * @param rawAnnotationName
     * @param rawAnnotationValue
     */
    public Java14AnnotationInvocationHander(Class annotationInterface, String rawAnnotationName,
                                            String rawAnnotationValue) {
        m_annotationClassName = annotationInterface.getName().replace('/', '.');
        m_rawAnnotationName = rawAnnotationName;
        m_rawAnnotationValue = rawAnnotationValue;

        // untyped
        if (annotationInterface.getName().equals(UntypedAnnotation.class.getName())) {
            m_isUntyped = true;
        } else {
            m_isUntyped = false;
        }

        // for @AfterReturning etc, we allow anonymous style but are using typed annotation
        // hence the @Around pc is a non supported syntax (should be @Around "pc")
        // but for compatibility purpose we fake it here.
        if (m_annotationClassName.equals("org.codehaus.aspectwerkz.annotation.AfterReturning")
            || m_annotationClassName.equals("org.codehaus.aspectwerkz.annotation.AfterThrowing")
            || m_annotationClassName.startsWith("org.codehaus.aspectwerkz.annotation.")
               && !m_annotationClassName.equals("org.codehaus.aspectwerkz.annotation.UntypedAnnotation")) {
            String trimed = m_rawAnnotationValue.trim();
            if (trimed.startsWith("type")
                || trimed.startsWith("pointcut")
                || trimed.startsWith("deploymentModel")) {
                ;// not using untyped syntax
            } else {
                if (m_rawAnnotationValue.startsWith("\"") && m_rawAnnotationValue.endsWith("\"")) {
                    ;
                } else {
                    m_rawAnnotationValue = "\"" + m_rawAnnotationValue + "\"";
                }
            }
        }

        // parse the raw representation for typed annotation
        if (!m_isUntyped) {
            StringBuffer representation = new StringBuffer("@");
            representation.append(m_annotationClassName).append('(');
            if (m_rawAnnotationValue != null) {
                // @Aspect perJVM is allowed, while should be @Aspect "perJVM"
                // for now patch it here...
                // FIXME
                if (m_annotationClassName.equals("org.codehaus.aspectwerkz.annotation.Aspect")) {
                    if (m_rawAnnotationValue.indexOf("name") < 0) {
                        representation.append(m_rawAnnotationValue);
                    }
                } else {
                    representation.append(m_rawAnnotationValue);
                }
            }
            representation.append(')');
            //TODO support for LazyClass
            AnnotationVisitor.parse(m_elements, representation.toString(), annotationInterface);
        }
    }

    /**
     * Raw constructor that assumes an already analysed annotation instance
     * Used for nested annotation
     *
     * @param annotationInterface
     * @param elements
     */
    public Java14AnnotationInvocationHander(Class annotationInterface, Map elements) {
        m_annotationClassName = annotationInterface.getName().replace('/', '.');
        m_rawAnnotationName = m_annotationClassName;
        m_isUntyped = false;
        m_rawAnnotationValue = null;

        m_elements.putAll(elements);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Object returned = null;
        if ("toString".equals(methodName)) {
            StringBuffer sb = new StringBuffer();
            sb.append('@').append(m_rawAnnotationName);
            sb.append("(");
            String sep = "";
            for (Iterator iterator = m_elements.keySet().iterator(); iterator.hasNext();) {
                String elementName = (String) iterator.next();
                AnnotationElement element = (AnnotationElement) m_elements.get(elementName);
                sb.append(sep).append(element.name + "=" + element.toString());
                sep = ", ";
            }
            sb.append(")");
            returned = sb.toString();
        } else if ("annotationType".equals(methodName)) {
            return Class.forName(m_annotationClassName, false, proxy.getClass().getClassLoader());
        } else if (m_isUntyped) {
            if ("value".equals(methodName)) {
                returned = m_rawAnnotationValue;
            } else if ("name".equals(methodName)) {
                returned = m_rawAnnotationName;
            } else if ("annotationType".equals(methodName)) {
                returned = Class.forName(m_annotationClassName, false, proxy.getClass().getClassLoader());
            } else {
                throw new RuntimeException(
                        "No such element on Annotation @" + m_annotationClassName + " : " + methodName
                );
            }
        } else if (m_elements.containsKey(methodName)) {
            AnnotationElement element = (AnnotationElement) m_elements.get(methodName);
            Object valueHolder = element.resolveValueHolderFrom(proxy.getClass().getClassLoader());
            returned = valueHolder;
        } else {
            returned = null;
        }

        //handle default value for primitive types
        if (returned == null && method.getReturnType().isPrimitive()) {
            Class returnedTyped = method.getReturnType();
            if (boolean.class.equals(returnedTyped)) {
                return Boolean.FALSE;
            } else {
                short s0 = 0;
                return new Short(s0);
            }
        } else {
            return returned;
        }
    }

//        private void readObject(final ObjectInputStream stream) throws Exception {
//            ObjectInputStream.GetField fields = stream.readFields();
//            m_value = (String) fields.get("m_value", null);
//            m_name = (String) fields.get("m_name", null);
//        }

}


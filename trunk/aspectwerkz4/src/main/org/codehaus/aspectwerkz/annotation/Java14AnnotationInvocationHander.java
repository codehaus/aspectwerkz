/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.annotation.expression.AnnotationVisitor;
import org.codehaus.aspectwerkz.annotation.expression.ast.ParseException;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAnnotationHelper;

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
class Java14AnnotationInvocationHander implements InvocationHandler, Serializable {

    //TODO calculate
    private static final long serialVersionUID = 1L;
    
    private String m_annotationTypeName;
    private String m_rawAnnotationName;//nickname f.e. @Before in 1.4
    private String m_rawAnnotationValue;
    private final boolean m_isUntyped;
    private final Map m_elements = new HashMap();

    public Java14AnnotationInvocationHander(Class annotationInterface, String rawAnnotationName, String rawAnnotationValue) {
        m_annotationTypeName = annotationInterface.getName().replace('/', '.');
        m_rawAnnotationName = rawAnnotationName;
        m_rawAnnotationValue = rawAnnotationValue;

        // untyped only if value() is the SOLE method
        Method[] methods = annotationInterface.getDeclaredMethods();
        if (methods.length == 1 && methods[0].getName().equals("value")) {
            m_isUntyped = true;
        } else {
            m_isUntyped = false;
        }

        // for @AfterReturning and @AfterThrowing, we allow untyped style as well but this
        // is not easy to determine
        if (m_annotationTypeName.equals("org.codehaus.aspectwerkz.annotation.AfterReturning")
            || m_annotationTypeName.equals("org.codehaus.aspectwerkz.annotation.AfterThrowing")) {
            String trimed = m_rawAnnotationValue.trim();
            if (trimed.startsWith("type") || trimed.startsWith("expression")) {
                ;// not using untyped syntax
            } else {
                m_rawAnnotationValue = "\"" + m_rawAnnotationValue + "\"";
            }
        }

        // parse the raw representation for typed annotation
        if (!m_isUntyped) {
            StringBuffer representation = new StringBuffer("@");
            representation.append(m_annotationTypeName).append('(');
            if (m_rawAnnotationValue != null) {
                // @Aspect perJVM is allowed, while should be @Aspect "perJVM"
                // for now patch it here...
                // FIXME
                if (m_annotationTypeName.equals("org.codehaus.aspectwerkz.annotation.Aspect")) {
                    if (m_rawAnnotationValue.indexOf("name") < 0) {
                        representation.append("\"");
                        representation.append(m_rawAnnotationValue);
                        representation.append("\"");
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

    public Object invoke(Object proxy, Method method, Object[] args) {
        //TODO support for LazyClass
        String methodName = method.getName();
        if ("toString".equals(methodName)) {
            //TODO implement toString as per JSR-175 spec
            StringBuffer sb = new StringBuffer(m_rawAnnotationName);
            sb.append("[");
            String sep = "";
            for (Iterator iterator = m_elements.keySet().iterator(); iterator.hasNext();) {
                Object elementName = iterator.next();
                sb.append(sep).append(elementName + "=" + m_elements.get(elementName).toString());
                sep = "; ";
            }
            sb.append("]");
            return sb.toString();
        } else if (m_isUntyped) {
            if ("value".equals(methodName)) {
                return m_rawAnnotationValue;
            } else {
                throw new RuntimeException("No such annotation element [" + method.getName() + "] on @" + m_annotationTypeName);
            }
        } else if (m_elements.containsKey(methodName)) {
            return m_elements.get(methodName);
        } else if (/*isExtended AND*/methodName.startsWith("set")) {
            char[] elementName = new char[methodName.length() - 3];
            methodName.getChars(3, methodName.length(), elementName, 0);
            elementName[0] = new String(elementName, 0, 1).toLowerCase().charAt(0);
            m_elements.put(new String(elementName), args[0]);
            return null;
        } else {
            return null;
        }
    }

//        private void readObject(final ObjectInputStream stream) throws Exception {
//            ObjectInputStream.GetField fields = stream.readFields();
//            m_value = (String) fields.get("m_value", null);
//            m_name = (String) fields.get("m_name", null);
//        }

}


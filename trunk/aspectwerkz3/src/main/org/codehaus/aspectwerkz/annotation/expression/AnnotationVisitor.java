/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.expression;

import org.codehaus.aspectwerkz.annotation.TypedAnnotationProxy;
import org.codehaus.aspectwerkz.annotation.expression.ast.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationVisitor implements AnnotationParserVisitor {
    protected ASTRoot m_root;
    protected TypedAnnotationProxy m_annotationProxy;

    /**
     * Creates a new visitor.
     *
     * @param root the AST root
     */
    public AnnotationVisitor(final ASTRoot root, final TypedAnnotationProxy annotationProxy) {
        m_root = root;
        m_annotationProxy = annotationProxy;
    }

    public static void parse(final TypedAnnotationProxy annotation, final ASTRoot root) {
        new AnnotationVisitor(root, annotation).visit(root, annotation);
    }

    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTRoot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTAnnotation node, Object data) {
        int nr = node.jjtGetNumChildren();
        for (int i = 0; i < nr; i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return null;
    }

    public Object visit(ASTKeyValuePair node, Object data) {
        String valueName = node.getKey();
        MethodInfo methodInfo = getMethodInfo(valueName);
        Object typedValue = node.jjtGetChild(0).jjtAccept(this, data);

        //m_annotationProxy.addTypedAnnotationValue(typedValue.getClass(), valueName, stringValue); // TODO: is this method call needed for JAM?
        invokeSetterMethod(methodInfo, typedValue, valueName);
        return null;
    }

    public Object visit(ASTArray node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTIdentifier node, Object data) {
        String identifier = node.getValue();
        Object value = null;
        if (isJavaReferenceType(identifier)) {
            int index = identifier.lastIndexOf('.');
            String className = identifier.substring(0, index);
            String fieldName = identifier.substring(index + 1, identifier.length());
            try {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                Field field = clazz.getDeclaredField(fieldName);
                value = field.get(null);
            } catch (Exception e) {
                throw new RuntimeException("could not access reference field [" + identifier + "] due to: "
                                           + e.toString());
            }
        }
        return value;
    }

    public Object visit(ASTBoolean node, Object data) {
        return Boolean.valueOf(node.getValue());
    }

    public Object visit(ASTChar node, Object data) {
        return new Character(node.getValue().charAt(0));
    }

    public Object visit(ASTString node, Object data) {
        return node.getValue();
    }

    public Object visit(ASTInteger node, Object data) {
        return new Integer(node.getValue());
    }

    public Object visit(ASTFloat node, Object data) {
        return new Float(node.getValue());
    }

    public Object visit(ASTHex node, Object data) {
        return new Integer(node.getValue());
    }

    public Object visit(ASTOct node, Object data) {
        throw new UnsupportedOperationException("octal numbers not yet supported");
    }

    private MethodInfo getMethodInfo(final String valueName) {
        MethodInfo methodInfo = new MethodInfo();
        try {
            Class clazz = m_annotationProxy.getClass();
            Method[] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method getterMethod = methods[i];
                if (getterMethod.getName().equals(valueName)) {
                    Class valueType = getterMethod.getReturnType();
                    Method setterMethod = clazz.getMethod("set" + valueName, new Class[] { valueType });
                    methodInfo.getterMethod = getterMethod;
                    methodInfo.setterMethod = setterMethod;
                    methodInfo.valueType = valueType;
                    break;
                }
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("could not find setter method for value [" + valueName + "] due to: "
                                       + e.toString());
        }
        if (methodInfo.setterMethod == null) {
            throw new RuntimeException("setter method with the name [set" + valueName
                                       + "] can not be found in annotation proxy ["
                                       + m_annotationProxy.getClass().getName() + "]");
        }
        return methodInfo;
    }

    private void invokeSetterMethod(final MethodInfo methodInfo, final Object typedValue, final String valueName) {
        try {
            methodInfo.setterMethod.invoke(m_annotationProxy, new Object[] { typedValue });
        } catch (Exception e) {
            throw new RuntimeException("could not invoke setter method for named value [" + valueName + "] due to: "
                                       + e.toString());
        }
    }

    private boolean isJavaReferenceType(final String valueAsString) {
        int first = valueAsString.indexOf('.');
        int last = valueAsString.lastIndexOf('.');
        int comma = valueAsString.indexOf(',');
        if ((first > 0) && (last > 0) && (first != last) && (comma < 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Holds the setter, getter methods and the value type.
     */
    private static class MethodInfo {
        public Method setterMethod;
        public Method getterMethod;
        public Class valueType;
    }
}

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
        Object typedValue = node.jjtGetChild(0).jjtAccept(this, methodInfo);

        //m_annotationProxy.addTypedAnnotationValue(typedValue.getClass(), valueName, stringValue); // TODO: is this method call needed for JAM?
        invokeSetterMethod(methodInfo, typedValue, valueName);
        return null;
    }

    public Object visit(ASTArray node, Object data) {
        MethodInfo methodInfo = (MethodInfo)data;
        Class valueType = methodInfo.valueType;
        if (!valueType.isArray()) {
            throw new RuntimeException("parameter type to setter method [" + methodInfo.setterMethod.getName()
                                       + "] is not of type array");
        }
        Class componentType = valueType.getComponentType();
        System.out.println("componentType = " + componentType);
        if (componentType.isArray()) {
            throw new UnsupportedOperationException("multidimensional arrays are not supported, required for for setter method ["
                                                    + methodInfo.setterMethod.getName() + "]");
        }
        return createTypedArray(node, data, node.jjtGetNumChildren(), componentType);
    }

    public Object visit(ASTIdentifier node, Object data) {
        String identifier = node.getValue();
        if (identifier.endsWith(".class")) {
            return handleClassIdentifier(identifier);
        } else if (isJavaReferenceType(identifier)) {
            return handleReferenceIdentifier(identifier);
        } else {
            throw new RuntimeException("unsupported format for java type or reference [" + identifier + "]");
        }
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
        String value = node.getValue();
        char lastChar = value.charAt(value.length() - 1);
        if ((lastChar == 'L') || (lastChar == 'l')) {
            return new Long(value.substring(0, value.length() - 1));
        } else if (value.length() > 9) {
            return new Long(value);
        } else {
            return new Integer(value);
        }
    }

    public Object visit(ASTFloat node, Object data) {
        String value = node.getValue();
        char lastChar = value.charAt(value.length() - 1);
        if ((lastChar == 'D') || (lastChar == 'd')) {
            return new Double(value.substring(0, value.length() - 1));
        } else if ((lastChar == 'F') || (lastChar == 'f')) {
            return new Float(value.substring(0, value.length() - 1));
        } else {
            return new Double(value);
        }
    }

    public Object visit(ASTHex node, Object data) {
        throw new UnsupportedOperationException("hex numbers not yet supported");
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

    private Object createTypedArray(final ASTArray node, final Object data, final int nrOfElements,
                                    final Class componentType) {
        if (componentType.equals(String.class)) {
            String[] array = new String[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                String value = (String)node.jjtGetChild(i).jjtAccept(this, data);
                if ((value.charAt(0) == '"') && (value.charAt(value.length() - 1) == '"')) {
                    array[i] = new String(value.substring(1, value.length() - 1));
                } else {
                    throw new RuntimeException("badly formatted string [" + value + "]");
                }
            }
            return array;
        } else if (componentType.equals(long.class)) {
            long[] array = new long[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Long)node.jjtGetChild(i).jjtAccept(this, data)).longValue();
            }
            return array;
        } else if (componentType.equals(int.class)) {
            int[] array = new int[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Integer)node.jjtGetChild(i).jjtAccept(this, data)).intValue();
            }
            return array;
        } else if (componentType.equals(short.class)) {
            short[] array = new short[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Short)node.jjtGetChild(i).jjtAccept(this, data)).shortValue();
            }
            return array;
        } else if (componentType.equals(double.class)) {
            double[] array = new double[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Double)node.jjtGetChild(i).jjtAccept(this, data)).doubleValue();
            }
            return array;
        } else if (componentType.equals(float.class)) {
            float[] array = new float[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Float)node.jjtGetChild(i).jjtAccept(this, data)).floatValue();
            }
            return array;
        } else if (componentType.equals(byte.class)) {
            byte[] array = new byte[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Byte)node.jjtGetChild(i).jjtAccept(this, data)).byteValue();
            }
            return array;
        } else if (componentType.equals(char.class)) {
            char[] array = new char[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Character)node.jjtGetChild(i).jjtAccept(this, data)).charValue();
            }
            return array;
        } else if (componentType.equals(boolean.class)) {
            boolean[] array = new boolean[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Boolean)node.jjtGetChild(i).jjtAccept(this, data)).booleanValue();
            }
            return array;
        } else if (componentType.equals(Class.class)) {
            Class[] array = new Class[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = (Class)node.jjtGetChild(i).jjtAccept(this, data);
            }
            return array;
        } else { // reference type 
            Object[] array = new Object[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = node.jjtGetChild(i).jjtAccept(this, data);
            }
            return array;
        }
    }

    /**
     * @TODO: handle array types
     */
    private Object handleClassIdentifier(String identifier) {
        int index = identifier.lastIndexOf('.');
        String className = identifier.substring(0, index);
        if (className.endsWith("[]")) {
            throw new UnsupportedOperationException("does currently not support array types [" + identifier + "]");
        }
        if (className.equals("long")) {
            return long.class;
        } else if (className.equals("int")) {
            return int.class;
        } else if (className.equals("short")) {
            return short.class;
        } else if (className.equals("double")) {
            return double.class;
        } else if (className.equals("float")) {
            return float.class;
        } else if (className.equals("byte")) {
            return byte.class;
        } else if (className.equals("char")) {
            return char.class;
        } else if (className.equals("boolean")) {
            return boolean.class;
        } else {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (Exception e) {
                throw new RuntimeException("could not load class [" + className + "] due to: " + e.toString());
            }
        }
    }

    private Object handleReferenceIdentifier(String identifier) {
        int index = identifier.lastIndexOf('.');
        String className = identifier.substring(0, index);
        String fieldName = identifier.substring(index + 1, identifier.length());
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Field field = clazz.getDeclaredField(fieldName);
            return field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("could not access reference field [" + identifier + "] due to: " + e.toString());
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

/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.expression;

import org.codehaus.aspectwerkz.annotation.expression.ast.ASTAnnotation;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTArray;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTBoolean;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTChar;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTFloat;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTHex;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTIdentifier;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTInteger;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTKeyValuePair;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTOct;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.annotation.expression.ast.ASTString;
import org.codehaus.aspectwerkz.annotation.expression.ast.AnnotationParserVisitor;
import org.codehaus.aspectwerkz.annotation.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.annotation.expression.ast.AnnotationParser;
import org.codehaus.aspectwerkz.annotation.expression.ast.ParseException;
import org.codehaus.aspectwerkz.annotation.Java5AnnotationInvocationHandler;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Parse a source-like annotation representation to feed a map of AnnotationElement which
 * contain holder to actual values. Class and type referenced are holded behind lazy
 * wrapper that won't load them unless used.
 * <p/>
 * Note that this parser will trigger class loading to ensure type consistency
 * [change to ASMClassInfo instead of reflect if embedded parsing needed]
 * <p/>
 * Note: the loader used here is the one from the annotation class and not the one from annotated element
 * That does not matter since parse time is a build time operation for now.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AnnotationVisitor implements AnnotationParserVisitor {

    /**
     * The one and only annotation parser.
     */
    protected static final AnnotationParser PARSER = new AnnotationParser(System.in);

    protected Map m_annotationElementValueHoldersByName;

    /**
     * We reference class at parse time. We don't need to avoid reflection.
     */
    protected Class m_annotationClass;

    /**
     * Creates a new visitor.
     */
    public AnnotationVisitor(final Map annotationElementValueHoldersByName, final Class annotationClass) {
        m_annotationElementValueHoldersByName = annotationElementValueHoldersByName;
        m_annotationClass = annotationClass;
    }

    /**
     * Parse the given annotationRepresentation (@XXX(...)) to feed the given annotationElements map,
     * based on the annotationClass annotation interface.
     *
     * @param annotationElements
     * @param annotationRepresentation
     * @param annotationClass
     */
    public static void parse(final Map annotationElements, final String annotationRepresentation,
                             final Class annotationClass) {
        try {
            ASTRoot root = PARSER.parse(annotationRepresentation);
            new AnnotationVisitor(annotationElements, annotationClass).visit(root, null);
        } catch (ParseException e) {
            throw new WrappedRuntimeException("cannot parse annotation [" + annotationRepresentation + "]", e);
        }
    }

    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTRoot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTAnnotation node, Object data) {
        int nr = node.jjtGetNumChildren();
        if (nr == 1 && !(node.jjtGetChild(0) instanceof ASTKeyValuePair)) {
            // single "value" default
            Object value = node.jjtGetChild(0).jjtAccept(this, data);
            m_annotationElementValueHoldersByName.put("value",
                    new Java5AnnotationInvocationHandler.AnnotationElement("value", value));
        } else {
            for (int i = 0; i < nr; i++) {
                node.jjtGetChild(i).jjtAccept(this, data);
            }
        }
        return null;
    }

    public Object visit(ASTKeyValuePair node, Object data) {
        String valueName = node.getKey();
        //FIXME support for nested annotation when grammar supports it, should create the dynamic proxy

        // get the methodInfo for this valueName to access its type from its name
        MethodInfo valueMethod = getMethodInfo(valueName);
        Object typedValue = node.jjtGetChild(0).jjtAccept(this, valueMethod);
        m_annotationElementValueHoldersByName.put(valueName,
                new Java5AnnotationInvocationHandler.AnnotationElement(valueName, typedValue));
        return null;
    }

    public Object visit(ASTArray node, Object data) {
        MethodInfo methodInfo = (MethodInfo) data;
        Class valueType = methodInfo.valueType;
        if (!valueType.isArray()) {
            throw new RuntimeException(
                    "value type for method ["
                    + methodInfo.valueMethod.getName()
                    + "] is not of type array"
            );
        }
        Class componentType = valueType.getComponentType();
        if (componentType.isArray()) {
            throw new UnsupportedOperationException(
                    "multidimensional arrays are not supported for value type, was required method ["
                    + methodInfo.valueMethod.getName()
                    + "]"
            );
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
        // the node contains the  \" string escapes
        if (node.getValue().length() >= 2) {
            return node.getValue().substring(1, node.getValue().length() - 1);
        } else {
            return node.getValue();
        }
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

    /**
     * For a typed annotation, there should be
     * - a setter method setx or setX
     * - a getter method x or getx or getX
     *
     * @param valueName
     * @return
     */
    private MethodInfo getMethodInfo(final String valueName) {
        StringBuffer javaBeanMethodPostfix = new StringBuffer();
        javaBeanMethodPostfix.append(valueName.substring(0, 1).toUpperCase());
        if (valueName.length() > 1) {
            javaBeanMethodPostfix.append(valueName.substring(1));
        }

        MethodInfo methodInfo = new MethodInfo();
        Method[] methods = m_annotationClass.getDeclaredMethods();
        // look for value methods
        for (int i = 0; i < methods.length; i++) {
            Method valueMethod = methods[i];
            if (valueMethod.getName().equals(valueName)) {
                methodInfo.valueMethod = valueMethod;
                methodInfo.valueType = valueMethod.getReturnType();
                break;
            }
        }
        if (methodInfo.valueMethod == null) {
            throw new RuntimeException(
                    "value method for the annotation value ["
                    + valueName
                    + "] can not be found in annotation interface ["
                    + m_annotationClass.getName()
                    + "]"
            );
        }
        return methodInfo;
    }

//    private void invokeSetterMethod(final MethodInfo methodInfo, final Object typedValue, final String valueName) {
//        try {
//            methodInfo.setterMethod.invoke(
//                    m_annotationProxy, new Object[]{
//                        typedValue
//                    }
//            );
//        } catch (Exception e) {
//            throw new RuntimeException(
//                    "could not invoke setter method for named value ["
//                    + valueName
//                    + "] due to: "
//                    + e.toString()
//            );
//        }
//    }

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

    private Object createTypedArray(final ASTArray node,
                                    final Object data,
                                    final int nrOfElements,
                                    final Class componentType) {
        if (componentType.equals(String.class)) {
            String[] array = new String[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                String value = (String) node.jjtGetChild(i).jjtAccept(this, data);
                array[i] = value;
//                if ((value.charAt(0) == '"') && (value.charAt(value.length() - 1) == '"')) {
//                    array[i] = value.substring(1, value.length() - 1);
//                } else {
//                    throw new RuntimeException("badly formatted string [" + value + "]");
//                }
            }
            return array;
        } else if (componentType.equals(long.class)) {
            long[] array = new long[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Long) node.jjtGetChild(i).jjtAccept(this, data)).longValue();
            }
            return array;
        } else if (componentType.equals(int.class)) {
            int[] array = new int[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Integer) node.jjtGetChild(i).jjtAccept(this, data)).intValue();
            }
            return array;
        } else if (componentType.equals(short.class)) {
            short[] array = new short[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Short) node.jjtGetChild(i).jjtAccept(this, data)).shortValue();
            }
            return array;
        } else if (componentType.equals(double.class)) {
            double[] array = new double[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Double) node.jjtGetChild(i).jjtAccept(this, data)).doubleValue();
            }
            return array;
        } else if (componentType.equals(float.class)) {
            float[] array = new float[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Float) node.jjtGetChild(i).jjtAccept(this, data)).floatValue();
            }
            return array;
        } else if (componentType.equals(byte.class)) {
            byte[] array = new byte[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Byte) node.jjtGetChild(i).jjtAccept(this, data)).byteValue();
            }
            return array;
        } else if (componentType.equals(char.class)) {
            char[] array = new char[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Character) node.jjtGetChild(i).jjtAccept(this, data)).charValue();
            }
            return array;
        } else if (componentType.equals(boolean.class)) {
            boolean[] array = new boolean[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = ((Boolean) node.jjtGetChild(i).jjtAccept(this, data)).booleanValue();
            }
            return array;
        } else if (componentType.equals(Class.class)) {
            Class[] array = new Class[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = (Class) node.jjtGetChild(i).jjtAccept(this, data);
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
     * FIXME handle array types
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
                Class referencedClass = Class.forName(className, false, m_annotationClass.getClassLoader());
                return new Java5AnnotationInvocationHandler.LazyClass(referencedClass.getName().replace('/', '.'));
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
            throw new RuntimeException(
                    "could not access reference field [" + identifier + "] due to: " + e.toString()
            );
        }
    }

    /**
     * Holds the setter, getter methods and the value type.
     */
    private static class MethodInfo {

        public Method valueMethod;

        public Class valueType;
    }
}
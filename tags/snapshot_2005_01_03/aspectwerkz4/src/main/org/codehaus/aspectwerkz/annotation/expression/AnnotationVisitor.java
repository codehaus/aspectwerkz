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
import org.codehaus.aspectwerkz.annotation.AnnotationElement;
import org.codehaus.aspectwerkz.annotation.AnnotationManager;
import org.codehaus.aspectwerkz.annotation.Annotation;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.HashMap;

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

        		if(!(node.jjtGetChild(0) instanceof ASTAnnotation)) { // child already set the value
               m_annotationElementValueHoldersByName.put("value",
                     new AnnotationElement("value", value));
        		}
        } else {
            for (int i = 0; i < nr; i++) {
                node.jjtGetChild(i).jjtAccept(this, data);
            }
        }
        return null;
    }

    public Object visit(ASTKeyValuePair node, Object data) {
        String elementName = node.getKey();

        // get the methodInfo for this elementName to access its type from its name
        MethodInfo elementMethod = getMethodInfo(elementName);

        // nested annotation
        if (node.jjtGetChild(0) instanceof ASTAnnotation) {
            Map nestedAnnotationElementValueHoldersByName = new HashMap();
            AnnotationVisitor nestedAnnotationVisitor = new AnnotationVisitor(
                    nestedAnnotationElementValueHoldersByName,
                    elementMethod.elementType
                    );
            nestedAnnotationVisitor.visit((ASTAnnotation)node.jjtGetChild(0), data);
            m_annotationElementValueHoldersByName.put(elementName,
                    new AnnotationElement(elementName,
                            AnnotationManager.instantiateNestedAnnotation(elementMethod.elementType, nestedAnnotationElementValueHoldersByName)));
        } else {
            Object typedValue = node.jjtGetChild(0).jjtAccept(this, elementMethod);
            m_annotationElementValueHoldersByName.put(elementName,
                    new AnnotationElement(elementName, typedValue));
        }
        return null;
    }

    public Object visit(ASTArray node, Object data) {
        MethodInfo methodInfo = (MethodInfo) data;
        Class elementType = methodInfo.elementType;
        if (!elementType.isArray()) {
            throw new RuntimeException(
                    "type for element ["
                    + methodInfo.elementMethod.getName()
                    + "] is not of type array"
            );
        }
        Class componentType = elementType.getComponentType();
        if (componentType.isArray()) {
            throw new UnsupportedOperationException(
                    "multidimensional arrays are not supported for element type, was required method ["
                    + methodInfo.elementMethod.getName()
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
     * @param elementName
     * @return
     */
    private MethodInfo getMethodInfo(final String elementName) {
        StringBuffer javaBeanMethodPostfix = new StringBuffer();
        javaBeanMethodPostfix.append(elementName.substring(0, 1).toUpperCase());
        if (elementName.length() > 1) {
            javaBeanMethodPostfix.append(elementName.substring(1));
        }

        MethodInfo methodInfo = new MethodInfo();
        Method[] methods = m_annotationClass.getDeclaredMethods();
        // look for element methods
        for (int i = 0; i < methods.length; i++) {
            Method elementMethod = methods[i];
            if (elementMethod.getName().equals(elementName)) {
                methodInfo.elementMethod = elementMethod;
                methodInfo.elementType = elementMethod.getReturnType();
                break;
            }
        }
        if (methodInfo.elementMethod == null) {
            throw new RuntimeException(
                    "method for the annotation element ["
                    + elementName
                    + "] can not be found in annotation interface ["
                    + m_annotationClass.getName()
                    + "]"
            );
        }
        return methodInfo;
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

    private Object createTypedArray(final ASTArray node,
                                    final Object data,
                                    final int nrOfElements,
                                    final Class componentType) {
        if (componentType.equals(String.class)) {
            String[] array = new String[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                String value = (String) node.jjtGetChild(i).jjtAccept(this, data);
                array[i] = value;
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
            AnnotationElement.LazyClass[] array = new AnnotationElement.LazyClass[nrOfElements];
            for (int i = 0; i < nrOfElements; i++) {
                array[i] = (AnnotationElement.LazyClass) node.jjtGetChild(i).jjtAccept(this, data);
            }
            return array;
        } else {
            if (nrOfElements > 1 && node.jjtGetChild(0) instanceof ASTAnnotation) {
                // nested array of annotation
                Object[] nestedTyped = (Object[])Array.newInstance(componentType, nrOfElements);
                for (int i = 0; i < nrOfElements; i++) {
                    Map nestedAnnotationElementValueHoldersByName = new HashMap();
                    AnnotationVisitor nestedAnnotationVisitor = new AnnotationVisitor(
                            nestedAnnotationElementValueHoldersByName,
                            componentType
                            );
                    nestedAnnotationVisitor.visit((ASTAnnotation)node.jjtGetChild(i), data);
                    nestedTyped[i] = AnnotationManager.instantiateNestedAnnotation(componentType, nestedAnnotationElementValueHoldersByName);
                }
                return nestedTyped;
            } else {
                // reference type
                Object[] array = new Object[nrOfElements];
                for (int i = 0; i < nrOfElements; i++) {
                    array[i] = node.jjtGetChild(i).jjtAccept(this, data);
                }
                return array;
            }
        }
    }

    /**
     * FIXME handle array types
     */
    private Object handleClassIdentifier(String identifier) {
        int index = identifier.lastIndexOf('.');
        String className = identifier.substring(0, index);

        int dimension = 0;
        String componentClassName = className;
        while (componentClassName.endsWith("[]")) {
            dimension++;
            componentClassName = componentClassName.substring(0, componentClassName.length()-2);
        }

        Class componentClass = null;
        boolean isComponentPrimitive = true;
        if (componentClassName.equals("long")) {
            componentClass = long.class;
        } else if (componentClassName.equals("int")) {
            componentClass = int.class;
        } else if (componentClassName.equals("short")) {
            componentClass = short.class;
        } else if (componentClassName.equals("double")) {
            componentClass = double.class;
        } else if (componentClassName.equals("float")) {
            componentClass = float.class;
        } else if (componentClassName.equals("byte")) {
            componentClass = byte.class;
        } else if (componentClassName.equals("char")) {
            componentClass = char.class;
        } else if (componentClassName.equals("boolean")) {
            componentClass = boolean.class;
        } else {
            isComponentPrimitive = false;
            try {
                componentClass = Class.forName(componentClassName, false, m_annotationClass.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("could not load class [" + className + "] due to: " + e.toString());
            }
        }

        // primitive types are not wrapped in a LazyClass
        if (isComponentPrimitive) {
            if (dimension <= 0) {
                return componentClass;
            } else {
                return Array.newInstance(componentClass, dimension);
            }
        } else {
            String componentType = Type.getType(componentClass).getDescriptor();
            for (int i = 0; i < dimension; i++) {
                componentType = "[" + componentType;
            }
            Type type = Type.getType(componentType);
            return new AnnotationElement.LazyClass(type.getClassName());
        }
    }

    private Object handleReferenceIdentifier(String identifier) {
        int index = identifier.lastIndexOf('.');
        String className = identifier.substring(0, index);
        String fieldName = identifier.substring(index + 1, identifier.length());
        try {
            // TODO m_annotationClass might be higher in the CL than a referenced identifier
            Class clazz = Class.forName(className, false, m_annotationClass.getClassLoader());
            Field field = clazz.getDeclaredField(fieldName);
            return field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(
                    "could not access reference field [" + identifier + "] due to: " + e.toString()
            );
        }
    }

    /**
     * Holds the element method and type.
     */
    private static class MethodInfo {

        public Method elementMethod;

        public Class elementType;
    }
}
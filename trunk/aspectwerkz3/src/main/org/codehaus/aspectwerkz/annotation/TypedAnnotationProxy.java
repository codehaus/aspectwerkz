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
import org.codehaus.aspectwerkz.annotation.expression.DumpVisitor;
import org.codehaus.aspectwerkz.annotation.expression.ast.AnnotationParser;
import org.codehaus.aspectwerkz.annotation.expression.ast.ParseException;
import org.codehaus.aspectwerkz.util.Strings;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class TypedAnnotationProxy extends AnnotationProxy implements Annotation, Serializable {
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

        /*
                // add a typed annotation value for later retrieval
                addTypedAnnotationValue(valueType, name, value);

                // transform the string value to a typed value
                Object typedValue = convertStringValueToTypedValue(valueType, valueAsString);

                // invoke the setter method
                try {
                    setterMethod.invoke(this, new Object[]{typedValue});
                } catch (Exception e) {
                    throw new RuntimeException(
                            "could not invoke setter method for value [" + name + "] due to: "
                            + e.toString()
                    );
                }
        */
    }

    private Object convertStringValueToTypedValue(Class valueType, String valueAsString) {
        Object realValue = null;
        if (isConstantReferenceType(valueAsString)) {
            realValue = handleConstantReferenceTyp(valueAsString, realValue);
        } else {
            if (valueType.equals(String.class)) {
                realValue = valueAsString;
            } else if (valueType.isArray()) {
                Class componentType = valueType.getComponentType();
                realValue = handleArrayType(componentType, valueAsString, realValue);
            } else if (valueType.isPrimitive()) {
                realValue = handlePrimitiveType(valueType, valueAsString, realValue);
            }
        }
        return realValue;
    }

    private Object handleConstantReferenceTyp(String valueAsString, Object realValue) {
        int index = valueAsString.lastIndexOf('.');
        String className = valueAsString.substring(0, index);
        String fieldName = valueAsString.substring(index + 1, valueAsString.length());
        try {
            // TODO: will the context CL be correct in all cases? User need to put dependent classes in claspath
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Field field = clazz.getDeclaredField(fieldName);
            realValue = field.get(null);
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }
        return realValue;
    }

    private boolean isConstantReferenceType(String valueAsString) {
        int first = valueAsString.indexOf('.');
        int last = valueAsString.lastIndexOf('.');
        int comma = valueAsString.indexOf(',');
        if ((first > 0) && (last > 0) && (first != last) && (comma < 0)) {
            return true;
        } else {
            return false;
        }
    }

    private Object handlePrimitiveType(Class valueType, String valueAsString, Object realValue) {
        if (valueType.equals(long.class)) {
            realValue = new Long(valueAsString);
        } else if (valueType.equals(int.class)) {
            realValue = new Integer(valueAsString);
        } else if (valueType.equals(short.class)) {
            realValue = new Short(valueAsString);
        } else if (valueType.equals(double.class)) {
            realValue = new Double(valueAsString);
        } else if (valueType.equals(float.class)) {
            realValue = new Float(valueAsString);
        } else if (valueType.equals(byte.class)) {
            realValue = new Byte(valueAsString);
        } else if (valueType.equals(boolean.class)) {
            realValue = new Boolean(valueAsString);
        } else if (valueType.equals(char.class)) {
            realValue = new Character(valueAsString.toCharArray()[0]);
        }
        return realValue;
    }

    private Object handleArrayType(Class valueType, String valueAsString, Object realValue) {
        if ((valueAsString.charAt(0) == '{') && (valueAsString.charAt(valueAsString.length() - 1) == '}')) {
            valueAsString = valueAsString.substring(1, valueAsString.length() - 1);
            String[] elements = Strings.splitString(valueAsString, ",");
            if (valueType.equals(String.class)) {
                String[] arr = new String[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    String stringValue = elements[i].trim();
                    if ((stringValue.charAt(0) == '"') && (stringValue.charAt(stringValue.length() - 1) == '"')) {
                        arr[i] = new String(stringValue.substring(1, stringValue.length() - 1));
                    } else {
                        throw new RuntimeException("elements in string arrays must be put in quotes");
                    }
                }
                realValue = arr;
            } else if (valueType.equals(long.class)) {
                long[] arr = new long[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    arr[i] = Long.parseLong(elements[i].trim());
                }
                realValue = arr;
            } else if (valueType.equals(int.class)) {
                int[] arr = new int[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    arr[i] = Integer.parseInt(elements[i].trim());
                }
                realValue = arr;
            } else if (valueType.equals(short.class)) {
                short[] arr = new short[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    arr[i] = Short.parseShort(elements[i].trim());
                }
                realValue = arr;
            } else if (valueType.equals(double.class)) {
                double[] arr = new double[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    arr[i] = Double.parseDouble(elements[i].trim());
                }
                realValue = arr;
            } else if (valueType.equals(float.class)) {
                float[] arr = new float[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    arr[i] = Float.parseFloat(elements[i].trim());
                }
                realValue = arr;
            } else if (valueType.equals(byte.class)) {
                byte[] arr = new byte[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    arr[i] = Byte.parseByte(elements[i].trim());
                }
                realValue = arr;
            } else if (valueType.equals(boolean.class)) {
                boolean[] arr = new boolean[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    arr[i] = Boolean.getBoolean(elements[i].trim());
                }
                realValue = arr;
            } else if (valueType.equals(char.class)) {
                char[] arr = new char[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    arr[i] = elements[i].trim().toCharArray()[0];
                }
                realValue = arr;
            }
            return realValue;
        } else {
            throw new RuntimeException("not a valid array type: " + valueAsString);
        }
    }

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

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.objectweb.asm.attrs.*;
import org.objectweb.asm.attrs.Annotation;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.io.Serializable;

/**
 * Dynamic proxy handler for ASM Annotations we extract
 * The handler resolve the LazyClass to a concrete Class so that the proxy creation does not trigger
 * any class loading.
 * <p/>
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Java5AnnotationInvocationHandler implements InvocationHandler {

    /**
     * The annotation class name
     */
    private final String m_annotationClassName;

    /**
     * A list of AnnotationElement containing the annotation instance elemnt values
     * (including the defaulted value)
     */
    private final List m_annotationElements;

    /**
     * private ctor - see getAnnotationProxy()
     *
     * @param annotationClassName
     * @param annotationElements
     */
    private Java5AnnotationInvocationHandler(String annotationClassName, Collection annotationElements) {
        m_annotationClassName = annotationClassName;
        m_annotationElements = new ArrayList(annotationElements.size());
        for (Iterator iterator = annotationElements.iterator(); iterator.hasNext();) {
            m_annotationElements.add(iterator.next());
        }
    }

    /**
     * Dynamic proxy based implementation
     * toString(), annotationType() and value() have a specific behavior
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if ("toString".equals(name)) {
            StringBuffer sb = new StringBuffer();
            sb.append('@').append(m_annotationClassName);
            sb.append("(");
            String sep = "";
            for (Iterator iterator = m_annotationElements.iterator(); iterator.hasNext();) {
                AnnotationElement annotationElement = (AnnotationElement) iterator.next();
                sb.append(sep).append(annotationElement.name + "=" + annotationElement.toString());
                sep = ", ";
            }
            sb.append(")");
            return sb.toString();
        } else if ("annotationType".equals(name)) {
            // funny, may explain why 1.5 Annotation intf has annotationType + getClass
            // since a dynamic proxy handler cannot hijack getClass() ..
            return Class.forName(m_annotationClassName, false, proxy.getClass().getClassLoader());
        } else if ("value".equals(name)) {
            if (m_annotationElements.isEmpty()) {
                return null;
            } else {
                //FIXME !!value can be there with other elements !
                // we could check that we don't have more than one element
                return ((AnnotationElement) m_annotationElements.get(0)).resolveValueHolderFrom(
                        proxy.getClass().getClassLoader()
                );
            }
        } else {
            for (Iterator iterator = m_annotationElements.iterator(); iterator.hasNext();) {
                AnnotationElement annotationElement = (AnnotationElement) iterator.next();
                if (name.equals(annotationElement.name)) {
                    return annotationElement.resolveValueHolderFrom(proxy.getClass().getClassLoader());
                }
            }
            // element not found for such a name
            throw new RuntimeException("No such element on Annotation @" + m_annotationClassName + " : " + name);
        }
    }

    /**
     * Build and return a dynamic proxy representing the given ASM Annotation.
     * The proxy implements the AspectWerkz Annotation interface, as well as the user type Annotation.
     * Each elements of the annotation is proxied if needed or agressively created unless Class types to not trigger
     * any nested loading.
     *
     * Note: JSR-175 does not support Annotation value of N-dimensional array. At most 1 dimension is supported and
     * only for a subset of Java types.
     *
     * @param annotation
     * @param loader the classloader of the annotatED component (can be different from the one of the annotation class)
     * @return
     */
    public static org.codehaus.aspectwerkz.annotation.Annotation getAnnotationProxy(org.objectweb.asm.attrs.Annotation annotation, ClassLoader loader) {
        String annotationClassName = Type.getType(annotation.type).getClassName();

        // get the ClassInfo for the annoation class to populate the assigned element values
        // with lazy value holders from the setted value or the default value if defaulted element
        // has been used in the annotation
        ClassInfo annotationClassInfo = AsmClassInfo.getClassInfo(annotationClassName, loader);
        Map annotationElementValueHoldersByName = new HashMap();

        // populate with the default values (might be then overriden by setted values)
        MethodInfo[] annotationMethods = annotationClassInfo.getMethods();
        for (int i = 0; i < annotationMethods.length; i++) {
            MethodInfo annotationMethod = annotationMethods[i];
            for (Iterator iterator = annotationMethod.getAnnotations().iterator(); iterator.hasNext();) {
                AnnotationInfo annotationInfo = (AnnotationInfo) iterator.next();
                // handles AnnotationDefault attribute that we have wrapped. See AnnotationDefault.
                if (annotationInfo.getName().equals(AnnotationDefault.NAME)) {
                    Object value = ((AnnotationDefault)annotationInfo.getAnnotation()).value();
                    Object valueHolder = getAnnotationValueHolder(value, loader);
                    annotationElementValueHoldersByName.put(annotationMethod.getName(),
                                                            new AnnotationElement(annotationMethod.getName(),
                                                                                  valueHolder)
                    );
                }
            }
        }

        // override and populate with the setted values
        List settedElementValues = annotation.elementValues;
        for (int i = 0; i < settedElementValues.size(); i++) {
            Object[] element = (Object[]) settedElementValues.get(i);
            String name = (String) element[0];
            Object valueHolder = getAnnotationValueHolder(element[1], loader);
            annotationElementValueHoldersByName.put(name, new AnnotationElement(name, valueHolder));
        }

        // create a dynamic proxy to embody the annotation instance
        try {
            Class typeClass = Class.forName(annotationClassName, false, loader);
            Object proxy = Proxy.newProxyInstance(
                    loader,
                    new Class[]{org.codehaus.aspectwerkz.annotation.Annotation.class, typeClass},
                    new Java5AnnotationInvocationHandler(annotationClassName,
                                                         annotationElementValueHoldersByName.values()
                    )
            );
            return (org.codehaus.aspectwerkz.annotation.Annotation) proxy;
        } catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Turn an ASM Annotation value into a concrete Java value holder, unless the value is of type
     * Class, in which case we wrap it behind a LazyClass() object so that actual loading of the class
     * will be done lazily
     *
     * @param value
     * @param loader
     * @return
     */
    private static Object getAnnotationValueHolder(Object value, ClassLoader loader) {
        if (value instanceof Annotation.EnumConstValue) {
            Annotation.EnumConstValue enumAsmValue = (Annotation.EnumConstValue) value;
            try {
                Class enumClass = Class.forName(Type.getType(enumAsmValue.typeName).getClassName(), false, loader);
                Field enumConstValue = enumClass.getField(enumAsmValue.constName);
                return enumConstValue.get(null);
            } catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        } else if (value instanceof Type) {
            // TODO may require additional filtering ?
            return new LazyClass(((Type) value).getClassName());
        } else if (value instanceof Annotation) {
            return getAnnotationProxy(((Annotation) value), loader);
        } else if (value instanceof Object[]) {
            Object[] values = (Object[]) value;
            Object[] holders = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                holders[i] = getAnnotationValueHolder(values[i], loader);
            }
            return holders;
        }
        return value;
    }

    /**
     * A wrapper for a className, that will allow late loading of the actual Class object of an annotation value
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public static class LazyClass implements Serializable {

        public LazyClass(String className) {
            this.className = className;
        }

        public String className;

        public String toString() {
            return className;
        }

        public Class getActualClassFrom(ClassLoader loader) {
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException e) {
                throw new WrappedRuntimeException(e);
            }
        }

    }

    /**
     * A structure for an Annotation element
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public static class AnnotationElement implements Serializable {
        String name;
        Object valueHolder;
        protected boolean isLazyClass = false;
        protected boolean isLazyClassArray = false;

        public AnnotationElement(String name, Object valueHolder) {
            this.name = name;
            this.valueHolder = valueHolder;
            if (valueHolder instanceof LazyClass) {
                isLazyClass = true;
            } else if (valueHolder instanceof Object[]) {
                if (((Object[])valueHolder).getClass().getComponentType().isAssignableFrom(LazyClass.class)) {
                    isLazyClassArray = true;
                }
            }
        }

        Object resolveValueHolderFrom(ClassLoader loader) {
            if (isLazyClass) {
                return ((LazyClass) valueHolder).getActualClassFrom(loader);
            } else if (isLazyClassArray) {
                Object[] annotationValueHolderArray = (Object[]) valueHolder;
                Class[] resolved = new Class[annotationValueHolderArray.length];
                for (int i = 0; i < annotationValueHolderArray.length; i++) {
                    resolved[i] = ((LazyClass)annotationValueHolderArray[i]).getActualClassFrom(loader);
                }
                return resolved;
                //TODO support N dimension array needed ?
            } else {
                return valueHolder;
            }
        }

        /**
         * Returns a string representation of the annotation element value
         * as per JSR-175 ie array are pretty printed
         *
         * Note that such a represention is not parsable again
         * (f.e. element String s() will not have quotes and escapes etc.
         * @return
         */
        public String toString() {
            if (isLazyClass) {
                StringBuffer sb = new StringBuffer("class ");
                sb.append(((LazyClass) valueHolder).className);
                return sb.toString();
            } else {
                if (valueHolder == null) {
                    return "null";
                } else {
                    return valueHolder.toString();
                }
            }
        }
    }
}


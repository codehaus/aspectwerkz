/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * A structure for an Annotation element
 * It wraps value behind an holder. The holder is the object itself (boxed) excepted
 * for Class, for which it is a LazyClass.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AnnotationElement implements Serializable {

    //TODO calculate
    private static final long serialVersionUID = 1L;

    /**
     * element name
     */
    String name;

    /**
     * element value holder
     */
    private Object valueHolder;

    /**
     * true if we have a lasy class (optimization to avoid instance of at each get)
     */
    protected boolean isLazyClass = false;

    /**
     * true if we have a lasy class array (N-dim) (optimization to avoid instance of at each get)
     */
    protected boolean isLazyClassArray = false;

    /**
     * Build a new annotation element
     * @param name
     * @param valueHolder
     */
    public AnnotationElement(String name, Object valueHolder) {
        this.name = name;
        this.valueHolder = valueHolder;
        if (valueHolder instanceof LazyClass) {
            isLazyClass = true;
        } else if (valueHolder instanceof LazyClass[]) {
            isLazyClassArray = true;
        }
    }

    /**
     * Returns the actual holded element value
     *
     * @param loader from which to resolve LazyClass. It should be the annotated element class loader
     * @return
     */
    public Object resolveValueHolderFrom(ClassLoader loader) {
        if (isLazyClass) {
            return ((LazyClass) valueHolder).resolveFrom(loader);
        } else if (isLazyClassArray) {
            Object[] annotationValueHolderArray = (Object[]) valueHolder;
            Class[] resolved = new Class[annotationValueHolderArray.length];
            for (int i = 0; i < annotationValueHolderArray.length; i++) {
                resolved[i] = ((LazyClass)annotationValueHolderArray[i]).resolveFrom(loader);
            }
            return resolved;
            //TODO support N dimension array needed ?
        } else {
            return valueHolder;
        }
    }

    /**
     * Returns a string representation of the annotation element value
     *
     * Note that such a represention won't look like source code.
     * (f.e. element String s() will not have quotes and escapes etc).
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

    /**
     * A wrapper for a className, that will allow late loading of the actual Class object of an annotation value
     * For array type, className is componentClassName([])*
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public static class LazyClass implements Serializable {

        //TODO calculate
        private static final long serialVersionUID = 1L;

        public String className;
        private String componentClassName = null;
        private int dimemsion = 0;

        public LazyClass(String className) {
            this.className = className;

            componentClassName = className;
            while (componentClassName.endsWith("[]")) {
                dimemsion++;
                componentClassName = componentClassName.substring(0, componentClassName.length()-2);
            }
        }

        public String toString() {
            return className;
        }

        public Class resolveFrom(ClassLoader loader) {
            try {
                if (dimemsion <= 0) {
                    return Class.forName(className, false, loader);
                } else {
                    Class componentClass = Class.forName(componentClassName, false, loader);
                    return (Array.newInstance(componentClass, dimemsion)).getClass();
                }
            } catch (ClassNotFoundException e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

}

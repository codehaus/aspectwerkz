/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.aspectwerkz.MethodComparator;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class ReflectHelper {

    /**
     * Creates a sorted method list of all the public methods in the class and super classes.
     * 
     * @param klass
     *            the class with the methods
     * @return the sorted method list
     */
    public static List createSortedMethodList(final Class klass) {
        if (klass == null) {
            throw new IllegalArgumentException("class to sort method on can not be null");
        }

        // get all public methods including the inherited methods
        java.lang.reflect.Method[] methods = klass.getMethods();
        List methodList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            java.lang.reflect.Method method = methods[i];
            if (!method.getName().equals("equals") && !method.getName().equals("hashCode")
                    && !method.getName().equals("getClass") && !method.getName().equals("toString")
                    && !method.getName().equals("wait") && !method.getName().equals("notify")
                    && !method.getName().equals("notifyAll")
                    && !method.getName().startsWith(TransformationUtil.CLASS_LOOKUP_METHOD)
                    && !method.getName().startsWith(TransformationUtil.GET_UUID_METHOD)
                    && !method.getName().startsWith(TransformationUtil.GET_META_DATA_METHOD)
                    && !method.getName().startsWith(TransformationUtil.SET_META_DATA_METHOD)
                    && !method.getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX)
                    && !method.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                methodList.add(method);
            }
        }
        Collections.sort(methodList, MethodComparator.getInstance(MethodComparator.NORMAL_METHOD));
        return methodList;
    }

    /**
     * Converts modifiers represented in a string array to an int.
     * 
     * @param modifiers
     *            the modifiers as strings
     * @return the modifiers as an int
     */
    public static int getModifiersAsInt(final String[] modifiers) {
        int accessFlags = 0;
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i].equals("abstract")) {
                accessFlags |= Modifier.ABSTRACT;
            } else if (modifiers[i].equals("final")) {
                accessFlags |= Modifier.FINAL;
            } else if (modifiers[i].equals("interface")) {
                accessFlags |= Modifier.INTERFACE;
            } else if (modifiers[i].equals("native")) {
                accessFlags |= Modifier.NATIVE;
            } else if (modifiers[i].equals("private")) {
                accessFlags |= Modifier.PRIVATE;
            } else if (modifiers[i].equals("protected")) {
                accessFlags |= Modifier.PROTECTED;
            } else if (modifiers[i].equals("public")) {
                accessFlags |= Modifier.PUBLIC;
            } else if (modifiers[i].equals("static")) {
                accessFlags |= Modifier.STATIC;
            } else if (modifiers[i].equals("strict")) {
                accessFlags |= Modifier.STRICT;
            } else if (modifiers[i].equals("synchronized")) {
                accessFlags |= Modifier.SYNCHRONIZED;
            } else if (modifiers[i].equals("transient")) {
                accessFlags |= Modifier.TRANSIENT;
            } else if (modifiers[i].equals("volatile")) {
                accessFlags |= Modifier.VOLATILE;
            }
        }
        return accessFlags;
    }

    /**
     * Calculate the hash for a class.
     * 
     * @param klass
     *            the class
     * @return the hash
     */
    public static int calculateHash(final Class klass) {
        //        int hash = 17;
        //        Method[] methods = klass.getDeclaredMethods();
        //        for (int i = 0; i < methods.length; i++) {
        //            hash = (37 * hash) + calculateHash(methods[i]);
        //        }
        //        Constructor[] constructors = klass.getDeclaredConstructors();
        //        for (int i = 0; i < constructors.length; i++) {
        //            hash = (37 * hash) + calculateHash(constructors[i]);
        //        }
        //        Field[] fields = klass.getDeclaredFields();
        //        for (int i = 0; i < fields.length; i++) {
        //            hash = (37 * hash) + calculateHash(fields[i]);
        //        }
        //        return hash;
        return klass.getName().hashCode();
    }

    /**
     * Calculate the hash for a method.
     * 
     * @param method
     *            the method
     * @return the hash
     */
    public static int calculateHash(final java.lang.reflect.Method method) {
        int hash = 17;
        hash = (37 * hash) + method.getName().hashCode();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class type = method.getParameterTypes()[i];
            hash = (37 * hash) + type.getName().hashCode();
        }
        return hash;
    }

    /**
     * Calculate the hash for a constructor.
     * 
     * @param constructor
     *            the constructor
     * @return the hash
     */
    public static int calculateHash(final Constructor constructor) {
        int hash = 17;
        hash = (37 * hash) + constructor.getName().hashCode();
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            Class type = constructor.getParameterTypes()[i];
            hash = (37 * hash) + type.getName().replace('/', '.').hashCode();
        }
        return hash;
    }

    /**
     * Calculate the hash for a field.
     * 
     * @param field
     *            the field
     * @return the hash
     */
    public static int calculateHash(final Field field) {
        int hash = 17;
        hash = (37 * hash) + field.getName().hashCode();
        Class type = field.getType();
        hash = (37 * hash) + type.getName().hashCode();
        return hash;
    }

}
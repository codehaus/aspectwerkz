/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect;

import java.lang.reflect.Modifier;

import org.codehaus.aspectwerkz.expression.SubtypePatternType;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;

/**
 * Utility method for manipulating and managing ClassInfo hierarchies.   \
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 * @TODO remove most methods here
 */
public class ClassInfoHelper {
    /**
     * Matches a type.
     *
     * @param typePattern the pattern to try to parse against
     * @param classInfo   the info of the class
     * @return
     */
    public static boolean matchType(final TypePattern typePattern, final ClassInfo classInfo) {
        SubtypePatternType type = typePattern.getSubtypePatternType();
        if (type.equals(SubtypePatternType.MATCH_ON_ALL_METHODS)) {
            return matchSuperClasses(classInfo, typePattern);
        } else if (type.equals(SubtypePatternType.MATCH_ON_BASE_TYPE_METHODS_ONLY)) {
            // TODO: matching on methods ONLY in base type needs to be completed
            // TODO: needs to work together with the method and field matching somehow
            return matchSuperClasses(classInfo, typePattern);
        } else {
            return typePattern.matches(classInfo.getName());
        }
    }

    /**
     * Tries to finds a parse at some superclass in the hierarchy. <p/>Only checks for a class parse to allow early
     * filtering. <p/>Recursive.
     *
     * @param classInfo the class info
     * @param pattern   the type pattern
     * @return boolean
     */
    public static boolean matchSuperClasses(final ClassInfo classInfo, final TypePattern pattern) {
        if ((classInfo == null) || (pattern == null)) {
            return false;
        }

        // parse the class/super class
        if (pattern.matches(classInfo.getName())) {
            return true;
        } else {
            // parse the interfaces for the class
            if (matchInterfaces(classInfo.getInterfaces(), classInfo, pattern)) {
                return true;
            }

            // no parse; getClass the next superclass
            return matchSuperClasses(classInfo.getSuperClass(), pattern);
        }
    }

    /**
     * Tries to finds a parse at some interface in the hierarchy. <p/>Only checks for a class parse to allow early
     * filtering. <p/>Recursive.
     *
     * @param interfaces the interfaces
     * @param classInfo  the class info
     * @param pattern    the type pattern
     * @return boolean
     */
    public static boolean matchInterfaces(final ClassInfo[] interfaces,
                                          final ClassInfo classInfo,
                                          final TypePattern pattern) {
        if ((interfaces.length == 0) || (classInfo == null) || (pattern == null)) {
            return false;
        }
        for (int i = 0; i < interfaces.length; i++) {
            ClassInfo anInterface = interfaces[i];
            if (pattern.matches(anInterface.getName())) {
                return true;
            } else {
                if (matchInterfaces(anInterface.getInterfaces(), classInfo, pattern)) {
                    return true;
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method is static or not.
     *
     * @param methodInfo the info for the method
     * @return boolean
     */
    public static boolean isMethodStatic(final MethodInfo methodInfo) {
        int modifiers = methodInfo.getModifiers();
        if ((modifiers & Modifier.STATIC) != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a class implements a certain inteface, somewhere up in the class hierarchy.
     *
     * @param classInfo
     * @param interfaceName
     * @return true if we have a parse else false
     */
    public static boolean implementsInterface(final ClassInfo classInfo, final String interfaceName) {
        if ((classInfo == null) || (interfaceName == null)) {
            return false;
            // TODO odd comparison
//        } else if (classInfo.getName().equals(null)) {
//            return true;
        } else {
            ClassInfo[] interfaces = classInfo.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                ClassInfo anInterface = interfaces[i];
                if (ClassInfoHelper.implementsInterface(anInterface, interfaceName)) {
                    return true;
                }
            }
            return ClassInfoHelper.implementsInterface(classInfo.getSuperClass(), interfaceName);
        }
    }

    /**
     * Checks if a class has a certain class as super class, somewhere up in the class hierarchy.
     *
     * @param classInfo the meta-data for the class to parse
     * @param className the name of the super class
     * @return true if we have a parse else false
     */
    public static boolean extendsSuperClass(final ClassInfo classInfo, final String className) {
        if ((classInfo == null) || (className == null)) {
            return false;
            // TODO odd comparison
//        } else if (classInfo.getName().equals(null)) {
//            return true;
        } else if (className.equals(classInfo.getName())) {
            return true;
        } else {
            return ClassInfoHelper.extendsSuperClass(classInfo.getSuperClass(), className);
        }
    }
}
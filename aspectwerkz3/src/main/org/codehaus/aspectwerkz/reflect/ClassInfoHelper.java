/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect;

import org.codehaus.aspectwerkz.expression.regexp.TypePattern;

/**
 * Utility method for manipulating and managing ClassInfo hierarchies.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ClassInfoHelper {
    /**
     * Matches a type.
     *
     * @param typePattern
     * @param classInfo
     * @return
     */
    public static boolean matchType(final TypePattern typePattern, final ClassInfo classInfo) {
        if (typePattern.isHierarchical()) {
            return matchSuperClasses(classInfo, typePattern);
        } else {
            return typePattern.matches(classInfo.getName());
        }
    }

    /**
     * Tries to finds a match at some superclass in the hierarchy. <p/>Only checks for a class match to allow early
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

        // match the class/super class
        if (pattern.matches(classInfo.getName())) {
            return true;
        } else {
            // match the interfaces for the class
            if (matchInterfaces(classInfo.getInterfaces(), classInfo, pattern)) {
                return true;
            }

            // no match; getClass the next superclass
            return matchSuperClasses(classInfo.getSuperClass(), pattern);
        }
    }

    /**
     * Tries to finds a match at some interface in the hierarchy. <p/>Only checks for a class match to allow early
     * filtering. <p/>Recursive.
     *
     * @param interfaces the interfaces
     * @param classInfo  the class info
     * @param pattern    the type pattern
     * @return boolean
     */
    public static boolean matchInterfaces(final ClassInfo[] interfaces, final ClassInfo classInfo,
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
}
/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.MethodComparator;

/**
 * Utility method for manipulating and managing ClassInfo hierarchies.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ClassInfoHelper {

    /**
     * Checks if a class has a certain class as super class or interface, somewhere up in the class hierarchy.
     *
     * @param classInfo the meta-data for the class to parse
     * @param className the name of the super class or interface
     * @return true if we have a parse else false
     */
    public static boolean instanceOf(final ClassInfo classInfo, final String className) {
        return implementsInterface(classInfo, className) || extendsSuperClass(classInfo, className);
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
        } else {
            //TODO: we could lookup in names onlny FIRST to not trigger lazy getInterfaces() stuff
            ClassInfo[] interfaces = classInfo.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                ClassInfo anInterface = interfaces[i];
                if (interfaceName.equals(anInterface.getName())) {
                    return true;
                } else if (ClassInfoHelper.extendsSuperClass(anInterface, interfaceName)) {
                    return true;
                }
            }
            return ClassInfoHelper.implementsInterface(classInfo.getSuperclass(), interfaceName);
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
            return ClassInfoHelper.extendsSuperClass(classInfo.getSuperclass(), className);
        }
    }

    /**
     * Creates a method list of all the methods in the class and super classes, including package private ones.
     * Inherited methods are last in the list.
     *
     * @param klass the class with the methods
     * @return the sorted method list
     */
    public static List createMethodList(final ClassInfo klass) {
        if (klass == null) {
            return new ArrayList();
        }

        // get all the inherited methods, as long as they are user defined ones
        List parentMethods = createMethodList(klass.getSuperclass());

        // get this klass methods
        List methods = new ArrayList();
        MethodInfo[] methodInfos = klass.getMethods();
        for (int i = 0; i < methodInfos.length; i++) {
            MethodInfo methodInfo = methodInfos[i];
            if (isUserDefinedMethod(methodInfo)) {
                methods.add(methodInfo);
            }
        }

        // merge the method list (parent discovered methods are not added if overrided in this klass)
        for (Iterator iterator = parentMethods.iterator(); iterator.hasNext();) {
            MethodInfo parentMethod = (MethodInfo) iterator.next();
            if (!methods.contains(parentMethod)) { //FIXME seems to work but ? since tied to declaringTypeName
                methods.add(parentMethod);
            }
        }
        return methods;
    }

    /**
     * Creates a sorted method list of all the methods in the class and super classes, including package private ones.
     *
     * @param klass the class with the methods
     * @return the sorted method list
     */
    public static List createSortedMethodList(final ClassInfo klass) {
        List methods = createMethodList(klass);

        //Note: sorting is only use to maintain mixin consistency
        Collections.sort(methods, MethodComparator.getInstance(MethodComparator.METHOD_META_DATA));

        return methods;
    }

    /**
     * Creates a sorted method list of all the methods in the class and super classes, if and only
     * if those are part of the given list of interfaces declared method
     *
     * @param klass                    the class with the methods
     * @param interfaceDeclaredMethods the list of interface declared methods
     * @return the sorted method list
     */
    public static List createInterfaceDefinedSortedMethodList(final ClassInfo klass,
                                                              final List interfaceDeclaredMethods) {
        if (klass == null) {
            throw new IllegalArgumentException("class to sort method on can not be null");
        }
        // get all methods including the inherited methods
        List methodList = new ArrayList();
        for (Iterator iterator = createSortedMethodList(klass).iterator(); iterator.hasNext();) {
            MethodInfo methodInfo = (MethodInfo) iterator.next();
            if (isDeclaredByInterface(methodInfo, interfaceDeclaredMethods)) {
                methodList.add(methodInfo);
            }
        }
        return methodList;
    }

    /**
     * Returns true if the method is not of on java.lang.Object and is not an AW generated one
     *
     * @param method
     * @return
     */
    private static boolean isUserDefinedMethod(final MethodInfo method) {
        if (!method.getName().startsWith(TransformationConstants.SYNTHETIC_MEMBER_PREFIX)
            && !method.getName().startsWith(TransformationConstants.ORIGINAL_METHOD_PREFIX)
            && !method.getName().startsWith(TransformationConstants.ASPECTWERKZ_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the method is declared by one of the given method declared in an interface class
     *
     * @param method
     * @param interfaceDeclaredMethods
     * @return
     */
    private static boolean isDeclaredByInterface(final MethodInfo method, final List interfaceDeclaredMethods) {
        boolean match = false;
        for (Iterator iterator = interfaceDeclaredMethods.iterator(); iterator.hasNext();) {
            MethodInfo methodIt = (MethodInfo) iterator.next();
            if (method.getName().equals(methodIt.getName())) {
                // TODO - using param type NAME should be enough - optimize
                if (method.getParameterTypes().length == methodIt.getParameterTypes().length) {
                    boolean matchArgs = true;
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        ClassInfo parameterType = method.getParameterTypes()[i];
                        if (parameterType.getName().equals(methodIt.getParameterTypes()[i].getName())) {
                            ;
                        } else {
                            matchArgs = false;
                            break;
                        }
                    }
                    if (matchArgs) {
                        match = true;
                        break;
                    }
                }
            }
        }
        return match;
    }
}
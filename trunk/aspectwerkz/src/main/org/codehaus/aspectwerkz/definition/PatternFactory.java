/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PatternFactory {
    /**
     * Creates a class pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    public static void createClassPattern(final String pattern,
                                          final PointcutDefinition pointcutDef,
                                          final String packageName) {
        String classPattern;
        if (packageName.equals("")) {
            classPattern = pattern;
        }
        else {
            classPattern = packageName + "." + pattern;
        }

        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }
        pointcutDef.setPattern(classPattern);
        pointcutDef.setClassPattern(classPattern);
    }

    /**
     * Creates a method pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    public static void createMethodPattern(final String pattern,
                                           final PointcutDefinition pointcutDef,
                                           final String packageName) {
        int indexFirstSpace = pattern.indexOf(' ');
        String returnType = pattern.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = pattern.substring(
                indexFirstSpace, pattern.indexOf('(')).trim();
        String parameterTypes = pattern.substring(
                pattern.indexOf('('), pattern.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');

        final String methodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        String classPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);
        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }

        StringBuffer buf = new StringBuffer();
        buf.append(returnType);
        buf.append(methodPattern);
        buf.append(parameterTypes);
        pointcutDef.setPattern(buf.toString());
        pointcutDef.setClassPattern(classPattern);
    }

    /**
     * Creates a field pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    public static void createFieldPattern(final String pattern,
                                          final PointcutDefinition pointcutDef,
                                          final String packageName) {
        int indexFirstSpace = pattern.indexOf(' ');
        String fieldType = pattern.substring(0, indexFirstSpace + 1);
        String classNameWithFieldName = pattern.substring(
                indexFirstSpace, pattern.length()).trim();
        int indexLastDot = classNameWithFieldName.lastIndexOf('.');

        final String fieldPattern = classNameWithFieldName.substring(
                indexLastDot + 1, classNameWithFieldName.length()).trim();
        String classPattern = packageName + classNameWithFieldName.substring(0, indexLastDot).trim();
        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }

        StringBuffer buf = new StringBuffer();
        buf.append(fieldType);
        buf.append(fieldPattern);
        pointcutDef.setPattern(buf.toString());
        pointcutDef.setClassPattern(classPattern);
    }

    /**
     * Creates a throws pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    public static void createThrowsPattern(final String pattern,
                                           final PointcutDefinition pointcutDef,
                                           final String packageName) {
        String classAndMethodName = pattern.substring(0, pattern.indexOf('#')).trim();
        final String exceptionName = pattern.substring(pattern.indexOf('#') + 1).trim();
        int indexFirstSpace = classAndMethodName.indexOf(' ');
        final String returnType = classAndMethodName.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = classAndMethodName.substring(
                indexFirstSpace, classAndMethodName.indexOf('(')).trim();
        final String parameterTypes = classAndMethodName.substring(
                classAndMethodName.indexOf('('), classAndMethodName.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');
        final String methodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        String classPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);
        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }

        StringBuffer buf = new StringBuffer();
        buf.append(returnType);
        buf.append(methodPattern);
        buf.append(parameterTypes);
        buf.append('#');
        buf.append(exceptionName);
        pointcutDef.setClassPattern(classPattern);
        pointcutDef.setPattern(buf.toString());
    }

    /**
     * Creates a caller side pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    public static void createCallerSidePattern(String pattern,
                                               final PointcutDefinition pointcutDef,
                                               final String packageName) {
        if (pattern.indexOf('>') == -1) {
            pattern = "*->" + pattern; // if no caller side pattern is specified => default to *
        }

        String callerClassPattern = packageName + pattern.substring(0, pattern.indexOf('-')).trim();
        if (callerClassPattern.endsWith("+")) {
            callerClassPattern = callerClassPattern.substring(0, callerClassPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }

        String calleePattern = pattern.substring(pattern.indexOf('>') + 1).trim();
        int indexFirstSpace = calleePattern.indexOf(' ');
        String returnType = calleePattern.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = calleePattern.substring(
                indexFirstSpace, calleePattern.indexOf('(')).trim();
        String parameterTypes = calleePattern.substring(
                calleePattern.indexOf('('), calleePattern.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');
        String calleeMethodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        String calleeClassPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);

        if (calleeClassPattern.endsWith("+")) {
            calleeClassPattern = calleeClassPattern.substring(0, calleeClassPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }
        calleeMethodPattern = returnType + calleeMethodPattern + parameterTypes;

        StringBuffer buf = new StringBuffer();
        buf.append(calleeClassPattern);
        buf.append('#');
        buf.append(calleeMethodPattern);
        pointcutDef.setPattern(buf.toString());
        pointcutDef.setClassPattern(callerClassPattern);
    }
}

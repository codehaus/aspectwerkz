/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.regexp.PatternTuple;
import org.codehaus.aspectwerkz.regexp.Pattern;

/**
 * A factory for the different kind of patterns in the AspectWerkz framework.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class PatternFactory {

    /**
     * Creates a class pattern tuple.
     *
     * @param pattern the pattern
     * @return a tuple of the class patterns and the member pattern
     */
    public static PatternTuple createClassPatternTuple(final String pattern) {
        return PatternFactory.createClassPatternTuple(pattern, "");
    }

    /**
     * Creates a class pattern tuple.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return a tuple of the class patterns and the member pattern
     */
    public static PatternTuple createClassPatternTuple(final String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        boolean isHierarchical = false;
        try {
            String classPattern;
            if (packageName.equals("")) {
                classPattern = pattern;
            }
            else {
                classPattern = packageName + "." + pattern;
            }
            if (classPattern.endsWith("+")) {
                classPattern = classPattern.substring(0, classPattern.length() - 1);
                isHierarchical = true;
            }

            return new PatternTuple(null, classPattern, classPattern, isHierarchical);
        }
        catch (Exception e) {
            throw new DefinitionException("class pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a class pattern.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return the class pattern
     */
    public static String createClassPattern(final String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        try {
            String classPattern;
            if (packageName.equals("")) {
                classPattern = pattern;
            }
            else {
                classPattern = packageName + "." + pattern;
            }
            return classPattern;
        }
        catch (Exception e) {
            throw new DefinitionException("class pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a method pattern tuple.
     *
     * @param pattern the pattern
     * @return a tuple of the class patterns and the execution pattern
     */
    public static PatternTuple createMethodPatternTuple(final String pattern) {
        return PatternFactory.createMethodPatternTuple(pattern, "");
    }

    /**
     * Creates a method pattern tuple.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return a tuple of the class patterns and the execution pattern
     */
    public static PatternTuple createMethodPatternTuple(final String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        boolean isHierarchical = false;
        try {
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
                isHierarchical = true;
            }
            StringBuffer memberPattern = new StringBuffer();
            memberPattern.append(returnType);
            memberPattern.append(methodPattern);
            memberPattern.append(parameterTypes);

            return new PatternTuple(null, classPattern, memberPattern.toString(), isHierarchical);
        }
        catch (Exception e) {
            throw new DefinitionException("method pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a constructor pattern tuple.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return a tuple of the class patterns and the execution pattern
     */
    public static PatternTuple createConstructorPatternTuple(final String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        boolean isHierarchical = false;
        try {
            String classNameWithMethodName = pattern.substring(0, pattern.indexOf('(')).trim();
            String parameterTypes = pattern.substring(pattern.indexOf('('), pattern.length()).trim();
            int indexLastDot = classNameWithMethodName.lastIndexOf('.');

            final String methodPattern = classNameWithMethodName.substring(
                    indexLastDot + 1, classNameWithMethodName.length()).trim();
            String classPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);
            if (classPattern.endsWith("+")) {
                classPattern = classPattern.substring(0, classPattern.length() - 1);
                isHierarchical = true;
            }
            StringBuffer memberPattern = new StringBuffer();
            memberPattern.append(methodPattern);
            memberPattern.append(parameterTypes);

            return new PatternTuple(null, classPattern, memberPattern.toString(), isHierarchical);
        }
        catch (Exception e) {
            throw new DefinitionException("constructor pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a method pattern.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return the execution pattern
     */
    public static String createMethodPattern(final String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        try {
            int indexFirstSpace = pattern.indexOf(' ');
            String returnType = pattern.substring(0, indexFirstSpace + 1);
            String classNameWithMethodName = pattern.substring(
                    indexFirstSpace, pattern.length()).trim();

            StringBuffer fullPattern = new StringBuffer();
            fullPattern.append(returnType);
            if (!packageName.equals("")) {
                fullPattern.append(packageName);
                fullPattern.append('.');
            }
            fullPattern.append(classNameWithMethodName);

            return fullPattern.toString();
        }
        catch (Exception e) {
            throw new DefinitionException("method pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a field pattern tuple.
     *
     * @param pattern the pattern
     * @return a tuple of the class patterns and the member pattern
     */
    public static PatternTuple createFieldPatternTuple(final String pattern) {
        return PatternFactory.createFieldPatternTuple(pattern, "");
    }

    /**
     * Creates a field pattern tuple.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return a tuple of the class patterns and the member pattern
     */
    public static PatternTuple createFieldPatternTuple(final String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        boolean isHierarchical = false;
        try {
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
                isHierarchical = true;
            }

            StringBuffer memberPattern = new StringBuffer();
            memberPattern.append(fieldType);
            memberPattern.append(fieldPattern);

            return new PatternTuple(null, classPattern, memberPattern.toString(), isHierarchical);
        }
        catch (Exception e) {
            throw new DefinitionException("field pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a field pattern.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return the member pattern
     */
    public static String createFieldPattern(final String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        boolean isHierarchical = false;
        try {
            int indexFirstSpace = pattern.indexOf(' ');
            String fieldType = pattern.substring(0, indexFirstSpace + 1);
            String classNameWithFieldName = pattern.substring(
                    indexFirstSpace, pattern.length()).trim();

            StringBuffer fullPattern = new StringBuffer();
            fullPattern.append(fieldType);
            if (!packageName.equals("")) {
                fullPattern.append(packageName);
                fullPattern.append('.');
            }
            fullPattern.append(classNameWithFieldName);

            return fullPattern.toString();
        }
        catch (Exception e) {
            throw new DefinitionException("field pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a throws pattern tuple.
     *
     * @param pattern the pattern
     * @return a tuple of the class patterns and the member pattern
     */
    public static PatternTuple createThrowsPatternTuple(final String pattern) {
        return PatternFactory.createThrowsPatternTuple(pattern, "");
    }

    /**
     * Creates a throws pattern tuple.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return a tuple of the class patterns and the member pattern
     */
    public static PatternTuple createThrowsPatternTuple(final String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        boolean isHierarchical = false;
        try {
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
                isHierarchical = true;
            }

            StringBuffer memberPattern = new StringBuffer();
            memberPattern.append(returnType);
            memberPattern.append(methodPattern);
            memberPattern.append(parameterTypes);
            memberPattern.append('#');
            memberPattern.append(exceptionName);

            return new PatternTuple(null, classPattern, memberPattern.toString(), isHierarchical);
        }
        catch (Exception e) {
            throw new DefinitionException("throws pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a throws pattern.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return the throws pattern
     */
    public static String createThrowsPattern(final String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        try {
            String classAndMethodName = pattern.substring(0, pattern.indexOf('#')).trim();
            final String exceptionName = pattern.substring(pattern.indexOf('#') + 1).trim();
            int indexFirstSpace = classAndMethodName.indexOf(' ');
            final String returnType = classAndMethodName.substring(0, indexFirstSpace + 1);
            String classNameWithMethodName = classAndMethodName.substring(
                    indexFirstSpace, classAndMethodName.length()).trim();

            StringBuffer fullPattern = new StringBuffer();
            fullPattern.append(returnType);
            if (!packageName.equals("")) {
                fullPattern.append(packageName);
                fullPattern.append('.');
            }
            fullPattern.append(classNameWithMethodName);
            fullPattern.append('#');
            fullPattern.append(exceptionName);

            return fullPattern.toString();
        }
        catch (Exception e) {
            throw new DefinitionException("throws pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a call pattern tuple.
     *
     * @param type the pattern type
     * @param pattern the pattern
     * @return a tuple of the class patterns and the member pattern
     */
    public static PatternTuple createCallPatternTuple(final int type, final String pattern) {
        return PatternFactory.createCallPatternTuple(type, pattern, "");
    }

    /**
     * Creates a call pattern tuple.
     *
     * @param type the pattern type
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return a tuple of the class patterns and the member pattern
     */
    public static PatternTuple createCallPatternTuple(final int type,
                                                      String pattern,
                                                      final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        boolean isHierarchical = false;
        boolean isHierarchicalCallee = false;
        try {
            if (pattern.indexOf('>') == -1) {
                pattern = "*->" + pattern; // if no caller side pattern is specified => default to *
            }
            String callerClassPattern = packageName + pattern.substring(0, pattern.indexOf('-')).trim();
            if (callerClassPattern.endsWith("+")) {
                callerClassPattern = callerClassPattern.substring(0, callerClassPattern.length() - 1);
                isHierarchical = true;
            }
            String calleePattern = pattern.substring(pattern.indexOf('>') + 1).trim();
            int indexFirstSpace = calleePattern.indexOf(' ');

            String calleeClassPattern = null;
            String calleeMethodPattern = null;
            if (type == Pattern.CONSTRUCTOR) {
                String classNameWithMethodName = calleePattern.substring(
                        0, calleePattern.indexOf('(')).trim();
                String parameterTypes = calleePattern.substring(
                        calleePattern.indexOf('('), calleePattern.length()).trim();
                int indexLastDot = classNameWithMethodName.lastIndexOf('.');
                calleeMethodPattern = classNameWithMethodName.substring(
                        indexLastDot + 1, classNameWithMethodName.length()).trim();
                calleeClassPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);

                if (calleeClassPattern.endsWith("+")) {
                    calleeClassPattern = calleeClassPattern.substring(0, calleeClassPattern.length() - 1);
                    isHierarchicalCallee = true;
                }
                calleeMethodPattern = calleeMethodPattern + parameterTypes;
            }
            else {
                String returnType = calleePattern.substring(0, indexFirstSpace + 1);
                String classNameWithMethodName = calleePattern.substring(
                        indexFirstSpace, calleePattern.indexOf('(')).trim();
                String parameterTypes = calleePattern.substring(
                        calleePattern.indexOf('('), calleePattern.length()).trim();
                int indexLastDot = classNameWithMethodName.lastIndexOf('.');
                calleeMethodPattern = classNameWithMethodName.substring(
                        indexLastDot + 1, classNameWithMethodName.length()).trim();
                calleeClassPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);

                if (calleeClassPattern.endsWith("+")) {
                    calleeClassPattern = calleeClassPattern.substring(0, calleeClassPattern.length() - 1);
                    isHierarchicalCallee = true;
                }
                calleeMethodPattern = returnType + calleeMethodPattern + parameterTypes;
            }

            StringBuffer buf = new StringBuffer();
            buf.append(calleeClassPattern);
            buf.append(SystemDefinition.CALLER_SIDE_DELIMITER);
            buf.append(calleeMethodPattern);

            return new PatternTuple(
                    callerClassPattern, calleeClassPattern, buf.toString(),
                    isHierarchical, isHierarchicalCallee
            );
        }
        catch (Exception e) {
            throw new DefinitionException("caller side pattern is not well formed [" + pattern + "]");
        }
    }

    /**
     * Creates a call pattern.
     *
     * @param pattern the pattern
     * @param packageName the name of the package
     * @return the call pattern
     */
    public static String createCallPattern(String pattern, final String packageName) {
        if (pattern == null) throw new IllegalArgumentException("pattern can not be null");
        if (packageName == null) throw new IllegalArgumentException("package name can not be null");

        try {
            if (pattern.indexOf('>') == -1) {
                pattern = "*->" + pattern; // if no caller side pattern is specified => default to *
            }
            String callerClassPattern = packageName + pattern.substring(0, pattern.indexOf('-')).trim();
            if (callerClassPattern.endsWith("+")) {
                callerClassPattern = callerClassPattern.substring(0, callerClassPattern.length() - 1);
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

            StringBuffer fullPattern = new StringBuffer();
            fullPattern.append(callerClassPattern);
            fullPattern.append(SystemDefinition.CALLER_SIDE_DELIMITER);
            fullPattern.append(returnType);
            if (!packageName.equals("")) {
                fullPattern.append(packageName);
                fullPattern.append('.');
            }
            fullPattern.append(calleeClassPattern);
            fullPattern.append('.');
            fullPattern.append(calleeMethodPattern);
            fullPattern.append(parameterTypes);

            return fullPattern.toString();
        }
        catch (Exception e) {
            throw new DefinitionException("caller side pattern is not well formed [" + pattern + "]");
        }
    }
}

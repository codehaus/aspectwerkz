/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.definition.regexp;

import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.definition.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Implements the regular expression pattern matcher for methods in AspectWerkz.
 * <p/>
 * Example of supported patterns:
 * <pre>
 *      String method() // supports abbreviations for the java.lang.* and java.util.* namespaces
 *      java.lang.String method()
 *      * method()
 *      int method(*) // matches one parameter
 *      int method(..) // matches any number of parameters
 *      int method(*,*,int,*)
 * </pre>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MethodPattern.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class MethodPattern extends Pattern {

    /**
     * The method name pattern.
     */
    protected java.util.regex.Pattern m_methodNamePattern;

    /**
     * A list with all the parameter type patterns.
     */
    protected List m_parameterTypePatterns;

    /**
     * The return type pattern.
     */
    protected java.util.regex.Pattern m_returnTypePattern;

    /**
     * The full pattern as a string.
     */
    protected String m_pattern;

    /**
     * Matches a method.
     *
     * @param method the method
     * @return true if we have a match
     */
    public boolean matches(final MethodMetaData method) {
        if (!matchMethodName(method.getName())) {
            return false;
        }
        if (!matchReturnType(method.getReturnType())) {
            return false;
        }
        if (!matchParameterTypes(method.getParameterTypes())) {
            return false;
        }
        return true;
    }

    /**
     * Matches a method name.
     *
     * @param methodName the name of the method
     * @return true if we have a match
     */
    public boolean matchMethodName(final String methodName) {
        if (methodName == null) throw new IllegalArgumentException("method name can not be null");
        if (methodName.equals("")) return false;
        return m_methodNamePattern.matcher(methodName).matches();
    }

    /**
     * Matches a method return type.
     *
     * @param  returnType the return type
     * @return true if we have a match
     */
    public boolean matchReturnType(final String returnType) {
        if (returnType == null) throw new IllegalArgumentException("return type class name can not be null");
        if (returnType.equals("")) return false;
        return m_returnTypePattern.matcher(returnType).matches();
    }

    /**
     * Matches a parameter list.
     *
     * @param parameterTypes the parameter types
     * @return true if we have a match
     */
    public boolean matchParameterTypes(final String[] parameterTypes) {
        if (parameterTypes.length == 0 && m_parameterTypePatterns.size() == 0) {
            return true;
        }
        if (parameterTypes.length == 0 && m_parameterTypePatterns.size() != 0 &&
                ((java.util.regex.Pattern)m_parameterTypePatterns.get(0)).
                pattern().equals(MULTIPLE_WILDCARD_KEY)) {
            return true;
        }
        if (parameterTypes.length == 0) {
            return false;
        }

        Iterator it = m_parameterTypePatterns.iterator();
        for (int i = 0; it.hasNext(); i++) {
            java.util.regex.Pattern pattern = (java.util.regex.Pattern)it.next();
            if (pattern.pattern().equals(MULTIPLE_WILDCARD_KEY)) {
                return true;
            }
            if (parameterTypes.length <= i) {
                return false;
            }
            String fullClassName = parameterTypes[i];
            if (fullClassName == null) throw new IllegalArgumentException("parameter class name can not be null");
            if (fullClassName.equals("")) return false;
            pattern.matcher(removePackageFromClassName(fullClassName));
        }
        if (parameterTypes.length == m_parameterTypePatterns.size()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns the pattern as a string.
     *
     * @return the pattern
     */
    public String getPattern() {
        return m_pattern;
    }

    /**
     * Parses the method pattern.
     *
     * @param pattern the method pattern
     */
    protected void parse(final String pattern) {
        m_pattern = pattern;
        try {
            parseReturnTypePattern(pattern);
            parseMethodNamePattern(pattern);
            parserParameterTypesPattern(pattern);
        }
        catch (Exception e) {
            throw new DefinitionException("method pattern is not well formed: " + pattern);
        }
    }

    /**
     * Parses the method name pattern.
     *
     * @param pattern the pattern
     */
    protected void parseMethodNamePattern(final String pattern) {
        final int startIndexMethodName = pattern.indexOf(' ') + 1;
        final int endIndexMethodName = pattern.indexOf('(');
        String methodNamePattern = pattern.
                substring(startIndexMethodName, endIndexMethodName);
        if (methodNamePattern.equals(SINGLE_WILDCARD)) {
            methodNamePattern = "\\b.*\\b";
        }
        else {
            methodNamePattern = methodNamePattern.replaceAll("\\*", "\\.*");
        }
        m_methodNamePattern = java.util.regex.Pattern.compile(methodNamePattern);
    }

    /**
     * Parses the return type pattern.
     *
     * @param pattern the pattern
     */
    protected void parseReturnTypePattern(final String pattern) {
        final int endIndexReturnType = pattern.indexOf(' ');
        String returnTypePattern = pattern.substring(0, endIndexReturnType);
        if (m_abbreviations.containsKey(returnTypePattern)) {
            returnTypePattern = (String)m_abbreviations.get(returnTypePattern);
        }
        if (returnTypePattern.equals(SINGLE_WILDCARD)) {
            returnTypePattern = "\\b.*\\b";
        }
        else {
            returnTypePattern = returnTypePattern.replaceAll("\\.", "\\\\.");
            returnTypePattern = returnTypePattern.replaceAll("\\*", "\\.*");
        }
        m_returnTypePattern = java.util.regex.Pattern.compile(returnTypePattern);
    }

    /**
     * Parse the parameter types pattern.
     *
     * @param pattern the pattern
     */
    protected void parserParameterTypesPattern(final String pattern) {

        final int startIndexParameterTypes = pattern.indexOf('(') + 1;
        final int endIndexParameterTypes = pattern.indexOf(')');
        String parameterTypesPattern = pattern.substring(
                startIndexParameterTypes, endIndexParameterTypes);

        m_parameterTypePatterns = new ArrayList();

        final StringTokenizer tokenizer =
                new StringTokenizer(parameterTypesPattern, ",");

        if (tokenizer.hasMoreTokens()) {
            // if the first parameter is (..) set it and return
            String firstParameter = tokenizer.nextToken().trim();
            if (m_abbreviations.containsKey(firstParameter)) {
                firstParameter = (String)m_abbreviations.get(firstParameter);
            }
            if (firstParameter.equals(SINGLE_WILDCARD)) {
                firstParameter = "\\b.*\\b";
            }
            else if (firstParameter.equals(MULTIPLE_WILDCARD)) {
                firstParameter = MULTIPLE_WILDCARD_KEY;
            }
            else {
                firstParameter = firstParameter.replaceAll("\\.", "\\\\.");
                firstParameter = firstParameter.replaceAll("\\*", "\\.*");
            }
            m_parameterTypePatterns.add(
                    java.util.regex.Pattern.compile(firstParameter));
        }
        // handle the remaining parameters
        while (tokenizer.hasMoreTokens()) {
            String parameter = tokenizer.nextToken().trim();
            if (m_abbreviations.containsKey(parameter)) {
                parameter = (String)m_abbreviations.get(parameter);
            }
            if (parameter.equals(SINGLE_WILDCARD)) {
                parameter = "\\b.*\\b";
            }
            else if (parameter.equals(MULTIPLE_WILDCARD)) {
                parameter = MULTIPLE_WILDCARD_KEY;
            }
            else {
                parameter = parameter.replaceAll("\\.", "\\\\.");
                parameter = parameter.replaceAll("\\*", "\\.*");
            }
            m_parameterTypePatterns.add(
                    java.util.regex.Pattern.compile(parameter));
        }
    }

    /**
     * Private constructor.
     *
     * @param pattern the pattern
     */
    MethodPattern(final String pattern) {
        parse(pattern);
    }
}

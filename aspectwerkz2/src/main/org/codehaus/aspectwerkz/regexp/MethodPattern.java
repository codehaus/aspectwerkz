/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.regexp;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.util.Strings;

import java.io.ObjectInputStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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
 */
public class MethodPattern extends Pattern
{
    /**
     * The method name pattern.
     */
    protected transient com.karneim.util.collection.regex.Pattern m_methodNamePattern;

    /**
     * and list with all the parameter type patterns.
     */
    protected transient List m_parameterTypePatterns;

    /**
     * The return type pattern.
     */
    protected transient com.karneim.util.collection.regex.Pattern m_returnTypePattern;

    /**
     * The full pattern as a string.
     */
    protected String m_pattern;

    /**
     * Private constructor.
     *
     * @param pattern the pattern
     */
    MethodPattern(final String pattern)
    {
        m_pattern = pattern;
        parse(m_pattern);
    }

    /**
     * Matches a member.
     *
     * @param methodMetaData the method metadata
     * @return true if we have a matches
     */
    public boolean matches(final MethodMetaData methodMetaData)
    {
        if (!matchMethodName(methodMetaData.getName()))
        {
            return false;
        }

        if (!matchReturnType(methodMetaData.getReturnType()))
        {
            return false;
        }

        if (!matchParameterTypes(methodMetaData.getParameterTypes()))
        {
            return false;
        }

        return true;
    }

    /**
     * Matches a method name.
     *
     * @param methodName the name of the method
     * @return true if we have a matches
     */
    public boolean matchMethodName(final String methodName)
    {
        if (methodName == null)
        {
            throw new IllegalArgumentException("method name can not be null");
        }

        if (methodName.equals(""))
        {
            return false;
        }

        return m_methodNamePattern.contains(methodName);
    }

    /**
     * Matches a method return type.
     *
     * @param returnType the return type
     * @return true if we have a matches
     */
    public boolean matchReturnType(final String returnType)
    {
        if (returnType == null)
        {
            throw new IllegalArgumentException(
                "return type class name can not be null");
        }

        if (returnType.equals(""))
        {
            return false;
        }

        return m_returnTypePattern.contains(returnType);
    }

    /**
     * Matches a parameter list.
     *
     * @param parameterTypes the parameter types
     * @return true if we have a matches
     */
    public boolean matchParameterTypes(final String[] parameterTypes)
    {
        if ((parameterTypes.length == 0)
            && (m_parameterTypePatterns.size() == 0))
        {
            return true;
        }

        if ((parameterTypes.length == 0)
            && (m_parameterTypePatterns.size() != 0)
            && ((com.karneim.util.collection.regex.Pattern) m_parameterTypePatterns
                .get(0)).getRegEx().equals(MULTIPLE_WILDCARD_KEY))
        {
            return true;
        }

        if (parameterTypes.length == 0)
        {
            return false;
        }

        if (m_parameterTypePatterns.size() > parameterTypes.length)
        {
            return false;
        }

        Iterator it = m_parameterTypePatterns.iterator();

        for (int i = 0; it.hasNext(); i++)
        {
            String fullClassName = parameterTypes[i];

            com.karneim.util.collection.regex.Pattern pattern = (com.karneim.util.collection.regex.Pattern) it
                .next();

            if (pattern.getRegEx().equals(MULTIPLE_WILDCARD_KEY))
            {
                return true;
            }

            if (parameterTypes.length <= i)
            {
                return false;
            }

            if (fullClassName == null)
            {
                throw new IllegalArgumentException(
                    "parameter class name can not be null");
            }

            if (fullClassName.equals(""))
            {
                return false;
            }

            if (!pattern.contains(fullClassName))
            {
                return false;
            }
        }

        if (parameterTypes.length == m_parameterTypePatterns.size())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns the pattern as a string.
     *
     * @return the pattern
     */
    public String getPattern()
    {
        return m_pattern;
    }

    /**
     * Parses the method pattern.
     *
     * @param pattern the method pattern
     */
    protected void parse(final String pattern)
    {
        try
        {
            parseReturnTypePattern(pattern);
            parseMethodNamePattern(pattern);
            parserParameterTypesPattern(pattern);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new DefinitionException("method pattern is not well formed: "
                + pattern, e);
        }
    }

    /**
     * Parses the method name pattern.
     *
     * @param pattern the pattern
     */
    protected void parseMethodNamePattern(final String pattern)
    {
        final int startIndexMethodName = pattern.indexOf(' ') + 1;
        final int endIndexMethodName = pattern.indexOf('(');
        String methodNamePattern = pattern.substring(startIndexMethodName,
                endIndexMethodName);

        if (methodNamePattern.equals(SINGLE_WILDCARD))
        {
            methodNamePattern = "[a-zA-Z0-9_$.]+";
        }
        else
        {
            methodNamePattern = Strings.replaceSubString(methodNamePattern,
                    "*", "[a-zA-Z0-9_$]*");
        }

        m_methodNamePattern = new com.karneim.util.collection.regex.Pattern(methodNamePattern);
    }

    /**
     * Parses the return type pattern.
     *
     * @param pattern the pattern
     */
    protected void parseReturnTypePattern(final String pattern)
    {
        final int endIndexReturnType = pattern.indexOf(' ');
        String returnTypePattern = pattern.substring(0, endIndexReturnType);

        //AW-91:check array type
        int arraySize = 0;

        while (returnTypePattern.endsWith("[]"))
        {
            returnTypePattern = returnTypePattern.substring(0,
                    returnTypePattern.length() - 2);
            arraySize++;
        }

        if (m_abbreviations.containsKey(returnTypePattern))
        {
            returnTypePattern = (String) m_abbreviations.get(returnTypePattern);
        }

        //AW-91:rebuild array types
        for (int i = arraySize; i > 0; i--)
        {
            returnTypePattern += "[]";
        }

        if (returnTypePattern.equals(SINGLE_WILDCARD))
        {
            returnTypePattern = "[a-zA-Z0-9_$.\\[\\]]+";
        }
        else
        {
            returnTypePattern = escapeString(returnTypePattern);
        }

        m_returnTypePattern = new com.karneim.util.collection.regex.Pattern(returnTypePattern);
    }

    /**
     * Parse the parameter types pattern.
     *
     * @param pattern the pattern
     */
    protected void parserParameterTypesPattern(final String pattern)
    {
        final int startIndexParameterTypes = pattern.indexOf('(') + 1;
        final int endIndexParameterTypes = pattern.indexOf(')');
        String parameterTypesPattern = pattern.substring(startIndexParameterTypes,
                endIndexParameterTypes);

        m_parameterTypePatterns = new ArrayList();

        final StringTokenizer tokenizer = new StringTokenizer(parameterTypesPattern,
                ",");

        if (tokenizer.hasMoreTokens())
        {
            // if the first parameter is (..) set it and return
            String firstParameter = tokenizer.nextToken().trim();

            //AW-91:check array type
            int arraySize = 0;

            while (firstParameter.endsWith("[]"))
            {
                firstParameter = firstParameter.substring(0,
                        firstParameter.length() - 2);
                arraySize++;
            }

            if (m_abbreviations.containsKey(firstParameter))
            {
                firstParameter = (String) m_abbreviations.get(firstParameter);
            }

            //AW-91:rebuild array types
            for (int i = arraySize; i > 0; i--)
            {
                firstParameter += "[]";
            }

            if (firstParameter.equals(SINGLE_WILDCARD))
            {
                firstParameter = "[a-zA-Z0-9_$.]+";
            }
            else if (firstParameter.equals(MULTIPLE_WILDCARD))
            {
                firstParameter = MULTIPLE_WILDCARD_KEY;
            }
            else
            {
                firstParameter = escapeString(firstParameter);
            }

            m_parameterTypePatterns.add(new com.karneim.util.collection.regex.Pattern(
                    firstParameter));
        }

        // handle the remaining parameters
        while (tokenizer.hasMoreTokens())
        {
            String parameter = tokenizer.nextToken().trim();

            if (m_abbreviations.containsKey(parameter))
            {
                parameter = (String) m_abbreviations.get(parameter);
            }

            if (parameter.equals(SINGLE_WILDCARD))
            {
                parameter = "[a-zA-Z0-9_$.]+";
            }
            else if (parameter.equals(MULTIPLE_WILDCARD))
            {
                parameter = MULTIPLE_WILDCARD_KEY;
            }
            else
            {
                parameter = escapeString(parameter);
            }

            m_parameterTypePatterns.add(new com.karneim.util.collection.regex.Pattern(
                    parameter));
        }
    }

    /**
     * Escapes the string.
     *
     * @param oldString
     * @return
     */
    protected static String escapeString(final String oldString)
    {
        String escapedString = Strings.replaceSubString(oldString, ".", "\\.");

        escapedString = Strings.replaceSubString(escapedString, "[", "\\[");
        escapedString = Strings.replaceSubString(escapedString, "]", "\\]");
        escapedString = Strings.replaceSubString(escapedString, "*",
                "[a-zA-Z0-9_$]*");

        return escapedString;
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream)
        throws Exception
    {
        ObjectInputStream.GetField fields = stream.readFields();

        m_pattern = (String) fields.get("m_pattern", null);
        parse(m_pattern);
    }

    public int hashCode()
    {
        int result = 17;

        result = (37 * result) + hashCodeOrZeroIfNull(m_pattern);
        result = (37 * result) + hashCodeOrZeroIfNull(m_methodNamePattern);
        result = (37 * result) + hashCodeOrZeroIfNull(m_parameterTypePatterns);
        result = (37 * result) + hashCodeOrZeroIfNull(m_returnTypePattern);
        result = (37 * result) + hashCodeOrZeroIfNull(m_abbreviations);

        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o)
    {
        if (null == o)
        {
            return 19;
        }

        return o.hashCode();
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof MethodPattern))
        {
            return false;
        }

        final MethodPattern obj = (MethodPattern) o;

        return areEqualsOrBothNull(obj.m_pattern, this.m_pattern)
        && areEqualsOrBothNull(obj.m_methodNamePattern, this.m_methodNamePattern)
        && areEqualsOrBothNull(obj.m_parameterTypePatterns,
            this.m_parameterTypePatterns)
        && areEqualsOrBothNull(obj.m_returnTypePattern, this.m_returnTypePattern)
        && areEqualsOrBothNull(obj.m_abbreviations, this.m_abbreviations);
    }

    protected static boolean areEqualsOrBothNull(final Object o1,
        final Object o2)
    {
        if (null == o1)
        {
            return (null == o2);
        }

        return o1.equals(o2);
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;

import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Parses a src tree with <code>QDox</code>.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class QDoxParser {
    /**
     * The QDox builder.
     */
    private JavaDocBuilder m_builder = new JavaDocBuilder();

    /**
     * The parsed java class.
     */
    private JavaClass m_class;

    /**
     * The name of the class.
     */
    private String m_className;

    /**
     * Transforms the QDox JavaMethod parameters to a String array with the types represented as strings.
     * 
     * @param method the JavaMethod
     * @return an array with the types as strings
     */
    public static String[] getJavaMethodParametersAsStringArray(final JavaMethod method) {
        JavaParameter[] javaParameters = method.getParameters();
        String[] parameters = new String[javaParameters.length];
        for (int i = 0; i < javaParameters.length; i++) {
            Type type = javaParameters[i].getType();
            int dimensions = type.getDimensions();
            StringBuffer parameter = new StringBuffer(type.getValue());
            for (int j = 0; j < dimensions; j++) {
                parameter.append("[]");
            }
            parameters[i] = parameter.toString();
        }
        return parameters;
    }

    /**
     * Adds a source tree to the builder.
     * 
     * @param srcDir the source tree
     */
    public QDoxParser(final String srcDir) {
        m_builder.addSourceTree(new File(srcDir));
    }

    /**
     * Parses a specific class.
     * 
     * @param className the name of the class to compile
     * @return true if class was found and false otherwise
     * @todo QDox seems to have a problem retrieving inner classes => null
     */
    public boolean parse(final String className) {
        m_class = m_builder.getClassByName(className);
        if (m_class == null) {
            return false;
        }
        m_className = m_class.getFullyQualifiedName();
        return true;
    }

    /**
     * Returns the QDox JavaClass.
     * 
     * @return the QDox JavaClass
     */
    public JavaClass getJavaClass() {
        if ((m_class == null) && (m_className == null)) {
            throw new DefinitionException("no class has been parsed, call parse(..) first");
        }
        if (m_class == null) {
            throw new DefinitionException("could not find source file for "
                + m_className
                + " (have you specified the correct srcDir)");
        }
        return m_class;
    }

    /**
     * Returns all classes.
     * 
     * @return a collections with all classes
     */
    public String[] getAllClassNames() {
        Collection classes = m_builder.getClassLibrary().all();
        Collection classNames = new ArrayList();
        String className = null;
        for (Iterator it = classes.iterator(); it.hasNext();) {
            className = (String) it.next();
            if ("java.lang.Object".equals(className)) {
                continue;
            }
            classNames.add(className);
        }
        return (String[]) classNames.toArray(new String[] {});
    }

    /**
     * Parses a specific class A returns an array with the methods.
     * 
     * @return an array with the methods
     */
    public JavaMethod[] getJavaMethods() {
        if ((m_class == null) && (m_className == null)) {
            throw new DefinitionException("no class has been parsed, call parse(..) first");
        }
        if (m_class == null) {
            throw new DefinitionException("could not find source file for "
                + m_className
                + " (have you specified the correct srcDir)");
        }
        return m_class.getMethods();
    }

    /**
     * Parses a specific class A returns an array with the methods.
     * 
     * @return an array with the methods
     */
    public JavaField[] getJavaFields() {
        if ((m_class == null) && (m_className == null)) {
            throw new DefinitionException("no class has been parsed, call parse(..) first");
        }
        if (m_class == null) {
            throw new DefinitionException("could not find source file for "
                + m_className
                + " (have you specified the correct srcDir)");
        }
        return m_class.getFields();
    }
}
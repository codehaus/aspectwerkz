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
package org.codehaus.aspectwerkz.metadata;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaField;

import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Parses a src tree with <code>QDox</code>.
 * Usage:
 * <pre>
 *     QDoxParser parser = new QDoxParser("src");
 *     parser.compile("foo.Bar");
 *     JavaMethod[] methods = parser.getJavaMethods();
 *     JavaFields[] fields = parser.getJavaFields();
 * </pre>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: QDoxParser.java,v 1.1 2003-06-17 14:58:31 jboner Exp $
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
     * Adds a source tree to the builder.
     *
     * @param srcDir the source tree to getJavaMethods
     */
    public QDoxParser(final String srcDir) {
        m_builder.addSourceTree(new File(srcDir));
    }

    /**
     * Parses a specific class.
     *
     * @todo QDox seems to have a problem retrieving inner classes => null
     *
     * @param className the name of the class to compile
     * @return true if class was found and false otherwise
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
        if (m_class == null && m_className == null) throw new DefinitionException("no class has been parsed, call parse(..) first");
        if (m_class == null) throw new DefinitionException("could not find source file for " + m_className + " (have you specified the correct srcDir)");
        return m_class;
    }

    /**
     * Returns all classes.
     *
     * @return a collections with all classes
     */
    public String[] getAllClassesNames() {
        Collection classes = m_builder.getClassLibrary().all();
        String[] classNames = new String[classes.size()];
        int i = 0;
        for (Iterator it = classes.iterator(); it.hasNext(); i++) {
            classNames[i] = (String)it.next();
        }
        return classNames;
    }

    /**
     * Parses a specific class and returns an array with the methods.
     *
     * @return an array with the methods
     */
    public JavaMethod[] getJavaMethods() {
        if (m_class == null && m_className == null) throw new DefinitionException("no class has been parsed, call parse(..) first");
        if (m_class == null) throw new DefinitionException("could not find source file for " + m_className + " (have you specified the correct srcDir)");
        return m_class.getMethods();
    }

    /**
     * Parses a specific class and returns an array with the methods.
     *
     * @return an array with the methods
     */
    public JavaField[] getJavaFields() {
        if (m_class == null && m_className == null) throw new DefinitionException("no class has been parsed, call parse(..) first");
        if (m_class == null) throw new DefinitionException("could not find source file for " + m_className + " (have you specified the correct srcDir)");
        return m_class.getFields();
    }
}

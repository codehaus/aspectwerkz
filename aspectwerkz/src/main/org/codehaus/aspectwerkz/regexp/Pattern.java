/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
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
package org.codehaus.aspectwerkz.regexp;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Implements an abstract regular expression pattern matcher for AspectWerkz.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: Pattern.java,v 1.2 2003-06-17 16:07:55 jboner Exp $
 */
public abstract class Pattern implements Serializable {

    /**
     * Defines a single wildcard.
     */
    public static final String SINGLE_WILDCARD = "*";

    /**
     * Defines a multiple wildcard.
     */
    public static final String MULTIPLE_WILDCARD = "..";

    /**
     * Defines a multiple wildcard key.
     */
    public static final String MULTIPLE_WILDCARD_KEY = "MULTIPLE_WILDCARD_KEY";

    /**
     * Abbreviations for all the classes in the java.lang.* and
     * the java.util.* namespaces.
     */
    protected static final Map m_abbreviations = new HashMap();

    /**
     * Compiles and returns a new class pattern.
     *
     * @param pattern the full pattern as a string
     * @return the pattern
     */
    public static ClassPattern compileClassPattern(final String pattern) {
        return new ClassPattern(pattern);
    }

    /**
     * Compiles and returns a new method pattern.
     *
     * @param pattern the full pattern as a string
     * @return the pattern
     */
    public static MethodPattern compileMethodPattern(final String pattern) {
        return new MethodPattern(pattern);
    }

    /**
     * Compiles and returns a new field pattern.
     *
     * @param pattern the full pattern as a string
     * @return the pattern
     */
    public static FieldPattern compileFieldPattern(final String pattern) {
        return new FieldPattern(pattern);
    }

    /**
     * Compiles and returns a new caller side pattern.
     *
     * @param pattern the full pattern as a string
     * @return the pattern
     */
    public static CallerSidePattern compileCallerSidePattern(final String pattern) {
        return new CallerSidePattern(pattern);
    }

    /**
     * Compiles and returns a new throws pattern.
     *
     * @param pattern the full pattern as a string
     * @return the pattern
     */
    public static ThrowsPattern compileThrowsPattern(final String pattern) {
        return new ThrowsPattern(pattern);
    }

    /**
     * Parses the method pattern.
     *
     * @param pattern the method pattern
     */
    protected abstract void parse(final String pattern);

    /**
     * Removes the package from the class name.
     *
     * @param fullClassName the full class name
     * @return the class name without package
     */
    protected String removePackageFromClassName(final String fullClassName) {
        int index = fullClassName.lastIndexOf('.');
        String className = fullClassName.
                substring(index + 1, fullClassName.length());
        return className;
    }

    static {
        // java.lang.*
        m_abbreviations.put("CharSequence", "java.lang.CharSequence");
        m_abbreviations.put("Cloneable", "java.lang.Cloneable");
        m_abbreviations.put("Comparable", "java.lang.Comparable");
        m_abbreviations.put("Runnable", "java.lang.Runnable");
        m_abbreviations.put("Boolean", "java.lang.Boolean");
        m_abbreviations.put("Byte", "java.lang.Byte");
        m_abbreviations.put("Character", "java.lang.Character");
        m_abbreviations.put("Class", "java.lang.Class");
        m_abbreviations.put("ClassLoader", "java.lang.ClassLoader");
        m_abbreviations.put("Compiler", "java.lang.Compiler");
        m_abbreviations.put("Double", "java.lang.Double");
        m_abbreviations.put("Float", "java.lang.Float");
        m_abbreviations.put("InheritableThreadLocal", "java.lang.InheritableThreadLocal");
        m_abbreviations.put("Integer", "java.lang.Integer");
        m_abbreviations.put("Long", "java.lang.Long");
        m_abbreviations.put("Math", "java.lang.Math");
        m_abbreviations.put("Number", "java.lang.Number");
        m_abbreviations.put("Object", "java.lang.Object");
        m_abbreviations.put("Package", "java.lang.Package");
        m_abbreviations.put("Process", "java.lang.Process");
        m_abbreviations.put("Runtime", "java.lang.Runtime");
        m_abbreviations.put("RuntimePermission", "java.lang.RuntimePermission");
        m_abbreviations.put("SecurityManager", "java.lang.SecurityManager");
        m_abbreviations.put("Short", "java.lang.Short");
        m_abbreviations.put("StackTraceElement", "java.lang.StackTraceElement");
        m_abbreviations.put("StrictMath", "java.lang.StrictMath");
        m_abbreviations.put("String", "java.lang.String");
        m_abbreviations.put("StringBuffer", "java.lang.StringBuffer");
        m_abbreviations.put("System", "java.lang.System");
        m_abbreviations.put("Thread", "java.lang.Thread");
        m_abbreviations.put("ThreadGroup", "java.lang.ThreadGroup");
        m_abbreviations.put("ThreadLocal", "java.lang.ThreadLocal");
        m_abbreviations.put("Throwable", "java.lang.Throwable");
        m_abbreviations.put("Void", "java.lang.Void");

        // java.util.*
        m_abbreviations.put("Collection", "java.util.Collection");
        m_abbreviations.put("Comparator", "java.util.Comparator");
        m_abbreviations.put("Enumeration", "java.util.Enumeration");
        m_abbreviations.put("EventListener", "java.util.EventListener");
        m_abbreviations.put("Iterator", "java.util.Iterator");
        m_abbreviations.put("List", "java.util.List");
        m_abbreviations.put("ListIterator", "java.util.ListIterator");
        m_abbreviations.put("Map", "java.util.Map");
        m_abbreviations.put("Map.Entry", "java.util.Map.Entry");
        m_abbreviations.put("Observer", "java.util.Observer");
        m_abbreviations.put("RandomAccess", "java.util.RandomAccess");
        m_abbreviations.put("Set", "java.util.Set");
        m_abbreviations.put("SortedMap", "java.util.SortedMap");
        m_abbreviations.put("SortedSet", "java.util.SortedSet");
        m_abbreviations.put("AbstractCollection", "java.util.AbstractCollection");
        m_abbreviations.put("AbstractList", "java.util.AbstractList");
        m_abbreviations.put("AbstractMap", "java.util.AbstractMap");
        m_abbreviations.put("AbstractSequentialList ", "java.util.AbstractSequentialList");
        m_abbreviations.put("AbstractSet", "java.util.AbstractSet");
        m_abbreviations.put("ArrayList", "java.util.ArrayList");
        m_abbreviations.put("Arrays", "java.util.Arrays");
        m_abbreviations.put("BitSet", "java.util.BitSet");
        m_abbreviations.put("Calendar", "java.util.Calendar");
        m_abbreviations.put("Collections", "java.util.Collections");
        m_abbreviations.put("Currency", "java.util.Currency");
        m_abbreviations.put("Date", "java.util.Date");
        m_abbreviations.put("Dictionary", "java.util.Dictionary");
        m_abbreviations.put("EventListenerProxy", "java.util.EventListenerProxy");
        m_abbreviations.put("EventObject", "java.util.EventObject");
        m_abbreviations.put("GregorianCalender", "java.util.GregorianCalender");
        m_abbreviations.put("HashMap", "java.util.HashMap");
        m_abbreviations.put("HashSet", "java.util.HashSet");
        m_abbreviations.put("Hashtable", "java.util.Hashtable");
        m_abbreviations.put("IdentityHashMap", "java.util.IdentityHashMap");
        m_abbreviations.put("LinkedHashMap", "java.util.LinkedHashMap");
        m_abbreviations.put("LinkedHashSet", "java.util.LinkedHashSet");
        m_abbreviations.put("LinkedList", "java.util.LinkedList");
        m_abbreviations.put("ListResourceBundle", "java.util.ListResourceBundle");
        m_abbreviations.put("Locale", "java.util.Locale");
        m_abbreviations.put("Observable", "java.util.Observable");
        m_abbreviations.put("Properties", "java.util.Properties");
        m_abbreviations.put("PropertyPermission", "java.util.PropertyPermission");
        m_abbreviations.put("PropertyResourceBundle", "java.util.PropertyResourceBundle");
        m_abbreviations.put("Random", "java.util.Random");
        m_abbreviations.put("ResourceBundle", "java.util.ResourceBundle");
        m_abbreviations.put("SimpleTimeZone", "java.util.SimpleTimeZone");
        m_abbreviations.put("Stack", "java.util.Stack");
        m_abbreviations.put("StringTokenizer", "java.util.StringTokenizer");
        m_abbreviations.put("Timer", "java.util.Timer");
        m_abbreviations.put("TimerTask", "java.util.TimerTask");
        m_abbreviations.put("TimeZone", "java.util.TimeZone");
        m_abbreviations.put("TreeMap", "java.util.TreeMap");
        m_abbreviations.put("TreeSet", "java.util.TreeSet");
        m_abbreviations.put("Vector", "java.util.Vector");
        m_abbreviations.put("WeakHashMap", "java.util.WeakHashMap");
    }
}

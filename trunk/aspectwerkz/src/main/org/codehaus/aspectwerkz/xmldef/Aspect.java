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
package org.codehaus.aspectwerkz;

import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import gnu.trove.THashMap;

import org.codehaus.aspectwerkz.pointcut.FieldPointcut;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.pointcut.ThrowsPointcut;
import org.codehaus.aspectwerkz.pointcut.ThrowsPointcutKey;
import org.codehaus.aspectwerkz.pointcut.CallerSidePointcut;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.definition.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.definition.regexp.MethodPattern;
import org.codehaus.aspectwerkz.definition.regexp.FieldPattern;
import org.codehaus.aspectwerkz.definition.regexp.ClassPattern;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;

/**
 * Implements the Aspect concept.<br/>
 * Stores method-based pointcuts, field-based pointcuts and introductions
 * for a specific class.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: Aspect.java,v 1.1.1.1 2003-05-11 15:13:27 jboner Exp $
 */
public class Aspect {

    /**
     * Holds references to all the the method pointcuts in the system.
     */
    protected final Map m_methodPointcuts = new THashMap();

    /**
     * Holds references to all the the getField pointcuts in the system.
     */
    protected final Map m_getFieldPointcuts = new THashMap();

    /**
     * Holds references to all the the setField pointcuts in the system.
     */
    protected final Map m_setFieldPointcuts = new THashMap();

    /**
     * Holds references to all the the throws pointcuts in the system.
     */
    protected final Map m_throwsPointcuts = new THashMap();

    /**
     * Holds references to all the the call side pointcuts in the system.
     */
    protected final Map m_callerSidePointcuts = new THashMap();

    /**
     * Holds references to all the the introductions in the system.
     */
    protected String[] m_introductions = new String[0];

    /**
     * Holds the pattern string for the aspect.
     */
    protected final String m_pattern;

    /**
     * Holds the class pattern for the advice.
     */
    protected final ClassPattern m_classPattern;

    /**
     * Marks the aspect as read-only.
     */
    protected boolean m_readOnly = false;

    /**
     * Creates a new aspect.
     *
     * @param classPattern the pattern for the aspect as a ClassPattern
     */
    public Aspect(final ClassPattern classPattern) {
        if (classPattern == null) throw new IllegalArgumentException("class pattern can not be null");
        m_classPattern = classPattern;
        m_pattern = classPattern.getPattern();
    }

    /**
     * Creates a new aspect.
     *
     * @param className the name of the class
     */
    public Aspect(final String classPattern) {
        if (classPattern == null) throw new IllegalArgumentException("class pattern can not be null");
        m_classPattern = Pattern.compileClassPattern(classPattern);
        m_pattern = classPattern;
    }

    /**
     * Creates a new pointcut for the method join point specified.
     *
     * @param methodPattern the method pattern
     * @return the pointcut
     */
    public MethodPointcut createMethodPointcut(final String methodPattern) {
        return createMethodPointcut(methodPattern, true);
    }

    /**
     * Creates a new pointcut for the method join point specified.
     *
     * @param methodPattern the method pattern
     * @param isThreadSafe the thread safe type
     * @return the pointcut
     */
    public MethodPointcut createMethodPointcut(final String methodPattern,
                                               final boolean isThreadSafe) {
        if (methodPattern == null) throw new IllegalArgumentException("method pattern can not be null");
        synchronized (m_methodPointcuts) {
            if (m_methodPointcuts.containsKey(methodPattern)) {
                return (MethodPointcut)m_methodPointcuts.get(methodPattern);
            }
            else {
                if (isReadOnly()) throw new IllegalStateException("aspect is read-only");
                final MethodPointcut pointcut =
                        new MethodPointcut(methodPattern, isThreadSafe);
                m_methodPointcuts.put(methodPattern, pointcut);
                return pointcut;
            }
        }
    }

    /**
     * Creates a new pointcut for the field specified.
     *
     * @param fieldPattern the field pattern
     * @return the pointcut
     */
    public FieldPointcut createGetFieldPointcut(final String fieldPattern) {
        return createGetFieldPointcut(fieldPattern, true);
    }

    /**
     * Creates a new pointcut for the field specified.
     *
     * @param fieldPattern the field pattern
     * @param isThreadSafe the thread safe type
     * @return the pointcut
     */
    public FieldPointcut createGetFieldPointcut(final String fieldPattern,
                                                final boolean isThreadSafe) {
        if (fieldPattern == null) throw new IllegalArgumentException("field pattern can not be null");
        synchronized (m_getFieldPointcuts) {
            if (m_getFieldPointcuts.containsKey(fieldPattern)) {
                return (FieldPointcut)m_getFieldPointcuts.get(fieldPattern);
            }
            else {
                if (isReadOnly()) throw new IllegalStateException("aspect repository is read-only");
                final FieldPointcut poincut =
                        new FieldPointcut(fieldPattern, isThreadSafe);

                m_getFieldPointcuts.put(fieldPattern, poincut);
                return poincut;
            }
        }
    }

    /**
     * Creates a new pointcut for the set field join point specified.
     *
     * @param fieldPattern the field pattern
     * @return the pointcut
     */
    public FieldPointcut createSetFieldPointcut(final String fieldPattern) {
        return createSetFieldPointcut(fieldPattern, true);
    }

    /**
     * Creates a new pointcut for the set field join point specified.
     *
     * @param fieldPattern the field pattern
     * @param isThreadSafe the thread safe type
     * @return the pointcut
     */
    public FieldPointcut createSetFieldPointcut(final String fieldPattern,
                                                final boolean isThreadSafe) {
        if (fieldPattern == null) throw new IllegalArgumentException("field pattern can not be null");
        synchronized (m_setFieldPointcuts) {
            if (m_setFieldPointcuts.containsKey(fieldPattern)) {
                return (FieldPointcut)m_setFieldPointcuts.get(fieldPattern);
            }
            else {
                if (isReadOnly()) throw new IllegalStateException("aspect repository is read-only");
                final FieldPointcut poincut =
                        new FieldPointcut(fieldPattern, isThreadSafe);
                m_setFieldPointcuts.put(fieldPattern, poincut);
                return poincut;
            }
        }
    }

    /**
     * Creates a new throws pointcut for the method and exception join point specified.
     *
     * @param throwsPattern the throws pattern
     * @return the pointcut
     */
    public ThrowsPointcut createThrowsPointcut(final String throwsPattern) {
        return createThrowsPointcut(throwsPattern, true);
    }

    /**
     * Creates a new throws pointcut for the method and exception join point specified.
     *
     * @param throwsPattern the throws pattern
     * @param isThreadSafe the thread safe type
     * @return the pointcut
     */
    public ThrowsPointcut createThrowsPointcut(final String throwsPattern,
                                               final boolean isThreadSafe) {
        if (throwsPattern == null) throw new IllegalArgumentException("throws pattern can not be null");
        synchronized (m_throwsPointcuts) {
            if (m_throwsPointcuts.containsKey(throwsPattern)) {
                return (ThrowsPointcut)m_throwsPointcuts.get(throwsPattern);
            }
            else {
                if (isReadOnly()) throw new IllegalStateException("aspect repository is read-only");
                final ThrowsPointcut pointcut =
                        new ThrowsPointcut(throwsPattern, isThreadSafe);
                m_throwsPointcuts.put(throwsPattern, pointcut);
                return pointcut;
            }
        }
    }

    /**
     * Creates a new call side pointcut for the method specified.
     *
     * @param methodPattern the pattern for the method
     * @return the pointcut
     */
    public CallerSidePointcut createCallSidePointcut(final String methodPattern) {
        return createCallerSidePointcut(methodPattern, true);
    }

    /**
     * Creates a new call side pointcut for the method specified.
     *
     * @param methodPattern the pattern for the method
     * @param isThreadSafe the thread safe type
     * @return the pointcut
     */
    public CallerSidePointcut createCallerSidePointcut(final String methodPattern,
                                                   final boolean isThreadSafe) {
        if (methodPattern == null) throw new IllegalArgumentException("method pattern can not be null");
        synchronized (m_callerSidePointcuts) {
            if (m_callerSidePointcuts.containsKey(methodPattern)) {
                return (CallerSidePointcut)m_callerSidePointcuts.get(methodPattern);
            }
            else {
                if (isReadOnly()) throw new IllegalStateException("aspect repository is read-only");
                final CallerSidePointcut pointcut =
                        new CallerSidePointcut(methodPattern, isThreadSafe);
                m_callerSidePointcuts.put(methodPattern, pointcut);
                return pointcut;
            }
        }
    }

    /**
     * Adds an introduction to the open class.
     *
     * @param introduction the name of the introduction to add
     */
    public void addIntroduction(final String introduction) {
        synchronized (m_introductions) {
            final String[] tmp = new String[m_introductions.length + 1];
            System.arraycopy(m_introductions, 0, tmp, 0, m_introductions.length);
            tmp[m_introductions.length] = introduction;
            m_introductions = new String[m_introductions.length + 1];
            System.arraycopy(tmp, 0, m_introductions, 0, tmp.length);
        }
    }

    /**
     * Adds an array with introductions to the open class.<br/>
     * Makes a defensive copy.
     *
     * @param introductions the introductions to add
     */
    public void addIntroductions(final String[] introductions) {
        synchronized (m_introductions) {
            final String[] clone = new String[introductions.length];
            System.arraycopy(introductions, 0, clone, 0, introductions.length);
            final String[] tmp = new String[m_introductions.length + introductions.length];
            int i;
            for (i = 0; i < m_introductions.length; i++) {
                tmp[i] = m_introductions[i];
            }
            for (int j = 0; j < clone.length; i++, j++) {
                tmp[i] = clone[j];
            }
            m_introductions = new String[tmp.length];
            System.arraycopy(tmp, 0, m_introductions, 0, tmp.length);
        }
    }

    /**
     * Adds a new method pointcut to the class.
     *
     * @param methodPointcut the method pointcut to add
     */
    public void addMethodPointcut(final MethodPointcut methodPointcut) {
        synchronized (m_methodPointcuts) {
            m_methodPointcuts.put(methodPointcut.getName(), methodPointcut);
        }
    }

    /**
     * Adds a new method pointcuts to the class.
     *
     * @param methodPointcuts an arrayt with the method pointcut to add
     */
    public void addMethodPointcuts(final MethodPointcut[] methodPointcuts) {
        synchronized (m_methodPointcuts) {
            for (int i = 0; i < methodPointcuts.length; i++) {
                m_methodPointcuts.put(
                        methodPointcuts[i].getName(), methodPointcuts[i]);
            }
        }
    }

    /**
     * Adds a new throws pointcut to the class.
     *
     * @param throwsPointcut the throws pointcut to add
     */
    public void addThrowsPointcut(final ThrowsPointcut throwsPointcut) {
        synchronized (m_throwsPointcuts) {
            m_throwsPointcuts.put(throwsPointcut.getName(), throwsPointcut);
        }
    }

    /**
     * Adds an array with new throws pointcut to the class.
     *
     * @param throwsPointcuts the throws pointcuts to add
     */
    public void addThrowsPointcuts(final ThrowsPointcut[] throwsPointcuts) {
        synchronized (m_throwsPointcuts) {
            for (int i = 0; i < throwsPointcuts.length; i++) {
                m_throwsPointcuts.put(
                        throwsPointcuts[i].getName(), throwsPointcuts[i]);
            }
        }
    }

    /**
     * Adds a new get field pointcut to the class.
     *
     * @param fieldPointcut the field pointcut to add
     */
    public void addGetFieldPointcut(final FieldPointcut fieldPointcut) {
        synchronized (m_getFieldPointcuts) {
            m_getFieldPointcuts.put(fieldPointcut.getName(), fieldPointcut);
        }
    }

    /**
     * Adds an array with new get field pointcut to the class.
     *
     * @param fieldPointcuts the field pointcuts to add
     */
    public void addGetFieldPointcuts(final FieldPointcut[] fieldPointcuts) {
        synchronized (m_getFieldPointcuts) {
            for (int i = 0; i < fieldPointcuts.length; i++) {
                m_getFieldPointcuts.put(
                        fieldPointcuts[i].getName(), fieldPointcuts[i]);
            }
        }
    }

    /**
     * Adds a new set field pointcut to the class.
     *
     * @param fieldPointcut the field pointcut to add
     */
    public void addSetFieldPointcut(final FieldPointcut fieldPointcut) {
        synchronized (m_setFieldPointcuts) {
            m_setFieldPointcuts.put(fieldPointcut.getName(), fieldPointcut);
        }
    }

    /**
     * Adds an array with new set field pointcut to the class.
     *
     * @param fieldPointcuts the field pointcuts to add
     */
    public void addSetFieldPointcuts(final FieldPointcut[] fieldPointcuts) {
        synchronized (m_setFieldPointcuts) {
            for (int i = 0; i < fieldPointcuts.length; i++) {
                m_setFieldPointcuts.put(
                        fieldPointcuts[i].getName(), fieldPointcuts[i]);
            }
        }
    }

    /**
     * Returns the introductions for the open class.
     *
     * @return an array with the introductions for the class
     */
    public String[] getIntroductions() {
        return m_introductions;
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param methodMetaData the meta-data for the method
     * @return the pointcuts
     */
    public MethodPointcut[] getMethodPointcuts(final MethodMetaData methodMetaData) {
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        List pointcutList = new ArrayList();
        for (Iterator it = m_methodPointcuts.values().iterator(); it.hasNext();) {
            final MethodPointcut pointcut = (MethodPointcut)it.next();
            MethodPattern pattern = pointcut.getPattern();
            if (pattern.matches(methodMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return getMethodPointcutsAsArray(pointcutList, methodMetaData.getName());
    }

    /**
     * Returns the pointcut for the method join point specified.
     *
     * @param methodPattern the pattern for the method
     * @return the pointcut
     */
    public MethodPointcut getMethodPointcut(final String methodPattern) {
        if (methodPattern == null) throw new IllegalArgumentException("method pattern can not be null");
        if (m_methodPointcuts.containsKey(methodPattern)) {
            return (MethodPointcut)m_methodPointcuts.get(methodPattern);
        }
        else {
            throw new DefinitionException(
                    createNoMethodPointcutsMessage(methodPattern));
        }
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param fieldMetaData the meta-data for the field
     * @return the pointcuts
     */
    public FieldPointcut[] getGetFieldPointcuts(final FieldMetaData fieldMetaData) {
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");
        List pointcutList = new ArrayList();
        for (Iterator it = m_getFieldPointcuts.values().iterator(); it.hasNext();) {
            final FieldPointcut pointcut = (FieldPointcut)it.next();
            FieldPattern pattern = pointcut.getPattern();
            if (pattern.matches(fieldMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return getGetFieldPointcutsAsArray(pointcutList, fieldMetaData.getName());
    }

    /**
     * Returns the pointcut for the method join point specified.
     *
     * @param fieldPattern the pattern for the field
     * @return the pointcut
     */
    public FieldPointcut getGetFieldPointcut(final String fieldPattern) {
        if (fieldPattern == null) throw new IllegalArgumentException("field pattern can not be null");
        if (m_getFieldPointcuts.containsKey(fieldPattern)) {
            return (FieldPointcut)m_getFieldPointcuts.get(fieldPattern);
        }
        else {
            throw new DefinitionException(
                    createNoGetFieldPointcutsMessage(fieldPattern));
        }
    }

    /**
     * Returns all the pointcuts for the method join point specified.
     *
     * @param fieldMetaData the meta-data for the field
     * @return the pointcuts
     */
    public FieldPointcut[] getSetFieldPointcuts(final FieldMetaData fieldMetaData) {
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");
        List pointcutList = new ArrayList();
        for (Iterator it = m_setFieldPointcuts.values().iterator(); it.hasNext();) {
            final FieldPointcut pointcut = (FieldPointcut)it.next();
            FieldPattern pattern = pointcut.getPattern();
            if (pattern.matches(fieldMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return getSetFieldPointcutsAsArray(pointcutList, fieldMetaData.getName());
    }

    /**
     * Returns the pointcut for the method join point specified.
     *
     * @param fieldPattern the pattern for the field
     * @return the pointcut
     */
    public FieldPointcut getSetFieldPointcut(final String fieldPattern) {
        if (fieldPattern == null) throw new IllegalArgumentException("field pattern can not be null");
        if (m_setFieldPointcuts.containsKey(fieldPattern)) {
            return (FieldPointcut)m_setFieldPointcuts.get(fieldPattern);
        }
        else {
            throw new DefinitionException(
                    createNoSetFieldPointcutsMessage(fieldPattern));
        }
    }

    /**
     * Returns all the pointcuts for the call side join point specified.
     *
     * @param methodMetaData the meta-data for the method
     * @return the pointcuts
     */
    public CallerSidePointcut[] getCallerSidePointcuts(final MethodMetaData methodMetaData) {
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        List pointcutList = new ArrayList();
        for (Iterator it = m_callerSidePointcuts.values().iterator(); it.hasNext();) {
            final CallerSidePointcut pointcut = (CallerSidePointcut)it.next();
            MethodPattern pattern = pointcut.getPattern();
            if (pattern.matches(methodMetaData)) {
                pointcutList.add(pointcut);
            }
        }
        return getCallSidePointcutsAsArray(pointcutList, methodMetaData.getName());
    }

    /**
     * Returns all the pointcuts for the call side join point specified.
     *
     * @param methodPattern the pattern for the method
     * @return the pointcuts
     */
    public CallerSidePointcut getCallerSidePointcut(final String methodPattern) {
        if (methodPattern == null) throw new IllegalArgumentException("method pattern can not be null");
        if (m_callerSidePointcuts.containsKey(methodPattern)) {
            return (CallerSidePointcut)m_callerSidePointcuts.get(methodPattern);
        }
        else {
            throw new DefinitionException(
                    createNoCallSidePointcutsMessage(methodPattern));
        }
    }

    /**
     * Returns the pointcut for the method/exception join point specified.
     *
     * @param methodMetaData the method meta-data
     * @param exception the name of the exception class
     * @return the pointcut
     */
    public ThrowsPointcut[] getThrowsPointcuts(final MethodMetaData methodMetaData,
                                               final String exception) {
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        if (exception == null) throw new IllegalArgumentException("exception class name can not be null");

        List pointcutList = new ArrayList();
        for (Iterator it = m_throwsPointcuts.values().iterator(); it.hasNext();) {
            final ThrowsPointcut pointcut = (ThrowsPointcut)it.next();
            MethodPattern methodPattern = pointcut.getMethodPattern();
            ClassPattern classPattern = pointcut.getExceptionPattern();
            if (methodPattern.matches(methodMetaData) &&
                    classPattern.matches(exception)) {
                pointcutList.add(pointcut);
            }
        }
        return getThrowsPointcutsAsArray(
                pointcutList, methodMetaData.getName(), exception);
    }

    /**
     * Returns the pointcut for the method/exception join point specified.
     *     *
     * @param throwsPattern the method pattern
     * @return the pointcut
     */
    public ThrowsPointcut getThrowsPointcut(final String throwsPattern) {
        if (throwsPattern == null) throw new IllegalArgumentException("throws pattern can not be null");
        if (m_throwsPointcuts.containsKey(throwsPattern)) {
            return (ThrowsPointcut)m_throwsPointcuts.get(throwsPattern);
        }
        else {
            throw new DefinitionException(
                    createNoThrowsPointcutsMessage(throwsPattern));
        }
    }

    /**
     * Returns a list with all the throws pointcuts for the method specified.
     *
     * @param methodPattern the pattern for the method
     * @return a list with all the throws pointcuts for the method
     */
    public List getThrowsPointcutsForMethod(final String methodPattern) {
        if (methodPattern == null) throw new IllegalArgumentException("method pattern can not be null");
        final List pointcuts = new ArrayList();
        for (Iterator it = m_throwsPointcuts.values().iterator(); it.hasNext();) {
            ThrowsPointcutKey key = (ThrowsPointcutKey)it.next();
            if (key.getMethodName().equals(methodPattern)) {
                pointcuts.add(m_throwsPointcuts.get(key));
            }
        }
        if (pointcuts.isEmpty()) {
            throw new DefinitionException(
                    createNoThrowsPointcutsMessage(methodPattern));
        }
        else {
            return pointcuts;
        }
    }

    /**
     * Checks if a specific method join point has a pointcut configured.
     *
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    public boolean hasMethodPointcut(final MethodMetaData methodMetaData) {
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        for (Iterator it = m_methodPointcuts.values().iterator(); it.hasNext();) {
            MethodPattern pattern = ((MethodPointcut)it.next()).getPattern();
            if (pattern.matches(methodMetaData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a specific method/exceptoin join point has a specific throws
     * pointcut configured.
     *
     * @param methodName the name pattern of the method
     * @param exceptionName the name pattern of the exception
     * @return boolean
     */
    public boolean hasThrowsPointcut(final String methodName,
                                     final String exceptionName) {
        if (methodName == null) throw new IllegalArgumentException("method pattern can not be null");
        if (exceptionName == null) throw new IllegalArgumentException("exception pattern can not be null");
        for (Iterator it = m_throwsPointcuts.values().iterator(); it.hasNext();) {
            final ThrowsPointcut pointcut = (ThrowsPointcut)it.next();
            MethodPattern methodPattern = pointcut.getMethodPattern();
            ClassPattern exceptionPattern = pointcut.getExceptionPattern();
            if (methodPattern.matchMethodName(methodName) && exceptionPattern.matches(exceptionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a specific method/exceptoin join point has a specific throws
     * pointcut configured.
     *
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    public boolean hasThrowsPointcut(final MethodMetaData methodMetaData) {
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        for (Iterator it = m_throwsPointcuts.values().iterator(); it.hasNext();) {
            final ThrowsPointcut pointcut = (ThrowsPointcut)it.next();
            MethodPattern methodPattern = pointcut.getMethodPattern();
            if (methodPattern.matches(methodMetaData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the aspect has any introductions configured.
     *
     * @return boolean
     */
    public boolean hasIntroductions() {
        if (m_introductions.length == 0) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Returns the name of the class defined in the repository.
     *
     * @return the class name
     */
    public String getPattern() {
        return m_pattern;
    }

    /**
     * Returns the pre-compiled class pattern for the aspect.
     *
     * @return the class pattern
     */
    public ClassPattern getClassPattern() {
        return m_classPattern;
    }

    /**
     * Marks the repository read-only.
     */
    public void markReadOnly() {
        m_readOnly = true;
    }

    /**
     * Checks if the repository is marked as read-only.
     *
     * @return boolean
     */
    public boolean isReadOnly() {
        return m_readOnly;
    }

    /**
     * Creates and returns an array with method pointcuts.
     *
     * @param pointcutList a list with the pointcuts
     * @param methodName the name of the method
     * @return the pointcuts
     * @throws DefinitionException if the list is empty
     */
    protected MethodPointcut[] getMethodPointcutsAsArray(
            final List pointcutList,
            final String methodName) {
//        if (pointcutList.isEmpty()) {
//            throw new DefinitionException(
//                    createNoMethodPointcutsMessage(methodName));
//        }
        MethodPointcut[] pointcuts = new MethodPointcut[pointcutList.size()];
        int i = 0;
        for (Iterator it = pointcutList.iterator(); it.hasNext(); i++) {
            pointcuts[i] = (MethodPointcut)it.next();
        }
        return pointcuts;
    }

    /**
     * Creates and returns an array with field pointcuts.
     *
     * @param pointcutList a list with the pointcuts
     * @param fieldName the name of the field
     * @return the pointcuts
     * @throws DefinitionException if the list is empty
     */
    protected FieldPointcut[] getGetFieldPointcutsAsArray(
            final List pointcutList,
            final String fieldName) {
//        if (pointcutList.isEmpty()) {
//            throw new DefinitionException(
//                    createNoGetFieldPointcutsMessage(fieldName));
//        }
        FieldPointcut[] pointcuts = new FieldPointcut[pointcutList.size()];
        int i = 0;
        for (Iterator it = pointcutList.iterator(); it.hasNext(); i++) {
            pointcuts[i] = (FieldPointcut)it.next();
        }
        return pointcuts;
    }

    /**
     * Creates and returns an array with field pointcuts.
     *
     * @param pointcutList a list with the pointcuts
     * @param fieldName the name of the field
     * @return the pointcuts
     * @throws DefinitionException if the list is empty
     */
    protected FieldPointcut[] getSetFieldPointcutsAsArray(
            final List pointcutList,
            final String fieldName) {
//        if (pointcutList.isEmpty()) {
//            throw new DefinitionException(
//                    createNoSetFieldPointcutsMessage(fieldName));
//        }
        FieldPointcut[] pointcuts = new FieldPointcut[pointcutList.size()];
        int i = 0;
        for (Iterator it = pointcutList.iterator(); it.hasNext(); i++) {
            pointcuts[i] = (FieldPointcut)it.next();
        }
        return pointcuts;
    }

    /**
     * Creates and returns an array with method pointcuts.
     *
     * @param pointcutList a list with the pointcuts
     * @param methodName the name of the method
     * @return the pointcuts
     * @throws DefinitionException if the list is empty
     */
    protected CallerSidePointcut[] getCallSidePointcutsAsArray(
            final List pointcutList,
            final String methodName) {
//        if (pointcutList.isEmpty()) {
//            throw new DefinitionException(
//                    createNoCallSidePointcutsMessage(methodName));
//        }
        CallerSidePointcut[] pointcuts = new CallerSidePointcut[pointcutList.size()];
        int i = 0;
        for (Iterator it = pointcutList.iterator(); it.hasNext(); i++) {
            pointcuts[i] = (CallerSidePointcut)it.next();
        }
        return pointcuts;
    }

    /**
     * Creates and returns an array with method pointcuts.
     *
     * @param pointcutList a list with the pointcuts
     * @param methodName the name of the method
     * @param exception the name of the exception
     * @return the pointcuts
     * @throws DefinitionException if the list is empty
     */
    protected ThrowsPointcut[] getThrowsPointcutsAsArray(
            final List pointcutList,
            final String methodName,
            final String exception) {
//        if (pointcutList.isEmpty()) {
//            throw new DefinitionException(
//                    createNoThrowsPointcutsMessage(methodName + "#" + exception));
//        }
        ThrowsPointcut[] pointcuts = new ThrowsPointcut[pointcutList.size()];
        int i = 0;
        for (Iterator it = pointcutList.iterator(); it.hasNext(); i++) {
            pointcuts[i] = (ThrowsPointcut)it.next();
        }
        return pointcuts;
    }

    /**
     * Creates a no method pointcuts message.
     *
     * @param methodName the pattern
     * @return the message
     */
    private String createNoMethodPointcutsMessage(final String methodName) {
        StringBuffer cause = new StringBuffer();
        cause.append(getPattern());
        cause.append('#');
        cause.append(methodName);
        cause.append(" does not have any method pointcuts defined");
        return cause.toString();
    }

    /**
     * Creates a no getfield pointcuts message.
     *
     * @param fieldName the pattern
     * @return the message
     */
    private String createNoGetFieldPointcutsMessage(final String fieldName) {
        StringBuffer cause = new StringBuffer();
        cause.append(getPattern());
        cause.append('#');
        cause.append(fieldName);
        cause.append(" does not have any getfield pointcuts defined");
        return cause.toString();
    }

    /**
     * Creates a no setfield pointcuts message.
     *
     * @param fieldName the pattern
     * @return the message
     */
    private String createNoSetFieldPointcutsMessage(final String fieldName) {
        StringBuffer cause = new StringBuffer();
        cause.append(getPattern());
        cause.append('#');
        cause.append(fieldName);
        cause.append(" does not have any setfield pointcuts defined");
        return cause.toString();
    }

    /**
     * Creates a no callside pointcuts message.
     *
     * @param methodName the pattern
     * @return the message
     */
    private String createNoCallSidePointcutsMessage(final String methodName) {
        StringBuffer cause = new StringBuffer();
        cause.append(getPattern());
        cause.append('#');
        cause.append(methodName);
        cause.append(" does not have any callside pointcuts defined");
        return cause.toString();
    }

    /**
     * Creates a no throws pointcuts message.
     *
     * @param throwsPattern the pattern
     * @return the message
     */
    protected String createNoThrowsPointcutsMessage(final String throwsPattern) {
        StringBuffer cause = new StringBuffer();
        cause.append(getPattern());
        cause.append('#');
        cause.append(throwsPattern);
        cause.append(" does not have any throws pointcuts defined");
        return cause.toString();
    }

// --- over-ridden methods ---

    public String toString() {
        return "["
                + super.toString()
                + ": "
                + "," + m_pattern
                + "," + m_introductions
                + "," + m_methodPointcuts
                + "," + m_getFieldPointcuts
                + "," + m_setFieldPointcuts
                + "," + m_callerSidePointcuts
                + "," + m_readOnly
                + "]";
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_pattern);
        result = 37 * result + hashCodeOrZeroIfNull(m_introductions);
        result = 37 * result + hashCodeOrZeroIfNull(m_methodPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_getFieldPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_setFieldPointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_callerSidePointcuts);
        result = 37 * result + (m_readOnly ? 0 : 1);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Aspect)) return false;
        final Aspect obj = (Aspect)o;
        return areEqualsOrBothNull(obj.m_methodPointcuts, this.m_methodPointcuts)
                && areEqualsOrBothNull(obj.m_getFieldPointcuts, this.m_getFieldPointcuts)
                && areEqualsOrBothNull(obj.m_setFieldPointcuts, this.m_setFieldPointcuts)
                && areEqualsOrBothNull(obj.m_introductions, this.m_introductions)
                && areEqualsOrBothNull(obj.m_pattern, this.m_pattern)
                && areEqualsOrBothNull(obj.m_callerSidePointcuts, this.m_callerSidePointcuts)
                && obj.m_readOnly == this.m_readOnly;
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }
}

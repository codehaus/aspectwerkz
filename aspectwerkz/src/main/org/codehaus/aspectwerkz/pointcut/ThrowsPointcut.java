/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
package org.codehaus.aspectwerkz.pointcut;

import java.util.Iterator;
import java.util.Map;
import java.util.List;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.regexp.ThrowsPattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.InterfaceMetaData;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;

/**
 * Implements the pointcut concept for exception handling.
 * Is an abstraction of a well defined point of execution in the program.<br/>
 * Could matches one or many points as long as they are well defined.<br/>
 * Stores the advices for this specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ThrowsPointcut.java,v 1.9 2003-07-22 14:03:18 jboner Exp $
 */
public class ThrowsPointcut extends AbstractPointcut {

    /**
     * Creates a new throws pointcut.
     *
     * @param pattern the pattern
     */
    public ThrowsPointcut(final String pattern) {
        this(AspectWerkz.DEFAULT_SYSTEM, pattern);
    }

    /**
     * Creates a new throws pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param pattern the pattern
     */
    public ThrowsPointcut(final String uuid, final String pattern) {
        super(uuid, pattern);
    }

    /**
     * Adds a new pointcut definition.
     *
     * @param pointcut the pointcut definition
     */
    public void addPointcutDef(final PointcutDefinition pointcut) {
        m_pointcutPatterns.put(pointcut.getName(),
                new PointcutPatternTuple(
                        pointcut.getRegexpClassPattern(),
                        pointcut.getRegexpPattern(),
                        pointcut.isHierarchical()));
    }

    /**
     * Checks if the pointcut matches a certain join point.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return boolean
     */
    public boolean matches(final ClassMetaData classMetaData,
                           final MethodMetaData methodMetaData) {
        try {
            JexlContext jexlContext = JexlHelper.createContext();

            matchPointcutPatterns(jexlContext, classMetaData, methodMetaData);

            // evaluate expression
            Boolean result = (Boolean)m_jexlExpr.evaluate(jexlContext);
            if (result == null || !result.booleanValue()) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Checks if the pointcut matches a certain join point.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param exception the name of the exception
     * @return boolean
     */
    public boolean matches(final ClassMetaData classMetaData,
                           final MethodMetaData methodMetaData,
                           final String exception) {
        try {
            JexlContext jexlContext = JexlHelper.createContext();

            matchesPointcutPatterns(jexlContext, classMetaData, methodMetaData, exception);

            // evaluate expression
            Boolean result = (Boolean)m_jexlExpr.evaluate(jexlContext);
            if (result == null || !result.booleanValue()) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Matches the method pointcut patterns.
     *
     * @param jexlContext the Jexl context
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     */
    private void matchPointcutPatterns(final JexlContext jexlContext,
                                       final ClassMetaData classMetaData,
                                       final MethodMetaData methodMetaData) {
        for (Iterator it = m_pointcutPatterns.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

            // try to find a match somewhere in the class hierarchy (interface or super class)
            if (pointcutPattern.isHierarchical()) {
                matchThrowsPointcutSuperClasses(
                        jexlContext, name, classMetaData, methodMetaData, pointcutPattern);
            }
            // match the class only
            else if (pointcutPattern.getClassPattern().matches(classMetaData.getName()) &&
                    ((ThrowsPattern)pointcutPattern.getPattern()).
                    matches(methodMetaData)) {
                jexlContext.getVars().put(name, Boolean.TRUE);
            }
            else {
                jexlContext.getVars().put(name, Boolean.FALSE);
            }
        }
    }

    /**
     * Matches the method pointcut patterns.
     *
     * @param jexlContext the Jexl context
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param exception the class name of the exception
     */
    private void matchesPointcutPatterns(final JexlContext jexlContext,
                                         final ClassMetaData classMetaData,
                                         final MethodMetaData methodMetaData,
                                         final String exception) {
        for (Iterator it = m_pointcutPatterns.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

            // try to find a match somewhere in the class hierarchy (interface or super class)
            if (pointcutPattern.isHierarchical()) {
                matchThrowsPointcutSuperClasses(
                        jexlContext, name, classMetaData,
                        methodMetaData, pointcutPattern, exception);
            }
            // match the class only
            else if (pointcutPattern.getClassPattern().matches(classMetaData.getName()) &&
                    ((ThrowsPattern)pointcutPattern.getPattern()).
                    matches(methodMetaData, exception)) {
                jexlContext.getVars().put(name, Boolean.TRUE);
            }
            else {
                jexlContext.getVars().put(name, Boolean.FALSE);
            }
        }
    }

    /**
     * Tries to finds a match at some superclass in the hierarchy.
     * Recursive.
     *
     * @param jexlContext the Jexl context
     * @param name the name of the pointcut to evaluate
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param pointcutPattern the pointcut pattern
     * @return boolean
     */
    public static boolean matchThrowsPointcutSuperClasses(final JexlContext jexlContext,
                                                          final String name,
                                                          final ClassMetaData classMetaData,
                                                          final MethodMetaData methodMetaData,
                                                          final PointcutPatternTuple pointcutPattern) {
        if (classMetaData == null) {
            return false;
        }

        // match the class/super class
        if (pointcutPattern.getClassPattern().matches(classMetaData.getName()) &&
                ((ThrowsPattern)pointcutPattern.getPattern()).matches(methodMetaData)) {
            jexlContext.getVars().put(name, Boolean.TRUE);
            return true;
        }
        else {
            // match the interfaces for the class
            if (matchThrowsPointcutInterfaces(
                    jexlContext, name, classMetaData.getInterfaces(),
                    classMetaData, methodMetaData, pointcutPattern)) {
                return true;
            }

            // no match; get the next superclass
            return matchThrowsPointcutSuperClasses(
                    jexlContext, name, classMetaData.getSuperClass(),
                    methodMetaData, pointcutPattern);
        }
    }

    /**
     * Tries to finds a match at some interface in the hierarchy.
     * Recursive.
     *
     * @param jexlContext the Jexl context
     * @param name the name of the pointcut to evaluate
     * @param interfaces the interfaces
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param pointcutPattern the pointcut pattern
     * @return boolean
     */
    private static boolean matchThrowsPointcutInterfaces(final JexlContext jexlContext,
                                                         final String name,
                                                         final List interfaces,
                                                         final ClassMetaData classMetaData,
                                                         final MethodMetaData methodMetaData,
                                                         final PointcutPatternTuple pointcutPattern) {
        if (interfaces.isEmpty()) {
            return false;
        }

        for (Iterator it = interfaces.iterator(); it.hasNext();) {
            InterfaceMetaData interfaceMD = (InterfaceMetaData)it.next();
            if (pointcutPattern.getClassPattern().matches(interfaceMD.getName()) &&
                    ((ThrowsPattern)pointcutPattern.getPattern()).matches(methodMetaData)) {
                jexlContext.getVars().put(name, Boolean.TRUE);
                return true;
            }
            else {
                if (matchThrowsPointcutInterfaces(
                        jexlContext, name, interfaceMD.getInterfaces(),
                        classMetaData, methodMetaData, pointcutPattern)) {
                    return true;
                }
                else {
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * Tries to finds a match at some superclass in the hierarchy.
     * Recursive.
     *
     * @param jexlContext the Jexl context
     * @param name the name of the pointcut to evaluate
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param pointcutPattern the pointcut pattern
     * @param exception the exception's class name
     * @return boolean
     */
    public static boolean matchThrowsPointcutSuperClasses(final JexlContext jexlContext,
                                                          final String name,
                                                          final ClassMetaData classMetaData,
                                                          final MethodMetaData methodMetaData,
                                                          final PointcutPatternTuple pointcutPattern,
                                                          final String exception) {
        if (classMetaData == null) {
            return false;
        }

        // match the class/super class
        if (pointcutPattern.getClassPattern().matches(classMetaData.getName()) &&
                ((ThrowsPattern)pointcutPattern.getPattern()).matches(methodMetaData, exception)) {
            jexlContext.getVars().put(name, Boolean.TRUE);
            return true;
        }
        else {
            // match the interfaces for the class
            if (matchThrowsPointcutInterfaces(
                    jexlContext, name, classMetaData.getInterfaces(),
                    classMetaData, methodMetaData, pointcutPattern, exception)) {
                return true;
            }

            // no match; get the next superclass
            return matchThrowsPointcutSuperClasses(
                    jexlContext, name, classMetaData.getSuperClass(),
                    methodMetaData, pointcutPattern, exception);
        }
    }

    /**
     * Tries to finds a match at some interface in the hierarchy.
     * Recursive.
     *
     * @param jexlContext the Jexl context
     * @param name the name of the pointcut to evaluate
     * @param interfaces the interfaces
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param pointcutPattern the pointcut pattern
     * @param exception the exception's class name
     * @return boolean
     */
    private static boolean matchThrowsPointcutInterfaces(final JexlContext jexlContext,
                                                         final String name,
                                                         final List interfaces,
                                                         final ClassMetaData classMetaData,
                                                         final MethodMetaData methodMetaData,
                                                         final PointcutPatternTuple pointcutPattern,
                                                         final String exception) {
        if (interfaces.isEmpty()) {
            return false;
        }

        for (Iterator it = interfaces.iterator(); it.hasNext();) {
            InterfaceMetaData interfaceMD = (InterfaceMetaData)it.next();
            if (pointcutPattern.getClassPattern().matches(interfaceMD.getName()) &&
                    ((ThrowsPattern)pointcutPattern.getPattern()).
                    matches(methodMetaData, exception)) {
                jexlContext.getVars().put(name, Boolean.TRUE);
                return true;
            }
            else {
                if (matchThrowsPointcutInterfaces(
                        jexlContext, name, interfaceMD.getInterfaces(),
                        classMetaData, methodMetaData, pointcutPattern, exception)) {
                    return true;
                }
                else {
                    continue;
                }
            }
        }
        return false;
    }
}

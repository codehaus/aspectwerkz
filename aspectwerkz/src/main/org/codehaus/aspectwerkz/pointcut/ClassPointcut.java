/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.pointcut;

import java.util.Iterator;
import java.util.Map;
import java.util.List;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.InterfaceMetaData;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;

/**
 * Implements the pointcut concept for classes.
 * Is an abstraction of a well defined point of execution in the program.<br/>
 * Could matches one or many points as long as they are well defined.<br/>
 * Stores the advices for the specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ClassPointcut extends AbstractPointcut {

    /**
     * Marks the pointcut as reentrant.
     */
    protected boolean m_isNonReentrant = false;

    /**
     * Creates a new method pointcut.
     *
     * @param expression the expression for the pointcut
     */
    public ClassPointcut(final String expression) {
        this(AspectWerkz.DEFAULT_SYSTEM, expression);
    }

    /**
     * Creates a new method pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param expression the expression of the pointcut
     */
    public ClassPointcut(final String uuid, final String expression) {
        super(uuid, expression);
    }

    /**
     * Checks if the pointcut is non-reentrant.
     *
     * @return the non-reentrancy flag
     */
    public boolean isNonReentrant() {
        return m_isNonReentrant;
    }

    /**
     * Adds a new pointcut pattern.
     *
     * @param pointcut the pointcut definition
     */
    public void addPointcutDef(final PointcutDefinition pointcut) {
        // if one of the pointcut defs is non-reentrant, set the pointcut as non-reentrant
        if (pointcut.isNonReentrant()) {
            m_isNonReentrant = true;
        }
        m_pointcutPatterns.put(pointcut.getName(),
                new PointcutPatternTuple(
                        pointcut.getRegexpClassPattern(),
                        pointcut.getRegexpPattern(),
                        pointcut.isHierarchical()
                ));
    }

    /**
     * Checks if the pointcut matches a certain join point.
     *
     * @param className the name of the class
     * @return boolean
     */
    public boolean matches(final ClassMetaData classMetaData) {
        try {
            JexlContext jexlContext = JexlHelper.createContext();

            matchPointcutPatterns(jexlContext, classMetaData);

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
     * Tries to finds a match at some superclass in the hierarchy.
     * Only checks for a class match to allow early filtering.
     * Recursive.
     *
     * @param name the name of the pointcut to evaluate
     * @param classMetaData the class meta-data
     * @param pointcutPattern the pointcut pattern
     * @return boolean
     */
    public static boolean matchClassPointcutSuperClasses(final String name,
                                                          final ClassMetaData classMetaData,
                                                          final PointcutPatternTuple pointcutPattern) {
        if (classMetaData == null) {
            return false;
        }

        // match the class/super class
        if (pointcutPattern.getClassPattern().matches(classMetaData.getName())) {
            return true;
        }
        else {
            // match the interfaces for the class
            if (matchClassPointcutInterfaces(
                    name, classMetaData.getInterfaces(), classMetaData, pointcutPattern)) {
                return true;
            }

            // no match; get the next superclass
            return matchClassPointcutSuperClasses(
                    name, classMetaData.getSuperClass(), pointcutPattern);
        }
    }

    /**
     * Tries to finds a match at some superclass in the hierarchy.
     * Recursive.
     *
     * @param jexlContext the Jexl context
     * @param name the name of the pointcut to evaluate
     * @param classMetaData the class meta-data
     * @param pointcutPattern the pointcut pattern
     * @return boolean
     */
    public static boolean matchClassPointcutSuperClasses(final JexlContext jexlContext,
                                                          final String name,
                                                          final ClassMetaData classMetaData,
                                                          final PointcutPatternTuple pointcutPattern) {
        if (classMetaData == null) {
            return false;
        }

        // match the class/super class
        if (pointcutPattern.getClassPattern().matches(classMetaData.getName())) {
            jexlContext.getVars().put(name, Boolean.TRUE);
            return true;
        }
        else {
            // match the interfaces for the class
            if (matchClassPointcutInterfaces(
                    jexlContext, name, classMetaData.getInterfaces(),
                    classMetaData, pointcutPattern)) {
                return true;
            }

            // no match; get the next superclass
            return matchClassPointcutSuperClasses(
                    jexlContext, name, classMetaData.getSuperClass(), pointcutPattern);
        }
    }

    /**
     * Tries to finds a match at some interface in the hierarchy.
     * Only checks for a class match to allow early filtering.
     * Recursive.
     *
     * @param name the name of the pointcut to evaluate
     * @param interfaces the interfaces
     * @param classMetaData the class meta-data
     * @param pointcutPattern the pointcut pattern
     * @return boolean
     */
    private static boolean matchClassPointcutInterfaces(final String name,
                                                         final List interfaces,
                                                         final ClassMetaData classMetaData,
                                                         final PointcutPatternTuple pointcutPattern) {
        if (interfaces.isEmpty()) {
            return false;
        }
        for (Iterator it = interfaces.iterator(); it.hasNext();) {
            InterfaceMetaData interfaceMD = (InterfaceMetaData)it.next();
            if (pointcutPattern.getClassPattern().matches(interfaceMD.getName())) {
                return true;
            }
            else {
                if (matchClassPointcutInterfaces(
                        name, interfaceMD.getInterfaces(),
                        classMetaData, pointcutPattern)) {
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
     * Tries to finds a match at some interface in the hierarchy.
     * Recursive.
     *
     * @param jexlContext the Jexl context
     * @param name the name of the pointcut to evaluate
     * @param interfaces the interfaces
     * @param classMetaData the class meta-data
     * @param pointcutPattern the pointcut pattern
     * @return boolean
     */
    private static boolean matchClassPointcutInterfaces(final JexlContext jexlContext,
                                                         final String name,
                                                         final List interfaces,
                                                         final ClassMetaData classMetaData,
                                                         final PointcutPatternTuple pointcutPattern) {
        if (interfaces.isEmpty()) {
            return false;
        }
        for (Iterator it = interfaces.iterator(); it.hasNext();) {
            InterfaceMetaData interfaceMD = (InterfaceMetaData)it.next();
            if (pointcutPattern.getClassPattern().matches(interfaceMD.getName())) {
                jexlContext.getVars().put(name, Boolean.TRUE);
                return true;
            }
            else {
                if (matchClassPointcutInterfaces(
                        jexlContext, name, interfaceMD.getInterfaces(),
                        classMetaData, pointcutPattern)) {
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
     * Matches the method pointcut patterns.
     *
     * @param jexlContext the Jexl context
     * @param classMetaData the class meta-data
     */
    private void matchPointcutPatterns(final JexlContext jexlContext,
                                       final ClassMetaData classMetaData) {
        for (Iterator it = m_pointcutPatterns.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

            // try to find a match somewhere in the class hierarchy (interface or super class)
            if (pointcutPattern.isHierarchical()) {
                matchClassPointcutSuperClasses(jexlContext, name, classMetaData, pointcutPattern);
            }
            // match the class only
            else if (pointcutPattern.getClassPattern().matches(classMetaData.getName())) {
                jexlContext.getVars().put(name, Boolean.TRUE);
            }
            else {
                jexlContext.getVars().put(name, Boolean.FALSE);
            }
        }
    }
}

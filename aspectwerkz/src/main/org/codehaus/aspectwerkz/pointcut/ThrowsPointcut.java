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

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.regexp.ThrowsPattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;

/**
 * Implements the pointcut concept for exception handling.
 * Is an abstraction of a well defined point of execution in the program.<br/>
 * Could matches one or many points as long as they are well defined.<br/>
 * Stores the advices for this specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ThrowsPointcut.java,v 1.6 2003-07-03 13:10:49 jboner Exp $
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
                        pointcut.getRegexpPattern()));
    }

    /**
     * Checks if the pointcut matches a certain join point.
     *
     * @param className the name of the class
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    public boolean matches(final String className,
                           final MethodMetaData methodMetaData) {
        JexlContext jexlContext = JexlHelper.createContext();

        for (Iterator it = m_pointcutPatterns.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

            if (pointcutPattern.getClassPattern().matches(className) &&
                    ((ThrowsPattern)pointcutPattern.getPattern()).
                    matches(methodMetaData)) {
                jexlContext.getVars().put(name, Boolean.TRUE);
            }
            else {
                jexlContext.getVars().put(name, Boolean.FALSE);
            }
        }
        try {
            Expression e = ExpressionFactory.createExpression(m_expression);
            Boolean result = (Boolean)e.evaluate(jexlContext);

            if (result.booleanValue()) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Checks if the pointcut matches a certain join point.
     *
     * @param className the name of the class
     * @param methodMetaData the meta-data for the method
     * @param exceptionClassName the name of the exception
     * @return boolean
     */
    public boolean matches(final String className,
                           final MethodMetaData methodMetaData,
                           final String exceptionClassName) {
        JexlContext jexlContext = JexlHelper.createContext();

        for (Iterator it = m_pointcutPatterns.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

            if (pointcutPattern.getClassPattern().matches(className) &&
                    ((ThrowsPattern)pointcutPattern.getPattern()).
                    matches(methodMetaData, exceptionClassName)) {
                jexlContext.getVars().put(name, Boolean.TRUE);
            }
            else {
                jexlContext.getVars().put(name, Boolean.FALSE);
            }
        }
        try {
            Expression e = ExpressionFactory.createExpression(m_expression);
            Boolean result = (Boolean)e.evaluate(jexlContext);

            if (result.booleanValue()) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}

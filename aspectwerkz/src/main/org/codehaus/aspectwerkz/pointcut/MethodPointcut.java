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
package org.codehaus.aspectwerkz.pointcut;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.regexp.MethodPattern;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;

/**
 * Implements the pointcut concept for method access.
 * Is an abstraction of a well defined point of execution in the program.<br/>
 * Could matches one or many points as long as they are well defined.<br/>
 * Stores the advices for the specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: MethodPointcut.java,v 1.3 2003-06-17 14:54:27 jboner Exp $
 */
public class MethodPointcut extends AbstractPointcut {

    /**
     * Creates a new method pointcut.
     *
     * @param expression the expression for the pointcut
     */
    public MethodPointcut(final String expression) {
        this(AspectWerkz.DEFAULT_SYSTEM, expression);
    }

    /**
     * Creates a new method pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param expression the expression of the pointcut
     */
    public MethodPointcut(final String uuid,
                          final String expression) {
        super(uuid, expression);
    }

    /**
     * Adds a new pointcut definition.
     *
     * @param pointcut the pointcut definition
     */
    public void addPointcutDef(final PointcutDefinition pointcut) {
        m_pointcutDefs.put(pointcut.getName(),
                new PointcutPattern(
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

        for (Iterator it = m_pointcutDefs.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = (String)entry.getKey();
            PointcutPattern pointcutPattern = (PointcutPattern)entry.getValue();

            if (pointcutPattern.getClassPattern().matches(className) &&
                    ((MethodPattern)pointcutPattern.getPattern()).matches(methodMetaData)) {
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

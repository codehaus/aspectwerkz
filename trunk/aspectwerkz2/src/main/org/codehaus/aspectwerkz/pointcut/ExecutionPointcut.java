/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.pointcut;

import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;

/**
 * Implements the pointcut concept for method access. Is an abstraction of a well defined point of execution in the
 * program.<br/> Could matches one or many points as long as they are well defined.<br/> Stores the advices for the
 * specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ExecutionPointcut extends AbstractPointcut {

    /**
     * Marks the pointcut as reentrant.
     */
    protected boolean m_isNonReentrant = false;

    /**
     * Creates a new execution pointcut.
     *
     * @param uuid       the UUID for the AspectWerkz system
     * @param expression the expression of the pointcut
     */
    public ExecutionPointcut(final String uuid, final Expression expression) {
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
        m_pointcutPatterns.put(pointcut.getName(), pointcut);
    }

    /**
     * Returns the pointcut type.
     *
     * @return the pointcut type
     */
    public PointcutType getPointcutType() {
        return PointcutType.EXECUTION;
    }
}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

/**
 * Context for the expression AST traversal.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ExpressionContext {

    /**
     * The type of the pointcut to evaluate.
     */
    private final PointcutType m_pointcutType;

    /**
     * Creates a new context.
     *
     * @param pointcutType the pointcut type
     */
    public ExpressionContext(final PointcutType pointcutType) {
        m_pointcutType = pointcutType;
    }

    /**
     * Returns the pointcut type.
     *
     * @return the pointcut type
     */
    public PointcutType getPointcutType() {
        return m_pointcutType;
    }
}

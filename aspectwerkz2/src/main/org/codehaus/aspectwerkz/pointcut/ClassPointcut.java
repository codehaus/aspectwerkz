/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.pointcut;

import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;

/**
 * Implements the pointcut concept for classes.
 * Is an abstraction of a well defined point of execution in the program.<br/>
 * Could matches one or many points as long as they are well defined.<br/>
 * Stores the advices for the specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ClassPointcut extends AbstractPointcut {

    /**
     * Marks the pointcut as reentrant.
     */
    protected boolean m_isNonReentrant = false;

    /**
     * Creates a new class pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param expression the expression of the pointcut
     */
    public ClassPointcut(final String uuid, final Expression expression) {
        super(uuid, expression);
    }

    /**
     * Returns the pointcut type.
     *
     * @return the pointcut type
     */
    public PointcutType getPointcutType() {
        return PointcutType.CLASS;
    }
}


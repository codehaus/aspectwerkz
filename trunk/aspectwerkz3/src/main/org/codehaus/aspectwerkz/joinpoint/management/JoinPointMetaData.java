/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.aspect.management.Pointcut;
import org.codehaus.aspectwerkz.expression.ExpressionContext;

import java.util.List;

/**
 * Holds meta data about a specific join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class JoinPointMetaData {
    /**
     * The indexes for the advices.
     */
    public AdviceIndexInfo[] adviceIndexes;

    /**
     * The cflow expressions runtime.
     */
    public List cflowExpressions;

    /**
     * The cflow pointcut.
     */
    public Pointcut cflowPointcut;

    /**
     * The join point expression context
     */
    public ExpressionContext expressionContext;
}

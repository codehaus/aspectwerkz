/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import gnu.trove.TIntObjectHashMap;
import org.codehaus.aspectwerkz.expression.CflowExpressionVisitorRuntime;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Thread local stack for cflow.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public final class CflowStack {

    /**
     * Holds references to all the systems defined. Maps the ClassLoader to a matching system instance.
     */
    private static final Map s_cflowStacks = new WeakHashMap();

    /**
     * Holds a list of the cflow join points passed by the control flow of the current thread.
     *
     * @TODO: I think we need to use a static TL - need test coverage
     */
    private final ThreadLocal m_cflowStack = new ThreadLocal();

    /**
     * Returns the System for a specific ClassLoader. If the system is not initialized, register the ClassLoader
     * hierarchy and all the definitions to initialize the system.
     *
     * @param loader the ClassLoader
     * @return the System instance for this ClassLoader
     */
    public synchronized static CflowStack getCflowStack(final ClassLoader loader) {
        CflowStack stack = (CflowStack) s_cflowStacks.get(loader);
        if (stack == null) {
            stack = new CflowStack();
            s_cflowStacks.put(loader, stack);
        }
        return stack;
    }

    /**
     * Registers entering of a control flow join point.
     *
     * @param pointcutType the pointcut type
     * @param methodInfo   the method info
     * @param withinInfo   the within info
     */
    public void enteringControlFlow(final PointcutType pointcutType,
                                    final MethodInfo methodInfo,
                                    final ClassInfo withinInfo) {
        if (pointcutType == null) {
            throw new IllegalArgumentException("pointcut type can not be null");
        }
        if (methodInfo == null) {
            throw new IllegalArgumentException("method info can not be null");
        }
        TIntObjectHashMap cflows = (TIntObjectHashMap) m_cflowStack.get();
        if (cflows == null) {
            cflows = new TIntObjectHashMap();
        }
        ExpressionContext expressionContext = new ExpressionContext(pointcutType, methodInfo, withinInfo);
        cflows.put(expressionContext.hashCode(), expressionContext);
        m_cflowStack.set(cflows);
    }

    /**
     * Registers exiting from a control flow join point.
     *
     * @param pointcutType the pointcut type
     * @param methodInfo   the method info
     * @param withinInfo   the within info
     */
    public void exitingControlFlow(final PointcutType pointcutType,
                                   final MethodInfo methodInfo,
                                   final ClassInfo withinInfo) {
        if (pointcutType == null) {
            throw new IllegalArgumentException("pointcut type can not be null");
        }
        if (methodInfo == null) {
            throw new IllegalArgumentException("method info can not be null");
        }
        TIntObjectHashMap cflows = (TIntObjectHashMap) m_cflowStack.get();
        if (cflows == null) {
            return;
        }
        ExpressionContext ctx = new ExpressionContext(pointcutType, methodInfo, withinInfo);
        cflows.remove(ctx.hashCode());
        m_cflowStack.set(cflows);
    }

    /**
     * Checks if we are in the control flow of a join point picked out by a specific pointcut expression.
     *
     * @param expression        the cflow expression runtime visitor
     * @param expressionContext the join point expression context whose pointcut contains cflows sub expression(s)
     * @return boolean
     */
    public boolean isInControlFlowOf(final CflowExpressionVisitorRuntime expression,
                                     ExpressionContext expressionContext) {
        if (expression == null) {
            throw new IllegalArgumentException("expression can not be null");
        }
        TIntObjectHashMap cflows = (TIntObjectHashMap) m_cflowStack.get();
        if (cflows == null) {
            // we still need to evaluate the expression to handle "NOT cflow"
            cflows = new TIntObjectHashMap();
        }
        if (expression.matchCflowStack(cflows.getValues(), expressionContext)) {
            return true;
        }
        return false;
    }

    private CflowStack() {
    }
}
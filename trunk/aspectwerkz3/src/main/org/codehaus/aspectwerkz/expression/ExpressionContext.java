/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;

/**
 * The expression context for AST evaluation.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ExpressionContext {
    public static final int NOTAVAILABLE_INFO = -1;

    public static final int METHOD_INFO = 0;

    public static final int CONSTRUCTOR_INFO = 1;

    public static final int FIELD_INFO = 2;

    public static final int CLASS_INFO = 3;

    private final int m_reflectionInfoType;

    private final PointcutType m_pointcutType;

    private final ReflectionInfo m_matchingReflectionInfo;

    private final ReflectionInfo m_withinReflectionInfo;

    private boolean m_inCflowSubAST = false;

    private boolean m_cflowEvaluation = false;

    private boolean m_hasBeenVisitingCflow = false;

    private int m_currentTartgetArgsIndex = 0;

    public gnu.trove.TIntIntHashMap m_exprIndexToTargetIndex = new gnu.trove.TIntIntHashMap();

    /**
     * Creates a new expression context.
     *
     * @param pointcutType
     * @param reflectionInfo       - can be null f.e. with early evaluation of CALL pointcut
     * @param withinReflectionInfo
     */
    public ExpressionContext(final PointcutType pointcutType,
                             final ReflectionInfo reflectionInfo,
                             final ReflectionInfo withinReflectionInfo) {
        if (pointcutType == null) {
            throw new IllegalArgumentException("pointcut type can not be null");
        }
        m_pointcutType = pointcutType;
        m_matchingReflectionInfo = reflectionInfo;
        if (withinReflectionInfo != null) {
            m_withinReflectionInfo = withinReflectionInfo;
        } else {
            // backward compatible, mainly for test suite
            m_withinReflectionInfo = m_matchingReflectionInfo;
        }
        if (reflectionInfo instanceof MethodInfo) {
            m_reflectionInfoType = METHOD_INFO;
        } else if (reflectionInfo instanceof ConstructorInfo) {
            m_reflectionInfoType = CONSTRUCTOR_INFO;
        } else if (reflectionInfo instanceof FieldInfo) {
            m_reflectionInfoType = FIELD_INFO;
        } else if (reflectionInfo instanceof ClassInfo) {
            m_reflectionInfoType = CLASS_INFO;
        } else {
            m_reflectionInfoType = NOTAVAILABLE_INFO;// used for early eval on CALL
        }
    }

    public ReflectionInfo getReflectionInfo() {
        return m_matchingReflectionInfo;
    }

    public ReflectionInfo getWithinReflectionInfo() {
        return m_withinReflectionInfo;
    }

    public boolean hasExecutionPointcut() {
        return m_pointcutType.equals(PointcutType.EXECUTION);
    }

    public boolean hasCallPointcut() {
        return m_pointcutType.equals(PointcutType.CALL);
    }

    public boolean hasSetPointcut() {
        return m_pointcutType.equals(PointcutType.SET);
    }

    public boolean hasGetPointcut() {
        return m_pointcutType.equals(PointcutType.GET);
    }

    public boolean hasHandlerPointcut() {
        return m_pointcutType.equals(PointcutType.HANDLER);
    }

    public boolean hasStaticInitializationPointcut() {
        return m_pointcutType.equals(PointcutType.STATIC_INITIALIZATION);
    }

    public boolean hasWithinPointcut() {
        return m_pointcutType.equals(PointcutType.WITHIN);
    }
//
//    public boolean hasHasMethodPointcut() {
//        return m_pointcutType.equals(PointcutType.HAS_METHOD);
//    }
//
//    public boolean hasHasFieldPointcut() {
//        return m_pointcutType.equals(PointcutType.HAS_FIELD);
//    }

    public boolean hasWithinReflectionInfo() {
        return m_withinReflectionInfo != null;
    }

    public boolean hasMethodInfo() {
        return m_reflectionInfoType == METHOD_INFO;
    }

    public boolean hasConstructorInfo() {
        return m_reflectionInfoType == CONSTRUCTOR_INFO;
    }

    public boolean hasFieldInfo() {
        return m_reflectionInfoType == FIELD_INFO;
    }

    public boolean hasClassInfo() {
        return m_reflectionInfoType == CLASS_INFO;
    }

    public boolean hasReflectionInfo() {
        return m_reflectionInfoType != NOTAVAILABLE_INFO;
    }

    public void setInCflowSubAST(final boolean inCflowAST) {
        m_inCflowSubAST = inCflowAST;
    }

    public boolean inCflowSubAST() {
        return m_inCflowSubAST;
    }

    public void setHasBeenVisitingCflow(final boolean hasBeenVisitingCflow) {
        m_hasBeenVisitingCflow = hasBeenVisitingCflow;
    }

    public boolean hasBeenVisitingCflow() {
        return m_hasBeenVisitingCflow;
    }

    public boolean getCflowEvaluation() {
        return m_cflowEvaluation;
    }

    public void setCflowEvaluation(boolean cflowEvaluation) {
        m_cflowEvaluation = cflowEvaluation;
    }

    public int getCurrentTargetArgsIndex() {
        return m_currentTartgetArgsIndex;
    }

    public void setCurrentTargetArgsIndex(int argsIndex) {
        this.m_currentTartgetArgsIndex = argsIndex;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExpressionContext)) {
            return false;
        }
        final ExpressionContext expressionContext = (ExpressionContext) o;
        if (m_reflectionInfoType != expressionContext.m_reflectionInfoType) {
            return false;
        }
        if (!m_matchingReflectionInfo.equals(expressionContext.m_matchingReflectionInfo)) {
            return false;
        }
        if (!m_pointcutType.equals(expressionContext.m_pointcutType)) {
            return false;
        }
        if ((m_withinReflectionInfo != null) ?
            (!m_withinReflectionInfo
                .equals(expressionContext.m_withinReflectionInfo)) :
            (expressionContext.m_withinReflectionInfo != null)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result;
        result = m_pointcutType.hashCode();
        result = (29 * result) + m_matchingReflectionInfo.hashCode();
        result = (29 * result) + ((m_withinReflectionInfo != null) ? m_withinReflectionInfo.hashCode() : 0);
        result = (29 * result) + m_reflectionInfoType;
        return result;
    }

    public PointcutType getPointcutType() {
        return m_pointcutType;
    }
}
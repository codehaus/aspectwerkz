/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining;

import org.codehaus.aspectwerkz.aspect.AdviceInfo;
import org.codehaus.aspectwerkz.transform.inlining.spi.AspectModel;

/**
 * Container for the advice method info.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AdviceMethodInfo {
    private final AspectInfo m_aspectInfo;
    private final AdviceInfo m_adviceInfo;
    private int m_specialArgumentIndex = -1;//FIXME remove - should not be here
    private int m_joinPointIndex;//FIXME remove - should not be here
    private String m_calleeClassSignature;
    private String m_callerClassSignature;
    private String m_joinPointClassName;
    private String m_calleeMemberDesc;

    public AdviceMethodInfo(final AspectInfo aspectInfo,
                            final AdviceInfo adviceInfo,
                            final String callerClassSignature,
                            final String calleeClassSignature,
                            final String joinPointClassName,
                            final String calleeMemberDesc) {
        m_aspectInfo = aspectInfo;
        m_adviceInfo = adviceInfo;
        m_callerClassSignature = callerClassSignature;
        m_calleeClassSignature = calleeClassSignature;
        m_joinPointClassName = joinPointClassName;
        m_calleeMemberDesc = calleeMemberDesc;
    }

    public AdviceInfo getAdviceInfo() {
        return m_adviceInfo;
    }

    public AspectInfo getAspectInfo() {
        return m_aspectInfo;
    }

    public int[] getAdviceMethodArgIndexes() {
        return m_adviceInfo.getMethodToArgIndexes();
    }

    public String getSpecialArgumentTypeDesc() {
        return m_adviceInfo.getSpecialArgumentTypeDesc();
    }

    public String getSpecialArgumentTypeName() {
        return m_adviceInfo.getSpecialArgumentTypeName();
    }

    public int getSpecialArgumentIndex() {
        return m_specialArgumentIndex;
    }

    public void setSpecialArgumentIndex(final int index) {
        m_specialArgumentIndex = index;
    }

    public String getCalleeClassSignature() {
        return m_calleeClassSignature;
    }

    public String getCallerClassSignature() {
        return m_callerClassSignature;
    }

    public String getJoinPointClassName() {
        return m_joinPointClassName;
    }

    public String getCalleeMemberDesc() {
        return m_calleeMemberDesc;
    }

    /**
     * @return true if the advice uses this or target (bounded or runtime check)
     */
    public boolean requiresThisOrTarget() {
        if (m_adviceInfo.hasTargetWithRuntimeCheck()) {
            return true;
        } else {
            // look for TARGET or THIS bindings
            for (int i = 0; i < m_adviceInfo.getMethodToArgIndexes().length; i++) {
                int index = m_adviceInfo.getMethodToArgIndexes()[i];
                if (index == AdviceInfo.TARGET_ARG ||
                    index == AdviceInfo.THIS_ARG) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return true if the advice uses non static JoinPoint explicitly
     */
    public boolean requiresJoinPoint() {
        // look for JoinPoint
        for (int i = 0; i < m_adviceInfo.getMethodToArgIndexes().length; i++) {
            int index = m_adviceInfo.getMethodToArgIndexes()[i];
            if (index == AdviceInfo.JOINPOINT_ARG) {
                return true;
            }
        }
        return false;
    }

}

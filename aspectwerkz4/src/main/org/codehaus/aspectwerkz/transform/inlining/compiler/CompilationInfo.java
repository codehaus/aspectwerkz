/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.compiler;

import org.codehaus.aspectwerkz.joinpoint.management.AdviceInfoContainer;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.codehaus.aspectwerkz.reflect.ClassInfo;

/**
 * Info needed for the compilation of the join point, holds both the initial model and the latest redefined model.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public final class CompilationInfo {
    private final Model m_initialModel;
    private Model m_redefinedModel;
    private int m_redefinitionCounter = 0;

    public CompilationInfo(final Model initialModel) {
        m_initialModel = initialModel;
    }

    public Model getInitialModel() {
        return m_initialModel;
    }

    public Model getRedefinedModel() {
        return m_redefinedModel;
    }

    public void setRedefinedModel(final Model redefinedModel) {
        m_redefinedModel = redefinedModel;
    }

    public int getRedefinitionCounter() {
        return m_redefinitionCounter;
    }

    public void incrementRedefinitionCounter() {
        m_redefinitionCounter += 1;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompilationInfo)) {
            return false;
        }

        final CompilationInfo compilationInfo = (CompilationInfo) o;

        if (m_redefinitionCounter != compilationInfo.m_redefinitionCounter) {
            return false;
        }
        if (m_initialModel != null ?
            !m_initialModel.equals(compilationInfo.m_initialModel) :
            compilationInfo.m_initialModel != null) {
            return false;
        }
        if (m_redefinedModel != null ?
            !m_redefinedModel.equals(compilationInfo.m_redefinedModel) :
            compilationInfo.m_redefinedModel != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (m_initialModel != null ? m_initialModel.hashCode() : 0);
        result = 29 * result + (m_redefinedModel != null ? m_redefinedModel.hashCode() : 0);
        result = 29 * result + m_redefinitionCounter;
        return result;
    }

    /**
     * Represents the information needed to compile one joinpoint at a given time
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
     */
    public final static class Model {
        private final String m_joinPointClassName;
        private final EmittedJoinPoint m_emittedJoinPoint;
        private final AdviceInfoContainer m_adviceInfoContainer;
        private final ClassInfo m_thisClassInfo;

        public Model(final EmittedJoinPoint emittedJoinPoint,
                     final AdviceInfoContainer adviceInfoContainer,
                     final ClassInfo thisClassInfo) {
            m_emittedJoinPoint = emittedJoinPoint;
            m_adviceInfoContainer = adviceInfoContainer;
            m_joinPointClassName = m_emittedJoinPoint.getJoinPointClassName();
            m_thisClassInfo = thisClassInfo;
        }

        public Model(final EmittedJoinPoint emittedJoinPoint,
                     final AdviceInfoContainer adviceInfoContainer,
                     final int redefinitionCounter,
                     final ClassInfo thisClassInfo) {
            m_emittedJoinPoint = emittedJoinPoint;
            m_adviceInfoContainer = adviceInfoContainer;
            m_joinPointClassName = Strings.replaceSubString(
                    m_emittedJoinPoint.getJoinPointClassName(),
                    TransformationConstants.JOIN_POINT_CLASS_SUFFIX,
                    new StringBuffer().append('_').append(redefinitionCounter).
                    append(TransformationConstants.JOIN_POINT_CLASS_SUFFIX).toString()
            );
            m_thisClassInfo = thisClassInfo;
        }

        public String getJoinPointClassName() {
            return m_joinPointClassName;
        }

        public EmittedJoinPoint getEmittedJoinPoint() {
            return m_emittedJoinPoint;
        }

        public AdviceInfoContainer getAdviceInfoContainer() {
            return m_adviceInfoContainer;
        }

        /**
         * JoinPoint this class class info (caller)
         * @return
         */
        public ClassInfo getThisClassInfo() {
            return m_thisClassInfo;
        }

        public int hashCode() {
            return m_emittedJoinPoint.hashCode();
        }

        public boolean equals(Object o) {
            return ((Model) o).m_emittedJoinPoint == m_emittedJoinPoint;
        }
    }
}
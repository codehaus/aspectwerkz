/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import org.codehaus.aspectwerkz.AspectContext;
import org.codehaus.aspectwerkz.AdviceInfo;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementation of the pointcut concept. I.e. an abstraction of a well defined point of execution in the program.
 * <p/>Could matches one or many as long at it is well defined. <br/>Stores the advices for the specific pointcut. <p/>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 *         <p/>
 *         TODO this class has transient fields that might cause pbms after serialization
 *         TODO change addXXAdvice to allow 'aspectName, adviceName' params
 */
public class Pointcut implements Serializable {
    /**
     * The aspect context for the aspect defining this pointcut.
     */
    protected final AspectContext m_aspectContext;

    /**
     * The expression for the pointcut.
     */
    protected transient ExpressionInfo m_expressionInfo;

    /**
     * The around advice infos.
     */
    protected List m_aroundAdviceInfos = new ArrayList();;

    /**
     * The before advice infos.
     */
    protected List m_beforeAdviceInfos = new ArrayList();;

    /**
     * The after finally advice infos.
     */
    protected List m_afterFinallyAdviceInfos = new ArrayList();;

    /**
     * The after returning advice infos.
     */
    protected List m_afterReturningAdviceInfos = new ArrayList();;

    /**
     * The after throwing advice infos.
     */
    protected List m_afterThrowingAdviceInfos = new ArrayList();;

    /**
     * Creates a new pointcut.
     *
     * @param context        the aspect context
     * @param expressionInfo the pattern for the pointcut
     */
    public Pointcut(final AspectContext context, final ExpressionInfo expressionInfo) {
        if (context == null) {
            throw new IllegalArgumentException("aspect context can not be null");
        }
        if (expressionInfo == null) {
            throw new IllegalArgumentException("expression info can not be null");
        }
        m_aspectContext = context;
        m_expressionInfo = expressionInfo;
    }

    /**
     * Clones the pointcut.
     *
     * @return
     */
    public Pointcut deepCopy() {
        return new Pointcut(m_aspectContext, m_expressionInfo);
    }

    /**
     * Returns the aspect context.
     *
     * @return the aspect context
     */
    public AspectContext getAspectContext() {
        return m_aspectContext;
    }

    /**
     * Adds an around name to the pointcut.
     *
     * @param name the name of the name to add
     */
    public void addAroundAdvice(final String name) {
        if ((name == null) || (name.trim().length() == 0)) {
            throw new IllegalArgumentException("name of name to add can not be null or an empty string");
        }
        synchronized (m_aroundAdviceInfos) {
            AdviceInfo adviceInfo = m_aspectContext.getContainer().getAdviceInfo(name);
            if (adviceInfo != null) {
                m_aroundAdviceInfos.add(adviceInfo);
            }
        }
    }

    /**
     * Adds a before advice to the pointcut.
     *
     * @param name the name of the advice to add
     */
    public void addBeforeAdvice(final String name) {
        if ((name == null) || (name.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }
        synchronized (m_beforeAdviceInfos) {
            final AdviceInfo adviceInfo = m_aspectContext.getContainer().getAdviceInfo(name);
            if (adviceInfo != null) {
                m_beforeAdviceInfos.add(adviceInfo);
            }
        }
    }

    /**
     * Adds an after finally advice to the pointcut.
     *
     * @param name the name of the advice to add
     */
    public void addAfterFinallyAdvices(final String name) {
        if ((name == null) || (name.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }
        synchronized (m_afterFinallyAdviceInfos) {
            final AdviceInfo adviceInfo = m_aspectContext.getContainer().getAdviceInfo(name);
            if (adviceInfo != null) {
                m_afterFinallyAdviceInfos.add(adviceInfo);
            }
        }
    }

    /**
     * Adds an after returning advice to the pointcut.
     *
     * @param name the name of the advice to add
     */
    public void addAfterReturningAdvices(final String name) {
        if ((name == null) || (name.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }
        synchronized (m_afterReturningAdviceInfos) {
            final AdviceInfo adviceInfo = m_aspectContext.getContainer().getAdviceInfo(name);
            if (adviceInfo != null) {
                m_afterReturningAdviceInfos.add(adviceInfo);
            }
        }
    }

    /**
     * Adds an after throwing advice to the pointcut.
     *
     * @param name the name of the advice to add
     */
    public void addAfterThrowingAdvices(final String name) {
        if ((name == null) || (name.trim().length() == 0)) {
            throw new IllegalArgumentException("name of advice to add can not be null or an empty string");
        }
        synchronized (m_afterThrowingAdviceInfos) {
            final AdviceInfo adviceInfo = m_aspectContext.getContainer().getAdviceInfo(name);
            if (adviceInfo != null) {
                m_afterThrowingAdviceInfos.add(adviceInfo);
            }
        }
    }

    /**
     * Returns a list with the indexes for the around advices for the pointcut.
     *
     * @return the advices
     */
    public List getAroundAdviceInfos() {
        return m_aroundAdviceInfos;
    }

    /**
     * Returns a list with the indexes for the before advices for the pointcut.
     *
     * @return the advices
     */
    public List getBeforeAdviceInfos() {
        return m_beforeAdviceInfos;
    }

    /**
     * Returns a list with the indexes for the after advices for the pointcut.
     *
     * @return the advices
     */
    public List getAfterFinallyAdviceInfos() {
        return m_afterFinallyAdviceInfos;
    }

    /**
     * Returns a list with the indexes for the after advices for the pointcut.
     *
     * @return the advices
     */
    public List getAfterReturningAdviceInfos() {
        return m_afterReturningAdviceInfos;
    }

    /**
     * Returns a list with the indexes for the after advices for the pointcut.
     *
     * @return the advices
     */
    public List getAfterThrowingAdviceInfos() {
        return m_afterThrowingAdviceInfos;
    }

    /**
     * Returns the expression for the pointcut.
     *
     * @return the expression
     */
    public ExpressionInfo getExpressionInfo() {
        return m_expressionInfo;
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_expressionInfo = (ExpressionInfo) fields.get("m_annotation", null);
        m_aroundAdviceInfos = (List) fields.get("m_aroundAdviceInfos", null);
        m_beforeAdviceInfos = (List) fields.get("m_beforeAdviceInfos", null);
        m_afterFinallyAdviceInfos = (List) fields.get("m_afterFinallyAdviceInfos", null);
        m_afterReturningAdviceInfos = (List) fields.get("m_afterReturningAdviceInfos", null);
        m_afterThrowingAdviceInfos = (List) fields.get("m_afterThrowingAdviceInfos", null);
    }
}
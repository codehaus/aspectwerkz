/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.delegation;

import gnu.trove.TIntObjectHashMap;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AdvisableImpl implements Advisable {

    public static final ClassInfo CLASS_INFO;

    static {
        try {
            CLASS_INFO =
            AsmClassInfo.getClassInfo(AdvisableImpl.class.getName(), AdvisableImpl.class.getClassLoader());
        } catch (Exception e) {
            throw new Error("could not load AdvisableImpl class for itself");
        }

    }

    private final TIntObjectHashMap m_aroundAdviceDelegators = new TIntObjectHashMap();
    private final TIntObjectHashMap m_beforeAdviceDelegators = new TIntObjectHashMap();
    private final TIntObjectHashMap m_afterFinallyAdviceDelegators = new TIntObjectHashMap();
    private final TIntObjectHashMap m_afterReturningAdviceDelegators = new TIntObjectHashMap();
    private final TIntObjectHashMap m_afterThrowingAdviceDelegators = new TIntObjectHashMap();

    /**
     * @param delegator
     */
    public void addAdviceDelegator(final AdviceDelegator delegator) {
        delegator.register(this);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AroundAdviceDelegator[] getAroundAdviceDelegators(final int joinPointIndex) {
        return (AroundAdviceDelegator[]) m_aroundAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void setAroundAdviceDelegators(final int joinPointIndex,
                                          final AroundAdviceDelegator[] delegators) {
        m_aroundAdviceDelegators.put(joinPointIndex, delegators);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public BeforeAdviceDelegator[] getBeforeAdviceDelegators(final int joinPointIndex) {
        return (BeforeAdviceDelegator[]) m_beforeAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void setBeforeAdviceDelegators(final int joinPointIndex,
                                          final BeforeAdviceDelegator[] delegators) {
        m_beforeAdviceDelegators.put(joinPointIndex, delegators);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AfterFinallyAdviceDelegator[] getAfterFinallyAdviceDelegators(final int joinPointIndex) {
        return (AfterFinallyAdviceDelegator[]) m_afterFinallyAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void setAfterFinallyAdviceDelegators(final int joinPointIndex,
                                                final AfterFinallyAdviceDelegator[] delegators) {
        m_afterFinallyAdviceDelegators.put(joinPointIndex, delegators);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AfterReturningAdviceDelegator[] getAfterReturningAdviceDelegators(final int joinPointIndex) {
        return (AfterReturningAdviceDelegator[]) m_afterReturningAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void setAfterReturningAdviceDelegators(final int joinPointIndex,
                                                  final AfterReturningAdviceDelegator[] delegators) {
        m_afterReturningAdviceDelegators.put(joinPointIndex, delegators);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AfterThrowingAdviceDelegator[] getAfterThrowingAdviceDelegators(final int joinPointIndex) {
        return (AfterThrowingAdviceDelegator[]) m_afterThrowingAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void setAfterThrowingAdviceDelegators(final int joinPointIndex,
                                                 final AfterThrowingAdviceDelegator[] delegators) {
        m_afterThrowingAdviceDelegators.put(joinPointIndex, delegators);
    }
}

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
            throw new Error("could not load class from itself [AdvisableImpl]");
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
    public void aw$addAdviceDelegator(final AdviceDelegator delegator) {
        delegator.register(this);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AroundAdviceDelegator[] aw$getAroundAdviceDelegators(final int joinPointIndex) {
        return (AroundAdviceDelegator[]) m_aroundAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void aw$setAroundAdviceDelegators(final int joinPointIndex,
                                             final AroundAdviceDelegator[] delegators) {
        m_aroundAdviceDelegators.put(joinPointIndex, delegators);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public BeforeAdviceDelegator[] aw$getBeforeAdviceDelegators(final int joinPointIndex) {
        return (BeforeAdviceDelegator[]) m_beforeAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void aw$setBeforeAdviceDelegators(final int joinPointIndex,
                                             final BeforeAdviceDelegator[] delegators) {
        m_beforeAdviceDelegators.put(joinPointIndex, delegators);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AfterFinallyAdviceDelegator[] aw$getAfterFinallyAdviceDelegators(final int joinPointIndex) {
        return (AfterFinallyAdviceDelegator[]) m_afterFinallyAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void aw$setAfterFinallyAdviceDelegators(final int joinPointIndex,
                                                   final AfterFinallyAdviceDelegator[] delegators) {
        m_afterFinallyAdviceDelegators.put(joinPointIndex, delegators);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AfterReturningAdviceDelegator[] aw$getAfterReturningAdviceDelegators(final int joinPointIndex) {
        return (AfterReturningAdviceDelegator[]) m_afterReturningAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void aw$setAfterReturningAdviceDelegators(final int joinPointIndex,
                                                     final AfterReturningAdviceDelegator[] delegators) {
        m_afterReturningAdviceDelegators.put(joinPointIndex, delegators);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AfterThrowingAdviceDelegator[] aw$getAfterThrowingAdviceDelegators(final int joinPointIndex) {
        return (AfterThrowingAdviceDelegator[]) m_afterThrowingAdviceDelegators.get(joinPointIndex);
    }

    /**
     * @param joinPointIndex
     * @param delegators
     */
    public void aw$setAfterThrowingAdviceDelegators(final int joinPointIndex,
                                                    final AfterThrowingAdviceDelegator[] delegators) {
        m_afterThrowingAdviceDelegators.put(joinPointIndex, delegators);
    }
}

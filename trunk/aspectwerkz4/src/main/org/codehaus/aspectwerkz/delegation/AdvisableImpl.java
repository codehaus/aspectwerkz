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
 * Implementation of the <code>Advisable</code> mixin.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AdvisableImpl implements Advisable {

    public static final ClassInfo CLASS_INFO;
    public static final AroundAdviceDelegator[] EMPTY_AROUND_DELEGATOR_ARRAY = new AroundAdviceDelegator[0];
    public static final BeforeAdviceDelegator[] EMPTY_BEFORE_DELEGATOR_ARRAY = new BeforeAdviceDelegator[0];
    public static final AfterFinallyAdviceDelegator[] EMPTY_AFTER_FINALLY_DELEGATOR_ARRAY = new AfterFinallyAdviceDelegator[0];
    public static final AfterReturningAdviceDelegator[] EMPTY_AFTER_RETURNING_DELEGATOR_ARRAY = new AfterReturningAdviceDelegator[0];
    public static final AfterThrowingAdviceDelegator[] EMPTY_AFTER_THROWING_DELEGATOR_ARRAY = new AfterThrowingAdviceDelegator[0];

    static {
        final Class clazz = AdvisableImpl.class;
        try {
            CLASS_INFO = AsmClassInfo.getClassInfo(clazz.getName(), clazz.getClassLoader());
        } catch (Exception e) {
            throw new Error("could not create class info for [" + clazz.getName() + ']');
        }
    }

    private final Advisable m_targetInstance;

    private final TIntObjectHashMap m_aroundAdviceDelegators = new TIntObjectHashMap();
    private final TIntObjectHashMap m_beforeAdviceDelegators = new TIntObjectHashMap();
    private final TIntObjectHashMap m_afterFinallyAdviceDelegators = new TIntObjectHashMap();
    private final TIntObjectHashMap m_afterReturningAdviceDelegators = new TIntObjectHashMap();
    private final TIntObjectHashMap m_afterThrowingAdviceDelegators = new TIntObjectHashMap();

    /**
     * Creates a new mixin impl.
     *
     * @param targetInstance the target for this mixin instance (perInstance deployed)
     */
    public AdvisableImpl(final Object targetInstance) {
        if (!(targetInstance instanceof Advisable)) {
            throw new RuntimeException(
                    "advisable mixin applied to target class that does not implement the Advisable interface"
            );
        }
        m_targetInstance = (Advisable) targetInstance;
    }

    /**
     * @param delegator
     */
    public void aw$addAdviceDelegator(final AdviceDelegator delegator) {
        delegator.register(m_targetInstance);
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AroundAdviceDelegator[] aw$getAroundAdviceDelegators(final int joinPointIndex) {
        Object delegators = m_aroundAdviceDelegators.get(joinPointIndex);
        if (delegators == null) {
            return EMPTY_AROUND_DELEGATOR_ARRAY;
        } else {
            return (AroundAdviceDelegator[]) delegators;
        }
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
        Object delegators = m_beforeAdviceDelegators.get(joinPointIndex);
        if (delegators == null) {
            return EMPTY_BEFORE_DELEGATOR_ARRAY;
        } else {
            return (BeforeAdviceDelegator[]) delegators;
        }
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
        Object delegators = m_afterFinallyAdviceDelegators.get(joinPointIndex);
        if (delegators == null) {
            return EMPTY_AFTER_FINALLY_DELEGATOR_ARRAY;
        } else {
            return (AfterFinallyAdviceDelegator[]) delegators;
        }
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
        Object delegators = m_afterReturningAdviceDelegators.get(joinPointIndex);
        if (delegators == null) {
            return EMPTY_AFTER_RETURNING_DELEGATOR_ARRAY;
        } else {
            return (AfterReturningAdviceDelegator[]) delegators;
        }
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
        Object delegators = m_afterThrowingAdviceDelegators.get(joinPointIndex);
        if (delegators == null) {
            return EMPTY_AFTER_THROWING_DELEGATOR_ARRAY;
        } else {
            return (AfterThrowingAdviceDelegator[]) delegators;
        }
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

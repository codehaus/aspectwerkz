/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.delegation;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface Advisable {

    /**
     * @param delegator
     */
    void aw$addAdviceDelegator(AdviceDelegator delegator);

    /**
     * @param joinPointIndex
     * @return
     */
    AroundAdviceDelegator[] aw$getAroundAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void aw$setAroundAdviceDelegators(int joinPointIndex, AroundAdviceDelegator[] delegators);

    /**
     * @param joinPointIndex
     * @return
     */
    BeforeAdviceDelegator[] aw$getBeforeAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void aw$setBeforeAdviceDelegators(int joinPointIndex, BeforeAdviceDelegator[] delegators);

    /**
     * @param joinPointIndex
     * @return
     */
    AfterFinallyAdviceDelegator[] aw$getAfterFinallyAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void aw$setAfterFinallyAdviceDelegators(int joinPointIndex, AfterFinallyAdviceDelegator[] delegators);

    /**
     * @param joinPointIndex
     * @return
     */
    AfterReturningAdviceDelegator[] aw$getAfterReturningAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void aw$setAfterReturningAdviceDelegators(int joinPointIndex, AfterReturningAdviceDelegator[] delegators);

    /**
     * @param joinPointIndex
     * @return
     */
    AfterThrowingAdviceDelegator[] aw$getAfterThrowingAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void aw$setAfterThrowingAdviceDelegators(int joinPointIndex, AfterThrowingAdviceDelegator[] delegators);
}

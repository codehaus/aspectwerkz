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
    void addAdviceDelegator(AdviceDelegator delegator);

    /**
     * @param joinPointIndex
     * @return
     */
    AroundAdviceDelegator[] getAroundAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void setAroundAdviceDelegators(int joinPointIndex, AroundAdviceDelegator[] delegators);

    /**
     * @param joinPointIndex
     * @return
     */
    BeforeAdviceDelegator[] getBeforeAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void setBeforeAdviceDelegators(int joinPointIndex, BeforeAdviceDelegator[] delegators);

    /**
     * @param joinPointIndex
     * @return
     */
    AfterFinallyAdviceDelegator[] getAfterFinallyAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void setAfterFinallyAdviceDelegators(int joinPointIndex, AfterFinallyAdviceDelegator[] delegators);

    /**
     * @param joinPointIndex
     * @return
     */
    AfterReturningAdviceDelegator[] getAfterReturningAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void setAfterReturningAdviceDelegators(int joinPointIndex, AfterReturningAdviceDelegator[] delegators);

    /**
     * @param joinPointIndex
     * @return
     */
    AfterThrowingAdviceDelegator[] getAfterThrowingAdviceDelegators(final int joinPointIndex);

    /**
     * @param joinPointIndex
     * @param delegators
     */
    void setAfterThrowingAdviceDelegators(int joinPointIndex, AfterThrowingAdviceDelegator[] delegators);
}

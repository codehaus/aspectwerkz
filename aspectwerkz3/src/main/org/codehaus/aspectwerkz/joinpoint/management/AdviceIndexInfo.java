/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.aspectwerkz.AdviceInfo;

/**
 * Holds the around, before and after XXX advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AdviceIndexInfo {
    private final AdviceInfo[] m_aroundAdvices;
    private final AdviceInfo[] m_beforeAdvices;
    private final AdviceInfo[] m_afterFinallyAdvices;
    private final AdviceInfo[] m_afterReturningAdvices;
    private final AdviceInfo[] m_afterThrowingAdvices;

    /**
     * Creates a new info instance.
     *
     * @param aroundAdvices
     * @param beforeAdvices
     * @param afterFinallyAdvices
     * @param afterReturningAdvices
     * @param afterThrowingAdvices
     */
    public AdviceIndexInfo(final AdviceInfo[] aroundAdvices,
                           final AdviceInfo[] beforeAdvices,
                           final AdviceInfo[] afterFinallyAdvices,
                           final AdviceInfo[] afterReturningAdvices,
                           final AdviceInfo[] afterThrowingAdvices) {
        m_aroundAdvices = aroundAdvices;
        m_beforeAdvices = beforeAdvices;
        m_afterFinallyAdvices = afterFinallyAdvices;
        m_afterReturningAdvices = afterReturningAdvices;
        m_afterThrowingAdvices = afterThrowingAdvices;
    }

    public AdviceInfo[] getAroundAdvices() {
        return m_aroundAdvices;
    }

    public AdviceInfo[] getBeforeAdvices() {
        return m_beforeAdvices;
    }

    public AdviceInfo[] getAfterFinallyAdvices() {
        return m_afterFinallyAdvices;
    }

    public AdviceInfo[] getAfterReturningAdvices() {
        return m_afterReturningAdvices;
    }

    public AdviceInfo[] getAfterThrowingAdvices() {
        return m_afterThrowingAdvices;
    }

    /**
     * Extracts the around advices.
     *
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractAroundAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List aroundAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getAroundAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                aroundAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] aroundAdvices = new AdviceInfo[aroundAdviceList.size()];
        i = 0;
        for (Iterator it = aroundAdviceList.iterator(); it.hasNext(); i++) {
            aroundAdvices[i] = (AdviceInfo) it.next();
        }
        return aroundAdvices;
    }

    /**
     * Extracts the before advices.
     *
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractBeforeAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List beforeAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getBeforeAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                beforeAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] beforeAdvices = new AdviceInfo[beforeAdviceList.size()];
        i = 0;
        for (Iterator it = beforeAdviceList.iterator(); it.hasNext(); i++) {
            beforeAdvices[i] = (AdviceInfo) it.next();
        }
        return beforeAdvices;
    }

    /**
     * Extracts the after finally advices.
     *
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractAfterFinallyAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getAfterFinallyAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                afterAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] afterAdvices = new AdviceInfo[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (AdviceInfo) it.next();
        }
        return afterAdvices;
    }

    /**
     * Extracts the after returning advices.
     *
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractAfterReturningAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getAfterReturningAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                afterAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] afterAdvices = new AdviceInfo[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (AdviceInfo) it.next();
        }
        return afterAdvices;
    }

    /**
     * Extracts the after throwing advices.
     *
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractAfterThrowingAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getAfterThrowingAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                afterAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] afterAdvices = new AdviceInfo[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (AdviceInfo) it.next();
        }
        return afterAdvices;
    }
}
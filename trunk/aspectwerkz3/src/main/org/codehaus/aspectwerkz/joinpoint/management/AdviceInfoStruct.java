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
public class AdviceInfoStruct {
    private final List m_aroundAdvices;
    private final List m_beforeAdvices;
    private final List m_afterFinallyAdvices;
    private final List m_afterReturningAdvices;
    private final List m_afterThrowingAdvices;

    /**
     * Creates a new info instance.
     *
     * @param aroundAdvices
     * @param beforeAdvices
     * @param afterFinallyAdvices
     * @param afterReturningAdvices
     * @param afterThrowingAdvices
     */
    public AdviceInfoStruct(
            final List aroundAdvices,
            final List beforeAdvices,
            final List afterFinallyAdvices,
            final List afterReturningAdvices,
            final List afterThrowingAdvices) {
        m_aroundAdvices = aroundAdvices;
        m_beforeAdvices = beforeAdvices;
        m_afterFinallyAdvices = afterFinallyAdvices;
        m_afterReturningAdvices = afterReturningAdvices;
        m_afterThrowingAdvices = afterThrowingAdvices;
    }

    public List getAroundAdvices() {
        return m_aroundAdvices;
    }

    public List getBeforeAdvices() {
        return m_beforeAdvices;
    }

    public List getAfterFinallyAdvices() {
        return m_afterFinallyAdvices;
    }

    public List getAfterReturningAdvices() {
        return m_afterReturningAdvices;
    }

    public List getAfterThrowingAdvices() {
        return m_afterThrowingAdvices;
    }

    /**
     * Extracts the around advices.
     *
     * @param adviceInfoStructs
     * @return
     */
    public final static AdviceInfo[] extractAroundAdvices(final AdviceInfoStruct[] adviceInfoStructs) {
        int i;
        List aroundAdviceList = new ArrayList();
        for (i = 0; i < adviceInfoStructs.length; i++) {
            aroundAdviceList.add(adviceInfoStructs[i].getAroundAdvices());
        }
        AdviceInfo[] aroundAdvices = new AdviceInfo[aroundAdviceList.size()];
        i = 0;
        for (Iterator it = aroundAdviceList.iterator(); it.hasNext(); i++) {
            aroundAdvices[i] = (AdviceInfo)it.next();
        }
        return aroundAdvices;
    }

    /**
     * Extracts the before advices.
     *
     * @param adviceInfoStructs
     * @return
     */
    public final static AdviceInfo[] extractBeforeAdvices(final AdviceInfoStruct[] adviceInfoStructs) {
        int i;
        List beforeAdviceList = new ArrayList();
        for (i = 0; i < adviceInfoStructs.length; i++) {
            beforeAdviceList.add(adviceInfoStructs[i].getBeforeAdvices());
        }
        AdviceInfo[] beforeAdvices = new AdviceInfo[beforeAdviceList.size()];
        i = 0;
        for (Iterator it = beforeAdviceList.iterator(); it.hasNext(); i++) {
            beforeAdvices[i] = (AdviceInfo)it.next();
        }
        return beforeAdvices;
    }

    /**
     * Extracts the after finally advices.
     *
     * @param adviceInfoStructs
     * @return
     */
    public final static AdviceInfo[] extractAfterFinallyAdvices(final AdviceInfoStruct[] adviceInfoStructs) {
        int i;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceInfoStructs.length; i++) {
            afterAdviceList.add(adviceInfoStructs[i].getAfterFinallyAdvices());
        }
        AdviceInfo[] afterAdvices = new AdviceInfo[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (AdviceInfo)it.next();
        }
        return afterAdvices;
    }

    /**
     * Extracts the after returning advices.
     *
     * @param adviceInfoStructs
     * @return
     */
    public final static AdviceInfo[] extractAfterReturningAdvices(final AdviceInfoStruct[] adviceInfoStructs) {
        int i;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceInfoStructs.length; i++) {
            afterAdviceList.add(adviceInfoStructs[i].getAfterReturningAdvices());
        }
        AdviceInfo[] afterAdvices = new AdviceInfo[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (AdviceInfo)it.next();
        }
        return afterAdvices;
    }

    /**
     * Extracts the after throwing advices.
     *
     * @param adviceInfoStructs
     * @return
     */
    public final static AdviceInfo[] extractAfterThrowingAdvices(final AdviceInfoStruct[] adviceInfoStructs) {
        int i;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceInfoStructs.length; i++) {
            afterAdviceList.add(adviceInfoStructs[i].getAfterThrowingAdvices());
        }
        AdviceInfo[] afterAdvices = new AdviceInfo[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (AdviceInfo)it.next();
        }
        return afterAdvices;
    }
}
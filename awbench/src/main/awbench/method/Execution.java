/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.method;

import awbench.Constants;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Execution implements IExecution {

    private int m_count = 0;

    public void before() {
        m_count++;
    }

    public void beforeSJP() {
        m_count++;
    }

    public void beforeJP() {
        m_count++;
    }

    public void beforeWithPrimitiveArgs(int i) {
        m_count++;
    }

    public void beforeWithWrappedArgs(Integer i) {
        m_count++;
    }

    public void beforeWithArgsAndTarget(int i) {
        m_count++;
    }

    public void beforeAfter() {
        m_count++;
    }

    public String afterReturningString() {
        return "afterReturningString";
    }

    public void afterThrowingRTE() throws RuntimeException {
        throw new RuntimeException("test exception");
    }

    public void around_() {
        m_count++;
    }

    public void aroundSJP() {
        m_count++;
    }

    public void aroundJP() {
        m_count++;
    }

    public void aroundStackedWithArgAndTarget(int i) {
        m_count++;
    }

    public void warmup() {
        for (int i = 0; i < 1000; i++) {
            before();
            beforeSJP();
            beforeJP();
            beforeWithPrimitiveArgs(Constants.CONST_0);
            beforeWithWrappedArgs(Constants.WRAPPED_0);
            beforeWithArgsAndTarget(Constants.CONST_0);
            beforeAfter();
            afterReturningString();
            try { afterThrowingRTE(); } catch (Throwable t) {;}
            around_();
            aroundJP();
            aroundSJP();
            aroundStackedWithArgAndTarget(Constants.CONST_0);
        }
    }
}

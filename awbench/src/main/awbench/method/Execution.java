/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.method;

import awbench.Measurement;
import awbench.Constants;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Execution implements Measurement {

    private int m_count = 0;

    public void before() {
        m_count++;
    }

    public void beforeSjp() {
        m_count++;
    }

    public void beforeJp() {
        m_count++;
    }

    public void withPrimitiveArgs(int i) {
        m_count++;
        //int j = i;
    }

    public void withWrappedArgs(Integer i) {
        m_count++;
        //int j = i.intValue();
    }

    public void withArgsAndTarget(int i) {
        m_count++;
        //int j = i;
    }

    public void beforeAfter() {
        m_count++;
    }

    public void aroundJp() {
        m_count++;
    }

    public void warmup() {
        before();
        beforeSjp();
        beforeJp();
        withPrimitiveArgs(Constants.CONST_0);
        withWrappedArgs(Constants.WRAPPED_0);
        withArgsAndTarget(Constants.CONST_0);
        beforeAfter();
        aroundJp();
    }
}

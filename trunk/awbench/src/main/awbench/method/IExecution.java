/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.method;

import awbench.Measurement;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface IExecution extends Measurement {
    void before();

    void beforeSJP();

    void beforeJP();

    void withPrimitiveArgs(int i);

    void withWrappedArgs(Integer i);

    void withArgsAndTarget(int i);

    void beforeAfter();

    String afterReturningString();

    void afterThrowingRTE() throws RuntimeException;

    void aroundJP();

    void aroundSJP();

    void aroundStackedWithArgAndTarget(int i);
}

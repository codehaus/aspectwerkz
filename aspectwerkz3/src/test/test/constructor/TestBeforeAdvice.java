/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.constructor;


/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class TestBeforeAdvice
{
    public TestBeforeAdvice()
    {
        ConstructorAdviceTest.logCall("init ");
        ConstructorAdviceTest.logExecution("init ");
    }
}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.control.DefaultJoinPointController;
import org.codehaus.aspectwerkz.joinpoint.control.JoinPointController;

/**
 * Some other joinpoint controller for testing
 *
 * @author <a href="mailto:"">Stefan Finkenzeller</a>
 */
public class DummyJoinPointController extends DefaultJoinPointController {

    public Object proceed(MethodJoinPoint jp) throws Throwable {
        return super.proceed(jp);
    }

    /**
     * Clones the controller
     */
    public JoinPointController deepCopy() {
        DummyJoinPointController clone = new DummyJoinPointController();
        clone.m_currentAdviceIndex = m_currentAdviceIndex;
        clone.m_currentPointcutIndex = m_currentPointcutIndex;
        return clone;
    }

}

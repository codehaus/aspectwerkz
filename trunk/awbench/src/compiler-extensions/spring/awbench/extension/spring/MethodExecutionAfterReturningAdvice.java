/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.extension.spring;

import java.lang.reflect.Method;
import org.springframework.aop.AfterReturningAdvice;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class MethodExecutionAfterReturningAdvice implements AfterReturningAdvice {
    public static int s_count = 0;
    public void afterReturning(Object object, Method m, Object[] args, Object target) throws Throwable {
        s_count++;
    }
}

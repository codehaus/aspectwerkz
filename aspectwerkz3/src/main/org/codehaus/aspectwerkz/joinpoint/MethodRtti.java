/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

import java.lang.reflect.Method;

/**
 * Interface for the method RTTI (Runtime Type Information).
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public interface MethodRtti extends CodeRtti {
    /**
     * Returns the method.
     * 
     * @return the method
     */
    Method getMethod();

    /**
     * Returns the return type.
     * 
     * @return the return type
     */
    Class getReturnType();

    /**
     * Returns the value of the return type.
     * 
     * @return the value of the return type
     */
    Object getReturnValue();
}
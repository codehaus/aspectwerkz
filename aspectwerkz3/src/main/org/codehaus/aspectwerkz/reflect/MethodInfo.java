/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect;


/**
 * Interface for the method info implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public interface MethodInfo extends MemberInfo {
    /**
    * Returns the return type.
    *
    * @return the return type
    */
    ClassInfo getReturnType();

    /**
    * Returns the parameter types.
    *
    * @return the parameter types
    */
    ClassInfo[] getParameterTypes();

    /**
    * Returns the exception types.
    *
    * @return the exception types
    */
    ClassInfo[] getExceptionTypes();
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

/**
 * Interface for the method metadata implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface MethodMetaData extends MemberMetaData {
    /**
     * Returns the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the modifiers.
     *
     * @return the modifiers
     */
    int getModifiers();

    /**
     * Returns the return type.
     *
     * @return the return type
     */
    String getReturnType();

    /**
     * Returns the parameter types.
     *
     * @return the parameter types
     */
    String[] getParameterTypes();

    /**
     * Returns the exception types.
     *
     * @return the exception types
     */
    String[] getExceptionTypes();
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

/**
 * Interface for the catch clause RTTI (Runtime Type Information).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @TODO rename to HandlerRtti
 */
public interface CatchClauseRtti extends Rtti {
    /**
     * Returns the parameter type.
     *
     * @return the parameter type
     */
    Class getParameterType();

    /**
     * Returns the value of the parameter.
     *
     * @return the value of the parameter
     */
    Object getParameterValue();

    /**
     * @param parameterValue the value of the parameter
     * @TODO remove in 2.0
     * <p/>
     * Sets the value of the parameter.
     */
    void setParameterValue(Object parameterValue);
}
/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;


/**
 * Interface for the constructor metadata implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface ConstructorMetaData extends MemberMetaData
{
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

    static class NullConstructorMetaData extends NullMemberMetaData
        implements ConstructorMetaData
    {
        public final static NullConstructorMetaData NULL_CONSTRUCTOR_METADATA = new NullConstructorMetaData();

        public String[] getParameterTypes()
        {
            return EMPTY_STRING_ARRAY;
        }

        public String[] getExceptionTypes()
        {
            return EMPTY_STRING_ARRAY;
        }
    }
}

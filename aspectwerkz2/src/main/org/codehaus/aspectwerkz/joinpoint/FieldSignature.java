/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface FieldSignature extends MemberSignature {

    /**
     * Returns the field type.
     *
     * @return the field type
     */
    Class getFieldType();

    /**
     * Returns the value of the field.
     *
     * @return the value of the field
     */
    Object getFieldValue();

    /**
     * Sets the value of the field.
     *
     * @param fieldValue the value of the field
     */
    void setFieldValue(final Object fieldValue);
}

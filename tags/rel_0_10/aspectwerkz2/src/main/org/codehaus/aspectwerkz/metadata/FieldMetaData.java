/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.lang.reflect.Modifier;

/**
 * Interface for the field metadata implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface FieldMetaData extends MemberMetaData {

    /**
     * Returns the type.
     *
     * @return the type
     */
    String getType();

    static class NullFieldMetaData extends NullMemberMetaData implements FieldMetaData {
        public final static NullFieldMetaData NULL_FIELD_METADATA = new NullFieldMetaData();
        public String getType() {return "";}
    }
}

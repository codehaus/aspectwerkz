/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

/**
 * Marker interface for the member meta-data classes (field and method).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface MemberMetaData extends MetaData {

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

    static class NullMemberMetaData extends NullMetaData implements MemberMetaData {
        public final static NullMemberMetaData NULL_MEMBER_METADATA = new NullMemberMetaData();
        public String getName() {return "";}
        public int getModifiers() {return -1;}
    }
}

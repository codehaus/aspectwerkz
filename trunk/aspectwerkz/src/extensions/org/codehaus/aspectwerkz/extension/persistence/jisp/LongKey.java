/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence.jisp;

/**
 * Extends the com.coyotegulch.jisp.LongKey from the JISP distribution.
 * Provides the possibility to build the key based on
 * the java.lang.Long type.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @see com.coyotegulch.jisp.LongKey
 */
public class LongKey extends com.coyotegulch.jisp.LongKey {

    /**
     * Constructor.
     */
    public LongKey() {
        super();
    }

    /**
     * Constructor.
     *
     * @param key the key
     */
    public LongKey(final long key) {
        super(key);
    }

    /**
     * Constructor.
     *
     * @param key the key as a long value wrapped in a java.lang.Long
     */
    public LongKey(final Long key) {
        super(key.longValue());
    }
}



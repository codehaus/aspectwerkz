/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.extension.persistence.jisp;

/**
 * Extends the com.coyotegulch.jisp.LongKey from the JISP distribution.
 * Provides the possibility to build the key based on
 * the java.lang.Long type.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: LongKey.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
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



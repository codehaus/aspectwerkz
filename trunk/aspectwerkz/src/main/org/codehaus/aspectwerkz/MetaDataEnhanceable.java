/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

/**
 * Interface that allows us to retrieve meta-data from the implementing class.
 * This interface along with a implementation of it is added to all transformed
 * classes when loaded.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface MetaDataEnhanceable {

    /**
     * Returns the meta-data for the implementing class.
     *
     * @param key the key for the meta-data
     * @return the meta-data itself
     */
    Object ___AW_getMetaData(final Object key);

    /**
     * Adds new meta-data to the implemeting class.
     *
     * @param key the key for the meta-data
     * @param value the meta-data itself
     */
    void ___AW_addMetaData(final Object key, final Object value);
}

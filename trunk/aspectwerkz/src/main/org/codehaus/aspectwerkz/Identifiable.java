/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

/**
 * Interface that allows us to make the added UUID field accessible without the
 * need for reflection. All transformed classes are are enhanced to implement
 * this interface.
 *
 * @see org.codehaus.aspectwerkz.transform.AddUuidTransformer
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface Identifiable {

    /**
     * Returns the UUID for the object implementing the interface.
     *
     * @return the UUID
     */
    String ___AW_getUuid();
}

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

/**
 * Interface that allows us to make the added UUID field accessible without the
 * need for reflection. All transformed classes are are enhanced to implement
 * this interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @see org.codehaus.aspectwerkz.transform.AddUuidTransformer
 */
public interface Identifiable {

    /**
     * Returns the UUID for the object implementing the interface.
     *
     * @return the UUID
     */
    String ___AW_getUuid();
}

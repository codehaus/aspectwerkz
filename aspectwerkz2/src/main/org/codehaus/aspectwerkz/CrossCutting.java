/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

/**
 * Interface that all classes that want to be "cross-cutting" must implemented.
 * <p/>
 * This is normally handled transparently by the framework itself and the users only needs to use this
 * interface when retrieving cross-cutting information.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface CrossCutting {

    /**
     * Returns the cross-cutting info for the class.
     *
     * @return the cross-cutting info
     */
    CrossCuttingInfo getCrossCuttingInfo();
}

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

/**
 * Interface that all classes that want to be "cross-cuttable" must implemented.
 * <p/>
 * This is normally handled transparently by the framework itself and the users only needs to use this
 * interface when retrieving cross-cutting information.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public interface CrossCuttable {
    
    /**
     * Returns the cross-cutting info for the class.
     *
     * @return the cross-cutting info
     */
    CrossCuttingInfo getCrossCuttingInfo();

    /**
     * Sets the cross-cutting info for the class.
     *
     * @param info the cross-cutting info
     */
    void setCrossCuttingInfo(CrossCuttingInfo info);
}

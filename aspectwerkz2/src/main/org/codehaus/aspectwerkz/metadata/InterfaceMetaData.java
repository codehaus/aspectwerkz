/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.List;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface InterfaceMetaData extends MetaData {
    /**
     * Returns the name of the class.
     *
     * @return the name of the class
     */
    String getName();

    /**
     * Returns the interfaces.
     *
     * @return the interfaces
     */
    List getInterfaces();
}

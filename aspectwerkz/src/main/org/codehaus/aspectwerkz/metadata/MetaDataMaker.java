/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Interface for the meta-data maker implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MetaDataMaker {

    /**
     * Caches the class meta-data.
     */
    protected static final Map s_classMetaDataCache = new WeakHashMap();

    /**
     * Caches the interface meta-data.
     */
    protected static final Map s_interfaceMetaDataCache = new WeakHashMap();
}

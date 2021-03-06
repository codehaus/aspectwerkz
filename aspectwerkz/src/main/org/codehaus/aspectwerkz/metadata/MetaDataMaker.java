/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Base class for the meta-data makers.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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

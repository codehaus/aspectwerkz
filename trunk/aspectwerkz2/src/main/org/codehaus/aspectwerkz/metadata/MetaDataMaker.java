/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MetaDataMaker {

    /**
     * The name of all constructors in the pattern language.
     */
    public static final String CONSTRUCTOR_NAME = "new";

    /**
     * Caches the class meta-data.
     */
    protected static final Map s_classMetaDataCache = new WeakHashMap();

    /**
     * Caches the interface meta-data.
     */
    protected static final Map s_interfaceMetaDataCache = new WeakHashMap();

    public static void invalidateClassMetaData(String className) {
        synchronized (s_classMetaDataCache) {
            s_classMetaDataCache.remove(className);
        }
    }
}

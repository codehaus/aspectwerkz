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
import java.util.HashMap;

/**
 * Base class for the meta-data makers.
 * <p/>
 * To support ClassLoader hierarchy there is one MetaDataMaker per ClassLoader, even if this
 * leads to some space waste.
 * TODO have a lazy way for MD retrieval
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MetaDataMaker {

    private JavassistMetaDataMaker m_javassistMetaDataMaker;
    private ReflectionMetaDataMaker m_reflectionMetaDataMaker;

    public MetaDataMaker() {
        m_javassistMetaDataMaker = new JavassistMetaDataMaker(this);
        m_reflectionMetaDataMaker = new ReflectionMetaDataMaker(this);
    }

    /**
     * The MetaDataMaker repository per ClassLoader
     */
    protected static final Map s_metaDataMakers = new WeakHashMap();

    /**
     * The name of all constructors in the pattern language.
     */
    public static final String CONSTRUCTOR_NAME = "new";

    /**
     * Caches the class meta-data.
     */
    protected final Map m_classMetaDataCache = new HashMap();

    /**
     * Caches the interface meta-data.
     */
    protected final Map m_interfaceMetaDataCache = new HashMap();

    /**
     * Removes klass metadata
     * We need to handle the ClassLoader hierarchy
     *
     * @param klass whose metadata must be removed
     */
    public static synchronized void invalidateClassMetaData(Class klass) {
        ClassLoader loader = klass.getClassLoader();
        while (loader != null) {
            MetaDataMaker maker = (MetaDataMaker)s_metaDataMakers.get(loader);
            maker.m_classMetaDataCache.remove(klass.getName());
            loader = loader.getParent();
        }
    }

    private static synchronized MetaDataMaker getMetaDataMaker(ClassLoader loader) {
        MetaDataMaker metaDataMaker = (MetaDataMaker)s_metaDataMakers.get(loader);
        if (metaDataMaker == null) {
            metaDataMaker = new MetaDataMaker();
            s_metaDataMakers.put(loader, metaDataMaker);
        }
        return metaDataMaker;
    }

    public static JavassistMetaDataMaker getJavassistMetaDataMaker(ClassLoader loader) {
        return getMetaDataMaker(loader).m_javassistMetaDataMaker;
    }

    public static ReflectionMetaDataMaker getReflectionMetaDataMaker(ClassLoader loader) {
        return getMetaDataMaker(loader).m_reflectionMetaDataMaker;
    }


}

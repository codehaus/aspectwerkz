/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper;
import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;
import org.codehaus.aspectwerkz.transform.ClassCacheTuple;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Stores the AspectSystem on a per ClassLoader basis. <p/>The <code>getSystem</code> method checks for system
 * initialization. <p/>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public final class SystemLoader {
    /**
     * Holds references to all the systems defined. Maps the ClassLoader to a matching system instance.
     */
    private static final Map s_systems = new WeakHashMap();

    /**
     * Returns the System for a specific ClassLoader. If the system is not initialized, register the ClassLoader
     * hierarchy and all the definitions to initialize the system.
     *
     * @param loader the ClassLoader
     * @return the System instance for this ClassLoader
     */
    public synchronized static AspectSystem getSystem(final ClassLoader loader) {
        AspectSystem system = (AspectSystem)s_systems.get(loader);
        if (system == null) {
            List defs = SystemDefinitionContainer.getHierarchicalDefs(loader);
            system = new AspectSystem(loader, defs);
            s_systems.put(loader, system);
        }
        return system;
    }

    /**
     * Returns the System for a specific instance. The instance class ClassLoader is queried. TODO: avoid bootCL lookup
     *
     * @param instance
     * @return the System instance for the instance class ClassLoader
     */
    public static AspectSystem getSystem(Object instance) {
        return getSystem(instance.getClass().getClassLoader());
    }

    /**
     * Returns the System for a specific class. The class ClassLoader is queried. TODO: avoid bootCL lookup
     *
     * @param klass
     * @return the System instance for the class ClassLoader
     */
    public static AspectSystem getSystem(Class klass) {
        return getSystem(klass.getClassLoader());
    }

    /**
     * Returns all systems.
     *
     * @return
     */
    public static Collection getAllSystems() {
        return s_systems.values();
    }
}
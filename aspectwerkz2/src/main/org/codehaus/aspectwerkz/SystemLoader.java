/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.util.Map;
import java.util.HashMap;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;

/**
 * Loads the different types of system. Caches the system, mapped to its id.
 * <p/>
 * TODO: put this class in the same package as the System impl. and set the constructor to package private
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SystemLoader {

    /**
     * Holds references to all the systems defined.
     * Maps the UUID to a matching system instance.
     */
    private static final Map s_systems = new HashMap();

    /**
     * Returns the system with a specific UUID.
     * <p/>
     * //     * @TODO: is this caching a bottleneck, since it req. the method to be synchronized? Is there a better impl.?
     *
     * @param uuid the UUID for the system
     * @return the system for the UUID specified
     */
    public synchronized static System getSystem(final String uuid) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");

        final SystemDefinition definition = DefinitionLoader.getDefinition(ContextClassLoader.getLoader(),
                uuid);
        try {
            System system = (System) s_systems.get(uuid);
            if (system == null) {
                system = new System(uuid, definition);
                s_systems.put(uuid, system);
            }
            return system;
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the default system.
     * <p/>
     * Only to be used when ONE definition is used per JVM and when no system id has been specified in the definition.
     *
     * @return the default system
     */
    public synchronized static System getDefaultSystem() {
        final SystemDefinition definition = DefinitionLoader.getDefinition(ContextClassLoader.getLoader(),
                System.DEFAULT_SYSTEM);
        try {
            System system = (System) s_systems.get(System.DEFAULT_SYSTEM);
            if (system == null) {
                system = new System(System.DEFAULT_SYSTEM, definition);
                s_systems.put(System.DEFAULT_SYSTEM, system);
            }
            return system;
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Loads the different types of system. Caches the system, mapped to its id.
 * <p/>
 * TODO: put this class in the same package as the System impl. and set the constructor to package private
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SystemLoader {

    /**
     * Holds references to all the systems defined. Maps the UUID to a matching system instance.
     */
    private static final Map s_systems = new HashMap();

    /**
     * Returns the system with a specific UUID.
     * <p/>
     *
     * @param uuid the UUID for the system
     * @return the system for the UUID specified
     * @TODO: is this caching a bottleneck, since it req. the method to be synchronized? Is there a better impl.?
     */
    public synchronized static System getSystem(final String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid can not be null");
        }
        try {
            System system = (System)s_systems.get(uuid);
            if (system == null) {
                final SystemDefinition definition = DefinitionLoader.getDefinition(
                        ContextClassLoader.getLoader(), uuid
                );
                system = new System(uuid, definition);
                s_systems.put(uuid, system);
            }
            return system;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * @TODO: stupid method, should be removed
     *
     * Returns the first system found, if not found throws an exception.
     *
     * @return the system
     */
    public synchronized static System getSystem() {
        try {
            Set systems = s_systems.entrySet();
            Iterator it = systems.iterator();
            if (it.hasNext()) {
                System system = (System)it.next();
                s_systems.put(system.getUuid(), system);
                return system;
            }
            else {
                throw new DefinitionException("no aspect system defined");
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }
}

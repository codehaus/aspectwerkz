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
import java.lang.reflect.Constructor;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;

/**
 * Loads the different types of system. Uses cache.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SystemLoader {

    /**
     * Holds references to all the AspectWerkz systems defined.
     * Maps the UUID to a matching AspectWerkz instance.
     */
    private static final Map s_systems = new HashMap();

    /**
     * The class name of the attrib def model system.
     */
    private static final String ATTRIB_DEF_SYSTEM_CLASS_NAME = "org.codehaus.aspectwerkz.attribdef.AttribDefSystem";

    /**
     * The class name of the xml def model system.
     */
    private static final String XML_DEF_SYSTEM_CLASS_NAME = "org.codehaus.aspectwerkz.xmldef.XmlDefSystem";

    /**
     * The constructor of the attrib def model system.
     */
    private static final Constructor ATTRIB_DEF_SYSTEM_CONSTRUCTOR;

    /**
     * The constructor of the xml def model system.
     */
    private static final Constructor XML_DEF_SYSTEM_CONSTRUCTOR;

    static {
        try {
            ATTRIB_DEF_SYSTEM_CONSTRUCTOR = ContextClassLoader.loadClass(ATTRIB_DEF_SYSTEM_CLASS_NAME).
                    getDeclaredConstructor(new Class[]{String.class, AspectWerkzDefinition.class});
            ATTRIB_DEF_SYSTEM_CONSTRUCTOR.setAccessible(true);
            XML_DEF_SYSTEM_CONSTRUCTOR = ContextClassLoader.loadClass(XML_DEF_SYSTEM_CLASS_NAME).
                    getDeclaredConstructor(new Class[]{String.class, AspectWerkzDefinition.class});
            XML_DEF_SYSTEM_CONSTRUCTOR.setAccessible(true);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the AspectWerkz system, no system UUID is needed to be specified.
     * <p/>
     * Only to be used when ONE definition is used per JVM.
     *
     * @return the default system
     */
    public synchronized static System getDefaultSystem() {
        final AspectWerkzDefinition definition =
                DefinitionLoader.getDefinition(System.DEFAULT_SYSTEM);

        System system = null;
        try {
            system = (System)s_systems.get(System.DEFAULT_SYSTEM);
            if (system == null) {
                if (definition.isXmlDef()) {
                    //TODO: remove the sync bloc - unsafe see AW-98
                    synchronized (s_systems) {
                        system = (System)XML_DEF_SYSTEM_CONSTRUCTOR.newInstance(
                                new Object[]{System.DEFAULT_SYSTEM, definition}
                        );
                        s_systems.put(System.DEFAULT_SYSTEM, system);
                    }
                }
                else if (definition.isAttribDef()) {
                    //TODO: remove the sync bloc - unsafe see AW-98
                    synchronized (s_systems) {
                        system = (System)ATTRIB_DEF_SYSTEM_CONSTRUCTOR.newInstance(
                                new Object[]{System.DEFAULT_SYSTEM, definition}
                        );
                        s_systems.put(System.DEFAULT_SYSTEM, system);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return system;
    }

    /**
     * Returns the AspectWerkz system with a specific UUID.
     *
     * @param uuid the UUID for the system
     * @return the system for the UUID specified
     */
    public synchronized static System getSystem(final String uuid) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");

        final AspectWerkzDefinition definition = DefinitionLoader.getDefinition(uuid);

        System system = null;
        try {
            system = (System)s_systems.get(uuid);
            if (system == null) {
                if (definition.isXmlDef()) {
                    //TODO: remove the sync bloc - unsafe see AW-98
                    synchronized (s_systems) {
                        // TODO: makes the clapp test fail, why?
                        system = (System)XML_DEF_SYSTEM_CONSTRUCTOR.newInstance(
                                new Object[]{uuid, definition}
                        );
//                        system = new org.codehaus.aspectwerkz.xmldef.XmlDefSystem(uuid, definition);
                        s_systems.put(uuid, system);
                    }
                }
                else if (definition.isAttribDef()) {
                    //TODO: remove the sync bloc - unsafe see AW-98
                    synchronized (s_systems) {
                        system = (System)ATTRIB_DEF_SYSTEM_CONSTRUCTOR.newInstance(
                                new Object[]{uuid, definition}
                        );
//                        system = new org.codehaus.aspectwerkz.attribdef.AttribDefSystem(uuid, definition);
                        s_systems.put(uuid, system);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return system;
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.util.*;

import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper;
import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;
import org.codehaus.aspectwerkz.transform.ClassCacheTuple;
import org.codehaus.aspectwerkz.extension.hotswap.HotSwapClient;

/**
 * Stores the AspectSystem on a per ClassLoader basis.<p/>
 * The <code>getSystem</code> method checks for system initialisation.
 * <p/>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class SystemLoader {

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
    public synchronized static AspectSystem getSystem(ClassLoader loader) {
        AspectSystem system = (AspectSystem)s_systems.get(loader);
        if (system == null) {
            SystemDefinitionContainer.registerClassLoader(loader);
            List defs = SystemDefinitionContainer.getHierarchicalDefs(loader);
            system = new AspectSystem(loader, defs);
            s_systems.put(loader, system);
        }
        return system;
    }

    /**
     * Returns the System for a specific instance. The instance class ClassLoader is queried.
     * TODO: avoid bootCL lookup
     *
     * @param instance
     * @return the System instance for the instance class ClassLoader
     */
    public static AspectSystem getSystem(Object instance) {
        return getSystem(instance.getClass().getClassLoader());

    }

    /**
     * Returns the System for a specific class. The class ClassLoader is queried.
     * TODO: avoid bootCL lookup
     *
     * @param klass
     * @return the System instance for the class ClassLoader
     */
    public static AspectSystem getSystem(Class klass) {
        return getSystem(klass.getClassLoader());
    }

    private SystemLoader() {
    }

    public static Collection getAllSystems() {
        return s_systems.values();
    }

    public static synchronized void deploySystemDefinitions(ClassLoader loader, List definitions, boolean activate) {
        SystemDefinitionContainer.deploySystemDefinitions(loader, definitions);

        //TODO check uuid in the bottom hierarchy
        AspectSystem system = getSystem(loader);
        AspectManager[] currentAspectManagers = system.getAspectManagers();

        AspectManager[] newAspectManagers = new AspectManager[currentAspectManagers.length + definitions.size()];
        System.arraycopy(currentAspectManagers, 0, newAspectManagers, 0, currentAspectManagers.length);
        int index = currentAspectManagers.length;
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            newAspectManagers[index++] = new AspectManager(system, (SystemDefinition)it.next());
        }

        // now we should grab all subclassloader' AspectSystem and rebuild em
        Collection systems = SystemLoader.getAllSystems();
        for (Iterator it = systems.iterator(); it.hasNext();) {
            AspectSystem aspectSystem = (AspectSystem)it.next();
            if (isChildOfOrEqual(aspectSystem.getDefiningClassLoader(), loader)) {
                system.propagateAspectManagers(newAspectManagers, currentAspectManagers.length);
            }
        }

        // hotswap if needed
        if (activate) {
            //TODO find a better way to trigger that
            // the singleton idea of AWPP is boring
            AspectWerkzPreProcessor awpp = (AspectWerkzPreProcessor) ClassPreProcessorHelper.getClassPreProcessor();
            for (Iterator it = awpp.getClassCacheTuples().iterator(); it.hasNext();) {
                ClassCacheTuple tuple = (ClassCacheTuple)it.next();
                if (isChildOfOrEqual(tuple.getClassLoader(), loader)) {
                    try {
                        System.out.println("hotswap = " + tuple.getClassName());
                        // TODO - HotSwap is in extensions // HotSwapClient.hotswap(tuple.getClassLoader().loadClass(tuple.getClassName()));
                    } catch (Throwable t) {
                        System.err.println("<WARN> " + t.getMessage());
                    }
                }
            }
        }


    }

    private static boolean isChildOfOrEqual(ClassLoader loader, ClassLoader parent) {
        if (loader.equals(parent))
            return true;
        ClassLoader currentParent = loader.getParent();
        while (currentParent != null) {
            if (currentParent.equals(parent)) {
                return true;
            }
            currentParent = currentParent.getParent();
        }
        return false;
    }



}

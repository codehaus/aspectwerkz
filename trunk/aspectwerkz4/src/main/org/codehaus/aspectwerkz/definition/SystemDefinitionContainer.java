/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.util.List;
import java.io.File;

/**
 * The SystemDefintionContainer maintains all the definition and is aware of the classloader hierarchy. <p/>A
 * ThreadLocal structure is used during weaving to store current classloader defintion hierarchy. <p/>Due to
 * getResources() API, we maintain a perClassLoader loaded resource list so that it contains only resource defined
 * within the classloader and not its parent.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class SystemDefinitionContainer {
    /**
     * Map of SystemDefinition[List] per ClassLoader.
     * NOTE: null key is supported
     */
    public static final Map s_classLoaderSystemDefinitions = new WeakHashMap();
    /**
     * Map of SystemDefinition[List] per ClassLoader, with the hierarchy structure
     * NOTE: null key is supported
     */
    public static final Map s_classLoaderHierarchicalSystemDefinitions = new WeakHashMap();

    /**
     * Map of SystemDefinition location (as URL[List]) per ClassLoader
     * NOTE: null key is supported
     */
    public static final Map s_classLoaderDefinitionLocations = new WeakHashMap();

    /**
     * Default location for default AspectWerkz definition file, JVM wide
     */
    public static final String URL_JVM_OPTION_SYSTEM = System.getProperty(
            "aspectwerkz.definition.file",
            "no -Daspectwerkz.definition.file"
    );

    /**
     * The AOP deployment descriptor for any deployed unit Note: Tomcat 5 does not handles war/META-INF
     */
    public static final String AOP_META_INF_XML_FILE = "META-INF/aop.xml";

    /**
     * The AOP deployment descriptor for any deployed unit in a webapp TODO for EAR/EJB/JCA stuff
     */
    public static final String AOP_WEB_INF_XML_FILE = "../aop.xml";

    public static final String WEB_WEB_INF_XML_FILE = "../web.xml";

    /**
     * An internal flag to disable registration of the -Daspectwerkz.definition.file definition in the System class
     * loader. This is used only in offline mode, where these definitions are registered programmatically at the
     * compilation class loader level.
     */
    private static boolean s_disableSystemWideDefinition = false;

    /**
     * The virtual systems, one per class loader.
     */
    private static final Map s_virtualSystems = new WeakHashMap();

    /**
     * Register a new ClassLoader in the system and gather all its definition and parents definitions.
     *
     * @param loader the class loader to register
     */
    public static void registerClassLoader(final ClassLoader loader) {
        if (s_classLoaderSystemDefinitions.containsKey(loader)) {
            return;
        }
        // skip boot classloader and ext classloader
        if (loader == null) {
            return;
        }

        // register parents first
        registerClassLoader(loader.getParent());

        // then register -D.. if system classloader and then all META-INF/aop.xml
        try {
            final Set definitions = new HashSet();
            final List locationOfDefinitions = new ArrayList();

            // early registration to avoid recursion
            s_classLoaderSystemDefinitions.put(loader, definitions);
            s_classLoaderDefinitionLocations.put(loader, locationOfDefinitions);

            // is this system classloader ?
            if ((loader == ClassLoader.getSystemClassLoader()) && !s_disableSystemWideDefinition) {
                // -D..file=... sysdef
                definitions.addAll(DefinitionLoader.getDefaultDefinition(loader));
                locationOfDefinitions.add(new File(URL_JVM_OPTION_SYSTEM).toURL());
            }
            if (loader.getResource(WEB_WEB_INF_XML_FILE) != null) {
                Enumeration webres = loader.getResources(AOP_WEB_INF_XML_FILE);
                while (webres.hasMoreElements()) {
                    URL def = (URL) webres.nextElement();
                    if (isDefinedBy(loader, def)) {
                        ;
                    } else {
                        definitions.addAll(XmlParser.parseNoCache(loader, def));
                        locationOfDefinitions.add(def);
                    }
                }
            }
            Enumeration res = loader.getResources(AOP_META_INF_XML_FILE);
            while (res.hasMoreElements()) {
                URL def = (URL) res.nextElement();
                if (isDefinedBy(loader, def)) {
                    ;
                } else {
                    definitions.addAll(XmlParser.parseNoCache(loader, def));
                    locationOfDefinitions.add(def);
                }
            }
            dump(loader);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Hotdeploy a list of SystemDefintions as defined at the level of the given ClassLoader
     * <p/>Note: this is used for Offline mode.
     * TODO: sync StartupManager TODO: flush sub systems defs or allow different organization if wished so?
     *
     * @param loader      ClassLoader
     * @param definitions SystemDefinitions list
     */
    public static void deployDefinitions(final ClassLoader loader, final Set definitions) {
        registerClassLoader(loader);
        Set defs = (Set) s_classLoaderSystemDefinitions.get(loader);
        defs.addAll(definitions);
        dump(loader);
    }

    /**
     * Lookup for a given SystemDefinition by uuid within a given ClassLoader The lookup does not go thru the
     * ClassLoader hierarchy
     *
     * @param loader ClassLoader
     * @param uuid   system uuid
     * @return SystemDefinition or null if no such defined definition
     */
    public static SystemDefinition getDefinitionFor(final ClassLoader loader, final String uuid) {
        getHierarchicalDefinitionsFor(loader);
        for (Iterator defs = getDefinitionsFor(loader).iterator(); defs.hasNext();) {
            SystemDefinition def = (SystemDefinition) defs.next();
            if (def.getUuid().equals(uuid)) {
                return def;
            }
        }
        return null;
    }

    /**
     * Return the list of SystemDefinitions defined at the given ClassLoader level. Does not handles the ClassLoader
     * hierarchy.
     *
     * @param loader
     * @return SystemDefinitions list
     */
    public static Set getDefinitionsFor(final ClassLoader loader) {
        getHierarchicalDefinitionsFor(loader);
        return (Set) s_classLoaderSystemDefinitions.get(loader);
    }

    /**
     * Returns all the system definitions, including the virtual system.
     *
     * @param loader
     * @return
     */
    public static Set getRegularAndVirtualDefinitionsFor(final ClassLoader loader) {
        final Set allDefs = new HashSet();
        allDefs.addAll(getDefinitionsFor(loader));
        allDefs.add(getVirtualDefinitionFor(loader));
        return allDefs;
    }

    /**
     * Returns the virtual system for the class loader specified.
     *
     * @param loader the class loader
     * @return the virtual system
     */
    public static SystemDefinition getVirtualDefinitionFor(final ClassLoader loader) {
        final SystemDefinition virtualSystemDef;
        if (!s_virtualSystems.containsKey(loader)) {
            virtualSystemDef = new SystemDefinition(new Integer(loader.hashCode()).toString());
            s_virtualSystems.put(loader, virtualSystemDef);
        } else {
            virtualSystemDef = (SystemDefinition) s_virtualSystems.get(loader);
        }
        return virtualSystemDef;
    }

    /**
     * Returns the list of all ClassLoaders registered so far Note: when a child ClassLoader is registered, all its
     * parent hierarchy is registered
     *
     * @return ClassLoader Set
     */
    public static Set getAllRegisteredClassLoaders() {
        return s_classLoaderSystemDefinitions.keySet();
    }

    /**
     * Turns on the option to avoid -Daspectwerkz.definition.file handling.
     */
    public static void disableSystemWideDefinition() {
        s_disableSystemWideDefinition = true;
    }

    /**
     * Returns the gathered SystemDefinition visible from a classloader. <p/>This method is using a cache. Caution when
     * modifying this method since when an aop.xml is loaded, the aspect classes gets loaded as well, which triggers
     * this cache, while the system is in fact not yet initialized properly. </p>
     *
     * @param loader
     * @return set with the system definitions
     */
    private static synchronized Set getHierarchicalDefinitionsFor(final ClassLoader loader) {
        // check cache
        final Set defs;
        if (!s_classLoaderHierarchicalSystemDefinitions.containsKey(loader)) {
            // if runtime access before load time
            if (!s_classLoaderSystemDefinitions.containsKey(loader)) {
                registerClassLoader(loader);
            }
            defs = new HashSet();
            // put it in the cache now since this method is recursive
            s_classLoaderHierarchicalSystemDefinitions.put(loader, defs);
            if (loader == null) {
                ; // go on to put in the cache at the end
            } else {
                ClassLoader parent = loader.getParent();
                defs.addAll(getHierarchicalDefinitionsFor(parent));
                defs.addAll((Set) s_classLoaderSystemDefinitions.get(loader));
            }
        } else {
            defs = ((Set) s_classLoaderHierarchicalSystemDefinitions.get(loader));
        }
        return defs;
    }

    /**
     * Check if a given resource has already been registered to a classloader and its parent hierachy
     *
     * @param loader the classloader which might define the resource
     * @param def    the resource
     * @return true if classloader or its parent defines the resource
     * @TODO what if child shares parent path?
     * @TODO What happens with smylinking and xml in jars etc ?
     * @TODO Needs test
     * @TODO No need for the s_ map
     * @TODO KICK the def map and crawl up the CL parents and redo a getResources check instead
     */
    private static boolean isDefinedBy(final ClassLoader loader, final URL def) {
        if (loader == null) {
            return false;
        }
        ArrayList defLocation = (ArrayList) s_classLoaderDefinitionLocations.get(loader);
        if (defLocation != null) {
            for (Iterator it = defLocation.iterator(); it.hasNext();) {
                URL definedDef = (URL) it.next();
                if (definedDef.sameFile(def)) {
                    return true;
                }
            }
        }
        return isDefinedBy(loader.getParent(), def);
    }

    /**
     * Pretty dump a classloader
     *
     * @param loader
     */
    private static void dump(final ClassLoader loader) {
        if (!AspectWerkzPreProcessor.VERBOSE) {
            return;
        }

        StringBuffer dump = new StringBuffer("******************************************************************");
        dump.append("\n* ClassLoader = ");

        //Note: Tomcat classLoader.toString is too verbose so we allow 120 chars.
        if ((loader != null) && (loader.toString().length() < 120)) {
            dump.append(loader.toString());
        } else if (loader != null) {
            dump.append(loader.getClass().getName()).append("@").append(loader.hashCode());
        } else {
            dump.append("null");
        }

        Set defs = (Set) s_classLoaderSystemDefinitions.get(loader);
        for (Iterator it = defs.iterator(); it.hasNext();) {
            SystemDefinition def = (SystemDefinition) it.next();
            dump.append("\n* SystemID = ").append(def.getUuid());
            dump.append(", ").append(def.getAspectDefinitions().size()).append(" aspects.");
        }
        for (Iterator it = ((List) s_classLoaderDefinitionLocations.get(loader)).iterator(); it.hasNext();) {
            dump.append("\n* ").append(it.next());
        }
        dump.append("\n******************************************************************");
        System.out.println(dump.toString());
    }
}
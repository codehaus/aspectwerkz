/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.XmlParser;
import org.codehaus.aspectwerkz.definition.SystemDefinition;

import java.util.*;
import java.net.URL;
import java.io.File;

/**
 * The SystemDefintionContainer maintains all the definition and is aware
 * of the classloader hierarchy.
 * <br/>
 * A ThreadLocal structure is used during weaving to store current classloader
 * defintion hierarchy.
 * <br/>
 * Due to getResources() API, we maintain a perClassLoader loaded resource list so that
 * it contains only resource defined within the classloader and not its parent.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class SystemDefinitionContainer {

    /** Map of SystemDefintion[List] per ClassLoader */
    public static Map s_classLoaderSystemDefinitions = new WeakHashMap();//note: null key is supported

    /** Map of SystemDefinition location (as String[List]) per ClassLoader */
    public static Map s_classLoaderDefinitionLocations = new WeakHashMap();//note: null key is supported

    /** Map of Aspect class names[List] (AKA lightweight def) per ClassLoader, for Aspect weaving */
    public static Map s_classLoaderAspectNames = new WeakHashMap();

    /** Default location for default AspectWerkz definition file, JVM wide */
    public static final String URL_JVM_OPTION_SYSTEM = System.getProperty("-Daspectwerkz.definition.file", "no -Daspectwerkz.definition.file");

    /** The AOP deployment descriptor for any deployed unit */
    public static final String AOP_XML_FILE = "META-INF/aop.xml";

    /** ThreadLocal context for SystemDefinitions[List] */
    private static ThreadLocal s_systemDefintionsContext = new ThreadLocal();

    /** ThreadLocal context for Aspect class names[List] */
    private static ThreadLocal s_aspectNamesContext = new ThreadLocal();

    /**
     * Register a new ClassLoader in the system
     * and gather all its definition and parents definitions
     *
     * @param loader the class loader to register
     */
    public static void registerClassLoader(ClassLoader loader) {
        if (s_classLoaderSystemDefinitions.containsKey(loader)) {
            return;
        }

        // skip boot classloader
        if (loader == null) {
            return;
        }

        //register parents first
        if (loader != null) {
            registerClassLoader(loader.getParent());
            // then register -D.. if system classloader
            // and then all META-INF/aop.xml
            try {
                List aspectNames = new ArrayList();
                List defs = new ArrayList();
                List defsLocation = new ArrayList();
                //early registration to avoid recursion
                s_classLoaderAspectNames.put(loader, aspectNames);
                s_classLoaderSystemDefinitions.put(loader, defs);
                s_classLoaderDefinitionLocations.put(loader, defsLocation);

                // is this system classloader ?
                if (loader == ClassLoader.getSystemClassLoader()) {
                    aspectNames.addAll(DefinitionLoader.getDefaultDefinitionAspectNames());
                    defs.addAll(DefinitionLoader.getDefaultDefinition(loader));//-D..file=... sysdef
                    defsLocation.add(URL_JVM_OPTION_SYSTEM);
                }

                Enumeration res = loader.getResources(AOP_XML_FILE);
                while (res.hasMoreElements()) {
                    URL def = (URL)res.nextElement();
                    if (isDefinedBy(loader.getParent(), def.toExternalForm())) {
                        ;
                    } else {
                        aspectNames.addAll(XmlParser.getAspectClassNames(def));
                        defs.addAll(XmlParser.parseNoCache(loader, def));
                        defsLocation.add(def.toExternalForm());
                    }
                }
                dump(loader);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Check if a given resource has already been registered to a classloader and its parent hierachy
     * TODO what if child shares parent path ?
     * TODO What happens with smylinking and xml in jars etc ?
     * TODO Needs test
     * TODO No need for the s_ map
     *
     * TODO KICK the def map and crawl up the CL parents and redo a getResources check instead
     *
     * @param loader the classloader which might define the resource
     * @param def the resource
     * @return true if classloader or its parent defines the resource
     */
    public static boolean isDefinedBy(ClassLoader loader, String def) {
        if (loader == null) {
            return false;
        }
        ArrayList defLocation = (ArrayList)s_classLoaderDefinitionLocations.get(loader);
        if (defLocation!=null && defLocation.contains(def)) {
            return true;
        } else {
            return isDefinedBy(loader.getParent(), def);
        }


    }

    /**
     * Pretty dump a classloader
     *
     * @param loader
     */
    public static void dump(ClassLoader loader) {
        StringBuffer dump = new StringBuffer("******************************************************************");
        dump.append("\n* ClassLoader = ").append(loader);
        List defs = (List)s_classLoaderSystemDefinitions.get(loader);
        for (Iterator it = defs.iterator(); it.hasNext();) {
            SystemDefinition def = (SystemDefinition)it.next();
            dump.append("\n*\t").append(def.getUuid());
        }
        dump.append("\n* Aspect total count = ").append(((List)s_classLoaderAspectNames.get(loader)).size());
        for (Iterator it = ((List)s_classLoaderDefinitionLocations.get(loader)).iterator(); it.hasNext();) {
            dump.append("\n* ").append(it.next());
        }
        dump.append("\n******************************************************************");
        System.out.println(dump.toString());
    }

    /**
     * Returned the gathered aspect names visible from a classloader
     * @param loader
     * @return List of Aspect class names
     */
    public static List getHierarchicalAspectNames(ClassLoader loader) {
        // if runtime access before load time
        if ( ! s_classLoaderSystemDefinitions.containsKey(loader))
            registerClassLoader(loader);

        List aspectNames = new ArrayList();
        if (loader == null) {
            return aspectNames;
        }

        ClassLoader parent = loader.getParent();
        aspectNames.addAll(getHierarchicalAspectNames(parent));
        aspectNames.addAll((List)s_classLoaderAspectNames.get(loader));

        return aspectNames;
    }

    /**
     * Returns the gathered SystemDefinition visible from a classloader
     * @param loader
     * @return List of SystemDefinition
     */
    public static List getHierarchicalDefs(ClassLoader loader) {
        // if runtime access before load time
        if ( ! s_classLoaderSystemDefinitions.containsKey(loader))
            registerClassLoader(loader);

        List defs = new ArrayList();
        if (loader == null) {
            return defs;
        }

        ClassLoader parent = loader.getParent();
        defs.addAll(getHierarchicalDefs(parent));
        defs.addAll((List)s_classLoaderSystemDefinitions.get(loader));

        return defs;
    }


    //----  Get/Set the ThreadLocal context for SystemDefintions and Aspect FQN

    public static void setDefinitionsContext(List defs) {
        s_systemDefintionsContext.set(defs);
    }

    public static List getDefinitionsContext() {
        return (List)s_systemDefintionsContext.get();
    }

    public static void setAspectNamesContext(List defs) {
        s_aspectNamesContext.set(defs);
    }

    public static List getAspectNamesContext() {
        return (List)s_aspectNamesContext.get();
    }
}

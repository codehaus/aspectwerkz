/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A repository for the class info hierarchy. Is class loader aware.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ClassInfoRepository {
    /**
     * Map with all the class info repositories mapped to their class loader.
     */
    private static final Map s_repositories = new WeakHashMap();

    /**
     * Map with all the class info mapped to their class names.
     */
    private final Map m_repository = new HashMap();

    /**
     * Class loader for the class repository.
     */
    private final ClassLoader m_loader;

    /**
     * Creates a new repository.
     *
     * @param loader
     */
    private ClassInfoRepository(final ClassLoader loader) {
        m_loader = loader;
    }

    /**
     * Returns the class info repository for the specific class loader
     *
     * @param loader
     * @return
     */
    public static synchronized ClassInfoRepository getRepository(final ClassLoader loader) {
        if (s_repositories.containsKey(loader)) {
            return (ClassInfoRepository)s_repositories.get(loader);
        } else {
            final ClassInfoRepository repository = new ClassInfoRepository(loader);
            s_repositories.put(loader, repository);
            return repository;
        }
    }

    /**
     * Remove a class from the repository.
     *
     * @param className the name of the class
     */
    public static void removeClassInfoFromAllClassLoaders(final String className) {
        for (Iterator it = s_repositories.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            if (entry.getKey().equals(className)) {
                s_repositories.remove(className);
            }
        }
    }

    /**
     * Returns the class info.
     *
     * @param className
     * @return
     */
    public ClassInfo getClassInfo(final String className) {
        ClassInfo info = (ClassInfo)m_repository.get(className);
        if (info == null) {
            return checkParentClassRepository(className, m_loader);
        }
        return (ClassInfo)m_repository.get(className);
    }

    /**
     * Adds a new class info.
     *
     * @param classInfo
     */
    public void addClassInfo(final ClassInfo classInfo) {
        // is the class loaded by a class loader higher up in the hierarchy?
        if (checkParentClassRepository(classInfo.getName(), m_loader) == null) {
            m_repository.put(classInfo.getName(), classInfo);
        } else {
            // TODO: remove class in child class repository and add it for the current (parent) CL
        }
    }

    /**
     * Checks if the class info for a specific class exists.
     *
     * @param name
     * @return
     */
    public boolean hasClassInfo(final String name) {
        return m_repository.containsKey(name);
    }

    /**
     * Searches for a class info up in the class loader hierarchy.
     *
     * @TODO might clash for specific class loader lookup algorithms, user need to override this class and implement this method
     *
     * @param className
     * @param loader
     * @return the class info
     */
    public ClassInfo checkParentClassRepository(final String className, final ClassLoader loader) {
        if (loader == null) {
            return null;
        }
        ClassInfo info;
        ClassLoader parent = loader.getParent();
        if (parent == null) {
            return null;
        } else {
            info = ClassInfoRepository.getRepository(parent).getClassInfo(className);
            if (info != null) {
                return info;
            } else {
                return checkParentClassRepository(className, parent);
            }
        }
    }
}

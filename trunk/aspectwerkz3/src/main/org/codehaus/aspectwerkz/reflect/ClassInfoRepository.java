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
            ClassInfoRepository repository = new ClassInfoRepository(loader);

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

            if (((String)entry.getKey()).equals(className)) {
                s_repositories.remove(className);
            }
        }
    }

    public ClassInfo getClassInfo(final String className) {
        ClassInfo info = (ClassInfo)m_repository.get(className);

        if (info == null) {
            return checkParentClassRepository(className, m_loader);
        }

        return (ClassInfo)m_repository.get(className);
    }

    public void addClassInfo(final ClassInfo classInfo) {
        // is the class loaded by a class loader higher up in the hierarchy?
        if (checkParentClassRepository(classInfo.getName(), m_loader) == null) {
            m_repository.put(classInfo.getName(), classInfo);
        } else {
            // TODO: remove class in child class repository and add it for the current (parent) CL
        }
    }

    public boolean hasClassInfo(final String name) {
        return m_repository.containsKey(name);
    }

    private ClassInfo checkParentClassRepository(final String className, final ClassLoader loader) {
        if (loader == null) {
            return null;
        }

        ClassInfo info = null;
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

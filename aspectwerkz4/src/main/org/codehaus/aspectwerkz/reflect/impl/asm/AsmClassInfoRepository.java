/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.asm;

import gnu.trove.TIntObjectHashMap;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import java.lang.ref.Reference;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * A repository for the class info hierarchy. Is class loader aware.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class AsmClassInfoRepository {
    /**
     * Map with all the class info repositories mapped to their class loader.
     */
    private static final TIntObjectHashMap s_repositories = new TIntObjectHashMap();

    /**
     * Map with all the class info mapped to their class names.
     */
    private final TIntObjectHashMap m_repository = new TIntObjectHashMap();

    /**
     * Class loader for the class repository.
     */
    private transient final WeakReference m_loaderRef;

    /**
     * The annotation properties file.
     */
    private final Properties m_annotationProperties;

    /**
     * Creates a new repository.
     *
     * @param loader
     */
    private AsmClassInfoRepository(final ClassLoader loader) {
        m_loaderRef = new WeakReference(loader);
        m_annotationProperties = new Properties();
        if (loader != null) {
            try {
                InputStream stream = loader.getResourceAsStream("annotation.properties");
                if (stream != null) {
                     try {
                         m_annotationProperties.load(stream);
                     } finally {
                         try { stream.close(); } catch(Exception e) {;}
                     }
                }
            } catch (IOException e) {
                throw new DefinitionException("could not find resource [annotation.properties] on classpath");
            }
        }
    }

    /**
     * Returns the class info repository for the specific class loader
     *
     * @param loader
     * @return
     */
    public static synchronized AsmClassInfoRepository getRepository(final ClassLoader loader) {
        int hash;
        if (loader == null) { // boot cl
            hash = 0;
        } else {
            hash = loader.hashCode();
        }
        Reference repositoryRef = (Reference) s_repositories.get(hash);
        AsmClassInfoRepository repository = ((repositoryRef == null) ? null : (AsmClassInfoRepository) repositoryRef
                .get());
        if (repository != null) {
            return repository;
        } else {
            AsmClassInfoRepository repo = new AsmClassInfoRepository(loader);
            s_repositories.put(hash, new SoftReference(repo));
            return repo;
        }
    }

    /**
     * Remove a class from the repository.
     *
     * @param className the name of the class
     */
    public static void removeClassInfoFromAllClassLoaders(final String className) {
        //TODO - fix algorithm
        throw new UnsupportedOperationException("fix algorithm");
    }

    /**
     * Returns the class info.
     *
     * @param className
     * @return
     */
    public ClassInfo getClassInfo(final String className) {
        Reference classInfoRef = ((Reference) m_repository.get(className.hashCode()));
        ClassInfo info = (classInfoRef == null) ? null : (ClassInfo) (classInfoRef.get());
        if (info == null) {
            return checkParentClassRepository(className, (ClassLoader) m_loaderRef.get());
        }
        return info;
    }

    /**
     * Adds a new class info.
     *
     * @param classInfo
     */
    public void addClassInfo(final ClassInfo classInfo) {
        // is the class loaded by a class loader higher up in the hierarchy?
        if (checkParentClassRepository(classInfo.getName(), (ClassLoader) m_loaderRef.get()) == null) {
            m_repository.put(classInfo.getName().hashCode(), new SoftReference(classInfo));
        } else {
            // TODO: remove class in child class repository and add it for the
            // current (parent) CL
        }
    }

    /**
     * Checks if the class info for a specific class exists.
     *
     * @param name
     * @return
     */
    public boolean hasClassInfo(final String name) {
        Reference classInfoRef = (Reference) m_repository.get(name.hashCode());
        return (classInfoRef == null) ? false : (classInfoRef.get() != null);
    }

    /**
     * Removes the class from the repository (since it has been modified and needs to be rebuild).
     *
     * @param className
     */
    public void removeClassInfo(final String className) {
        m_repository.remove(className.hashCode());
    }

    /**
     * Returns the annotation properties for the specific class loader.
     *
     * @return the annotation properties
     */
    public Properties getAnnotationProperties() {
        return m_annotationProperties;
    }

    /**
     * Searches for a class info up in the class loader hierarchy.
     *
     * @param className
     * @param loader
     * @return the class info
     * @TODO might clash for specific class loader lookup algorithms, user need to override this class and implement
     * this method
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
            info = AsmClassInfoRepository.getRepository(parent).getClassInfo(className);
            if (info != null) {
                return info;
            } else {
                return checkParentClassRepository(className, parent);
            }
        }
    }
}
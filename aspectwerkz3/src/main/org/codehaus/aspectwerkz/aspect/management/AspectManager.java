/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.annotation.AspectAnnotationParser;
import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.StartupManager;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Manages the aspects. <p/>Handles deployment, redeployment, management, configuration or redefinition of the aspects.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @TODO: Must handle : - undeployment of the aspects - notification of all the pointcuts that it should remove a
 *        certain advice from the pointcut - notification of the JoinPoinManager.
 */
public final class AspectManager {
    /**
     * The system this AspectManager is defined in.
     */
    public final AspectSystem m_system;

    /**
     * The definition.
     */
    public SystemDefinition m_definition;

    /**
     * The aspect registry.
     */
    private final AspectRegistry m_aspectRegistry;

    /**
     * The annotation parser to parse the definitions for the hot deployed aspects.
     */
    //    private AspectAttributeParser m_annotationParser = new AspectAttributeParser();
    private AspectAnnotationParser m_annotationParser = new AspectAnnotationParser();

    /**
     * Cache for the pointcuts.
     * 
     * @TODO: when unweaving (and reordering) of aspects is supported then this cache must have a way of being
     *        invalidated.
     */
    private final Map m_pointcutCache = new WeakHashMap();

    /**
     * Cache for the cflow pointcuts.
     * 
     * @TODO: when unweaving (and reordering) of aspects is supported then this cache must have a way of being
     *        invalidated.
     */
    private final Map m_cflowPointcutCache = new WeakHashMap();

    /**
     * Creates a new aspect manager.
     * 
     * @param system the system
     * @param definition the system definition
     */
    public AspectManager(final AspectSystem system, final SystemDefinition definition) {
        m_system = system;
        m_definition = definition;
        m_aspectRegistry = new AspectRegistry(this, m_definition);
    }

    /**
     * Initializes the manager. The initialization needs to be separated fromt he construction of the manager, and is
     * triggered by the runtime system.
     */
    public void initialize() {
        m_aspectRegistry.initialize();
    }

    /**
     * Registers a new aspect.
     * 
     * @param container the containern for the aspect to register
     * @param aspectMetaData the aspect meta-data
     */
    public void register(final AspectContainer container, final PointcutManager aspectMetaData) {
        m_aspectRegistry.register(container, aspectMetaData);
    }

    /**
     * Creates and registers new aspect at runtime.
     * 
     * @param name the name of the aspect
     * @param aspectClassName the class name of the aspect
     * @param deploymentModel the deployment model for the aspect (constants in the DeploymemtModel class, e.g. f.e.
     *            DeploymentModel.PER_JVM)
     * @param loader an optional class loader (if null it uses the context classloader)
     */
    public void createAspect(
        final String name,
        final String aspectClassName,
        final int deploymentModel,
        final ClassLoader loader) {
        if (name == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }
        if (aspectClassName == null) {
            throw new IllegalArgumentException("class name can not be null");
        }
        if ((deploymentModel < 0) || (deploymentModel > 3)) {
            throw new IllegalArgumentException(deploymentModel + " is not a valid deployment model type");
        }
        Class aspectClass = null;
        try {
            if (loader == null) {
                aspectClass = ContextClassLoader.loadClass(aspectClassName);
            } else {
                aspectClass = loader.loadClass(aspectClassName);
            }
        } catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("could not load aspect class [");
            msg.append(aspectClassName);
            msg.append("] with name ");
            msg.append(name);
            msg.append(": ");
            msg.append(e.toString());
            throw new RuntimeException(msg.toString());
        }

        // create the aspect definition
        AspectDefinition aspectDef = new AspectDefinition(aspectClassName, aspectClassName, m_definition.getUuid());
        aspectDef.setDeploymentModel(DeploymentModel.getDeploymentModelAsString(deploymentModel));

        // parse the class attributes and create a definition
        m_annotationParser.parse(aspectClass, aspectDef, m_definition);
        m_definition.addAspect(aspectDef);
        CrossCuttingInfo crossCuttingInfo = new CrossCuttingInfo(
            null,
            aspectClass,
            aspectDef.getName(),
            deploymentModel,
            aspectDef,
            new HashMap());
        AspectContainer container = StartupManager.createAspectContainer(crossCuttingInfo);
        crossCuttingInfo.setContainer(container);
        m_aspectRegistry.register(container, new PointcutManager(name, deploymentModel));
    }

    /**
     * Returns the UUID for the system.
     * 
     * @return the UUID
     */
    public String getUuid() {
        return m_definition.getUuid();
    }

    /**
     * Returns the aspect container by its index.
     * 
     * @param index the index of the aspect
     * @return the aspect
     */
    public AspectContainer getAspectContainer(final int index) {
        return m_aspectRegistry.getAspectContainer(index);
    }

    /**
     * Returns the aspect container for a specific name.
     * 
     * @param name the name of the aspect
     * @return the the aspect prototype
     */
    public AspectContainer getAspectContainer(final String name) {
        return m_aspectRegistry.getAspectContainer(name);
    }

    /**
     * Returns an array with all the aspect containers.
     * 
     * @return the aspect containers
     */
    public AspectContainer[] getAspectContainers() {
        return m_aspectRegistry.getAspectContainers();
    }

    /**
     * Returns the aspect for a specific name, deployed as perJVM.
     * 
     * @param name the name of the aspect
     * @return the the aspect
     */
    public Object getCrossCuttingInfo(final String name) {
        return m_aspectRegistry.getCrossCuttingInfo(name);
    }

    /**
     * Returns an array with all the cross-cutting infos.
     * 
     * @return the cross-cutting infos
     */
    public CrossCuttingInfo[] getCrossCuttingInfos() {
        AspectContainer[] aspectContainers = m_aspectRegistry.getAspectContainers();
        CrossCuttingInfo[] infos = new CrossCuttingInfo[aspectContainers.length];
        for (int i = 0; i < aspectContainers.length; i++) {
            AspectContainer aspectContainer = aspectContainers[i];
            infos[i] = aspectContainer.getCrossCuttingInfo();
        }
        return infos;
    }

    /**
     * Retrieves a specific mixin based on its index.
     * 
     * @param index the index of the introduction (aspect in this case)
     * @return the the mixin (aspect in this case)
     */
    public Mixin getMixin(final int index) {
        return m_aspectRegistry.getMixin(index);
    }

    /**
     * Returns the mixin implementation for a specific name.
     * 
     * @param name the name of the introduction (aspect in this case)
     * @return the the mixin (aspect in this case)
     */
    public Mixin getMixin(final String name) {
        return m_aspectRegistry.getMixin(name);
    }

    /**
     * Returns the index for a specific name to aspect mapping.
     * 
     * @param name the name of the aspect
     * @return the index of the aspect
     */
    public int getAspectIndexFor(final String name) {
        return m_aspectRegistry.getAspectIndexFor(name);
    }

    /**
     * Returns the index for a specific name to advice mapping.
     * 
     * @param name the name of the advice
     * @return the index of the advice
     */
    public IndexTuple getAdviceIndexFor(final String name) {
        return m_aspectRegistry.getAdviceIndexFor(name);
    }

    /**
     * Returns the pointcut manager for the name specified.
     * 
     * @param name the name of the aspect
     * @return thepointcut manager
     */
    public PointcutManager getPointcutManager(final String name) {
        return m_aspectRegistry.getPointcutManager(name);
    }

    /**
     * Returns a list with all the pointcut managers.
     * 
     * @return thepointcut managers
     */
    public Collection getPointcutManagers() {
        return m_aspectRegistry.getPointcutManagers();
    }

    /**
     * Returns the pointcut list for the context specified. <p/>Caches the list, needed since the actual method call is
     * expensive and is made each time a new instance of an advised class is created.
     * 
     * @param ctx the expression context
     * @return the pointcuts for this join point
     */
    public List getPointcuts(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("expression context can not be null");
        }
        initialize();
        List pointcuts;
        if (m_pointcutCache.containsKey(ctx)) {
            pointcuts = (List) m_pointcutCache.get(ctx);
            if (pointcuts == null) { // strange enough, but can be null
                System.out.println("AspectManager.getPointcuts " + "**IS NULL**");
                pointcuts = new ArrayList();
            }
        } else {
            pointcuts = m_aspectRegistry.getPointcuts(ctx);
            synchronized (m_pointcutCache) {
                m_pointcutCache.put(ctx, pointcuts);
            }
        }
        return pointcuts;
    }

    /**
     * Returns the cflow pointcut list for the context specified. <p/>Caches the list, needed since the actual method
     * call is expensive and is made each time a new instance of an advised class is created.
     * 
     * @param ctx the expression context
     * @return the pointcuts for this join point
     */
    public List getCflowPointcuts(final ExpressionContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("expression context can not be null");
        }
        initialize();
        List pointcuts;
        if (m_cflowPointcutCache.containsKey(ctx)) {
            pointcuts = (List) m_cflowPointcutCache.get(ctx);
            if (pointcuts == null) { // strange enough, but can be null
                pointcuts = new ArrayList();
            }
        } else {
            pointcuts = m_aspectRegistry.getCflowPointcuts(ctx);
            synchronized (m_cflowPointcutCache) {
                m_cflowPointcutCache.put(ctx, pointcuts);
            }
        }
        return pointcuts;
    }

    /**
     * Checks if a specific class has an aspect defined.
     * 
     * @param name the name of the aspect
     * @return boolean true if the class has an aspect defined
     */
    public boolean hasAspect(final String name) {
        return m_aspectRegistry.hasAspect(name);
    }

    /**
     * Returns a specific method by the class and the method index.
     * 
     * @param klass the class housing the method
     * @param methodHash the method hash
     * @return the method
     */
    public MethodTuple getMethodTuple(final Class klass, final int methodHash) {
        return AspectRegistry.getMethodTuple(klass, methodHash);
    }

    /**
     * Returns a specific constructor by the class and the constructor index.
     * 
     * @param klass the class housing the method
     * @param constructorHash the method hash
     * @return the constructor
     */
    public ConstructorTuple getConstructorTuple(final Class klass, final int constructorHash) {
        return AspectRegistry.getConstructorTuple(klass, constructorHash);
    }

    /**
     * Returns a specific field by the class and the field index.
     * 
     * @param klass the class housing the method
     * @param fieldHash the method hash
     * @return the field
     */
    public Field getField(final Class klass, final int fieldHash) {
        return AspectRegistry.getField(klass, fieldHash);
    }

    /**
     * Returns the string representation of the manager.
     * 
     * @return
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("AspectManager::");
        sb.append(this.hashCode());
        sb.append("[").append(m_definition.getUuid());
        sb.append(" @ ").append(Util.classLoaderToString(m_system.getDefiningClassLoader()));
        sb.append("]");
        return sb.toString();
    }
}
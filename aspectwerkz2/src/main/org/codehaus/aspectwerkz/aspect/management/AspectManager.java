/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.CrossCuttable;
import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.attribute.AspectAttributeParser;
import org.codehaus.aspectwerkz.definition.attribute.AttributeParser;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.util.Util;

/**
 * Manages the aspects.
 * <p/>
 * Handles deployment, redeployment, management, configuration or redefinition of the aspects.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @TODO: Must handle : - undeployment of the aspects - notification of all the pointcuts that it should remove a certain advice from the pointcut - notification of the JoinPoinManager.
 */
public final class AspectManager {

    /**
     * The UUID for the system.
     */
    private final String m_uuid;

    /**
     * The definition.
     */
    private SystemDefinition m_definition;

    /**
     * The aspect registry.
     */
    private final AspectRegistry m_aspectRegistry;

    /**
     * The attribute parser to parse the definitions for the aspects loaded at runtime.
     *
     * @TODO: should be configurable, need to handle other attribute implementations, f.e. JSR-175
     */
    private AttributeParser m_attributeParser = new AspectAttributeParser();

    /**
     * Cache for the execution pointcuts.
     *
     * @TODO: when unweaving (and reordering) of aspects is supported then this cache must have a way of being
     * invalidated.
     */
    private final Map m_executionPointcutCache = new WeakHashMap();

    /**
     * Cache for the get pointcuts.
     *
     * @TODO: when unweaving (and reordering) of aspects is supported then this cache must have a way of being
     * invalidated.
     */
    private final Map m_getPointcutCache = new WeakHashMap();

    /**
     * Cache for the set pointcuts.
     *
     * @TODO: when unweaving (and reordering) of aspects is supported then this cache must have a way of being
     * invalidated.
     */
    private final Map m_setPointcutCache = new WeakHashMap();

    /**
     * Cache for the handler pointcuts.
     *
     * @TODO: when unweaving (and reordering) of aspects is supported then this cache must have a way of being
     * invalidated.
     */
    private final Map m_handlerPointcutCache = new WeakHashMap();

    /**
     * Cache for the call pointcuts.
     *
     * @TODO: when unweaving (and reordering) of aspects is supported then this cache must have a way of being
     * invalidated.
     */
    private final Map m_callPointcutCache = new WeakHashMap();

    /**
     * Cache for the cflow pointcuts.
     *
     * @TODO: when unweaving (and reordering) of aspects is supported then this cache must have a way of being
     * invalidated.
     */
    private final Map m_cflowPointcutCache = new WeakHashMap();

    /**
     * Creates a new aspect manager.
     *
     * @param uuid       the system UUID
     * @param definition the system definition
     */
    public AspectManager(final String uuid, final SystemDefinition definition) {
        m_uuid = uuid;
        m_definition = definition;
        m_aspectRegistry = new AspectRegistry(m_uuid, m_definition);
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
     * @param aspect         the aspect to register
     * @param aspectMetaData the aspect meta-data
     */
    public void register(final CrossCuttable aspect, final PointcutManager aspectMetaData) {
        m_aspectRegistry.register(aspect, aspectMetaData);
    }

    /**
     * Creates and registers new aspect at runtime.
     *
     * @param name            the name of the aspect
     * @param aspectClassName the class name of the aspect
     * @param deploymentModel the deployment model for the aspect (constants in the DeploymemtModel class, e.g. f.e.
     *                        DeploymentModel.PER_JVM)
     * @param loader          an optional class loader (if null it uses the context classloader)
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
        if (deploymentModel < 0 || deploymentModel > 3) {
            throw new IllegalArgumentException(deploymentModel + " is not a valid deployment model type");
        }

        CrossCuttable prototype = null;
        Class aspectClass = null;
        try {
            if (loader == null) {
                aspectClass = ContextClassLoader.loadClass(aspectClassName);
            }
            else {
                aspectClass = loader.loadClass(aspectClassName);
            }
        }
        catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("could not load aspect class [");
            msg.append(aspectClassName);
            msg.append("] with name ");
            msg.append(name);
            msg.append(": ");
            msg.append(e.toString());
            throw new RuntimeException(msg.toString());
        }

        try {
            prototype = (CrossCuttable)aspectClass.newInstance();
        }
        catch (Exception e) {
            StringBuffer msg = new StringBuffer();
            msg.append("could not create a new instance of aspect [");
            msg.append(aspectClassName);
            msg.append("]: ");
            msg.append(e.toString());
            throw new RuntimeException(msg.toString());
        }

        // create the aspect definition
        AspectDefinition aspectDef = new AspectDefinition(
                aspectClassName,
                aspectClassName,
                DeploymentModel.getDeploymentModelAsString(deploymentModel)
        );

        // parse the class attributes and create a definition
        m_attributeParser.parse(aspectClass, aspectDef, m_definition);
        m_definition.addAspect(aspectDef);

        CrossCuttingInfo crossCuttingInfo = prototype.getCrossCuttingInfo();
        crossCuttingInfo.setDeploymentModel(deploymentModel);
        crossCuttingInfo.setName(name);
        crossCuttingInfo.setAspectClass(aspectClass);
        crossCuttingInfo.setContainer(new AspectContainer(crossCuttingInfo));
        crossCuttingInfo.setAspectDef(aspectDef);

        m_aspectRegistry.register(prototype, new PointcutManager(m_uuid, name, deploymentModel));
    }

    /**
     * Retrieves a specific aspect based on index.
     *
     * @param index the index of the aspect
     * @return the aspect
     */
    public CrossCuttable getAspect(final int index) {
        return m_aspectRegistry.getAspect(index);
    }

    /**
     * Returns the aspect for a specific name.
     *
     * @param name the name of the aspect
     * @return the the aspect
     */
    public CrossCuttable getAspect(final String name) {
        return m_aspectRegistry.getAspect(name);
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
     * Returns an array with all the aspects.
     *
     * @return the aspects
     */
    public CrossCuttable[] getAspects() {
        return m_aspectRegistry.getAspects();
    }

    /**
     * Returns the execution pointcut list for the class and member specified. <p/>Caches the list, needed since the
     * actual method call is expensive and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData  the meta-data for the class
     * @param memberMetaData meta-data for the member
     * @return the pointcuts for this join point
     */
    public List getExecutionPointcuts(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (memberMetaData == null) {
            throw new IllegalArgumentException("method meta-data can not be null");
        }
        initialize();

        Integer hashKey = Util.calculateHash(classMetaData.getName(), memberMetaData);

        // if cached; return the cached list
        if (m_executionPointcutCache.containsKey(hashKey)) {
            return (List)m_executionPointcutCache.get(hashKey);
        }

        List pointcuts = m_aspectRegistry.getExecutionPointcuts(classMetaData, memberMetaData);
        synchronized (m_executionPointcutCache) {
            m_executionPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns the get pointcut list for the class and field specified. <p/>Caches the list, needed since the actual
     * method call is expensive and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getGetPointcuts(
            final ClassMetaData classMetaData,
            final FieldMetaData fieldMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (fieldMetaData == null) {
            throw new IllegalArgumentException("field meta-data can not be null");
        }

        initialize();

        Integer hashKey = Util.calculateHash(classMetaData.getName(), fieldMetaData);

        // if cached; return the cached list
        if (m_getPointcutCache.containsKey(hashKey)) {
            return (List)m_getPointcutCache.get(hashKey);
        }

        List pointcuts = m_aspectRegistry.getGetPointcuts(classMetaData, fieldMetaData);

        synchronized (m_getPointcutCache) {
            m_getPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns the set pointcut list for the class and field specified. <p/>Caches the list, needed since the actual
     * method call is expensive and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getSetPointcuts(
            final ClassMetaData classMetaData,
            final FieldMetaData fieldMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (fieldMetaData == null) {
            throw new IllegalArgumentException("field meta-data can not be null");
        }

        initialize();

        Integer hashKey = Util.calculateHash(classMetaData.getName(), fieldMetaData);

        // if cached; return the cached list
        if (m_setPointcutCache.containsKey(hashKey)) {
            return (List)m_setPointcutCache.get(hashKey);
        }

        List pointcuts = m_aspectRegistry.getSetPointcuts(classMetaData, fieldMetaData);

        synchronized (m_setPointcutCache) {
            m_setPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns the handler pointcut list for the class and field specified. <p/>Caches the list, needed since the actual
     * method call is expensive and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @return the pointcuts for this join point
     */
    public List getHandlerPointcuts(final ClassMetaData classMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }

        initialize();

        Integer hashKey = new Integer(classMetaData.getName().hashCode());

        // if cached; return the cached list
        if (m_handlerPointcutCache.containsKey(hashKey)) {
            return (List)m_handlerPointcutCache.get(hashKey);
        }

        List pointcuts = m_aspectRegistry.getHandlerPointcuts(classMetaData);

        synchronized (m_handlerPointcutCache) {
            m_handlerPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns the call pointcut list for the class and member specified. <p/>Caches the list, needed since the actual
     * method call is expensive and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData  the meta-data for the class
     * @param memberMetaData meta-data for the member
     * @return the pointcuts for this join point
     */
    public List getCallPointcuts(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (memberMetaData == null) {
            throw new IllegalArgumentException("member meta-data can not be null");
        }

        initialize();

        Integer hashKey = Util.calculateHash(classMetaData.getName(), memberMetaData);

        // if cached; return the cached list
        if (m_callPointcutCache.containsKey(hashKey)) {
            return (List)m_callPointcutCache.get(hashKey);
        }

        List pointcuts = m_aspectRegistry.getCallPointcuts(classMetaData, memberMetaData);
        //System.out.println(pointcuts.size() + " for " + memberMetaData.getName());
        synchronized (m_callPointcutCache) {
            m_callPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns all Expression that match (no matter cflow) at join point related
     * to given metadata and assumed type, and that contains 1+ cflow construct.
     *
     * The Expressions are inflated and evaluated to allow optimization
     * (pc1 AND cflow => TRUE|FALSE AND cflow) depending on given MetaDataBase
     *
     * @param classMetaData
     * @param memberMetaData
     * @param callerClassMetaData can be null if not @CALL
     * @param pointcutType assumed
     * @return
     */
    public List getCFlowExpressions(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData,
            final ClassMetaData callerClassMetaData,
            final PointcutType pointcutType) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (memberMetaData == null) {
            throw new IllegalArgumentException("member meta-data can not be null");
        }

        initialize();
        // Note: cache is done at JP level
        return m_aspectRegistry.getCflowExpressions(classMetaData, memberMetaData,
                                                    callerClassMetaData, pointcutType);
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
     * @param klass      the class housing the method
     * @param methodHash the method hash
     * @return the method
     */
    public MethodTuple getMethodTuple(final Class klass, final int methodHash) {
        return m_aspectRegistry.getMethodTuple(klass, methodHash);
    }

    /**
     * Returns a specific constructor by the class and the constructor index.
     *
     * @param klass           the class housing the method
     * @param constructorHash the method hash
     * @return the constructor
     */
    public ConstructorTuple getConstructorTuple(final Class klass, final int constructorHash) {
        return m_aspectRegistry.getConstructorTuple(klass, constructorHash);
    }

    /**
     * Returns a specific field by the class and the field index.
     *
     * @param klass     the class housing the method
     * @param fieldHash the method hash
     * @return the field
     */
    public Field getField(final Class klass, final int fieldHash) {
        return m_aspectRegistry.getField(klass, fieldHash);
    }
}

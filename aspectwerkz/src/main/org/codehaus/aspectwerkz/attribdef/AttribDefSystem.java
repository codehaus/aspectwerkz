/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.WeakHashMap;
import java.util.Properties;
import java.lang.reflect.Method;
import java.io.FileInputStream;

import gnu.trove.TObjectIntHashMap;

import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.AspectMetaData;
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.attribdef.aspect.Introduction;
import org.codehaus.aspectwerkz.attribdef.definition.StartupManager;
import org.codehaus.aspectwerkz.attribdef.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.AspectWerkzDefinitionImpl;
import org.codehaus.aspectwerkz.attribdef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.DefaultAspectAttributeParser;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AspectAttributeParser;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.regexp.CallerSidePattern;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.ClassNameMethodMetaDataTuple;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.util.Util;
import org.codehaus.aspectwerkz.connectivity.Invoker;
import org.codehaus.aspectwerkz.connectivity.RemoteProxyServer;
import org.codehaus.aspectwerkz.connectivity.RemoteProxy;

/**
 * Manages the aspects in the AspectWerkz system.<br/>
 * Handles the initialization and configuration of the system.<br/>
 * Stores and indexes the aspects defined in the system.<br/>
 * Stores and indexes the advised methods.<br/>
 * Stores and indexes the introduced methods.<br/>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public final class AttribDefSystem implements System {

    /**
     * Holds references to all the the aspects in the system.
     */
    private final Map m_aspectMetaDataMap = new SequencedHashMap();

    /**
     * and cache for the method pointcuts.
     *
     * todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_methodPointcutCache = new WeakHashMap();

    /**
     * and cache for the get field pointcuts.
     *
     * todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_getFieldPointcutCache = new WeakHashMap();

    /**
     * and cache for the set field pointcuts.
     *
     * todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_setFieldPointcutCache = new WeakHashMap();

    /**
     * and cache for the throws pointcuts.
     *
     * todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_throwsPointcutCache = new WeakHashMap();

    /**
     * and cache for the caller side pointcuts.
     *
     * todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_callerSidePointcutCache = new WeakHashMap();

    /**
     * and cache for the cflow pointcuts.
     *
     * todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_cflowPointcutCache = new WeakHashMap();

    /**
     * Holds references to all the the advised methods in the system.
     */
    private final Map m_methods = new HashMap();

    /**
     * Holds references to all the the aspects in the system.
     */
    private Aspect[] m_aspects = new Aspect[0];

    /**
     * Holds the indexes for the aspects.
     */
    private final TObjectIntHashMap m_aspectIndexes = new TObjectIntHashMap();

    /**
     * Holds the indexes for the advices.
     */
    private final Map m_adviceIndexes = new HashMap();

    /**
     * Holds the indexes for the introductions.
     * Each nested class in aspect has its own index
     */
    private Mixin[] m_mixins = new Mixin[0];

    /**
     * Marks the system as initialized.
     */
    private boolean m_initialized = false;

    /**
     * The UUID for the system.
     */
    private final String m_uuid;

    /**
     * The definition.
     */
    private AspectWerkzDefinitionImpl m_definition;

    /**
     * Holds a list of the cflow join points passed by the control flow of the current thread.
     */
    private final ThreadLocal m_controlFlowLog = new ThreadLocal();

    /**
     * The remote proxy server instance.
     */
    private RemoteProxyServer m_remoteProxyServer = null;

    /**
     * The attribute parser to parse the definitions for the aspects loaded at runtime.
     */
    private AspectAttributeParser m_attributeParser;

    /**
     * Creates a new AspectWerkz system instance.
     * Sets the UUID for the system.
     * Is set to private since the instance should be retrieved using the getSystem(..) method.
     *
     * @param uuid the UUID for the system
     * @param definition the definition for the system
     */
    private AttribDefSystem(final String uuid, final AspectWerkzDefinition definition) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (definition == null) throw new IllegalArgumentException("definition can not be null");

        m_uuid = uuid;
        m_definition = (AspectWerkzDefinitionImpl)definition;

        // TODO: allow custom attribute parser, f.e. to support JSR-175
        m_attributeParser = new DefaultAspectAttributeParser();

        if (START_REMOTE_PROXY_SERVER) {
            startRemoteProxyServer();
        }
    }

    /**
     * Initializes the system.
     */
    public synchronized void initialize() {
        if (m_initialized) return;
        m_initialized = true;
        StartupManager.initializeSystem(m_uuid, m_definition);
    }

    /**
     * Checks if the definition is of type attribute definition.
     *
     * @return returns true for this system
     */
    public boolean isAttribDef() {
        return true;
    }

    /**
     * Checks if the definition is of type XML definition.
     *
     * @return returns false for this system
     */
    public boolean isXmlDef() {
        return false;
    }

    /**
     * Registers a new aspect.
     *
     * @param aspect the aspect to register
     * @param aspectMetaData the aspect meta-data
     */
    public void register(final Aspect aspect, final AspectMetaData aspectMetaData) {
        if (aspect == null) throw new IllegalArgumentException("aspect can not be null");
        if (aspectMetaData == null) throw new IllegalArgumentException("aspect meta-data can not be null");

        synchronized (m_aspects) {
            synchronized (m_aspectIndexes) {
                synchronized (m_adviceIndexes) {
                    synchronized (m_mixins) {
                        synchronized (m_aspectMetaDataMap) {
                            try {
                                m_aspectMetaDataMap.put(aspect.___AW_getName(), aspectMetaData);

                                final int indexAspect = m_aspects.length + 1;
                                m_aspectIndexes.put(aspect.___AW_getName(), indexAspect);

                                final Aspect[] tmpAspects = new Aspect[m_aspects.length + 1];
                                java.lang.System.arraycopy(m_aspects, 0, tmpAspects, 0, m_aspects.length);

                                tmpAspects[m_aspects.length] = aspect;

                                m_aspects = new Aspect[m_aspects.length + 1];
                                java.lang.System.arraycopy(tmpAspects, 0, m_aspects, 0, tmpAspects.length);

                                // retrieve a sorted advices list => matches the sorted method list in the container
                                List advices = aspect.___AW_getAspectDef().getAllAdvices();
                                for (Iterator it = advices.iterator(); it.hasNext();) {
                                    final AdviceDefinition adviceDef = (AdviceDefinition)it.next();
                                    m_adviceIndexes.put(
                                            adviceDef.getName(),
                                            new IndexTuple(indexAspect, adviceDef.getMethodIndex())
                                    );
                                }


                                List introductions = aspect.___AW_getAspectDef().getIntroductions();
                                for (Iterator it = introductions.iterator(); it.hasNext(); ) {
                                    IntroductionDefinition introDef = (IntroductionDefinition) it.next();
                                    Introduction mixin = new Introduction(introDef.getName(), aspect, introDef);
                                    //todo : very bad
                                    final Mixin[] tmpMixins = new Mixin[m_mixins.length + 1];
                                    java.lang.System.arraycopy(m_mixins, 0, tmpMixins, 0, m_mixins.length);
                                    tmpMixins[m_mixins.length] = mixin;
                                    m_mixins = new Mixin[m_mixins.length + 1];
                                    java.lang.System.arraycopy(tmpMixins, 0, m_mixins, 0, tmpMixins.length);
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();;
                                throw new DefinitionException("could not register aspect [" + aspect.___AW_getName() + "] due to: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates and registers new aspect at runtime.
     *
     * @param name the name of the aspect
     * @param className the class name of the aspect
     * @param deploymentModel the deployment model for the aspect
     *        (constants in the DeploymemtModel class, e.g. f.e. DeploymentModel.PER_JVM)
     * @param loader an optional class loader (if null it uses the context classloader)
     */
    public void createAspect(final String name,
                             final String className,
                             final int deploymentModel,
                             final ClassLoader loader) {
        if (name == null) throw new IllegalArgumentException("aspect name can not be null");
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (deploymentModel < 0 || deploymentModel > 3) throw new IllegalArgumentException(deploymentModel + " is not a valid deployment model type");

        Aspect prototype = null;
        Class aspectClass = null;
        try {
            if (loader == null) {
                aspectClass = ContextClassLoader.loadClass(className);
            }
            else {
                aspectClass = loader.loadClass(className);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("could not load aspect class [" + className + "] with name " +  name);
        }

        try {
            prototype = (Aspect)aspectClass.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException("could not create a new instance of aspect [" + className + "], does the class inherit the [org.codehaus.aspectwerkz.attribdef.aspect.Aspect] class?");
        }

        // parse the class attributes and create a definition
        AspectDefinition aspectDef = m_attributeParser.parse(aspectClass);
        m_definition.addAspect(aspectDef);

        prototype.___AW_setDeploymentModel(deploymentModel);
        prototype.___AW_setName(name);
        prototype.___AW_setAspectClass(prototype.getClass());
        prototype.___AW_setContainer(StartupManager.createAspectContainer(prototype));
        prototype.___AW_setAspectDef(aspectDef);

        // register the aspect
        register(prototype, new AspectMetaData(m_uuid, name, deploymentModel));
    }

    /**
     * Registers entering of a control flow join point.
     *
     * @param metaData the classname:methodMetaData metaData
     */
    public void enteringControlFlow(final ClassNameMethodMetaDataTuple metaData) {
        if (metaData == null) throw new IllegalArgumentException("classname:methodMetaData tuple can not be null");

        Set cflowSet = (Set)m_controlFlowLog.get();
        if (cflowSet == null) {
            cflowSet = new HashSet();
        }
        cflowSet.add(metaData);
        m_controlFlowLog.set(cflowSet);
    }

    /**
     * Registers exiting from a control flow join point.
     *
     * @param metaData the classname:methodMetaData metaData
     */
    public void exitingControlFlow(final ClassNameMethodMetaDataTuple metaData) {
        if (metaData == null) throw new IllegalArgumentException("classname:methodMetaData tuple can not be null");

        Set cflowSet = (Set)m_controlFlowLog.get();
        if (cflowSet == null) {
            return;
        }
        cflowSet.remove(metaData);
        m_controlFlowLog.set(cflowSet);
    }

    /**
     * Checks if we are in the control flow of a specific cflow pointcut.
     *
     * @param patternTuple the compiled tuple with the class pattern and the method pattern of the cflow pointcut
     * @return boolean
     */
    public boolean isInControlFlowOf(final PointcutPatternTuple patternTuple) {
        if (patternTuple == null) throw new IllegalArgumentException("class:method pattern tuple can not be null");

        Set cflowSet = (Set)m_controlFlowLog.get();
        if (cflowSet == null || cflowSet.isEmpty()) {
            return false;
        }
        else {
            for (Iterator it = cflowSet.iterator(); it.hasNext();) {
                ClassNameMethodMetaDataTuple tuple = (ClassNameMethodMetaDataTuple)it.next();
                CallerSidePattern callerSidePattern = ((CallerSidePattern)patternTuple.getPattern());
                if (callerSidePattern.matches(tuple.getClassName(), tuple.getMethodMetaData())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves a specific aspect based on index.
     *
     * @param index the index of the aspect
     * @return the aspect
     */
    public Aspect getAspect(final int index) {
        Aspect aspect;
        try {
            aspect = m_aspects[index - 1];
        }
        catch (Throwable e) {
            initialize();
            try {
                aspect = m_aspects[index - 1];
            }
            catch (ArrayIndexOutOfBoundsException e1) {
                throw new DefinitionException("no aspect with index " + index);
            }
        }
        return aspect;
    }

    /**
     * Returns the aspect for a specific name.
     *
     * @param name the name of the aspect
     * @return the the aspect
     */
    public Aspect getAspect(final String name) {
        Aspect aspect;
        try {
            aspect = m_aspects[m_aspectIndexes.get(name) - 1];
        }
        catch (Throwable e1) {
            initialize();
            try {
                aspect = m_aspects[m_aspectIndexes.get(name) - 1];
            }
            catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("aspect [" + name + "] is not properly defined");
            }
        }
        return aspect;
    }

    /**
     * Retrieves a specific mixin based on its index.
     *
     * @param index the index of the introduction (aspect in this case)
     * @return the the mixin (aspect in this case)
     */
    public Mixin getMixin(final int index) {
        Mixin mixin;
        try {
            mixin = m_mixins[index - 1];
        }
        catch (Throwable e1) {
            initialize();
            try {
                mixin = m_mixins[index - 1];
            }
            catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("no mixin with index " + index);
            }
        }
        return mixin;
    }

    /**
     * Returns the mixin implementation for a specific name.
     *
     * @param name the name of the introduction (aspect in this case)
     * @return the the mixin (aspect in this case)
     */
    public Mixin getMixin(final String name) {
        if (name == null) throw new IllegalArgumentException("introduction name can not be null");

        Mixin introduction;
        try {
            introduction = m_mixins[m_definition.getMixinIndexByName(name) - 1];
        }
        catch (Throwable e1) {
            initialize();
            try {
                introduction = m_mixins[m_definition.getMixinIndexByName(name) - 1];
            }
            catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("no introduction with name " + name);
            }
        }
        return introduction;
    }

    /**
     * Returns the index for a specific name to aspect mapping.
     *
     * @param name the name of the aspect
     * @return the index of the aspect
     */
    public int getAspectIndexFor(final String name) {
        if (name == null) throw new IllegalArgumentException("aspect name can not be null");
        final int index = m_aspectIndexes.get(name);
        if (index == 0) throw new DefinitionException("aspect " + name + " is not properly defined");
        return index;
    }

    /**
     * Returns the index for a specific name to advice mapping.
     *
     * @param name the name of the advice
     * @return the index of the advice
     */
    public IndexTuple getAdviceIndexFor(final String name) {
        if (name == null) throw new IllegalArgumentException("advice name can not be null");
        final IndexTuple index = (IndexTuple)m_adviceIndexes.get(name);
        if (index == null) throw new DefinitionException("advice " + name + " is not properly defined");
        return index;
    }

    /**
     * Returns the aspect meta-data for the name specified.
     *
     * @param name the name of the aspect
     * @return the aspect
     */
    public AspectMetaData getAspectMetaData(final String name) {
        if (name == null) throw new IllegalArgumentException("aspect name can not be null");

        if (m_aspectMetaDataMap.containsKey(name)) {
            return (AspectMetaData)m_aspectMetaDataMap.get(name);
        }
        else {
            initialize();
            if (m_aspectMetaDataMap.containsKey(name)) {
                return (AspectMetaData)m_aspectMetaDataMap.get(name);
            }
            else {
                throw new DefinitionException("aspect " + name + " is not properly defined");
            }
        }
    }

    /**
     * Returns the aspect meta-data for the class pattern specified.
     *
     * TODO: needed?
     *
     * @param classPattern the class pattern
     * @return the aspect
     */
    public AspectMetaData getAspectMetaData(final ClassPattern classPattern) {
        if (classPattern == null) throw new IllegalArgumentException("class pattern can not be null");

        if (m_aspectMetaDataMap.containsKey(classPattern)) {
            return (AspectMetaData)m_aspectMetaDataMap.get(classPattern);
        }
        else {
            initialize();
            if (m_aspectMetaDataMap.containsKey(classPattern)) {
                return (AspectMetaData)m_aspectMetaDataMap.get(classPattern);
            }
            else {
                throw new DefinitionException(classPattern.getPattern() + " does not have any aspects defined");
            }
        }
    }

    /**
     * Returns a list with all the aspects meta-data.
     *
     * @return the aspects
     */
    public Collection getAspectsMetaData() {
        initialize();
        return m_aspectMetaDataMap.values();
    }

    /**
     * Returns an array with all the aspects.
     *
     * @return the aspects
     */
    public Aspect[] getAspects() {
        initialize();
        return m_aspects;
    }

    /**
     * Returns the method pointcut list for the class and method specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getMethodPointcuts(final ClassMetaData classMetaData,
                                   final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        initialize();

        Integer hashKey = Util.calculateHash(classMetaData.getName(), methodMetaData);

        // if cached; return the cached list
        if (m_methodPointcutCache.containsKey(hashKey)) {
            return (List)m_methodPointcutCache.get(hashKey);
        }

        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            List methodPointcuts = aspect.getMethodPointcuts(classMetaData, methodMetaData);
            pointcuts.addAll(methodPointcuts);
        }

        synchronized (m_methodPointcutCache) {
            m_methodPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns the get field pointcut list for the class and field specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getGetFieldPointcuts(final ClassMetaData classMetaData,
                                     final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        initialize();

        Integer hashKey = Util.calculateHash(classMetaData.getName(), fieldMetaData);

        // if cached; return the cached list
        if (m_getFieldPointcutCache.containsKey(hashKey)) {
            return (List)m_getFieldPointcutCache.get(hashKey);
        }

        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            pointcuts.addAll(aspect.getGetFieldPointcuts(classMetaData, fieldMetaData));
        }

        synchronized (m_getFieldPointcutCache) {
            m_getFieldPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns the set field pointcut list for the class and field specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getSetFieldPointcuts(final ClassMetaData classMetaData,
                                     final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        initialize();

        Integer hashKey = Util.calculateHash(classMetaData.getName(), fieldMetaData);

        // if cached; return the cached list
        if (m_setFieldPointcutCache.containsKey(hashKey)) {
            return (List)m_setFieldPointcutCache.get(hashKey);
        }

        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            pointcuts.addAll(aspect.getSetFieldPointcuts(classMetaData, fieldMetaData));
        }

        synchronized (m_setFieldPointcutCache) {
            m_setFieldPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns the throws pointcut list for the class and method specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getThrowsPointcuts(final ClassMetaData classMetaData,
                                   final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        initialize();

        Integer hashKey = Util.calculateHash(classMetaData.getName(), methodMetaData);

        // if cached; return the cached list
        if (m_throwsPointcutCache.containsKey(hashKey)) {
            return (List)m_throwsPointcutCache.get(hashKey);
        }

        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            pointcuts.addAll(aspect.getThrowsPointcuts(classMetaData, methodMetaData));
        }

        synchronized (m_throwsPointcutCache) {
            m_throwsPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns the caller side pointcut list for the class and method specified.
     * Caches the list, needed since the actual method call is expensive
     * and is made each time a new instance of an advised class is created.
     *
     * @param className the class name
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getCallerSidePointcuts(final String className,
                                       final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        initialize();

        Integer hashKey = Util.calculateHash(className, methodMetaData);

        // if cached; return the cached list
        if (m_callerSidePointcutCache.containsKey(hashKey)) {
            return (List)m_callerSidePointcutCache.get(hashKey);
        }

        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            pointcuts.addAll(aspect.getCallerSidePointcuts(className, methodMetaData));
        }

        synchronized (m_callerSidePointcutCache) {
            m_callerSidePointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns a list with the cflow pointcuts that affects the join point with the
     * class name and the method name specified.
     *
     * @param className the name of the class for the join point
     * @param methodMetaData the meta-data for the method for the join point
     * @return a list with the cflow pointcuts
     */
    public List getCFlowPointcuts(final String className,
                                  final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        initialize();

        Integer hashKey = Util.calculateHash(className, methodMetaData);

        // if cached; return the cached list
        if (m_cflowPointcutCache.containsKey(hashKey)) {
            return (List)m_cflowPointcutCache.get(hashKey);
        }

        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            pointcuts.addAll(aspect.getCFlowPointcuts(className, methodMetaData));
        }

        synchronized (m_cflowPointcutCache) {
            m_cflowPointcutCache.put(hashKey, pointcuts);
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
        if (name == null) throw new IllegalArgumentException("aspect name can not be null");

        initialize();
        if (m_aspectMetaDataMap.containsKey(name)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns a specific method by the class and the method index.
     *
     * @param klass the class housing the method
     * @param index the method index
     * @return the method
     */
    public Method getMethod(final Class klass, final int index) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");
        if (index < 0) throw new IllegalArgumentException("method index can not be less than 0");

        try {
            // create the method repository lazily
            if (!m_methods.containsKey(klass)) {
                createMethodRepository(klass);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }

        Method method;
        try {
            method = ((Method[])m_methods.get(klass))[index];
        }
        catch (Throwable e1) {
            initialize();
            try {
                method = ((Method[])m_methods.get(klass))[index];
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return method;
    }

    /**
     * Creates a new method repository for the class specified.
     *
     * @param klass the class
     */
    protected void createMethodRepository(final Class klass) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");

        final List methods = new ArrayList();
        collectMethods(klass, methods);

        Collections.sort(methods, MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD));

        final Method[] sortedMethods = new Method[methods.size()];
        for (int i = 0; i < sortedMethods.length; i++) {
            sortedMethods[i] = (Method)methods.get(i);
        }

        synchronized (m_methods) {
            m_methods.put(klass, sortedMethods);
        }
    }

    /**
     * Collects all methods for the class specified, calls itself recursively with
     * the class' super class as argument to collect all methods.
     *
     * @param klass the class
     * @param methods the method list
     */
    protected void collectMethods(final Class klass, final List methods) {

        final Method[] declaredMethods = klass.getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {

            // add only the advised original methods to the lookup table,
            // method pairs that consists of original:proxy
            if (declaredMethods[i].getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX)) {
                methods.add(declaredMethods[i]);
            }
        }
    }

    /**
     * Starts up the remote proxy server.
     */
    private void startRemoteProxyServer() {
        Invoker invoker = getInvoker();
        m_remoteProxyServer = new RemoteProxyServer(ContextClassLoader.getLoader(), invoker);
        m_remoteProxyServer.start();
    }

    /**
     * Returns the Invoker instance to use.
     *
     * @return the Invoker
     */
    private Invoker getInvoker() {
        Invoker invoker = null;
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(
                    java.lang.System.getProperty("aspectwerkz.resource.bundle")
            ));
            String className = properties.getProperty("remote.server.invoker.classname");
            invoker = (Invoker)ContextClassLoader.getLoader().loadClass(className).newInstance();
        }
        catch (Exception e) {
            invoker = getDefaultInvoker();
        }
        return invoker;
    }

    /**
     * Returns the default Invoker.
     *
     * @return the default invoker
     */
    private Invoker getDefaultInvoker() {
        return new Invoker() {
            public Object invoke(final String handle,
                                 final String methodName,
                                 final Class[] paramTypes,
                                 final Object[] args,
                                 final Object context) {
                Object result = null;
                try {
                    final Object instance = RemoteProxy.getWrappedInstance(handle);
                    final Method method = instance.getClass().getMethod(methodName, paramTypes);
                    result = method.invoke(instance, args);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
                return result;
            }
        };
    }
}

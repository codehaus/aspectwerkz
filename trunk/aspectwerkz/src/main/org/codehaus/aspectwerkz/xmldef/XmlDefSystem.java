/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.WeakHashMap;
import java.util.Properties;
import java.lang.reflect.Method;
import java.io.FileInputStream;

import org.codehaus.aspectwerkz.xmldef.advice.Advice;
import org.codehaus.aspectwerkz.xmldef.advice.AbstractAdvice;
import org.codehaus.aspectwerkz.xmldef.introduction.Introduction;
import org.codehaus.aspectwerkz.xmldef.definition.StartupManager;
import org.codehaus.aspectwerkz.xmldef.definition.AspectWerkzDefinitionImpl;
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
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.util.Util;
import org.codehaus.aspectwerkz.connectivity.RemoteProxyServer;
import org.codehaus.aspectwerkz.connectivity.Invoker;
import org.codehaus.aspectwerkz.connectivity.RemoteProxy;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.AspectMetaData;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.System;

/**
 * Manages the aspects in the AspectWerkz system.<br/>
 * Handles the initialization and configuration of the system.<br/>
 * Stores and indexes the aspects defined in the system.<br/>
 * Stores and indexes the advised methods.<br/>
 * Stores and indexes the introduced methods.<br/>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class XmlDefSystem implements System {

    /**
     * Holds references to all the the aspects in the system.
     */
    private final Map m_aspects = new SequencedHashMap();

    /**
     * and cache for the method pointcuts.
     *
     * @todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_methodPointcutCache = new WeakHashMap();

    /**
     * and cache for the get field pointcuts.
     *
     * @todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_getFieldPointcutCache = new WeakHashMap();

    /**
     * and cache for the set field pointcuts.
     *
     * @todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_setFieldPointcutCache = new WeakHashMap();

    /**
     * and cache for the throws pointcuts.
     *
     * @todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_throwsPointcutCache = new WeakHashMap();

    /**
     * and cache for the caller side pointcuts.
     *
     * @todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_callerSidePointcutCache = new WeakHashMap();

    /**
     * and cache for the cflow pointcuts.
     *
     * @todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private final Map m_cflowPointcutCache = new WeakHashMap();

    /**
     * Holds references to all the the advised methods in the system.
     */
    private final Map m_methods = new HashMap();

    /**
     * Holds references to all the the advices in the system.
     */
    private Advice[] m_advices = new Advice[0];

    /**
     * Holds the indexes for the advices.
     */
    private final Map m_adviceIndexes = new HashMap();

    /**
     * Holds references to all the the introductions in the system.
     */
    private Introduction[] m_introductions = new Introduction[0];

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
     * Creates a new AspectWerkz system instance.
     * Sets the UUID for the system.
     * Is set to private since the instance should be retrieved using the getSystem(..) method.
     *
     * @param uuid the UUID for the system
     * @param definition the definition for the system
     */
    public XmlDefSystem(final String uuid, final AspectWerkzDefinition definition) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (definition == null) throw new IllegalArgumentException("definition can not be null");

        m_uuid = uuid;
        m_definition = (AspectWerkzDefinitionImpl)definition;

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
     * @return returns false for this system
     */
    public boolean isAttribDef() {
        return false;
    }

    /**
     * Checks if the definition is of type XML definition.
     *
     * @return returns true for this system
     */
    public boolean isXmlDef() {
        return true;
    }

    /**
     * Registers a new aspect for a specific class.
     *
     * @param aspect the aspect to register
     */
    public void register(final AspectMetaData aspect) {
        if (aspect == null) throw new IllegalArgumentException("aspect can not be null");
        if (aspect.getName() == null) throw new IllegalArgumentException("aspect name can not be null");

        synchronized (m_aspects) {
            m_aspects.put(aspect.getName(), aspect);
        }
    }

    /**
     * Registers a new advice and maps it to a name.
     *
     * @param name the name to map the advice to
     * @param advice the advice to register
     */
    public void register(final String name, final Advice advice) {
        if (name == null) throw new IllegalArgumentException("advice name can not be null");
        if (advice == null) throw new IllegalArgumentException("advice can not be null");

        synchronized (m_adviceIndexes) {
            synchronized (m_advices) {
                final int index = m_advices.length + 1;
                m_adviceIndexes.put(name, new IndexTuple(index, -1));

                final Advice[] tmp = new Advice[m_advices.length + 1];
                java.lang.System.arraycopy(m_advices, 0, tmp, 0, m_advices.length);

                tmp[m_advices.length] = advice;

                m_advices = new Advice[m_advices.length + 1];
                java.lang.System.arraycopy(tmp, 0, m_advices, 0, tmp.length);
            }
        }
    }

    /**
     * Registers an introduction and maps it to a name.
     * At the moment it is not possible to add new introductions at runtime (don't know
     * if it makes sense).
     *
     * @param name the name to map the introduction to
     * @param introduction the introduction to register
     */
    public void register(final String name, final Introduction introduction) {
        if (name == null) throw new IllegalArgumentException("introduction name can not be null");
        if (introduction == null) throw new IllegalArgumentException("introduction can not be null");

        synchronized (m_introductions) {
            int nrOfIntroductions = m_definition.getIntroductionIndexes().size();
            if (m_introductions.length == 0) {
                m_introductions = new Introduction[nrOfIntroductions];
            }
            int index = m_definition.getIntroductionIndex(name) - 1;
            m_introductions[index] = introduction;
        }
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
     * Creates and registers new advice at runtime.
     *
     * @param name the name of the advice
     * @param className the class name of the advice
     * @param deploymentModel the deployment model for the advice
     * @param loader an optional class loader (if null it uses the context classloader)
     */
    public void createAdvice(final String name,
                             final String className,
                             final String deploymentModel,
                             final ClassLoader loader) {
        if (name == null) throw new IllegalArgumentException("advice name can not be null");
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (deploymentModel == null) throw new IllegalArgumentException("deployment model can not be null");

        AbstractAdvice prototype = null;
        Class adviceClass = null;
        try {
            if (loader == null) {
                adviceClass = ContextClassLoader.loadClass(className);
            }
            else {
                adviceClass = loader.loadClass(className);
            }
            prototype = (AbstractAdvice)adviceClass.newInstance();
        }
        catch (Exception e) {
            StringBuffer cause = new StringBuffer();
            cause.append("could not deploy new prototype with name ");
            cause.append(name);
            cause.append(" and class ");
            cause.append(className);
            cause.append(" due to: ");
            cause.append(e);
            throw new RuntimeException(cause.toString());
        }

        prototype.setDeploymentModel(DeploymentModel.getDeploymentModelAsInt(deploymentModel));
        prototype.setName(name);
        prototype.setAdviceClass(prototype.getClass());

        prototype.setContainer(StartupManager.createAdviceContainer(prototype));

        // register the advice
        register(name, prototype);
    }

    /**
     * Returns the aspect for the name specified.
     *
     * @param name the name of the aspect
     * @return the aspect
     */
    public AspectMetaData getAspectMetaData(final String name) {
        if (name == null) throw new IllegalArgumentException("aspect name can not be null");

        if (m_aspects.containsKey(name)) {
            return (AspectMetaData)m_aspects.get(name);
        }
        else {
            initialize();
            if (m_aspects.containsKey(name)) {
                return (AspectMetaData)m_aspects.get(name);
            }
            else {
                throw new DefinitionException("aspect " + name + " is not properly defined");
            }
        }
    }

    /**
     * Returns the aspect for the class pattern specified.
     *
     * @param classPattern the class pattern
     * @return the aspect
     */
    public AspectMetaData getAspectMetaData(final ClassPattern classPattern) {
        if (classPattern == null) throw new IllegalArgumentException("class pattern can not be null");

        if (m_aspects.containsKey(classPattern)) {
            return (AspectMetaData)m_aspects.get(classPattern);
        }
        else {
            initialize();
            if (m_aspects.containsKey(classPattern)) {
                return (AspectMetaData)m_aspects.get(classPattern);
            }
            else {
                throw new DefinitionException(classPattern.getPattern() + " does not have any aspects defined");
            }
        }
    }

    /**
     * Returns a list with all the aspects.
     *
     * @return the aspects
     */
    public Collection getAspectsMetaData() {
        initialize();
        return m_aspects.values();
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
        for (Iterator it = m_aspects.values().iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            pointcuts.addAll(aspect.getMethodPointcuts(classMetaData, methodMetaData));
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
     * @param methodMetaData meta-data for the method
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
        for (Iterator it = m_aspects.values().iterator(); it.hasNext();) {
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
     * @param methodMetaData meta-data for the method
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
        for (Iterator it = m_aspects.values().iterator(); it.hasNext();) {
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
        for (Iterator it = m_aspects.values().iterator(); it.hasNext();) {
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
        for (Iterator it = m_aspects.values().iterator(); it.hasNext();) {
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
        for (Iterator it = m_aspects.values().iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            pointcuts.addAll(aspect.getCFlowPointcuts(className, methodMetaData));
        }

        synchronized (m_cflowPointcutCache) {
            m_cflowPointcutCache.put(hashKey, pointcuts);
        }

        return pointcuts;
    }

    /**
     * Returns the index for a specific name to advice mapping.
     *
     * @param name the name of the advice
     * @return the index of the advice
     */
    public IndexTuple getAdviceIndexFor(final String name) {
        if (name == null) throw new IllegalArgumentException("advice name can not be null");
        return (IndexTuple)m_adviceIndexes.get(name);
    }

    /**
     * Retrieves a specific advice based setfield's index.
     *
     * @param index the index of the advice
     * @return the advice
     */
    public Advice getAdvice(final IndexTuple index) {
        Advice advice;
        try {
            advice = m_advices[index.getAspectIndex() - 1];
        }
        catch (Throwable e) {
            initialize();
            try {
                advice = m_advices[index.getAspectIndex() - 1];
            }
            catch (ArrayIndexOutOfBoundsException e1) {
                throw new DefinitionException("no advice with index " + index);
            }
        }
        return advice;
    }

    /**
     * Returns the advice for a specific name.
     *
     * @param name the name of the advice
     * @return the the advice
     */
    public Advice getAdvice(final String name) {
        Advice advice;
        try {
            advice = m_advices[((IndexTuple)m_adviceIndexes.get(name)).getAspectIndex() - 1];
        }
        catch (Throwable e1) {
            initialize();
            try {
                advice = m_advices[((IndexTuple)m_adviceIndexes.get(name)).getAspectIndex() - 1];
            }
            catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("advice " + name + " is not properly defined");
            }
        }
        return advice;
    }

    /**
     * Returns an array with all the introductions in the system.
     *
     * @return the introductions
     */
    public Introduction[] getIntroductions() {
        return m_introductions;
    }

    /**
     * Returns the index for a specific name to introduction mapping.
     *
     * @param name the name of the introduction
     * @return the index of the introduction
     */
    public int getIntroductionIndex(final String name) {
        if (name == null) throw new IllegalArgumentException("introduction name can not be null");

        final int index = m_definition.getIntroductionIndex(name);
        if (index == 0) throw new DefinitionException("introduction " + name + " is not properly defined");
        return index;
    }

    /**
     * Retrieves a specific introduction based it's index.
     *
     * @param index the index of the introduction
     * @return the introduction
     */
    public Mixin getMixin(final int index) {
        Mixin introduction;
        try {
            introduction = m_introductions[index - 1];
        }
        catch (Throwable e1) {
            initialize();
            try {
                introduction = m_introductions[index - 1];
            }
            catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("no introduction with index " + index);
            }
        }
        return introduction;
    }

    /**
     * Returns the introduction for a specific name.
     *
     * @param name the name of the introduction
     * @return the the introduction
     */
    public Mixin getMixin(final String name) {
        if (name == null) throw new IllegalArgumentException("introduction name can not be null");

        Mixin introduction;
        try {
            introduction = m_introductions[m_definition.getIntroductionIndex(name) - 1];
        }
        catch (Throwable e1) {
            initialize();
            try {
                introduction = m_introductions[m_definition.getIntroductionIndex(name) - 1];
            }
            catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("no introduction with name " + name);
            }
        }
        return introduction;
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
        if (m_aspects.containsKey(name)) {
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
    private void createMethodRepository(final Class klass) {
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
     * @param addedMethods the method added to the method list
     */
    private void collectMethods(final Class klass, final List methods) {

        final Method[] declaredMethods = klass.getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {

            // add only the advised original methods to the lookup table,
            // method pairs that consists of original:proxy
            if (declaredMethods[i].getName().startsWith(
                    TransformationUtil.ORIGINAL_METHOD_PREFIX)) {
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

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.aspect.Introduction;
import org.codehaus.aspectwerkz.aspect.DefaultIntroductionContainerStrategy;
import org.codehaus.aspectwerkz.pointcut.PointcutManager;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.StartupManager;
import org.codehaus.aspectwerkz.definition.SystemDefinition;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TLongObjectHashMap;

/**
 * Stores the aspects, advices, pointcuts etc. Manages the method, advice and aspect indexing.
 *
 * TODO: FIRST REFACTOR
 * 1. Use hashes, aspect=>hashcode for class advice=>hashcode for method signature
 * 3. Store references to all join points that uses advices from a certain aspect [aspectKey=>joinPoints]
 * 4. Map all aspects to a key, meaning have a key that maps to a data structure that contains full info about the
 * aspect and all its advice methods. [aspectKey=>aspectDataStructure].
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectRegistry {

    /**
     * The UUID for the system.
     */
    private final String m_uuid;

    /**
     * The definition.
     */
    private final SystemDefinition m_definition;

    /**
     * Marks the system as initialized.
     */
    private boolean m_initialized = false;

    /**
     * Sorted map with PointcutManager instance containing the pointcut instance the aspect,
     * mapped to its name (the name of the class implementing the aspect).
     */
    private final Map m_aspectMetaDataMap = new SequencedHashMap();

    /**
     * Holds the indexes for the aspects, maps the aspect name to the index for the aspect.
     */
    private final TObjectIntHashMap m_aspectIndexes = new TObjectIntHashMap();

    /**
     * Holds the index (a tuple of the aspect index and the advice index) for the advices,
     * mapped to its name ([fullyQualifiedClassName].[methodName]).
     */
    private final Map m_adviceIndexes = new HashMap();

    /**
     * An array with all the the Aspect prototypes in the system.
     */
    private Aspect[] m_aspects = new Aspect[0];

    /**
     * An array of all the mixins in the system, each nested class in aspect has its own index.
     */
    private Mixin[] m_mixins = new Mixin[0];

    /**
     * Holds references to all the the advised methods in the system, maps the target Class to a sorted list of
     * all the advised methods in the class.
     */
    private final Map m_methods = new HashMap();

    /**
     * Holds references to all the the advised constructors in the system, maps the target Class to a sorted list of
     * all the advised constructors in the class.
     */
    private final Map m_constructors = new HashMap();

    /**
     * Creates a new aspect registry.
     *
     * @param uuid the system UUID
     * @param definition the system definition
     */
    public AspectRegistry(final String uuid, final SystemDefinition definition) {
        m_uuid = uuid;
        m_definition = definition;
    }

    /**
     * Initializes the aspect registry. The initialization needs to be separated fromt he construction
     * of the registry, and is triggered by the runtime system.
     */
    public void initialize() {
        synchronized (this) {
            if (m_initialized) return;
            m_initialized = true;
            StartupManager.initializeSystem(m_uuid, m_definition);
        }
    }

    /**
     * Registers a new aspect.
     *
     * @param aspect the aspect to register
     * @param aspectMetaData the aspect meta-data
     */
    public void register(final Aspect aspect, final PointcutManager aspectMetaData) {
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
                                for (Iterator it = introductions.iterator(); it.hasNext();) {
                                    IntroductionDefinition introDef = (IntroductionDefinition)it.next();
                                    // load default mixin impl from the aspect which defines it
                                    Class defaultImplClass = aspect.getClass().getClassLoader().loadClass(introDef.getName());
                                    Introduction mixin = new Introduction(introDef.getName(), defaultImplClass, aspect, introDef);
                                    // prepare the container
                                    DefaultIntroductionContainerStrategy introContainer = new DefaultIntroductionContainerStrategy(mixin, aspect.___AW_getContainer());
                                    mixin.setContainer(introContainer);
                                    final Mixin[] tmpMixins = new Mixin[m_mixins.length + 1];
                                    java.lang.System.arraycopy(m_mixins, 0, tmpMixins, 0, m_mixins.length);
                                    tmpMixins[m_mixins.length] = mixin;
                                    m_mixins = new Mixin[m_mixins.length + 1];
                                    java.lang.System.arraycopy(tmpMixins, 0, m_mixins, 0, tmpMixins.length);
                                }
                            }
                            catch (Exception e) {
                                throw new DefinitionException("could not register aspect [" + aspect.___AW_getName() + "] due to: " + e.toString());
                            }
                        }
                    }
                }
            }
        }
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
    public PointcutManager getAspectMetaData(final String name) {
        if (name == null) throw new IllegalArgumentException("aspect name can not be null");
        if (m_aspectMetaDataMap.containsKey(name)) {
            return (PointcutManager)m_aspectMetaDataMap.get(name);
        }
        else {
            initialize();
            if (m_aspectMetaDataMap.containsKey(name)) {
                return (PointcutManager)m_aspectMetaDataMap.get(name);
            }
            else {
                throw new DefinitionException("aspect " + name + " is not properly defined");
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
     * Returns the execution pointcut list for the class and method specified.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getExecutionPointcuts(final ClassMetaData classMetaData, final MethodMetaData methodMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            List methodPointcuts = aspect.getExecutionPointcuts(classMetaData, methodMetaData);
            pointcuts.addAll(methodPointcuts);
        }
        return pointcuts;
    }

    /**
     * Returns the get pointcut list for the class and field specified.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData meta-data for the field
     * @return the pointcuts for this join point
     */
    public List getGetPointcuts(final ClassMetaData classMetaData, final FieldMetaData fieldMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            pointcuts.addAll(aspect.getGetPointcuts(classMetaData, fieldMetaData));
        }
        return pointcuts;
    }

    /**
     * Returns the set pointcut list for the class and field specified.
     *
     * @param classMetaData the meta-data for the class
     * @param fieldMetaData meta-data for the field
     * @return the pointcuts for this join point
     */
    public List getSetPointcuts(final ClassMetaData classMetaData, final FieldMetaData fieldMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            pointcuts.addAll(aspect.getSetPointcuts(classMetaData, fieldMetaData));
        }
        return pointcuts;
    }

    /**
     * Returns the throws pointcut list for the class and method specified.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getThrowsPointcuts(final ClassMetaData classMetaData, final MethodMetaData methodMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            pointcuts.addAll(aspect.getThrowsPointcuts(classMetaData, methodMetaData));
        }
        return pointcuts;
    }

    /**
     * Returns the call pointcut list for the class and method specified.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getCallPointcuts(final ClassMetaData classMetaData, final MethodMetaData methodMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            pointcuts.addAll(aspect.getCallPointcuts(classMetaData, methodMetaData));
        }
        return pointcuts;
    }

    /**
     * Returns the cflow pointcut list for the class and method specified.
     *
     * @param classMetaData the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getCflowPointcuts(final ClassMetaData classMetaData, final MethodMetaData methodMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_aspectMetaDataMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            pointcuts.addAll(aspect.getCFlowExpressions(classMetaData, methodMetaData));
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
     * Returns a specific method by the class and the method hash.
     *
     * @param klass the class housing the method
     * @param methodHash the method hash
     * @return the method
     */
    public Method getMethod(final Class klass, final int methodHash) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");
        if (methodHash < 0) throw new IllegalArgumentException("method hash is not a valid hash");

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
            method = (Method)((TLongObjectHashMap)m_methods.get(klass)).get(methodHash);
        }
        catch (Throwable e1) {
            initialize();
            try {
                method = (Method)((TLongObjectHashMap)m_methods.get(klass)).get(methodHash);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return method;
    }

    /**
     * Returns a specific constructor by the class and the method hash.
     *
     * @param klass the class housing the method
     * @param methodHash the method hash
     * @return the method
     */
    public Method getConstructor(final Class klass, final int methodHash) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");
        if (methodHash < 0) throw new IllegalArgumentException("method hash is not a valid hash");

        try {
            // create the method repository lazily
            if (!m_constructors.containsKey(klass)) {
                createMethodRepository(klass);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }

        Method method;
        try {
            method = (Method)((TLongObjectHashMap)m_constructors.get(klass)).get(methodHash);
        }
        catch (Throwable e1) {
            initialize();
            try {
                method = (Method)((TLongObjectHashMap)m_constructors.get(klass)).get(methodHash);
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

        Method[] methods = klass.getDeclaredMethods();
        TLongObjectHashMap methodMap = new TLongObjectHashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            int methodHash = TransformationUtil.calculateHash(method);
            methodMap.put(methodHash, method);
        }

        synchronized (m_methods) {
            m_methods.put(klass, methodMap);
        }
    }

    /**
     * Creates a new constructor repository for the class specified.
     *
     * @param klass the class
     */
    protected void createConstructorRepository(final Class klass) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");

        Constructor[] constructors = klass.getDeclaredConstructors();
        TLongObjectHashMap constructorMap = new TLongObjectHashMap(constructors.length);
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];

            int constructorHash = TransformationUtil.calculateHash(constructor);
            constructorMap.put(constructorHash, constructor);
        }

        synchronized (m_constructors) {
            m_constructors.put(klass, constructorMap);
        }
    }
}

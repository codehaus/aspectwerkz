/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.aspect.Introduction;
import org.codehaus.aspectwerkz.aspect.IntroductionContainer;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.StartupManager;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.util.Strings;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Stores the aspects, advices, pointcuts etc. Manages the method, advice and aspect indexing.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @TODO Use hashes, aspect=>hashcode for class advice=>hashcode for method signature
 * @TODO Store references to all join points that uses advices from a certain aspect [aspectKey=>joinPoints]
 * @TODO Map all aspects to a key, meaning have a key that maps to a data structure that contains full info about the
 * aspect and all its advice methods. [aspectKey=>aspectDataStructure].
 */
public class AspectRegistry {
    /**
     * Holds references to the methods to the advised classes in the system.
     */
    private final static Map s_methods = new HashMap(); //WEAK

    /**
     * Holds references to the fields to the advised classes in the system.
     */
    private final static Map s_fields = new HashMap();

    /**
     * Holds references to all the the advised constructors in the system, maps the target Class to a sorted list of all
     * the advised constructors in the class.
     */
    private final static Map s_constructors = new HashMap();

    /**
     * The AspectManager for the system.
     */
    private final AspectManager m_aspectManager;

    /**
     * The definition.
     */
    private final SystemDefinition m_definition;

    /**
     * Marks the system as initialized.
     */
    private boolean m_initialized = false;

    /**
     * Sorted map with PointcutManager instance containing the pointcut instance the aspect, mapped to its name (the
     * name of the class implementing the aspect).
     */
    private final Map m_pointcutManagerMap = new SequencedHashMap();

    /**
     * Holds the indexes for the aspects, maps the aspect name to the index for the aspect.
     */
    private final TObjectIntHashMap m_aspectIndexes = new TObjectIntHashMap();

    /**
     * Holds the index (a tuple of the aspect index and the advice index) for the advices, mapped to its name
     * ([fullyQualifiedClassName].[methodName]).
     */
    private final Map m_adviceIndexes = new HashMap();

    /**
     * An array with all the the aspect containers in the system.
     */
    private AspectContainer[] m_aspectContainers = new AspectContainer[0];

    /**
     * An array of all the mixins in the system, each nested class in aspect has its own index.
     */
    private Mixin[] m_mixins = new Mixin[0];

    /**
     * Creates a new aspect registry.
     *
     * @param aspectManager the system aspectManager
     * @param definition    the system definition
     */
    public AspectRegistry(final AspectManager aspectManager, final SystemDefinition definition) {
        m_aspectManager = aspectManager;
        m_definition = definition;
    }

    /**
     * Initializes the aspect registry. The initialization needs to be separated fromt he construction of the registry,
     * and is triggered by the runtime system.
     */
    public void initialize() {
        synchronized (this) {
            if (m_initialized) {
                return;
            }
            m_initialized = true;
            StartupManager.initializeSystem(m_aspectManager, m_definition);
        }
    }

    /**
     * Registers a new aspect.
     *
     * @param container       the container for the aspect to register
     * @param pointcutManager the pointcut manager
     */
    public void register(final AspectContainer container, final PointcutManager pointcutManager) {
        if (container == null) {
            throw new IllegalArgumentException("aspect container can not be null");
        }
        if (pointcutManager == null) {
            throw new IllegalArgumentException("pointcut manager can not be null");
        }
        synchronized (m_aspectContainers) {
            synchronized (m_aspectIndexes) {
                synchronized (m_adviceIndexes) {
                    synchronized (m_mixins) {
                        synchronized (m_pointcutManagerMap) {
                            try {
                                CrossCuttingInfo crossCuttingInfo = container.getCrossCuttingInfo();
                                m_pointcutManagerMap.put(crossCuttingInfo.getName(), pointcutManager);
                                final int indexAspect = m_aspectContainers.length + 1;
                                m_aspectIndexes.put(crossCuttingInfo.getName(), indexAspect);
                                final Object[] tmpAspects = new Object[m_aspectContainers.length + 1];
                                java.lang.System.arraycopy(m_aspectContainers, 0, tmpAspects, 0,
                                                           m_aspectContainers.length);
                                tmpAspects[m_aspectContainers.length] = container;
                                m_aspectContainers = new AspectContainer[m_aspectContainers.length + 1];
                                java.lang.System.arraycopy(tmpAspects, 0, m_aspectContainers, 0, tmpAspects.length);

                                // retrieve a sorted advices list => matches the sorted method list in the container
                                List advices = crossCuttingInfo.getAspectDefinition().getAllAdvices();
                                for (Iterator it = advices.iterator(); it.hasNext();) {
                                    final AdviceDefinition adviceDef = (AdviceDefinition)it.next();
                                    IndexTuple tuple = new IndexTuple(indexAspect, adviceDef.getMethodIndex(),
                                                                      m_aspectManager);

                                    //prefix AdviceName with AspectName to allow AspectReuse
                                    m_adviceIndexes.put(crossCuttingInfo.getName() + "/" + adviceDef.getName(), tuple);
                                }

                                // mixins
                                List introductions = crossCuttingInfo.getAspectDefinition().getIntroductions();
                                for (Iterator it = introductions.iterator(); it.hasNext();) {
                                    IntroductionDefinition introDef = (IntroductionDefinition)it.next();

                                    // load default mixin impl from the aspect which defines it
                                    Class defaultImplClass = crossCuttingInfo.getAspectClass().getClassLoader()
                                                                             .loadClass(introDef.getName());
                                    Introduction mixin = new Introduction(introDef.getName(), defaultImplClass,
                                                                          crossCuttingInfo, introDef);

                                    // prepare the container
                                    mixin.setContainer(new IntroductionContainer(mixin, container));
                                    final Mixin[] tmpMixins = new Mixin[m_mixins.length + 1];
                                    java.lang.System.arraycopy(m_mixins, 0, tmpMixins, 0, m_mixins.length);
                                    tmpMixins[m_mixins.length] = mixin;
                                    m_mixins = new Mixin[m_mixins.length + 1];
                                    java.lang.System.arraycopy(tmpMixins, 0, m_mixins, 0, tmpMixins.length);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new DefinitionException("could not register aspect ["
                                                              + container.getCrossCuttingInfo().getName()
                                                              + "] due to: " + e.toString());
                            }
                            if (m_aspectContainers.length != m_aspectIndexes.size()) {
                                throw new IllegalStateException("aspect indexing out of synch");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves a specific aspect container based on index.
     *
     * @param index the index of the aspect
     * @return the aspect container
     */
    public AspectContainer getAspectContainer(final int index) {
        AspectContainer aspect;
        try {
            aspect = m_aspectContainers[index - 1];
        } catch (Throwable e) {
            initialize();
            try {
                aspect = m_aspectContainers[index - 1];
            } catch (ArrayIndexOutOfBoundsException e1) {
                throw new DefinitionException("no aspect with index " + index);
            }
        }
        return aspect;
    }

    /**
     * Returns the aspect container for a specific name.
     *
     * @param name the name of the aspect
     * @return the the aspect container
     */
    public AspectContainer getAspectContainer(final String name) {
        AspectContainer container;
        try {
            container = m_aspectContainers[m_aspectIndexes.get(name) - 1];
        } catch (Throwable e1) {
            initialize();
            try {
                container = m_aspectContainers[m_aspectIndexes.get(name) - 1];
            } catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("container for cross-cutting class [" + name
                                              + "] is not properly defined");
            }
        }
        return container;
    }

    /**
     * Returns the aspect for a specific name, deployed as perJVM.
     *
     * @param name the name of the aspect
     * @return the the aspect
     */
    public CrossCuttingInfo getCrossCuttingInfo(final String name) {
        return getAspectContainer(name).getCrossCuttingInfo();
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
        } catch (Throwable e1) {
            initialize();
            try {
                mixin = m_mixins[index - 1];
            } catch (ArrayIndexOutOfBoundsException e2) {
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
        if (name == null) {
            throw new IllegalArgumentException("introduction name can not be null");
        }
        Mixin introduction;
        try {
            introduction = m_mixins[m_definition.getMixinIndexByName(name) - 1];
        } catch (Throwable e1) {
            initialize();
            try {
                introduction = m_mixins[m_definition.getMixinIndexByName(name) - 1];
            } catch (ArrayIndexOutOfBoundsException e2) {
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
        if (name == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }
        final int index = m_aspectIndexes.get(name);
        if (index == 0) {
            throw new DefinitionException("aspect " + name + " is not properly defined");
        }
        return index;
    }

    /**
     * Returns the index for a specific name to advice mapping.
     *
     * @param name the name of the advice
     * @return the index of the advice
     */
    public IndexTuple getAdviceIndexFor(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("advice name can not be null");
        }
        final IndexTuple index = (IndexTuple)m_adviceIndexes.get(name);
        if (index == null) {
            throw new DefinitionException("advice " + name + " is not properly defined");
        }
        return index;
    }

    /**
     * Returns the pointcut managers for the name specified.
     *
     * @param name the name of the aspect
     * @return the pointcut manager
     */
    public PointcutManager getPointcutManager(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }
        if (m_pointcutManagerMap.containsKey(name)) {
            return (PointcutManager)m_pointcutManagerMap.get(name);
        } else {
            initialize();
            if (m_pointcutManagerMap.containsKey(name)) {
                return (PointcutManager)m_pointcutManagerMap.get(name);
            } else {
                throw new DefinitionException("aspect " + name + " is not properly defined");
            }
        }
    }

    /**
     * Returns a list with all the pointcut managers.
     *
     * @return the pointcut managers
     */
    public Collection getPointcutManagers() {
        initialize();
        return m_pointcutManagerMap.values();
    }

    /**
     * Returns an array with all the aspect containers.
     *
     * @return the aspect containers
     */
    public AspectContainer[] getAspectContainers() {
        initialize();
        return m_aspectContainers;
    }

    /**
     * Returns the pointcut list for the context specified.
     *
     * @param ctx the expression context
     * @return the pointcuts for this join point
     */
    public List getPointcuts(final ExpressionContext ctx) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_pointcutManagerMap.values().iterator(); it.hasNext();) {
            PointcutManager pointcutManager = (PointcutManager)it.next();
            pointcuts.addAll(pointcutManager.getPointcuts(ctx));
        }
        return pointcuts;
    }

    /**
     * Returns the cflow pointcut list for the context specified.
     *
     * @param ctx the expression context
     * @return the pointcuts for this join point
     */
    public List getCflowPointcuts(final ExpressionContext ctx) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_pointcutManagerMap.values().iterator(); it.hasNext();) {
            PointcutManager pointcutManager = (PointcutManager)it.next();
            pointcuts.addAll(pointcutManager.getCflowPointcuts(ctx));
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
        if (name == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }
        initialize();
        if (m_pointcutManagerMap.containsKey(name)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a specific method by the class and the method hash.
     *
     * @param klass      the class housing the method
     * @param methodHash the method hash
     * @return the method tuple
     */
    public static MethodTuple getMethodTuple(final Class klass, final int methodHash) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        try {
            // create the method repository lazily
            if (!s_methods.containsKey(klass)) {
                createMethodRepository(klass);
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        MethodTuple methodTuple;
        try {
            methodTuple = (MethodTuple)((TIntObjectHashMap)s_methods.get(klass)).get(methodHash);
        } catch (Throwable e1) {
            throw new WrappedRuntimeException(e1);
        }
        return methodTuple;
    }

    /**
     * Returns a specific constructor by the class and the method hash.
     *
     * @param klass           the class housing the method
     * @param constructorHash the constructor hash
     * @return the constructor
     */
    public static ConstructorTuple getConstructorTuple(final Class klass, final int constructorHash) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        try {
            // create the constructor repository lazily
            if (!s_constructors.containsKey(klass)) {
                createConstructorRepository(klass);
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        ConstructorTuple constructorTuple;
        try {
            constructorTuple = (ConstructorTuple)((TIntObjectHashMap)s_constructors.get(klass)).get(constructorHash);
        } catch (Throwable e1) {
            throw new WrappedRuntimeException(e1);
        }
        return constructorTuple;
    }

    /**
     * Returns a specific field by the class and the field hash.
     *
     * @param klass     the class housing the method
     * @param fieldHash the method hash
     * @return the method tuple
     */
    public static Field getField(final Class klass, final int fieldHash) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        try {
            // create the fields repository lazily
            if (!s_fields.containsKey(klass)) {
                createFieldRepository(klass);
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        Field field;
        try {
            field = (Field)((TIntObjectHashMap)s_fields.get(klass)).get(fieldHash);
        } catch (Throwable e1) {
            throw new WrappedRuntimeException(e1);
        }
        return field;
    }

    /**
     * Creates a new method repository for the class specified.
     *
     * @param klass the class
     */
    protected static void createMethodRepository(final Class klass) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        Method[] methods = klass.getDeclaredMethods();
        TIntObjectHashMap methodMap = new TIntObjectHashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method wrapperMethod = methods[i];
            if (!wrapperMethod.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                Method prefixedMethod = null;
                for (int j = 0; j < methods.length; j++) {
                    Method method2 = methods[j];
                    if (method2.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                        String[] tokens = Strings.splitString(method2.getName(), TransformationUtil.DELIMITER);
                        String methodName = tokens[1];
                        Class[] parameterTypes1 = wrapperMethod.getParameterTypes();
                        Class[] parameterTypes2 = method2.getParameterTypes();
                        if (!methodName.equals(wrapperMethod.getName())) {
                            continue;
                        }
                        if (parameterTypes2.length != parameterTypes1.length) {
                            continue;
                        }
                        boolean match = true;
                        for (int k = 0; k < parameterTypes1.length; k++) {
                            if (parameterTypes1[k] != parameterTypes2[k]) {
                                match = false;
                                break;
                            }
                        }
                        if (!match) {
                            continue;
                        }
                        prefixedMethod = method2;
                        break;
                    }
                }

                // create a method tuple with 'wrapped method' and 'prefixed method'
                MethodTuple methodTuple = new MethodTuple(wrapperMethod, prefixedMethod);

                // map the tuple to the hash for the 'wrapper method'
                int methodHash = TransformationUtil.calculateHash(wrapperMethod);
                methodMap.put(methodHash, methodTuple);
            }
        }
        synchronized (s_methods) {
            s_methods.put(klass, methodMap);
        }
    }

    /**
     * Creates a new constructor repository for the class specified.
     *
     * @param klass the class
     */
    protected static void createConstructorRepository(final Class klass) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        Constructor[] constructors = klass.getDeclaredConstructors();
        TIntObjectHashMap constructorMap = new TIntObjectHashMap(constructors.length);
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor1 = constructors[i];
            Constructor prefixedConstructor = constructor1;
            Constructor wrapperConstructor = constructor1;
            for (int j = 0; j < constructors.length; j++) {
                Constructor constructor2 = constructors[j];
                Class[] parameterTypes1 = constructor1.getParameterTypes();
                Class[] parameterTypes2 = constructor2.getParameterTypes();
                if (!constructor2.getName().equals(constructor1.getName())) {
                    continue;
                }
                if (parameterTypes1.length == parameterTypes2.length) {
                    continue;
                } else if ((parameterTypes1.length < parameterTypes2.length)
                           && (parameterTypes1.length == (parameterTypes2.length - 1))) {
                    boolean match = true;
                    for (int k = 0; k < parameterTypes1.length; k++) {
                        if (parameterTypes1[k] != parameterTypes2[k]) {
                            match = false;
                            break;
                        }
                    }
                    if (parameterTypes2[parameterTypes1.length].getName().equals(TransformationUtil.JOIN_POINT_MANAGER_CLASS)) {
                        match = true;
                    }
                    if (!match) {
                        continue;
                    }
                    wrapperConstructor = constructor1;
                    prefixedConstructor = constructor2;
                    break;
                } else if ((parameterTypes2.length < parameterTypes1.length)
                           && (parameterTypes2.length == (parameterTypes1.length - 1))) {
                    boolean match = true;
                    for (int k = 0; k < parameterTypes2.length; k++) {
                        if (parameterTypes2[k] != parameterTypes1[k]) {
                            match = false;
                            break;
                        }
                    }
                    if (parameterTypes1[parameterTypes2.length].getName().equals(TransformationUtil.JOIN_POINT_MANAGER_CLASS)) {
                        match = true;
                    }
                    if (!match) {
                        continue;
                    }
                    wrapperConstructor = constructor2;
                    prefixedConstructor = constructor1;
                    break;
                }
            }

            // create a constructor tuple with 'wrapper constructor' and 'prefixed constructor'
            ConstructorTuple constructorTuple = new ConstructorTuple(wrapperConstructor, prefixedConstructor);

            // map the tuple to the hash for the 'wrapper constructor'
            int constructorHash = TransformationUtil.calculateHash(wrapperConstructor);
            constructorMap.put(constructorHash, constructorTuple);
        }
        synchronized (s_constructors) {
            s_constructors.put(klass, constructorMap);
        }
    }

    /**
     * Creates a new field repository for the class specified.
     *
     * @param klass the class
     */
    protected static void createFieldRepository(final Class klass) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        Field[] fields = klass.getDeclaredFields();
        TIntObjectHashMap fieldMap = new TIntObjectHashMap(fields.length);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            int fieldHash = TransformationUtil.calculateHash(field);
            fieldMap.put(fieldHash, field);
        }
        synchronized (s_fields) {
            s_fields.put(klass, fieldMap);
        }
    }
}

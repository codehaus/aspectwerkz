/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.aspect.DefaultIntroductionContainerStrategy;
import org.codehaus.aspectwerkz.aspect.Introduction;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.StartupManager;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.pointcut.PointcutManager;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.util.Strings;

/**
 * Stores the aspects, advices, pointcuts etc. Manages the method, advice and aspect indexing.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @TODO Use hashes, aspect=>hashcode for class advice=>hashcode for method signature
 * @TODO Store references to all join points that uses advices from a certain aspect [aspectKey=>joinPoints]
 * @TODO Map all aspects to a key, meaning have a key that maps to a data structure that contains full info about the
 * aspect and all its advice methods. [aspectKey=>aspectDataStructure].
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
     * An array with all the the Aspect prototypes in the system.
     */
    private Aspect[] m_aspects = new Aspect[0];

    /**
     * An array of all the mixins in the system, each nested class in aspect has its own index.
     */
    private Mixin[] m_mixins = new Mixin[0];

    /**
     * Holds references to the methods to the advised classes in the system.
     */
    private final Map m_methods = new HashMap();

    /**
     * Holds references to the fields to the advised classes in the system.
     */
    private final Map m_fields = new HashMap();

    /**
     * Holds references to all the the advised constructors in the system, maps the target Class to a sorted list of all
     * the advised constructors in the class.
     */
    private final Map m_constructors = new HashMap();

    /**
     * Creates a new aspect registry.
     *
     * @param uuid       the system UUID
     * @param definition the system definition
     */
    public AspectRegistry(final String uuid, final SystemDefinition definition) {
        m_uuid = uuid;
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
            StartupManager.initializeSystem(m_uuid, m_definition);
        }
    }

    /**
     * Registers a new aspect.
     *
     * @param aspect          the aspect to register
     * @param pointcutManager the pointcut manager
     */
    public void register(final Aspect aspect, final PointcutManager pointcutManager) {
        if (aspect == null) {
            throw new IllegalArgumentException("aspect can not be null");
        }
        if (pointcutManager == null) {
            throw new IllegalArgumentException("pointcut manager can not be null");
        }

        synchronized (m_aspects) {
            synchronized (m_aspectIndexes) {
                synchronized (m_adviceIndexes) {
                    synchronized (m_mixins) {
                        synchronized (m_pointcutManagerMap) {
                            try {
                                m_pointcutManagerMap.put(aspect.___AW_getName(), pointcutManager);

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
                                    Class defaultImplClass = aspect.getClass().getClassLoader().loadClass(
                                            introDef.getName()
                                    );
                                    Introduction mixin = new Introduction(
                                            introDef.getName(), defaultImplClass, aspect, introDef
                                    );
                                    // prepare the container
                                    DefaultIntroductionContainerStrategy introContainer = new DefaultIntroductionContainerStrategy(
                                            mixin, aspect.___AW_getContainer()
                                    );
                                    mixin.setContainer(introContainer);
                                    final Mixin[] tmpMixins = new Mixin[m_mixins.length + 1];
                                    java.lang.System.arraycopy(m_mixins, 0, tmpMixins, 0, m_mixins.length);
                                    tmpMixins[m_mixins.length] = mixin;
                                    m_mixins = new Mixin[m_mixins.length + 1];
                                    java.lang.System.arraycopy(tmpMixins, 0, m_mixins, 0, tmpMixins.length);
                                }
                            }
                            catch (Exception e) {
                                throw new DefinitionException(
                                        "could not register aspect [" + aspect.___AW_getName() + "] due to: " +
                                        e.toString()
                                );
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
        if (name == null) {
            throw new IllegalArgumentException("introduction name can not be null");
        }

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
        }
        else {
            initialize();
            if (m_pointcutManagerMap.containsKey(name)) {
                return (PointcutManager)m_pointcutManagerMap.get(name);
            }
            else {
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
     * Returns an array with all the aspects.
     *
     * @return the aspects
     */
    public Aspect[] getAspects() {
        initialize();
        return m_aspects;
    }

    /**
     * Returns the execution pointcut list for the class and member specified.
     *
     * @param classMetaData  the meta-data for the class
     * @param memberMetaData meta-data for the member
     * @return the pointcuts for this join point
     */
    public List getExecutionPointcuts(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_pointcutManagerMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            List executionPointcuts = aspect.getExecutionPointcuts(classMetaData, memberMetaData);
            pointcuts.addAll(executionPointcuts);
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
        for (Iterator it = m_pointcutManagerMap.values().iterator(); it.hasNext();) {
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
        for (Iterator it = m_pointcutManagerMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            pointcuts.addAll(aspect.getSetPointcuts(classMetaData, fieldMetaData));
        }
        return pointcuts;
    }

    /**
     * Returns the handler pointcut list for the class and field specified.
     *
     * @param classMetaData the meta-data for the class
     * @return the pointcuts for this join point
     */
    public List getHandlerPointcuts(final ClassMetaData classMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_pointcutManagerMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            List handlerPointcuts = aspect.getHandlerPointcuts(classMetaData);
            pointcuts.addAll(handlerPointcuts);
        }
        return pointcuts;
    }

    /**
     * Returns the call pointcut list for the class and member specified.
     *
     * @param classMetaData  the meta-data for the class
     * @param memberMetaData meta-data for the member
     * @return the pointcuts for this join point
     */
    public List getCallPointcuts(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_pointcutManagerMap.values().iterator(); it.hasNext();) {
            PointcutManager aspect = (PointcutManager)it.next();
            pointcuts.addAll(aspect.getCallPointcuts(classMetaData, memberMetaData));
        }
        return pointcuts;
    }

    /**
     * Returns the cflow pointcut list for the class and method specified.
     *
     * @param classMetaData  the meta-data for the class
     * @param methodMetaData meta-data for the method
     * @return the pointcuts for this join point
     */
    public List getCflowPointcuts(final ClassMetaData classMetaData, final MethodMetaData methodMetaData) {
        List pointcuts = new ArrayList();
        for (Iterator it = m_pointcutManagerMap.values().iterator(); it.hasNext();) {
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
        if (name == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }

        initialize();
        if (m_pointcutManagerMap.containsKey(name)) {
            return true;
        }
        else {
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
    public MethodTuple getMethodTuple(final Class klass, final int methodHash) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }

        try {
            // create the method repository lazily
            if (!m_methods.containsKey(klass)) {
                createMethodRepository(klass);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }

        MethodTuple methodTuple;
        try {
            methodTuple = (MethodTuple)((TIntObjectHashMap)m_methods.get(klass)).get(methodHash);
        }
        catch (Throwable e1) {
            initialize();
            try {
                methodTuple = (MethodTuple)((TIntObjectHashMap)m_methods.get(klass)).get(methodHash);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
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
    public ConstructorTuple getConstructorTuple(final Class klass, final int constructorHash) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }

        try {
            // create the constructor repository lazily
            if (!m_constructors.containsKey(klass)) {
                createConstructorRepository(klass);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }

        ConstructorTuple constructorTuple;
        try {
            constructorTuple = (ConstructorTuple)((TIntObjectHashMap)m_constructors.get(klass)).get(constructorHash);
        }
        catch (Throwable e1) {
            initialize();
            try {
                constructorTuple =
                (ConstructorTuple)((TIntObjectHashMap)m_constructors.get(klass)).get(constructorHash);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
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
    public Field getField(final Class klass, final int fieldHash) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }

        try {
            // create the fields repository lazily
            if (!m_fields.containsKey(klass)) {
                createFieldRepository(klass);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }

        Field field;
        try {
            field = (Field)((TIntObjectHashMap)m_fields.get(klass)).get(fieldHash);
        }
        catch (Throwable e1) {
            initialize();
            try {
                field = (Field)((TIntObjectHashMap)m_fields.get(klass)).get(fieldHash);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return field;
    }

    /**
     * Creates a new method repository for the class specified.
     *
     * @param klass the class
     */
    protected void createMethodRepository(final Class klass) {
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
                }
                else if (parameterTypes1.length < parameterTypes2.length &&
                         parameterTypes1.length == parameterTypes2.length - 1) {
                    boolean match = true;
                    for (int k = 0; k < parameterTypes1.length; k++) {
                        if (parameterTypes1[k] != parameterTypes2[k]) {
                            match = false;
                            break;
                        }
                    }
                    if (parameterTypes2[parameterTypes1.length].getName().
                            equals(TransformationUtil.JOIN_POINT_MANAGER_CLASS)) {
                        match = true;
                    }
                    if (!match) {
                        continue;
                    }
                    wrapperConstructor = constructor1;
                    prefixedConstructor = constructor2;
                    break;
                }
                else if (parameterTypes2.length < parameterTypes1.length &&
                         parameterTypes2.length == parameterTypes1.length - 1) {
                    boolean match = true;
                    for (int k = 0; k < parameterTypes2.length; k++) {
                        if (parameterTypes2[k] != parameterTypes1[k]) {
                            match = false;
                            break;
                        }
                    }
                    if (parameterTypes1[parameterTypes2.length].getName().
                            equals(TransformationUtil.JOIN_POINT_MANAGER_CLASS)) {
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

        synchronized (m_constructors) {
            m_constructors.put(klass, constructorMap);
        }
    }

    /**
     * Creates a new field repository for the class specified.
     *
     * @param klass the class
     */
    protected void createFieldRepository(final Class klass) {
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

        synchronized (m_fields) {
            m_fields.put(klass, fieldMap);
        }
    }
}

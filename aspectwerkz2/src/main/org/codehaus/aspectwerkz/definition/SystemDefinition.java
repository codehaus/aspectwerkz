/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gnu.trove.TObjectIntHashMap;
import org.codehaus.aspectwerkz.aspect.CFlowSystemAspect;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.util.SequencedHashMap;

/**
 * Abstraction of the system definition, defines the aspect system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class SystemDefinition {

    public static final String PER_JVM = "perJVM";
    public static final String PER_CLASS = "perClass";
    public static final String PER_INSTANCE = "perInstance";
    public static final String PER_THREAD = "perThread";
    public static final String THROWS_DELIMITER = "#";
    public static final String CALLER_SIDE_DELIMITER = "#";
    public static final String SYSTEM_CFLOW_ASPECT = "___AW_system_cflow";

    /**
     * Empty hash map.
     */
    public static final Map EMPTY_HASH_MAP = new HashMap();

    /**
     * Holds the indexes for the aspects. The aspect indexes are needed here (instead of in the AspectWerkz class like
     * the advice indexes) since they need to be available to the transformers before the AspectWerkz system has been
     * initialized.
     */
    private final TObjectIntHashMap m_aspectIndexes = new TObjectIntHashMap();

    /**
     * Holds the indexes for the mixins. The mixin indexes are needed here (instead of in the AspectWerkz class like the
     * advice indexes) since they need to be available to the transformers before the AspectWerkz system has been
     * initialized.
     */
    private final TObjectIntHashMap m_introductionIndexes = new TObjectIntHashMap();

    /**
     * Maps the aspects to it's name.
     */
    private final Map m_aspectMap = new SequencedHashMap();

    /**
     * Maps the mixins to it's name.
     */
    private final Map m_introductionMap = new HashMap();

    /**
     * Maps the interface mixins to it's name.
     */
    private final Map m_interfaceIntroductionMap = new HashMap();

    /**
     * The UUID for this definition.
     */
    private String m_uuid = "default";

    /**
     * The include packages.
     */
    private final Set m_includePackages = new HashSet();

    /**
     * The exclude packages.
     */
    private final Set m_excludePackages = new HashSet();

    /**
     * The prepare packages.
     */
    private final Set m_preparePackages = new HashSet();

    /**
     * The parameters passed to the aspects.
     */
    private final Map m_parametersToAspects = new HashMap();

    /**
     * Creates a new instance, creates and sets the system cflow aspect.
     */
    public SystemDefinition() {
        AspectDefinition systemAspect = new AspectDefinition(SYSTEM_CFLOW_ASPECT, CFlowSystemAspect.CLASS_NAME);
        systemAspect.setDeploymentModel(CFlowSystemAspect.DEPLOYMENT_MODEL);
        synchronized (m_aspectMap) {
            m_aspectMap.put(SYSTEM_CFLOW_ASPECT, systemAspect);
        }
    }

    /**
     * Sets the UUID for the definition.
     *
     * @param uuid the UUID
     */
    public void setUuid(final String uuid) {
        m_uuid = uuid;
    }

    /**
     * Returns the UUID for the definition.
     *
     * @return the UUID
     */
    public String getUuid() {
        return m_uuid;
    }

    /**
     * Returns the include packages.
     *
     * @return the include packages
     */
    public Set getIncludePackages() {
        return m_includePackages;
    }

    /**
     * Returns the exclude packages.
     *
     * @return the exclude packages
     */
    public Set getExcludePackages() {
        return m_excludePackages;
    }

    /**
     * Returns a collection with the aspect definitions registered.
     *
     * @return the aspect definitions
     */
    public Collection getAspectDefinitions() {
        Collection clone = new ArrayList(m_aspectMap.size());
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            clone.add(it.next());
        }
        return clone;
    }

    /**
     * Returns a collection with the introduction definitions registered.
     *
     * @return the introduction definitions
     */
    public Collection getIntroductionDefinitions() {
        Collection clone = new ArrayList(m_introductionMap.size());
        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
            clone.add(it.next());
        }
        return clone;
    }

    /**
     * Returns a collection with the advice definitions registered.
     *
     * @return the advice definitions
     */
    public Collection getAdviceDefinitions() {
        final Collection adviceDefs = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            adviceDefs.addAll(aspectDef.getAroundAdvices());
            adviceDefs.addAll(aspectDef.getBeforeAdvices());
            adviceDefs.addAll(aspectDef.getAfterAdvices());
        }
        return adviceDefs;
    }

    /**
     * Returns a specific aspect definition.
     *
     * @param name the name of the aspect definition
     * @return the aspect definition
     */
    public AspectDefinition getAspectDefinition(final String name) {
        return (AspectDefinition)m_aspectMap.get(name);
    }

    /**
     * Returns a specific advice definition.
     *
     * @param name the name of the advice definition
     * @return the advice definition
     */
    public AdviceDefinition getAdviceDefinition(final String name) {
        Collection adviceDefs = getAdviceDefinitions();
        for (Iterator it = adviceDefs.iterator(); it.hasNext();) {
            AdviceDefinition adviceDef = (AdviceDefinition)it.next();
            if (adviceDef.getName().equals(name)) {
                return adviceDef;
            }
        }
        return null;
    }

    /**
     * Returns the introduction definitions for a specific class.
     *
     * @param classMetaData the class meta-data
     * @return a list with the introduction definitions
     */
    public List getIntroductionDefinitions(final ClassMetaData classMetaData) {
        final List introDefs = new ArrayList();
        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
            IntroductionDefinition introDef = (IntroductionDefinition)it.next();
            for (int i = 0; i < introDef.getExpressions().length; i++) {
                if (introDef.getExpressions()[i].match(classMetaData, PointcutType.CLASS)) {
                    introDefs.add(introDef);
                }
            }
        }
        return introDefs;
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param aspectName the name of the aspect
     * @return the index
     */
    public int getAspectIndexByName(final String aspectName) {
        if (aspectName == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }
        int index = m_aspectIndexes.get(aspectName);
        if (index < 1) {
            throw new RuntimeException(
                    "aspect [" + aspectName + "] does not exist, failed in retrieving aspect index"
            );
        }
        return index;
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param mixinName the name of the mixin
     * @return the index
     */
    public int getMixinIndexByName(final String mixinName) {
        if (mixinName == null) {
            throw new IllegalArgumentException("mixin name can not be null");
        }
        int index = m_introductionIndexes.get(mixinName);
        if (index < 1) {
            throw new RuntimeException("mixin [" + mixinName + "] does not exist, failed in retrieving mixin index");
        }
        return index;
    }

    /**
     * Adds a new aspect definition.
     *
     * @param aspectDef the aspect definition
     */
    public void addAspect(final AspectDefinition aspectDef) {
        if (aspectDef == null) {
            throw new IllegalArgumentException("aspect definition can not be null");
        }
        if (m_aspectIndexes.containsKey(aspectDef.getName())) {
            return;
        }
        synchronized (m_aspectMap) {
            synchronized (m_aspectIndexes) {
                final int index = m_aspectMap.values().size() + 1;
                m_aspectIndexes.put(aspectDef.getName(), index);
                m_aspectMap.put(aspectDef.getName(), aspectDef);
            }
        }
    }

    /**
     * Adds a new mixin definition.
     *
     * @param introDef the mixin definition
     */
    public void addIntroductionDefinition(final IntroductionDefinition introDef) {
        if (introDef == null) {
            throw new IllegalArgumentException("introduction definition can not be null");
        }
        if (m_introductionIndexes.containsKey(introDef.getName())) {
            IntroductionDefinition def = (IntroductionDefinition)m_introductionMap.get(introDef.getName());
            def.addExpressions(introDef.getExpressions());
            //if (true) throw new RuntimeException("warning here - doublon in name");
            return;
        }
        synchronized (m_introductionMap) {
            synchronized (m_introductionIndexes) {
                final int index = m_introductionMap.values().size() + 1;
                m_introductionIndexes.put(introDef.getName(), index);
                m_introductionMap.put(introDef.getName(), introDef);
            }
        }
    }

    /**
     * Adds a new pure interface mixin definition.
     *
     * @param introDef the mixin definition
     */
    public void addInterfaceIntroductionDefinition(final InterfaceIntroductionDefinition introDef) {
        if (introDef == null) {
            throw new IllegalArgumentException("introduction definition can not be null");
        }
        synchronized (m_interfaceIntroductionMap) {
            m_interfaceIntroductionMap.put(introDef.getName(), introDef);
        }
    }

    /**
     * Adds a new include package.
     *
     * @param includePackage the new include package
     */
    public void addIncludePackage(final String includePackage) {
        synchronized (m_includePackages) {
            m_includePackages.add(includePackage + '.');
        }
    }

    /**
     * Adds a new exclude package.
     *
     * @param excludePackage the new exclude package
     */
    public void addExcludePackage(final String excludePackage) {
        synchronized (m_excludePackages) {
            m_excludePackages.add(excludePackage + '.');
        }
    }

    /**
     * Adds a new prepare package.
     *
     * @param preparePackage the new prepare package
     */
    public void addPreparePackage(final String preparePackage) {
        synchronized (m_preparePackages) {
            m_preparePackages.add(preparePackage + '.');
        }
    }

    /**
     * Checks if there exists an advice with the name specified.
     *
     * @param name the name of the advice
     * @return boolean
     */
    public boolean hasAdvice(final String name) {
        Collection adviceDefs = getAdviceDefinitions();
        for (Iterator it = adviceDefs.iterator(); it.hasNext();) {
            AdviceDefinition adviceDef = (AdviceDefinition)it.next();
            if (adviceDef.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there exists an introduction with the name specified.
     *
     * @param name the name of the introduction
     * @return boolean
     */
    public boolean hasIntroduction(final String name) {
        return m_introductionMap.containsKey(name);
    }

    /**
     * Checks if a class should be included.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean inIncludePackage(final String className) {
        if (className == null) {
            throw new IllegalArgumentException("class name can not be null");
        }
        if (m_includePackages.isEmpty()) {
            return true;
        }
        for (Iterator it = m_includePackages.iterator(); it.hasNext();) {
            String packageName = (String)it.next();
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class should be excluded.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean inExcludePackage(final String className) {
        if (className == null) {
            throw new IllegalArgumentException("class name can not be null");
        }
        for (Iterator it = m_excludePackages.iterator(); it.hasNext();) {
            String packageName = (String)it.next();
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class is in prepare declaration
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean inPreparePackage(String className) {
        if (className == null) {
            throw new IllegalArgumentException("class name can not be null");
        }
        for (Iterator it = m_preparePackages.iterator(); it.hasNext();) {
            String packageName = (String)it.next();
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class has a <tt>Mixin</tt>.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasIntroductions(final ClassMetaData classMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }

        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
            IntroductionDefinition introDef = (IntroductionDefinition)it.next();
            for (int i = 0; i < introDef.getExpressions().length; i++) {
                Expression expression = introDef.getExpressions()[i];
                if (expression.isOfType(PointcutType.CLASS)
                    && expression.match(classMetaData, PointcutType.CLASS)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has an execution pointcut. Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasExecutionPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();
                if (expression.isOfType(PointcutType.EXECUTION)
                    && expression.match(classMetaData, PointcutType.EXECUTION)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has a execution pointcut.
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the member meta-data
     * @return boolean
     */
    public boolean hasExecutionPointcut(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (memberMetaData == null) {
            throw new IllegalArgumentException("member meta-data can not be null");
        }

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();
                if (expression.isOfType(PointcutType.EXECUTION)
                    && expression.match(classMetaData, memberMetaData, PointcutType.EXECUTION)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a get pointcut. Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasGetPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();
                if (expression.isOfType(PointcutType.GET)
                    && expression.match(classMetaData, PointcutType.GET)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a get pointcut.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasGetPointcut(
            final ClassMetaData classMetaData,
            final FieldMetaData fieldMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (fieldMetaData == null) {
            throw new IllegalArgumentException("field meta-data can not be null");
        }

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();
                if (expression.isOfType(PointcutType.GET)
                    && expression.match(classMetaData, fieldMetaData, PointcutType.GET)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a set pointcut. Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasSetPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();
                if (expression.isOfType(PointcutType.SET)
                    && expression.match(classMetaData, PointcutType.SET)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a set pointcut.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasSetPointcut(
            final ClassMetaData classMetaData,
            final FieldMetaData fieldMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (fieldMetaData == null) {
            throw new IllegalArgumentException("field meta-data can not be null");
        }

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();
                if (expression.isOfType(PointcutType.SET)
                    && expression.match(classMetaData, fieldMetaData, PointcutType.SET)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a handler pointcut.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     * @TODO: needs to be implemented when/if handler pointcuts supports target class filtering
     */
    public boolean hasHandlerPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        return true;
//        return false;//TODO AVAOSD: FIX FOR AOSD
    }

    /**
     * Checks if a class has a handler pointcut.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     * @TODO: needs to USE the class and method metadata for filtering
     */
    public boolean hasHandlerPointcut(
            final ClassMetaData classMetaData,
            final MethodMetaData methodMetaData,
            final ClassMetaData exceptionMetaData) {
        if (exceptionMetaData == null) {
            throw new IllegalArgumentException("exception meta-data can not be null");
        }

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();
                if (expression.isOfType(PointcutType.HANDLER)
                    && expression.match(exceptionMetaData, PointcutType.HANDLER)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class should care about advising caller side method invocations. This method matches the caller class
     * (when the isCallerSideMethod matches the callee class)
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasCallPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();
                if (expression.isOfType(PointcutType.CALL)
                    && expression.match(classMetaData, PointcutType.CALL)) {
                    return true;
                }
//                if (expression.isOfType(PointcutType.CFLOW)
//                    && expression.match(classMetaData, PointcutType.CFLOW)) {
//                    return true;
//                }
                if (expression.matchInOrNotIn(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method is a defined as a caller side method. This method matches the callee class (when the
     * hasCallerSideMethod matches the caller class)
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the member meta-data
     * @return boolean
     */
    public boolean isPickedOutByCallPointcut(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }
        if (memberMetaData == null) {
            throw new IllegalArgumentException("method meta-data can not be null");
        }

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();
                if (expression.isOfType(PointcutType.CALL)
                    && expression.match(classMetaData, memberMetaData, PointcutType.CALL)) {
                    return true;
                }
//                if (expression.isOfType(PointcutType.CFLOW)) {
//                    if (expression.match(classMetaData, memberMetaData, PointcutType.CFLOW)) {
//                    return true;
//                    }
//                }
                if (expression.matchInOrNotIn(classMetaData, memberMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the interface introductions for a certain class merged with the implementation based introductions as
     * well
     *
     * @param classMetaData the class meta-data
     * @return the names
     */
    public List getInterfaceIntroductions(final ClassMetaData classMetaData) {
        if (classMetaData == null) {
            throw new IllegalArgumentException("class meta-data can not be null");
        }

        List interfaceIntroductionDefs = new ArrayList();
        for (Iterator it = m_interfaceIntroductionMap.values().iterator(); it.hasNext();) {
            InterfaceIntroductionDefinition introDef = (InterfaceIntroductionDefinition)it.next();
            for (int i = 0; i < introDef.getExpressions().length; i++) {
                Expression expression = introDef.getExpressions()[i];
                if (expression.isOfType(PointcutType.CLASS) && expression.match(classMetaData, PointcutType.CLASS)) {
                    interfaceIntroductionDefs.add(introDef);
                }
            }
        }
        // add introduction definitions as well
        interfaceIntroductionDefs.addAll(getIntroductionDefinitions(classMetaData));
        return interfaceIntroductionDefs;
    }

    /**
     * Adds a new parameter for the aspect.
     * <p/>
     * TODO: should perhaps move to the aspect def instead of being separated from the aspect def concept?
     *
     * @param aspectName the name of the aspect
     * @param key        the key
     * @param value      the value
     */
    public void addParameter(final String aspectName, final String key, final String value) {
        Map parameters;
        if (m_parametersToAspects.containsKey(aspectName)) {
            parameters = (Map)m_parametersToAspects.get(aspectName);
            parameters.put(key, value);
        }
        else {
            parameters = new HashMap();
            parameters.put(key, value);
            m_parametersToAspects.put(aspectName, parameters);
        }
    }

    /**
     * Returns parameters for the aspect.
     *
     * @param aspectName the name of the aspect
     * @return parameters
     */
    public Map getParameters(final String aspectName) {
        if (m_parametersToAspects.containsKey(aspectName)) {
            return (Map)m_parametersToAspects.get(aspectName);
        }
        else {
            return EMPTY_HASH_MAP;
        }
    }
}


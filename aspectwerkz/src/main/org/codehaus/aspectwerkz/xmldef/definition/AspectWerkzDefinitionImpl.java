/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import gnu.trove.TObjectIntHashMap;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.util.SequencedHashMap;

/**
 * Implementation of the AspectWerkz interface for the xmldef definition model.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectWerkzDefinitionImpl implements AspectWerkzDefinition {

    /**
     * Holds the indexes for the introductions. The introduction indexes are needed here
     * (instead of in the AspectWerkz class like the advice indexes) since they need to
     * be available to the transformers before the AspectWerkz system has been initialized.
     */
    private final TObjectIntHashMap m_introductionIndexes = new TObjectIntHashMap();

    /**
     * Set with all the class names of the aspects to use.
     */
    private final Set m_aspectsToUse = new HashSet();

    /**
     * Maps the introductions to it's name.
     */
    private final Map m_introductionMap = new SequencedHashMap();

    /**
     * Maps the advices to it's name.
     */
    private final Map m_adviceMap = new HashMap();

    /**
     * Maps the aspects to it's name.
     */
    private final Map m_aspectMap = new SequencedHashMap();

    /**
     * The abstract advice definitions.
     */
    private final Map m_abstractAdviceMap = new HashMap();

    /**
     * Maps the advice stacks to it's name.
     */
    private final Map m_adviceStackMap = new HashMap();

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
     * Creates a new instance, creates and sets the system aspect.
     */
    public AspectWerkzDefinitionImpl() {
        AspectDefinition systemAspect = new AspectDefinition();
        systemAspect.setName(SYSTEM_ASPECT);
        synchronized (m_aspectMap) {
            m_aspectMap.put(SYSTEM_ASPECT, systemAspect);
        }
    }

    /**
     * Checks if the definition is of type attribute definition.
     *
     * @return returns false for this definition
     */
    public boolean isAttribDef() {
        return false;
    }

    /**
     * Checks if the definition is of type XML definition.
     *
     * @return returns true for this definition
     */
    public boolean isXmlDef() {
        return true;
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
     * Returns a collection with the abstract aspect definitions registered.
     *
     * @return the abstract aspect definitions
     */
    public Collection getAbstractAspectDefinitions() {
        return m_abstractAdviceMap.values();
    }

    /**
     * Returns a collection with the aspect definitions registered.
     *
     * @return the aspect definitions
     */
    public Collection getAspectDefinitions() {
        return m_aspectMap.values();
    }

    /**
     * Returns a collection with the introduction definitions registered.
     *
     * @return the introduction definitions
     */
    public Collection getIntroductionDefinitions() {
        return m_introductionMap.values();
    }

    /**
     * Returns a collection with the advice definitions registered.
     *
     * @return the advice definitions
     */
    public Collection getAdviceDefinitions() {
        return m_adviceMap.values();
    }

    /**
     * Finds an advice stack definition by its name.
     *
     * @param adviceStackName the advice stack name
     * @return the definition
     */
    public AdviceStackDefinition getAdviceStackDefinition(final String adviceStackName) {
        return (AdviceStackDefinition)m_adviceStackMap.get(adviceStackName);
    }

    /**
     * Returns a specific abstract aspect definition.
     *
     * @param name the name of the abstract aspect definition
     * @return the abstract aspect definition
     */
    public AspectDefinition getAbstractAspectDefinition(final String name) {
        return (AspectDefinition)m_abstractAdviceMap.get(name);
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
     * Returns the names of the target classes.
     *
     * @return the names of the target classes
     */
    public String[] getAspectTargetClassNames() {
        String[] classNames = new String[m_aspectMap.keySet().size()];
        int i = 0;
        for (Iterator it = m_aspectMap.keySet().iterator(); it.hasNext();) {
            String key = (String)it.next();
            classNames[i++] = key;
        }
        return classNames;
    }

    /**
     * Returns a specific advice definition.
     *
     * @param name the name of the advice definition
     * @return the advice definition
     */
    public AdviceDefinition getAdviceDefinition(final String name) {
        for (Iterator it = m_adviceMap.values().iterator(); it.hasNext();) {
            AdviceDefinition adviceDefinition = (AdviceDefinition)it.next();
            if (adviceDefinition.getName().equals(name)) {
                return adviceDefinition;
            }
        }
        return null;
    }

    /**
     * Finds the name of an advice by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the advice
     */
    public String getAdviceNameByAttribute(final String attribute) {
        if (attribute == null) return null;
        for (Iterator it = m_adviceMap.values().iterator(); it.hasNext();) {
            AdviceDefinition adviceDefinition = (AdviceDefinition)it.next();
            if (adviceDefinition.getAttribute().equals(attribute)) {
                return adviceDefinition.getName();
            }
        }
        return null;
    }

    /**
     * Finds the name of an introduction by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the introduction
     */
    public String getIntroductionNameByAttribute(final String attribute) {
        if (attribute == null) {
            return null;
        }
        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
            IntroductionDefinition introductionDefinition = (IntroductionDefinition)it.next();
            if (introductionDefinition.getAttribute().equals(attribute)) {
                return introductionDefinition.getName();
            }
        }
        return null;
    }

//    /**
//     * ALEX
//     * Returns the interface introductions for a certain class.
//     *
//     * @param classMetaData the class meta-data
//     * @return the names
//     */
//    public List getInterfaceIntroductions(final ClassMetaData classMetaData) {
//        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
//
//        List introductionDefs = new ArrayList();
//        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
//            IntroductionDefinition introDef = (IntroductionDefinition)it.next();
//            for (Iterator it2 = introDef.getInterfaceIntroductions().iterator(); it2.hasNext();) {
//                InterfaceIntroductionDefinition intfIntroDef = (InterfaceIntroductionDefinition)it2.next();
//                if (intfIntroDef.getExpression().matchClassPointcut(classMetaData)) {
//                    introductionDefs.add(intfIntroDef);
//                }
//            }
//        }
//        return introductionDefs;
//    }

    /**
     * Returns the name of the interface for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionInterfaceName(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        if (!m_introductionMap.containsKey(introductionName)) {
            return null;
        }
        return ((IntroductionDefinition)m_introductionMap.get(introductionName)).getInterface();
    }

    /**
     * Returns the name of the implementation for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionImplName(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        if (!m_introductionMap.containsKey(introductionName)) {
            return null;
        }
        return ((IntroductionDefinition)m_introductionMap.get(introductionName)).getImplementation();
    }

    /**
     * Returns a specific introduction definition.
     *
     * @param introductionName the name of the introduction
     * @return the introduction definition
     */
    public IntroductionDefinition getIntroductionDefinition(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return (IntroductionDefinition)m_introductionMap.get(introductionName);
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return the index
     */
    public int getIntroductionIndex(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return m_introductionIndexes.get(introductionName);
    }

    /**
     * Returns the indexes for the introductions.
     *
     * @return the indexes
     */
    public TObjectIntHashMap getIntroductionIndexes() {
        return m_introductionIndexes;
    }

    /**
     * Returns the class name for the join point controller, if there is a match.
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return the controller class name
     */
    public String getJoinPointController(final ClassMetaData classMetaData,
                                         final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            Collection controllerDefs = aspectDef.getControllerDefs();
            for (Iterator it2 = controllerDefs.iterator(); it2.hasNext();) {
                ControllerDefinition controllerDef = (ControllerDefinition)it2.next();
                if (controllerDef.getExpression().match(classMetaData, methodMetaData)) {
                    return controllerDef.getClassName();
                }
            }
        }
        return null;
    }

    /**
     * Returns a set with the aspects to use.
     *
     * @return the aspects to use
     */
    public Set getAspectsToUse() {
        return m_aspectsToUse;
    }

    /**
     * Adds a new aspect to use.
     *
     * @param className the class name of the aspect
     */
    public void addAspectToUse(final String className) {
        m_aspectsToUse.add(className);
    }

    /**
     * Adds a new include package.
     *
     * @param includePackage the new include package
     */
    public void addIncludePackage(final String includePackage) {
        synchronized (m_includePackages) {
            m_includePackages.add(includePackage+".");
        }
    }

    /**
     * Adds a new exclude package.
     *
     * @param excludePackage the new exclude package
     */
    public void addExcludePackage(final String excludePackage) {
        synchronized (m_excludePackages) {
            m_excludePackages.add(excludePackage+".");
        }
    }

    /**
     * Adds an abstract aspect definition.
     *
     * @param aspect a new abstract aspect definition
     */
    public void addAbstractAspect(final AspectDefinition aspect) {
        synchronized (m_abstractAdviceMap) {
            m_abstractAdviceMap.put(aspect.getName(), aspect);
        }
    }

    /**
     * Adds an aspect definition.
     *
     * @param aspect a new aspect definition
     */
    public void addAspect(final AspectDefinition aspect) {
        synchronized (m_aspectMap) {
            m_aspectMap.put(aspect.getName(), aspect);
        }
    }

    /**
     * Adds an advice stack definition.
     *
     * @param adviceStackDef the advice stack definition
     */
    public void addAdviceStack(final AdviceStackDefinition adviceStackDef) {
        synchronized (m_adviceStackMap) {
            m_adviceStackMap.put(adviceStackDef.getName(), adviceStackDef);
        }
    }

    /**
     * Adds an advice definition.
     *
     * @param advice the advice definition
     */
    public void addAdvice(final AdviceDefinition advice) {
        synchronized (m_adviceMap) {
            m_adviceMap.put(advice.getName(), advice);
        }
    }

    /**
     * Adds a new introductions definition.
     *
     * @param introduction the introduction definition
     */
    public void addIntroduction(final IntroductionDefinition introduction) {
        if (m_introductionIndexes.containsKey(introduction.getName())) {
            return;
        }
        synchronized (m_introductionMap) {
            synchronized (m_introductionIndexes) {
                final int index = m_introductionMap.values().size() + 1;
                m_introductionIndexes.put(introduction.getName(), index);
                m_introductionMap.put(introduction.getName(), introduction);
            }
        }
    }

    /**
     * Checks if there exists an advice with the name specified.
     *
     * @param name the name of the advice
     * @return boolean
     */
    public boolean hasAdvice(final String name) {
        return m_adviceMap.containsKey(name);
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
     * Checks if a class has an <tt>AspectMetaData</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean inIncludePackage(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
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
     * Checks if a class has an <tt>AspectMetaData</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean inExcludePackage(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        for (Iterator it = m_excludePackages.iterator(); it.hasNext();) {
            String packageName = (String)it.next();
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class has an <tt>Mixin</tt>.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasIntroductions(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it1 = m_aspectMap.values().iterator(); it1.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it1.next();
            List bindAdviceRules = aspectDef.getBindIntroductionRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindIntroductionRule rule = (BindIntroductionRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.CLASS)
                        && expression.match(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has an execution pointcut.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasExecutionPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            if (aspectDef.isAbstract()) {
                continue;
            }
            List bindAdviceRules = aspectDef.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule rule = (BindAdviceRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.EXECUTION)
                        && expression.match(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has an execution pointcut.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return boolean
     */
    public boolean hasExecutionPointcut(final ClassMetaData classMetaData,
                                        final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List bindAdviceRules = aspectDef.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule rule = (BindAdviceRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.EXECUTION)
                        && expression.match(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a get pointcut.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasGetPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List bindAdviceRules = aspectDef.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule rule = (BindAdviceRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.GET)
                        && expression.match(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a get pointcut.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasGetPointcut(final ClassMetaData classMetaData,
                                  final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List bindAdviceRules = aspectDef.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule rule = (BindAdviceRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.GET)
                        && expression.match(classMetaData, fieldMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a set pointcut.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasSetPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List bindAdviceRules = aspectDef.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule rule = (BindAdviceRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.SET)
                        && expression.match(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a set pointcut.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasSetPointcut(final ClassMetaData classMetaData,
                                  final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List bindAdviceRules = aspectDef.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule rule = (BindAdviceRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.SET)
                        && expression.match(classMetaData, fieldMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a throws pointcut.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasThrowsPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List bindAdviceRules = aspectDef.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule rule = (BindAdviceRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.THROWS)
                        && expression.match(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a throws pointcut.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean hasThrowsPointcut(final ClassMetaData classMetaData,
                                     final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List bindAdviceRules = aspectDef.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule rule = (BindAdviceRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.THROWS) &&
                        expression.match(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class is invoking a method that is picked out by a call pointcut.
     *
     * @TODO: implement, now it filters nothing
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasCallPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
//        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
//            AspectDefinition aspectDef = (AspectDefinition)it.next();
//
//            Collection pointcuts = aspectDef.getPointcutDefs();
//            for (Iterator it2 = pointcuts.iterator(); it2.hasNext();) {
//                PointcutDefinition pointcutDefinition = (PointcutDefinition)it2.next();
//                if ((pointcutDefinition.getType().equalsIgnoreCase(PointcutDefinition.CALLER_SIDE) ||
//                        pointcutDefinition.getType().equalsIgnoreCase(PointcutDefinition.CFLOW)) &&
//                        pointcutDefinition.getRegexpCallerClassPattern().matches(classMetaData.getName())) {
//                    return true;
//                }
//            }
//
//        }
//        return false;

        return true;
    }

    /**
     * Checks if a method is a picked out by a call pointcut.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean isPickedOutByCallPointcut(final ClassMetaData classMetaData,
                                             final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List bindAdviceRules = aspectDef.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule rule = (BindAdviceRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.CALL)
                        && expression.match(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the names of the introductions for a certain class.
     *
     * @param classMetaData the class meta-data
     * @return the names
     */
    public List getIntroductionNames(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        List introductionNames = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            List bindIntroductionRules = aspectDef.getBindIntroductionRules();
            for (Iterator it2 = bindIntroductionRules.iterator(); it2.hasNext();) {
                BindIntroductionRule rule = (BindIntroductionRule)it2.next();
                Expression expression = rule.getExpression();
                if (expression.getType().equals(PointcutType.CLASS)
                        && expression.match(classMetaData)) {
                    introductionNames.addAll(rule.getIntroductionRefs());
                }
            }
        }
        return introductionNames;
    }

    /**
     * Builds up a meta-data repository for the mixins.
     *
     * @param repository the repository
     * @param loader the class loader to use
     */
    public void buildMixinMetaDataRepository(final Set repository, final ClassLoader loader) {
        for (Iterator it = getIntroductionDefinitions().iterator(); it.hasNext();) {
            String className = ((IntroductionDefinition)it.next()).getImplementation();
            if (className != null) {
                try {
                    Class mixin = loader.loadClass(className);
                    ClassMetaData metaData = ReflectionMetaDataMaker.createClassMetaData(mixin);
                    repository.add(metaData);
                }
                catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Loads the aspects.
     *
     * @param loader the class loader to use to load the aspects
     */
    public void loadAspects(final ClassLoader loader) {
        // not needed for this definition implementation
    }
}



/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

import gnu.trove.TObjectIntHashMap;

import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.attribdef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.AspectAttributeParser;
import org.codehaus.aspectwerkz.attribdef.definition.attribute.DefaultAspectAttributeParser;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Implementation of the AspectWerkz interface for the attribdef definition model.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectWerkzDefinitionImpl implements AspectWerkzDefinition {

    /**
     * Holds the indexes for the aspects. The aspect indexes are needed here (instead of in the
     * AspectWerkz class like the advice indexes) since they need to be available to the
     * transformers before the AspectWerkz system has been initialized.
     */
    private final TObjectIntHashMap m_aspectIndexes = new TObjectIntHashMap();

    /**
     * Set with the aspect class names.
     */
    private Set m_aspectsToUse = new HashSet();

    /**
     * Maps the aspects to it's name.
     */
    private final Map m_aspectMap = new SequencedHashMap();

    /**
     * The UUID for this definition.
     */
    private String m_uuid = "default";

    /**
     * The transformation scopes.
     */
    private final Set m_transformationScopeSet = new HashSet();

    /**
     * The default attribute parser.
     * @TODO: make customizable (-D..)
     * @TODO: use factory
     */
    private final AspectAttributeParser m_attributeParser = new DefaultAspectAttributeParser();

    /**
     * Marks the definition as initialized.
     */
    private boolean m_aspectsLoaded = false;

    /**
     * Creates a new instance, creates and sets the system aspect.
     */
    public AspectWerkzDefinitionImpl() {
//        AspectDefinition systemAspect = new AspectDefinition();
//        systemAspect.setName(SYSTEM_ASPECT);
//        synchronized (m_aspectMap) {
//            m_aspectMap.put(SYSTEM_ASPECT, systemAspect);
//        }
    }

    /**
     * Loads the aspects.
     *
     * @param loader the class loader to use to load the aspects
     */
    public void loadAspects(final ClassLoader loader) {
        if (m_aspectsLoaded) return;
        m_aspectsLoaded = true;
        for (Iterator it = getAspectsToUse().iterator(); it.hasNext();) {
            loadAspect((String)it.next(), loader);
        }
    }

    /**
     * Checks if the definition is of type attribute definition.
     *
     * @return returns true for this definition
     */
    public boolean isAttribDef() {
        return true;
    }

    /**
     * Checks if the definition is of type XML definition.
     *
     * @return returns false for this definition
     */
    public boolean isXmlDef() {
        return false;
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
     * Returns the transformation scopes.
     *
     * @return the transformation scopes
     */
    public Set getTransformationScopes() {
        return m_transformationScopeSet;
    }

    /**
     * Returns a collection with the aspect definitions registered.
     *
     * @return the aspect definitions
     */
    public Collection getAspectDefinitions() {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
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
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        final Collection introductionDefs = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            introductionDefs.addAll(aspectDef.getMethodIntroductions());
        }
        return introductionDefs;
    }

    /**
     * Returns a collection with the advice definitions registered.
     *
     * @return the advice definitions
     */
    public Collection getAdviceDefinitions() {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
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
     * @TODO: needed??
     *
     * @param name the name of the aspect definition
     * @return the aspect definition
     */
    public AspectDefinition getAspectDefinition(final String name) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        return (AspectDefinition)m_aspectMap.get(name);
    }

    /**
     * Returns a specific advice definition.
     *
     * @TODO: needed??
     *
     * @param name the name of the advice definition
     * @return the advice definition
     */
    public AdviceDefinition getAdviceDefinition(final String name) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
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
     * Returns the name of the implementation for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionImplName(final String introductionName) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            List introductions = aspectDef.getMethodIntroductions();
            for (Iterator it2 = introductions.iterator(); it2.hasNext();) {
                MethodIntroductionDefinition introDef = (MethodIntroductionDefinition)it2.next();
                if (introDef.getName().equals(introductionName)) {
                    return introDef.getAspectClassName();
                }
            }
        }
        return null;
    }

    /**
     * Returns a specific introduction definition.
     *
     * @TODO: needed??
     *
     * @param introductionName the name of the introduction
     * @return the introduction definition
     */
    public MethodIntroductionDefinition getIntroductionDefinition(final String introductionName) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            List introductions = aspectDef.getMethodIntroductions();
            for (Iterator it2 = introductions.iterator(); it2.hasNext();) {
                MethodIntroductionDefinition introDef = (MethodIntroductionDefinition)it2.next();
                if (introDef.getName().equals(introductionName)) {
                    return introDef;
                }
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
    public List getIntroductionDefinitionsForClass(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        final List introDefs = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getMethodIntroductions().iterator(); it2.hasNext();) {
                MethodIntroductionDefinition introDef = (MethodIntroductionDefinition)it2.next();
                if (introDef.getWeavingRule().matchClassPointcut(classMetaData)) {
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
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (aspectName == null) throw new IllegalArgumentException("aspect name can not be null");
        int index = m_aspectIndexes.get(aspectName);
        if (index < 1) throw new RuntimeException("aspect [" + aspectName + "] does not exist, failed in retrieving aspect index");
        return index;
    }

    /**
     * Returns the indexes for the introductions.
     *
     * @TODO: needed??
     *
     * @return the indexes
     */
    public TObjectIntHashMap getAspectIndexes() {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        return m_aspectIndexes;
    }

    /**
     * Returns the class name for the join point controller, if there is a match.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return the controller class name
     */
    public String getJoinPointController(final ClassMetaData classMetaData,
                                         final MethodMetaData methodMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            Collection controllerDefs = aspectDef.getControllers();
            for (Iterator it2 = controllerDefs.iterator(); it2.hasNext();) {
                ControllerDefinition controllerDef = (ControllerDefinition)it2.next();
                if (controllerDef.matchMethodPointcut(classMetaData, methodMetaData)) {
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
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        return m_aspectsToUse;
    }

    /**
     * Adds a new aspect definition.
     *
     * @param aspectDef the aspect definition
     */
    public void addAspect(final AspectDefinition aspectDef) {
        if (aspectDef == null) throw new IllegalArgumentException("aspect definition can not be null");
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
     * Adds a new aspect to use.
     *
     * @param className the class name of the aspect
     */
    public void addAspectToUse(final String className) {
        synchronized (m_aspectsToUse) {
            m_aspectsToUse.add(className);
        }
    }

    /**
     * Adds a new transformation scope.
     *
     * @param transformationScope the new scope
     */
    public void addTransformationScope(final String transformationScope) {
        synchronized (m_transformationScopeSet) {
            m_transformationScopeSet.add(transformationScope);
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
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (m_aspectMap.containsKey(name)) {
            AspectDefinition aspectDef = (AspectDefinition)m_aspectMap.get(name);
            if (!aspectDef.getInterfaceIntroductions().isEmpty() ||
                    (!aspectDef.getMethodIntroductions().isEmpty())) {
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
    public boolean inTransformationScope(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (m_transformationScopeSet.isEmpty()) {
            return true;
        }
        for (Iterator it = m_transformationScopeSet.iterator(); it.hasNext();) {
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
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getMethodIntroductions().iterator(); it2.hasNext();) {
                MethodIntroductionDefinition introDef = (MethodIntroductionDefinition)it2.next();

                IntroductionWeavingRule weavingRule = introDef.getWeavingRule();
                if (weavingRule.matchClassPointcut(classMetaData)) {
                    return true;
                }
            }
            for (Iterator it2 = aspectDef.getInterfaceIntroductions().iterator(); it2.hasNext();) {
                InterfaceIntroductionDefinition introDef = (InterfaceIntroductionDefinition)it2.next();
                if (introDef.getWeavingRule().matchClassPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasMethodPointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchMethodPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return boolean
     */
    public boolean hasMethodPointcut(final ClassMetaData classMetaData,
                                     final MethodMetaData methodMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchMethodPointcut(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a <tt>GetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasGetFieldPointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchGetFieldPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasGetFieldPointcut(final ClassMetaData classMetaData,
                                       final FieldMetaData fieldMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchGetFieldPointcut(classMetaData, fieldMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a <tt>SetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasSetFieldPointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchSetFieldPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasSetFieldPointcut(final ClassMetaData classMetaData,
                                       final FieldMetaData fieldMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchSetFieldPointcut(classMetaData, fieldMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasThrowsPointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchThrowsPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean hasThrowsPointcut(final ClassMetaData classMetaData,
                                     final MethodMetaData methodMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchThrowsPointcut(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class should care about advising caller side method invocations.
     * This method matches the caller class (when the isCallerSideMethod matches the callee class)
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasCallerSidePointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchCallerSidePointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method is a defined as a caller side method.
     * This method matches the callee class (when the hasCallerSideMethod matches the caller class)
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean isCallerSideMethod(final ClassMetaData classMetaData,
                                      final MethodMetaData methodMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchCallerSidePointcut(
                        classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the interface introductions for a certain class.
     *
     * @param classMetaData the class meta-data
     * @return the names
     */
    public List getInterfaceIntroductions(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        List introductionDefs = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getInterfaceIntroductions().iterator(); it2.hasNext();) {
                InterfaceIntroductionDefinition introDef = (InterfaceIntroductionDefinition)it2.next();
                if (introDef.getWeavingRule().matchClassPointcut(classMetaData)) {
                    introductionDefs.add(introDef);
                }
            }
        }
        return introductionDefs;
    }

    /**
     * Builds up a meta-data repository for the mixins.
     *
     * @param repository the repository
     * @param loader the class loader to use
     */
    public void buildMixinMetaDataRepository(final Set repository, final ClassLoader loader) {
        loadAspects(loader);

        Set definedAspects = getAspectsToUse();
        for (Iterator it = definedAspects.iterator(); it.hasNext();) {
            String className = (String)it.next();
            try {
                Class mixin = loader.loadClass(className);
                ClassMetaData metaData = ReflectionMetaDataMaker.createClassMetaData(mixin);
                repository.add(metaData);
            }
            catch (ClassNotFoundException e) {
                ;// ignore
            }
        }
    }

    /**
     * Loads and parser the aspect.
     *
     * @param aspectClassName the class name of the aspect
     * @param loader the class loader to use
     */
    private void loadAspect(final String aspectClassName, final ClassLoader loader) {
        try {
            Class klass = loader.loadClass(aspectClassName);
            AspectDefinition aspectDef = m_attributeParser.parse(klass);
            addAspect(aspectDef);
        }
        catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}


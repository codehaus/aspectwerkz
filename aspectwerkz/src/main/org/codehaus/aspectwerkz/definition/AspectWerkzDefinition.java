/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.definition;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.Serializable;
import java.net.URL;

import gnu.trove.TObjectIntHashMap;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.ContextClassLoader;

/**
 * Implements the <code>AspectWerkz</code> definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: AspectWerkzDefinition.java,v 1.17 2003-07-19 20:36:15 jboner Exp $
 */
public class AspectWerkzDefinition implements Serializable {

    public static final String PER_JVM = "perJVM";
    public static final String PER_CLASS = "perClass";
    public static final String PER_INSTANCE = "perInstance";
    public static final String PER_THREAD = "perThread";
    public static final String THROWS_DELIMITER = "#";
    public static final String CALLER_SIDE_DELIMITER = "#";

    /**
     * The path to the definition file.
     */
    public static final String DEFINITION_FILE =
            System.getProperty("aspectwerkz.definition.file", null);

    /**
     * Default name for the definition file.
     */
    public static final String DEFAULT_DEFINITION_FILE_NAME = "aspectwerkz.xml";

    /**
     * Name of the system aspect.
     */
    public static final String SYSTEM_ASPECT = "org/codehaus/aspectwerkz/system";

    /**
     * Holds the indexes for the introductions. The introduction indexes are needed here
     * (instead of in the AspectWerkz class like the advice indexes) since they need to
     * be available to the transformers before the AspectWerkz system has been initalized.
     */
    private final TObjectIntHashMap m_introductionIndexes = new TObjectIntHashMap();

    /**
     * Maps the introductions to it's name.
     */
    private final Map m_introductionMap = new HashMap();

    /**
     * Maps the advices to it's name.
     */
    private final Map m_adviceMap = new HashMap();

    /**
     * Maps the aspects to it's name.
     */
    private final Map m_aspectMap = new HashMap();

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
    private final String m_uuid = "default";

    /**
     * The transformation scopes.
     */
    private final Set m_transformationScopeSet = new HashSet();

    /**
     * Returns the definition.
     * <p/>
     * If the file name is not specified as a parameter to the JVM it tries
     * to locate a file named 'aspectwerkz.xml' on the classpath.
     *
     * @param isDirty flag to mark the the defintion as updated or not
     * @return the definition
     */
    public static AspectWerkzDefinition getDefinition(boolean isDirty) {
        String definitionFileName;
        if (DEFINITION_FILE == null) {
            URL definition = ContextClassLoader.loadResource(DEFAULT_DEFINITION_FILE_NAME);
            if (definition == null) throw new DefinitionException("definition file could not be found on classpath (either specify the file by using the -Daspectwerkz.definition.file=.. option or by having a definition file called aspectwerkz.xml somewhere on the classpath)");
            definitionFileName = definition.getFile();
        }
        else {
            definitionFileName = DEFINITION_FILE;
        }
        return getDefinition(definitionFileName, isDirty);
    }

    /**
     * Returns the definition.
     *
     * @param definitionFile the definition file
     * @return the definition
     */
    public static AspectWerkzDefinition getDefinition(final String definitionFile) {
        return getDefinition(definitionFile, false);
    }

    /**
     * Returns the definition.
     *
     * @param definitionFile the definition file
     * @param isDirty flag to mark the the defintion as updated or not
     * @return the definition
     */
    public static AspectWerkzDefinition getDefinition(final String definitionFile,
                                                      boolean isDirty) {
        return XmlDefinitionParser.parse(new File(definitionFile), isDirty);
    }

    /**
     * Creates a new instance, creates and sets the system aspect.
     */
    public AspectWerkzDefinition() {
        AspectDefinition systemAspect = new AspectDefinition();
        systemAspect.setName(SYSTEM_ASPECT);
        synchronized (m_aspectMap) {
            m_aspectMap.put(SYSTEM_ASPECT, systemAspect);
        }
    }

    /**
     * Returns the UUID for the weave model.
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
    public String getIntroductionImplementationName(final String introductionName) {
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
                if (controllerDef.matchMethodPointcut(classMetaData, methodMetaData)) {
                    return controllerDef.getClassName();
                }
            }
        }
        return null;
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
     * Checks if a class has an <tt>Aspect</tt>.
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
     * Checks if a class has an <tt>Introduction</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean hasIntroductions(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        for (Iterator it1 = m_aspectMap.values().iterator(); it1.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
            List weavingRules = aspectDefinition.getIntroductionWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                IntroductionWeavingRule weavingRule = (IntroductionWeavingRule)it2.next();
                if (weavingRule.getRegexpClassPattern().matches(className)) {
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
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchMethodPointcut(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasGetFieldPointcut(final ClassMetaData classMetaData,
                                       final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchGetFieldPointcut(classMetaData, fieldMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasSetFieldPointcut(final ClassMetaData classMetaData,
                                       final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchSetFieldPointcut(classMetaData, fieldMetaData)) {
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
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchThrowsPointcut(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class should care about advising caller side method invocations.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasCallerSidePointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it.next();
            Collection pointcuts = aspectDefinition.getPointcutDefs();
            for (Iterator it2 = pointcuts.iterator(); it2.hasNext();) {
                PointcutDefinition pointcutDefinition = (PointcutDefinition)it2.next();
                if (pointcutDefinition.getType().equalsIgnoreCase(PointcutDefinition.CALLER_SIDE) &&
                        pointcutDefinition.getRegexpClassPattern().matches(classMetaData.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method is a defined as a caller side method.
     *
     * @param className the class name
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean isCallerSideMethod(final String className,
                                      final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchCallerSidePointcut(className, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the names of the introductions for a certain class.
     *
     * @param className the name of the class
     * @return the names
     */
    public List getIntroductionNames(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        List introductionNames = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition definition = (AspectDefinition)it.next();
            List weavingRules = definition.getIntroductionWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                IntroductionWeavingRule weavingRule = (IntroductionWeavingRule)it2.next();
                if (weavingRule.getRegexpClassPattern().matches(className)) {
                    introductionNames.addAll(weavingRule.getIntroductionRefs());
                }
            }
        }
        return introductionNames;
    }
}


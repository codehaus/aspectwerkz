/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.MethodComparator;

/**
 * Holds the meta-data for the aspect.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectDefinition {

    /**
     * The name of the aspect.
     */
    private String m_name;

    /**
     * The aspect class name.
     */
    private final String m_className;

    /**
     * The deployment model for the aspect.
     */
    private final String m_deploymentModel;

    /**
     * The around advices.
     */
    private final List m_aroundAdvices = new ArrayList();

    /**
     * The pre advices.
     */
    private final List m_preAdvices = new ArrayList();

    /**
     * The post advices.
     */
    private final List m_postAdvices = new ArrayList();

    /**
     * The method introductions.
     */
    private final List m_methodIntroductions = new ArrayList();

    /**
     * The interface introductions.
     */
    private final List m_interfaceIntroductions = new ArrayList();

    /**
     * The pointcuts.
     */
    private final List m_pointcutDefs = new ArrayList();

    /**
     * The parameters passed to the advice at definition time.
     */
    private Map m_parameters = new HashMap();

    /**
     * The controller definitions.
     */
    private final Map m_controllerDefs = new HashMap();

    /**
     * Creates a new aspect meta-data instance.
     *
     * @param name the name of the aspect
     * @param className the class name of the aspect
     * @param deploymentModel the deployment model
     */
    public AspectDefinition(final String name,
                            final String className,
                            final String deploymentModel) {
        if (name == null) throw new IllegalArgumentException("aspect name can not be null");
        if (className == null) throw new IllegalArgumentException("aspect class name can not be null");
        if (deploymentModel == null) throw new IllegalArgumentException("deployment model can not be null");
        m_name = name;
        m_className = className;
        m_deploymentModel = deploymentModel;
    }

    /**
     * Returns the pattern for the aspect
     * @return the pattern
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name for the aspect.
     *
     * @param name the name
     */
    public void setName(final String name) {
        m_name = name.trim();
    }

    /**
     * Returns the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * Returns the deployment model.
     *
     * @return the deployment model
     */
    public String getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Adds a new around advice.
     *
     * @param adviceMetaData the around advice
     */
    public void addAroundAdvice(final AdviceDefinition adviceMetaData) {
        m_aroundAdvices.add(adviceMetaData);
    }

    /**
     * Returns the around advices.
     *
     * @return the around advices
     */
    public List getAroundAdvices() {
        return m_aroundAdvices;
    }

    /**
     * Adds a new pre advice.
     *
     * @param adviceMetaData the pre advice
     */
    public void addPreAdvice(final AdviceDefinition adviceMetaData) {
        m_preAdvices.add(adviceMetaData);
    }

    /**
     * Returns the pre advices.
     *
     * @return the pre advices
     */
    public List getPreAdvices() {
        return m_preAdvices;
    }

    /**
     * Adds a new post advice.
     *
     * @param adviceMetaData the post advice
     */
    public void addPostAdvice(final AdviceDefinition adviceMetaData) {
        m_postAdvices.add(adviceMetaData);
    }

    /**
     * Returns the post advices.
     *
     * @return the post advices
     */
    public List getPostAdvices() {
        return m_postAdvices;
    }

    /**
     * Adds a new method introduction.
     *
     * @param introductionMetaData the introduction
     */
    public void addMethodIntroduction(final MethodIntroductionDefinition introductionMetaData) {
        m_methodIntroductions.add(introductionMetaData);
    }

    /**
     * Adds a new interface introduction.
     *
     * @param interfaceIntroDef the introduction
     */
    public void addInterfaceIntroduction(final InterfaceIntroductionDefinition introductionMetaData) {
        m_interfaceIntroductions.add(introductionMetaData);
    }

    /**
     * Returns the method introductions.
     *
     * @TODO: gets sorted every time, have a flag?
     *
     * @return the introductions
     */
    public List getMethodIntroductions() {
        return sortIntroductions(m_methodIntroductions);
    }

    /**
     * Returns the interface introductions.
     *
     * @return the introductions
     */
    public List getInterfaceIntroductions() {
        return m_interfaceIntroductions;
    }

    /**
     * Adds a new pointcut definition.
     *
     * @param pointcutDef the pointcut definition
     */
    public void addPointcut(final PointcutDefinition pointcutDef) {
        m_pointcutDefs.add(pointcutDef);
    }

    /**
     * Returns the pointcuts.
     *
     * @return the pointcuts
     */
    public Collection getPointcuts() {
        return m_pointcutDefs;
    }

    /**
     * Returns a specific pointcut.
     *
     * @param pointcutName the pointcut name
     * @return the pointcut definition
     */
    public PointcutDefinition getPointcutDef(final String pointcutName) {
        for (Iterator it = m_pointcutDefs.iterator(); it.hasNext();) {
            PointcutDefinition pointcutDef = (PointcutDefinition)it.next();
            if (pointcutDef.getName().equals(pointcutName)) {
                return pointcutDef;
            }
        }
        return null;
    }

    /**
     * Adds a new parameter to the advice.
     *
     * @param name the name of the parameter
     * @param value the value for the parameter
     */
    public void addParameter(final String name, final String value) {
        m_parameters.put(name, value);
    }

    /**
     * Returns the parameters as a Map.
     *
     * @return the parameters
     */
    public Map getParameters() {
        return m_parameters;
    }

    /**
      * Returns a list with the controllers.
      *
      * @return the controllers
      */
     public Collection getControllers() {
         return m_controllerDefs.values();
     }

    /**
     * Adds a new controller definition.
     *
     * @param controllerDef a controller definition
     */
    public void addController(final ControllerDefinition controllerDef) {
        m_controllerDefs.put(controllerDef.getExpression(), controllerDef);
    }

    /**
     * Returns all the advices for this aspect.
     *
     * @TODO: gets sorted every time, have a flag?
     *
     * @return all the advices
     */
    public List getAllAdvices() {
        final List allAdvices = new ArrayList();
        allAdvices.addAll(m_aroundAdvices);
        allAdvices.addAll(m_preAdvices);
        allAdvices.addAll(m_postAdvices);
        return sortAdvices(allAdvices);
    }

    /**
     * Sorts the advice by method.
     *
     * @param advices a list with the advices to sort
     * @return a sorted list with the advices
     */
    public static List sortAdvices(final List advices) {
        Collections.sort(advices, new Comparator() {
            private Comparator m_comparator = MethodComparator.getInstance(MethodComparator.NORMAL_METHOD);
            public int compare(final Object obj1, final Object obj2) {
                AdviceDefinition advice1 = (AdviceDefinition)obj1;
                AdviceDefinition advice2 = (AdviceDefinition)obj2;
                return m_comparator.compare(advice1.getMethod(), advice2.getMethod());
            }
        });
        return advices;
    }

    /**
     * Sorts the introductions by method.
     *
     * @param introductions a list with the introductions to sort
     * @return a sorted list with the introductions
     */
    public static List sortIntroductions(final List introductions) {
        Collections.sort(introductions, new Comparator() {
            private Comparator m_comparator = MethodComparator.getInstance(MethodComparator.NORMAL_METHOD);
            public int compare(final Object obj1, final Object obj2) {
                MethodIntroductionDefinition introduction1 = (MethodIntroductionDefinition)obj1;
                MethodIntroductionDefinition introduction2 = (MethodIntroductionDefinition)obj2;
                return m_comparator.compare(introduction1.getMethod(), introduction2.getMethod());
            }
        });
        return introductions;
    }
}


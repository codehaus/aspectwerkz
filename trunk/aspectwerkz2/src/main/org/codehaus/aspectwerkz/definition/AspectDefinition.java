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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.aspectwerkz.*;
import org.codehaus.aspectwerkz.AspectSystem;

/**
 * Holds the meta-data for the aspect.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
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
    private String m_deploymentModel = "perJVM";

    /**
     * The around advices.
     */
    private final List m_aroundAdvices = new ArrayList();

    /**
     * The before advices.
     */
    private final List m_beforeAdvices = new ArrayList();

    /**
     * The after advices.
     */
    private final List m_afterAdvices = new ArrayList();

    /**
     * The interface introductions (pure interfaces)
     */
    private final List m_interfaceIntroductions = new ArrayList();

    /**
     * The pointcuts definitions. The implementation introductions
     */
    private final List m_introductions = new ArrayList();

    /**
     * The pointcuts.
     */
    private final List m_pointcutDefs = new ArrayList();

    /**
     * The parameters passed to the advice at definition time.
     */
    private Map m_parameters = new HashMap();

    /**
     * Creates a new aspect meta-data instance.
     *
     * @param name            the name of the aspect
     * @param className       the class name of the aspect
     */
    public AspectDefinition(final String name, final String className) {
        if (name == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }
        if (className == null) {
            throw new IllegalArgumentException("aspect class name can not be null");
        }
        m_name = name;
        m_className = className;
    }

    /**
     * Returns the pattern for the aspect
     *
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
     * Sets the deployment model.
     *
     * @param deploymentModel the deployment model
     */
    public void setDeploymentModel(final String deploymentModel) {
        m_deploymentModel = deploymentModel;
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
     * Remove an around advice.
     * Experimental
     *
     * @param adviceMetaData the around advice
     */
    public void removeAroundAdvice(final AdviceDefinition adviceMetaData) {
        m_aroundAdvices.remove(adviceMetaData);
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
     * Adds a new before advice.
     *
     * @param adviceMetaData the before advice
     */
    public void addBeforeAdvice(final AdviceDefinition adviceMetaData) {
        m_beforeAdvices.add(adviceMetaData);
    }

    /**
     * Returns the before advices.
     *
     * @return the before advices
     */
    public List getBeforeAdvices() {
        return m_beforeAdvices;
    }

    /**
     * Adds a new after advice.
     *
     * @param adviceMetaData the after advice
     */
    public void addAfterAdvice(final AdviceDefinition adviceMetaData) {
        m_afterAdvices.add(adviceMetaData);
    }

    /**
     * Returns the after advices.
     *
     * @return the after advices
     */
    public List getAfterAdvices() {
        return m_afterAdvices;
    }

    /**
     * Adds a new pure interface introduction.
     *
     * @param interfaceIntroductionMetaData the introduction
     */
    public void addInterfaceIntroduction(final InterfaceIntroductionDefinition interfaceIntroductionMetaData) {
        m_interfaceIntroductions.add(interfaceIntroductionMetaData);
    }

    /**
     * Adds a new implementation introduction.
     *
     * @param introductionMetaData the introduction
     */
    public void addIntroduction(final IntroductionDefinition introductionMetaData) {
        m_introductions.add(introductionMetaData);
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
     * Returns the implementation introductions.
     *
     * @return the introductions
     */
    public List getIntroductions() {
        return m_introductions;
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
     * @param name  the name of the parameter
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
     * Returns all the advices for this aspect.
     *
     * @return all the advices
     * @TODO: gets sorted every time, have a flag?
     */
    public List getAllAdvices() {
        final List allAdvices = new ArrayList();
        allAdvices.addAll(m_aroundAdvices);
        allAdvices.addAll(m_beforeAdvices);
        allAdvices.addAll(m_afterAdvices);
        return sortAdvices(allAdvices);
    }

    /**
     * Sorts the advice by method.
     *
     * @param advices a list with the advices to sort
     * @return a sorted list with the advices
     */
    public static List sortAdvices(final List advices) {
        Collections.sort(
                advices, new Comparator() {
                    private Comparator m_comparator = MethodComparator.getInstance(MethodComparator.NORMAL_METHOD);

                    public int compare(final Object obj1, final Object obj2) {
                        AdviceDefinition advice1 = (AdviceDefinition)obj1;
                        AdviceDefinition advice2 = (AdviceDefinition)obj2;
                        return m_comparator.compare(advice1.getMethod(), advice2.getMethod());
                    }
                }
        );
        return advices;
    }
}


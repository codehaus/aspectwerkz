/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition2;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.Serializable;

import org.codehaus.aspectwerkz.definition2.AdviceDefinition2;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;

/**
 * Holds the meta-data for the aspect.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectDefinition2 implements Serializable {

    /**
     * The name of the aspect.
     */
    private String m_name;

    /**
     * The aspect class.
     */
    private final Class m_klass;

    /**
     * The deployment model for the aspect.
     */
    private final String m_deploymentModel;

    /**
     * The around advices.
     */
    private final Set m_aroundAdvices = new HashSet();

    /**
     * The pre advices.
     */
    private final Set m_preAdvices = new HashSet();

    /**
     * The post advices.
     */
    private final Set m_postAdvices = new HashSet();

    /**
     * The introductions.
     */
    private final Set m_introductions = new HashSet();

    /**
     * The pointcuts.
     */
    private final Set m_pointcuts = new HashSet();

    /**
     * The parameters passed to the advice at definition time.
     */
    private Map m_parameters = new HashMap();

    /**
     * Creates a new aspect meta-data instance.
     *
     * @param name the name of the aspect
     * @param klass the class of the aspect
     * @param deploymentModel the deployment model
     */
    public AspectDefinition2(final String name, final Class klass, final String deploymentModel) {
        m_name = name;
        m_klass = klass;
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
     * Returns the class.
     *
     * @return the class
     */
    public Class getKlass() {
        return m_klass;
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
    public void addAroundAdvice(final AdviceDefinition2 adviceMetaData) {
        m_aroundAdvices.add(adviceMetaData);
    }

    /**
     * Returns the around advices.
     *
     * @return the around advices
     */
    public Set getAroundAdvices() {
        return m_aroundAdvices;
    }

    /**
     * Adds a new pre advice.
     *
     * @param adviceMetaData the pre advice
     */
    public void addPreAdvice(final AdviceDefinition2 adviceMetaData) {
        m_preAdvices.add(adviceMetaData);
    }

    /**
     * Returns the pre advices.
     *
     * @return the pre advices
     */
    public Set getPreAdvices() {
        return m_preAdvices;
    }

    /**
     * Adds a new post advice.
     *
     * @param adviceMetaData the post advice
     */
    public void addPostAdvice(final AdviceDefinition2 adviceMetaData) {
        m_postAdvices.add(adviceMetaData);
    }

    /**
     * Returns the post advices.
     *
     * @return the post advices
     */
    public Set getPostAdvices() {
        return m_postAdvices;
    }

    /**
     * Adds a new introduction.
     *
     * @param introductionMetaData the introduction
     */
    public void addIntroduction(final IntroductionDefinition2 introductionMetaData) {
        m_introductions.add(introductionMetaData);
    }

    /**
     * Returns the introductions.
     *
     * @return the introductions
     */
    public Set getIntroductions() {
        return m_introductions;
    }

    /**
     * Adds a new pointcut.
     *
     * @param pointcutMetaData the pointcut
     */
    public void addPointcut(final PointcutDefinition2 pointcutMetaData) {
        m_pointcuts.add(pointcutMetaData);
    }

    /**
     * Returns the pointcuts.
     *
     * @return the pointcuts
     */
    public Set getPointcuts() {
        return m_pointcuts;
    }

    /**
     * Returns a specific pointcut.
     *
     * @param pointcutName the pointcut name
     * @return the pointcut definition
     */
    public PointcutDefinition2 getPointcutDef(final String pointcutName) {
        for (Iterator it = m_pointcuts.iterator(); it.hasNext();) {
            PointcutDefinition2 pointcutDef = (PointcutDefinition2)it.next();
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
}


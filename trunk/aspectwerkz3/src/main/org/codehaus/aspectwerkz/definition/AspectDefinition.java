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
import java.util.List;
import java.util.Map;

/**
 * Holds the meta-data for the aspect.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AspectDefinition {

    /**
     * The default aspectwerkz aspect model type id.
     */
    public static final String ASPECTWERKZ_ASPECT_MODEL_TYPE = "aspectwerkz";

    /**
     * The name of the aspect (nickname).
     */
    private String m_name;

    /**
     * The nickname of the aspect prefixed by the <system uuid>/
     */
    private String m_qualifiedName;

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
    private final List m_aroundAdviceDefinitions = new ArrayList();

    /**
     * The before advices.
     */
    private final List m_beforeAdviceDefinitions = new ArrayList();

    /**
     * The after advices.
     */
    private final List m_afterAdviceDefinitions = new ArrayList();

    /**
     * The interface introductions (pure interfaces)
     */
    private final List m_interfaceIntroductionDefinitions = new ArrayList();

    /**
     * The pointcuts definitions. The implementation introductions
     */
    private final List m_introductionDefinitions = new ArrayList();

    /**
     * The pointcuts.
     */
    private final List m_pointcutDefinitions = new ArrayList();

    /**
     * The parameters passed to the advice at definition time.
     */
    private Map m_parameters = new HashMap();

    /**
     * The container implementation class name.
     */
    private String m_containerClassName;

    /**
     * The system definition.
     */
    private SystemDefinition m_systemDefinition;

    /**
     * The aspect model.
     */
    private String m_aspectModelType = ASPECTWERKZ_ASPECT_MODEL_TYPE;

    /**
     * Creates a new aspect meta-data instance.
     *
     * @param name      the name of the aspect
     * @param className the class name of the aspect
     */
    public AspectDefinition(final String name, final String className, final SystemDefinition systemDefinition) {
        if (name == null) {
            throw new IllegalArgumentException("aspect name can not be null");
        }
        if (className == null) {
            throw new IllegalArgumentException("aspect class name can not be null");
        }
        m_name = name;
        m_className = className;
        m_systemDefinition = systemDefinition;
        m_qualifiedName = systemDefinition.getUuid() + '/' + name;
    }

    /**
     * Returns the name for the advice
     *
     * @return the name
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
     * Returns the fully qualified name for the advice
     *
     * @return the fully qualified name
     */
    public String getQualifiedName() {
        return m_qualifiedName;
    }

    /**
     * Returns the system definition.
     *
     * @return
     */
    public SystemDefinition getSystemDefinition() {
        return m_systemDefinition;
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
     * Returns the aspect model.
     *
     * @return the aspect model
     */
    public String getAspectModel() {
        return m_aspectModelType;
    }

    /**
     * Checks if the aspect defined is an AspectWerkz aspect.
     *
     * @return
     */
    public boolean isAspectWerkzAspect() {
        return m_aspectModelType.equals(ASPECTWERKZ_ASPECT_MODEL_TYPE);
    }

    /**
     * Sets the aspect model.
     *
     * @param aspectModelType the aspect model
     */
    public void setAspectModel(final String aspectModelType) {
        m_aspectModelType = aspectModelType;
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
     * @param adviceDef the around advice
     */
    public void addAroundAdviceDefinition(final AdviceDefinition adviceDef) {
        m_aroundAdviceDefinitions.add(adviceDef);
    }

    /**
     * Returns the around advices.
     *
     * @return the around advices
     */
    public List getAroundAdviceDefinitions() {
        return m_aroundAdviceDefinitions;
    }

    /**
     * Adds a new before advice.
     *
     * @param adviceDef the before advice
     */
    public void addBeforeAdviceDefinition(final AdviceDefinition adviceDef) {
        m_beforeAdviceDefinitions.add(adviceDef);
    }

    /**
     * Returns the before advices.
     *
     * @return the before advices
     */
    public List getBeforeAdviceDefinitions() {
        return m_beforeAdviceDefinitions;
    }

    /**
     * Adds a new after advice.
     *
     * @param adviceDef the after advice
     */
    public void addAfterAdviceDefinition(final AdviceDefinition adviceDef) {
        m_afterAdviceDefinitions.add(adviceDef);
    }

    /**
     * Returns the after advices.
     *
     * @return the after advices
     */
    public List getAfterAdviceDefinitions() {
        return m_afterAdviceDefinitions;
    }

    /**
     * Adds a new pure interface introduction.
     *
     * @param interfaceIntroDef the introduction
     */
    public void addInterfaceIntroductionDefinition(final InterfaceIntroductionDefinition interfaceIntroDef) {
        m_interfaceIntroductionDefinitions.add(interfaceIntroDef);
    }

    /**
     * Adds a new implementation introduction.
     *
     * @param introductionDef the introduction
     */
    public void addIntroductionDefinition(final IntroductionDefinition introductionDef) {
        m_introductionDefinitions.add(introductionDef);
    }

    /**
     * Returns the interface introductions.
     *
     * @return the introductions
     */
    public List getInterfaceIntroductionDefinitions() {
        return m_interfaceIntroductionDefinitions;
    }

    /**
     * Returns the implementation introductions.
     *
     * @return the introductions
     */
    public List getIntroductionDefinitions() {
        return m_introductionDefinitions;
    }

    /**
     * Adds a new pointcut definition.
     *
     * @param pointcutDef the pointcut definition
     */
    public void addPointcutDefinition(final PointcutDefinition pointcutDef) {
        m_pointcutDefinitions.add(pointcutDef);
    }

    /**
     * Returns the pointcuts.
     *
     * @return the pointcuts
     */
    public Collection getPointcutDefinitions() {
        return m_pointcutDefinitions;
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
     * Sets the name of the container implementation class.
     *
     * @param containerClassName the container class name
     */
    public void setContainerClassName(final String containerClassName) {
        m_containerClassName = containerClassName;
    }

    /**
     * Returns the name of the container implementation class.
     *
     * @return the container class name
     */
    public String getContainerClassName() {
        return m_containerClassName;
    }

    /**
     * Returns all the advices for this aspect.
     *
     * @return all the advices
     */
    public List getAdviceDefinitions() {
        final List allAdvices = new ArrayList();
        allAdvices.addAll(m_aroundAdviceDefinitions);
        allAdvices.addAll(m_beforeAdviceDefinitions);
        allAdvices.addAll(m_afterAdviceDefinitions);
        return allAdvices;
    }
}
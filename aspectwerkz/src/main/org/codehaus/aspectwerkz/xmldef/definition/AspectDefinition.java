/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.io.Serializable;

/**
 * Holds the aspect definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectDefinition implements Serializable {

    /**
     * The name of the aspect.
     */
    private String m_name;

    /**
     * The aspect that this aspect extends.
     */
    private String m_extends;

    /**
     * The pointcuts for this aspect.
     */
    private final Map m_pointcutDefs = new SequencedHashMap();

    /**
     * The controller definitions.
     */
    private final Map m_controllerDefs = new HashMap();

    /**
     * The introduction weaving rules for this aspect.
     */
    private final List m_introductionWeavingRules = new ArrayList();

    /**
     * The advice weaving rules for this aspect.
     */
    private final List m_adviceWeavingRules = new ArrayList();

    /**
     * Flag to mark the aspect as abstract.
     */
    private boolean isAbstract = false;

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
     * Returns the name of the aspect to extend.
     *
     * @return the name of the aspect to extend
     */
    public String getExtends() {
        return m_extends;
    }

    /**
     * Sets the name of the aspect to extend.
     *
     * @param anExtends the name of the aspect to extend
     */
    public void setExtends(final String anExtends) {
        m_extends = anExtends;
    }

    /**
     * Returns a list with the pointcuts.
     *
     * @return the pointcuts
     */
    public Collection getPointcutDefs() {
        return m_pointcutDefs.values();
    }

    /**
     * Adds a new pointcut.
     *
     * @param pointcut a pointcut
     */
    public void addPointcutDef(final PointcutDefinition pointcut) {
        m_pointcutDefs.put(pointcut.getName(), pointcut);
    }

    /**
     * Returns a list with the controllers.
     *
     * @return the controllers
     */
    public Collection getControllerDefs() {
        return m_controllerDefs.values();
    }

    /**
     * Adds a new controller definition.
     *
     * @param controllerDef a controller definition
     */
    public void addControllerDef(final ControllerDefinition controllerDef) {
        m_controllerDefs.put(controllerDef.getExpression(), controllerDef);
    }

    /**
     * Returns a list with the introduction weaving rules.
     *
     * @return the introduction weaving rules
     */
    public List getIntroductionWeavingRules() {
        return m_introductionWeavingRules;
    }

    /**
     * Adds a new introduction weaving rule.
     *
     * @param weavingRule an introduction weaving rule
     */
    public void addIntroductionWeavingRule(final IntroductionWeavingRule weavingRule) {
        m_introductionWeavingRules.add(weavingRule);
    }

    /**
     * Returns a list with the advice weaving rules.
     *
     * @return the advice weaving rules
     */
    public List getAdviceWeavingRules() {
        return m_adviceWeavingRules;
    }

    /**
     * Adds a new advice weaving rule.
     *
     * @param weavingRule an advice weaving rule
     */
    public void addAdviceWeavingRule(final AdviceWeavingRule weavingRule) {
        m_adviceWeavingRules.add(weavingRule);
    }

    /**
     * Returns the pointcut definition by its name.
     *
     * @param pointcut the name of the pointcut
     * @return the pointcut definition
     */
    public PointcutDefinition getPointcutDef(final String pointcut) {
        return (PointcutDefinition)m_pointcutDefs.get(pointcut);
    }

    /**
     * Checks if the aspect is abstract.
     *
     * @return boolean
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * Marks the aspect as abstract.
     *
     * @param anAbstract boolean
     */
    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }
}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;

/**
 * Validates an AspectWerkz definition, looking for:
 * <ul>
 *  <li>Class and interface references not found on the CLASSPATH</li>
 *  <li>Duplicate aspect, advice, pointcut and introduction definitions</li>
 *  <li>Undefined advice and introduction references
 * </ul>
 *
 * @todo Add warning for duplicated/redundant advices for the same join point
 * @todo Check pattern expressions syntax
 *
 * @author <a href="mailto:carlos@bluebox.com.br">Carlos Villela</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DefinitionValidator {

    private List m_errors = new ArrayList();

    private Set m_names = new HashSet();
    private Set m_attributes = new HashSet();

    private final AspectWerkzDefinition m_definition;

    /**
     * Creates a new Definition Validator
     *
     * @param definition the definition to validate
     */
    public DefinitionValidator(final AspectWerkzDefinition definition) {
        m_definition = definition;
    }

    /**
     * Validates a Weave Model
     */
    public void validate() {
        Collection introductionDefs = m_definition.getIntroductionDefinitions();
        Collection adviceDefs = m_definition.getAdviceDefinitions();
        Collection aspectDefs = m_definition.getAspectDefinitions();

        validateIntroductions(introductionDefs);
        validateAdvices(adviceDefs);
        validateAspects(aspectDefs);
    }

    /**
     * Validates aspect definitions
     *
     * @param aspectDefs a Collection of AspectDefinitions
     */
    private void validateAspects(Collection aspectDefs) {
        for (Iterator i = aspectDefs.iterator(); i.hasNext();) {
            AspectDefinition def = (AspectDefinition)i.next();

            List adviceRules = def.getAdviceWeavingRules();
            List introductionRules = def.getIntroductionWeavingRules();

            for (Iterator i1 = adviceRules.iterator(); i1.hasNext();) {
                AdviceWeavingRule rule = (AdviceWeavingRule)i1.next();
                checkUndefinedAdvices(rule.getAdviceRefs());
                // @todo Check for undefined advice stacks
            }

            for (Iterator i2 = introductionRules.iterator(); i2.hasNext();) {
                IntroductionWeavingRule rule =
                        (IntroductionWeavingRule)i2.next();
                checkUndefinedIntroductions(rule.getIntroductionRefs());
            }

            Collection pointcutDefs = def.getPointcutDefs();
            validatePointcuts(pointcutDefs);
        }
    }

    /**
     * Checks for undefined introduction references
     * @param refs introduction references to check
     */
    private void checkUndefinedIntroductions(List refs) {
        for (Iterator i = refs.iterator(); i.hasNext();) {
            String ref = (String)i.next();
            if (!m_definition.hasIntroduction(ref))
                addErrorMessage("Introduction '" + ref + "' not defined");
        }
    }

    /**
     * Check for undefined advice references
     * @param refs advice references to check
     */
    private void checkUndefinedAdvices(List refs) {
        for (Iterator i = refs.iterator(); i.hasNext();) {
            String ref = (String)i.next();
            if (!m_definition.hasAdvice(ref))
                addErrorMessage("Advice '" + ref + "' not defined");
        }
    }

    /**
     * Validates pointcut definitions
     * @param pointcutDefs a Collection of PointcutDefinitions
     */
    private void validatePointcuts(Collection pointcutDefs) {
        for (Iterator i = pointcutDefs.iterator(); i.hasNext();) {
            PointcutDefinition def = (PointcutDefinition)i.next();
            checkDuplicateName(def.getName());
        }
    }

    /**
     * Validates advice definitions
     *
     * @param adviceDefs a Collection of AdviceDefinitions
     */
    private void validateAdvices(Collection adviceDefs) {

        for (Iterator i = adviceDefs.iterator(); i.hasNext();) {
            AdviceDefinition def = (AdviceDefinition)i.next();

            String adviceClass = def.getAdviceClassName();
            try {
                Class.forName(adviceClass);
            }
            catch (ClassNotFoundException e) {
                addErrorMessage(
                        "Advice class not found: "
                        + adviceClass
                        + "(exception: "
                        + e
                        + ")");
            }
            catch (NoClassDefFoundError e) {
                addErrorMessage(
                        "Advice class not found: "
                        + adviceClass
                        + "(exception: "
                        + e
                        + ")");

            }

            checkDuplicateName(def.getName());
            checkDuplicateAttribute(def.getAttribute());
        }
    }

    /**
     * Validates introduction definitions
     *
     * @param introductionDefs a Collection of IntroductionDefinitions
     */
    private void validateIntroductions(Collection introductionDefs) {

        for (Iterator i = introductionDefs.iterator(); i.hasNext();) {
            IntroductionDefinition def = (IntroductionDefinition)i.next();

            String interf = def.getInterface();
            try {
                Class.forName(interf);
            }
            catch (ClassNotFoundException e) {
                addErrorMessage("Introduction interface not found: " + interf);
            }

            String impl = def.getImplementation();
            if (impl != null) {
                try {
                    Class.forName(impl);
                }
                catch (ClassNotFoundException e) {
                    addErrorMessage(
                            "Introduction implementation not found: " + impl);
                }
            }

            checkDuplicateName(def.getName());
            checkDuplicateAttribute(def.getAttribute());
        }
    }

    /**
     * Checks for duplicate attribute m_names
     * @param attribute attribute name to check
     */
    private void checkDuplicateAttribute(String attribute) {
        if (attribute != null
                && !"".equals(attribute)
                && !m_attributes.add(attribute))

            addErrorMessage("Duplicate attribute definition: " + attribute);
    }

    /**
     * Checks for duplicate aspect, advice or introduction m_names
     * @param name name to check
     */
    private void checkDuplicateName(String name) {
        if (!m_names.add(name))
            addErrorMessage("Duplicate name definition: " + name);
    }

    /**
     * Adds an error message to the error messages list.
     * You can query these error messages using getErrorMessages().
     * @param errorMessage
     */
    private void addErrorMessage(String errorMessage) {
        this.m_errors.add(errorMessage);
    }

    /**
     * @return a list of the m_errors found
     */
    public List getErrorMessages() {
        return m_errors;
    }

}

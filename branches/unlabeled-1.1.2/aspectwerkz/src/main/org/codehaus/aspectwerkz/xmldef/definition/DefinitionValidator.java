/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR and PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.aspectwerkz.metadata.WeaveModel;

/**
 * Validates a Weave Model, looking for:
 * <ul>
 *  <li>Class and interface references not found on the CLASSPATH</li>
 *  <li>Duplicate aspect, advice, pointcut and introduction definitions</li>
 *  <li>Undefined advice and introduction references
 * </ul>
 *
 * TODO: Add warning for duplicated/redundant advices for the same join point
 * TODO: Check pattern expressions syntax 
 * 
 * @author <a href="mailto:carlos@bluebox.com.br">Carlos Villela</a>
 * @version $Id: DefinitionValidator.java,v 1.1.2.1 2003-07-20 10:38:36 avasseur Exp $
 */
public class DefinitionValidator {

	private List errors = new ArrayList();

	private Set names = new HashSet();
	private Set attributes = new HashSet();

	private WeaveModel weaveModel;

	/**
	 * Creates a new WeaveModel Validator
	 * @param weaveModel the WeaveModel to validate
	 */
	public DefinitionValidator(WeaveModel weaveModel) {
		this.weaveModel = weaveModel;
	}

	/**
	 * Validates a Weave Model
	 */
	public void validate() {
		AspectWerkzDefinition definition = weaveModel.getDefinition();

		Collection introductionDefs = definition.getIntroductionDefinitions();
		Collection adviceDefs = definition.getAdviceDefinitions();
		Collection aspectDefs = definition.getAspectDefinitions();

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
			AspectDefinition def = (AspectDefinition) i.next();

			List adviceRules = def.getAdviceWeavingRules();
			List introductionRules = def.getIntroductionWeavingRules();

			for (Iterator i1 = adviceRules.iterator(); i1.hasNext();) {
				AdviceWeavingRule rule = (AdviceWeavingRule) i1.next();
				checkUndefinedAdvices(rule.getAdviceRefs());
				// TODO: Check for undefined advice stacks
			}

			for (Iterator i2 = introductionRules.iterator(); i2.hasNext();) {
				IntroductionWeavingRule rule =
					(IntroductionWeavingRule) i2.next();
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
		AspectWerkzDefinition definition = weaveModel.getDefinition();

		Collection defs = definition.getIntroductionDefinitions();
		for (Iterator i = refs.iterator(); i.hasNext();) {
			String ref = (String) i.next();
			if (!definition.hasIntroduction(ref))
				addErrorMessage("Introduction '" + ref + "' not defined");
		}
	}

	/**
	 * Check for undefined advice references
	 * @param refs advice references to check
	 */
	private void checkUndefinedAdvices(List refs) {
		AspectWerkzDefinition definition = weaveModel.getDefinition();

		Collection defs = definition.getAdviceDefinitions();
		for (Iterator i = refs.iterator(); i.hasNext();) {
			String ref = (String) i.next();
			if (!definition.hasAdvice(ref))
				addErrorMessage("Advice '" + ref + "' not defined");
		}
	}

	/**
	 * Validates pointcut definitions
	 * @param pointcutDefs a Collection of PointcutDefinitions
	 */
	private void validatePointcuts(Collection pointcutDefs) {
		for (Iterator i = pointcutDefs.iterator(); i.hasNext();) {
			PointcutDefinition def = (PointcutDefinition) i.next();

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
			AdviceDefinition def = (AdviceDefinition) i.next();

			String adviceClass = def.getAdviceClassName();
			try {
				Class.forName(adviceClass);
			} catch (ClassNotFoundException e) {
				addErrorMessage(
					"Advice class not found: "
						+ adviceClass
						+ "(exception: "
						+ e
						+ ")");
			} catch (NoClassDefFoundError e) {
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
			IntroductionDefinition def = (IntroductionDefinition) i.next();

			String interf = def.getInterface();
			try {
				Class.forName(interf);
			} catch (ClassNotFoundException e) {
				addErrorMessage("Introduction interface not found: " + interf);
			}

			String impl = def.getImplementation();
			if (impl != null) {
				try {
					Class.forName(impl);
				} catch (ClassNotFoundException e) {
					addErrorMessage(
						"Introduction implementation not found: " + impl);
				}
			}

			checkDuplicateName(def.getName());
			checkDuplicateAttribute(def.getAttribute());
		}
	}

	/**
	 * Checks for duplicate attribute names
	 * @param attribute attribute name to check
	 */
	private void checkDuplicateAttribute(String attribute) {
		if (attribute != null
			&& !"".equals(attribute)
			&& !attributes.add(attribute))
			
			addErrorMessage("Duplicate attribute definition: " + attribute);
	}

	/**
	 * Checks for duplicate aspect, advice or introduction names
	 * @param name name to check
	 */
	private void checkDuplicateName(String name) {
		if (!names.add(name))
			addErrorMessage("Duplicate name definition: " + name);
	}

	/**
	 * Adds an error message to the error messages list.
	 * You can query these error messages using getErrorMessages().
	 * @param errorMessage
	 */
	private void addErrorMessage(String errorMessage) {
		this.errors.add(errorMessage);
	}

	/**
	 * @return a list of the errors found
	 */
	public List getErrorMessages() {
		return errors;
	}

}

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.definition.expression.Expression;

/**
 * Holds the meta-data for an interface + implementation introduction.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class IntroductionDefinition extends InterfaceIntroductionDefinition {

    /**
     * The deployment model for the introduction.
     */
    private final String m_deploymentModel;

    /**
     * The introduced methods meta-data list.
     */
    private final List m_methodIntroduction = new ArrayList();

    /**
     * Construct a new Definition for introduction.
     *
     * @param name of the introduction
     * @param expression
     * @param interfaceClassNames FQNs for introduced interfaces
     * @param introducedMethods Methods from introduced implementation
     * @param deploymentModel introduction deployment model
     */
    public IntroductionDefinition(final String name,
                                  final Expression expression,
                                  final String[] interfaceClassNames,
                                  final Method[] introducedMethods,
                                  final String deploymentModel) {
        super(name, expression, interfaceClassNames[0]);
        for (int i = 1; i < interfaceClassNames.length; i++) {
            m_interfaceClassNames.add(interfaceClassNames[i]);
        }
        for (int i = 0; i < introducedMethods.length; i++) {
            m_methodIntroduction.add(ReflectionMetaDataMaker.createMethodMetaData(introducedMethods[i]));
        }
        m_deploymentModel = deploymentModel;
    }

    /**
     * Returns the methods to introduce.
     *
     * @return the methods to introduce
     */
    public List getMethodIntroductions() {
        return m_methodIntroduction;
    }

    /**
     * Returns the deployment model.
     *
     * @return the deployment model
     */
    public String getDeploymentModel() {
        return m_deploymentModel;
    }

}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;

/**
 * Holds the meta-data for an interface + implementation introduction.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionDefinition extends InterfaceIntroductionDefinition {

    /**
     * The introduced methods MetData list
     */
    private List m_methodIntroduction = new ArrayList();

    /**
     * Construct a new Definition for introduction
     * @param name of the introduction
     * @param expression
     * @param interfaceClassNames FQNs for introduced interfaces
     * @param introducedMethods Methods from introduced implementation
     */
    public IntroductionDefinition(final String name,
                                           final String expression,
                                           final String[] interfaceClassNames,
                                           final Method[] introducedMethods) {
        super(name, expression, interfaceClassNames[0]);
        for (int i = 1; i < interfaceClassNames.length; i++) {
            m_interfaceClassNames.add(interfaceClassNames[i]);
        }

        // turn methods in metadata
        for (int i = 0; i < introducedMethods.length; i++) {
            m_methodIntroduction.add(ReflectionMetaDataMaker.createMethodMetaData(introducedMethods[i]));
        }
    }

    /**
     * @return the introduced methods MetaData list
     */
    public List getMethodIntroductions() {
        return m_methodIntroduction;
    }

}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence.definition;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ParameterDefinition implements Definition {

    private String m_name;
    private String m_value;

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(String value) {
        m_value = value;
    }
}

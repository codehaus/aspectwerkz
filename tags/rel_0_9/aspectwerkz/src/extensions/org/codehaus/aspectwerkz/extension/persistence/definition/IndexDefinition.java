/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence.definition;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class IndexDefinition implements Definition {

    private String m_name = null;
    private String m_type = null;

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        m_type = type;
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence.definition;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class PersistentObjectDefinition implements Definition {

    private String m_className = null;
    private List m_indexRefs = new ArrayList();

    public String getClassName() {
        return m_className;
    }

    public void setClassName(final String klass) {
        m_className = klass;
    }

    public List getIndexRefs() {
        return m_indexRefs;
    }

    public void addIndexRef(final IndexRefDefinition index) {
        m_indexRefs.add(index);
    }
}


/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Marker interface for the meta-data classes.
 *
 * TODO: align on JSR-175 API
 * TODO: CustomAttribute.class comes from def package but should be in Metadata or elsewhere generic
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public abstract class MetaData implements Serializable {

    private List m_attributes = new ArrayList();

    public List getAttributes() {
        return m_attributes;
    }

    public void addAttribute(CustomAttribute attribute) {
        m_attributes.add(attribute);
    }


}

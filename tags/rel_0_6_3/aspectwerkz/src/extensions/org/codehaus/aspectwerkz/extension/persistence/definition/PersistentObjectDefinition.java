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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.extension.persistence.definition;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: PersistentObjectDefinition.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
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


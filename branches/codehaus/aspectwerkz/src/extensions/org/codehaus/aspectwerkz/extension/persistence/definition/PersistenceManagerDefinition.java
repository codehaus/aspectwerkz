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
import java.util.Iterator;
import java.util.Properties;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: PersistenceManagerDefinition.java,v 1.1.1.1 2003-05-11 15:13:17 jboner Exp $
 */
public class PersistenceManagerDefinition implements Definition {

    public static final String DATABASE_DIR = "databaseDir";
    public static final String DATABASE_NAME = "databaseName";
    public static final String INDEX_DIR = "indexDir";

    private String m_className;
    private String m_active = "false";
    private List m_indexRefs = new ArrayList();
    private List m_parameters = new ArrayList();
    private Properties m_properties = null;

    public String getClassName() {
        return m_className;
    }

    public void setClassName(final String className) {
        m_className = className;
    }

    public String getActive() {
        return m_active;
    }

    public void setActive(final String active) {
        m_active = active;
    }

    public boolean isActive() {
        if (m_active.equals("true") || m_active.equals("yes")) {
            return true;
        }
        else {
            return false;
        }
    }

    public List getIndexRefs() {
        return m_indexRefs;
    }

    public void addIndexRef(final IndexRefDefinition index) {
        m_indexRefs.add(index);
    }

    public List getParameters() {
        return m_parameters;
    }

    public void addParameter(final ParameterDefinition parameter) {
        m_parameters.add(parameter);
    }

    public Properties getProperties() {
        if (m_properties == null) {
            m_properties = new Properties();
            for (Iterator it = m_parameters.iterator(); it.hasNext();) {
                ParameterDefinition def = (ParameterDefinition)it.next();
                m_properties.setProperty(def.getName(), def.getValue());
            }
        }
        return m_properties;
    }
}

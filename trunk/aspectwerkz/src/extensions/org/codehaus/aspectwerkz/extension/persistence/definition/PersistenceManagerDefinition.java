/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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

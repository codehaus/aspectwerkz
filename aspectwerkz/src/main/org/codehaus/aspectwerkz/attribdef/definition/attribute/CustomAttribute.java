/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

/**
 * Attribute for the generic attributes defined by the user.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CustomAttribute implements Attribute {

    private static final long serialVersionUID = -8248076796205275115L;

    /**
     * The name of the attribute.
     */
    private final String m_name;

    /**
     * The value of the attribute.
     */
    private final String m_value;

    /**
     * The the parameters passed to the attribute.
     */
//    private final String[] m_parameters;

    /**
     * Create a new CustomAttribute instance.
     *
     * @param name the name
     * @param value the value
     * @param parameters the parameters
     */
    public CustomAttribute(final String name, final String value, final String[] parameters) {
        m_name = name;
        m_value = value;

        // TODO: put the parameters pairs in a hashmap to allow easy retrieval
//        m_parameters = parameters;
    }

    /**
     * Returns the name of the attribute.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the value of the attribute.
     *
     * @return the value
     */
    public String getValue() {
        return m_value;
    }

    /**
     * Returns the parameters.
     *
     * @return the parameters
     */
//    public String[] getParameters() {
//        return m_parameters;
//    }
}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

import java.io.Serializable;

import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Attribute for the Pointcut construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PointcutAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PC_EXECUTION = "execution";
    public static final String PC_CALL = "call";
    public static final String PC_SET = "set";
    public static final String PC_GET = "get";
    public static final String PC_THROWS = "throws";
    public static final String PC_CFLOW = "cflow";

    /**
     * An array with all the valid pointcut types.
     */
    public static final String[] POINTCUT_TYPES = new String[] {
        PC_EXECUTION, PC_CALL, PC_SET, PC_GET, PC_THROWS, PC_CFLOW
    };

    /**
     * The expression for the pointcut.
     */
    private final String m_expression;

    /**
     * The type of pointcut.
     */
    private final String m_type;

    /**
     * Create an Pointcut attribute.
     *
     * @param pointcut the pointcut expression
     */
    public PointcutAttribute(final String pointcut) {
        if (pointcut == null) throw new IllegalArgumentException("pointcut expression is not valid");
        int index = pointcut.indexOf('(');
        String type = pointcut.substring(0, index);
        validateType(type);
        m_type = type;
        m_expression = pointcut.substring(index + 1, pointcut.length() - 1);
    }

    /**
     * Return the expression for the pointcut.
     *
     * @return the expression for the pointcut
     */
    public String getExpression() {
        return m_expression;
    }

    /**
     * Returns the type of the pointcut.
     *
     * @return the type of the pointcut
     */
    public String getType() {
        return m_type;
    }

    /**
     * Validates the pointcut type.
     *
     * @param type the type
     */
    private void validateType(final String type) {
        boolean valid = false;
        for (int i = 0; i < POINTCUT_TYPES.length; i++) {
            if (POINTCUT_TYPES[i].equals(type)) {
                valid = true;
            }
        }
        if (!valid) {
            throw new DefinitionException("pointcut type [" + type + "] is not a valid type");
        }
    }
}

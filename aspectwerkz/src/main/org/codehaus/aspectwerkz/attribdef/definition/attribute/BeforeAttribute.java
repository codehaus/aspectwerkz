/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

/**
 * Attribute for the Before Advice construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class BeforeAttribute extends AbstractAdviceAttribute {

    /**
     * Create an Before attribute.
     *
     * @param name the name of the advice
     * @param expression the expression for the advice
     */
    public BeforeAttribute(final String name, final String expression) {
        super(name, expression);
    }
}

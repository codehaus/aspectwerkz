/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

/**
 * Attribute for the Around Advice construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class AroundAttribute extends AbstractAdviceAttribute {

    /**
     * Create an AroundAdvice attribute.
     *
     * @param name the name of the advice
     * @param expression the pointcut for the advice
     */
    public AroundAttribute(final String name, final String expression) {
        super(name, expression);
    }
}

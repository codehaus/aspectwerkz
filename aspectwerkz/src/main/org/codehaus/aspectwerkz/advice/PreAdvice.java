/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.advice;

/**
 * Is invoked before that a specific join point (method or field) is executed.
 * <p/>
 * Supports four different deployment models:
 *     PER_JVM, PER_CLASS, PER_INSTANCE A PER_THREAD.<br/>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization A object creation is needed.
 *
 * @see aspectwerkz.DeploymentModel
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public abstract class PreAdvice extends AbstractPrePostAdvice {

    /**
     * Sets the class of the class.
     */
    public PreAdvice() {
        super();
    }
}

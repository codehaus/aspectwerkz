/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

/**
 * Interface for the mixin implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public interface Mixin {

    /**
     * Invokes the method with the index specified.
     * Invoked by methods without any parameters (slight performance gain since
     * we are saving us one array creation).
     *
     * @param methodIndex the method index
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */
    Object ___AW_invokeMixin(int methodIndex, Object callingObject);

    /**
     * Invokes an introduced method with the index specified.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */

    Object ___AW_invokeMixin(int methodIndex, Object[] parameters, Object callingObject);
}

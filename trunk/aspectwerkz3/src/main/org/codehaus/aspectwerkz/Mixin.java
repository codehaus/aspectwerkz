/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

/**
 * Interface for the mixin implementations.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface Mixin {
    /**
     * Returns the name of the mixin.
     * 
     * @return the name
     */
    String getName();

    /**
     * Returns the deployment model.
     * 
     * @return the deployment model
     */
    int getDeploymentModel();

    /**
     * Invokes the method with the index specified. Invoked by methods without any parameters (slight performance gain
     * since we are saving us one array creation).
     * 
     * @param methodIndex the method index
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */
    Object invokeMixin(int methodIndex, Object callingObject) throws Throwable;

    /**
     * Invokes an introduced method with the index specified.
     * 
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */
    Object invokeMixin(int methodIndex, Object[] parameters, Object callingObject) throws Throwable;

    /**
     * Returns the implementation class name for the mixin.
     * 
     * @return the implementation class name for the mixin
     */
    String getImplementationClassName();

    /**
     * Swaps the current introduction implementation.
     * 
     * @param className the class name of the new implementation
     */
    void swapImplementation(String className);
}
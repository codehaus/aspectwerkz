/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.advice;

import java.io.Serializable;
import java.util.Map;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.ContainerType;

/**
 * Implements the Advice concept.<br/>
 * I.e.a function object that can be defined to be invoked before,
 * after or instead of specific points in the execution flow of the program.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface Advice extends Serializable {

    /**
     * Executes the current advice A then redirects to the next
     * advice in the chain.<p/>
     * Callback method for the framework.
     *
     * @param joinPoint the join point the advice is executing at
     * @return the result from the next invocation
     */
    Object doExecute(final JoinPoint joinPoint) throws Throwable;

    /**
     * Sets the name of the advice.
     *
     * @param name the name of the advice
     */
    void setName(final String name);

    /**
     * Returns the name of the advice.
     *
     * @return the name of the advice
     */
    String getName();

    /**
     * Sets the deployment model for the advice.<br/>
     * Possible models are PER_JVM, PER_CLASS, PER_INSTANCE A PER_THREAD
     *
     * @param deploymentModel the deployment model for the advice
     */
    void setDeploymentModel(final int deploymentModel);

    /**
     * Returns the deployment model for the advice.
     *
     * @return the deployment model for the advice
     */
    int getDeploymentModel();

    /**
     * Sets the memory strategy.
     *
     * @param memoryStrategy the memory strategy
     */
    void setContainer(final AdviceContainer memoryStrategy);

    /**
     * Returns the distribution strategy.
     *
     * @return the distribution strategy
     */
    AdviceContainer getContainer();

    /**
     * Returns the distribution type.
     *
     * @return the distribution type
     */
    ContainerType getMemoryType();

    /**
     * Sets the class for the advice.
     *
     * @param adviceClass the class
     */
    void setAdviceClass(final Class adviceClass);

    /**
     * Returns the class for the advice.
     *
     * @return the class
     */
    Class getAdviceClass();

    /**
     * Sets a parameter for the advice.
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    void setParameter(final String name, final String value);

    /**
     * Returns the value of a parameter with the name specified.
     *
     * @param name the name of the parameter
     * @return the value of the parameter
     */
    String getParameter(final String name);

    /**
     * Sets the parameters for the advice.
     *
     * @param parameters the parameters as a map
     */
    void setParameters(final Map parameters);

    /**
     * Returns the parameters for the advice.
     *
     * @return the parameters as a map
     */
    Map getParameters();
}

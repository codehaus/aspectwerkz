/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
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
package org.codehaus.aspectwerkz.advice;

import java.io.Serializable;
import java.util.Map;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.MemoryType;

/**
 * Implements the Advice concept.<br/>
 * I.e.a function object that can be defined to be invoked before,
 * after or instead of specific points in the execution flow of the program.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bon�r</a>
 * @version $Id: Advice.java,v 1.1.1.1 2003-05-11 15:13:36 jboner Exp $
 */
public interface Advice extends Serializable {

    /**
     * Executes the current advice and then redirects to the next
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
     * Possible models are PER_JVM, PER_CLASS, PER_INSTANCE and PER_THREAD
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
    void setMemoryStrategy(final AdviceMemoryStrategy memoryStrategy);

    /**
     * Returns the distribution strategy.
     *
     * @return the distribution strategy
     */
    AdviceMemoryStrategy getMemoryStrategy();

    /**
     * Returns the distribution type.
     *
     * @return the distribution type
     */
    MemoryType getMemoryType();

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

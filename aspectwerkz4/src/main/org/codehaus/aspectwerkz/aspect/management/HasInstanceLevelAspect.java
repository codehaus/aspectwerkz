/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

/**
 * Interface applied to a target class when it has instance level aspects (perInstance, perThis, perTarget)
 * <p/>
 * Should <b>NEVER</b> be implemented by the user, but is applied to target classes by the weaver.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public interface HasInstanceLevelAspect {

    /**
     * Returns the instance level aspect given a specific aspect factory class, since we know that one aspect class
     * has one or more factory (due to qNames) and one factory acts for only one aspect qName.
     *
     * @param aspectFactoryClass
     * @return the aspect instance or null if no such aspect
     */
    Object aw$getAspect(Class aspectFactoryClass);
    
    /**
     * Cheks if the instance level aspect with the specific factory class was associated with the instance.
     * 
     * @param aspectFactoryClass
     * @return true in case the aspect was registers, false otherwise
     */
    boolean aw$hasAspect(Class aspectFactoryClass);

    Object aw$bindAspect(Class aspectFactoryClass, Object aspect);
}

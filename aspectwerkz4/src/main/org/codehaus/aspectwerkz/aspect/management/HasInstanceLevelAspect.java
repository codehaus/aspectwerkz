/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

/**
 * Interface throught the runtime system can retrieve instance level aspects for a specific target instance.
 * <p/>
 * Should <b>NEVER</b> be implemented by the user, but is applied to target classes by the weaver.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface HasInstanceLevelAspect {

    /**
     * Returns the instance level aspect with a specific name.
     *
     * @param aspectClassName     the class name of the aspect
     * @param qualifiedAspectName the qualified name of the aspect
     * @param containerClassName  the aspect container class name
     * @return the aspect instance
     */
    Object aw$getAspect(String aspectClassName, String qualifiedAspectName, String containerClassName);
}

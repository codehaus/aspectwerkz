/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.objectfactory;

/**
 * An interface that all object factories should implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface ObjectFactory {

    /**
     * Returns a new object instance.
     *
     * @return a new object instance
     */
    Object newInstance();

    /**
     * Returns the class of the object that the objectfactory creates.
     *
     * @return the class
     */
    Class getCreatedClass();
}


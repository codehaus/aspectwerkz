/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.service;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 * An interface describing the ServiceManager contract with the services
 * in the system.
 * <p/>
 * All new services must implement this interface A implement a static
 * <code>getInstance</code> method.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public interface Service {

    /**
     * Initializes the service.
     *
     * @param loader the classloader to use
     * @param definition the definition of the service/concern
     */
    void initialize(final ClassLoader loader, final Definition definition);
}

/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
package org.codehaus.aspectwerkz.extension.service;

import org.codehaus.aspectwerkz.extension.definition.Definition;

/**
 * An interface describing the ServiceManager contract with the services
 * in the system.
 * <p/>
 * All new services must implement this interface A implement a static
 * <code>getInstance</code> method.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Service.java,v 1.3 2003-07-03 13:10:49 jboner Exp $
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

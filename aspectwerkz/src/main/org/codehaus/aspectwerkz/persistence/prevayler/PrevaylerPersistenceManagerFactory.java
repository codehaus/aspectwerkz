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
package org.codehaus.aspectwerkz.persistence.prevayler;

import org.codehaus.aspectwerkz.persistence.PersistenceManager;
import org.codehaus.aspectwerkz.persistence.PersistenceManagerFactory;
import org.codehaus.aspectwerkz.persistence.prevayler.PrevaylerPersistenceManager;

/**
 * Factory for the Prevayler persistence manager.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bon�r</a>
 * @version $Id: PrevaylerPersistenceManagerFactory.java,v 1.1.1.1 2003-05-11 15:14:44 jboner Exp $
 */
public class PrevaylerPersistenceManagerFactory extends PersistenceManagerFactory {

    private static final PersistenceManager s_soleInstance =
            new PrevaylerPersistenceManager();

    /**
     * Returns an instance of the persistence manager.
     *
     * @return the persistence manager
     */
    public PersistenceManager createPersistenceManager() {
        s_soleInstance.initialize();
        return s_soleInstance;
    }
}

/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
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
package org.codehaus.aspectwerkz.persistence;

import org.codehaus.aspectwerkz.persistence.prevayler.PrevaylerPersistenceManagerFactory;

/**
 * Abstract class for the persistence manager factories to implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: PersistenceManagerFactory.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public abstract class PersistenceManagerFactory {

    /**
     * Constant for the prevayler persistence manager type.
     */
    public static final int PREVAYLER = 0;

    /**
     * The prevayler persistence manager factory.
     */
    private static final PersistenceManagerFactory s_prevaylerFactory
            = new PrevaylerPersistenceManagerFactory();
    /**
     * Defines the persistence type system property key.
     */
    public static final String SYSTEM_PROPERTY_KEY_PERSISTENCE_TYPE = "aspectwerkz.persistence.type";
    /**
     * Defines the prevayler system property option.
     */
    public static final String SYSTEM_PROPERTY_VALUE_PREVAYLER = "prevayler";

    /**
     * Returns a concrete factory object that is an instance of the concrete
     * factory class appropriate for the given persistence manager.
     *
     * @param type the type of persistence manager to use
     * @return the persistence manager
     */
    public static PersistenceManagerFactory getFactory(final int type) {
        switch (type) {
            case PREVAYLER:
                return s_prevaylerFactory;
            default:
                throw new IllegalArgumentException("invalid persistence manager factory type");
        }
    }

    /**
     * Returns an instance of the persistence manager.
     * To be over-ridden.
     *
     * @return the persistence manager
     */
    public abstract PersistenceManager createPersistenceManager();

    /**
     * Tries to get the persistence manager type as a system property.
     * If not found it uses the default persistence manager.
     *
     * @return the persistence manager type
     */
    public static int getPersistenceManagerType() {
        String persistenceManagerType = System.getProperty(
                SYSTEM_PROPERTY_KEY_PERSISTENCE_TYPE,
                SYSTEM_PROPERTY_VALUE_PREVAYLER);
        int type = PREVAYLER;
        if (persistenceManagerType.equals(
                SYSTEM_PROPERTY_VALUE_PREVAYLER)) {
            type = PREVAYLER;
        }
        return type;
    }
}
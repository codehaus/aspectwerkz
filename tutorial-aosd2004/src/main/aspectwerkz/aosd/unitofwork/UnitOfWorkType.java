/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork;

/**
 * Typesafe enum for the transaction types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class UnitOfWorkType {

    public static final UnitOfWorkType DEFAULT = new UnitOfWorkType("DEFAULT");
    public static final UnitOfWorkType PERSISTABLE = new UnitOfWorkType("PERSISTABLE");
    public static final UnitOfWorkType JTA_AWARE = new UnitOfWorkType("JTA_AWARE");

    private final String m_name;

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String toString() {
        return m_name;
    }

    /**
     * Private constructor.
     *
     * @param name
     */
    private UnitOfWorkType(final String name) {
        m_name = name;
    }
}

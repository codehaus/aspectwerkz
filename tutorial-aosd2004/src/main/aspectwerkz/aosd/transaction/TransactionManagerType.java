/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.transaction;

/**
 * Typesafe enum for the transaction manager types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TransactionManagerType {

    public static final TransactionManagerType JTA = new TransactionManagerType("JTA");

    private final String m_name;

    public String toString() {
        return m_name;
    }

    private TransactionManagerType(final String name) {
        m_name = name;
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.transaction;

import aspectwerkz.aosd.transaction.jta.JtaTransactionManager;

/**
 * Factory for the TransactionManager classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TransactionManagerFactory {

    /**
     * The JTA transaction manager.
     */
    private static final TransactionManager JTA = JtaTransactionManager.getInstance();

    /**
     * Creates a new transaction manager.
     *
     * @return a new transaction manager
     */
    public static TransactionManager getInstance(final TransactionManagerType type) {
        if (type.equals(TransactionManagerType.JTA)) {
            return JTA;
        }
        else {
            throw new TransactionException("transaction manager " + type.toString() + " is not supported");
        }
    }
}



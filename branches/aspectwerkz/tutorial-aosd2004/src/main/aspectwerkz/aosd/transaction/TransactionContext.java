/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.transaction;

import aspectwerkz.aosd.AbstractContext;

/**
 * The transaction context holds the transaction instance along with some
 * other information about the current transaction.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TransactionContext extends AbstractContext {

    /**
     * The transaction instance.
     */
    private final Object m_transaction;

    /**
     * The transaction manager.
     */
    private final TransactionManager m_txManager;

    /**
     * A flag marking the transaction as doomed.
     */
    private boolean m_isRollbackOnly = false;

    /**
     * Creates a new transaction context.
     *
     * @param transaction the transaction manager
     * @param txManager the transaction instance
     */
    public TransactionContext(final Object transaction, final TransactionManager txManager) {
        m_transaction = transaction;
        m_txManager = txManager;
    }

    /**
     * Returns the transaction.
     *
     * @return the transaction
     */
    public Object getTransaction() {
        return m_transaction;
    }

    /**
     * Sets the transaction to restoreObject only, e.g. marks it as doomed.
     */
    public void setRollbackOnly() {
        m_txManager.setRollbackOnly(m_transaction);
        m_isRollbackOnly = true;
    }

    /**
     * Checks if the transaction is set to restoreObject only
     *
     * @return boolean
     */
    public boolean isRollbackOnly() {
        return m_isRollbackOnly;
    }

    /**
     * Checks if a transaction is an existing transaction.
     *
     * @return booelan
     */
    public boolean isExistingTransaction() {
        return m_txManager.isExistingTransaction(m_transaction);
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.transaction;

import aspectwerkz.aosd.definition.Definition;

/**
 * Interface for the different transaction managers to implement.
 * <p/>
 * Provides a generic set of operations for transaction handling.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface TransactionManager {

    /**
     * Initializes the transaction manager.
     *
     * @param definition the definition
     */
    void initialize(Definition definition);

    /**
     * Returns the current transaction, if no transaction exists then it creates a new one.
     *
     * @return the transaction context
     * @throws TransactionException
     */
	TransactionContext getTransaction() throws TransactionException;

    /**
     * Commits the transaction.
     *
     * @param txContext the transaction context to commit
     * @throws TransactionException
     */
	void commit(TransactionContext txContext) throws TransactionException;

    /**
     * Rolls back the transaction.
     *
     * @param txContext the transaction context to be abort
     * @throws TransactionException
     */
	void rollback(TransactionContext txContext) throws TransactionException;

    /**
     * Marks the transaction as setRollbackOnly.
     *
     * @param transaction the transaction to mark as setRollbackOnly
     * @throws TransactionException
     */
    void setRollbackOnly(Object transaction) throws TransactionException;

    /**
     * Disposes the transaction specified.
     *
     * @param txContext the transaction context to be disposed
     * @throws TransactionException
     */
    void dispose(TransactionContext txContext) throws TransactionException;

    /**
     * Checks if a transaction is an existing transaction.
     *
     * @param transaction the transaction
     * @return booelan
     */
    boolean isExistingTransaction(Object transaction);
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.transaction.jta;

import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.Status;

import aspectwerkz.aosd.definition.Definition;
import aspectwerkz.aosd.transaction.TransactionManager;
import aspectwerkz.aosd.transaction.TransactionContext;
import aspectwerkz.aosd.transaction.TransactionException;

/**
 * A JTA based implementation of a {@link aspectwerkz.aosd.transaction.TransactionManager} interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JtaTransactionManager implements TransactionManager {

    /**
     * The default UserTransaction name.
     */
    public static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";

    /**
     * The sole instance of the transaction manager.
     */
    protected static final TransactionManager INSTANCE = new JtaTransactionManager();

    /**
     * Marks the manager as initialized.
     */
    protected boolean m_initialized = false;

    /**
     * Custom UserTransaction name, if the default name is not used.
     */
    protected String m_userTransactionName;

    /**
     * Returns the sole transaction manager instance.
     *
     * @return the transaction manager
     */
    public static TransactionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the transaction manager.
     *
     * @param definition the definition
     */
    public synchronized void initialize(final Definition definition) {
        if (m_initialized) return;
        m_initialized = true;
    }

    /**
     * Returns the current transaction, if no transaction exists then it creates a new one.
     *
     * @return the new transaction context
     */
    public TransactionContext getTransaction() {
        try {
            InitialContext initialContext = new InitialContext();
            UserTransaction tx = (UserTransaction)initialContext.lookup(
                    m_userTransactionName != null ?
                    m_userTransactionName :
                    DEFAULT_USER_TRANSACTION_NAME
            );
            if (!isExistingTransaction(tx)) {
                throw new TransactionException("JTA failure: could not create UserTransaction (is JNDI and a TransactionManager available?)");
            }

            tx.setTransactionTimeout(-1);
            tx.begin();

            return new TransactionContext(tx, this);
        }
        catch (NamingException ex) {
            throw new TransactionException("JTA is not available");
        }
        catch (NotSupportedException e) {
            throw new TransactionException("JTA failure: implementation does not support nested transactions");
        }
        catch (SystemException e) {
            throw new TransactionException("JTA failure: could not begin transaction");
        }
    }

    /**
     * Commits the transaction.
     *
     * @param txContext the transaction context
     * @throws TransactionException
     */
    public void commit(final TransactionContext txContext) throws TransactionException {
        UserTransaction tx = (UserTransaction)txContext.getTransaction();
        try {
            tx.commit();
        }
        catch (RollbackException e) {
            throw new TransactionException("JTA failure: transaction rolled back");
        }
        catch (HeuristicMixedException e) {
            throw new TransactionException("JTA failure: heuristic mixed");
        }
        catch (HeuristicRollbackException e) {
            throw new TransactionException("JTA failure: heuristic restoreObject");
        }
        catch (SystemException e) {
            throw new TransactionException("JTA failure: could not commit transaction");
        }
    }

    /**
     * Rolls back the transaction.
     *
     * @param txContext the transaction context
     * @throws TransactionException
     */
    public void rollback(final TransactionContext txContext) throws TransactionException {
        UserTransaction tx = (UserTransaction)txContext.getTransaction();
        try {
            tx.rollback();
        }
        catch (SystemException e) {
            throw new TransactionException("JTA failure: could not restoreObject transaction");
        }
    }

    /**
     * Disposes the transaction.
     *
     * @param txContext the transaction txContext to be disposed
     * @throws TransactionException
     */
    public void dispose(TransactionContext txContext) throws TransactionException {
    }

    /**
     * Marks the transaction as setRollbackOnly.
     *
     * @param transaction the transaction to mark as setRollbackOnly
     */
    public void setRollbackOnly(final Object transaction) {
        UserTransaction tx = (UserTransaction)transaction;
        try {
            tx.setRollbackOnly();
        }
        catch (SystemException e) {
            throw new TransactionException("JTA failure: could not mark transaction as setRollbackOnly");
        }
    }

    /**
     * Sets the JNDI name of the <tt>UserTransaction</tt>.
     *
     * @param name the JNDI name of the <tt>UserTransaction</tt>
     */
    public void setUserTransactionName(final String name) {
        m_userTransactionName = name;
    }

    /**
     * Checks if a transaction is an existing transaction.
     *
     * @param transaction the transaction
     * @return booelan
     */
    public boolean isExistingTransaction(final Object transaction) {
        try {
            return (((UserTransaction)transaction).getStatus() != Status.STATUS_NO_TRANSACTION);
        }
        catch (SystemException ex) {
            throw new TransactionException("JTA failure: could not get the status of the transaction");
        }
    }

    /**
     * Private contructor.
     */
    protected JtaTransactionManager() {
    }
}


/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork;

import aspectwerkz.aosd.transaction.TransactionManager;
import aspectwerkz.aosd.transaction.TransactionContext;
import aspectwerkz.aosd.transaction.TransactionManagerFactory;
import aspectwerkz.aosd.transaction.TransactionManagerType;

/**
 * A JTA aware implementation {@link aspectwerkz.aosd.unitofwork.UnitOfWork} that uses
 * the {@link aspectwerkz.aosd.transaction.jta.JtaTransactionManager} implementation
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JtaAwareUnitOfWork extends UnitOfWork {

    /**
     * The JTA transaction manager.
     */
    private final TransactionManager m_txManager =
            TransactionManagerFactory.getInstance(TransactionManagerType.JTA);

    /**
     * The current transaction.
     */
    private TransactionContext m_transaction;

    /**
     */
    public void doBegin() {
        m_transaction = m_txManager.getTransaction();
    }

    /**
     */
    public void doRollback() {
        m_txManager.rollback(m_transaction);
    }

    /**
     */
    public void doCommit() {
        // if the JTA transaction is set to restoreObject only; rollback and restoreObject
        // the transaction as well as the the unit of work
        if (m_transaction.isExistingTransaction()
                && m_transaction.isRollbackOnly()) {
            rollback();
            m_txManager.rollback(m_transaction);
        }
        else {
            m_txManager.commit(m_transaction);
        }
    }

    /**
     * Private constructor.
     */
    protected JtaAwareUnitOfWork() {
        super();
    }
}


/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.tutorial.tx;


import org.codehaus.aspectwerkz.annotation.Before;
import org.codehaus.aspectwerkz.annotation.AfterThrowing;
import org.codehaus.aspectwerkz.annotation.Expression;
import org.codehaus.aspectwerkz.annotation.AfterFinally;
import org.codehaus.aspectwerkz.annotation.Aspect;
import org.codehaus.aspectwerkz.definition.Pointcut;

import org.objectweb.transaction.jta.TMService;
import org.objectweb.jotm.Jotm;

import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.transaction.Status;
import javax.naming.NamingException;

/**
 * Aspect that implements a simple transaction service, supports the TransactionAttributeType.SUPPORTS semantics only.s
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
@Aspect("perJVM")
public class SimpleTransactionProtocol {

    public static final String TRANSACTED_METHODS_POINTCUT = "transactional";

    private static final TMService TM;
    static {
        try {
            TM = new Jotm(true, false);
        } catch (NamingException e) {
            throw new TransactionException(e);
        }
    }

    @Expression("execution(@javax.ejb.TransactionAttribute * *..*(..))")
    Pointcut transactional;

    @Before(TRANSACTED_METHODS_POINTCUT)
    void enterTransactedMethod() {
        try {
            UserTransaction tx = getTransaction();
            if (!isExistingTransaction(tx)) { // TransactionAttributeType.SUPPORTS
                tx.begin();
            }
        } catch (Exception e) {
            throw new TransactionException("could not begin a new transaction", e);
        }
    }

    @AfterThrowing(
            type = "java.lang.RuntimeException",
            pointcut = TRANSACTED_METHODS_POINTCUT
    )
    void exitTransactedMethodWithException() throws SystemException {
        UserTransaction tx = getTransaction();
        if (isExistingTransaction(tx)) {
            tx.setRollbackOnly();
        }
    }

    @AfterFinally(TRANSACTED_METHODS_POINTCUT)
    void exitTransactedMethod() throws Exception {
        UserTransaction tx = getTransaction();
        if (isExistingTransaction(tx)) {
            if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                tx.rollback();
            } else {
                tx.commit();
            }
        }
    }

    /**
     * Returns the user transaction.
     * <p/>
     * To be overridden by subclass if needed. F.e. to get the TX from JNDI etc.
     *
     * @return the user transaction
     */
    public UserTransaction getTransaction() {
        return TM.getUserTransaction();
    }

    /**
     * Checks if a transaction is an existing transaction.
     *
     * @param tx the user transaction
     * @return boolean
     */
    private boolean isExistingTransaction(final UserTransaction tx) {
        try {
            return (tx.getStatus() != Status.STATUS_NO_TRANSACTION);
        } catch (SystemException ex) {
            throw new TransactionException("could not get status from transaction", ex);
        }
    }
}

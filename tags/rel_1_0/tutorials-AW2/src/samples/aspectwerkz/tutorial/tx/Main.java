/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.tutorial.tx;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.Inject;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;

/**
 * Main class that shows how the different transaction attribute semantics work.
 * Prints out the steps in standard out.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class Main {

    /**
     * We make use of EJB 3 spec chpt 8 instance variable injection.
     * See the TransactionAttributeAwareTransactionProtocol aspect.
     */
    @Inject
    public UserTransaction m_userTransaction;

    // ==== top level methods ====

    @TransactionAttribute(TransactionAttributeType.REQUIRED)    
    private void startTxRequired_InvokeTxRequired() {
        logInfo("    startTxRequired_InvokeTxRequired");
        txRequired();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void startTxRequires_InvokeTxSupports_InvokeNoOpMethod_ShouldCommit() {
        logInfo("    startTxRequires_InvokeTxSupports_InvokeNoOpMethod_ShouldCommit");
        txSupports();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void startTxRequired_InvokeMethodThrowingCheckedException_ShouldCommit() throws Exception {
        logInfo("    startTxRequired_InvokeMethodThrowingCheckedException_ShouldCommit");
        throwException();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void startTxRequired_InvokeMethodThrowingUncheckedException_ShouldRollback() {
        logInfo("    startTxRequired_InvokeMethodThrowingUncheckedException_ShouldRollback");
        throwRuntimeException();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void startTxRequired_InvokeMethodWithTxNever_ShouldThrowRemoteException() {
        logInfo("    startTxRequired_InvokeMethodWithTxNever_ShouldThrowRemoteException");
        txNever();
    }

    private void startNoTx_InvokeMethodWithTxMandatory_ShouldThrowTransactionRequiredException() {
        logInfo("    startNoTx_InvokeMethodWithTxMandatory_ShouldThrowTransactionRequiredException");
        txMandatory();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void startTxRequired_InvokeTxRequiresNew_TopLevelShouldRollbackNestedShouldCommit() {
        logInfo("    startTxRequired_InvokeTxRequiresNew_TopLevelShouldRollbackNestedShouldCommit");
        txRequiresNew();
        throwRuntimeException();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void startTxRequired_InvokeTxNotSuppported_TopLevelShouldCommitEvenThoughNestedThrowsRTE() {
        logInfo("    startTxRequired_InvokeTxNotSuppported_TopLevelShouldCommitEvenThoughNestedThrowsRTE");
        try {
            txNotSupported(true);
        } catch (Throwable e) {
            logInfo("exception expected: " + e.toString());
            // assume we do not rethrow it
        }
    }

    // ==== nested methods ====

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void txRequired() {
        logInfo("        txRequired");
        noOp();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    private void txSupports() {
        logInfo("        txSupports");
        noOp();
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    private void txNever() {
        logInfo("        txNever");
        noOp();
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    private void txMandatory() {
        logInfo("        txMandatory");
        noOp();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRESNEW)
    private void txRequiresNew() {
        logInfo("        txRequiresNew");
        noOp();
    }

    @TransactionAttribute(TransactionAttributeType.NOTSUPPORTED)
    private void txNotSupported(boolean failWithRTE) {
        logInfo("        txNotSupported");
        if (failWithRTE) {
            throw new RuntimeException("txNotSupported failed with RTE");
        }
        noOp();
    }

    private void throwException() throws Exception {
        logInfo("        throwException");
        noOp();
        throw new Exception();
    }

    private void throwRuntimeException() {
        logInfo("        throwRuntimeException");
        noOp();
        throw new RuntimeException();
    }

    private void noOp() {
        logInfo("        noOp");
        printTxStatus();
    }

    public static void main(String[] args) {
        Main main = new Main();

        logInfo("\n-------------------------------");
        main.startTxRequires_InvokeTxSupports_InvokeNoOpMethod_ShouldCommit();

        logInfo("\n-------------------------------");
        try {
            main.startTxRequired_InvokeMethodThrowingCheckedException_ShouldCommit();
        } catch (Exception e) {
        }

        logInfo("\n-------------------------------");
        try {
            main.startTxRequired_InvokeMethodThrowingUncheckedException_ShouldRollback();
        } catch (RuntimeException e) {
        }

        logInfo("\n-------------------------------");
        try {
            main.startTxRequired_InvokeMethodWithTxNever_ShouldThrowRemoteException();
        } catch (Throwable e) {
            logInfo("exception expected: " + e.toString());
        }

        logInfo("\n-------------------------------");
        try {
            main.startNoTx_InvokeMethodWithTxMandatory_ShouldThrowTransactionRequiredException();
        } catch (Throwable e) {
            logInfo("exception expected: " + e.toString());
        }

        logInfo("\n-------------------------------");
        try {
            main.startTxRequired_InvokeTxRequiresNew_TopLevelShouldRollbackNestedShouldCommit();
        } catch (Throwable e) {
            logInfo("exception expected: " + e.toString());
        }

        logInfo("\n-------------------------------");
        try {
            main.startTxRequired_InvokeTxNotSuppported_TopLevelShouldCommitEvenThoughNestedThrowsRTE();
        } catch (Throwable e) {
            logInfo("exception expected: " + e.toString());
        }

        logInfo("\n-------------------------------");
        main.startTxRequired_InvokeTxRequired();


        System.exit(0);
    }

    private void printTxStatus() {
        if (m_userTransaction == null) {
            logInfo("TX status: STATUS_NO_TRANSACTION");
        } else {
            try {
                switch (m_userTransaction.getStatus()) {
                    case Status.STATUS_COMMITTED:
                        logInfo("TX status: STATUS_COMMITTED");
                        break;
                    case Status.STATUS_COMMITTING:
                        logInfo("TX status: STATUS_COMMITTING");
                        break;
                    case Status.STATUS_ACTIVE:
                        logInfo("TX status: STATUS_ACTIVE");
                        break;
                    case Status.STATUS_MARKED_ROLLBACK:
                        logInfo("TX status: STATUS_MARKED_ROLLBACK");
                        break;
                    case Status.STATUS_NO_TRANSACTION:
                        logInfo("TX status: STATUS_NO_TRANSACTION");
                        break;
                    case Status.STATUS_PREPARED:
                        logInfo("TX status: STATUS_PREPARED");
                        break;
                    case Status.STATUS_PREPARING:
                        logInfo("TX status: STATUS_PREPARING");
                        break;
                    case Status.STATUS_ROLLEDBACK:
                        logInfo("TX status: STATUS_ROLLEDBACK");
                        break;
                    case Status.STATUS_ROLLING_BACK:
                        logInfo("TX status: STATUS_ROLLING_BACK");
                        break;
                    case Status.STATUS_UNKNOWN:
                        logInfo("TX status: STATUS_UNKNOWN");
                        break;
                }
            } catch (SystemException e) {
                logInfo("TX status: fault : " + e.toString());
            }
        }
    }

    private static void logInfo(final String message) {
        System.out.println("[Main:INFO] " + message);
    }
}

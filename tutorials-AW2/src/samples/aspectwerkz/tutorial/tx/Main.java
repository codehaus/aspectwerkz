/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.tutorial.tx;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.SystemException;
import javax.transaction.Status;

import org.codehaus.aspectwerkz.aspect.management.Aspects;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class Main {

    // ==== top level methods ====

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    private void startTxSupports_InvokeNoOpMethod_ShouldCommit() {
        System.out.println("startTxSupports_InvokeNoOpMethod_ShouldCommit");
        noOp();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void startTxRequired_InvokeMethodThrowingCheckedException_ShouldCommit() throws Exception {
        System.out.println("startTxRequired_InvokeMethodThrowingCheckedException_ShouldCommit");
        throwException();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void startTxRequired_InvokeMethodThrowingUncheckedException_ShouldRollback() {
        System.out.println("startTxRequired_InvokeMethodThrowingUncheckedException_ShouldRollback");
        throwRuntimeException();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void startTxRequired_InvokeMethodWithTxNever_ShouldThrowTxException() {
        System.out.println("startTxRequired_InvokeMethodWithTxNever_ShouldThrowTxException");
        txNever();
    }

    private void startNoTx_InvokeMethodWithTxMandatory_ShouldThrowTxException() {
        System.out.println("startNoTx_InvokeMethodWithTxMandatory_ShouldThrowTxException");
        txMandatory();
    }

    // ==== nested methods ====

    @TransactionAttribute(TransactionAttributeType.NEVER)
    private void txNever() {
        System.out.println("    txNever");
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    private void txMandatory() {
        System.out.println("    txMandatory");
    }

    private void throwException() throws Exception {
        System.out.println("    throwException");
        throw new Exception();
    }

    private void throwRuntimeException() {
        System.out.println("    throwRuntimeException");
        throw new RuntimeException();
    }

    private void noOp() {
        System.out.println("    noOp");
    }

    public static void main(String[] args) {
        Main main = new Main();

        System.out.println("-------------------------------");
        main.startTxSupports_InvokeNoOpMethod_ShouldCommit();

        System.out.println("-------------------------------");
        try {
            main.startTxRequired_InvokeMethodThrowingCheckedException_ShouldCommit();
        } catch (Exception e) {
        }

        System.out.println("-------------------------------");
        try {
            main.startTxRequired_InvokeMethodThrowingUncheckedException_ShouldRollback();
        } catch (RuntimeException e) {
        }

        System.out.println("-------------------------------");
        try {
            main.startTxRequired_InvokeMethodWithTxNever_ShouldThrowTxException();
        } catch (TransactionException e) {
            System.out.println("INFO - catched expected TransactionException: " + e.getMessage());
        }

        System.out.println("-------------------------------");
        try {
            main.startNoTx_InvokeMethodWithTxMandatory_ShouldThrowTxException();
        } catch (TransactionException e) {
            System.out.println("INFO - catched expected TransactionException: " + e.getMessage());
        }

        System.exit(0);
    }

    public static void printTxStatus() {
        try {
            TransactionAttribyteTypeAwareTransactionProtocol txAspect =
                    (TransactionAttribyteTypeAwareTransactionProtocol)
                    Aspects.aspectOf(TransactionAttribyteTypeAwareTransactionProtocol.class);
            switch (txAspect.getTransaction().getStatus()) {
                case Status.STATUS_COMMITTED:
                    System.out.println("        STATUS_COMMITTED");
                    break;
                case Status.STATUS_COMMITTING:
                    System.out.println("        STATUS_COMMITTING");
                    break;
                case Status.STATUS_ACTIVE:
                    System.out.println("        STATUS_ACTIVE");
                    break;
                case Status.STATUS_MARKED_ROLLBACK:
                    System.out.println("        STATUS_MARKED_ROLLBACK");
                    break;
                case Status.STATUS_NO_TRANSACTION:
                    System.out.println("        STATUS_NO_TRANSACTION");
                    break;
                case Status.STATUS_PREPARED:
                    System.out.println("        STATUS_PREPARED");
                    break;
                case Status.STATUS_PREPARING:
                    System.out.println("        STATUS_PREPARING");
                    break;
                case Status.STATUS_ROLLEDBACK:
                    System.out.println("        STATUS_ROLLEDBACK");
                    break;
                case Status.STATUS_ROLLING_BACK:
                    System.out.println("        STATUS_ROLLING_BACK");
                    break;
                case Status.STATUS_UNKNOWN:
                    System.out.println("        STATUS_UNKNOWN");
                    break;
            }
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

}

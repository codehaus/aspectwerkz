/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.tutorial.tx;

import static javax.ejb.TransactionAttributeType.*;

import org.codehaus.aspectwerkz.annotation.Before;
import org.codehaus.aspectwerkz.annotation.AfterThrowing;
import org.codehaus.aspectwerkz.annotation.Expression;
import org.codehaus.aspectwerkz.annotation.AfterFinally;
import org.codehaus.aspectwerkz.annotation.Aspect;
import org.codehaus.aspectwerkz.definition.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;

import org.objectweb.transaction.jta.TMService;
import org.objectweb.jotm.Jotm;

import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionAttribute;
import javax.transaction.SystemException;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.TransactionRequiredException;
import javax.naming.NamingException;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

/**
 * <p>
 * Aspect that implements a simple transaction service that obeys the transaction semantics defined
 * in the transaction attribute types for the transacted methods.
 * </p>
 *
 * <p>
 * <h3>Transaction attribute semantics</h3>
 * (From http://www.kevinboone.com/ejb-transactions.html)
 *
 * <h4>Required</h4>
 * `Required' is probably the best choice (at least initially) for an EJB method that will need to be transactional. In this case, if the method's caller is already part of a transaction, then the EJB method does not create a new transaction, but continues in the same transaction as its caller. If the caller is not in a transaction, then a new transaction is created for the EJB method. If something happens in the EJB that means that a rollback is required, then the extent of the rollback will include everything done in the EJB method, whatever the condition of the caller. If the caller was in a transaction, then everything done by the caller will be rolled back as well. Thus the `required' attribute ensures that any work done by the EJB will be rolled back if necessary, and if the caller requires a rollback that too will be rolled back.
 *
 * <h4>RequiresNew</h4>
 * `RequiresNew' will be appropriate if you want to ensure that the EJB method is rolled back if necessary, but you don't want the rollback to propogate back to the caller. This attribute results in the creation of a new transaction for the method, regardless of the transactional state of the caller. If the caller was operating in a transaction, then its transaction is suspended until the EJB method completes. Because a new transaction is always created, there may be a slight performance penalty if this attribute is over-used.
 *
 * <h4>Mandatory</h4>
 * With the `mandatory' attribute, the EJB method will not even start unless its caller is in a transaction. It will throw a <code>TransactionRequiredException</code> instead. If the method does start, then it will become part of the transaction of the caller. So if the EJB method signals a failure, the caller will be rolled back as well as the EJB.
 *
 * <h4>Supports</h4>
 * With this attribute, the EJB method does not care about the transactional context of its caller. If the caller is part of a transaction, then the EJB method will be part of the same transaction. If the EJB method fails, the transaction will roll back. If the caller is not part of a transaction, then the EJB method will still operate, but a failure will not cause anything to roll back. `Supports' is probably the attribute that leads to the fastest method call (as there is no transactional overhead), but it can lead to unpredicatable results. If you want a method to be isolated from transactions, that is, to have no effect on the transaction of its caller, then use `NotSupported' instead.
 *
 * <h4>NotSupported</h4>
 * With the `NotSupported' attribute, the EJB method will never take part in a transaction. If the caller is part of a transaction, then the caller's transaction is suspended. If the EJB method fails, there will be no effect on the caller's transaction, and no rollback will occur. Use this method if you want to ensure that the EJB method will not cause a rollback in its caller. This is appropriate if, for example, the method does something non-essential, such as logging a message. It would not be helpful if the failure of this operation caused a transaction rollback.
 *
 * <h4>Never</h4>
 * The `NotSupported' attribute will ensure that the EJB method is never called by a transactional caller. Any attempt to do so will result in a <code>RemoteException</code> being thrown. This attribute is probably less useful than `NotSupported', in that NotSupported will assure that the caller's transaction is never affected by the EJB method (just as `Never' does), but will allow a call from a transactional caller if necessary.
 * </p>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
@Aspect("perJVM")
public class TransactionAttribyteTypeAwareTransactionProtocol {

    public static final String TRANSACTED_METHODS_POINTCUT = "transactional";
    private static final String SUSPENDED_TRANSACTION = "SUSPENDED_TRANSACTION";
    private static final TMService TM;
    static {
        try {
            TM = new Jotm(true, false);
        } catch (NamingException e) {
            throw new TransactionException("Could not create a new JOTM Transaction Manager Service", e);
        }
    }

    @Expression("execution(@javax.ejb.TransactionAttribute * *..*(..))")
    Pointcut transactional;

    /**
     * Invoked when entering a transacted method. Handles the different transaction attribute semantics and
     * depending on these: starts a new transaction, suspends the current transaction or attaches itself to the
     * existing transaction.
     *
     * @param jp the static join point instance
     * @throws Throwable
     */
    @Before(TRANSACTED_METHODS_POINTCUT)
    void enterTransactedMethod(final StaticJoinPoint jp) throws Throwable {

        final MethodSignature sig = (MethodSignature)jp.getSignature();
        final Class declaringType = sig.getDeclaringType();
        final Method method = sig.getMethod();
        final TransactionAttributeType txType = getTransactionTypeFor(declaringType, method);

        switch(txType) {
            case REQUIRED:
                logInfo("Starts TX with attribute REQUIRED at [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                if (!hasExistingTransaction()) {
                    getTransactionManager().begin();
                }
                break;

            case REQUIRESNEW:
                logInfo("Starts TX with attribute REQUIRESNEW at [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                final TransactionManager tm = getTransactionManager();
                if (hasExistingTransaction()) {
                    Transaction suspendedTx = tm.suspend();
                    jp.addMetaData(SUSPENDED_TRANSACTION, suspendedTx);
                }
                tm.begin();
                break;

            case SUPPORTS:
                // attach to current if exists else skip -> do nothing
                logInfo("Entering method with TX attribute SUPPORTS [" + declaringType.getName() + '.' + method.getName() + ']');
                break;

            case MANDATORY:
                logInfo("Entering method with TX attribute MANDATORY [" + declaringType.getName() + '.' + method.getName() + ']');
                if (!hasExistingTransaction()) {
                    throw new TransactionRequiredException("No active TX at method with TX type set to MANDATORY [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                }
                break;

            case NEVER:
                logInfo("Entering method with TX attribute NEVER [" + declaringType.getName() + '.' + method.getName() + ']');
                if (hasExistingTransaction()) {
                    throw new RemoteException("Detected active TX at method with TX type set to NEVER [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                }
                break;

            case NOTSUPPORTED:
                logInfo("Entering method with TX attribute NOTSUPPORTED [" + declaringType.getName() + '.' + method.getName() + ']');
                if (hasExistingTransaction()) {
                    Transaction suspendedTx = getTransactionManager().suspend();
                    jp.addMetaData(SUSPENDED_TRANSACTION, suspendedTx);
                }
                break;
        }
    }

    /**
     * Invoked when an unchecked exception is thrown out of a transacted method. Marks the current transaction
     * as ROLLBACK_ONLY.
     *
     * @throws Throwable
     */
    @AfterThrowing(
            type = "java.lang.RuntimeException",
            expression = TRANSACTED_METHODS_POINTCUT
    )
    void exitTransactedMethodWithException() throws Throwable {
        TransactionManager tm = getTransactionManager();
        if (hasExistingTransaction()) {
            logInfo("Setting TX to rollback only");
            tm.setRollbackOnly();
        }
    }

    /**
     * Invoked when exiting a transacted method. Performs the actual commit or rollback depending on the
     * status of the transaction. Resumes suspended transactions.
     *
     * @param jp the static join point instance
     * @throws Throwable
     */
    @AfterFinally(TRANSACTED_METHODS_POINTCUT)
    void exitTransactedMethod(final StaticJoinPoint jp) throws Throwable {
        TransactionManager tm = getTransactionManager();
        if (hasExistingTransaction()) {
            if (tm.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                logInfo("Rolling back TX marked as ROLLBACK");
                tm.rollback();
            } else {
                logInfo("Committing TX");
                tm.commit();
            }
        }
        // handle REQUIRESNEW
        Object suspendedTx = jp.getMetaData(SUSPENDED_TRANSACTION);
        if (suspendedTx != null) {
            tm.resume((Transaction)suspendedTx);
        }
    }

    /**
     * Returns the current transaction.
     * <p/>
     * To be overridden by subclass if needed. F.e. to get the TX from JNDI etc.
     *
     * @return the current transaction
     */
    public static Transaction getTransaction() {
        try {
            return getTransactionManager().getTransaction();
        } catch (SystemException e) {
            throw new TransactionException("Could not retrieve current transaction", e);
        }
    }

    /**
     * Returns the status of the current transaction.
     *
     * @return status of the current transaction
     */
    public static int getTransactionStatus() {
        try {
            return getTransactionManager().getStatus();
        } catch (SystemException e) {
            throw new TransactionException("Could not get status of current transaction", e);
        }
    }

    /**
     * Returns the transaction attribute type for a specific method.
     *
     * @param klass
     * @param method
     * @return the TX type
     */
    public static TransactionAttributeType getTransactionTypeFor(final Class klass, final Method method) {
        TransactionAttribute tx = method.getAnnotation(TransactionAttribute.class);
        if (tx == null) {
            tx = (TransactionAttribute) klass.getAnnotation(TransactionAttribute.class);
        }
        if (tx != null) {
            return tx.value();
        } else {
            return REQUIRED; // default for CMT components - EJB3/9.4
        }
    }

    /**
     * Returns the transaction manager.
     * <p/>
     * To be overridden by subclass if needed. F.e. to get the TX from JNDI etc.
     *
     * @return the transaction manager
     */
    protected static TransactionManager getTransactionManager() {
        return TM.getTransactionManager();
    }

    /**
     * Checks if a transaction is an existing transaction.
     *
     * @return boolean
     */
    protected static boolean hasExistingTransaction() {
        return getTransactionStatus() != Status.STATUS_NO_TRANSACTION;
    }

    public static void logInfo(final String message) {
        System.out.println("[TransactionProtocol:INFO] " + message);
    }
}

/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.annotation.Around;
import org.codehaus.aspectwerkz.definition.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.joinpoint.FieldSignature;

import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionAttribute;
import javax.transaction.SystemException;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.TransactionRequiredException;
import javax.transaction.UserTransaction;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

/**
 * <p>
 * Abstract aspect that implements a JTA transaction service that obeys the transaction semantics defined
 * in the transaction attribute types for the transacted methods according to the EJB 3 draft specification.
 * The aspect handles UserTransaction, TransactionManager instance variable injection thru @javax.ejb.Inject
 * (name subject to change as per EJB 3 spec) and method transaction levels thru @javax.ejb.TransactionAttribute.
 * </p>
 *
 * <p>
 * This aspects should be inherited to implement the getTransactionManager() method that should return a concrete
 * javax.transaction.TransactionManager implementation (from JNDI lookup etc).
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
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Aspect("perJVM")
public abstract class TransactionAttributeAwareTransactionProtocol {

    // pointcut fields names (optional, we can refer to field name directly)
    public static final String TRANSACTED_METHODS_POINTCUT = "transactedMethods";
    private final static String INJECTED_USERTRANSACTION_POINTCUT = "injectedUserTransaction";

    /**
     * A ThreadLocal variable where to store suspended TX and enable pay as you go
     * before advice - after advice data sharing in a specific case of requiresNew TX
     */
    private ThreadLocal m_suspendedTxTL = new ThreadLocal() {
        public Object initialValue() {
            return null;
        }
    };

    /**
     * The pointcut that picks out all transacted methods.
     * As per EJB 3 specification, we should pick all business method from an EJB bean annotated with a
     * class level TransactionAttribute as well:
     * <pre>
     * execution(@javax.ejb.TransactionAttribute * *.*(..))
     *  ||
     * (execution(* *.*(..)) && within(@javax.ejb.TransactionAttribute *))
     * </pre>
     * Note that this pointcut should be narrowed down to some EJB bean annotated classes using a within(...) pointcut.
     */
    @Expression("execution(@javax.ejb.TransactionAttribute * *.*(..))")
    Pointcut transactedMethods;

    /**
     * The pointcut that picks out all insntance variable injections.
     * The @Inject annotation as been simplified in this sample.
     * Note that this pointcut should be narrowed down to some EJB bean annotated classes using a within(...) pointcut.
     * </p>
     * We limit this pointcut to UserTransaction exact type. The specification does not describe what to do
     * if we were to inject a WebLogicuserTransaction (casting or field type subclass of UserTransaction allowed ?)
     * In such a case we would have to write javax.transaction.UserTransaction+ to match on subclass as well.
     */
    @Expression("get(@javax.ejb.Inject javax.transaction.UserTransaction *)")
    Pointcut injectedUserTransaction;

    /**
     * Around advice on @Inject instance variables access that will only resolve
     * UserTransaction type.
     * </p>
     * Note: we don't call StaticJoinPoint.proceed() so it may shortcut other aspects. We don't have other aspect here
     * but this might be worse considering.
     */
    @Around(INJECTED_USERTRANSACTION_POINTCUT)
    public Object resolveUserTransactionInjection() throws Throwable {
        logInfo("Accessing injected UserTransaction");
        return getUserTransaction();
    }

    /**
     * Invoked when entering a transacted method. Handles the different transaction attribute semantics and
     * depending on these: starts a new transaction, suspends the current transaction or attaches itself to the
     * existing transaction.
     *
     * @param jp the static join point instance
     * @throws Throwable
     */
    @Before(TRANSACTED_METHODS_POINTCUT)
    public void enterTransactedMethod(final StaticJoinPoint jp) throws Throwable {

        final MethodSignature sig = (MethodSignature)jp.getSignature();
        final Class declaringType = sig.getDeclaringType();        final Method method = sig.getMethod();
        final TransactionAttributeType txType = getTransactionAttributeTypeFor(declaringType, method);

        final TransactionManager tm = getTransactionManager();

        switch(txType) {
            case REQUIRED:
                logInfo("Starts TX with attribute REQUIRED at [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                if (!isExistingTransaction(tm)) {
                    logInfo("  TX begin");
                    tm.begin();
                }
                break;

            case REQUIRESNEW:
                logInfo("Starts TX with attribute REQUIRESNEW at [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                if (isExistingTransaction(tm)) {
                    logInfo("  TX suspend");
                    Transaction suspendedTx = tm.suspend();
                    storeInThreadLocal(suspendedTx);
                }
                logInfo("  TX begin");
                tm.begin();
                break;

            case SUPPORTS:
                // attach to current if exists else skip -> do nothing
                logInfo("Entering method with TX attribute SUPPORTS [" + declaringType.getName() + '.' + method.getName() + ']');
                break;

            case MANDATORY:
                logInfo("Entering method with TX attribute MANDATORY [" + declaringType.getName() + '.' + method.getName() + ']');
                if (!isExistingTransaction(tm)) {
                    throw new TransactionRequiredException("No active TX at method with TX type set to MANDATORY [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                }
                break;

            case NEVER:
                logInfo("Entering method with TX attribute NEVER [" + declaringType.getName() + '.' + method.getName() + ']');
                if (isExistingTransaction(tm)) {
                    throw new RemoteException("Detected active TX at method with TX type set to NEVER [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                }
                break;

            case NOTSUPPORTED:
                logInfo("Entering method with TX attribute NOTSUPPORTED [" + declaringType.getName() + '.' + method.getName() + ']');
                if (isExistingTransaction(tm)) {
                    logInfo("  TX suspend");
                    Transaction suspendedTx = tm.suspend();
                    storeInThreadLocal(suspendedTx);
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
            pointcut = TRANSACTED_METHODS_POINTCUT
    )
    public void exitTransactedMethodWithException(StaticJoinPoint jp) throws Throwable {
        final TransactionManager tm = getTransactionManager();
        if (isExistingTransaction(tm)) {
            logInfo("Setting TX to ROLLBACK_ONLY");
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
    public void exitTransactedMethod(final StaticJoinPoint jp) throws Throwable {
        final TransactionManager tm = getTransactionManager();
        if (isExistingTransaction(tm)) {
            if (isRollbackOnly(tm)) {
                logInfo("Rolling back TX marked as ROLLBACK_ONLY");
                tm.rollback();
            } else {
                logInfo("Committing TX");
                tm.commit();
            }
        }
        // handle REQUIRESNEW
        Transaction suspendedTx = fetchFromThreadLocal();
        if (suspendedTx != null) {
            logInfo("Resuming TX");
            tm.resume(suspendedTx);
            storeInThreadLocal(null);
        }
    }

    /**
     * Returns the transaction manager.
     * <p/>
     * To be overridden by subclass. F.e. to get the TX from JNDI etc.
     *
     * @return the transaction manager
     */
    protected abstract TransactionManager getTransactionManager();

    /**
     * Returns the user transaction.
     * <p/>
     * To be overridden by subclass.
     *
     * @return the user transaction within the caller thread context
     */
    protected abstract UserTransaction getUserTransaction();

    //--- Helper methods

    private void storeInThreadLocal(Transaction tx) {
        m_suspendedTxTL.set(tx);
    }

    private Transaction fetchFromThreadLocal() {
        if (m_suspendedTxTL != null && m_suspendedTxTL.get() != null) {
            return (Transaction)m_suspendedTxTL.get();
        } else {
            return null;
        }
    }

    /**
     * Returns the transaction attribute type for a specific method.
     *
     * @param klass
     * @param method
     * @return the TX type
     */
    public static TransactionAttributeType getTransactionAttributeTypeFor(final Class klass, final Method method) {
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
     * Checks if a transaction is an existing transaction.
     *
     * @param tm the transaction manager
     * @return boolean
     */
    protected static boolean isExistingTransaction(final TransactionManager tm) throws SystemException {
        return tm.getStatus() != Status.STATUS_NO_TRANSACTION;
    }

    /**
     * Checks if current transaction is set to rollback only.
     *
     * @param tm the transaction manager
     * @return boolean
     */
    protected static boolean isRollbackOnly(final TransactionManager tm) throws SystemException {
        return tm.getStatus() == Status.STATUS_MARKED_ROLLBACK;
    }

    /**
     * Prints log messages to standard out.
     *
     * @param message the message
     */
    public static void logInfo(final String message) {
        System.out.println("[TransactionProtocol:INFO] " + message);
    }
}

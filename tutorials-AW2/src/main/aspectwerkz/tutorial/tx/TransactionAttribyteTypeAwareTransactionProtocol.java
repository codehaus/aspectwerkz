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
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.transaction.Status;
import javax.naming.NamingException;

import java.lang.reflect.Method;

/**
 * Aspect that implements a simple transaction service that obeys the transaction semantics defined
 * in the transaction attribute types for the transacted methods.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
@Aspect("perJVM")
public class TransactionAttribyteTypeAwareTransactionProtocol {

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
    void enterTransactedMethod(StaticJoinPoint jp) {
        MethodSignature sig = (MethodSignature)jp.getSignature();
        final Class declaringType = sig.getDeclaringType();
        final Method method = sig.getMethod();
        TransactionAttributeType txType = getTransactionTypeFor(declaringType, method);
        UserTransaction tx = getTransaction();
        try {
            switch(txType) {
                case REQUIRED:
                    tx.begin();
                    logInfo("begin TX REQUIRED at [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                    break;

                case REQUIRESNEW:
                    logInfo("begin TX REQUIRESNEW at [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                    // FIXME - spawn in new thread?
                    break;
                case SUPPORTS:
                    logInfo("TX SUPPORTS at [" + declaringType.getName() + '.' + method.getName() + ']');
                    // attach to current if exists else skip -> do nothing
                    break;
                case MANDATORY:
                    if (!isExistingTransaction(tx)) {
                        throw new TransactionException("No active TX at method with TX type set to MANDATORY [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                    }
                    break;
                case NEVER:
                    if (isExistingTransaction(tx)) {
                        throw new TransactionException("Detected active TX at method with TX type set to NEVER [" + declaringType.getName() + '.' + method.getName() + "(..)]");
                    }
                    break;
                case NOTSUPPORTED:
                    // FIXME - what todo here?
                    break;
            }
        } catch (Exception e) {
            throw new TransactionException(e);
        }
    }

    @AfterThrowing(
            type = "java.lang.RuntimeException",
            expression = TRANSACTED_METHODS_POINTCUT
    )
    void exitTransactedMethodWithException() throws SystemException {
        UserTransaction tx = getTransaction();
        if (isExistingTransaction(tx)) {
            logInfo("setting TX to rollback only (RuntimeException was thrown)");
            tx.setRollbackOnly();
        }
    }

    @AfterFinally(TRANSACTED_METHODS_POINTCUT)
    void exitTransactedMethod() throws Exception {
        UserTransaction tx = getTransaction();
        if (isExistingTransaction(tx)) {
            if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                logInfo("TX marked as ROLLBACK -  rolling back TX");
                tx.rollback();
            } else {
                logInfo("committing TX");
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

    /**
     * Returns the transaction attribute type for a specific method.
     *
     * @param klass
     * @param method
     * @return the TX type
     */
    private TransactionAttributeType getTransactionTypeFor(final Class klass, final Method method) {
        TransactionAttribute tx = method.getAnnotation(TransactionAttribute.class);
        if (tx == null) {
            tx = (TransactionAttribute) klass.getAnnotation(TransactionAttribute.class);
        }
        if (tx != null) {
            return tx.value();
        } else {
            return REQUIRED; // defaults assumed for CMT components - EJB3/9.4
        }
    }

    public static void logInfo(final String message) {
        System.out.println("INFO - " + message);
    }
}

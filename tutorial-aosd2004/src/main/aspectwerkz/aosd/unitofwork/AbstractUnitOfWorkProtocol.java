/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork;

import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;

import aspectwerkz.aosd.unitofwork.UnitOfWork;
import aspectwerkz.aosd.transaction.TransactionManager;
import aspectwerkz.aosd.transaction.TransactionManagerFactory;
import aspectwerkz.aosd.transaction.TransactionManagerType;
import aspectwerkz.aosd.transaction.TransactionContext;

import java.io.Serializable;

/**
 * Specifies the transactional behaviour in the system.
 *
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractUnitOfWorkProtocol extends Aspect {

    // ======== Pointcuts ===============================================================

    /**
     * To be defined by the subclass.
     * <p/>
     * Defines all the fields in all objects that should participate in the transaction,
     * e.g. which ones we want to be notified by when they become dirty.
     */
    Pointcut transactionalObjectCreationPoints;

    /**
     * To be defined by the subclass.
     * <p/>
     * Defines all the fields in all objects that should participate in the transaction,
     * e.g. which ones we want to be notified by when they become dirty.
     */
    Pointcut transactionalObjectModificationPoints;

    /**
     * To be defined by the subclass.
     * <p/>
     * Defines the methods that start a new transaction.
     */
    Pointcut transactionalMethods;

    // ======== Advices ===============================================================

    /**
     * Registes the object as new right after it has been created.
     *
     * @Around transactionalObjectCreationPoints
     */
    public Object registerNew(final JoinPoint joinPoint) throws Throwable {
        Object newInstance = joinPoint.proceed();
        if (UnitOfWork.isInUnitOfWork()) {
            UnitOfWork unitOfWork = UnitOfWork.getCurrent();
            unitOfWork.registerNew(newInstance);
        }
        return newInstance;
    }

    /**
     * Make a backup of the object just before an object becomes dirty.
     *
     * @Before transactionalObjectModificationPoints
     */
    public void registerDirty(final JoinPoint joinPoint) throws Throwable {
        if (UnitOfWork.isInUnitOfWork()) {
            FieldJoinPoint jp = (FieldJoinPoint)joinPoint;
            UnitOfWork unitOfWork = UnitOfWork.getCurrent();
            unitOfWork.registerDirty(
                    jp.getTargetInstance(),
                    jp.getFieldName()
            );
        }
    }

    /**
     * Starts a new transaction before proceeding with the method invocation.
     * <p/>Commits the transaction if the method completes succesfully.
     * <p/>Rolls back the transaction if either an exception (of the right type) is thrown or
     * setRollBackOnly is set for the UnitOfWork.
     *
     * @Around transactionalMethods
     */
    public Object proceedInTransaction(final JoinPoint joinPoint) throws Throwable {
        if (UnitOfWork.isInUnitOfWork()) {
            return joinPoint.proceed(); // proceed in the current UnitOfWork
        }
        final UnitOfWork unitOfWork = UnitOfWork.begin();
        final Object result;
        try {
            result = joinPoint.proceed();
            if (unitOfWork.isRollbackOnly()) {
                unitOfWork.rollback();
            }
            else {
                unitOfWork.commit();
            }
        }
        catch (final Throwable e) {
            throw handleException(e, unitOfWork);
        }
        finally {
            UnitOfWork.dispose();
        }
        return result;
    }

    /**
     * Handles exceptions that are thrown within the UnitOfWork.
     *
     * @param e the exception
     * @param unitOfWork the unit of work
     * @return the exception
     */
    private Throwable handleException(final Throwable e, final UnitOfWork unitOfWork) {
        if (e instanceof RuntimeException) { // should be configurable
            unitOfWork.rollback();
        }
        else {
            unitOfWork.commit();
        }
        return e;
    }

    // ======== Introductions ===============================================================

    /**
     * Mixin that implements the <tt>Transactional</tt> and the <tt>Serializable</tt>
     * interfaces.
     * <p/>Provides life-cycle methods for the objects that should participate
     * in the UnitOfWork as well as convenience methods for retrieving the
     * UnitOfWork or Transaction, setting rollback only only etc.
     *
     * @Introduce TO_BE_DEFINED deploymentModel=perInstance
     */
    public abstract class TransactionalImpl implements Transactional, Serializable {

        /**
         * The transaction manager.
         * @todo make configurable
         */
        private final TransactionManager m_txManager =
                TransactionManagerFactory.getInstance(TransactionManagerType.JTA);

        /**
         * The name of the mixin (needed to lookup the target instance
         * for this mixin).
         */
        private final String m_name = this.getClass().getName();

        /**
         * Stores the persistable object in persistent storage.
         */
        public void create() {
            Object obj = ___AW_getMixinTargetInstance(m_name, this);
            UnitOfWork.registerNew(obj);
        }

        /**
         * Removes the persistable object from persistent storage.
         */
        public void remove() {
            Object obj = ___AW_getMixinTargetInstance(m_name, this);
            UnitOfWork.registerRemoved(obj);
        }

        /**
         * Checks if the persistable object exists in the persistent storage.
         *
         * @return true if the object exists in the persistent storage
         */
        public boolean exists() {
            Object obj = ___AW_getMixinTargetInstance(m_name, this);
            return UnitOfWork.exists(obj);
        }

        /**
         * Marks the persistable object as dirty.
         * <p/>Is handled automatically by the framework but this method
         * exists for the users to be able to do it explicitly.
         */
        public void markDirty() {
            Object obj = ___AW_getMixinTargetInstance(m_name, this);
            UnitOfWork.registerDirty(obj);
        }

        /**
         * Clones the persistable object.
         *
         * @return a clone of the persistable object
         */
        public Object clone() {
            // to be implemented
            return null;
        }

        /**
         * Marks the current UnitOfWork and attached transaction as rollback only.
         */
        public void setRollbackOnly() {
            UnitOfWork.setRollbackOnly();
            m_txManager.getTransaction().setRollbackOnly();
        }

        /**
         * Returns the current transaction.
         *
         * @return the current transaction
         */
        public TransactionContext getTransaction() {
            return m_txManager.getTransaction();
        }

        /**
         * Returns the current UnitOfWork.
         *
         * @return the current UnitOfWork
         */
        public UnitOfWork getUnitOfWork() {
            return UnitOfWork.getCurrent();
        }
    }
}

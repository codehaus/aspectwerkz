/**************************************************************************************
 * Copyright (c) Jonas B+on�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.aspect;

import aspectwerkz.aosd.unitofwork.AbstractUnitOfWorkProtocol;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * Defines the pointcuts used by the abstract base class. Specifies the UnitOfWork behaviour in the system.
 *
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class UnitOfWorkProtocol extends AbstractUnitOfWorkProtocol {

    // ======== Pointcuts ===============================================================

    /**
     * Defines the methods that should run in a transaction.
     *
     * @TO_BE_DEFINED execution
     */
    Pointcut transactionalObjectCreationPoints;

    /**
     * Defines all the fields (in all object) that should mark an object as dirty.
     *
     * @TO_BE_DEFINED set
     */
    Pointcut transactionalObjectModificationPoints;

    /**
     * Defines the methods that should run in a transaction.
     *
     * @TO_BE_DEFINED execution
     */
    Pointcut transactionalMethods;

    // ======== Introductions ===============================================================

    /**
     * Defines the objects that we want to participate in the transaction.
     *
     * @Introduce TO_BE_DEFINED deploymentModel=perInstance
     */
    public class MyTransactionalImpl extends AbstractUnitOfWorkProtocol.TransactionalImpl {}
}

/**************************************************************************************
 * Copyright (c) Jonas B+onér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.test.unitofwork;

import aspectwerkz.aosd.unitofwork.AbstractUnitOfWorkProtocol;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * Defines the pointcuts used by the abstract base class. Specifies the UnitOfWork behaviour in the system.
 *
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class UnitOfWorkProtocol extends AbstractUnitOfWorkProtocol {

    // ======== Pointcuts ===============================================================

    /**
     * Defines the methods that should run in a transaction.
     *
     * @Expression execution(* aspectwerkz.aosd.app.domain.DomainObjectFactory.*(..))
     */
    Pointcut transactionalObjectCreationPoints;

    /**
     * Defines all the fields (in all object) that should mark an object as dirty.
     *
     * @Expression set(* aspectwerkz.aosd.app.domain.*.*)
     */
    Pointcut transactionalObjectModificationPoints;

    /**
     * Defines the methods that should run in a transaction.
     *
     * @Expression execution(* aspectwerkz.aosd.app.service.*.*(..))
     */
    Pointcut transactionalMethods;

    // ======== Introductions ===============================================================

    /**
     * Defines the objects that we want to participate in the transaction.
     *
     * @Introduce class(aspectwerkz.aosd.app.domain.*) deploymentModel=perInstance
     */
    public class MyTransactionalImpl extends AbstractUnitOfWorkProtocol.TransactionalImpl {}
}

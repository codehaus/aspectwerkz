/**************************************************************************************
 * Copyright (c) Jonas B+on�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.web.aspect;

import aspectwerkz.aosd.unitofwork.AbstractUnitOfWorkProtocol;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * Defines the pointcuts used by the abstract base class. Specifies the UnitOfWork behaviour in the system.
 *
 * @Aspect perJVM
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class UnitOfWorkProtocol extends AbstractUnitOfWorkProtocol {

    // ======== Pointcuts ===============================================================

    /**
     * Defines the methods that should run in a transaction.
     *
     * @Expression execution(aspectwerkz.aosd.addressbook.AddressBookManager+.new*(..))
     */
    Pointcut transactionalObjectCreationPoints;

    /**
     * Defines all the fields (in all object) that should mark an object as dirty.
     *
     * @Expression set(* aspectwerkz.aosd.addressbook.AddressBook.m_foo)
     */
    Pointcut transactionalObjectModificationPoints;

    /**
     * Defines the methods that should run in a transaction.
     *
     * @Expression execution(* aspectwerkz.aosd.addressbook.AddressBookManager+.*(..))
     */
    Pointcut transactionalMethods;

    // ======== Introductions ===============================================================

    /**
     * Defines the objects that we want to participate in the transaction.
     *
     * @Introduce class(aspectwerkz.aosd.addressbook.*) deploymentModel=perInstance
     */
    public class MyTransactionalImpl extends AbstractUnitOfWorkProtocol.TransactionalImpl {}
}

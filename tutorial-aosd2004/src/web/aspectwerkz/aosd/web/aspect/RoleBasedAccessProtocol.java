/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.web.aspect;

import aspectwerkz.aosd.security.AbstractRoleBasedAccessProtocol;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect perThread
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class RoleBasedAccessProtocol extends AbstractRoleBasedAccessProtocol {

    /**
     * @Execution * aspectwerkz.aosd.ServiceManager.get*(..)
     */
    Pointcut authenticationPoints;

    /**
     * @Execution * aspectwerkz.aosd.addressbook.AddressBookManager+.*(..)
     */
    Pointcut authorizationPointsA;
    /**
     * @Execution * aspectwerkz.aosd.addressbook.AddressBookManager+.new*(..)
     */
    Pointcut authorizationPointsB;

    /**
     * @Execution authorizationPointsA && ! authorizationPointsB
     */
    Pointcut authorizationPoints;
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.app.service;

import aspectwerkz.aosd.app.domain.Customer;
import aspectwerkz.aosd.app.domain.Address;
import aspectwerkz.aosd.app.domain.DomainObjectFactory;
import aspectwerkz.aosd.app.service.CustomerManager;
import aspectwerkz.aosd.unitofwork.UnitOfWork;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CustomerManagerImpl implements CustomerManager {

    /**
     * @transaction requires
     */
    public String getName() {
        return "CustomerManagerImpl";
    }

    /**
     * @transaction requires
     */
    public void getForbiddenMethod() {
        // skip impl
    }

    /**
     * @transaction requires
     */
    public void updateCustomerName(Customer c) {
        c.setFirstName("dummy");
        c.setLastName("dummy");
    }

    /**
     * @transaction requires
     */
    public void updateCustomerNameSetRollbackOnly(Customer c) {
        c.setFirstName("dummy");
        c.setLastName("dummy");
        // mark the transaction as doomed
        UnitOfWork.setRollbackOnly();
    }

    /**
     * @transaction requires
     */
    public void updateCustomerNameForceRollback(Customer customer) {
        Address address = DomainObjectFactory.newAddress();
        customer.setAddress(address);
        throw new RuntimeException("Forced exception");
    }

    /**
     * @transaction requires
     */
    public void nestedMethod1(Customer c) {
        c.setFirstName("Kalle");
        c.setLastName("Anka");
        nestedMethod2(c);
        nestedMethod3(c);
    }

    /**
     * @transaction requires-new
     */
    public void nestedMethod2(Customer c) {
//        Address addr = c.getAddress();
//        addr.setCity("Uppsala");
//        addr.setCountry("Sverige");
    }

    /**
     * @transaction requires
     */
    public void nestedMethod3(Customer c) {
        c.setFirstName("Douglas");
        c.setLastName("Coupland");
        c.doSomethingIllegal();
    }
}

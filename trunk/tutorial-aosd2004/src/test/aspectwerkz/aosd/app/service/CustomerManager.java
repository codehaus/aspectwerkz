/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.app.service;

import aspectwerkz.aosd.app.domain.Customer;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface CustomerManager {

    String getName();

    void getForbiddenMethod();

    void updateCustomerName(Customer c);

    void updateCustomerNameSetRollbackOnly(Customer c);

    void updateCustomerNameForceRollback(Customer c);

    void nestedMethod1(Customer c);

    void nestedMethod2(Customer c);

    void nestedMethod3(Customer c);
}

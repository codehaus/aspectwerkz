/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.app.domain;

import aspectwerkz.aosd.app.domain.Address;
import aspectwerkz.aosd.app.domain.Customer;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DomainObjectFactory {

    public static Customer newCustomer(final String firstName, final String lastName) {
        return new Customer(firstName, lastName);
    }

    public static Address newAddress() {
        return new Address();
    }
}

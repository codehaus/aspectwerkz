/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd;

import aspectwerkz.aosd.definition.Definition;

/**
 * An interface describing the ServiceManager contract with the services
 * in the system. All new services must implement this interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface Service {

    /**
     * Initializes the service.
     *
     * @param definition the definition of the service/concern
     */
    void initialize(Definition definition);
}

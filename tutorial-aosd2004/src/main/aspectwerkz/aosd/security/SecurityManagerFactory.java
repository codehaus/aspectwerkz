/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.security;

import aspectwerkz.aosd.security.jaas.JaasSecurityManager;

/**
 * Factory for the SecurityManager classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SecurityManagerFactory {

    /**
     * The JAAS security manager.
     */
    private static final SecurityManager JAAS_SECURITY_MANAGER = JaasSecurityManager.getInstance();

    /**
     * Creates a new security manager.
     *
     * @return a new security manager
     */
    public static SecurityManager getInstance(final SecurityManagerType type) {
        if (type.equals(SecurityManagerType.JAAS)) {
            return JAAS_SECURITY_MANAGER;
        }
        else {
            throw new SecurityException("security manager " + type.toString() + " is not supported");
        }
    }
}



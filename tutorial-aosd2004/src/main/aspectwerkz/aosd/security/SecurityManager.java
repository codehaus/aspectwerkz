/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.security;

import java.lang.reflect.Method;
import java.security.Principal;

import aspectwerkz.aosd.Service;
import aspectwerkz.aosd.Context;

/**
 * Handles the access control in the system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface SecurityManager extends Service {

    /**
     * Implements the authentication of the client.
     *
     * @param context the user context
     */
    void authenticate(Context context);

    /**
     * Checks if a specific role has access to a specific method.
     *
     * @param principal the principal to to check access for
     * @param klass the class to check access at
     * @param method the method to check access at
     * @return boolean
     */
    boolean checkPermission(Principal principal, Class klass, Method method);
}



/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.test.security;

import aspectwerkz.aosd.security.AbstractRoleBasedAccessProtocol;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect perThread
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class RoleBasedAccessProtocol extends AbstractRoleBasedAccessProtocol {

    /**
     * @Expression execution(* aspectwerkz.aosd.app.facade.*.*(..))
     */
    Pointcut authenticationPoints;

    /**
     * @Expression execution(* aspectwerkz.aosd.app.service.*.*(..))
     */
    Pointcut authorizationPoints;
}

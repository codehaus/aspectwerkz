/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.connectivity;

import examples.introduction.Target;
import examples.introduction.Mixin;

/**
 * Starts up the aspectwerkz system (which starts up the remote proxy server).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class Server {
    public static void main(String[] args) {
        Target target = new Target();
        ((Mixin)target).sayHello(); // to start up the AspectWerkz system, just a trigger
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.SystemLoader;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class InlinedJoinPointBase {

    protected static final Class TARGET_CLASS;

    static {
        try {
            TARGET_CLASS = Class.forName("Foo");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("ERROR");
        }
    }

    protected static final AspectSystem SYSTEM = SystemLoader.getSystem(TARGET_CLASS);
}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.attribdef.caching;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class Pi {

    public static int getPiDecimal(int n) {
        System.out.println("using method");
        String decimals = "141592653";
        if (n > decimals.length()) {
            return 0;
        }
        else {
            return Integer.parseInt(decimals.substring(n, n + 1));
        }
    }
}
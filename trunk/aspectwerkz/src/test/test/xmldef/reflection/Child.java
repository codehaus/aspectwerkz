/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.reflection;

public class Child extends Super {

    /**
     * This method will end in an exception if a pointcut is
     * applied both on this method and on the overrrided one in the super class
     */
    public int incr(int value) {
        int res = super.incr(value);
        return res+1;
    }

}

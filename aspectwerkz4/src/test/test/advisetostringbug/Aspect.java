/**
 * ***********************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 * ************************************************************************************
 */
package test.advisetostringbug;

public class Aspect {

    /**
     * @Mixin within(test.advisetostringbug.A+)
     */
    public static class BImpl implements B {
        public void toString(boolean b, String s) {

        }

    }
}

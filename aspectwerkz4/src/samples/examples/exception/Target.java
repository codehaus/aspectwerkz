/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.exception;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class Target {

    private void method() throws Exception {
        throw new Exception("exception message");
    }

    public static void main(String[] args) {
        Target target = new Target();
        try {
            target.method();
        } catch (Exception e) {
        }
    }
}
/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aopc;


/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CallablePrototype extends BaseCallable implements Callable {
    public void methodAround() {
        log("methodAround ");
    }

    public void methodPre() {
        log("methodPre ");
    }

    public void methodPost() {
        log("methodPost ");
    }
}

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook.impl;

import org.codehaus.aspectwerkz.hook.ClassPreProcessor;

import java.util.Hashtable;

/**
 * A simple implementation of class preprocessor.
 *
 * It does not modify the bytecode. It just prints on stdout some messages.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @version $Id: StdoutPreProcessor.java,v 1.2 2003-07-23 14:20:32 avasseur Exp $
 */
public class StdoutPreProcessor implements ClassPreProcessor {

    private void log(String s) {
        System.out.println(Thread.currentThread().getName() + ": StdoutPreProcessor: " + s);
    }

    public void initialize(Hashtable hashtable) {
        log("initialize");
        log("loaded by " + this.getClass().getClassLoader());
    }

    public byte[] preProcess(String klass, byte abyte[], ClassLoader caller) {
        log(klass);
        return abyte;
    }

}

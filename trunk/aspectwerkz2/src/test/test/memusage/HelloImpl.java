/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.memusage;

import org.apache.bcel.util.BCELifier;
import org.apache.bcel.util.ClassLoaderRepository;

/**
 * Helper class for BCELifier MemUsageTest create such classes on the fly
 */
public class HelloImpl implements Hello {

    private byte[] buffer = new byte[1000];

    private static byte[] sbuffer = new byte[1000];

    public String sayHello0() {
        return "sayHello0";
    }

    public static void main(String args[]) throws Exception {
        BCELifier bc = new BCELifier(
                (new ClassLoaderRepository(HelloImpl.class.getClassLoader())).loadClass(HelloImpl.class.getName()),
                System.out
        );
        bc.start();
    }
}

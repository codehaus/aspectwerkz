/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.xmldef;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.aspectwerkz.xmldef.AspectWerkz;
import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.TestFactory;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AllPerfTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All perf tests");

        suite.addTest(asLoadTest(test.clapp.CustomClassLoaderTest.class, 4, 6));
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public AllPerfTests(String aName) {
        super(aName);
        AspectWerkz.getSystem("tests").initialize();
    }

    private static Test asLoadTest(Class testClass, int thread, int loops) {
        return new LoadTest(new TestFactory(testClass), thread, loops);
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.aspectwerkz.xmldef.XmlDefSystem;
import org.codehaus.aspectwerkz.SystemLoader;
import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.TestFactory;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AllPerfTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All perf tests");

        //suite.addTest(asLoadTest(new TestFactory(test.xmldef.clapp.CustomClassLoaderTest.class), 2, 1));// concurent system initialization (see AW-98)
        //suite.addTest(asLoadTest(new TestFactory(test.xmldef.clapp.CustomClassLoaderTest.class), 10, 10));// concurent system use (see AW-98 concurrent modification issue)

        suite.addTest(asLoadTest(test.xmldef.memusage.MemUsageTest.suite(), 2, 1));
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public AllPerfTests(String aName) {
        super(aName);
        SystemLoader.getSystem("tests").initialize();
    }

    private static Test asLoadTest(Test testClass, int thread, int loops) {
        return new LoadTest(testClass, thread, loops);
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.adviseonintroducedinterface;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Aspect {
    
    /**
     * @Before execution(void test.adviseonintroducedinterface.Intf+.method())
     */
    public void before(JoinPoint jp) {
        System.out.println("Aspect.before()");
        Test.log("before ");
    }
    
    /**
     * @Introduce within(test.adviseonintroducedinterface.Target)
     */
    public static class Impl implements Intf {
        public void method() {
            System.out.println("Impl.method()");
            Test.log("method ");
        }
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;
import org.codehaus.aspectwerkz.joinpoint.management.InlinedJoinPointManager;

import java.lang.reflect.Modifier;

/**
 * A fake WeavedTarget that mimics the weaver result of Target.
 * This class can be run standalone to test the JIT regeneration at load time.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class WeavedTarget {

    private int m_counter1;

    private int m_counter2;

    public int getCounter() {
        return m_counter1;
    }

    public void increment() {
        m_counter2 = m_counter2 + 1;
    }

    public static int toLog1(int i)
    {
        try {//ignore the try catch - it is some compiler glue
        return InlinedJoinPoint.invoke(i, null, null);
        } catch (Throwable t) {t.printStackTrace(); return 0;}//ignore the try catch - it is some compiler glue
    }

    public static int ___AW_$_AW_$toLog1$_AW_$2$_AW_$examples_logging_Target(int i)
    {
        System.out.println("Target.toLog1()");
        (new Target()).toLog2(new String[] {
            "parameter"
        });
        return 1;
    }

    public String[] toLog2(String[] arg) {
        System.out.println("Target.toLog2()");
        new WeavedTarget().toLog3();
        return null;
    }

    public String toLog3() {
        System.out.println("Target.toLog3()");
        return "result";
    }

    public static void main(String[] args) {
        try {
            System.out.println("Target.main");
            WeavedTarget.toLog1(3);
            WeavedTarget target = new WeavedTarget();
            target.increment();
            target.getCounter();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // added by the weaver to trigger JIT generation if not packaged together
    private static void ___AW_$_AW_$initJoinPoints() {
//        InlinedJoinPointManager.loadJoinPoint(JoinPointType.METHOD_EXECUTION, ___AW_Clazz, "toLog1", "(I)I", Modifier.PUBLIC, null, null, null, 2,
//                -2091835264, "examples/logging/WeavedTarget_1__2091835264___AW_JoinPoint" );
    }

    private final static Class ___AW_Clazz = WeavedTarget.class;/* the weaver will produce: Class.forName("examples.logging.WeavedTarget");*/

    // added by the weaver to trigger JIT generation if not packaged together
    static {
        ___AW_$_AW_$initJoinPoints();
    }
}
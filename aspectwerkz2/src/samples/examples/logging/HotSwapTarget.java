/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;
import org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper;
import org.codehaus.aspectwerkz.hook.RuntimeClassProcessor;
import org.codehaus.aspectwerkz.extension.hotswap.HotSwapClient;
import org.codehaus.aspectwerkz.aspect.management.PointcutManager;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.definition.expression.ExecutionExpression;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.StartupManager;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Sample on how to use Runtime Weaving programmatically.
 *
 * Note: to debug from within the IDE with the WeavingClassLoader it is mandatory
 * to use the -Daspectwerkz.transform.forceWCL=true option so that AspectWerkz
 * gets loaded in the WeavingClassLoader and not in the (parallel) system class loader.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class HotSwapTarget {

    private int m_counter1;
    private int m_counter2;

    public static boolean showStack = false;

    public int getCounter() {
        System.out.println("getCounter()");
        return m_counter1;
    }

    public void increment() {
        System.out.println("increment()");
        m_counter2 = m_counter2 + 1;
    }

    public static void toLog1WithStack() {
        showStack = true;
        toLog1();
        showStack = false;
    }

    public static void toLog1() {
        System.out.println("toLog1()");
        new HotSwapTarget().toLog2("parameter");
    }

    private void toLog2(java.lang.String arg) {
        System.out.println("  toLog2()");
        new HotSwapTarget().toLog3();
    }

    private String toLog3() {
        System.out.println("    toLog3()");
        if (showStack) {
            (new Exception("expected exception to show stack trace...")).printStackTrace(System.out);
        }
        return "result";
    }

    public static void demo1() throws Throwable {
        // regular calls
        System.out.println("\n\n== before activation ==");
        HotSwapTarget.toLog1();
        HotSwapTarget target = new HotSwapTarget();
        target.increment();
        target.getCounter();
        // show stack
        HotSwapTarget.toLog1WithStack();
        Thread.sleep(3000);
        System.out.println("\n\n\n");

        // add new pointcuts
        JavaLoggingAspect.addPointcutForLoggingAdvice("execution(* examples.logging.HotSwapTarget.toLog1())", "runtimePCToLog1");
        //JavaLoggingAspect.addPointcutForLoggingAdvice("call(*->* examples.logging.HotSwapTarget.toLog2(..))", "CALLruntimePCToLog2");
        // call HotSwap for runtime weaving
        HotSwapClient.hotswap(HotSwapTarget.class);

        // hotswapped calls
        System.out.println("\n\n== after activation of toLog1() ==");
        HotSwapTarget.toLog1();
        target.increment();
        target.getCounter();
        // show stack


        HotSwapTarget.toLog1WithStack();
        Thread.sleep(3000);
        System.out.println("\n\n\n");


        // add new pointcuts
        //addPointcutForLoggingAdvice("* examples.logging.HotSwapTarget.toLog3(int)", "runtimePCToLog3b");
        JavaLoggingAspect.addPointcutForLoggingAdvice("execution(* examples.logging.HotSwapTarget.toLog2(..))", "runtimePCToLog2");
        // call HotSwap for runtime weaving
        HotSwapClient.hotswap(HotSwapTarget.class);

        // hotswapped calls
        System.out.println("\n\n== after second activation, same instance ==");
        HotSwapTarget.toLog1();
        target.increment();
        target.getCounter();
//        System.out.println("\n== after second activation, other instance ==");
//        HotSwapTarget.toLog1();
//        target = new HotSwapTarget();
//        target.increment();
//        target.getCounter();


        // remove
        JavaLoggingAspect.removePointcutForLoggingAdvice("","runtimePCToLog1");
        JavaLoggingAspect.removePointcutForLoggingAdvice("","runtimePCToLog2");
        JavaLoggingAspect.removePointcutForLoggingAdvice("","CALLruntimePCToLog2");
        System.out.println("\n\n== after removal of pc defs ==");
        HotSwapTarget.toLog1();

        // call HotSwap for runtime weaving
        HotSwapClient.hotswap(HotSwapTarget.class);
        System.out.println("\n\n== after un-weaving of removed pc defs ==");
        HotSwapTarget.toLog1();
        // show stack
        HotSwapTarget.toLog1WithStack();
        Thread.sleep(3000);
        System.out.println("\n\n\n");
    }

    public static void main(String a[]) throws Throwable {
        demo1();
        benchHotSwap();
        System.exit(0);
    }


    public static void benchHotSwap() {
        int loop = 100;
        long ts = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            HotSwapClient.hotswap(HotSwapTarget.class);
        }
        System.out.println("perSwap without def change = " + (System.currentTimeMillis()-ts)/loop);

        ts = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            JavaLoggingAspect.addPointcutForLoggingAdvice("execution(* examples.logging.HotSwapTarget.toLog1())", "runtimePCToLog1");
            HotSwapClient.hotswap(HotSwapTarget.class);
            JavaLoggingAspect.removePointcutForLoggingAdvice("","runtimePCToLog1");
        }
        System.out.println("perSwap with def change = " + (System.currentTimeMillis()-ts)/loop);

    }


}

/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;
import org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper;
import org.codehaus.aspectwerkz.hook.RuntimeClassProcessor;
import org.codehaus.aspectwerkz.extension.hotswap.HotSwapClient;
import org.codehaus.aspectwerkz.pointcut.ExecutionPointcut;
import org.codehaus.aspectwerkz.pointcut.PointcutManager;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.definition.expression.ExecutionExpression;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;

import java.util.Iterator;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class HotSwapTarget {

    private int m_counter1;
    private int m_counter2;

    public int getCounter() {
        System.out.println("getCounter()");
        return m_counter1;
    }

    public void increment() {
        System.out.println("increment()");
        m_counter2 = m_counter2 + 1;
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
        return "result";
    }

    public static void main(String[] args) throws Throwable {
        // regular calls
        System.out.println("== before activation ==");
        HotSwapTarget.toLog1();
        HotSwapTarget target = new HotSwapTarget();
        target.increment();
        target.getCounter();

        // add new pointcuts
        addPointcutForLoggingAdvice("* examples.logging.HotSwapTarget.toLog3(..)", "runtimePCToLog3");
        // call HotSwap for runtime weaving
        HotSwapClient.hotswap(HotSwapTarget.class);

        // hotswapped calls
        System.out.println("== after activation, same instance ==");
        HotSwapTarget.toLog1();
        target.increment();
        target.getCounter();

        // add new pointcuts
        addPointcutForLoggingAdvice("* examples.logging.HotSwapTarget.toLog1(..)", "runtimePCToLog2");
        // call HotSwap for runtime weaving
        HotSwapClient.hotswap(HotSwapTarget.class);

        // hotswapped calls
        System.out.println("== after second activation, same instance ==");
        HotSwapTarget.toLog1();
        target.increment();
        target.getCounter();
        System.out.println("== after second activation, other instance ==");
        HotSwapTarget.toLog1();
        target = new HotSwapTarget();
        target.increment();
        target.getCounter();
    }

    /**
     * A damned complicated API to
     * - alter the def so that new weaving can be done
     * - alter the internal aspect repr. so that runtime management can occur
     *
     * Note: seems to have a redundancy on the pointcut somewhere.
     * CRAP
     *
     * @param pointcut
     * @param pointcutName
     */
    private static void addPointcutForLoggingAdvice(String pointcut, String pointcutName) {
        final String aspectName = "examples.logging.LoggingAspect";
        ExecutionExpression pcExpression = ExpressionNamespace.getExpressionNamespace(aspectName)
                .createExecutionExpression(
                    pointcut,
                    "",
                    pointcutName
                );
        SystemDefinition sysDef = DefinitionLoader.getDefinition(HotSwapTarget.class.getClassLoader(), "samples");
        AspectDefinition aspectDef = sysDef.getAspectDefinition(aspectName);
        AdviceDefinition newDef = null;
        for (Iterator arounds = aspectDef.getAroundAdvices().iterator(); arounds.hasNext();) {
            AdviceDefinition around = (AdviceDefinition) arounds.next();
            if (around.getName().equals(aspectName+".logMethod")) {
                // copy the logMethod advice
                // note: we could add a totally new advice as well
                newDef = around.copyAt(pcExpression);
                break;
            }
        }
        aspectDef.addAroundAdvice(newDef);

        ExecutionPointcut pointcutInstance = new ExecutionPointcut("samples", newDef.getExpression());
        PointcutManager pointcutManager = SystemLoader.getSystem("samples").
                getAspectManager().getPointcutManager(aspectName);
        pointcutManager.addExecutionPointcut(pointcutInstance);
        pointcutInstance.addAroundAdvice(aspectName+".logMethod");
    }
}

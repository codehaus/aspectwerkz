/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.cglib;

import awbench.method.Execution;
import awbench.method.IExecution;
import awbench.Run;
import awbench.Constants;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodExecutionRun {

    public static void main(String args[]) throws Throwable {
        // iteration
        if (args.length > 0) {
            int iteration = Integer.parseInt(args[0]);
            if (iteration > 0) {
                Run.ITERATIONS = iteration;
            }
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallback(new MethodExecutionBeforeAdvice());
        IExecution test = (IExecution) enhancer.create();
        test.warmup();
        Run run = null;
        run = new Run("method execution, before advice");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.before();
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallback(new MethodExecutionBeforeSJPAdvice());
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, before advice, Static JP");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.beforeSJP();
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallback(new MethodExecutionBeforeJPAdvice());
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, before advice, JP");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.beforeJP();
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallback(new MethodExecutionAfterReturningAdvice());
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, after returning <TYPE> advice");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.afterReturningString();
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallback(new MethodExecutionAfterThrowingAdvice());
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, after throwing <TYPE> advice");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            try {
                test.afterThrowingRTE();
            } catch (RuntimeException e) {
                // ignore
            }
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallback(new MethodExecutionBeforeAfterAdvice(new MethodExecutionAfterAdvice()));
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, before + after advice");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.beforeAfter();
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallbacks(new Callback[]{
            new MethodExecutionBeforeWithPrimitiveArgsAdvice(),
            new IgnoreInterceptor()});
        enhancer.setCallbackFilter(new IgnoreAllBut("beforeWithPrimitiveArgs"));
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, before advice, args() access for primitive");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.beforeWithPrimitiveArgs(Constants.CONST_0);
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallbacks(new Callback[]{
            new MethodExecutionBeforeWithWrappedArgsAdvice(),
            new IgnoreInterceptor()});
        enhancer.setCallbackFilter(new IgnoreAllBut("beforeWithWrappedArgs"));
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, before advice, args() access for objects");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.beforeWithWrappedArgs(Constants.WRAPPED_0);
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallbacks(new Callback[]{
            new MethodExecutionBeforeWithArgsAndTargetAdvice(),
            new IgnoreInterceptor()});
        enhancer.setCallbackFilter(new IgnoreAllBut("beforeWithArgsAndTarget"));
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, before advice, args() and target() access");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.beforeWithArgsAndTarget(Constants.CONST_0);
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallback(new MethodExecutionAroundAdvice());
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, around advice");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.around_();
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallback(new MethodExecutionAroundSJPAdvice());
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, around advice, SJP");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.aroundSJP();
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallback(new MethodExecutionAroundJPAdvice());
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, around advice, JP");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.aroundJP();
        }
        run.end();

        enhancer = new Enhancer();
        enhancer.setSuperclass(Execution.class);
        enhancer.setCallbacks(new Callback[]{
            new MethodExecutionGetTargetAndArgsAroundAdvice(
                new MethodExecutionGetTargetAndArgsAroundAdvice2()),
            new IgnoreInterceptor()});
        enhancer.setCallbackFilter(new IgnoreAllBut("aroundStackedWithArgAndTarget"));
        test = (IExecution) enhancer.create();
        test.warmup();
        run = new Run("method execution, around advice x 2, args() and target() access");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.aroundStackedWithArgAndTarget(Constants.CONST_0);
        }
        run.end();

        Run.report();
        Run.flush();
    }

    private static class IgnoreInterceptor implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            return methodProxy.invokeSuper(o, objects);
        }
    }

    private static class IgnoreAllBut implements CallbackFilter {
        String m_methodName;
        IgnoreAllBut(String methodName) {
            m_methodName = methodName;
        }
        public int accept(Method method) {
            if (method.getName().equals(m_methodName)) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}

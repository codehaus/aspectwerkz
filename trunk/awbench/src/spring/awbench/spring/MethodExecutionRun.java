/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.spring;

import awbench.method.IExecution;
import awbench.Run;
import awbench.Constants;
import awbench.Measurement;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
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

        ApplicationContext ctx = new FileSystemXmlApplicationContext("src/spring/springconfig.xml");
        IExecution test = (IExecution) ctx.getBean("execution");

        Run run = null;

        run = new Run("method execution, @Before");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.before();
        }
        run.end();

        run = new Run("method execution, @Before, Static JP");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.beforeSJP();
        }
        run.end();

        run = new Run("method execution, @Before, JP");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.beforeJP();
        }
        run.end();

        run = new Run("method execution, @Before/@After");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.beforeAfter();
        }
        run.end();

        run = new Run("method execution, @Before, args() access for primitive");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.withPrimitiveArgs(Constants.CONST_0);
        }
        run.end();

        run = new Run("method execution, @Before, args() access for objects");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.withWrappedArgs(Constants.WRAPPED_0);
        }
        run.end();

        run = new Run("method execution, @Before, args() and target() access");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.withArgsAndTarget(Constants.CONST_0);
        }
        run.end();

        run = new Run("method execution, @Around, JP");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.aroundJP();
        }
        run.end();

        run = new Run("method execution, @Around, SJP");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.aroundSJP();
        }
        run.end();

        run = new Run("method execution, @Around x 2, args() and target() access");
        for (int i = 0; i < Run.ITERATIONS; i++) {
            test.aroundStackedWithArgAndTarget(Constants.CONST_0);
        }
        run.end();

        Run.report();
        Run.flush();
    }
}

package awbench.extension.aopalliance;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import awbench.method.Execution;

public class MethodExecutionGetTargetAndArgsAroundAdvice implements MethodInterceptor {
    public static int s_count = 0;

    public Object invoke(MethodInvocation invocation) throws Throwable {
        s_count++;
        int i = ((Integer)invocation.getArguments()[0]).intValue();
        Execution execution = (Execution)invocation.getThis();
        return invocation.proceed();
    }
}
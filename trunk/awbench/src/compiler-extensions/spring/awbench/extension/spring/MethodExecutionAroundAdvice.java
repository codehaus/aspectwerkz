package awbench.extension.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class MethodExecutionAroundAdvice implements MethodInterceptor {
    public static int s_count = 0;

    public Object invoke(MethodInvocation invocation) throws Throwable {
        s_count++;
        return invocation.proceed();
    }
}
package awbench.extension.aopalliance;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import awbench.method.Execution;
import awbench.Run;

public class MethodExecutionGetTargetAndArgsAroundAdvice implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Run.ADVICE_HIT++;
        int i = ((Integer)invocation.getArguments()[0]).intValue();
        Execution execution = (Execution)invocation.getThis();
        return invocation.proceed();
    }
}
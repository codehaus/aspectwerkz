package awbench.extension.aopalliance;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import awbench.Run;

public class MethodExecutionAroundAdvice implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Run.ADVICE_HIT++;
        return invocation.proceed();
    }
}
package awbench.spring;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;
import awbench.Run;

public class MethodExecutionAfterReturningAdvice implements AfterReturningAdvice {
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        if (returnValue instanceof String) {
            Run.ADVICE_HIT++;
        }
    }
}
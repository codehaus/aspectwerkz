package awbench.extension.spring;

import java.lang.reflect.Method;

import org.springframework.aop.ThrowsAdvice;
import awbench.Run;

public class MethodExecutionAfterThrowingAdvice implements ThrowsAdvice {
    public void afterThrowing(Method method, Object[] args, Object target, Throwable subclass) {
        if (subclass instanceof RuntimeException) {
            Run.ADVICE_HIT++;
        }
    }
}
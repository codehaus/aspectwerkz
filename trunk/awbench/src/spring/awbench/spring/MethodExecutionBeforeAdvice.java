package awbench.spring;

import java.lang.reflect.Method;

import org.springframework.aop.MethodBeforeAdvice;
import awbench.Run;

public class MethodExecutionBeforeAdvice implements MethodBeforeAdvice {
    public void before(Method m, Object[] args, Object target) throws Throwable {
        Run.ADVICE_HIT++;
    }
}
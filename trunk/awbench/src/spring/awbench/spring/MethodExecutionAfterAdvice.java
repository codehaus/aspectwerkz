package awbench.spring;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;
import awbench.Run;

public class MethodExecutionAfterAdvice implements AfterReturningAdvice {
    public void afterReturning(Object o, Method method, Object[] objects, Object o1) throws Throwable {
        Run.ADVICE_HIT++;
    }
}
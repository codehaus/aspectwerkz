package awbench.spring;

import java.lang.reflect.Method;

import org.springframework.aop.MethodBeforeAdvice;

public class MethodExecutionBeforeAdvice implements MethodBeforeAdvice {
    public static int s_count = 0;
    public void before(Method m, Object[] args, Object target) throws Throwable {
        s_count++;
    }
}
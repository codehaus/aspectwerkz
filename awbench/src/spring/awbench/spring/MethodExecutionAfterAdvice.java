package awbench.spring;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;

public class MethodExecutionAfterAdvice implements AfterReturningAdvice {
    public static int s_count = 0;
    public void afterReturning(Object o, Method method, Object[] objects, Object o1) throws Throwable {
        s_count++;
    }
}
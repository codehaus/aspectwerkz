package test;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.advice.AroundAdvice;

public class SimpleMethodAdvice extends AroundAdvice {
    public SimpleMethodAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        return result;
    }
}

package test.inlining.proxymethod;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class Aspect {

    public Object advice(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
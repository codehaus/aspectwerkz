package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

public class MyMemberMethodAdvice4 extends AroundAdvice {
    public MyMemberMethodAdvice4() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        String metadata =
                jp.getTargetClass().getName() +
                jp.getMethod().getName() +
                jp.getTargetObject().hashCode() +
                jp.getParameters()[0] +
                jp.getParameterTypes()[0].getName() +
                jp.getReturnType().getName() +
                jp.getResult();
        return metadata;
    }
}
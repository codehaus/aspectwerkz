package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

public class MyMemberMethodAdvice3 extends AroundAdvice {
    public MyMemberMethodAdvice3() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetObject()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetObject()).log("after2 ");
        return result;
    }
}


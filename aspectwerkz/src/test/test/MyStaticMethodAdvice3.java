package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

public class MyStaticMethodAdvice3 extends AroundAdvice {
    public MyStaticMethodAdvice3() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((StaticMethodAdviceTest)jp.getTargetObject()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((StaticMethodAdviceTest)jp.getTargetObject()).log("after2 ");
        return result;
    }
}


package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

public class MyStaticMethodAdvice5 extends AroundAdvice {
    public MyStaticMethodAdvice5() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((StaticMethodAdviceTest)jp.getTargetObject()).log("before ");
        final Object result = joinPoint.proceedInNewThread();
        ((StaticMethodAdviceTest)jp.getTargetObject()).log("after ");
        return result;
    }
}

package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyMemberMethodAdvice1 extends AroundAdvice {
    public MyMemberMethodAdvice1() {
        super();
    }

//    public MyMemberMethodAdvice1(final int deploymentModel) {
//        super(deploymentModel);
//    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}

package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

public class MyMemberMethodAdvice2 extends AroundAdvice {
    public MyMemberMethodAdvice2() {
        super();
    }

//    public MyMemberMethodAdvice2(final int deploymentModel) {
//        super(deploymentModel);
//    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetObject()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetObject()).log("after1 ");
        return result;
    }
}

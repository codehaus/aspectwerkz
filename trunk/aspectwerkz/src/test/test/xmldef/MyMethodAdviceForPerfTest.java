package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyMethodAdviceForPerfTest extends AroundAdvice {
    public MyMethodAdviceForPerfTest() {
        super();
    }

//    public MyMethodAdviceForPerfTest(final int deploymentModel) {
//        super(deploymentModel);
//    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}

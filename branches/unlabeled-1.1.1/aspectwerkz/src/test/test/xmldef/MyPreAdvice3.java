package test;

import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyPreAdvice3 extends PreAdvice {
    public MyPreAdvice3() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        FieldAdviceTest.log("pre2 ");
        CallerSideAdviceTest.log("pre2 ");
    }
}

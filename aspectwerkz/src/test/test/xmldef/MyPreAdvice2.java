package test;

import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyPreAdvice2 extends PreAdvice {
    public MyPreAdvice2() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        FieldAdviceTest.log("pre1 ");
        CallerSideAdviceTest.log("pre1 ");
    }
}

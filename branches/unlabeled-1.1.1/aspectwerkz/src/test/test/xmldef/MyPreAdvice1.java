package test;

import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyPreAdvice1 extends PreAdvice {
    public MyPreAdvice1() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
    }
}

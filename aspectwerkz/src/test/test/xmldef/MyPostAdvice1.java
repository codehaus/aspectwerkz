package test;

import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyPostAdvice1 extends PostAdvice {
    public MyPostAdvice1() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
    }
}

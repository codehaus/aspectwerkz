package test;

import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyPostAdvice2 extends PostAdvice {
    public MyPostAdvice2() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        FieldAdviceTest.log("post1 ");
        CallerSideAdviceTest.log("post1 ");
    }
}

package test;

import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyPostAdvice3 extends PostAdvice {
    public MyPostAdvice3() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        FieldAdviceTest.log("post2 ");
        CallerSideAdviceTest.log("post2 ");
    }
}

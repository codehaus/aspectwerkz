package test;

import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyPreAdvice4 extends PreAdvice {
    public MyPreAdvice4() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        CallerSideAdviceTest.log(getParameter("test"));
    }
}

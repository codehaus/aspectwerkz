package test;

import org.codehaus.aspectwerkz.advice.ThrowsAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.ThrowsJoinPoint;

public class MyThrowsAdvice1 extends ThrowsAdvice {

    public MyThrowsAdvice1() {
        super();
    }

    public void execute(final JoinPoint joinPoint) throws Throwable {
        // Needs to cast to the correct join point.
        // A bit tedious but optimizes the performance since I otherwise need to perform a cast at EVERY invocation
        throw new test.TestException(((ThrowsJoinPoint)joinPoint).getMessage());
    }
}

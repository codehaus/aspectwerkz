package test;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.advice.ThrowsAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.ThrowsJoinPoint;

public class MyThrowsAdvice2 extends ThrowsAdvice {
    public MyThrowsAdvice2() {
        super();
    }

    public void execute(final JoinPoint joinPoint) throws Throwable {
        // Needs to cast to the correct join point.
        // A bit tedious but optimizes the performance since I otherwise need to perform a cast at EVERY invocation
        final ThrowsJoinPoint jp = (ThrowsJoinPoint)joinPoint;

        final Throwable exception = jp.getException();
        final String message = jp.getMessage();
        final Class klass = jp.getExceptionClass();
        final String exceptionName = jp.getExceptionName();
        final String localizedMessage = jp.getLocalizedMessage();
        final Method method = jp.getMethod();
        final String methodName = jp.getMethodName();
        final Class[] methodParameterTypes = jp.getMethodParameterTypes();
        final Object[] methodParameters = jp.getMethodParameters();
        final Class methodReturnType = jp.getMethodReturnType();
        final Object originalObject = jp.getTargetObject();
        final String originalObjectClassName = jp.getTargetObjectsClassName();
        throw new test.TestException(
                exception +
                message +
                klass +
                exceptionName +
                localizedMessage +
                method +
                methodName +
                methodReturnType +
                originalObject +
                originalObjectClassName);
    }
}

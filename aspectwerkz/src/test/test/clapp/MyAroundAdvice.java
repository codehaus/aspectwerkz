package test.clapp;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class MyAroundAdvice extends AroundAdvice {

    public Object execute(JoinPoint jp) throws Throwable {
        Object res = jp.proceed();
        res = "before " + (String) res + " after";
        return res;
    }

}

package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MyMemberMethodAdvice1.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class MyMemberMethodAdvice1 extends AroundAdvice {
    public MyMemberMethodAdvice1() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}

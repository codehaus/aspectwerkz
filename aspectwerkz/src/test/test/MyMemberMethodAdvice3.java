package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MyMemberMethodAdvice3.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class MyMemberMethodAdvice3 extends AroundAdvice {
    public MyMemberMethodAdvice3() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetObject()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetObject()).log("after2 ");
        return result;
    }
}


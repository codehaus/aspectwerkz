package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MyMemberMethodAdvice7.java,v 1.1 2003-07-03 13:42:02 jboner Exp $
 */
public class MyMemberMethodAdvice7 extends AroundAdvice {
    public MyMemberMethodAdvice7() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetObject()).log("# ");
        return joinPoint.proceed();
    }
}

package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MyMemberMethodAdvice5.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class MyMemberMethodAdvice5 extends AroundAdvice {
    public MyMemberMethodAdvice5() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetObject()).log("before ");
        final Object result = joinPoint.proceedInNewThread();
        ((Loggable)jp.getTargetObject()).log("after ");
        return result;
    }
}

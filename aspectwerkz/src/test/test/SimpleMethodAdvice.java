package test;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.advice.AroundAdvice;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: SimpleMethodAdvice.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class SimpleMethodAdvice extends AroundAdvice {
    public SimpleMethodAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        return result;
    }
}

package examples.exception;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.CatchClauseRtti;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ExceptionHandlingAspect {

    /**
     * @Before handler(java.lang.Exception)
     */
    public void logEntry(final JoinPoint joinPoint) throws Throwable {
        CatchClauseRtti rtti = (CatchClauseRtti)joinPoint.getRtti();
        Exception e = (Exception)rtti.getParameterValue();
        System.out.println("[From advice] exception catched:" + e.toString());
    }
}

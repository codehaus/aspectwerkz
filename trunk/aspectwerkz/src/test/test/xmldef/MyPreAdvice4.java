package test;

import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MyPreAdvice4.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class MyPreAdvice4 extends PreAdvice {
    public MyPreAdvice4() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        CallerSideAdviceTest.log(getParameter("test"));
    }
}

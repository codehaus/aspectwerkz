package test;

import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MyPreAdvice2.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class MyPreAdvice2 extends PreAdvice {
    public MyPreAdvice2() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        FieldAdviceTest.log("pre1 ");
        CallerSideAdviceTest.log("pre1 ");
    }
}

package test;

import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MyPostAdvice3.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class MyPostAdvice3 extends PostAdvice {
    public MyPostAdvice3() {
        super();
    }

    public void execute(final JoinPoint joinPoint) {
        FieldAdviceTest.log("post2 ");
        CallerSideAdviceTest.log("post2 ");
    }
}

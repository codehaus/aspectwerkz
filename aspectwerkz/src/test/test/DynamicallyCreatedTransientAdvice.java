/*
 * AspectWerkz AOP Framework.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: DynamicallyCreatedTransientAdvice.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class DynamicallyCreatedTransientAdvice extends AroundAdvice {
    public DynamicallyCreatedTransientAdvice() {
        super();
    }
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetObject()).log("before ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetObject()).log("after ");
        return result;
    }
}

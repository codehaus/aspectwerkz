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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: DynamicallyCreatedPersistentAdvice.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class DynamicallyCreatedPersistentAdvice extends AroundAdvice {
    private int m_counter = 0;

    public DynamicallyCreatedPersistentAdvice() {
        super();
    }
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        m_counter++;
        ((Loggable)jp.getTargetObject()).log(new Integer(m_counter).toString());
        return joinPoint.proceed();
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.spring;

import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.aopalliance.aop.Advice;
import org.codehaus.aspectwerkz.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaMethodInfo;

import java.lang.reflect.Method;

/**
 * AspectWerkz Spring integration top provide to Spring a way to define
 * pointcuts using AW syntax.
 *
 * TODO: more test.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AspectWerkzPointcutAdvisor extends DefaultPointcutAdvisor {

    /**
     * One static namespace is enough
     * We won't allow composition, all Spring pointcuts will be anonymous for now.
     */
    static ExpressionNamespace s_namespace = ExpressionNamespace.getNamespace(
            AspectWerkzPointcutAdvisor.class.getName()
    );

    /**
     * The pointcut impl that will use AW to impl the matching
     */
    Pointcut m_pointcut;

    public AspectWerkzPointcutAdvisor() {
        super();
    }

    public AspectWerkzPointcutAdvisor(Advice advice) {
        super(advice);
    }

    public void setExpression(String s) throws Exception {
        ExpressionInfo expression = new ExpressionInfo(s, s_namespace.getName());
        m_pointcut = new AspectWerkzPointcut(expression);
    }

    public Pointcut getPointcut() {
        System.out.println("AspectWerkzPointcutAdvisor.getPointcut");
        return m_pointcut;
    }

    /**
     * AspectWerkz Spring pointcut implementation
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    static class AspectWerkzPointcut implements Pointcut {

        final ExpressionInfo m_expression;

        AspectWerkzPointcut(ExpressionInfo expression) {
            m_expression = expression;
        }

        public ClassFilter getClassFilter() {
            return new ClassFilter() {
                public boolean matches(Class aClass) {
                    // TODO: do we need more than execution fe for mixin support in Spring ??
                    // assume that it early filtering only.
                    ExpressionContext ctx = new ExpressionContext(
                            PointcutType.EXECUTION, JavaClassInfo.getClassInfo(aClass), null
                    );
                    boolean match = m_expression.getAdvisedClassFilterExpression().match(ctx);
                    //System.out.println("\nclass ? " + aClass.getName() + " : " + match);
                    return match;
                }
            };
        }

        public MethodMatcher getMethodMatcher() {
            return new MethodMatcher() {

                public boolean matches(Method method, Class aClass) {
                    // note: for execution, whithin(code) = the method itself
                    ExpressionContext ctx = new ExpressionContext(
                            PointcutType.EXECUTION, JavaMethodInfo.getMethodInfo(method), JavaMethodInfo.getMethodInfo(method)
                    );
                    boolean match = m_expression.getExpression().match(ctx);
                    //System.out.println("\nmethod ? " + method.toString() + " : " + match);
                    return match;

                }

                public boolean isRuntime() {
                    return false;
                }

                // TODO: what is that for ?
                public boolean matches(Method method, Class aClass, Object[] objects) {
                    if (true) throw new UnsupportedOperationException(" What is it for ? ");
                    return false;
                }
            };
        }
    }

}

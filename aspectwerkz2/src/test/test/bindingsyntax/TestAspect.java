/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.bindingsyntax;

import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @TODO Alex - can you fix this test? I don't get what you are doing here. Below is the XML def that should be defined
 * in this file using attributes. The rest is done.
 * <p/>
 * <!-- ============================================= --> <!--  BindingSyntax testing --> <!--  The test validates XML
 * ordering defines aspect/advice precedence --> <!--  for method pointcut --> <!--
 * ============================================= --> <advice-def name="BindingSyntax_1"
 * class="test.xmldef.bindingsyntax.Prepend1Advice"/> <advice-def name="BindingSyntax_2"
 * class="test.xmldef.bindingsyntax.Prepend2Advice"/> <!-- bindingsyntax : advice stack --> <advices-def
 * name="BindingSyntaxA"> <advice-ref name="BindingSyntax_1"/> <advice-ref name="BindingSyntax_2"/> </advices-def>
 * <advices-def name="BindingSyntaxRA"> <advice-ref name="BindingSyntax_2"/> <advice-ref name="BindingSyntax_1"/>
 * </advices-def> <aspect name="BindingSyntaxA"> <pointcut-def name="test" type="method" pattern="*
 * test.xmldef.bindingsyntax.*.doA*(..)"/> <bind-advice pointcut="test"> <advices-ref name="BindingSyntaxA"/>
 * </bind-advice> </aspect> <aspect name="BindingSyntaxRA"> <pointcut-def name="test" type="method" pattern="*
 * test.xmldef.bindingsyntax.*.doRA*(..)"/> <bind-advice pointcut="test"> <advices-ref name="BindingSyntaxRA"/>
 * </bind-advice> </aspect> <!-- bindingsyntax : two advices bounded --> <aspect name="BindingSyntaxB"> <pointcut-def
 * name="test" type="method" pattern="* test.xmldef.bindingsyntax.*.doB*(..)"/> <bind-advice pointcut="test">
 * <advice-ref name="BindingSyntax_1"/> <advice-ref name="BindingSyntax_2"/> </bind-advice> </aspect> <aspect
 * name="BindingSyntaxRB"> <pointcut-def name="test" type="method" pattern="* test.xmldef.bindingsyntax.*.doRB*(..)"/>
 * <bind-advice pointcut="test"> <advice-ref name="BindingSyntax_2"/> <advice-ref name="BindingSyntax_1"/>
 * </bind-advice> </aspect> <!-- bindingsyntax : two equivalent pointcuts, one advice bounded on each --> <!-- Note: the
 * precedence is altered, since the first pointcut is completeted before applying the second pointcut --> <aspect
 * name="BindingSyntaxC"> <!-- the pointcut-def order has NO impact on precedence --> <pointcut-def name="test2"
 * type="method" pattern="* test.xmldef.bindingsyntax.*.doC*(..)"/> <pointcut-def name="test" type="method" pattern="*
 * test.xmldef.bindingsyntax.*.doC*(..)"/> <bind-advice pointcut="test"> <advice-ref name="BindingSyntax_1"/>
 * </bind-advice> <bind-advice pointcut="test2"><!-- do not bind to pointcut "test" or it will be overided -->
 * <advice-ref name="BindingSyntax_2"/> </bind-advice> </aspect> <aspect name="BindingSyntaxRC"> <pointcut-def
 * name="test2" type="method" pattern="* test.xmldef.bindingsyntax.*.doRC*(..)"/> <pointcut-def name="test"
 * type="method" pattern="* test.xmldef.bindingsyntax.*.doRC*(..)"/> <bind-advice pointcut="test2"> <advice-ref
 * name="BindingSyntax_2"/> </bind-advice> <bind-advice pointcut="test"> <advice-ref name="BindingSyntax_1"/>
 * </bind-advice> </aspect> <!--  bindingsyntax : two aspects --> <aspect name="BindingSyntaxD_1"> <pointcut-def
 * name="test" type="method" pattern="* test.xmldef.bindingsyntax.*.doD*(..)"/> <bind-advice pointcut="test">
 * <advice-ref name="BindingSyntax_1"/> </bind-advice> </aspect> <aspect name="BindingSyntaxD_2"> <pointcut-def
 * name="test" type="method" pattern="* test.xmldef.bindingsyntax.*.doD*(..)"/> <bind-advice pointcut="test">
 * <advice-ref name="BindingSyntax_2"/> </bind-advice> </aspect> <aspect name="BindingSyntaxRD_2"> <pointcut-def
 * name="test" type="method" pattern="* test.xmldef.bindingsyntax.*.doRD*(..)"/> <bind-advice pointcut="test">
 * <advice-ref name="BindingSyntax_2"/> </bind-advice> </aspect> <aspect name="BindingSyntaxRD_1"> <pointcut-def
 * name="test" type="method" pattern="* test.xmldef.bindingsyntax.*.doRD*(..)"/> <bind-advice pointcut="test">
 * <advice-ref name="BindingSyntax_1"/> </bind-advice> </aspect>
 * @Aspect
 */
public class TestAspect extends Aspect {

    /**
     * @Around
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        String last = AdviceBindingTest.flow;
        AdviceBindingTest.flow += " ";
        //System.out.println(AdviceBindingTest.flow + " -> Advice_1");
        Object r = joinPoint.proceed();
        //System.out.println(AdviceBindingTest.flow + " <- Advice_1");
        AdviceBindingTest.flow = last;
        return '1' + (String)r;
    }


    /**
     * @Around
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        String last = AdviceBindingTest.flow;
        AdviceBindingTest.flow += " ";
        //System.out.println(AdviceBindingTest.flow + " -> Advice_2");
        Object r = joinPoint.proceed();
        //System.out.println(AdviceBindingTest.flow + " <- Advice_2");
        AdviceBindingTest.flow = last;
        return '2' + (String)r;
    }
}

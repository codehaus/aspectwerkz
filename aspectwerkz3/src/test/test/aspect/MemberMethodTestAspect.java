/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aspect;

import test.Loggable;
import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @Aspect perJVM
 */
public class MemberMethodTestAspect {
    // ============ Pointcuts ============

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.get*(..))
     */
    Pointcut member_pc1;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.*Param**(..))
     */
    Pointcut member_pc2;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.exceptionThrower*(..))
     */
    Pointcut member_pc3;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.methodAdvicedMethod())
     */
    Pointcut member_pc4;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.meth*AdvicedMethod())
     */
    Pointcut member_pc5;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.method*icedMethodNewThread(..))
     */
    Pointcut member_pc6;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.method*dvicedMethodNewThread(..))
     */
    Pointcut member_pc7;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.multipleMethodAdvicedMethod(..))
     */
    Pointcut member_pc8;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.multipleChainedMethodAdvicedMethod(..))
     */
    Pointcut member_pc9;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.joinPointMetaData(..))
     */
    Pointcut member_pc10;

    /**
     * @Expression execution(void test.MemberMethodAdviceTest.passingParameterToAdviceMethod(..))
     */
    Pointcut member_pc11;

    /**
     * @Expression execution(void test.MemberMethodAdviceTest.multiplePointcutsMethod(..))
     */
    Pointcut member_pc12;

    /**
     * @Expression execution(void test.MemberMethodAdviceTest.multiplePointcutsMethod(..))
     */
    Pointcut member_pc13;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.takesArrayAsArgument(String[]))
     */
    Pointcut member_pc14;

    /**
     * @Expression execution(long test.MemberMethodAdviceTest.getPrimitiveAndNullFromAdvice())
     */
    Pointcut member_pc15;

    /**
     * @Expression execution(void test.MemberMethodAdviceTest.beforeAdvicedMethod())
     */
    Pointcut member_pc16;

    /**
     * @Expression execution(void test.MemberMethodAdviceTest.afterAdvicedMethod())
     */
    Pointcut member_pc17;

    /**
     * @Expression execution(void test.MemberMethodAdviceTest.beforeAfterAdvicedMethod())
     */
    Pointcut member_pc18;

    /**
     * @Expression execution(void test.MemberMethodAdviceTest.beforeAroundAfterAdvicedMethod())
     */
    Pointcut member_pc19;

    /**
     * @Expression execution(* test.MemberMethodAdviceTest.longNoAroundAdvice(..))
     */
    Pointcut noAroundAdvice;

    // ============ Advices ============

    /**
     * @Around member_pc1 || member_pc2 || member_pc3 || member_pc4 || member_pc14 || member_pc9
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * @Around member_pc5 || member_pc8 || member_pc9 || member_pc12 || member_pc19
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        ((Loggable) joinPoint.getTarget()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable) joinPoint.getTarget()).log("after1 ");
        return result;
    }

    /**
     * @Around member_pc8 || member_pc9 || member_pc13 || member_pc19
     */
    public Object advice3(final JoinPoint joinPoint) throws Throwable {
        ((Loggable) joinPoint.getTarget()).log("before2 ");
        final Object result = joinPoint.proceed();
        ((Loggable) joinPoint.getTarget()).log("after2 ");
        return result;
    }

    /**
     * @Around member_pc10
     */
    public Object advice4(JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        MethodRtti rtti = (MethodRtti) joinPoint.getRtti();
        String metadata = joinPoint.getTargetClass().getName()
            + rtti.getMethod().getName()
            + joinPoint.getTarget().hashCode()
            + rtti.getParameterValues()[0]
            + rtti.getParameterTypes()[0].getName()
            + rtti.getReturnType().getName()
            + rtti.getReturnValue();
        return metadata;
    }

    /**
     * @Around member_pc6 || member_pc7
     */
    public Object advice5(final JoinPoint joinPoint) throws Throwable {
        ((Loggable) joinPoint.getTarget()).log("before ");
        final Object result = joinPoint.proceed();
        ((Loggable) joinPoint.getTarget()).log("after ");
        return result;
    }

    /**
     * @Around member_pc15
     */
    public Object advice6(JoinPoint joinPoint) throws Throwable {
        // test to serialize the join point instance
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream("joinpoint.ser"));
            out.writeObject(joinPoint);
            out.close();
            File file = new File("joinpoint.ser");
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            joinPoint = (JoinPoint) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("FIXME: serialization for JIT compiled join points");
        }
        return null;
    }

    /**
     * @Before member_pc16 || member_pc18 || member_pc19 || noAroundAdvice
     */
    public void before(final JoinPoint joinPoint) throws Throwable {
        ((Loggable) joinPoint.getTarget()).log("pre ");
    }

    /**
     * @After member_pc17 || member_pc18 || member_pc19
     */
    public void after(final JoinPoint joinPoint) throws Throwable {
        ((Loggable) joinPoint.getTarget()).log("post ");
    }

}
/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MemberSignature;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.StartupManager;
import org.codehaus.aspectwerkz.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.expression.ExpressionVisitor;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 */
public class JavaLoggingAspect {

    private int m_level = 0;

    /**
     */
    public Object logMethod(final JoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature)joinPoint.getSignature();
        indent();
        System.out.println(
                joinPoint.getType() + "--> " + joinPoint.getTargetClass().getName() + "::" + signature.getName()
        );
        m_level++;
        final Object result = joinPoint.proceed();
        m_level--;
        indent();
        System.out.println(
                joinPoint.getType() + "<-- " + joinPoint.getTargetClass().getName() + "::" + signature.getName()
        );
        return result;
    }

    /**
     */
    public void logEntry(final JoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature)joinPoint.getSignature();
        System.out.println("ENTER: " + joinPoint.getTargetClass().getName() + "::" + signature.getName());
    }

    /**
     */
    public void logExit(final JoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature)joinPoint.getSignature();
        System.out.println("EXIT: " + joinPoint.getTargetClass().getName() + "::" + signature.getName());
    }

    private void indent() {
        for (int i = 0; i < m_level; i++) {
            System.out.print("  ");
        }
    }

    /**
     * A damned complicated API to - alter the def so that new weaving can be done - alter the internal aspect repr. so
     * that runtime management can occur
     * <p/>
     * Note: seems to have a redundancy on the pointcut somewhere. CRAP
     *
     * @param pointcut
     * @param pointcutName
     */
    public static void addPointcutForLoggingAdvice(String pointcut, String pointcutName) {
        //if (true) return;

        final String aspectName = "examples.logging.JavaLoggingAspect";
        SystemDefinition sysDef = DefinitionLoader.getDefinition(HotSwapTarget.class.getClassLoader(), "hotdeployed");
        AspectDefinition aspectDef = sysDef.getAspectDefinition(aspectName);
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(aspectDef.getName());
        ExpressionInfo expressionInfo = new ExpressionInfo(pointcut, aspectDef.getName());
        namespace.addExpressionInfo(pointcutName, expressionInfo);
        AdviceDefinition newDef = null;
        for (Iterator arounds = aspectDef.getAroundAdvices().iterator(); arounds.hasNext();) {
            AdviceDefinition around = (AdviceDefinition)arounds.next();
            if (around.getName().equals(aspectName + ".logMethod")) {
                // copy the logMethod advice
                // note: we could add a totally new advice as well
                newDef = around.copyAt(expressionInfo);
                break;
            }
        }
        aspectDef.addAroundAdvice(newDef);

        //TODO: experimental API
        StartupManager.reinitializeSystem(HotSwapTarget.class.getClassLoader(), sysDef);
        System.out.println("sysDef = " + sysDef.getClass().getClassLoader());

        /*
        ExecutionPointcut pointcutInstance = new ExecutionPointcut("samples", newDef.getExpression());
        PointcutManager pointcutManager = SystemLoader.getSystem("samples").
                getAspectManager().getPointcutManager(aspectName);
        //pointcutManager.addExecutionPointcut(pointcutInstance);//needed only after initialization
        pointcutInstance.addAroundAdvice(aspectName+".logMethod");
        */
    }

    /**
     * A damned complicated API to - alter the def so that pc is removed - alter the internal aspect repr. so that
     * pointcut struct is released (TODO)
     *
     * @param pointcut
     * @param pointcutName
     */
    public static void removePointcutForLoggingAdvice(String pointcut, String pointcutName) {
        //if (true) return;

        final String aspectName = "examples.logging.JavaLoggingAspect";
        SystemDefinition sysDef = DefinitionLoader.getDefinition(HotSwapTarget.class.getClassLoader(), "hotdeployed");
        AspectDefinition aspectDef = sysDef.getAspectDefinition(aspectName);
        List removedAdviceDefs = new ArrayList();
        for (Iterator arounds = aspectDef.getAroundAdvices().iterator(); arounds.hasNext();) {
            AdviceDefinition around = (AdviceDefinition)arounds.next();
            if (pointcutName.equals(around.getExpressionInfo().getExpressionAsString())) {
                System.out.println("<removing> " + around.getName() + " at " + pointcutName);
                removedAdviceDefs.add(around);
            } else {
                //System.out.println("around = " + around.getExpression().getName());
            }
        }
        for (Iterator arounds = removedAdviceDefs.iterator(); arounds.hasNext();) {
            aspectDef.removeAroundAdvice((AdviceDefinition)arounds.next());
        }
        //TODO remove from PointcutManager as well for mem safety ?
    }

}

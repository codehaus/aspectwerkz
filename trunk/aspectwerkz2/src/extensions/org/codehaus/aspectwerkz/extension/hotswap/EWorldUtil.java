/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.hotswap;

import org.codehaus.aspectwerkz.definition.*;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper;
import org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor;
import org.codehaus.aspectwerkz.transform.ClassCacheTuple;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * eworld/wlw/aop
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class EWorldUtil {

    public static void activate(String uuid, String aspectName, String adviceName, String expression, String pointcutName) {
        System.out.println("activate  = " + uuid +","+aspectName+"."+adviceName+" @ "+expression +","+ pointcutName);
        SystemDefinition sysDef = SystemDefinitionContainer.getSystemDefinition(ClassLoader.getSystemClassLoader(), uuid);
        if (sysDef == null) {
            return;
        }
        AspectDefinition aspectDef = sysDef.getAspectDefinition(aspectName);

        Expression pcExpression = ExpressionNamespace.getExpressionNamespace(aspectDef)
                .createExpression(
                        expression,
                        "",
                        pointcutName
                );

        AdviceDefinition newDef = null;
        boolean found = false;
        for (Iterator arounds = aspectDef.getAroundAdvices().iterator(); arounds.hasNext();) {
            AdviceDefinition around = (AdviceDefinition)arounds.next();
            if (around.getName().equals(aspectName + "."+adviceName)) {
                // copy the logMethod advice
                // note: we could add a totally new advice as well
                newDef = around.copyAt(pcExpression);
                System.out.println("<adding> " + around.getName() + " at " + pointcutName);
                found = true;
                break;
            }
        }
        if (!found) {
            System.err.println("  advice not found");
        } else {
            aspectDef.addAroundAdvice(newDef);
            StartupManager.reinitializeSystem(ClassLoader.getSystemClassLoader(), sysDef);
        }
    }

    public static void deactivate(String uuid, String aspectName, String adviceName, String pointcutName) {
        System.out.println("deactivate  = " + uuid +","+aspectName+"."+adviceName+" @ "+ pointcutName);
        SystemDefinition sysDef = SystemDefinitionContainer.getSystemDefinition(ClassLoader.getSystemClassLoader(), uuid);
        if (sysDef == null) {
            return;
        }
        AspectDefinition aspectDef = sysDef.getAspectDefinition(aspectName);

        List removedAdviceDefs = new ArrayList();
        boolean found = false;
        for (Iterator arounds = aspectDef.getAroundAdvices().iterator(); arounds.hasNext();) {
            AdviceDefinition around = (AdviceDefinition)arounds.next();
            if (around.getName().equals(aspectName + "."+adviceName)) {
                found = true;
                if (pointcutName.equals(around.getExpression().getName()) || pointcutName.equals(around.getExpression().getExpression())) {
                    System.out.println("<removing> " + around.getName() + " at " + pointcutName);
                    removedAdviceDefs.add(around);
                }
            }
        }
        if (!found) {
            System.err.println("  advice not found");
        }
        for (Iterator arounds = removedAdviceDefs.iterator(); arounds.hasNext();) {
            aspectDef.removeAroundAdvice((AdviceDefinition)arounds.next());
        }
        StartupManager.reinitializeSystem(ClassLoader.getSystemClassLoader(), sysDef);
    }





    public static void activateCache(String expression, String pointcutName) {
        activate("eworld/wlw/aop", "examples.caching.CachingAspect", "cache", expression, pointcutName);
    }

    public static void deactivateCache(String pointcutName) {
        deactivate("eworld/wlw/aop", "examples.caching.CachingAspect", "cache", pointcutName);
    }

    public static void activateTrace(String expression, String pointcutName) {
        activate("eworld/wlw/aop", "examples.logging.LoggingAspect", "logMethod", expression, pointcutName);
    }

    public static void deactivateTrace(String pointcutName) {
        deactivate("eworld/wlw/aop", "examples.logging.LoggingAspect", "logMethod", pointcutName);
    }

    public static void hotswap(String classPattern) {
        AspectWerkzPreProcessor awpp = (AspectWerkzPreProcessor) ClassPreProcessorHelper.getClassPreProcessor();
        for (Iterator it = awpp.getClassCacheTuples().iterator(); it.hasNext();) {
            ClassCacheTuple tuple = (ClassCacheTuple)it.next();
            if (tuple.getClassName().startsWith(classPattern)) {
                try {
                    System.out.println("hotswap " + tuple.getClassName());
                    HotSwapClient.hotswap(tuple.getClassLoader().loadClass(tuple.getClassName()));
                } catch (Throwable t) {
                    System.err.println("Unable to hotswap " + tuple.getClassName() + ": " + t.getMessage());
                }
            }
        }
    }
}

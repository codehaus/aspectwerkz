/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.cflow;

import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CflowBinding {

    public int cflowID;

    public ExpressionInfo cflowSubExpression;

    public boolean isCflowBelow;

    public CflowBinding(int cflowID, ExpressionInfo cflowSubExpression, boolean isCflowBelow) {
        this.cflowID = cflowID;
        this.cflowSubExpression = cflowSubExpression;
        this.isCflowBelow = isCflowBelow;
    }

    public AspectDefinition getAspectDefinition(SystemDefinition systemDefinition, ClassLoader loader) {
        String aspectName = CflowCompiler.getCflowAspectClassName(cflowID);

        // check if we have already register this aspect
        // TODO: it may happen that the aspect gets register somewhere up in the hierarchy ??
        // it is optim only

        // TODO: how to do this class define lazyly and not pass in a classloader ?
        Class aspectClass = CflowCompiler.compileCflowAspectAndAttachToClassLoader(loader, cflowID);

        ClassInfo cflowAspectInfo = JavaClassInfo.getClassInfo(aspectClass);
        ClassInfo abstractCflowAspectInfo = cflowAspectInfo.getSuperclass();
        MethodInfo beforeAdvice = null;
        MethodInfo afterFinallyAdvice = null;
        for (int i = 0; i < abstractCflowAspectInfo.getMethods().length; i++) {
            MethodInfo methodInfo = abstractCflowAspectInfo.getMethods()[i];
            if (methodInfo.getName().equals("enter")) {
                beforeAdvice = methodInfo;
            } else if (methodInfo.getName().equals("exit")) {
                afterFinallyAdvice = methodInfo;
            }
        }
        if (beforeAdvice == null || afterFinallyAdvice == null) {
            throw new DefinitionException("Could not gather cflow advice from " + aspectName);
        }

        AspectDefinition aspectDef = new AspectDefinition(
                aspectName,
                cflowAspectInfo,
                systemDefinition
        );
        aspectDef.addBeforeAdviceDefinition(
                new AdviceDefinition(
                        beforeAdvice.getName(),
                        AdviceType.BEFORE,
                        null,
                        aspectName,
                        aspectName,
                        cflowSubExpression,
                        beforeAdvice,
                        aspectDef
                )
        );
        aspectDef.addAfterAdviceDefinition(
                new AdviceDefinition(
                        afterFinallyAdvice.getName(),
                        AdviceType.AFTER_FINALLY,
                        null,
                        aspectName,
                        aspectName,
                        cflowSubExpression,
                        afterFinallyAdvice,
                        aspectDef
                )
        );

        return aspectDef;
    }

    public static List getCflowAspectDefinitionForCflowOf(AspectDefinition aspect) {
        List cflowBindings = new ArrayList();

        // handles advices
        for (Iterator iterator = aspect.getAdviceDefinitions().iterator(); iterator.hasNext();) {
            AdviceDefinition adviceDefinition = (AdviceDefinition) iterator.next();
            ExpressionInfo binding = adviceDefinition.getExpressionInfo();
            if (binding != null) {
                binding.getCflowAspectExpression().populateCflowAspectBindings(cflowBindings);
            }
        }

        // TODO do we have to take care of non bounded pointcuts as well ?
        // aspect.getPointcutDefinitions()

        return cflowBindings;
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.util.ContextClassLoader;
import org.codehaus.aspectwerkz.util.ContextClassLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.expression.CflowStack;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaMethodInfo;
import org.codehaus.aspectwerkz.reflect.ReflectHelper;
import org.codehaus.aspectwerkz.AspectContext;
import org.codehaus.aspectwerkz.DeploymentModel;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the cflow pointcuts.
 *
 * TODO REMOVE ME
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class CFlowSystemAspect {
    /**
     * The class name for the aspect.
     */
    public static final String CLASS_NAME = CFlowSystemAspect.class.getName();

    /**
     * A unique name for the aspect.
     */
    public static final String NAME = CLASS_NAME.replace('.', '$');

    /**
     * The deployment model for the aspect.
     */
    public static final DeploymentModel DEPLOYMENT_MODEL = null; // FIXME - broken, used to be PER_THREAD

    /**
     * The name of the pre advice method.
     */
    public static final String PRE_ADVICE = "enterControlFlow";

    /**
     * The name of the post advice method.
     */
    public static final String POST_ADVICE = "exitControlFlow";

    /**
     * Index of the pre advice method.
     */
    public static final int PRE_ADVICE_INDEX;

    /**
     * Index of the post advice method.
     */
    public static final int POST_ADVICE_INDEX;

    static {
        // set the method flow indexes
        // this is used when the aspect is registered in the system
        // we assume enterControlFlow and exitControlFlow are defined once in this class
        List methods = ReflectHelper.createSortedMethodList(CFlowSystemAspect.class);
        int index = 0;
        int preIndex = 0;
        int postIndex = 0;
        for (Iterator i = methods.iterator(); i.hasNext(); index++) {
            Method m = (Method) i.next();
            if (PRE_ADVICE.equals(m.getName())) {
                preIndex = index;
            } else if (POST_ADVICE.equals(m.getName())) {
                postIndex = index;
            }
        }
        PRE_ADVICE_INDEX = preIndex;
        POST_ADVICE_INDEX = postIndex;
    }

    /**
     * Reference to the system.
     */
    private CflowStack m_cflowStack = null;

    /**
     * Creates a new cflow system aspect instance.
     *
     * @param info the cross-cutting info
     */
    public CFlowSystemAspect(final AspectContext info) {
        // FIXME XXX is the context CL the correct one?
        m_cflowStack = CflowStack.getCflowStack(ContextClassLoader.getLoader());
    }

    /**
     * Registers the join point as the start of a control flow (cflow) in the system.
     *
     * @param joinPoint the join point
     * @throws Throwable the exception from the invocation
     */
    public void enterControlFlow(final JoinPoint joinPoint) throws Throwable {
        m_cflowStack.enteringControlFlow(
                getPointcutType(joinPoint),
                createMethodInfo(joinPoint),
                createWithinInfo(joinPoint)
        );
    }

    /**
     * Registers the join point as the end of a control flow (cflow) in the system.
     *
     * @param joinPoint the join point
     * @throws Throwable the exception from the invocation
     */
    public void exitControlFlow(final JoinPoint joinPoint) throws Throwable {
        m_cflowStack.exitingControlFlow(
                getPointcutType(joinPoint),
                createMethodInfo(joinPoint),
                createWithinInfo(joinPoint)
        );
    }

    /**
     * Returns the pointcut type for the join point.
     *
     * @param joinPoint the join point
     * @return the pointcut type
     */
    private PointcutType getPointcutType(final JoinPoint joinPoint) {
        String type = joinPoint.getType();
        if (type.equals(JoinPoint.METHOD_EXECUTION) || type.equals(JoinPoint.CONSTRUCTOR_EXECUTION)) {
            return PointcutType.EXECUTION;
        } else if (type.equals(JoinPoint.METHOD_CALL) || type.equals(JoinPoint.CONSTRUCTOR_CALL)) {
            return PointcutType.CALL;
        } else if (type.endsWith(JoinPoint.FIELD_SET)) {
            return PointcutType.SET;
        } else if (type.endsWith(JoinPoint.FIELD_GET)) {
            return PointcutType.GET;
        } else if (type.equals(JoinPoint.HANDLER)) {
            return PointcutType.HANDLER;
        } else if (type.endsWith(JoinPoint.STATIC_INITIALIZATION)) {
            return PointcutType.STATIC_INITIALIZATION;
        } else {
            throw new IllegalStateException("join point [" + type + "] is unknown");
        }
    }

    /**
     * Creates info for the method.
     *
     * @return the created method info
     */
    private static MethodInfo createMethodInfo(final JoinPoint joinPoint) {
        throw new UnsupportedOperationException("todo - use Rtti explicitly");
        //MethodRtti rtti = (MethodRtti) joinPoint.getRtti();
        //Method method = rtti.getMethod();
        //return JavaMethodInfo.getMethodInfo(method);
    }

    /**
     * Creates info for the within class.
     *
     * @return the created within info
     */
    private static ClassInfo createWithinInfo(final JoinPoint joinPoint) {
        Class targetClass = joinPoint.getTargetClass();
        return JavaClassInfo.getClassInfo(targetClass);
    }
}
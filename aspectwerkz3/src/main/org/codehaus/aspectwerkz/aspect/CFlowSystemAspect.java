/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaMethodInfo;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

import java.lang.reflect.Method;

import java.util.Iterator;
import java.util.List;

/**
 * Manages the cflow pointcuts.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CFlowSystemAspect
{
    /**
     * A unique name for the aspect.
     */
    public static final String NAME = "org$codehaus$aspectwerkz$aspect$CFlowSystemAspect";

    /**
     * The class name for the aspect.
     */
    public static final String CLASS_NAME = CFlowSystemAspect.class.getName();

    /**
     * The deployment model for the aspect.
     */
    public static final String DEPLOYMENT_MODEL = SystemDefinition.PER_THREAD;

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

    static
    {
        // set the method flow indexes
        // this is used when the aspect is registered in the system
        // we assume enterControlFlow and exitControlFlow are defined once in this class
        List methods = TransformationUtil.createSortedMethodList(CFlowSystemAspect.class);
        int index = 0;
        int preIndex = 0;
        int postIndex = 0;

        for (Iterator i = methods.iterator(); i.hasNext(); index++)
        {
            Method m = (Method) i.next();

            if (PRE_ADVICE.equals(m.getName()))
            {
                preIndex = index;
            }
            else if (POST_ADVICE.equals(m.getName()))
            {
                postIndex = index;
            }
        }

        PRE_ADVICE_INDEX = preIndex;
        POST_ADVICE_INDEX = postIndex;
    }

    /**
     * Reference to the system.
     */
    private AspectSystem m_system = null;

    /**
     * The cross-cutting info.
     */
    private final CrossCuttingInfo m_crossCuttingInfo;

    /**
     * Creates a new cflow system aspect instance.
     *
     * @param info the cross-cutting info
     */
    public CFlowSystemAspect(final CrossCuttingInfo info)
    {
        m_crossCuttingInfo = info;
        m_system = info.getSystem();
    }

    /**
     * Registers the join point as the start of a control flow (cflow) in the system.
     *
     * @param joinPoint the join point
     * @throws Throwable the exception from the invocation
     */
    public void enterControlFlow(final JoinPoint joinPoint)
        throws Throwable
    {
        m_system.enteringControlFlow(getMethodInfo(joinPoint));
    }

    /**
     * Registers the join point as the end of a control flow (cflow) in the system.
     *
     * @param joinPoint the join point
     * @throws Throwable the exception from the invocation
     */
    public void exitControlFlow(final JoinPoint joinPoint)
        throws Throwable
    {
        m_system.exitingControlFlow(getMethodInfo(joinPoint));
    }

    /**
     * Creates and returns the meta-data for the join point. Uses a cache.
     *
     * @param joinPoint the join point
     * @return the meta-data
     * @todo should use a cache (used to cache on the Method instance but at caller side pointcuts no Method instance is
     * available)
     */
    private static MethodInfo getMethodInfo(final JoinPoint joinPoint)
    {
        return createMethodInfo(joinPoint);
    }

    /**
     * Creates meta-data for the method.
     *
     * @return the created method meta-data
     */
    private static MethodInfo createMethodInfo(final JoinPoint joinPoint)
    {
        MethodRtti rtti = (MethodRtti) joinPoint.getRtti();
        Method method = rtti.getMethod();

        return JavaMethodInfo.getMethodInfo(method);
    }
}

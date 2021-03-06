/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.metadata.CflowMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MetaDataMaker;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

import java.lang.reflect.Method;

import java.util.Iterator;
import java.util.List;

/**
 * Manages the cflow pointcuts.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
    public static final String CLASS_NAME = "org.codehaus.aspectwerkz.aspect.CFlowSystemAspect";

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
        m_system.enteringControlFlow(getMetaData(joinPoint));
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
        m_system.exitingControlFlow(getMetaData(joinPoint));
    }

    /**
     * Creates and returns the meta-data for the join point. Uses a cache.
     *
     * @param joinPoint the join point
     * @return the meta-data
     * @todo should use a cache (used to cache on the Method instance but at caller side pointcuts no Method instance is
     * available)
     */
    private static CflowMetaData getMetaData(final JoinPoint joinPoint)
    {
        return new CflowMetaData(createClassMetaData(joinPoint),
            createMethodMetaData(joinPoint));
    }

    /**
     * Creates meta-data for the class. Note: we use a contextual class loader to access the Class object
     *
     * @return the created class meta-data
     */
    private static ClassMetaData createClassMetaData(final JoinPoint joinPoint)
    {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // TODO AV - The following leads to a synchronize at runtime to grab the MetaDataMaker
        //         we cannot do that at the Aspect init level since an Aspect CL can be upper in the CL hierarchy
        // the ReflectionMetaDataMaker might be separated from the TF one may be (and focus 1.5 API)
        return MetaDataMaker.getReflectionMetaDataMaker(signature.getDeclaringType()
                                                                 .getClassLoader())
                            .createClassMetaData(signature.getDeclaringType());
    }

    /**
     * Creates meta-data for the method.
     *
     * @return the created method meta-data
     */
    private static MethodMetaData createMethodMetaData(
        final JoinPoint joinPoint)
    {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        return ReflectionMetaDataMaker.createMethodMetaData(signature.getName(),
            signature.getParameterTypes(), signature.getReturnType());
    }

    public void postCreate()
    {
        ;
    }
}

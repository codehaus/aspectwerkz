/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.aspect;

import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.metadata.ClassNameMethodMetaDataTuple;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

import java.util.List;
import java.util.Iterator;
import java.lang.reflect.Method;

/**
 * Manages the cflow pointcuts.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CFlowSystemAspect extends Aspect {

    /**
     * A unique name for the aspect.
     */
    public static final String NAME = "org$codehaus$aspectwerkz$attribdef$aspect$CFlowSystemAspect";

    /**
     * The class name for the aspect.
     */
    public static final String CLASS_NAME = "org.codehaus.aspectwerkz.attribdef.aspect.CFlowSystemAspect";

    /**
     * The deployment model for the aspect.
     */
    public static final String DEPLOYMENT_MODEL = AspectWerkzDefinition.PER_THREAD;

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
        List methods = TransformationUtil.createSortedMethodList(CFlowSystemAspect.class);
        int index = 0;
        int preIndex = 0;
        int postIndex = 0;
        for (Iterator i = methods.iterator(); i.hasNext(); index++) {
            Method m = (Method)i.next();
            if (PRE_ADVICE.equals(m.getName()))
                preIndex = index;
            else if (POST_ADVICE.equals(m.getName()))
                postIndex = index;
        }
        PRE_ADVICE_INDEX = preIndex;
        POST_ADVICE_INDEX = postIndex;
    }

    /**
     * Registers the join point as the start of a control flow (cflow) in the system.
     *
     * @param joinPoint the join point
     * @throws Throwable the exception from the invocation
     */
    public void enterControlFlow(final JoinPoint joinPoint) throws Throwable {
        ___AW_getSystem().enteringControlFlow(getMetaData(joinPoint));
    }

    /**
     * Registers the join point as the end of a control flow (cflow) in the system.
     *
     * @param joinPoint the join point
     * @throws Throwable the exception from the invocation
     */
    public void exitControlFlow(final JoinPoint joinPoint) throws Throwable {
        ___AW_getSystem().exitingControlFlow(getMetaData(joinPoint));
    }

    /**
     * Creates meta-data for the method.
     *
     * @return the created method meta-data
     */
    private static MethodMetaData createMetaData(final CallerSideJoinPoint joinPoint) {
        return ReflectionMetaDataMaker.createMethodMetaData(
                joinPoint.getCalleeMethodName(),
                joinPoint.getCalleeMethodParameterTypes(),
                joinPoint.getCalleeMethodReturnType());
    }

    /**
     * Creates and returns the meta-data for the join point. Uses a cache.
     *
     * @todo should use a cache (used to cache on the Method instance but at caller side pointcuts no Method instance is available)
     *
     * @param joinPoint the join point
     * @return the meta-data
     */
    private ClassNameMethodMetaDataTuple getMetaData(final JoinPoint joinPoint) {
        CallerSideJoinPoint jp = ((CallerSideJoinPoint)joinPoint);
        return new ClassNameMethodMetaDataTuple(jp.getCalleeClassName(), createMetaData(jp));
    }
}

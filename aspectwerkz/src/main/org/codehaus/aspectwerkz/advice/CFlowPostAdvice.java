/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.advice;

import java.util.Map;
import java.util.WeakHashMap;

import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassNameMethodMetaDataTuple;

/**
 * Registers the join point as the end of a control flow (cflow) in the AspectWerkz system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class CFlowPostAdvice extends PostAdvice {

    /**
     * A unique name for the advice.
     */
    public static final String NAME = "org$codehaus$aspectwerkz$advice$CFlowPostAdvice";

    /**
     * The deployment model for the advice.
     */
    public static final String DEPLOYMENT_MODEL = AspectWerkzDefinition.PER_THREAD;

    /**
     * Caches the meta-data A maps it to the method instance.
     */
    private final Map m_metaDataCache = new WeakHashMap();

    /**
     * Creates a new cflow advice.
     */
    public CFlowPostAdvice() {
        super();
    }

    /**
     * Registers the join point as the end of a control flow (cflow) in the system.
     *
     * @param joinPoint the join point
     * @return the result from the invocation
     * @throws Throwable the exception from the invocation
     */
    public void execute(final JoinPoint joinPoint) throws Throwable {
        getSystem().exitingControlFlow(getMetaData(joinPoint));
    }

    /**
     * Returns the definition for this advice.
     *
     * @return the definition
     */
    public static AdviceDefinition getDefinition() {
        AdviceDefinition definition = new AdviceDefinition();
        definition.setName(NAME);
        definition.setAdviceClassName(CFlowPostAdvice.class.getName());
        definition.setDeploymentModel(DEPLOYMENT_MODEL);
        return definition;
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
     * Creates A returns the meta-data for the join point. Uses a cache.
     *
     * @todo should use a cache (used to cache on the Method instance but at caller side pointcuts no Method instance is available)
     *
     * @param joinPoint the join point
     * @return the meta-data
     */
    private ClassNameMethodMetaDataTuple getMetaData(final JoinPoint joinPoint) {
        CallerSideJoinPoint jp = ((CallerSideJoinPoint)joinPoint);
        ClassNameMethodMetaDataTuple metaData;

//        Method method = jp.getMethod();
//        Object cachedMetaData = m_metaDataCache.get(method);
//        if (cachedMetaData == null) {
            metaData = new ClassNameMethodMetaDataTuple(jp.getCalleeClassName(), createMetaData(jp));
//            m_metaDataCache.put(method, metaData);
//        }
//        else {
//            metaData = (ClassNameMethodMetaDataTuple)cachedMetaData;
//        }
        return metaData;
    }
}
/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
 * Registers the join point as the start of a control flow (cflow) in the AspectWerkz system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: CFlowPreAdvice.java,v 1.2 2003-07-23 14:20:30 avasseur Exp $
 */
public class CFlowPreAdvice extends PreAdvice {

    /**
     * A unique name for the advice.
     */
    public static final String NAME = "org$codehaus$aspectwerkz$advice$CFlowPreAdvice";

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
    public CFlowPreAdvice() {
        super();
    }

    /**
     * Registers the join point as the start of a control flow (cflow) in the system.
     *
     * @param joinPoint the join point
     * @return the result from the invocation
     * @throws Throwable the exception from the invocation
     */
    public void execute(final JoinPoint joinPoint) throws Throwable {
        getSystem().enteringControlFlow(getMetaData(joinPoint));
    }

    /**
     * Returns the definition for this advice.
     *
     * @return the definition
     */
    public static AdviceDefinition getDefinition() {
        AdviceDefinition definition = new AdviceDefinition();
        definition.setName(NAME);
        definition.setAdviceClassName(CFlowPreAdvice.class.getName());
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

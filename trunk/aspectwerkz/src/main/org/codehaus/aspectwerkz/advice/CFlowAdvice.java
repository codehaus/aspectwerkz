/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassNameMethodMetaDataTuple;

/**
 * Registers the join point as the start of a control flow (cflow) in the AspectWerkz system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: CFlowAdvice.java,v 1.1 2003-06-30 16:00:06 jboner Exp $
 */
public class CFlowAdvice extends AroundAdvice {

    /**
     * A unique name for the advice.
     */
    public static final String NAME = "org$codehaus$aspectwerkz$advice$CFlowAdvice";

    /**
     * The deployment model for the advice.
     */
    public static final String DEPLOYMENT_MODEL = AspectWerkzDefinition.PER_THREAD;

    /**
     * Caches the meta-data and maps it to the method instance.
     */
    private final Map m_metaDataCache = new HashMap();

    /**
     * Creates a new cflow advice.
     */
    public CFlowAdvice() {
        super();
    }

    /**
     * Registers the join point as the start of a control flow (cflow) in the system.
     *
     * @param joinPoint the join point
     * @return the result from the invocation
     * @throws Throwable the exception from the invocation
     */
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        getSystem().enteringControlFlow(getMetaData(joinPoint));
        Object result = joinPoint.proceed();
        getSystem().exitingControlFlow(getMetaData(joinPoint));
        return result;
    }

    /**
     * Returns the definition for this advice.
     *
     * @return the definition
     */
    public static AdviceDefinition getDefinition() {
        AdviceDefinition definition = new AdviceDefinition();
        definition.setName(NAME);
        definition.setAdviceClassName(CFlowAdvice.class.getName());
        definition.setDeploymentModel(DEPLOYMENT_MODEL);
        return definition;
    }

    /**
     * Creates meta-data for the method.
     *
     * @todo should perhaps use a cache
     *
     * @return the created method meta-data
     */
    private MethodMetaData createMetaData(final MethodJoinPoint joinPoint) {
        return ReflectionMetaDataMaker.createMethodMetaData(
                joinPoint.getMethodName(),
                joinPoint.getParameterTypes(),
                joinPoint.getReturnType());
    }

    /**
     * Creates and returns the meta-data for the join point. Uses a cache.
     *
     * @param joinPoint the join point
     * @return the meta-data
     */
    private ClassNameMethodMetaDataTuple getMetaData(final JoinPoint joinPoint) {
        MethodJoinPoint jp = ((MethodJoinPoint)joinPoint);
        Method method = jp.getMethod();

        ClassNameMethodMetaDataTuple metaData;
        Object cachedMetaData = m_metaDataCache.get(method);
        if (cachedMetaData == null) {
            metaData = new ClassNameMethodMetaDataTuple(
                    joinPoint.getTargetClass().getName(), createMetaData(jp));
            m_metaDataCache.put(method, metaData);
        }
        else {
            metaData = (ClassNameMethodMetaDataTuple)cachedMetaData;
        }
        return metaData;
    }
}

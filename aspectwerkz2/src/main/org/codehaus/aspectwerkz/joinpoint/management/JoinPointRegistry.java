/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gnu.trove.TLongObjectHashMap;
import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.RuntimeSystem;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;

/**
 * Manages the registration of join points and advices for these join points.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
class JoinPointRegistry {

    /**
     * Pre allocated empty array list.
     */
    private static final List EMTPY_ARRAY_LIST = new ArrayList();

    /**
     * Pre allocated empty index tuple array.
     */
    private static final IndexTuple[] EMPTY_INDEX_TUPLE_ARRAY = new IndexTuple[]{};

    /**
     * The registry with all the classes and the index for the advices attatched to the join points in this class.
     * <p/>
     * Map of: the class hash => map of: join point hash => map of: join point type => array with advice indexes.
     */
    private static final TLongObjectHashMap m_joinPointAdvicesMap = new TLongObjectHashMap();

    /**
     * Registers the advices for the method join point.
     *
     * @param joinPointType
     * @param joinPointHash
     * @param signature
     * @param classHash
     * @param definedClass
     * @param definedClassMetaData
     * @param system
     * @TODO: cache the metadata created in the method - map it to the method hash (see pointcut for caching)
     */
    public void registerJoinPoint(
            final int joinPointType,
            final int joinPointHash,
            final String signature,
            final int classHash,
            final Class definedClass,
            final ClassMetaData definedClassMetaData,
            final RuntimeSystem system) {

        if (!m_joinPointAdvicesMap.containsKey(classHash)) {
            m_joinPointAdvicesMap.put(classHash, new TLongObjectHashMap());
        }

        Map pointcutTypeToAdvicesMap = setUpPointcutTypeMap();

        TLongObjectHashMap joinPointHashToPointcutTypesMap = (TLongObjectHashMap)m_joinPointAdvicesMap.get(classHash);
        joinPointHashToPointcutTypesMap.put(joinPointHash, pointcutTypeToAdvicesMap);

        switch (joinPointType) {

            case JoinPointType.METHOD_EXECUTION:
                registerMethodExecutionJoinPoint(
                        system, definedClass, definedClassMetaData, joinPointHash, pointcutTypeToAdvicesMap
                );
                break;

            case JoinPointType.METHOD_CALL:
                registerMethodCallJoinPoint(
                        system, definedClass, definedClassMetaData, joinPointHash, pointcutTypeToAdvicesMap
                );
                break;

            case JoinPointType.CONSTRUCTOR_EXECUTION:
                registerConstructorExecutionJoinPoint(
                        system, definedClass, definedClassMetaData, joinPointHash, pointcutTypeToAdvicesMap
                );
                break;

            case JoinPointType.CONSTRUCTOR_CALL:
                registerConstructorCallJoinPoint(
                        system, definedClass, definedClassMetaData, joinPointHash, pointcutTypeToAdvicesMap
                );
                break;

            case JoinPointType.FIELD_SET:
                registerFieldSetJoinPoint(system, definedClassMetaData, signature, pointcutTypeToAdvicesMap);
                break;

            case JoinPointType.FIELD_GET:
                registerFieldGetJoinPoint(system, definedClassMetaData, signature, pointcutTypeToAdvicesMap);
                break;

            case JoinPointType.HANDLER:
                registerHandlerJoinPoint(system, definedClassMetaData, pointcutTypeToAdvicesMap);
                break;

            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("not implemented");

            default:
                throw new RuntimeException("join point type not valid");
        }
    }

    /**
     * Returns the keys to the advices for the join point.
     *
     * @param classHash
     * @param joinPointHash
     * @return the advices attached to the join point
     */
    public Map getAdvicesForJoinPoint(final long classHash, final long joinPointHash) {
        TLongObjectHashMap joinPoints = (TLongObjectHashMap)m_joinPointAdvicesMap.get(classHash);
        return (Map)joinPoints.get(joinPointHash);
    }

    /**
     * Register method execution join points.
     *
     * @param system
     * @param definedClass
     * @param definedClassMetaData
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     */
    private void registerMethodExecutionJoinPoint(
            final RuntimeSystem system,
            final Class definedClass,
            final ClassMetaData definedClassMetaData,
            final int joinPointHash,
            final Map pointcutTypeToAdvicesMap) {

        List executionAdvices = new ArrayList();
        MethodTuple methodTuple = system.getAspectManager().getMethodTuple(definedClass, joinPointHash);
        Method wrapperMethod = methodTuple.getWrapperMethod();
        List executionPointcuts = system.getAspectManager().getExecutionPointcuts(
                definedClassMetaData,
                ReflectionMetaDataMaker.createMethodMetaData(wrapperMethod)
        );
        for (Iterator it = executionPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(
                    pointcut.getAroundAdviceIndexes(),
                    pointcut.getBeforeAdviceIndexes(),
                    pointcut.getAfterAdviceIndexes()
            );
            executionAdvices.add(advices);
        }
        AdviceContainer[] adviceContainers = new AdviceContainer[executionAdvices.size()];
        int i = 0;
        for (Iterator iterator = executionAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }
        pointcutTypeToAdvicesMap.put(PointcutType.EXECUTION, adviceContainers);
        return;
    }

    /**
     * Register method call join points.
     *
     * @param system
     * @param definedClass
     * @param definedClassMetaData
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     */
    private void registerMethodCallJoinPoint(
            final RuntimeSystem system,
            final Class definedClass,
            final ClassMetaData definedClassMetaData,
            final int joinPointHash,
            final Map pointcutTypeToAdvicesMap) {

        List methodCallAdvices = new ArrayList();
        List methodCallPointcuts = system.getAspectManager().getCallPointcuts(
                definedClassMetaData,
                ReflectionMetaDataMaker.createMethodMetaData(
                        system.getAspectManager().
                        getMethodTuple(definedClass, joinPointHash)
                        .getWrapperMethod()
                )
        );
        for (Iterator it = methodCallPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(
                    pointcut.getAroundAdviceIndexes(),
                    pointcut.getBeforeAdviceIndexes(),
                    pointcut.getAfterAdviceIndexes()
            );
            methodCallAdvices.add(advices);
        }
        AdviceContainer[] adviceContainers = new AdviceContainer[methodCallAdvices.size()];
        int i = 0;
        for (Iterator iterator = methodCallAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }
        pointcutTypeToAdvicesMap.put(PointcutType.CALL, adviceContainers);
    }

    /**
     * Register constructor execution join points.
     *
     * @param system
     * @param definedClass
     * @param definedClassMetaData
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     */
    private void registerConstructorExecutionJoinPoint(
            final RuntimeSystem system,
            final Class definedClass,
            final ClassMetaData definedClassMetaData,
            final int joinPointHash,
            final Map pointcutTypeToAdvicesMap) {

        List executionAdvices = new ArrayList();
        ConstructorTuple constructorTuple = system.getAspectManager().getConstructorTuple(definedClass, joinPointHash);
        Constructor wrapperConstructor = constructorTuple.getWrapperConstructor();

        List executionPointcuts = system.getAspectManager().getExecutionPointcuts(
                definedClassMetaData,
                ReflectionMetaDataMaker.createConstructorMetaData(wrapperConstructor)
        );
        for (Iterator it = executionPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(
                    pointcut.getAroundAdviceIndexes(),
                    pointcut.getBeforeAdviceIndexes(),
                    pointcut.getAfterAdviceIndexes()
            );
            executionAdvices.add(advices);
        }
        AdviceContainer[] adviceContainers = new AdviceContainer[executionAdvices.size()];
        int i = 0;
        for (Iterator iterator = executionAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }
        pointcutTypeToAdvicesMap.put(PointcutType.EXECUTION, adviceContainers);
    }

    /**
     * Register constructor call join points.
     *
     * @param system
     * @param definedClass
     * @param definedClassMetaData
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     */
    private void registerConstructorCallJoinPoint(
            final RuntimeSystem system,
            final Class definedClass,
            final ClassMetaData definedClassMetaData,
            final int joinPointHash,
            final Map pointcutTypeToAdvicesMap) {

        List constructorCallAdvices = new ArrayList();
        List constructorCallPointcuts = system.getAspectManager().getCallPointcuts(
                definedClassMetaData,
                ReflectionMetaDataMaker.createConstructorMetaData(
                        system.getAspectManager().
                        getConstructorTuple(definedClass, joinPointHash)
                        .getWrapperConstructor()
                )
        );
        for (Iterator it = constructorCallPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(
                    pointcut.getAroundAdviceIndexes(),
                    pointcut.getBeforeAdviceIndexes(),
                    pointcut.getAfterAdviceIndexes()
            );
            constructorCallAdvices.add(advices);
        }
        AdviceContainer[] adviceContainers = new AdviceContainer[constructorCallAdvices.size()];
        int i = 0;
        for (Iterator iterator = constructorCallAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }
        pointcutTypeToAdvicesMap.put(PointcutType.CALL, adviceContainers);
    }

    /**
     * Register field set join points.
     *
     * @param system
     * @param definedClassMetaData
     * @param signature
     * @param pointcutTypeToAdvicesMap
     */
    private void registerFieldSetJoinPoint(
            final RuntimeSystem system,
            final ClassMetaData definedClassMetaData,
            final String signature,
            final Map pointcutTypeToAdvicesMap) {

        List setAdvices = new ArrayList();
        List setPointcuts = system.getAspectManager().getSetPointcuts(
                definedClassMetaData,
                ReflectionMetaDataMaker.createFieldMetaData(signature)
        );
        for (Iterator it = setPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(
                    pointcut.getAroundAdviceIndexes(),
                    pointcut.getBeforeAdviceIndexes(),
                    pointcut.getAfterAdviceIndexes()
            );
            setAdvices.add(advices);
        }
        AdviceContainer[] adviceContainers = new AdviceContainer[setAdvices.size()];
        int i = 0;
        for (Iterator iterator = setAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }
        pointcutTypeToAdvicesMap.put(PointcutType.SET, adviceContainers);
    }

    /**
     * Register field get join points.
     *
     * @param system
     * @param definedClassMetaData
     * @param signature
     * @param pointcutTypeToAdvicesMap
     */
    private void registerFieldGetJoinPoint(
            final RuntimeSystem system,
            final ClassMetaData definedClassMetaData,
            final String signature,
            final Map pointcutTypeToAdvicesMap) {

        List getAdvices = new ArrayList();
        List getPointcuts = system.getAspectManager().getGetPointcuts(
                definedClassMetaData,
                ReflectionMetaDataMaker.createFieldMetaData(signature)
        );
        for (Iterator it = getPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(
                    pointcut.getAroundAdviceIndexes(),
                    pointcut.getBeforeAdviceIndexes(),
                    pointcut.getAfterAdviceIndexes()
            );
            getAdvices.add(advices);
        }
        AdviceContainer[] adviceContainers = new AdviceContainer[getAdvices.size()];
        int i = 0;
        for (Iterator iterator = getAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }
        pointcutTypeToAdvicesMap.put(PointcutType.GET, adviceContainers);
    }

    /**
     * Register handler join points.
     *
     * @param system
     * @param exceptionClassMetaData
     * @param pointcutTypeToAdvicesMap
     */
    private void registerHandlerJoinPoint(
            final RuntimeSystem system,
            final ClassMetaData exceptionClassMetaData,
            final Map pointcutTypeToAdvicesMap) {
        List handlerAdvices = new ArrayList();
        List handlerPointcuts = system.getAspectManager().getHandlerPointcuts(exceptionClassMetaData);
        for (Iterator it = handlerPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(
                    EMPTY_INDEX_TUPLE_ARRAY,
                    pointcut.getBeforeAdviceIndexes(),
                    EMPTY_INDEX_TUPLE_ARRAY
            );
            handlerAdvices.add(advices);
        }
        AdviceContainer[] adviceContainers = new AdviceContainer[handlerAdvices.size()];
        int i = 0;
        for (Iterator iterator = handlerAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }
        pointcutTypeToAdvicesMap.put(PointcutType.HANDLER, adviceContainers);
    }

    /**
     * Creates a map with the pointcut types mapped to array lists.
     *
     * @return the map
     */
    private Map setUpPointcutTypeMap() {
        Map pointcutTypeToAdvicesMap = new HashMap();
        pointcutTypeToAdvicesMap.put(PointcutType.EXECUTION, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.CALL, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.SET, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.GET, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.HANDLER, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.ATTRIBUTE, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.CLASS, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.CFLOW, EMTPY_ARRAY_LIST);
        return pointcutTypeToAdvicesMap;
    }

    /**
     * Package private constructor.
     */
    JoinPointRegistry() {
    }
}

/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import gnu.trove.TLongObjectHashMap;
import gnu.trove.TLongIntHashMap;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.pointcut.ExecutionPointcut;
import org.codehaus.aspectwerkz.pointcut.CallPointcut;
import org.codehaus.aspectwerkz.pointcut.GetPointcut;
import org.codehaus.aspectwerkz.pointcut.SetPointcut;

/**
 * Manages the registration of join points and advices for these join points.
 * <p/>
 * Manages the states of all join points.
 * <p/>
 * A join point can be in one of the following states:
 * <ol>
 *      <li>
 *          <code>JoinPointState.NOT_ADVISED</code> - the join point is not advised
 *      </li>
 *      <li>
 *          <code>JoinPointState.ADVISED</code> -     the join point is advised (this does not mean that it has advices attached to it)
 *      </li>
 *      <li>
 *          <code>JoinPointState.HAS_ADVICES</code> - the join point has advices attached to it
 *      </li>
 *      <li>
 *          <code>JoinPointState.REDEFINED</code>  -  the definition of the join point had been changed, needs to be updated
 *      </li>
 * </ol>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JoinPointRegistry {

    /**
     * Pre allocated empty array list.
     */
    private static final List EMTPY_ARRAY_LIST = new ArrayList();

    /**
     * The registry with all the classes and the status of their join points.
     */
    private static final TLongObjectHashMap m_joinPointStateMap = new TLongObjectHashMap();

    /**
     * The registry with all the classes and the index for the advices attatched to the join points in this class.
     * <p/>
     * Map of: the class hash => map of: join point hash => map of: join point type => array with advice indexes.
     */
    private static final TLongObjectHashMap m_joinPointAdvicesMap = new TLongObjectHashMap();

    /**
     * Registers the method join point.
     *
     * @param joinPointType
     * @param joinPointHash
     * @param signature
     * @param classHash
     * @param definedClass
     * @param definedClassMetaData
     * @param system
     */
    public void registerJoinPoint(final int joinPointType,
                                  final int joinPointHash,
                                  final String signature,
                                  final long classHash,
                                  final Class definedClass,
                                  final ClassMetaData definedClassMetaData,
                                  final System system) {
        registerStateForJoinPoint(joinPointHash, classHash);
        registerAdvicesForJoinPoint(
                joinPointType, joinPointHash, signature, classHash,
                definedClass, definedClassMetaData, system
        );
    }

    /**
     * Registes the state for the join point.
     *
     * @param joinPointHash
     * @param classHash
     */
    public void registerStateForJoinPoint(final long joinPointHash, final long classHash) {
        if (!m_joinPointStateMap.containsKey(classHash)) {
            m_joinPointStateMap.put(classHash, new TLongIntHashMap());
        }
        TLongIntHashMap joinPointStatus = (TLongIntHashMap)m_joinPointStateMap.get(classHash);
        joinPointStatus.put(joinPointHash, JoinPointState.NOT_ADVISED);
    }

    /**
     * Registers the advices for the method join point.
     *
     * @TODO: clean up and refactor
     * @TODO: cache the metadata created in the method - map it to the method hash (see pointcut for caching)
     *
     * @param joinPointType
     * @param joinPointHash
     * @param signature
     * @param classHash
     * @param definedClass
     * @param definedClassMetaData
     * @param system
     */
    public void registerAdvicesForJoinPoint(final int joinPointType,
                                            final int joinPointHash,
                                            final String signature,
                                            final long classHash,
                                            final Class definedClass,
                                            final ClassMetaData definedClassMetaData,
                                            final System system) {
        if (!m_joinPointAdvicesMap.containsKey(classHash)) {
            m_joinPointAdvicesMap.put(classHash, new TLongObjectHashMap());
        }

        Map pointcutTypeToAdvicesMap = setUpPointcutTypeMap();

        TLongObjectHashMap joinPointHashToPointcutTypesMap = (TLongObjectHashMap)m_joinPointAdvicesMap.get(classHash);
        joinPointHashToPointcutTypesMap.put(joinPointHash, pointcutTypeToAdvicesMap);
        AdviceContainer[] adviceContainers = null;

        switch (joinPointType) {

            case JoinPointType.METHOD_EXECUTION:
                List executionAdvices = new ArrayList();
                MethodTuple methodTuple = system.getAspectManager().getMethodTuple(definedClass, joinPointHash);
                Method wrapperMethod = methodTuple.getWrapperMethod();
                List executionPointcuts = system.getAspectManager().getExecutionPointcuts(
                        definedClassMetaData,
                        ReflectionMetaDataMaker.createMethodMetaData(wrapperMethod)
                );
                for (Iterator it = executionPointcuts.iterator(); it.hasNext();) {
                    ExecutionPointcut pointcut = (ExecutionPointcut)it.next();
                    AdviceContainer advices = new AdviceContainer(
                            pointcut.getAroundAdviceIndexes(),
                            pointcut.getBeforeAdviceIndexes(),
                            pointcut.getAfterAdviceIndexes());
                    executionAdvices.add(advices);
                }
                adviceContainers = new AdviceContainer[executionAdvices.size()];
                int i = 0;
                for (Iterator iterator = executionAdvices.iterator(); iterator.hasNext(); i++) {
                    AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
                    adviceContainers[i] = adviceContainer;
                }
                pointcutTypeToAdvicesMap.put(PointcutType.EXECUTION, adviceContainers);
                break;

            case JoinPointType.METHOD_CALL:
                List callAdvices = new ArrayList();
                List callPointcuts = system.getAspectManager().getCallPointcuts(
                        definedClassMetaData,
                        ReflectionMetaDataMaker.createMethodMetaData(system.getAspectManager().
                        getMethodTuple(definedClass, joinPointHash).getWrapperMethod())
                );
                for (Iterator it = callPointcuts.iterator(); it.hasNext();) {
                    CallPointcut pointcut = (CallPointcut)it.next();
                    AdviceContainer advices = new AdviceContainer(
                            pointcut.getAroundAdviceIndexes(),
                            pointcut.getBeforeAdviceIndexes(),
                            pointcut.getAfterAdviceIndexes()
                    );
                    callAdvices.add(advices);
                }
                adviceContainers = new AdviceContainer[callAdvices.size()];
                i = 0;
                for (Iterator iterator = callAdvices.iterator(); iterator.hasNext(); i++) {
                    AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
                    adviceContainers[i] = adviceContainer;
                }
                pointcutTypeToAdvicesMap.put(PointcutType.CALL, adviceContainers);
                break;

            case JoinPointType.CONSTRUCTOR_EXECUTION:
                throw new UnsupportedOperationException("not implemented");

            case JoinPointType.CONSTRUCTOR_CALL:
                throw new UnsupportedOperationException("not implemented");

            case JoinPointType.FIELD_SET:
                List setAdvices = new ArrayList();
                List setPointcuts = system.getAspectManager().getSetPointcuts(
                        definedClassMetaData,
                        ReflectionMetaDataMaker.createFieldMetaData(signature)
                );
                for (Iterator it = setPointcuts.iterator(); it.hasNext();) {
                    SetPointcut pointcut = (SetPointcut)it.next();
                    AdviceContainer advices = new AdviceContainer(
                            pointcut.getAroundAdviceIndexes(),
                            pointcut.getBeforeAdviceIndexes(),
                            pointcut.getAfterAdviceIndexes()
                    );
                    setAdvices.add(advices);
                }
                adviceContainers = new AdviceContainer[setAdvices.size()];
                i = 0;
                for (Iterator iterator = setAdvices.iterator(); iterator.hasNext(); i++) {
                    AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
                    adviceContainers[i] = adviceContainer;
                }
                pointcutTypeToAdvicesMap.put(PointcutType.SET, adviceContainers);
                break;

            case JoinPointType.FIELD_GET:
                List getAdvices = new ArrayList();
                List getPointcuts = system.getAspectManager().getGetPointcuts(
                        definedClassMetaData,
                        ReflectionMetaDataMaker.createFieldMetaData(signature)
                );
                for (Iterator it = getPointcuts.iterator(); it.hasNext();) {
                    GetPointcut pointcut = (GetPointcut)it.next();
                    AdviceContainer advices = new AdviceContainer(
                            pointcut.getAroundAdviceIndexes(),
                            pointcut.getBeforeAdviceIndexes(),
                            pointcut.getAfterAdviceIndexes()
                    );
                    getAdvices.add(advices);
                }
                adviceContainers = new AdviceContainer[getAdvices.size()];
                i = 0;
                for (Iterator iterator = getAdvices.iterator(); iterator.hasNext(); i++) {
                    AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
                    adviceContainers[i] = adviceContainer;
                }
                pointcutTypeToAdvicesMap.put(PointcutType.GET, adviceContainers);
                break;

            case JoinPointType.CATCH_CLAUSE:
                throw new UnsupportedOperationException("not implemented");

            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException("not implemented");

            default:
                throw new RuntimeException("join point type not valid");
        }
    }

    /**
     * Returns the state of the join point.
     *
     * @param classHash
     * @param joinPointHash
     * @return the state
     */
    public int getStateForJoinPoint(final long classHash, final long joinPointHash) {
        if (!m_joinPointStateMap.containsKey(classHash)) {
            return JoinPointState.NOT_ADVISED;
        }
        TLongIntHashMap joinPoints = (TLongIntHashMap)m_joinPointStateMap.get(classHash);
        return joinPoints.get(joinPointHash);
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
     * Checks if the join point is registered or not.
     *
     * @param classHash
     * @param joinPointHash
     * @return
     */
    public boolean isRegistered(final long classHash, final long joinPointHash) {
        return getStateForJoinPoint(classHash, joinPointHash) == JoinPointState.NOT_ADVISED;
    }

    /**
     * Checks if the join point is not advised.
     *
     * @param classHash
     * @param joinPointHash
     * @return
     */
    public boolean isNotAdvised(final long classHash, final long joinPointHash) {
        return getStateForJoinPoint(classHash, joinPointHash) == JoinPointState.NOT_ADVISED;
    }

    /**
     * Checks if the join point has advices.
     *
     * @param classHash
     * @param joinPointHash
     * @return
     */
    public boolean isAdvised(final long classHash, final long joinPointHash) {
        return getStateForJoinPoint(classHash, joinPointHash) == JoinPointState.ADVISED;
    }

    /**
     * Checks if the join point has advices.
     *
     * @param classHash
     * @param joinPointHash
     * @return
     */
    public boolean hasAdvices(final long classHash, final long joinPointHash) {
        return getStateForJoinPoint(classHash, joinPointHash) == JoinPointState.HAS_ADVICES;
    }

    /**
     * Checks if the join point has beed redefined.
     *
     * @param classHash
     * @param joinPointHash
     * @return
     */
    public boolean isRedefined(final long classHash, final long joinPointHash) {
        return getStateForJoinPoint(classHash, joinPointHash) == JoinPointState.REDEFINED;
    }

    /**
     * Sets the join point as not advised.
     *
     * @param classHash
     * @param joinPointHash
     */
    public void setNotAdvised(final long classHash, final long joinPointHash) {
        getJoinPointStates(classHash).put(joinPointHash, JoinPointState.NOT_ADVISED);
    }

    /**
     * Sets the join point as advised.
     *
     * @param classHash
     * @param joinPointHash
     */
    public void setAdvised(final long classHash, final long joinPointHash) {
        getJoinPointStates(classHash).put(joinPointHash, JoinPointState.ADVISED);
    }

    /**
     * Marks the join point as one that has advices attached to it.
     *
     * @param classHash
     * @param joinPointHash
     */
    public void setHasAdvices(final long classHash, final long joinPointHash) {
        getJoinPointStates(classHash).put(joinPointHash, JoinPointState.HAS_ADVICES);
    }

    /**
     * Sets the join point as redefined.
     *
     * @param classHash
     * @param joinPointHash
     */
    public void setRedefined(final long classHash, final long joinPointHash) {
        getJoinPointStates(classHash).put(joinPointHash, JoinPointState.REDEFINED);
    }

    /**
     * Returns information about the join points for as specific class
     *
     * @param classHash
     * @return information about the join points for the class
     */
    private TLongIntHashMap getJoinPointStates(final long classHash) {
        return (TLongIntHashMap)m_joinPointStateMap.get(classHash);
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
        pointcutTypeToAdvicesMap.put(PointcutType.THROWS, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.CATCH_CLAUSE, EMTPY_ARRAY_LIST);
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

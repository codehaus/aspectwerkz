/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.joinpoint.CatchClauseSignature;
import org.codehaus.aspectwerkz.joinpoint.CodeSignature;
import org.codehaus.aspectwerkz.joinpoint.FieldSignature;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.metadata.ClassMetaDataImpl;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;

/**
 * Manages the join points, invokes the correct advice chains, handles redeployment, JIT compilation etc. Each advised
 * class' instance holds one instance of this class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JoinPointManager {

    /**
     * The JIT compilation boundry for nr of method invocations before optimizing a certain method.
     */
    private static final long JIT_COMPILATION_BOUNDRY;

    /**
     * @TODO: document JVM options
     * <p/>
     * Turns on/off the JIT compiler.
     */
    private static final boolean ENABLE_JIT_COMPILATION;

    static {
        String noJIT = java.lang.System.getProperty("aspectwerkz.jit.off");
        if ((noJIT != null && ("true".equalsIgnoreCase(noJIT) || "yes".equalsIgnoreCase(noJIT)))) {
            ENABLE_JIT_COMPILATION = false;
        }
        else {
            ENABLE_JIT_COMPILATION = true;
        }
        String boundry = java.lang.System.getProperty("aspectwerkz.jit.boundry");
        if (boundry != null) {
            JIT_COMPILATION_BOUNDRY = new Long(boundry).longValue();
        }
        else {
            JIT_COMPILATION_BOUNDRY = 1L;
        }
    }

    /**
     * Block size of the join point index repository grow algorithm.
     */
    private static final int JOIN_POINT_INDEX_GROW_BLOCK = 10;

    private static final List EMTPY_ARRAY_LIST = new ArrayList();
    private static final Map s_managers = new HashMap();
    private static final JoinPointRegistry s_registry = new JoinPointRegistry();

    private final System m_system;
    private final String m_uuid;
    private final Class m_targetClass;
    private final int m_classHash;
    private final ClassMetaDataImpl m_targetClassMetaData;

    private ThreadLocal[] m_joinPoints = new ThreadLocal[0];

    /**
     * Returns the join point manager for a specific class.
     *
     * @param targetClass
     * @param uuid
     * @return the join point manager instance for this class
     */
    public final static JoinPointManager getJoinPointManager(final Class targetClass, final String uuid) {
        if (s_managers.containsKey(targetClass)) {
            return (JoinPointManager)s_managers.get(targetClass);
        }
        else {
            JoinPointManager manager = new JoinPointManager(targetClass, uuid);
            s_managers.put(targetClass, manager);
            return manager;
        }
    }

    /**
     * Returs the join point registry.
     *
     * @return the join point registry
     */
    public static JoinPointRegistry getJoinPointRegistry() {
        return s_registry;
    }

    /**
     * Checks if a join point is advised, this does not mean that it has any advices attached to it.
     * <p/>
     * This method should be used by inserting a check in the wrapper/proxy method similar to this:
     * <pre>
     *     if (___AW_joinPointManager.hasAdvices(joinPointHash)) {
     *          // execute the advice chain
     *     }
     *     else {
     *          // invoke the prefixed target method
     *     }
     * </pre>
     *
     * @param joinPointHash
     * @return
     */
    public boolean isAdvised(final int joinPointHash) {
        // TODO: impl.
        return true;
//        return s_registry.getStateForJoinPoint(m_classHash, joinPointHash) > JoinPointState.NOT_ADVISED;
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the target
     * instance.
     *
     * @param methodHash
     * @param joinPointIndex
     * @param parameters
     * @param targetInstance
     * @param joinPointType
     * @return
     * @throws Throwable
     */
    public final Object proceedWithExecutionJoinPoint(
            final int methodHash,
            final int joinPointIndex,
            final Object[] parameters,
            final Object targetInstance,
            final int joinPointType) throws Throwable {

        ThreadLocal threadLocal = null;
        if (joinPointIndex >= m_joinPoints.length || m_joinPoints[joinPointIndex] == null) {

            s_registry.registerJoinPoint(
                    joinPointType, methodHash, null,
                    m_classHash, m_targetClass, m_targetClassMetaData, m_system
            );

            threadLocal = new ThreadLocal();

            synchronized (m_joinPoints) {
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            }
        }
        else {
            threadLocal = m_joinPoints[joinPointIndex];
        }

        JoinPointInfo joinPointInfo = (JoinPointInfo)threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }

        //TODO: ALEX AVCF - JIT breaks cflow - dig that
        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                    methodHash, joinPointType, PointcutType.EXECUTION, joinPointInfo, m_targetClass, m_targetClass
            );
        }

        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, methodHash);

            AdviceContainer[] adviceIndexes = null;
            switch (joinPointType) {
                case JoinPointType.METHOD_EXECUTION:
                    adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.EXECUTION);
                    joinPoint = createMethodJoinPoint(methodHash, joinPointType, m_targetClass, adviceIndexes);
                    break;

                case JoinPointType.CONSTRUCTOR_EXECUTION:
                    adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.EXECUTION);
                    joinPoint = createConstructorJoinPoint(methodHash, joinPointType, m_targetClass, adviceIndexes);
                    break;

                default:
                    throw new RuntimeException("join point type not valid");
            }

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            }
            else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        // set the RTTI
        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);
        if (parameters != null) {
            ((CodeSignature)joinPoint.getSignature()).setParameterValues(parameters);
        }

        return ((JoinPointBase)joinPoint).proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the target
     * instance.
     *
     * @param methodHash
     * @param joinPointIndex
     * @param parameters
     * @param targetInstance
     * @param declaringClass
     * @param joinPointType
     * @return the result from the method invocation
     * @throws Throwable
     */
    public final Object proceedWithCallJoinPoint(
            final int methodHash,
            final int joinPointIndex,
            final Object[] parameters,
            final Object targetInstance,
            final Class declaringClass,
            final int joinPointType) throws Throwable {

        ThreadLocal threadLocal = null;
        if (joinPointIndex >= m_joinPoints.length || m_joinPoints[joinPointIndex] == null) {

            s_registry.registerJoinPoint(
                    joinPointType, methodHash, null, m_classHash, declaringClass,
                    ReflectionMetaDataMaker.createClassMetaData(declaringClass), m_system
            );

            threadLocal = new ThreadLocal();

            synchronized (m_joinPoints) {
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            }
        }
        else {
            threadLocal = m_joinPoints[joinPointIndex];
        }

        JoinPointInfo joinPointInfo = (JoinPointInfo)threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }

        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                    methodHash, joinPointType, PointcutType.CALL, joinPointInfo, declaringClass, m_targetClass
            );
        }

        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, methodHash);
            AdviceContainer[] adviceIndexes = null;
            switch (joinPointType) {
                case JoinPointType.METHOD_CALL:
                    adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.CALL);
                    joinPoint = createMethodJoinPoint(methodHash, joinPointType, declaringClass, adviceIndexes);
                    break;

                case JoinPointType.CONSTRUCTOR_CALL:
                    adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.CALL);
                    joinPoint = createConstructorJoinPoint(methodHash, joinPointType, declaringClass, adviceIndexes);
                    break;

                default:
                    throw new RuntimeException("join point type not valid");
            }

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            }
            else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);
        if (parameters != null) {
            ((CodeSignature)joinPoint.getSignature()).setParameterValues(parameters);
        }

        return ((JoinPointBase)joinPoint).proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the target
     * instance.
     *
     * @param fieldHash
     * @param joinPointIndex
     * @param fieldValue     as the first arg in an Object array
     * @param targetInstance
     * @param declaringClass
     * @param fieldSignature
     * @throws Throwable
     */
    public final void proceedWithSetJoinPoint(
            final int fieldHash,
            final int joinPointIndex,
            final Object[] fieldValue,
            final Object targetInstance,
            final Class declaringClass,
            final String fieldSignature) throws Throwable {

        ThreadLocal threadLocal = null;
        if (joinPointIndex >= m_joinPoints.length || m_joinPoints[joinPointIndex] == null) {

            s_registry.registerJoinPoint(
                    JoinPointType.FIELD_SET, fieldHash, fieldSignature, m_classHash, declaringClass,
                    ReflectionMetaDataMaker.createClassMetaData(declaringClass), m_system
            );

            threadLocal = new ThreadLocal();

            synchronized (m_joinPoints) {
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            }
        }
        else {
            threadLocal = m_joinPoints[joinPointIndex];
        }

        JoinPointInfo joinPointInfo = (JoinPointInfo)threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }

        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                    fieldHash, JoinPointType.FIELD_SET, PointcutType.SET, joinPointInfo, declaringClass, m_targetClass
            );
        }

        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, fieldHash);

            AdviceContainer[] adviceIndexes = null;
            adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.SET);
            joinPoint =
            createFieldJoinPoint(fieldHash, fieldSignature, JoinPointType.FIELD_SET, m_targetClass, adviceIndexes);

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            }
            else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        // intialize the join point before each usage
        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);
        if (fieldValue[0] != null) {
            ((FieldSignature)joinPoint.getSignature()).setFieldValue(fieldValue[0]);
        }
        ((JoinPointBase)joinPoint).proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the target
     * instance.
     *
     * @param fieldHash
     * @param joinPointIndex
     * @param targetInstance
     * @param declaringClass
     * @param fieldSignature
     * @throws Throwable
     */
    public final Object proceedWithGetJoinPoint(
            final int fieldHash,
            final int joinPointIndex,
            final Object targetInstance,
            final Class declaringClass,
            final String fieldSignature) throws Throwable {

        ThreadLocal threadLocal = null;
        if (joinPointIndex >= m_joinPoints.length || m_joinPoints[joinPointIndex] == null) {

            s_registry.registerJoinPoint(
                    JoinPointType.FIELD_GET, fieldHash, fieldSignature, m_classHash, declaringClass,
                    ReflectionMetaDataMaker.createClassMetaData(declaringClass), m_system
            );

            threadLocal = new ThreadLocal();

            synchronized (m_joinPoints) {
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            }
        }
        else {
            threadLocal = m_joinPoints[joinPointIndex];
        }

        JoinPointInfo joinPointInfo = (JoinPointInfo)threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }

        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                    fieldHash, JoinPointType.FIELD_GET, PointcutType.GET, joinPointInfo, declaringClass, m_targetClass
            );
        }

        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, fieldHash);

            AdviceContainer[] adviceIndexes = null;
            adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.GET);
            joinPoint =
            createFieldJoinPoint(fieldHash, fieldSignature, JoinPointType.FIELD_GET, m_targetClass, adviceIndexes);

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            }
            else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        // intialize the join point before each usage
        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);

        return ((JoinPointBase)joinPoint).proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the target
     * instance.
     *
     * @param handlerHash
     * @param joinPointIndex
     * @param exceptionInstance
     * @param targetInstance
     * @param handlerSignature
     * @throws Throwable
     */
    public final void proceedWithHandlerJoinPoint(
            final int handlerHash,
            final int joinPointIndex,
            final Object exceptionInstance,
            final Object targetInstance,
            final String handlerSignature) throws Throwable {

        ThreadLocal threadLocal = null;
        if (joinPointIndex >= m_joinPoints.length || m_joinPoints[joinPointIndex] == null) {

            ClassMetaDataImpl exceptionMetaData = ReflectionMetaDataMaker.createClassMetaData(
                    exceptionInstance.getClass()
            );
            s_registry.registerJoinPoint(
                    JoinPointType.HANDLER, handlerHash, handlerSignature,
                    m_classHash, m_targetClass, exceptionMetaData, m_system
            );

            threadLocal = new ThreadLocal();

            synchronized (m_joinPoints) {
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            }
        }
        else {
            threadLocal = m_joinPoints[joinPointIndex];
        }

        JoinPointInfo joinPointInfo = (JoinPointInfo)threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }

        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                    handlerHash, JoinPointType.HANDLER, PointcutType.HANDLER, joinPointInfo, m_targetClass,
                    m_targetClass
            );
        }

        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, handlerHash);

            AdviceContainer[] adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.HANDLER);
            joinPoint =
            createCatchClauseJoinPoint(exceptionInstance.getClass(), m_targetClass, handlerSignature, adviceIndexes);

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            }
            else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        // intialize the join point before each usage
        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);
        ((CatchClauseSignature)joinPoint.getSignature()).setParameterValue(exceptionInstance);

        ((JoinPointBase)joinPoint).proceed();
    }

    /**
     * Handles the Just-In-Time (JIT) compilation of the advice execution chains.
     *
     * @param joinPointHash
     * @param joinPointType
     * @param pointcutType
     * @param joinPointInfo
     * @param declaringClass
     * @param targetClass
     */
    private final void handleJitCompilation(
            final int joinPointHash,
            final int joinPointType,
            final PointcutType pointcutType,
            final JoinPointInfo joinPointInfo,
            final Class declaringClass,
            final Class targetClass) {
        joinPointInfo.invocations++;
        if (joinPointInfo.state == JoinPointState.REDEFINED) {
            joinPointInfo.invocations = 0L;
        }
        else if (joinPointInfo.invocations == JIT_COMPILATION_BOUNDRY) {
            Map advices = s_registry.getAdvicesForJoinPoint(m_classHash, joinPointHash);
            if (advices.containsKey(pointcutType)) {
                AdviceContainer[] adviceIndexes = (AdviceContainer[])advices.get(pointcutType);
                joinPointInfo.joinPoint = JitCompiler.compileJoinPoint(
                        joinPointHash, joinPointType, pointcutType, adviceIndexes,
                        declaringClass, targetClass, m_uuid
                );
                joinPointInfo.isJitCompiled = true;
            }
        }
    }

    /**
     * Create a method join point.
     *
     * @param methodHash
     * @param joinPointType
     * @param declaringClass
     * @param adviceIndexes
     * @return
     */
    private final MethodJoinPoint createMethodJoinPoint(
            final int methodHash,
            final int joinPointType,
            final Class declaringClass,
            final AdviceContainer[] adviceIndexes) {
        MethodTuple methodTuple = m_system.getAspectManager().getMethodTuple(declaringClass, methodHash);
        Class declaringType = methodTuple.getDeclaringClass();
        Signature signature = new MethodSignatureImpl(declaringType, methodTuple);

        List cflowExpressions = m_system.getAspectManager().getCFlowExpressions(
                ReflectionMetaDataMaker.createClassMetaData(declaringClass),
                ReflectionMetaDataMaker.createMethodMetaData(methodTuple.getWrapperMethod()),
                null, PointcutType.EXECUTION//TODO CAN BE @CALL - see proceedWithCallJoinPoint
        );

        // ALEX - cflow is a pain to debug
        for (Iterator it = cflowExpressions.iterator(); it.hasNext();) {
            java.lang.System.out.println("(Expression)(it.next() = " + ((Expression)(it.next())).getExpression());
        }

        // TODO: cflow for before and after advices needed
        return new MethodJoinPoint(
                m_uuid, joinPointType, m_targetClass, signature, cflowExpressions,
                createAroundAdviceExecutor(adviceIndexes, joinPointType),
                createBeforeAdviceExecutor(adviceIndexes),
                createAfterAdviceExecutor(adviceIndexes)
        );
    }

    /**
     * Create a constructor join point.
     *
     * @param constructorHash
     * @param joinPointType
     * @param declaringClass
     * @param adviceIndexes
     * @return
     */
    private final JoinPoint createConstructorJoinPoint(
            final int constructorHash,
            final int joinPointType,
            final Class declaringClass,
            final AdviceContainer[] adviceIndexes) {
        ConstructorTuple constructorTuple = m_system.getAspectManager().getConstructorTuple(
                declaringClass, constructorHash
        );

        Class declaringType = constructorTuple.getDeclaringClass();
        Signature signature = new ConstructorSignatureImpl(declaringType, constructorTuple);

        // TODO: enable cflow for constructors
//        List cflowExpressions = m_system.getAspectManager().getCFlowExpressions(
//                ReflectionMetaDataMaker.createClassMetaData(declaringClass),
//                ReflectionMetaDataMaker.createConstructorMetaData(constructor)
//        );
        return new ConstructorJoinPoint(
                m_uuid, joinPointType, m_targetClass, signature, EMTPY_ARRAY_LIST,
                createAroundAdviceExecutor(adviceIndexes, joinPointType),
                createBeforeAdviceExecutor(adviceIndexes),
                createAfterAdviceExecutor(adviceIndexes)
        );
    }

    /**
     * Create a field join point.
     *
     * @param fieldHash
     * @param fieldSignature
     * @param joinPointType
     * @param declaringClass
     * @param adviceIndexes
     * @return
     */
    private final JoinPoint createFieldJoinPoint(
            final int fieldHash,
            final String fieldSignature,
            final int joinPointType,
            final Class declaringClass,
            final AdviceContainer[] adviceIndexes) {

        Field field = m_system.getAspectManager().getField(declaringClass, fieldHash);
        Signature signature = new FieldSignatureImpl(declaringClass, field);

        // TODO: enable cflow for field set get pointcuts
//        List cflowExpressions = new ArrayList();
//                m_system.getAspectManager().getCFlowExpressions(
//                 ReflectionMetaDataMaker.createClassMetaData(declaringClass),
//                 ReflectionMetaDataMaker.createFieldMetaData(fieldSignature)
//         );
        return new FieldJoinPoint(
                m_uuid, joinPointType, m_targetClass, signature, EMTPY_ARRAY_LIST,
                createAroundAdviceExecutor(adviceIndexes, joinPointType),
                createBeforeAdviceExecutor(adviceIndexes),
                createAfterAdviceExecutor(adviceIndexes)
        );
    }

    /**
     * Create a catch clause join point.
     *
     * @param exceptionClass
     * @param declaringClass
     * @param catchClauseSignature
     * @param adviceIndexes
     * @return
     */
    private final JoinPoint createCatchClauseJoinPoint(
            final Class exceptionClass,
            final Class declaringClass,
            final String catchClauseSignature,
            final AdviceContainer[] adviceIndexes) {
        Signature signature = new CatchClauseSignatureImpl(exceptionClass, declaringClass, catchClauseSignature);
        // TODO: enable cflow for catch clauses
//        List cflowExpressions = m_system.getAspectManager().getCFlowExpressions(
//                ReflectionMetaDataMaker.createClassMetaData(declaringClass),
//                ReflectionMetaDataMaker.createCatchClauseMetaData(signature)
//        );
        return new CatchClauseJoinPoint(
                m_uuid, m_targetClass, signature, EMTPY_ARRAY_LIST,
                createAroundAdviceExecutor(adviceIndexes, JoinPointType.HANDLER),
                createBeforeAdviceExecutor(adviceIndexes),
                createAfterAdviceExecutor(adviceIndexes)
        );
    }

    /**
     * Creates an around advice executor.
     *
     * @param adviceIndexes
     * @param joinPointType
     * @return the advice executor
     */
    private final AroundAdviceExecutor createAroundAdviceExecutor(
            final AdviceContainer[] adviceIndexes,
            final int joinPointType) {
        return new AroundAdviceExecutor(extractAroundAdvice(adviceIndexes), m_system, joinPointType);
    }

    /**
     * Creates a before advice executor.
     *
     * @param adviceIndexes
     * @return the advice executor
     */
    private final BeforeAdviceExecutor createBeforeAdviceExecutor(final AdviceContainer[] adviceIndexes) {
        return new BeforeAdviceExecutor(extractBeforeAdvice(adviceIndexes), m_system);
    }

    /**
     * Creates an after advice executor.
     *
     * @param adviceIndexes
     * @return the advice executor
     */
    private final AfterAdviceExecutor createAfterAdviceExecutor(final AdviceContainer[] adviceIndexes) {
        return new AfterAdviceExecutor(extractAfterAdvice(adviceIndexes), m_system);
    }

    /**
     * Extracts the around advices.
     *
     * @param adviceIndexes
     * @return
     */
    final static IndexTuple[] extractAroundAdvice(final AdviceContainer[] adviceIndexes) {
        int i, j;
        List aroundAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceContainer adviceIndex = adviceIndexes[i];
            IndexTuple[] indexTuples = adviceIndex.getAroundAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                aroundAdviceList.add(indexTuples[j]);
            }
        }
        IndexTuple[] aroundAdvices = new IndexTuple[aroundAdviceList.size()];
        i = 0;
        for (Iterator it = aroundAdviceList.iterator(); it.hasNext(); i++) {
            aroundAdvices[i] = (IndexTuple)it.next();
        }
        return aroundAdvices;
    }

    /**
     * Extracts the before advices.
     *
     * @param adviceIndexes
     * @return
     */
    final static IndexTuple[] extractBeforeAdvice(final AdviceContainer[] adviceIndexes) {
        int i, j;
        List beforeAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceContainer adviceIndex = adviceIndexes[i];
            IndexTuple[] indexTuples = adviceIndex.getBeforeAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                beforeAdviceList.add(indexTuples[j]);
            }
        }
        IndexTuple[] beforeAdvices = new IndexTuple[beforeAdviceList.size()];
        i = 0;
        for (Iterator it = beforeAdviceList.iterator(); it.hasNext(); i++) {
            beforeAdvices[i] = (IndexTuple)it.next();
        }
        return beforeAdvices;
    }

    /**
     * Extracts the after advices.
     *
     * @param adviceIndexes
     * @return
     */
    final static IndexTuple[] extractAfterAdvice(final AdviceContainer[] adviceIndexes) {
        int i, j;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceContainer adviceIndex = adviceIndexes[i];
            IndexTuple[] indexTuples = adviceIndex.getAfterAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                afterAdviceList.add(indexTuples[j]);
            }
        }
        IndexTuple[] afterAdvices = new IndexTuple[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (IndexTuple)it.next();
        }
        return afterAdvices;
    }

    /**
     * Creates a new join point manager for a specific class.
     *
     * @param targetClass
     * @param uuid
     */
    private JoinPointManager(final Class targetClass, final String uuid) {
        m_uuid = uuid;
        m_system = SystemLoader.getSystem(m_uuid);
        m_targetClass = targetClass;
        m_classHash = m_targetClass.hashCode();
        m_targetClassMetaData = ReflectionMetaDataMaker.createClassMetaData(m_targetClass);
    }

    /**
     * Contains the JoinPoint instance and some RTTI about the join point.
     */
    static class JoinPointInfo {
        public JoinPoint joinPoint = null;
        public int state = JoinPointState.NOT_ADVISED;
        public long invocations = 0L;
        public boolean isJitCompiled = false;
    }
}



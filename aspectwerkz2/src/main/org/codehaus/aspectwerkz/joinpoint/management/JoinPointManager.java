/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Field;

import gnu.trove.TLongObjectHashMap;
import gnu.trove.TLongLongHashMap;

import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.FieldSignature;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.joinpoint.CatchClauseSignature;
import org.codehaus.aspectwerkz.joinpoint.CodeSignature;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.JoinPointBase;

/**
 * Manages the join points, invokes the correct advice chains, handles redeployment, JIT compilation etc.
 * One instance per advised class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class JoinPointManager {

    /**
     * The JIT compilation boundry for nr of method invocations before optimizing a certain method.
     * @TODO: JIT boundry should be configurable, how?, JVM option?
     * @TODO: what should the default value be?
     */
    private static final long JIT_COMPILATION_BOUNDRY = 100L;

    /**
     * Turns on/off the JIT compiler.
     */
    private static final boolean ENABLE_JIT_COMPILATION;

    static {
        String noJIT = "true";
//        String noJIT = java.lang.System.getProperty("aspectwerkz.nojit");
        if ((noJIT != null && ("true".equalsIgnoreCase(noJIT) || "yes".equalsIgnoreCase(noJIT)))) {
            ENABLE_JIT_COMPILATION = false;
        }
        else {
            ENABLE_JIT_COMPILATION = true;
        }
    }

    private static final List EMTPY_ARRAY_LIST = new ArrayList();
    private static final Map s_managers = new HashMap();
    private static final JoinPointRegistry s_registry = new JoinPointRegistry();

    private final System m_system;
    private final String m_uuid;
    private final Class m_targetClass;
    private final long m_classHash;
    private final ClassMetaData m_targetClassMetaData;

    private final TLongObjectHashMap m_joinPoints = new TLongObjectHashMap();
    private final TLongLongHashMap m_invocations = new TLongLongHashMap();

    /**
     * Returns the join point manager for a specific class.
     * <p/>
     * Each instrumented class should have a static field with a reference to the JoinPointManager, e.g.:
     * <pre>
     *       private static final JoinPointManager ___AW_joinPointManager =
     *           JoinPointManager.getJoinPointManager(___AW_clazz, "uuid");
     * </pre>
     *
     * @param targetClass
     * @param uuid
     * @return the join point manager instance for this class
     */
    public static JoinPointManager getJoinPointManager(final Class targetClass, final String uuid) {
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
        return s_registry.getStateForJoinPoint(m_classHash, joinPointHash) > JoinPointState.NOT_ADVISED;
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the
     * target instance.
     * <p/>
     * Example of bytecode needed to be generated to invoke the method:
     * <pre>
     *        return ___AW_joinPointManager.proceedWithExecutionJoinPoint(
     *            joinPointHash, new Object[]{parameter}, this,
     *            JoinPointType.METHOD_EXECUTION, joinPointSignature
     *       );
     * </pre>
     *
     * @param methodHash
     * @param parameters
     * @param targetInstance null if invoked in a static context
     * @param joinPointType
     * @param methodSignature
     * @return the result from the method invocation
     * @throws Throwable
     */
    public Object proceedWithExecutionJoinPoint___FAST(final int methodHash,
                                                       final Object[] parameters,
                                                       final Object targetInstance,
                                                       final int joinPointType,
                                                       final String methodSignature)
            throws Throwable {

        ThreadLocal threadLocal = (ThreadLocal)m_joinPoints.get(methodHash);
        if (threadLocal == null) {
            registerJoinPoint(
                    joinPointType, methodHash, methodSignature,
                    m_targetClass, m_targetClassMetaData
            );
            threadLocal = new ThreadLocal();
            m_joinPoints.put(methodHash, threadLocal);
        }

        JoinPoint joinPoint = (JoinPoint)threadLocal.get();

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, methodHash);

            AdviceContainer[] adviceIndexes = null;
            switch (joinPointType) {
                case JoinPointType.METHOD_EXECUTION:
                    adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.EXECUTION);
                    joinPoint = createMethodJoinPoint(
                            methodHash, joinPointType, m_targetClass, adviceIndexes
                    );
                    break;

                case JoinPointType.CONSTRUCTOR_EXECUTION:
                    adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.EXECUTION);
                    joinPoint = createConstructorJoinPoint(
                            methodHash, joinPointType, m_targetClass, adviceIndexes
                    );
                    break;

                default:
                    throw new RuntimeException("join point type not valid");
            }
            // create the join point
            threadLocal.set(joinPoint);
            m_joinPoints.put(methodHash, threadLocal);
        }

        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);
        ((CodeSignature)joinPoint.getSignature()).setParameterValues(parameters);

        return joinPoint.proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the
     * target instance.
     * <p/>
     * Example of bytecode needed to be generated to invoke the method:
     * <pre>
     *        return ___AW_joinPointManager.proceedWithExecutionJoinPoint(
     *            joinPointHash, new Object[]{parameter}, this,
     *            JoinPointType.METHOD_EXECUTION, joinPointSignature
     *       );
     * </pre>
     *
     * @param methodHash
     * @param parameters
     * @param targetInstance null if invoked in a static context
     * @param joinPointType
     * @param methodSignature
     * @return the result from the method invocation
     * @throws Throwable
     */
    public Object proceedWithExecutionJoinPoint(final int methodHash,
                                                final Object[] parameters,
                                                final Object targetInstance,
                                                final int joinPointType,
                                                final String methodSignature)
            throws Throwable {

        // get the state for the join point
        final long joinPointState = s_registry.getStateForJoinPoint(m_classHash, methodHash);

        if (joinPointState == JoinPointState.NOT_ADVISED) {
            registerJoinPoint(
                    joinPointType, methodHash, methodSignature,
                    m_targetClass, m_targetClassMetaData
            );
            m_joinPoints.put(methodHash, new ThreadLocal());
        }

        JoinPoint joinPoint = null;
        if (ENABLE_JIT_COMPILATION) {
            joinPoint = handleJitCompilation(methodHash, joinPointState);
        }

        if (joinPoint == null) {
            // get the join point from the cache
            ThreadLocal threadLocal = (ThreadLocal)m_joinPoints.get(methodHash);
            joinPoint = (JoinPoint)threadLocal.get();

            // if null or redefined -> create a new join point and cache it
            if (joinPoint == null || joinPointState == JoinPointState.REDEFINED) {
                Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, methodHash);

                AdviceContainer[] adviceIndexes = null;
                switch (joinPointType) {
                    case JoinPointType.METHOD_EXECUTION:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.EXECUTION);
                        joinPoint = createMethodJoinPoint(
                                methodHash, joinPointType, m_targetClass, adviceIndexes
                        );
                        break;

                    case JoinPointType.CONSTRUCTOR_EXECUTION:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.EXECUTION);
                        joinPoint = createConstructorJoinPoint(
                                methodHash, joinPointType, m_targetClass, adviceIndexes
                        );
                        break;

                    default:
                        throw new RuntimeException("join point type not valid");
                }

                // create the join point
                threadLocal.set(joinPoint);
                m_joinPoints.put(methodHash, threadLocal);

                if (adviceIndexes.length == 0) {
                    s_registry.setAdvised(m_classHash, methodHash);
                }
                else {
                    s_registry.setHasAdvices(m_classHash, methodHash);
                }
            }
        }

        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);
        ((CodeSignature)joinPoint.getSignature()).setParameterValues(parameters);

        return joinPoint.proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the
     * target instance.
     * <p/>
     * Example of bytecode needed to be generated to invoke the method:
     * <pre>
     *        return ___AW_joinPointManager.proceedWithCallJoinPoint(
     *            joinPointHash, new Object[]{parameter}, null, declaringClass,
     *            JoinPointType.METHOD_CALL, joinPointSignature
     *       );
     * </pre>
     *
     * @param methodHash
     * @param parameters
     * @param targetInstance
     * @param declaringClass
     * @param joinPointType
     * @param methodSignature
     * @return the result from the method invocation
     * @throws Throwable
     */
    public Object proceedWithCallJoinPoint(final int methodHash,
                                           final Object[] parameters,
                                           final Object targetInstance,
                                           final Class declaringClass,
                                           final int joinPointType,
                                           final String methodSignature)
            throws Throwable {
        // get the state for the join point
        final long joinPointState = s_registry.getStateForJoinPoint(m_classHash, methodHash);

        if (joinPointState == JoinPointState.NOT_ADVISED) {
            registerJoinPoint(
                    joinPointType, methodHash, methodSignature,
                    declaringClass, ReflectionMetaDataMaker.createClassMetaData(declaringClass)
            );
            m_joinPoints.put(methodHash, new ThreadLocal());
        }

        JoinPoint joinPoint = null;
        if (ENABLE_JIT_COMPILATION) {
            joinPoint = handleJitCompilation(methodHash, joinPointState);
        }

        if (joinPoint == null) {
            // get the join point from the cache
            ThreadLocal threadLocal = (ThreadLocal)m_joinPoints.get(methodHash);
            joinPoint = (JoinPoint)threadLocal.get();

            // if null or redefined -> create a new join point and cache it
            if (joinPoint == null || joinPointState == JoinPointState.REDEFINED) {
                m_invocations.put(methodHash, 0L);
                Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, methodHash);

                AdviceContainer[] adviceIndexes = null;
                switch (joinPointType) {
                    case JoinPointType.METHOD_CALL:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.CALL);
                        joinPoint = createMethodJoinPoint(
                                methodHash, joinPointType, declaringClass, adviceIndexes
                        );
                        break;

                    case JoinPointType.CONSTRUCTOR_CALL:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.CALL);
                        joinPoint = createConstructorJoinPoint(
                                methodHash, joinPointType, declaringClass, adviceIndexes
                        );
                        break;

                    default:
                        throw new RuntimeException("join point type not valid");
                }

                threadLocal.set(joinPoint);
                m_joinPoints.put(methodHash, threadLocal);

                if (adviceIndexes.length == 0) {
                    s_registry.setAdvised(m_classHash, methodHash);
                }
                else {
                    s_registry.setHasAdvices(m_classHash, methodHash);
                }
            }
        }

        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);
        ((CodeSignature)joinPoint.getSignature()).setParameterValues(parameters);

        return joinPoint.proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the
     * target instance.
     *
     * @param fieldHash
     * @param fieldValue as the first arg in an Object array
     * @param targetInstance
     * @param declaringClass
     * @param fieldSignature
     * @throws Throwable
     */
    public void proceedWithSetJoinPoint(final int fieldHash,
                                        final Object[] fieldValue,
                                        final Object targetInstance,
                                        final Class declaringClass,
                                        final String fieldSignature)
            throws Throwable {

        // get the state for the join point
        final long joinPointState = s_registry.getStateForJoinPoint(m_classHash, fieldHash);

        if (joinPointState == JoinPointState.NOT_ADVISED) {
            registerJoinPoint(
                    JoinPointType.FIELD_SET, fieldHash, fieldSignature,
                    declaringClass, ReflectionMetaDataMaker.createClassMetaData(declaringClass)
            );
            m_joinPoints.put(fieldHash, new ThreadLocal());
        }

        JoinPoint joinPoint = null;
        if (ENABLE_JIT_COMPILATION) {
            joinPoint = handleJitCompilation(fieldHash, joinPointState);
        }

        if (joinPoint == null) {
            // get the join point from the cache
            ThreadLocal threadLocal = (ThreadLocal)m_joinPoints.get(fieldHash);
            joinPoint = (JoinPoint)threadLocal.get();

            // if null or redefined -> create a new join point and cache it
            if (joinPoint == null || joinPointState == JoinPointState.REDEFINED) {
                m_invocations.put(fieldHash, 0L);
                Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, fieldHash);

                AdviceContainer[] adviceIndexes = null;
                adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.SET);
                joinPoint = createFieldJoinPoint(
                        fieldHash, fieldSignature, JoinPointType.FIELD_SET, m_targetClass, adviceIndexes
                );

                threadLocal.set(joinPoint);
                m_joinPoints.put(fieldHash, threadLocal);

                if (adviceIndexes.length == 0) {
                    s_registry.setAdvised(m_classHash, fieldHash);
                }
                else {
                    s_registry.setHasAdvices(m_classHash, fieldHash);
                }
            }
        }

        // intialize the join point before each usage
        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);
        ((FieldSignature)joinPoint.getSignature()).setFieldValue(fieldValue[0]);

        joinPoint.proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the
     * target instance.
     *
     * @param fieldHash
     * @param targetInstance
     * @param declaringClass
     * @param fieldSignature
     * @throws Throwable
     */
    public Object proceedWithGetJoinPoint(final int fieldHash,
                                          final Object targetInstance,
                                          final Class declaringClass,
                                          final String fieldSignature)
            throws Throwable {

        // get the state for the join point
        final long joinPointState = s_registry.getStateForJoinPoint(m_classHash, fieldHash);

        if (joinPointState == JoinPointState.NOT_ADVISED) {
            registerJoinPoint(
                    JoinPointType.FIELD_GET, fieldHash, fieldSignature,
                    declaringClass, ReflectionMetaDataMaker.createClassMetaData(declaringClass)
            );
            m_joinPoints.put(fieldHash, new ThreadLocal());
        }

        JoinPoint joinPoint = null;
        if (ENABLE_JIT_COMPILATION) {
            joinPoint = handleJitCompilation(fieldHash, joinPointState);
        }

        if (joinPoint == null) {
            // get the join point from the cache
            ThreadLocal threadLocal = (ThreadLocal)m_joinPoints.get(fieldHash);
            joinPoint = (JoinPoint)threadLocal.get();

            // if null or redefined -> create a new join point and cache it
            if (joinPoint == null || joinPointState == JoinPointState.REDEFINED) {
                m_invocations.put(fieldHash, 0L);
                Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, fieldHash);

                AdviceContainer[] adviceIndexes = null;
                adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.GET);
                joinPoint = createFieldJoinPoint(
                        fieldHash, fieldSignature, JoinPointType.FIELD_GET, m_targetClass, adviceIndexes
                );

                threadLocal.set(joinPoint);
                m_joinPoints.put(fieldHash, threadLocal);

                if (adviceIndexes.length == 0) {
                    s_registry.setAdvised(m_classHash, fieldHash);
                }
                else {
                    s_registry.setHasAdvices(m_classHash, fieldHash);
                }
            }
        }

        // intialize the join point before each usage
        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);

        return joinPoint.proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the
     * target instance.
     * <p/>
     * Example of bytecode needed to be generated to invoke the method:
     * <pre>
     *        ___AW_joinPointManager.proceedWithCatchClauseJoinPoint(
     *            joinPointHash, exceptionInstance, this,
     *            JoinPointType.FIELD_SET, joinPointSignature
     *       );
     * </pre>
     *
     * @param catchClauseHash
     * @param exceptionInstance
     * @param targetInstance
     * @param handlerSignature
     * @throws Throwable
     */
    public void proceedWithCatchClauseJoinPoint(final int catchClauseHash,
                                                final Object exceptionInstance,
                                                final Object targetInstance,
                                                final String handlerSignature)
            throws Throwable {

        // get the state for the join point
        final long joinPointState = s_registry.getStateForJoinPoint(m_classHash, catchClauseHash);

        if (joinPointState == JoinPointState.NOT_ADVISED) {
            registerJoinPoint(
                    JoinPointType.CATCH_CLAUSE, catchClauseHash, handlerSignature,
                    m_targetClass, m_targetClassMetaData
            );
            m_joinPoints.put(catchClauseHash, new ThreadLocal());
        }

        JoinPoint joinPoint = null;
        if (ENABLE_JIT_COMPILATION) {
            joinPoint = handleJitCompilation(catchClauseHash, joinPointState);
        }

        if (joinPoint == null) {
            // get the join point from the cache
            ThreadLocal threadLocal = (ThreadLocal)m_joinPoints.get(catchClauseHash);
            joinPoint = (JoinPoint)threadLocal.get();

            // if null or redefined -> create a new join point and cache it
            if (joinPoint == null || joinPointState == JoinPointState.REDEFINED) {
                m_invocations.put(catchClauseHash, 0L);
                Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(
                        m_classHash, catchClauseHash
                );

                AdviceContainer[] adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(
                        PointcutType.CATCH_CLAUSE
                );
                joinPoint = createCatchClauseJoinPoint(
                        handlerSignature, m_targetClass, adviceIndexes
                );

                threadLocal.set(joinPoint);
                m_joinPoints.put(catchClauseHash, threadLocal);

                if (adviceIndexes.length == 0) {
                    s_registry.setAdvised(m_classHash, catchClauseHash);
                }
                else {
                    s_registry.setHasAdvices(m_classHash, catchClauseHash);
                }
            }
        }

        // intialize the join point before each usage
        ((JoinPointBase)joinPoint).setTargetInstance(targetInstance);
        ((CatchClauseSignature)joinPoint.getSignature()).setParameterValue(exceptionInstance);

        joinPoint.proceed();
    }

    /**
     * Handles the Just-In-Time (JIT) compilation of the advice execution chains.
     *
     * @param joinPointHash
     * @param joinPointState
     * @return the precompiled and optimized join point
     */
    private JoinPoint handleJitCompilation(final int joinPointHash, long joinPointState) {
        JoinPoint joinPoint = null;

        long invocations = m_invocations.get(joinPointHash);
        invocations++;
        m_invocations.put(joinPointHash, invocations);

        if (joinPointState == JoinPointState.REDEFINED) {
            m_invocations.put(joinPointHash, 0L);
        }
        else if (invocations > JIT_COMPILATION_BOUNDRY) {
            // JIT compile a new join point
            Map adviceKeys = s_registry.getAdvicesForJoinPoint(m_classHash, joinPointHash);
            joinPoint = JitCompiler.compileJoinPoint(joinPointHash, adviceKeys);
        }

        return joinPoint;
    }

    /**
     * Registers the join point, needed if the join point has never been reached before.
     *
     * @param joinPointType
     * @param joinPointHash
     * @param joinPointSignature
     * @param definedClass
     * @param definedClassMetaData
     */
    private void registerJoinPoint(final int joinPointType,
                                   final int joinPointHash,
                                   final String joinPointSignature,
                                   final Class definedClass,
                                   final ClassMetaData definedClassMetaData) {
        s_registry.registerJoinPoint(
                joinPointType, joinPointHash, joinPointSignature,
                m_classHash, definedClass, definedClassMetaData, m_system
        );
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
    private JoinPoint createMethodJoinPoint(final int methodHash,
                                            final int joinPointType,
                                            final Class declaringClass,
                                            final AdviceContainer[] adviceIndexes) {
        MethodTuple methodTuple = m_system.getAspectManager().getMethodTuple(declaringClass, methodHash);
        Class declaringType = methodTuple.getDeclaringClass();
        Signature signature = new MethodSignatureImpl(declaringType, methodTuple);

        List cflowExpressions = m_system.getAspectManager().getCFlowExpressions(
                ReflectionMetaDataMaker.createClassMetaData(declaringClass),
                ReflectionMetaDataMaker.createMethodMetaData(methodTuple.getWrapperMethod())
        );

        // TODO: cflow for before and after advices needed
        return new MethodJoinPoint(
                m_uuid, joinPointType, m_targetClass, signature,
                createAroundAdviceExecutor(adviceIndexes, cflowExpressions, joinPointType),
                createBeforeAdviceExecutor(adviceIndexes, cflowExpressions),
                createAfterAdviceExecutor(adviceIndexes, cflowExpressions)
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
    private JoinPoint createConstructorJoinPoint(final int constructorHash,
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
                m_uuid, joinPointType, m_targetClass, signature,
                createAroundAdviceExecutor(adviceIndexes, EMTPY_ARRAY_LIST, joinPointType),
                createBeforeAdviceExecutor(adviceIndexes, EMTPY_ARRAY_LIST),
                createAfterAdviceExecutor(adviceIndexes, EMTPY_ARRAY_LIST)
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
    private JoinPoint createFieldJoinPoint(final int fieldHash,
                                           final String fieldSignature,
                                           final int joinPointType,
                                           final Class declaringClass,
                                           final AdviceContainer[] adviceIndexes) {

        Field field = m_system.getAspectManager().getField(declaringClass, fieldHash);
        Signature signature = new FieldSignatureImpl(field, declaringClass, fieldSignature);

        // TODO: enable cflow for field set get pointcuts
//        List cflowExpressions = new ArrayList();
//                m_system.getAspectManager().getCFlowExpressions(
//                 ReflectionMetaDataMaker.createClassMetaData(declaringClass),
//                 ReflectionMetaDataMaker.createFieldMetaData(fieldSignature)
//         );
        return new FieldJoinPoint(
                m_uuid, joinPointType, m_targetClass, signature,
                createAroundAdviceExecutor(adviceIndexes, EMTPY_ARRAY_LIST, joinPointType),
                createBeforeAdviceExecutor(adviceIndexes, EMTPY_ARRAY_LIST),
                createAfterAdviceExecutor(adviceIndexes, EMTPY_ARRAY_LIST)
        );
    }

    /**
     * Create a catch clause join point.
     *
     * @param catchClauseSignature
     * @param declaringClass
     * @param adviceIndexes
     * @return
     */
    private JoinPoint createCatchClauseJoinPoint(final String catchClauseSignature,
                                                 final Class declaringClass,
                                                 final AdviceContainer[] adviceIndexes) {
        Signature signature = new CatchClauseSignatureImpl(declaringClass, catchClauseSignature);
        // TODO: enable cflow for catch clauses
//        List cflowExpressions = m_system.getAspectManager().getCFlowExpressions(
//                ReflectionMetaDataMaker.createClassMetaData(declaringClass),
//                ReflectionMetaDataMaker.createCatchClauseMetaData(signature)
//        );
        return new CatchClauseJoinPoint(
                m_uuid, m_targetClass, signature,
                createAroundAdviceExecutor(adviceIndexes, EMTPY_ARRAY_LIST, JoinPointType.CATCH_CLAUSE),
                createBeforeAdviceExecutor(adviceIndexes, EMTPY_ARRAY_LIST),
                createAfterAdviceExecutor(adviceIndexes, EMTPY_ARRAY_LIST)
        );
    }

    /**
     * Creates an around advice executor.
     *
     * @param adviceIndexes
     * @param cflowExpressions
     * @param joinPointType
     * @return the advice executor
     */
    private AdviceExecutor createAroundAdviceExecutor(final AdviceContainer[] adviceIndexes,
                                                      final List cflowExpressions,
                                                      final int joinPointType) {
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
        return new AroundAdviceExecutor(aroundAdvices, cflowExpressions, m_system, joinPointType);
    }

    /**
     * Creates a before advice executor.
     *
     * @param adviceIndexes
     * @param cflowExpressions
     * @return the advice executor
     */
    private AdviceExecutor createBeforeAdviceExecutor(final AdviceContainer[] adviceIndexes,
                                                      final List cflowExpressions) {
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
        return new BeforeAdviceExecutor(beforeAdvices, cflowExpressions, m_system);
    }

    /**
     * Creates an after advice executor.
     *
     * @param adviceIndexes
     * @param cflowExpressions
     * @return the advice executor
     */
    private AdviceExecutor createAfterAdviceExecutor(final AdviceContainer[] adviceIndexes,
                                                     final List cflowExpressions) {
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
        return new AfterAdviceExecutor(afterAdvices, cflowExpressions, m_system);
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
}



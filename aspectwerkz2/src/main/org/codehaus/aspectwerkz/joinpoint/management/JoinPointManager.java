/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
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
import java.lang.reflect.Method;

import gnu.trove.TLongObjectHashMap;
import gnu.trove.TLongLongHashMap;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseSignatureImpl;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Manages the join points, invokes the correct advice chains, handles redeployment, JIT compilation etc.
 * One instance per advised class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
        String noJIT = java.lang.System.getProperty("aspectwerkz.nojit");
        if ((noJIT != null && ("true".equalsIgnoreCase(noJIT) || "yes".equalsIgnoreCase(noJIT)))) {
            ENABLE_JIT_COMPILATION = false;
        }
        else {
            ENABLE_JIT_COMPILATION = true;
        }
    }

    private static final Map s_managers = new HashMap();
    private static final JoinPointRegistry s_registry = new JoinPointRegistry();

    private final System m_system;
    private final String m_uuid;
    private final Class m_targetClass;
    private final long m_classHash;
    private final ClassMetaData m_classMetaData;

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
     * @param className
     * @param uuid
     * @return the join point manager instance for this class
     */
    public static JoinPointManager getJoinPointManager(final String className, final String uuid) {
        if (s_managers.containsKey(className)) {
            return (JoinPointManager)s_managers.get(className);
        }
        else {
            JoinPointManager manager = new JoinPointManager(className, uuid);
            s_managers.put(className, manager);
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
     *      if (___AW_joinPointManager.hasAdvices(joinPointHash)) {
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
     *        return (ReturnType)___AW_joinPointManager.proceedWithExecutionJoinPoint(
     *            joinPointHash, new Object[]{parameter}, this,
     *            JoinPointType.METHOD_EXECUTION, AdviceType.AROUND, joinPointSignature
     *        );
     * </pre>
     *
     * @param joinPointHash
     * @param parameters
     * @param targetInstance null if invoked in a static context
     * @param joinPointType
     * @param adviceType
     * @param joinPointSignature
     * @return the result from the method invocation
     * @throws Throwable
     */
    public Object proceedWithExecutionJoinPoint(final int joinPointHash,
                                                final Object[] parameters,
                                                final Object targetInstance,
                                                final int joinPointType,
                                                final int adviceType,
                                                final String joinPointSignature)
            throws Throwable {

        // get the state for the join point
        final long joinPointState = s_registry.getStateForJoinPoint(m_classHash, joinPointHash);

        if (joinPointState == JoinPointState.NOT_ADVISED) {
            registerJoinPoint(joinPointType, joinPointHash, joinPointSignature);
        }

        JoinPoint joinPoint = null;
        if (ENABLE_JIT_COMPILATION) {
            joinPoint = handleJitCompilation(joinPointHash, joinPointState);
        }
        if (joinPoint == null) {
            // get the join point from the cache
            joinPoint = (JoinPoint)m_joinPoints.get(joinPointHash);

            // if null or redefined -> create a new join point and cache it
            if (joinPoint == null || joinPointState == JoinPointState.REDEFINED) {
                Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, joinPointHash);

                AdviceContainer[] adviceIndexes = null;
                switch (joinPointType) {
                    case JoinPointType.METHOD_EXECUTION:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.EXECUTION);
                        joinPoint = createMethodJoinPoint(joinPointHash, joinPointType, adviceIndexes);
                        break;

                    case JoinPointType.CONSTRUCTOR_EXECUTION:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.EXECUTION);
                        joinPoint = createConstructorJoinPoint(joinPointHash, joinPointType, adviceIndexes);
                        break;

                    default:
                        throw new RuntimeException("join point type not valid");
                }

                // create the join point
                m_joinPoints.put(joinPointHash, joinPoint);
            }
        }

        // intialize the join point before each usage
        ((MethodJoinPoint)joinPoint).initialize(targetInstance, parameters);

        return joinPoint.proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the
     * target instance.
     * <p/>
     * Example of bytecode needed to be generated to invoke the method:
     * <pre>
     *        ___AW_joinPointManager.proceedWithCallJoinPoint(
     *            joinPointHash, new Object[]{parameter}, this, declaringClass,
     *            JoinPointType.METHOD_CALL, AdviceType.BEFORE, joinPointSignature
     *       );
     * </pre>
     *
     * @param joinPointHash
     * @param parameters
     * @param targetInstance
     * @param declaringClass
     * @param joinPointType
     * @param adviceType
     * @param joinPointSignature
     * @return the result from the method invocation
     * @throws Throwable
     */
    public Object proceedWithCallJoinPoint(final int joinPointHash,
                                           final Object[] parameters,
                                           final Object targetInstance,
                                           final Class declaringClass,
                                           final int joinPointType,
                                           final int adviceType,
                                           final String joinPointSignature)
            throws Throwable {

        // get the state for the join point
        final long joinPointState = s_registry.getStateForJoinPoint(m_classHash, joinPointHash);

        if (joinPointState == JoinPointState.NOT_ADVISED) {
            registerJoinPoint(joinPointType, joinPointHash, joinPointSignature);
        }

        JoinPoint joinPoint = null;
        if (ENABLE_JIT_COMPILATION) {
            joinPoint = handleJitCompilation(joinPointHash, joinPointState);
        }
        if (joinPoint == null) {
            // get the join point from the cache
            joinPoint = (JoinPoint)m_joinPoints.get(joinPointHash);

            // if null or redefined -> create a new join point and cache it
            if (joinPoint == null || joinPointState == JoinPointState.REDEFINED) {
                m_invocations.put(joinPointHash, 0L);
                Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, joinPointHash);

                AdviceContainer[] adviceIndexes = null;
                switch (joinPointType) {
                    case JoinPointType.METHOD_CALL:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.CALL);
                        joinPoint = createMethodJoinPoint(joinPointHash, joinPointType, adviceIndexes);
                        break;

                    case JoinPointType.CONSTRUCTOR_CALL:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.CALL);
                        joinPoint = createConstructorJoinPoint(joinPointHash, joinPointType, adviceIndexes);
                        break;

                    default:
                        throw new RuntimeException("join point type not valid");
                }

                // create the join point
                m_joinPoints.put(joinPointHash, joinPoint);
            }
        }

        // intialize the join point before each usage
        ((MethodJoinPoint)joinPoint).initialize(targetInstance, parameters);

        return joinPoint.proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the
     * target instance.
     * <p/>
     * Example of bytecode needed to be generated to invoke the method:
     * <pre>
     *        ___AW_joinPointManager.proceedWithSetGetJoinPoint(
     *            joinPointHash, fieldValue, this,
     *            JoinPointType.FIELD_SET, AdviceType.BEFORE, joinPointSignature
     *       );
     * </pre>
     *
     * @param joinPointHash
     * @param fieldValue
     * @param targetInstance
     * @param joinPointType
     * @param adviceType
     * @param joinPointSignature
     * @throws Throwable
     */
    public void proceedWithSetGetJoinPoint(final int joinPointHash,
                                           final Object fieldValue,
                                           final Object targetInstance,
                                           final int joinPointType,
                                           final int adviceType,
                                           final String joinPointSignature)
            throws Throwable {

        // get the state for the join point
        final long joinPointState = s_registry.getStateForJoinPoint(m_classHash, joinPointHash);

        if (joinPointState == JoinPointState.NOT_ADVISED) {
            registerJoinPoint(joinPointType, joinPointHash, joinPointSignature);
        }

        JoinPoint joinPoint = null;
        if (ENABLE_JIT_COMPILATION) {
            joinPoint = handleJitCompilation(joinPointHash, joinPointState);
        }
        if (joinPoint == null) {
            // get the join point from the cache
            joinPoint = (JoinPoint)m_joinPoints.get(joinPointHash);

            // if null or redefined -> create a new join point and cache it
            if (joinPoint == null || joinPointState == JoinPointState.REDEFINED) {
                m_invocations.put(joinPointHash, 0L);
                Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, joinPointHash);

                AdviceContainer[] adviceIndexes = null;
                switch (joinPointType) {
                    case JoinPointType.FIELD_SET:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.SET);
                        joinPoint = createFieldJoinPoint(joinPointSignature, joinPointType, adviceIndexes);
                        break;

                    case JoinPointType.FIELD_GET:
                        adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.GET);
                        joinPoint = createFieldJoinPoint(joinPointSignature, joinPointType, adviceIndexes);
                        break;

                    default:
                        throw new RuntimeException("join point type not valid");
                }

                // create the join point
                m_joinPoints.put(joinPointHash, joinPoint);
            }
        }

        // intialize the join point before each usage
        ((FieldJoinPoint)joinPoint).initialize(targetInstance, fieldValue);

        joinPoint.proceed();
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the
     * target instance.
     * <p/>
     * Example of bytecode needed to be generated to invoke the method:
     * <pre>
     *        ___AW_joinPointManager.proceedWithCatchClauseJoinPoint(
     *            joinPointHash, exceptionInstance, this,
     *            JoinPointType.FIELD_SET, AdviceType.BEFORE, joinPointSignature
     *       );
     * </pre>
     *
     * @param joinPointHash
     * @param exceptionInstance
     * @param targetInstance
     * @param joinPointType
     * @param adviceType
     * @param joinPointSignature
     * @throws Throwable
     */
    public void proceedWithCatchClauseJoinPoint(final int joinPointHash,
                                                final Object exceptionInstance,
                                                final Object targetInstance,
                                                final int joinPointType,
                                                final int adviceType,
                                                final String joinPointSignature)
            throws Throwable {

        // get the state for the join point
        final long joinPointState = s_registry.getStateForJoinPoint(m_classHash, joinPointHash);

        if (joinPointState == JoinPointState.NOT_ADVISED) {
            registerJoinPoint(joinPointType, joinPointHash, joinPointSignature);
        }

        JoinPoint joinPoint = null;
        if (ENABLE_JIT_COMPILATION) {
            joinPoint = handleJitCompilation(joinPointHash, joinPointState);
        }
        if (joinPoint == null) {
            // get the join point from the cache
            joinPoint = (JoinPoint)m_joinPoints.get(joinPointHash);

            // if null or redefined -> create a new join point and cache it
            if (joinPoint == null || joinPointState == JoinPointState.REDEFINED) {
                m_invocations.put(joinPointHash, 0L);
                Map pointcutTypeToAdvicesMap = s_registry.getAdvicesForJoinPoint(m_classHash, joinPointHash);

                if (joinPointType == JoinPointType.CATCH_CLAUSE) {
                    AdviceContainer[] adviceIndexes = (AdviceContainer[])pointcutTypeToAdvicesMap.get(PointcutType.CATCH_CLAUSE);
                    joinPoint = createCatchClauseJoinPoint(joinPointType, joinPointSignature, adviceIndexes);
                }
                else {
                    throw new RuntimeException("join point type not valid");
                }

                // create the join point
                m_joinPoints.put(joinPointHash, joinPoint);
            }
        }

        // intialize the join point before each usage
        ((CatchClauseJoinPoint)joinPoint).initialize(targetInstance, exceptionInstance);

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
     */
    private void registerJoinPoint(final int joinPointType,
                                   final int joinPointHash,
                                   final String joinPointSignature) {
        // register as new the join point if not initialized
        s_registry.registerJoinPoint(
                joinPointType, joinPointHash, joinPointSignature, m_classHash,
                m_classMetaData, m_targetClass, m_system
        );
    }

    /**
     * Create a method join point.
     *
     * @param joinPointHash
     * @param joinPointType
     * @param adviceIndexes
     * @return
     */
    private JoinPoint createMethodJoinPoint(final int joinPointHash,
                                            final int joinPointType,
                                            final AdviceContainer[] adviceIndexes) {

        Method method = m_system.getAspectManager().getMethod(m_targetClass, joinPointHash);
        Class declaringType = method.getDeclaringClass();
        Signature signature = new MethodSignatureImpl(declaringType, method);

        return new MethodJoinPoint(
                m_uuid, joinPointType, m_targetClass, signature,
                createAroundAdviceExecutor(adviceIndexes),
                createBeforeAdviceExecutor(adviceIndexes),
                createAfterAdviceExecutor(adviceIndexes)
        );
    }

    /**
     * Create a constructor join point.
     *
     * @param joinPointHash
     * @param joinPointType
     * @param adviceIndexes
     * @return
     */
    private JoinPoint createConstructorJoinPoint(final int joinPointHash,
                                                 final int joinPointType,
                                                 final AdviceContainer[] adviceIndexes) {

        Method method = m_system.getAspectManager().getConstructor(m_targetClass, joinPointHash);
        Class declaringType = method.getDeclaringClass();
        Signature signature = new MethodSignatureImpl(declaringType, method);

        return new ConstructorJoinPoint(
                m_uuid, joinPointType, m_targetClass, signature,
                createAroundAdviceExecutor(adviceIndexes),
                createBeforeAdviceExecutor(adviceIndexes),
                createAfterAdviceExecutor(adviceIndexes)
        );
    }

    /**
     * Create a field join point.
     *
     * @param joinPointSignature
     * @param joinPointType
     * @param adviceIndexes
     * @return
     */
    private JoinPoint createFieldJoinPoint(final String joinPointSignature,
                                           final int joinPointType,
                                           final AdviceContainer[] adviceIndexes) {
        Signature signature = new FieldSignatureImpl(m_targetClass, joinPointSignature);
        return new FieldJoinPoint(
                m_uuid, joinPointType, m_targetClass, signature,
                createAroundAdviceExecutor(adviceIndexes),
                createBeforeAdviceExecutor(adviceIndexes),
                createAfterAdviceExecutor(adviceIndexes)
        );
    }

    /**
     * Create a catch clause join point.
     *
     * @param joinPointType
     * @param joinPointSignature
     * @param adviceIndexes
     * @return
     */
    private JoinPoint createCatchClauseJoinPoint(final int joinPointType,
                                                 final String joinPointSignature,
                                                 final AdviceContainer[] adviceIndexes) {
        Signature signature = new CatchClauseSignatureImpl(m_targetClass, joinPointSignature);
        return new CatchClauseJoinPoint(
                m_uuid, joinPointType, m_targetClass, signature,
                createAroundAdviceExecutor(adviceIndexes),
                createBeforeAdviceExecutor(adviceIndexes),
                createAfterAdviceExecutor(adviceIndexes)
        );
    }

    /**
     * Creates an around advice executor.
     *
     * @param adviceIndexes
     * @return the advice executor
     */
    private AdviceExecutor createAroundAdviceExecutor(final AdviceContainer[] adviceIndexes) {
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
        return new AroundAdviceExecutor(aroundAdvices);
    }

    /**
     * Creates a before advice executor.
     *
     * @param adviceIndexes
     * @return the advice executor
     */
    private AdviceExecutor createBeforeAdviceExecutor(final AdviceContainer[] adviceIndexes) {
        int i, j;
        List beforeAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceContainer adviceIndex = adviceIndexes[i];
            IndexTuple[] indexTuples = adviceIndex.getAroundAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                beforeAdviceList.add(indexTuples[j]);
            }
        }
        IndexTuple[] beforeAdvices = new IndexTuple[beforeAdviceList.size()];
        i = 0;
        for (Iterator it = beforeAdviceList.iterator(); it.hasNext(); i++) {
            beforeAdvices[i] = (IndexTuple)it.next();
        }
        return new BeforeAdviceExecutor(beforeAdvices);
    }

    /**
     * Creates an after advice executor.
     *
     * @param adviceIndexes
     * @return the advice executor
     */
    private AdviceExecutor createAfterAdviceExecutor(final AdviceContainer[] adviceIndexes) {
        int i, j;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceContainer adviceIndex = adviceIndexes[i];
            IndexTuple[] indexTuples = adviceIndex.getAroundAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                afterAdviceList.add(indexTuples[j]);
            }
        }
        IndexTuple[] afterAdvices = new IndexTuple[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (IndexTuple)it.next();
        }
        return new AfterAdviceExecutor(afterAdvices);
    }

    /**
     * Creates a new join point manager for a specific class.
     *
     * @param className
     * @param uuid
     */
    private JoinPointManager(final String className, final String uuid) {
        m_uuid = uuid;
        m_system = SystemLoader.getSystem(m_uuid);

        try {
            m_targetClass = ContextClassLoader.loadClass(className);
        }
        catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
        m_classHash = m_targetClass.hashCode();
        m_classMetaData = ReflectionMetaDataMaker.createClassMetaData(m_targetClass);
    }
}



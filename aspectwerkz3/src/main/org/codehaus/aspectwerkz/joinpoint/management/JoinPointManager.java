/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.AdviceInfo;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.aspect.management.AspectRegistry;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.joinpoint.CodeRtti;
import org.codehaus.aspectwerkz.joinpoint.FieldRtti;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfoRepository;
import org.codehaus.aspectwerkz.transform.TransformationConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

//import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
//import EDU.oswego.cs.dl.util.concurrent.ReaderPreferenceReadWriteLock;

/**
 * Manages the join points, invokes the correct advice chains, handles redeployment, JIT compilation etc. Each advised
 * class' instance holds one instance of this class.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class JoinPointManager {
    /**
     * The JIT compilation boundry for nr of method invocations before optimizing a certain method.
     */
    private static final long JIT_COMPILATION_BOUNDRY;

    /**
     * Turns on/off the JIT compiler.
     */
    private static final boolean ENABLE_JIT_COMPILATION;

    static {
        String noJIT = java.lang.System.getProperty("aspectwerkz.jit.off");
        if (((noJIT != null) && ("true".equalsIgnoreCase(noJIT) || "yes".equalsIgnoreCase(noJIT)))) {
            ENABLE_JIT_COMPILATION = false;
        } else {
            ENABLE_JIT_COMPILATION = true;
        }
        String boundry = java.lang.System.getProperty("aspectwerkz.jit.boundry");
        if (boundry != null) {
            JIT_COMPILATION_BOUNDRY = new Long(boundry).longValue();
        } else {
            JIT_COMPILATION_BOUNDRY = 1L;
        }
    }

    /**
     * Block size of the join point index repository grow algorithm.
     */
    private static final int JOIN_POINT_INDEX_GROW_BLOCK = 10;

    private static final Map s_managers = new HashMap();

    private static final JoinPointRegistry s_registry = new JoinPointRegistry();

    private final JavaClassInfoRepository m_classInfoRepository;

    private final AspectSystem m_system;

    private final Class m_targetClass;

    private final int m_classHash;

    private int m_hotswapCount = 0;

    private ThreadLocal[] m_joinPoints = new ThreadLocal[0];

    //    private final ReadWriteLock m_readWriteLock = new ReaderPreferenceReadWriteLock();

    /**
     * Creates a new join point manager for a specific class.
     * 
     * @param targetClass
     */
    private JoinPointManager(final Class targetClass) {
        m_system = SystemLoader.getSystem(targetClass.getClassLoader());
        m_targetClass = targetClass;
        m_classHash = m_targetClass.hashCode();
        m_classInfoRepository = JavaClassInfoRepository.getRepository(targetClass.getClassLoader());
        m_hotswapCount = 0;
    }

    /**
     * Creates a new join point manager for a specific class.
     * 
     * @param targetClass
     * @param hotswapCount
     */
    private JoinPointManager(final Class targetClass, int hotswapCount) {
        m_system = SystemLoader.getSystem(targetClass.getClassLoader());
        m_targetClass = targetClass;
        m_classHash = m_targetClass.hashCode();
        m_classInfoRepository = JavaClassInfoRepository.getRepository(targetClass.getClassLoader());
        m_hotswapCount = hotswapCount;
    }

    /**
     * Returns the join point manager for a specific class.
     * 
     * @param targetClass
     * @param uuid
     * @return the join point manager instance for this class
     * @TODO: UUID is not use or needed anymore, remove it from the TFs
     */
    public final static JoinPointManager getJoinPointManager(final Class targetClass, final String uuid) {
        if (s_managers.containsKey(targetClass)) { //TODO AVAOPC should be Weak ?
            return (JoinPointManager) s_managers.get(targetClass);
        } else {
            JoinPointManager manager = new JoinPointManager(targetClass);
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
     * Checks if a join point is advised, this does not mean that it has any advices attached to it. <p/>This method
     * should be used by inserting a check in the wrapper/proxy method similar to this:
     * 
     * <pre>
     * if (___AW_joinPointManager.hasAdvices(joinPointHash)) {
     *     // execute the advice chain
     * } else {
     *     // invoke the prefixed target method
     * }
     * </pre>
     * 
     * @param joinPointHash
     * @return
     */
    public boolean isAdvised(final int joinPointHash) {
        // TODO: impl.
        return true;

        //        return s_registry.getStateForJoinPoint(m_classHash, joinPointHash) >
        // JoinPointState.NOT_ADVISED;
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
     * @return @throws Throwable
     */
    public final Object proceedWithExecutionJoinPoint(
        final int methodHash,
        final int joinPointIndex,
        final Object[] parameters,
        final Object targetInstance,
        final int joinPointType) throws Throwable {
        ThreadLocal threadLocal;
        if (joinPointIndex < 0) {
            throw new RuntimeException();
        }

        //        m_readWriteLock.writeLock().acquire();
        synchronized (m_joinPoints) {
            //        try {
            if ((joinPointIndex >= m_joinPoints.length) || (m_joinPoints[joinPointIndex] == null)) {
                s_registry.registerJoinPoint(
                    joinPointType,
                    methodHash,
                    null,
                    m_classHash,
                    m_targetClass,
                    null,//will be set to target method info
                    m_system);
                threadLocal = new ThreadLocal();
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            } else {
                threadLocal = m_joinPoints[joinPointIndex];
            }
        }

        //        } finally {
        //            m_readWriteLock.writeLock().release();
        //        }
        //
        JoinPointInfo joinPointInfo = (JoinPointInfo) threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }
        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                methodHash,
                joinPointType,
                PointcutType.EXECUTION,
                joinPointInfo,
                m_targetClass,                      
                m_targetClass,
                targetInstance,
                targetInstance);
        }
        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map joinPointMetaDataMap = s_registry.getJoinPointMetaData(m_classHash, methodHash);
            JoinPointMetaData joinPointMetaData = (JoinPointMetaData) joinPointMetaDataMap.get(PointcutType.EXECUTION);
            AdviceIndexInfo[] adviceIndexes = joinPointMetaData.adviceIndexes;
            List cflowExpressions = joinPointMetaData.cflowExpressions;
            Pointcut cflowPointcut = joinPointMetaData.cflowPointcut;
            initCflowManagement(cflowPointcut, joinPointInfo);
            switch (joinPointType) {
                case JoinPointType.METHOD_EXECUTION:
                    joinPoint = createMethodJoinPoint(
                        methodHash,
                        joinPointType,
                        m_targetClass,
                        adviceIndexes,
                        joinPointMetaData,
                        targetInstance,
                        targetInstance);
                    break;
                case JoinPointType.CONSTRUCTOR_EXECUTION:
                    joinPoint = createConstructorJoinPoint(
                        methodHash,
                        joinPointType,
                        m_targetClass,
                        adviceIndexes,
                        joinPointMetaData,
                        targetInstance,
                        targetInstance);
                    break;
                default:
                    throw new RuntimeException("join point type not valid");
            }

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            } else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        // set the RTTI
        Rtti rtti = joinPoint.getRtti().cloneFor(targetInstance, targetInstance);
        if (parameters != null) {
            ((CodeRtti) rtti).setParameterValues(parameters);
        }
        setRtti(joinPointInfo, rtti);
        enterCflow(joinPointInfo);
        try {
            return joinPoint.proceed();
        } finally {
            unsetRtti(joinPointInfo);
            exitCflow(joinPointInfo);
        }
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the target
     * instance.
     * 
     * @param methodHash
     * @param joinPointIndex
     * @param parameters
     * @param targetClass
     * @param targetInstance
     * @param thisClass
     * @param thisInstance
     * @param withinMethodName
     * @param withinMethodSignature
     * @param joinPointType
     * @return the result from the method invocation
     * @throws Throwable
     */
    public final Object proceedWithCallJoinPoint(
        final int methodHash,
        final int joinPointIndex,
        final Object[] parameters,
        final Class targetClass,
        final Object targetInstance,
        final Class thisClass,
        final Object thisInstance,
        final String withinMethodName,
        final String withinMethodSignature,
        final int joinPointType) throws Throwable {
        ThreadLocal threadLocal;
        synchronized (m_joinPoints) {
            if ((joinPointIndex >= m_joinPoints.length) || (m_joinPoints[joinPointIndex] == null)) {
                MemberInfo withinMemberInfo = ClassInfoHelper.createMemberInfo(
                    targetClass,
                    withinMethodName,
                    withinMethodSignature);
                s_registry.registerJoinPoint(
                    joinPointType,
                    methodHash,
                    null,
                    m_classHash,
                    thisClass,
                    withinMemberInfo,
                    m_system);
                threadLocal = new ThreadLocal();
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            } else {
                threadLocal = m_joinPoints[joinPointIndex];
            }
        }
        JoinPointInfo joinPointInfo = (JoinPointInfo) threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }

        // TODO: make diff between target and this instances
        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                methodHash,
                joinPointType,
                PointcutType.CALL,
                joinPointInfo,
                thisClass,
                m_targetClass,
                thisInstance,
                thisInstance);
        }
        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map joinPointMetaDataMap = s_registry.getJoinPointMetaData(m_classHash, methodHash);
            JoinPointMetaData joinPointMetaData = (JoinPointMetaData) joinPointMetaDataMap.get(PointcutType.CALL);
            AdviceIndexInfo[] adviceIndexes = joinPointMetaData.adviceIndexes;
            List cflowExpressions = joinPointMetaData.cflowExpressions;
            Pointcut cflowPointcut = joinPointMetaData.cflowPointcut;
            initCflowManagement(cflowPointcut, joinPointInfo);
            switch (joinPointType) {
                case JoinPointType.METHOD_CALL:

                    // TODO: make diff between target and this instances
                    joinPoint = createMethodJoinPoint(
                        methodHash,
                        joinPointType,
                        thisClass,
                        adviceIndexes,
                        joinPointMetaData,
                        thisInstance,
                        thisInstance);
                    break;
                case JoinPointType.CONSTRUCTOR_CALL:

                    // TODO: make diff between target and this instances
                    joinPoint = createConstructorJoinPoint(
                        methodHash,
                        joinPointType,
                        thisClass,
                        adviceIndexes,
                        joinPointMetaData,
                        thisInstance,
                        thisInstance);
                    break;
                default:
                    throw new RuntimeException("join point type not valid");
            }

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            } else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        Rtti rtti = joinPoint.getRtti().cloneFor(targetInstance, thisInstance);//AW-265
        if (parameters != null) {
            ((CodeRtti) rtti).setParameterValues(parameters);
        }
        setRtti(joinPointInfo, rtti);
        enterCflow(joinPointInfo);
        try {
            return joinPoint.proceed();
        } finally {
            unsetRtti(joinPointInfo);
            exitCflow(joinPointInfo);
        }
    }

    /**
     * Proceeds with the invocation of the join point, passing on the method hash, the parameter values and the target
     * instance.
     * 
     * @param fieldHash
     * @param joinPointIndex
     * @param fieldValue as the first arg in an Object array
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
        ThreadLocal threadLocal;
        synchronized (m_joinPoints) {
            if ((joinPointIndex >= m_joinPoints.length) || (m_joinPoints[joinPointIndex] == null)) {
                s_registry.registerJoinPoint(
                    JoinPointType.FIELD_SET,
                    fieldHash,
                    fieldSignature,
                    m_classHash,
                    declaringClass,
                    null,//AVAJ within/withincode support ?
                    m_system);
                threadLocal = new ThreadLocal();
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            } else {
                threadLocal = m_joinPoints[joinPointIndex];
            }
        }
        JoinPointInfo joinPointInfo = (JoinPointInfo) threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }
        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                fieldHash,
                JoinPointType.FIELD_SET,
                PointcutType.SET,
                joinPointInfo,
                declaringClass,
                m_targetClass,
                targetInstance,
                targetInstance);
        }
        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map joinPointMetaDataMap = s_registry.getJoinPointMetaData(m_classHash, fieldHash);
            JoinPointMetaData joinPointMetaData = (JoinPointMetaData) joinPointMetaDataMap.get(PointcutType.SET);
            AdviceIndexInfo[] adviceIndexes = joinPointMetaData.adviceIndexes;
            List cflowExpressions = joinPointMetaData.cflowExpressions;
            Pointcut cflowPointcut = joinPointMetaData.cflowPointcut;
            initCflowManagement(cflowPointcut, joinPointInfo);
            joinPoint = createFieldJoinPoint(
                fieldHash,
                JoinPointType.FIELD_SET,
                m_targetClass,
                adviceIndexes,
                joinPointMetaData,
                targetInstance,
                targetInstance);

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            } else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        // intialize the join point before each usage
        Rtti rtti = joinPoint.getRtti().cloneFor(targetInstance, targetInstance);//AW-265
        if (fieldValue[0] != null) {
            ((FieldRtti) rtti).setFieldValue(fieldValue[0]);
            // array due to sucky javassist field handling
        }
        setRtti(joinPointInfo, rtti);
        enterCflow(joinPointInfo);
        try {
            joinPoint.proceed();
        } finally {
            unsetRtti(joinPointInfo);
            exitCflow(joinPointInfo);
        }
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
        ThreadLocal threadLocal;
        synchronized (m_joinPoints) {
            if ((joinPointIndex >= m_joinPoints.length) || (m_joinPoints[joinPointIndex] == null)) {
                s_registry.registerJoinPoint(
                    JoinPointType.FIELD_GET,
                    fieldHash,
                    fieldSignature,
                    m_classHash,
                    declaringClass,
                    null,//AVAJ within/withincode support ?
                    m_system);
                threadLocal = new ThreadLocal();
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            } else {
                threadLocal = m_joinPoints[joinPointIndex];
            }
        }
        JoinPointInfo joinPointInfo = (JoinPointInfo) threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }
        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                fieldHash,
                JoinPointType.FIELD_GET,
                PointcutType.GET,
                joinPointInfo,
                declaringClass,
                m_targetClass,
                targetInstance,
                targetInstance);
        }
        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map joinPointMetaDataMap = s_registry.getJoinPointMetaData(m_classHash, fieldHash);
            JoinPointMetaData joinPointMetaData = (JoinPointMetaData) joinPointMetaDataMap.get(PointcutType.GET);
            AdviceIndexInfo[] adviceIndexes = joinPointMetaData.adviceIndexes;
            List cflowExpressions = joinPointMetaData.cflowExpressions;
            Pointcut cflowPointcut = joinPointMetaData.cflowPointcut;
            initCflowManagement(cflowPointcut, joinPointInfo);
            joinPoint = createFieldJoinPoint(
                fieldHash,
                JoinPointType.FIELD_GET,
                m_targetClass,
                adviceIndexes,
                joinPointMetaData,
                targetInstance,
                targetInstance);

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            } else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        // intialize the join point before each usage
        Rtti rtti = joinPoint.getRtti().cloneFor(targetInstance, targetInstance);//AW-265
        setRtti(joinPointInfo, rtti);
        enterCflow(joinPointInfo);
        try {
            return joinPoint.proceed();
        } finally {
            unsetRtti(joinPointInfo);
            exitCflow(joinPointInfo);
        }
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
        ThreadLocal threadLocal;
        synchronized (m_joinPoints) {
            if ((joinPointIndex >= m_joinPoints.length) || (m_joinPoints[joinPointIndex] == null)) {
                ClassInfo withinClassInfo = createClassInfo(m_targetClass);//AVAJ within/withincode support ?
                s_registry.registerJoinPoint(
                    JoinPointType.HANDLER,
                    handlerHash,
                    handlerSignature,
                    m_classHash,
                    exceptionInstance.getClass(),
                    withinClassInfo,
                    m_system);
                threadLocal = new ThreadLocal();
                if (m_joinPoints.length <= joinPointIndex) {
                    ThreadLocal[] tmp = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(m_joinPoints, 0, tmp, 0, m_joinPoints.length);
                    m_joinPoints = new ThreadLocal[joinPointIndex + JOIN_POINT_INDEX_GROW_BLOCK];
                    java.lang.System.arraycopy(tmp, 0, m_joinPoints, 0, tmp.length);
                }
                m_joinPoints[joinPointIndex] = threadLocal;
            } else {
                threadLocal = m_joinPoints[joinPointIndex];
            }
        }
        JoinPointInfo joinPointInfo = (JoinPointInfo) threadLocal.get();
        if (joinPointInfo == null) {
            joinPointInfo = new JoinPointInfo();
            threadLocal.set(joinPointInfo);
        }
        if (ENABLE_JIT_COMPILATION && !joinPointInfo.isJitCompiled) {
            handleJitCompilation(
                handlerHash,
                JoinPointType.HANDLER,
                PointcutType.HANDLER,
                joinPointInfo,
                m_targetClass,
                m_targetClass,
                targetInstance,
                targetInstance);
        }
        JoinPoint joinPoint = joinPointInfo.joinPoint;

        // if null or redefined -> create a new join point and cache it
        if (joinPoint == null) {
            Map joinPointMetaDataMap = s_registry.getJoinPointMetaData(m_classHash, handlerHash);
            JoinPointMetaData joinPointMetaData = (JoinPointMetaData) joinPointMetaDataMap.get(PointcutType.HANDLER);
            AdviceIndexInfo[] adviceIndexes = joinPointMetaData.adviceIndexes;
            List cflowExpressions = joinPointMetaData.cflowExpressions;
            Pointcut cflowPointcut = joinPointMetaData.cflowPointcut;
            initCflowManagement(cflowPointcut, joinPointInfo);
            joinPoint = createCatchClauseJoinPoint(
                exceptionInstance.getClass(),
                m_targetClass,
                handlerSignature,
                adviceIndexes,
                joinPointMetaData,
                targetInstance,
                targetInstance);

            // set the join point
            joinPointInfo.joinPoint = joinPoint;

            // update the state
            if (adviceIndexes.length == 0) {
                joinPointInfo.state = JoinPointState.ADVISED;
            } else {
                joinPointInfo.state = JoinPointState.HAS_ADVICES;
            }
        }

        // intialize the join point before each usage
        Rtti rtti = joinPoint.getRtti().cloneFor(targetInstance, targetInstance);//AW-265
        setRtti(joinPointInfo, rtti);
        enterCflow(joinPointInfo);
        try {
            joinPoint.proceed();
        } finally {
            unsetRtti(joinPointInfo);
            exitCflow(joinPointInfo);
        }
    }

    /**
     * Creates a class info instance out of a class instance.
     * 
     * @param klass
     * @return class info
     */
    private ClassInfo createClassInfo(final Class klass) {
        ClassInfo classInfo = m_classInfoRepository.getClassInfo(klass.getName());
        if (classInfo == null) {
            classInfo = JavaClassInfo.getClassInfo(klass);
        }
        return classInfo;
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
     * @param thisInstance
     * @param targetInstance
     */
    private final void handleJitCompilation(
        final int joinPointHash,
        final int joinPointType,
        final PointcutType pointcutType,
        final JoinPointInfo joinPointInfo,
        final Class declaringClass,
        final Class targetClass,
        final Object thisInstance,
        final Object targetInstance) {
        synchronized (joinPointInfo) {
            joinPointInfo.invocations++;
            if (joinPointInfo.state == JoinPointState.REDEFINED) {
                joinPointInfo.invocations = 0L;
            } else if (joinPointInfo.invocations == JIT_COMPILATION_BOUNDRY) {
                Map joinPointMetaDataMap = s_registry.getJoinPointMetaData(m_classHash, joinPointHash);
                if (joinPointMetaDataMap.containsKey(pointcutType)) {
                    JoinPointMetaData joinPointMetaData = (JoinPointMetaData) joinPointMetaDataMap.get(pointcutType);
                    AdviceIndexInfo[] adviceIndexes = joinPointMetaData.adviceIndexes;
                    joinPointInfo.joinPoint = JitCompiler.compileJoinPoint(
                        joinPointHash,
                        joinPointType,
                        pointcutType,
                        adviceIndexes,
                        declaringClass,
                        targetClass,
                        m_system,
                        thisInstance,
                        targetInstance,
                        m_hotswapCount);
                    joinPointInfo.isJitCompiled = true;
                }
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
     * @param joinPointMetaData
     * @param thisInstance
     * @param targetInstance
     * @return
     */
    private final MethodJoinPoint createMethodJoinPoint(
        final int methodHash,
        final int joinPointType,
        final Class declaringClass,
        final AdviceIndexInfo[] adviceIndexes,
        final JoinPointMetaData joinPointMetaData,
        final Object thisInstance,
        final Object targetInstance) {
        MethodTuple methodTuple = AspectRegistry.getMethodTuple(declaringClass, methodHash);
        Class declaringType = methodTuple.getDeclaringClass();
        MethodSignatureImpl signature = new MethodSignatureImpl(declaringType, methodTuple);
        Rtti rtti = new MethodRttiImpl(signature, thisInstance, targetInstance);
        return new MethodJoinPoint(
            joinPointType,
            m_targetClass,
            signature,
            rtti,
            joinPointMetaData,
            createAroundAdviceExecutor(adviceIndexes, joinPointType),
            createBeforeAdviceExecutor(adviceIndexes),
            createAfterAdviceExecutor(adviceIndexes));
    }

    /**
     * Create a constructor join point.
     * 
     * @param constructorHash
     * @param joinPointType
     * @param declaringClass
     * @param adviceIndexes
     * @param joinPointMetaData
     * @param thisInstance
     * @param targetInstance
     * @return
     */
    private final JoinPoint createConstructorJoinPoint(
        final int constructorHash,
        final int joinPointType,
        final Class declaringClass,
        final AdviceIndexInfo[] adviceIndexes,
        final JoinPointMetaData joinPointMetaData,
        final Object thisInstance,
        final Object targetInstance) {
        ConstructorTuple constructorTuple = AspectRegistry.getConstructorTuple(declaringClass, constructorHash);
        Class declaringType = constructorTuple.getDeclaringClass();
        ConstructorSignatureImpl signature = new ConstructorSignatureImpl(declaringType, constructorTuple);
        Rtti rtti = new ConstructorRttiImpl(signature, thisInstance, targetInstance);
        return new ConstructorJoinPoint(
            joinPointType,
            m_targetClass,
            signature,
            rtti,
            joinPointMetaData,
            createAroundAdviceExecutor(adviceIndexes, joinPointType),
            createBeforeAdviceExecutor(adviceIndexes),
            createAfterAdviceExecutor(adviceIndexes));
    }

    /**
     * Create a field join point.
     * 
     * @param fieldHash
     * @param joinPointType
     * @param declaringClass
     * @param adviceIndexes
     * @param joinPointMetaData
     * @param thisInstance
     * @param targetInstance
     * @return
     */
    private final JoinPoint createFieldJoinPoint(
        final int fieldHash,
        final int joinPointType,
        final Class declaringClass,
        final AdviceIndexInfo[] adviceIndexes,
        final JoinPointMetaData joinPointMetaData,
        final Object thisInstance,
        final Object targetInstance) {
        Field field = AspectRegistry.getField(declaringClass, fieldHash);
        FieldSignatureImpl signature = new FieldSignatureImpl(declaringClass, field);
        Rtti rtti = new FieldRttiImpl(signature, thisInstance, targetInstance);
        return new FieldJoinPoint(
            joinPointType,
            m_targetClass,
            signature,
            rtti,
            joinPointMetaData,
            createAroundAdviceExecutor(adviceIndexes, joinPointType),
            createBeforeAdviceExecutor(adviceIndexes),
            createAfterAdviceExecutor(adviceIndexes));
    }

    /**
     * Create a catch clause join point.
     * 
     * @param exceptionClass
     * @param declaringClass
     * @param catchClauseSignature
     * @param adviceIndexes
     * @param joinPointMetaData
     * @param thisInstance
     * @param targetInstance
     * @return
     */
    private final JoinPoint createCatchClauseJoinPoint(
        final Class exceptionClass,
        final Class declaringClass,
        final String catchClauseSignature,
        final AdviceIndexInfo[] adviceIndexes,
        final JoinPointMetaData joinPointMetaData,
        final Object thisInstance,
        final Object targetInstance) {
        CatchClauseSignatureImpl signature = new CatchClauseSignatureImpl(
            exceptionClass,
            declaringClass,
            catchClauseSignature);
        Rtti rtti = new CatchClauseRttiImpl(signature, thisInstance, targetInstance);
        return new CatchClauseJoinPoint(m_targetClass, signature, rtti, joinPointMetaData, createAroundAdviceExecutor(
            adviceIndexes,
            JoinPointType.HANDLER), createBeforeAdviceExecutor(adviceIndexes), createAfterAdviceExecutor(adviceIndexes));
    }

    /**
     * Creates an around advice executor.
     * 
     * @param adviceIndexes
     * @param joinPointType
     * @return the advice executor
     */
    private final AroundAdviceExecutor createAroundAdviceExecutor(
        final AdviceIndexInfo[] adviceIndexes,
        final int joinPointType) {
        return new AroundAdviceExecutor(extractAroundAdvices(adviceIndexes), joinPointType);
    }

    /**
     * Creates a before advice executor.
     * 
     * @param adviceIndexes
     * @return the advice executor
     */
    private final BeforeAdviceExecutor createBeforeAdviceExecutor(final AdviceIndexInfo[] adviceIndexes) {
        return new BeforeAdviceExecutor(extractBeforeAdvices(adviceIndexes));
    }

    /**
     * Creates an after advice executor.
     * 
     * @param adviceIndexes
     * @return the advice executor
     */
    private final AfterAdviceExecutor createAfterAdviceExecutor(final AdviceIndexInfo[] adviceIndexes) {
        return new AfterAdviceExecutor(extractAfterFinallyAdvices(adviceIndexes));
    }

    /**
     * Extracts the around advices.
     * 
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractAroundAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List aroundAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getAroundAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                aroundAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] aroundAdvices = new AdviceInfo[aroundAdviceList.size()];
        i = 0;
        for (Iterator it = aroundAdviceList.iterator(); it.hasNext(); i++) {
            aroundAdvices[i] = (AdviceInfo) it.next();
        }
        return aroundAdvices;
    }

    /**
     * Extracts the before advices.
     * 
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractBeforeAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List beforeAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getBeforeAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                beforeAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] beforeAdvices = new AdviceInfo[beforeAdviceList.size()];
        i = 0;
        for (Iterator it = beforeAdviceList.iterator(); it.hasNext(); i++) {
            beforeAdvices[i] = (AdviceInfo) it.next();
        }
        return beforeAdvices;
    }

    /**
     * Extracts the after finally advices.
     *
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractAfterFinallyAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getAfterFinallyAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                afterAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] afterAdvices = new AdviceInfo[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (AdviceInfo) it.next();
        }
        return afterAdvices;
    }

    /**
     * Extracts the after returning advices.
     *
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractAfterReturningAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getAfterReturningAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                afterAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] afterAdvices = new AdviceInfo[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (AdviceInfo) it.next();
        }
        return afterAdvices;
    }

    /**
     * Extracts the after throwing advices.
     *
     * @param adviceIndexes
     * @return
     */
    public final static AdviceInfo[] extractAfterThrowingAdvices(final AdviceIndexInfo[] adviceIndexes) {
        int i;
        int j;
        List afterAdviceList = new ArrayList();
        for (i = 0; i < adviceIndexes.length; i++) {
            AdviceIndexInfo adviceIndex = adviceIndexes[i];
            AdviceInfo[] indexTuples = adviceIndex.getAfterThrowingAdvices();
            for (j = 0; j < indexTuples.length; j++) {
                afterAdviceList.add(indexTuples[j]);
            }
        }
        AdviceInfo[] afterAdvices = new AdviceInfo[afterAdviceList.size()];
        i = 0;
        for (Iterator it = afterAdviceList.iterator(); it.hasNext(); i++) {
            afterAdvices[i] = (AdviceInfo) it.next();
        }
        return afterAdvices;
    }

    /**
     * Reset the join point manager.
     * 
     * @param klass
     */
    public static synchronized void reset(Class klass) {
        JoinPointManager oldJoinPointManager = getJoinPointManager(klass, "N/A/runtime");

        // flush JP Registry
        s_registry.reset(klass.hashCode());
        JoinPointManager joinPointManager = new JoinPointManager(klass, oldJoinPointManager.m_hotswapCount + 1);
        s_managers.put(klass, joinPointManager);
        try {
            Field jpMan = klass.getDeclaredField(TransformationConstants.JOIN_POINT_MANAGER_FIELD);
            jpMan.setAccessible(true);
            jpMan.set(null, joinPointManager);
        } catch (Exception e) {
            System.err.println("ERROR: Unable to propagate JoinPointManager");
            e.printStackTrace();
        }
        joinPointManager.m_joinPoints = new ThreadLocal[0];
    }

    /**
     * Records entering into cflow.
     * 
     * @param joinPointInfo
     */
    private void enterCflow(final JoinPointInfo joinPointInfo) throws Throwable {
        AdviceInfo enter = joinPointInfo.enterCflow;
        if (enter != null) {
            enter.getAspectManager().getAspectContainer(enter.getAspectIndex()).invokeAdvice(
                enter.getMethodIndex(),
                joinPointInfo.joinPoint);
        }
    }

    /**
     * Records exiting from cflow.
     * 
     * @param joinPointInfo
     */
    private void exitCflow(final JoinPointInfo joinPointInfo) throws Throwable {
        AdviceInfo exit = joinPointInfo.exitCflow;
        if (exit != null) {
            exit.getAspectManager().getAspectContainer(exit.getAspectIndex()).invokeAdvice(
                exit.getMethodIndex(),
                joinPointInfo.joinPoint);
        }
    }

    /**
     * Initializes the cflow management.
     * 
     * @param cflowPointcut
     * @param joinPointInfo
     */
    private void initCflowManagement(final Pointcut cflowPointcut, final JoinPointInfo joinPointInfo) {
        if (cflowPointcut != null) {
            AdviceInfo[] beforeAdviceIndexes = cflowPointcut.getBeforeAdviceIndexes();
            AdviceInfo[] afterAdviceIndexes = cflowPointcut.getAfterFinallyAdviceIndexes();
            if ((beforeAdviceIndexes.length != 0) && (afterAdviceIndexes.length != 0)) {
                joinPointInfo.enterCflow = beforeAdviceIndexes[0];
                joinPointInfo.exitCflow = afterAdviceIndexes[0];
            }
        }
    }

    /**
     * Contains the JoinPoint instance and some RTTI about the join point.
     * This class is wrapped behing a ThreadLocal.
     * 
     */
    static class JoinPointInfo {
        public JoinPoint joinPoint = null;

        public int state = JoinPointState.NOT_ADVISED;

        public long invocations = 0L;

        public boolean isJitCompiled = false;

        public AdviceInfo enterCflow;

        public AdviceInfo exitCflow;

        /**
         * A stack of RTTI that allows us to keep RTTI even with reentrant target method call on other instances etc
         */
        public Stack rttiStack = new Stack();
    }

    /**
     * Helper method, pop the rtti from the stack and reset the JP with it
     *
     * @param joinPointInfo
     */
    private static void unsetRtti(final JoinPointInfo joinPointInfo) {
        JoinPoint joinPoint = joinPointInfo.joinPoint;
        ((JoinPointBase)joinPoint).setRtti((Rtti)joinPointInfo.rttiStack.pop());
    }

    /**
     * Helper method, push the rtti to the stack and reset the jp with it
     *
     * @param joinPointInfo
     * @param rtti
     */
    private static void setRtti(final JoinPointInfo joinPointInfo, final Rtti rtti) {
        JoinPoint joinPoint = joinPointInfo.joinPoint;
        ((JoinPointBase)joinPoint).setRtti(rtti);
        joinPointInfo.rttiStack.push(rtti);
    }
}
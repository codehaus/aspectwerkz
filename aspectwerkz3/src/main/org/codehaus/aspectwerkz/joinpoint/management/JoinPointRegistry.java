/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import gnu.trove.TLongObjectHashMap;
import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.aspect.management.AspectRegistry;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoRepository;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaConstructorInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaFieldInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaMethodInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private static final IndexTuple[] EMPTY_INDEX_TUPLE_ARRAY = new IndexTuple[] {  };

    /**
     * The registry with all the classes and the index for the advices attatched to the join points in this class.
     * <p/>
     * Map of: the class hash => map of: join point hash => map of: join point type => array with advice indexes.
     */
    private static final TLongObjectHashMap m_joinPointAdvicesMap = new TLongObjectHashMap();

    /**
     * The registry with all the classes and the index for the cflow expressions attatched to the join points in this
     * class.
     * <p/>
     * Map of: the class hash => map of: join point hash => map of: join point type => array cflow expressions.
     */
    private static final TLongObjectHashMap m_joinPointCflowExpressionMap = new TLongObjectHashMap();

    /**
     * Package private constructor.
     */
    JoinPointRegistry() {
    }

    /**
     * Registers the advices for the method join point.
     *
     * @param joinPointType
     * @param joinPointHash
     * @param signature
     * @param classHash
     * @param declaringClass
     * @param withinInfo
     * @param system
     * @TODO: cache the metadata created in the method - map it to the method hash (see pointcut for caching)
     */
    public void registerJoinPoint(final int joinPointType, final int joinPointHash, final String signature,
                                  final int classHash, final Class declaringClass, final ReflectionInfo withinInfo,
                                  final AspectSystem system) {
        if (!m_joinPointAdvicesMap.containsKey(classHash)) {
            m_joinPointAdvicesMap.put(classHash, new TLongObjectHashMap());
        }
        Map pointcutTypeToAdvicesMap = new HashMap();
        pointcutTypeToAdvicesMap.put(PointcutType.EXECUTION, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.CALL, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.SET, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.GET, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.HANDLER, EMTPY_ARRAY_LIST);
        pointcutTypeToAdvicesMap.put(PointcutType.ATTRIBUTE, EMTPY_ARRAY_LIST);
        ((TLongObjectHashMap)m_joinPointAdvicesMap.get(classHash)).put(joinPointHash, pointcutTypeToAdvicesMap);

        if (!m_joinPointCflowExpressionMap.containsKey(classHash)) {
            m_joinPointCflowExpressionMap.put(classHash, new TLongObjectHashMap());
        }
        Map pointcutTypeToCflowExpressionsMap = new HashMap();
        pointcutTypeToCflowExpressionsMap.put(PointcutType.EXECUTION, EMTPY_ARRAY_LIST);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.CALL, EMTPY_ARRAY_LIST);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.SET, EMTPY_ARRAY_LIST);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.GET, EMTPY_ARRAY_LIST);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.HANDLER, EMTPY_ARRAY_LIST);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.ATTRIBUTE, EMTPY_ARRAY_LIST);
        ((TLongObjectHashMap)m_joinPointCflowExpressionMap.get(classHash)).put(joinPointHash,
                                                                               pointcutTypeToCflowExpressionsMap);

        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
                registerMethodExecutionJoinPoint(system, declaringClass, withinInfo, joinPointHash,
                                                 pointcutTypeToAdvicesMap, pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.METHOD_CALL:
                registerMethodCallJoinPoint(system, declaringClass, withinInfo, joinPointHash,
                                            pointcutTypeToAdvicesMap, pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                registerConstructorExecutionJoinPoint(system, declaringClass, withinInfo, joinPointHash,
                                                      pointcutTypeToAdvicesMap, pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
                registerConstructorCallJoinPoint(system, declaringClass, withinInfo, joinPointHash,
                                                 pointcutTypeToAdvicesMap, pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.FIELD_SET:
                registerFieldSetJoinPoint(system, declaringClass, withinInfo, joinPointHash, pointcutTypeToAdvicesMap,
                                          pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.FIELD_GET:
                registerFieldGetJoinPoint(system, declaringClass, withinInfo, joinPointHash, pointcutTypeToAdvicesMap,
                                          pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.HANDLER:
                registerHandlerJoinPoint(system, createClassInfo(declaringClass), withinInfo, pointcutTypeToAdvicesMap,
                                         pointcutTypeToCflowExpressionsMap);
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
     * Returns the keys to the advices for the join point.
     *
     * @param classHash
     * @param joinPointHash
     * @return the advices attached to the join point
     */
    public Map getCflowExpressionsForJoinPoint(final long classHash, final long joinPointHash) {
        TLongObjectHashMap joinPoints = (TLongObjectHashMap)m_joinPointCflowExpressionMap.get(classHash);
        return (Map)joinPoints.get(joinPointHash);
    }

    /**
     * @TODO do better RW/RuW/JPredef eWorld brute force reset Needed since JoinPointRegistry is somehow a singleton
     * (static in JoinPointManager)
     */
    public void reset(final int classHash) {
        m_joinPointAdvicesMap.remove(classHash);
    }

    /**
     * Creates a class info instance out of a class instance.
     *
     * @param klass
     * @return class info
     */
    private ClassInfo createClassInfo(final Class klass) {
        ClassInfo classInfo = ClassInfoRepository.getRepository(klass.getClassLoader()).getClassInfo(klass.getName());
        if (classInfo == null) {
            classInfo = new JavaClassInfo(klass);
        }
        return classInfo;
    }

    /**
     * Register method execution join points.
     *
     * @param system
     * @param definedClass
     * @param withinInfo
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     * @param pointcutTypeToCflowExpressionsMap
     *
     */
    private void registerMethodExecutionJoinPoint(final AspectSystem system, final Class definedClass,
                                                  final ReflectionInfo withinInfo, final int joinPointHash,
                                                  final Map pointcutTypeToAdvicesMap,
                                                  final Map pointcutTypeToCflowExpressionsMap) {
        Method wrapperMethod = AspectRegistry.getMethodTuple(definedClass, joinPointHash).getWrapperMethod();
        MethodInfo methodInfo = JavaMethodInfo.getMethodInfo(wrapperMethod);

        List executionAdvices = new ArrayList();
        List executionPointcuts = new ArrayList();
        List cflowExpressions = new ArrayList();

        AspectManager[] aspectManagers = system.getAspectManagers();
        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];
            ExpressionContext ctx = new ExpressionContext(PointcutType.EXECUTION, methodInfo, null);
            executionPointcuts.addAll(aspectManager.getPointcuts(ctx));
            for (Iterator it = aspectManager.getCflowPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut)it.next();
                cflowExpressions.add(pointcut.getExpressionInfo().getCflowExpression());
            }
        }

        for (Iterator it = executionPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(pointcut.getAroundAdviceIndexes(),
                                                          pointcut.getBeforeAdviceIndexes(),
                                                          pointcut.getAfterAdviceIndexes());
            executionAdvices.add(advices);
        }

        AdviceContainer[] adviceContainers = new AdviceContainer[executionAdvices.size()];
        int i = 0;
        for (Iterator iterator = executionAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }

        pointcutTypeToAdvicesMap.put(PointcutType.EXECUTION, adviceContainers);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.EXECUTION, cflowExpressions);

        return;
    }

    /**
     * Register method call join points.
     *
     * @param system
     * @param definedClass
     * @param withinInfo
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     * @param pointcutTypeToCflowExpressionsMap
     */
    private void registerMethodCallJoinPoint(final AspectSystem system, final Class definedClass,
                                             final ReflectionInfo withinInfo, final int joinPointHash,
                                             final Map pointcutTypeToAdvicesMap,
                                             final Map pointcutTypeToCflowExpressionsMap) {
        Method wrapperMethod = AspectRegistry.getMethodTuple(definedClass, joinPointHash).getWrapperMethod();
        MethodInfo methodInfo = JavaMethodInfo.getMethodInfo(wrapperMethod);

        List methodCallAdvices = new ArrayList();
        List methodCallPointcuts = new ArrayList();
        List cflowExpressions = new ArrayList();
        AspectManager[] aspectManagers = system.getAspectManagers();

        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];
            ExpressionContext ctx = new ExpressionContext(PointcutType.CALL, methodInfo, withinInfo);
            methodCallPointcuts.addAll(aspectManager.getPointcuts(ctx));
            for (Iterator it = aspectManager.getCflowPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut)it.next();
                cflowExpressions.add(pointcut.getExpressionInfo().getCflowExpression());
            }
        }

        for (Iterator it = methodCallPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(pointcut.getAroundAdviceIndexes(),
                                                          pointcut.getBeforeAdviceIndexes(),
                                                          pointcut.getAfterAdviceIndexes());
            methodCallAdvices.add(advices);
        }

        AdviceContainer[] adviceContainers = new AdviceContainer[methodCallAdvices.size()];
        int i = 0;
        for (Iterator iterator = methodCallAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }

        pointcutTypeToAdvicesMap.put(PointcutType.CALL, adviceContainers);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.CALL, cflowExpressions);
    }

    /**
     * Register constructor execution join points.
     *
     * @param system
     * @param definedClass
     * @param withinInfo
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     * @param pointcutTypeToCflowExpressionsMap
     */
    private void registerConstructorExecutionJoinPoint(final AspectSystem system, final Class definedClass,
                                                       final ReflectionInfo withinInfo, final int joinPointHash,
                                                       final Map pointcutTypeToAdvicesMap,
                                                       final Map pointcutTypeToCflowExpressionsMap) {
        Constructor wrapperConstructor = AspectRegistry.getConstructorTuple(definedClass, joinPointHash)
                                                       .getWrapperConstructor();

        ConstructorInfo constructorInfo = JavaConstructorInfo.getConstructorInfo(wrapperConstructor);

        List executionAdvices = new ArrayList();
        List executionPointcuts = new ArrayList();
        List cflowExpressions = new ArrayList();
        AspectManager[] aspectManagers = system.getAspectManagers();

        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];
            ExpressionContext ctx = new ExpressionContext(PointcutType.EXECUTION, constructorInfo, null);
            executionPointcuts.addAll(aspectManager.getPointcuts(ctx));
            for (Iterator it = aspectManager.getCflowPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut)it.next();
                cflowExpressions.add(pointcut.getExpressionInfo().getCflowExpression());
            }
        }

        for (Iterator it = executionPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(pointcut.getAroundAdviceIndexes(),
                                                          pointcut.getBeforeAdviceIndexes(),
                                                          pointcut.getAfterAdviceIndexes());
            executionAdvices.add(advices);
        }

        AdviceContainer[] adviceContainers = new AdviceContainer[executionAdvices.size()];
        int i = 0;
        for (Iterator iterator = executionAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }

        pointcutTypeToAdvicesMap.put(PointcutType.EXECUTION, adviceContainers);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.EXECUTION, cflowExpressions);
    }

    /**
     * Register constructor call join points.
     *
     * @param system
     * @param definedClass
     * @param withinInfo
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     * @param pointcutTypeToCflowExpressionsMap
     */
    private void registerConstructorCallJoinPoint(final AspectSystem system, final Class definedClass,
                                                  final ReflectionInfo withinInfo, final int joinPointHash,
                                                  final Map pointcutTypeToAdvicesMap,
                                                  final Map pointcutTypeToCflowExpressionsMap) {
        Constructor wrapperConstructor = AspectRegistry.getConstructorTuple(definedClass, joinPointHash)
                                                       .getWrapperConstructor();

        ConstructorInfo constructorInfo = JavaConstructorInfo.getConstructorInfo(wrapperConstructor);
        List constructorCallAdvices = new ArrayList();
        List constructorCallPointcuts = new ArrayList();
        List cflowExpressions = new ArrayList();

        AspectManager[] aspectManagers = system.getAspectManagers();
        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];
            ExpressionContext ctx = new ExpressionContext(PointcutType.CALL, constructorInfo, withinInfo);
            constructorCallPointcuts.addAll(aspectManager.getPointcuts(ctx));
            for (Iterator it = aspectManager.getCflowPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut)it.next();
                cflowExpressions.add(pointcut.getExpressionInfo().getCflowExpression());
            }
        }

        for (Iterator it = constructorCallPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(pointcut.getAroundAdviceIndexes(),
                                                          pointcut.getBeforeAdviceIndexes(),
                                                          pointcut.getAfterAdviceIndexes());
            constructorCallAdvices.add(advices);
        }

        AdviceContainer[] adviceContainers = new AdviceContainer[constructorCallAdvices.size()];
        int i = 0;
        for (Iterator iterator = constructorCallAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }

        pointcutTypeToAdvicesMap.put(PointcutType.CALL, adviceContainers);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.CALL, cflowExpressions);
    }

    /**
     * Register field set join points.
     *
     * @param system
     * @param definedClass
     * @param withinInfo
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     * @param pointcutTypeToCflowExpressionsMap
     */
    private void registerFieldSetJoinPoint(final AspectSystem system, final Class definedClass,
                                           final ReflectionInfo withinInfo, final int joinPointHash,
                                           final Map pointcutTypeToAdvicesMap,
                                           final Map pointcutTypeToCflowExpressionsMap) {
        Field field = AspectRegistry.getField(definedClass, joinPointHash);

        List setAdvices = new ArrayList();
        List setPointcuts = new ArrayList();
        List cflowExpressions = new ArrayList();

        AspectManager[] aspectManagers = system.getAspectManagers();
        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];
            FieldInfo fieldInfo = JavaFieldInfo.getFieldInfo(field);
            if (fieldInfo == null) {
                new JavaClassInfo(field.getDeclaringClass());
                fieldInfo = JavaFieldInfo.getFieldInfo(field);
            }
            ExpressionContext ctx = new ExpressionContext(PointcutType.SET, fieldInfo, withinInfo);
            setPointcuts.addAll(aspectManager.getPointcuts(ctx));
            for (Iterator it = aspectManager.getCflowPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut)it.next();
                cflowExpressions.add(pointcut.getExpressionInfo().getCflowExpression());
            }
        }

        for (Iterator it = setPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(pointcut.getAroundAdviceIndexes(),
                                                          pointcut.getBeforeAdviceIndexes(),
                                                          pointcut.getAfterAdviceIndexes());
            setAdvices.add(advices);
        }

        AdviceContainer[] adviceContainers = new AdviceContainer[setAdvices.size()];
        int i = 0;
        for (Iterator iterator = setAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }

        pointcutTypeToAdvicesMap.put(PointcutType.SET, adviceContainers);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.SET, cflowExpressions);
    }

    /**
     * Register field get join points.
     *
     * @param system
     * @param definedClass
     * @param withinInfo
     * @param joinPointHash
     * @param pointcutTypeToAdvicesMap
     * @param pointcutTypeToCflowExpressionsMap
     */
    private void registerFieldGetJoinPoint(final AspectSystem system, final Class definedClass,
                                           final ReflectionInfo withinInfo, final int joinPointHash,
                                           final Map pointcutTypeToAdvicesMap,
                                           final Map pointcutTypeToCflowExpressionsMap) {
        Field field = AspectRegistry.getField(definedClass, joinPointHash);

        List getAdvices = new ArrayList();
        List getPointcuts = new ArrayList();
        List cflowExpressions = new ArrayList();

        AspectManager[] aspectManagers = system.getAspectManagers();
        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];
            FieldInfo fieldInfo = JavaFieldInfo.getFieldInfo(field);
            if (fieldInfo == null) {
                new JavaClassInfo(field.getDeclaringClass());
                fieldInfo = JavaFieldInfo.getFieldInfo(field);
            }
            ExpressionContext ctx = new ExpressionContext(PointcutType.GET, fieldInfo, withinInfo);
            getPointcuts.addAll(aspectManager.getPointcuts(ctx));
            for (Iterator it = aspectManager.getCflowPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut)it.next();
                cflowExpressions.add(pointcut.getExpressionInfo().getCflowExpression());
            }
        }

        for (Iterator it = getPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(pointcut.getAroundAdviceIndexes(),
                                                          pointcut.getBeforeAdviceIndexes(),
                                                          pointcut.getAfterAdviceIndexes());
            getAdvices.add(advices);
        }

        AdviceContainer[] adviceContainers = new AdviceContainer[getAdvices.size()];
        int i = 0;
        for (Iterator iterator = getAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }

        pointcutTypeToAdvicesMap.put(PointcutType.GET, adviceContainers);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.GET, cflowExpressions);
    }

    /**
     * Register handler join points.
     *
     * @param system
     * @param exceptionClassInfo
     * @param withinInfo
     * @param pointcutTypeToAdvicesMap
     * @param pointcutTypeToCflowExpressionsMap
     */
    private void registerHandlerJoinPoint(final AspectSystem system, final ClassInfo exceptionClassInfo,
                                          final ReflectionInfo withinInfo, final Map pointcutTypeToAdvicesMap,
                                          final Map pointcutTypeToCflowExpressionsMap) {
        List handlerAdvices = new ArrayList();
        List handlerPointcuts = new ArrayList();
        List cflowExpressions = new ArrayList();

        AspectManager[] aspectManagers = system.getAspectManagers();
        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];
            ExpressionContext ctx = new ExpressionContext(PointcutType.HANDLER, exceptionClassInfo, withinInfo);
            handlerPointcuts.addAll(aspectManager.getPointcuts(ctx));
            for (Iterator it = aspectManager.getCflowPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut)it.next();
                cflowExpressions.add(pointcut.getExpressionInfo().getCflowExpression());
            }
        }

        for (Iterator it = handlerPointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();
            AdviceContainer advices = new AdviceContainer(EMPTY_INDEX_TUPLE_ARRAY, pointcut.getBeforeAdviceIndexes(),
                                                          EMPTY_INDEX_TUPLE_ARRAY);
            handlerAdvices.add(advices);
        }

        AdviceContainer[] adviceContainers = new AdviceContainer[handlerAdvices.size()];
        int i = 0;
        for (Iterator iterator = handlerAdvices.iterator(); iterator.hasNext(); i++) {
            AdviceContainer adviceContainer = (AdviceContainer)iterator.next();
            adviceContainers[i] = adviceContainer;
        }

        pointcutTypeToAdvicesMap.put(PointcutType.HANDLER, adviceContainers);
        pointcutTypeToCflowExpressionsMap.put(PointcutType.HANDLER, cflowExpressions);
    }
}

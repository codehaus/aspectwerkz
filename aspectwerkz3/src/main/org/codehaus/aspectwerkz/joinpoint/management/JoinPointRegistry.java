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
        // set up advice repository
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

        // set up cflow expression repository
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
                Method wrapperMethod = AspectRegistry.getMethodTuple(declaringClass, joinPointHash).getWrapperMethod();
                MethodInfo methodInfo = JavaMethodInfo.getMethodInfo(wrapperMethod);
                registerJoinPoint(PointcutType.EXECUTION, system, methodInfo, withinInfo, pointcutTypeToAdvicesMap,
                                  pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.METHOD_CALL:
                wrapperMethod = AspectRegistry.getMethodTuple(declaringClass, joinPointHash).getWrapperMethod();
                methodInfo = JavaMethodInfo.getMethodInfo(wrapperMethod);
                registerJoinPoint(PointcutType.CALL, system, methodInfo, withinInfo, pointcutTypeToAdvicesMap,
                                  pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                Constructor wrapperConstructor = AspectRegistry.getConstructorTuple(declaringClass, joinPointHash)
                                                               .getWrapperConstructor();
                ConstructorInfo constructorInfo = JavaConstructorInfo.getConstructorInfo(wrapperConstructor);
                registerJoinPoint(PointcutType.EXECUTION, system, constructorInfo, withinInfo,
                                  pointcutTypeToAdvicesMap, pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
                wrapperConstructor = AspectRegistry.getConstructorTuple(declaringClass, joinPointHash)
                                                   .getWrapperConstructor();
                constructorInfo = JavaConstructorInfo.getConstructorInfo(wrapperConstructor);
                registerJoinPoint(PointcutType.CALL, system, constructorInfo, withinInfo, pointcutTypeToAdvicesMap,
                                  pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.FIELD_SET:
                FieldInfo fieldInfo = JavaFieldInfo.getFieldInfo(AspectRegistry.getField(declaringClass, joinPointHash));
                registerJoinPoint(PointcutType.SET, system, fieldInfo, withinInfo, pointcutTypeToAdvicesMap,
                                  pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.FIELD_GET:
                fieldInfo = JavaFieldInfo.getFieldInfo(AspectRegistry.getField(declaringClass, joinPointHash));
                registerJoinPoint(PointcutType.GET, system, fieldInfo, withinInfo, pointcutTypeToAdvicesMap,
                                  pointcutTypeToCflowExpressionsMap);
                break;
            case JoinPointType.HANDLER:
                registerJoinPoint(PointcutType.HANDLER, system, createClassInfo(declaringClass), withinInfo,
                                  pointcutTypeToAdvicesMap, pointcutTypeToCflowExpressionsMap);
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
     * Resets the registry.
     * @TODO do better RW/RuW/JPredef eWorld brute force reset Needed since JoinPointRegistry is somehow a singleton (static in JoinPointManager)
     *
     * @param classHash
     */
    public void reset(final int classHash) {
        m_joinPointAdvicesMap.remove(classHash);
        m_joinPointCflowExpressionMap.remove(classHash);
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
     * Register field get join points.
     *
     * @param type
     * @param system
     * @param reflectInfo
     * @param withinInfo
     * @param pointcutTypeToAdvicesMap
     * @param pointcutTypeToCflowExpressionsMap
     */
    private void registerJoinPoint(final PointcutType type, final AspectSystem system,
                                   final ReflectionInfo reflectInfo, final ReflectionInfo withinInfo,
                                   final Map pointcutTypeToAdvicesMap, final Map pointcutTypeToCflowExpressionsMap) {
        List getAdvices = new ArrayList();
        List getPointcuts = new ArrayList();
        List cflowExpressions = new ArrayList();

        AspectManager[] aspectManagers = system.getAspectManagers();
        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];
            ExpressionContext ctx = new ExpressionContext(type, reflectInfo, withinInfo);
            getPointcuts.addAll(aspectManager.getPointcuts(ctx));
            List pointcuts = aspectManager.getCflowPointcuts(ctx);
            for (Iterator it = pointcuts.iterator(); it.hasNext();) {
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

        pointcutTypeToAdvicesMap.put(type, adviceContainers);
        pointcutTypeToCflowExpressionsMap.put(type, cflowExpressions);
    }
}

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
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfoRepository;
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
    private static final TLongObjectHashMap m_joinPointMetaDataMap = new TLongObjectHashMap();

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
    public void registerJoinPoint(
            final int joinPointType, final int joinPointHash, final String signature,
            final int classHash, final Class declaringClass, final ReflectionInfo withinInfo,
            final AspectSystem system) {
        if (!m_joinPointMetaDataMap.containsKey(classHash)) {
            m_joinPointMetaDataMap.put(classHash, new TLongObjectHashMap());
        }
        Map joinPointMetaDataMap = new HashMap();
        joinPointMetaDataMap.put(PointcutType.EXECUTION, EMTPY_ARRAY_LIST);
        joinPointMetaDataMap.put(PointcutType.CALL, EMTPY_ARRAY_LIST);
        joinPointMetaDataMap.put(PointcutType.SET, EMTPY_ARRAY_LIST);
        joinPointMetaDataMap.put(PointcutType.GET, EMTPY_ARRAY_LIST);
        joinPointMetaDataMap.put(PointcutType.HANDLER, EMTPY_ARRAY_LIST);
        joinPointMetaDataMap.put(PointcutType.STATIC_INITIALIZATION, EMTPY_ARRAY_LIST);
        joinPointMetaDataMap.put(PointcutType.ATTRIBUTE, EMTPY_ARRAY_LIST);
        ((TLongObjectHashMap)m_joinPointMetaDataMap.get(classHash)).put(joinPointHash, joinPointMetaDataMap);
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
                Method wrapperMethod = AspectRegistry.getMethodTuple(declaringClass, joinPointHash).getWrapperMethod();
                MethodInfo methodInfo = JavaMethodInfo.getMethodInfo(wrapperMethod);
                registerJoinPoint(PointcutType.EXECUTION, system, methodInfo, withinInfo, joinPointMetaDataMap);
                break;
            case JoinPointType.METHOD_CALL:
                wrapperMethod = AspectRegistry.getMethodTuple(declaringClass, joinPointHash).getWrapperMethod();
                methodInfo = JavaMethodInfo.getMethodInfo(wrapperMethod);
                registerJoinPoint(PointcutType.CALL, system, methodInfo, withinInfo, joinPointMetaDataMap);
                break;
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                Constructor wrapperConstructor = AspectRegistry.getConstructorTuple(declaringClass, joinPointHash)
                        .getWrapperConstructor();
                ConstructorInfo constructorInfo = JavaConstructorInfo.getConstructorInfo(wrapperConstructor);
                registerJoinPoint(PointcutType.EXECUTION, system, constructorInfo, withinInfo, joinPointMetaDataMap);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
                wrapperConstructor = AspectRegistry.getConstructorTuple(declaringClass, joinPointHash)
                        .getWrapperConstructor();
                constructorInfo = JavaConstructorInfo.getConstructorInfo(wrapperConstructor);
                registerJoinPoint(PointcutType.CALL, system, constructorInfo, withinInfo, joinPointMetaDataMap);
                break;
            case JoinPointType.FIELD_SET:
                FieldInfo fieldInfo = JavaFieldInfo.getFieldInfo(
                        AspectRegistry.getField(declaringClass, joinPointHash)
                );
                registerJoinPoint(PointcutType.SET, system, fieldInfo, withinInfo, joinPointMetaDataMap);
                break;
            case JoinPointType.FIELD_GET:
                fieldInfo = JavaFieldInfo.getFieldInfo(AspectRegistry.getField(declaringClass, joinPointHash));
                registerJoinPoint(PointcutType.GET, system, fieldInfo, withinInfo, joinPointMetaDataMap);
                break;
            case JoinPointType.HANDLER:
                registerJoinPoint(
                        PointcutType.HANDLER, system, createClassInfo(declaringClass), withinInfo,
                        joinPointMetaDataMap
                );
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
    public Map getJoinPointMetaData(final long classHash, final long joinPointHash) {
        TLongObjectHashMap joinPoints = (TLongObjectHashMap)m_joinPointMetaDataMap.get(classHash);
        return (Map)joinPoints.get(joinPointHash);
    }

    /**
     * Returns the keys to the advices for the join point.
     *
     * @param classHash
     * @param joinPointHash
     * @return the advices attached to the join point
     */
    public Map getCflowPointcutsForJoinPoint(final long classHash, final long joinPointHash) {
        TLongObjectHashMap joinPoints = (TLongObjectHashMap)m_joinPointCflowExpressionMap.get(classHash);
        return (Map)joinPoints.get(joinPointHash);
    }

    /**
     * Resets the registry.
     *
     * @param classHash
     * @TODO do better RW/RuW/JPredef eWorld brute force reset Needed since JoinPointRegistry is somehow a singleton
     * (static in JoinPointManager)
     */
    public void reset(final int classHash) {
        m_joinPointMetaDataMap.remove(classHash);
        m_joinPointCflowExpressionMap.remove(classHash);
    }

    /**
     * Creates a class info instance out of a class instance.
     *
     * @param klass
     * @return class info
     */
    private ClassInfo createClassInfo(final Class klass) {
        ClassInfo classInfo = JavaClassInfoRepository.getRepository(klass.getClassLoader()).getClassInfo(
                klass.getName()
        );
        if (classInfo == null) {
            classInfo = JavaClassInfo.getClassInfo(klass);
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
     * @param joinPointMetaDataMap
     */
    private void registerJoinPoint(
            final PointcutType type, final AspectSystem system,
            final ReflectionInfo reflectInfo, final ReflectionInfo withinInfo,
            final Map joinPointMetaDataMap) {
        List adviceIndexInfoList = new ArrayList();
        List cflowExpressionList = new ArrayList();
        Pointcut cflowPointcut = null;
        ExpressionContext ctx = new ExpressionContext(type, reflectInfo, withinInfo);
        AspectManager[] aspectManagers = system.getAspectManagers();
        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];

            /// grab the first one found, one single cflow pointcut is enough per join point
            if (cflowPointcut == null) {
                List cflowPointcuts = aspectManager.getCflowPointcuts(ctx);
                if (!cflowPointcuts.isEmpty()) {
                    cflowPointcut = (Pointcut)cflowPointcuts.get(0);
                }
            }

            // get all matching pointcuts from all managers
            for (Iterator it = aspectManager.getPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut)it.next();
                AdviceIndexInfo adviceIndexInfo = new AdviceIndexInfo(
                        pointcut.getAroundAdviceIndexes(),
                        pointcut.getBeforeAdviceIndexes(),
                        pointcut.getAfterAdviceIndexes()
                );
                adviceIndexInfoList.add(adviceIndexInfo);

                // collect the cflow expressions for the matching pointcuts (if they have one)
                if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                    cflowExpressionList.add(pointcut.getExpressionInfo().getCflowExpressionRuntime());
                }
            }
        }

        // turn the lists into arrays for performance reasons
        AdviceIndexInfo[] adviceIndexInfo = new AdviceIndexInfo[adviceIndexInfoList.size()];
        int i = 0;
        for (Iterator iterator = adviceIndexInfoList.iterator(); iterator.hasNext(); i++) {
            adviceIndexInfo[i] = (AdviceIndexInfo)iterator.next();
        }
        JoinPointMetaData metaData = new JoinPointMetaData();
        metaData.adviceIndexes = adviceIndexInfo;
        metaData.cflowExpressions = cflowExpressionList;
        metaData.cflowPointcut = cflowPointcut;
        metaData.expressionContext = ctx;
        joinPointMetaDataMap.put(type, metaData);
    }
}

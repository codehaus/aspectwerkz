/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.transform.inlining.JoinPointCompiler;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.AdviceInfo;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;
import org.codehaus.aspectwerkz.aspect.management.Aspects;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Manages the join point compilation, loading and instantiation for the target classes.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class JoinPointManager {

    /**
     * Ensures that the specific joinPoint class for the given target class and joinPoint info is generated. This call
     * is added to the weaved class as a "clinit" block
     *
     * @param joinPointType
     * @param callerClass
     * @param callerMethodName
     * @param callerMethodDesc
     * @param callerMethodModifiers
     * @param calleeClassName
     * @param calleeMemberName
     * @param calleeMemberDesc
     * @param calleeMemberModifiers
     * @param joinPointSequence
     * @param joinPointHash
     * @param joinPointClassName
     */
    public static void loadJoinPoint(
            final int joinPointType,
            final Class callerClass,
            final String callerMethodName,
            final String callerMethodDesc,
            final int callerMethodModifiers,
            final String calleeClassName,
            final String calleeMemberName,
            final String calleeMemberDesc,
            final int calleeMemberModifiers,
            final int joinPointSequence,
            final int joinPointHash,
            final String joinPointClassName) {

        Class calleeClass = null;
        try {
            if (calleeClassName != null) {
                calleeClass = callerClass.getClassLoader().loadClass(calleeClassName.replace('/', '.'));
            }
        } catch (ClassNotFoundException calleeNotFound) {
            throw new RuntimeException(
                    "callee class [" + calleeClassName + "] can not be found in class loader [" +
                    callerClass.getClassLoader() +
                    "]"
            );
        }

        // check if the JP is already loaded
        // this can occurs if user packaged its JIT classes, or if we are using multiweaving
        boolean generateJoinPoint = false;
        try {
            if (calleeClass == null) {
                throw new RuntimeException("callee class [" + calleeClassName + "] is NULL");
            }
            calleeClass.getClassLoader().loadClass(joinPointClassName.replace('/', '.'));
        } catch (ClassNotFoundException e) {
            generateJoinPoint = true;
        }
        if (!generateJoinPoint) {
            return;
        }


        // FIXME XXX use one 
        Aspects.initialize();
        SystemLoader.getSystem(calleeClass.getClassLoader()).initialize();

        ClassInfo calleeClassInfo = JavaClassInfo.getClassInfo(calleeClass);
        ReflectionInfo reflectionInfo = null;

        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
                reflectionInfo = calleeClassInfo.getMethod(joinPointHash);
                doLoadJoinPoint(
                        joinPointClassName,
                        JoinPointType.METHOD_EXECUTION,
                        PointcutType.EXECUTION,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMethodModifiers,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        reflectionInfo,
                        calleeClassInfo
                );
                break;

            case JoinPointType.METHOD_CALL:
                reflectionInfo = calleeClassInfo.getMethod(joinPointHash);
                doLoadJoinPoint(
                        joinPointClassName,
                        JoinPointType.METHOD_CALL,
                        PointcutType.CALL,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMethodModifiers,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        reflectionInfo,
                        calleeClassInfo
                );
                break;
            case JoinPointType.FIELD_GET:
                reflectionInfo = calleeClassInfo.getField(joinPointHash);
                doLoadJoinPoint(
                        joinPointClassName,
                        JoinPointType.FIELD_GET,
                        PointcutType.GET,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMethodModifiers,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        reflectionInfo,
                        calleeClassInfo
                );
                break;

            case JoinPointType.FIELD_SET:
                reflectionInfo = calleeClassInfo.getField(joinPointHash);
                doLoadJoinPoint(
                        joinPointClassName,
                        JoinPointType.FIELD_SET,
                        PointcutType.SET,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMethodModifiers,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        reflectionInfo,
                        calleeClassInfo
                );
                break;

            case JoinPointType.CONSTRUCTOR_EXECUTION:
                reflectionInfo = calleeClassInfo.getConstructor(joinPointHash);
                doLoadJoinPoint(
                        joinPointClassName,
                        JoinPointType.CONSTRUCTOR_EXECUTION,
                        PointcutType.EXECUTION,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMethodModifiers,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        reflectionInfo,
                        calleeClassInfo
                );
                break;

            case JoinPointType.CONSTRUCTOR_CALL:
                reflectionInfo = calleeClassInfo.getConstructor(joinPointHash);
                doLoadJoinPoint(
                        joinPointClassName,
                        JoinPointType.CONSTRUCTOR_CALL,
                        PointcutType.CALL,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMethodModifiers,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        reflectionInfo,
                        calleeClassInfo
                );
                break;

            case JoinPointType.HANDLER:
                // FIXME wrong reflection info
                reflectionInfo = calleeClassInfo.getMethod(joinPointHash);
                doLoadJoinPoint(
                        joinPointClassName,
                        JoinPointType.HANDLER,
                        PointcutType.HANDLER,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMethodModifiers,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        reflectionInfo,
                        calleeClassInfo
                );
                break;

            case JoinPointType.STATIC_INITALIZATION:
                throw new UnsupportedOperationException(
                        "join point type handling is not implemented: " + joinPointType
                );
        }
    }

    /**
     * Loads the join point.
     *
     * @param joinPointClassName
     * @param joinPointType
     * @param pointcutType
     * @param callerClass
     * @param callerMethodName
     * @param callerMethodDesc
     * @param callerMethodModifiers
     * @param calleeClass
     * @param calleeMemberName
     * @param calleeMemberDesc
     * @param calleeMemberModifiers
     * @param joinPointSequence
     * @param joinPointHash
     * @param reflectionInfo
     * @param thisClassInfo
     */
    private static void doLoadJoinPoint(
            final String joinPointClassName,
            final int joinPointType,
            final PointcutType pointcutType,
            final Class callerClass,
            final String callerMethodName,
            final String callerMethodDesc,
            final int callerMethodModifiers,
            final Class calleeClass,
            final String calleeMemberName,
            final String calleeMemberDesc,
            final int calleeMemberModifiers,
            final int joinPointSequence,
            final int joinPointHash,
            final ReflectionInfo reflectionInfo,
            final ClassInfo thisClassInfo) {

        ClassInfo callerClassInfo = JavaClassInfo.getClassInfo(callerClass);
        ReflectionInfo withinInfo = null;
        // FIXME: refactor getMethod in INFO so that we can apply it on "<init>" and that it delegates to ctor
        // instead of checking things here.
        switch (joinPointType) {
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                withinInfo = callerClassInfo.getConstructor(AsmHelper.calculateConstructorHash(callerMethodDesc));
                break;
            default:
                withinInfo = callerClassInfo.getMethod(
                        AsmHelper.calculateMethodHash(callerMethodName, callerMethodDesc)
                );
        }

        AdviceInfoStruct[] adviceInfos = getAdviceInfosForJoinPoint(pointcutType, reflectionInfo, withinInfo);

        Class clazz = JoinPointCompiler.loadJoinPoint(
                joinPointClassName,
                joinPointType,
                joinPointHash,

                callerClass.getName(),
                callerMethodName,
                callerMethodDesc,
                callerMethodModifiers,

                calleeClass.getName(),
                calleeMemberName,
                calleeMemberDesc,
                calleeMemberModifiers,

                adviceInfos,
                calleeClass.getClassLoader(),
                joinPointSequence
        );
    }

    /**
     * Retrieves the join point metadata.
     *
     * @param type
     * @param reflectInfo
     * @param withinInfo
     */
    public static AdviceInfoStruct[] getAdviceInfosForJoinPoint(
            final PointcutType type,
            final ReflectionInfo reflectInfo,
            final ReflectionInfo withinInfo) {

        List adviceIndexInfoList = new ArrayList();
        List cflowExpressionList = new ArrayList();
        Pointcut cflowPointcut = null;

        // FIXME XXX handle cflow

        ExpressionContext exprCtx = new ExpressionContext(type, reflectInfo, withinInfo);

        final List pointcuts = Aspects.getPointcuts(exprCtx);

        // get all matching pointcuts from all managers
        for (Iterator it = pointcuts.iterator(); it.hasNext();) {
            Pointcut pointcut = (Pointcut)it.next();

            List aroundAdviceInfos = pointcut.getAroundAdviceInfos();
            List beforeAdviceInfos = pointcut.getBeforeAdviceInfos();
            List afterFinallyAdviceInfos = pointcut.getAfterFinallyAdviceInfos();
            List afterReturningAdviceInfos = pointcut.getAfterReturningAdviceInfos();
            List afterThrowingAdviceInfos = pointcut.getAfterThrowingAdviceInfos();

            AdviceInfoStruct adviceInfoStruct = new AdviceInfoStruct(
                    aroundAdviceInfos,
                    beforeAdviceInfos,
                    afterFinallyAdviceInfos,
                    afterReturningAdviceInfos,
                    afterThrowingAdviceInfos
            );

            // compute target args to advice args mapping, it is a property of each *advice*

            // refresh the arg index map
            pointcut.getExpressionInfo().getArgsIndexMapper().match(exprCtx);

            //TODO can we do cache, can we do in another visitor
            //TODO skip map when no args()

            for (Iterator adviceInfos = aroundAdviceInfos.iterator(); adviceInfos.hasNext();) {
                AdviceInfo adviceInfo = (AdviceInfo)adviceInfos.next();
                setMethodArgumentIndexes(pointcut, exprCtx, adviceInfo);
            }
            for (Iterator adviceInfos = beforeAdviceInfos.iterator(); adviceInfos.hasNext();) {
                setMethodArgumentIndexes(pointcut, exprCtx, (AdviceInfo)adviceInfos.next());
            }
            for (Iterator adviceInfos = afterFinallyAdviceInfos.iterator(); adviceInfos.hasNext();) {
                setMethodArgumentIndexes(pointcut, exprCtx, (AdviceInfo)adviceInfos.next());
            }
            for (Iterator adviceInfos = afterReturningAdviceInfos.iterator(); adviceInfos.hasNext();) {
                setMethodArgumentIndexes(pointcut, exprCtx, (AdviceInfo)adviceInfos.next());
            }
            for (Iterator adviceInfos = afterThrowingAdviceInfos.iterator(); adviceInfos.hasNext();) {
                setMethodArgumentIndexes(pointcut, exprCtx, (AdviceInfo)adviceInfos.next());
            }

            adviceIndexInfoList.add(adviceInfoStruct);

            // collect the cflow expressions for the matching pointcuts (if they have one)
            if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                cflowExpressionList.add(pointcut.getExpressionInfo().getCflowExpressionRuntime());
            }
        }

        // turn the lists into arrays for performance reasons
        AdviceInfoStruct[] adviceIndexInfo = new AdviceInfoStruct[adviceIndexInfoList.size()];
        int i = 0;
        for (Iterator iterator = adviceIndexInfoList.iterator(); iterator.hasNext(); i++) {
            adviceIndexInfo[i] = (AdviceInfoStruct)iterator.next();
        }
        return adviceIndexInfo;
    }

    /**
     * Get the parameter names from a "method declaration" signature like pc(type a, type2 b) => 0:a, 1:b
     *
     * @param expression
     * @return the parameter names
     */
    public static String[] getParameterNames(final String expression) {
        int paren = expression.indexOf('(');
        List paramNames = new ArrayList();
        if (paren > 0) {
            String params = expression.substring(paren + 1, expression.lastIndexOf(')')).trim();
            String[] javaParameters = Strings.splitString(params, ",");
            for (int i = 0; i < javaParameters.length; i++) {
                String javaParameter = Strings.replaceSubString(javaParameters[i], "  ", " ").trim();
                String[] paramInfo = Strings.splitString(javaParameter, " ");
                paramNames.add(paramInfo[1]);
            }
        }
        String[] paramNamesArray = new String[paramNames.size()];
        int index = 0;
        for (Iterator it = paramNames.iterator(); it.hasNext(); index++) {
            paramNamesArray[index] = (String)it.next();
        }
        return paramNamesArray;
    }

    /**
     * Sets the method argument indexes.
     *
     * @param pointcut
     * @param ctx
     * @param adviceInfo
     */
    private static void setMethodArgumentIndexes(
            final Pointcut pointcut,
            final ExpressionContext ctx,
            final AdviceInfo adviceInfo) {

        // grab the parameters names
        String[] adviceArgNames = getParameterNames(adviceInfo.getName());

        // map them from the ctx info
        int[] adviceToTargetArgs = new int[adviceArgNames.length];
        for (int k = 0; k < adviceArgNames.length; k++) {
            String adviceArgName = adviceArgNames[k];
            int exprArgIndex = pointcut.getExpressionInfo().getArgumentIndex(adviceArgName);
            if (exprArgIndex >= 0 && ctx.m_exprIndexToTargetIndex.containsKey(exprArgIndex)) {
                adviceToTargetArgs[k] = ctx.m_exprIndexToTargetIndex.get(exprArgIndex);
            } else {
                adviceToTargetArgs[k] = -1;
            }
        }
        adviceInfo.setMethodToArgIndexes(adviceToTargetArgs);
    }
}
/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.AdviceInfo;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds and creates meta data about a specific join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class JoinPointMetaData {
    /**
     * The indexes for the advices.
     */
    public AdviceIndexInfo[] adviceIndexes;

    /**
     * The cflow expressions runtime.
     */
    public List cflowExpressions;

    /**
     * The cflow pointcut.
     */
    public Pointcut cflowPointcut;

    /**
     * The join point expression context
     */
    public ExpressionContext expressionContext;

    /**
     * Retrieves the join point metadata.
     *
     * @param type
     * @param system
     * @param reflectInfo
     * @param withinInfo
     */
    public static JoinPointMetaData getJoinPointMetaData(final PointcutType type,
                                                         final AspectSystem system,
                                                         final ReflectionInfo reflectInfo,
                                                         final ReflectionInfo withinInfo) {

        List adviceIndexInfoList = new ArrayList();
        List cflowExpressionList = new ArrayList();
        Pointcut cflowPointcut = null;

        ExpressionContext ctx = new ExpressionContext(type, reflectInfo, withinInfo);//AVAJ null?
        AspectManager[] aspectManagers = system.getAspectManagers();
        for (int i = 0; i < aspectManagers.length; i++) {
            AspectManager aspectManager = aspectManagers[i];

            /// grab the first one found, one single cflow pointcut is enough per join point
            if (cflowPointcut == null) {
                List cflowPointcuts = aspectManager.getCflowPointcuts(ctx);
                if (!cflowPointcuts.isEmpty()) {
                    cflowPointcut = (Pointcut) cflowPointcuts.get(0);
                }
            }

            // get all matching pointcuts from all managers
            for (Iterator it = aspectManager.getPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut) it.next();

                AdviceInfo[] aroundAdviceIndexes = pointcut.getAroundAdviceIndexes();
                AdviceInfo[] beforeAdviceIndexes = pointcut.getBeforeAdviceIndexes();
                AdviceInfo[] afterFinallyAdviceIndexes = pointcut.getAfterFinallyAdviceIndexes();
                AdviceInfo[] afterReturningAdviceIndexes = pointcut.getAfterReturningAdviceIndexes();
                AdviceInfo[] afterThrowingAdviceIndexes = pointcut.getAfterThrowingAdviceIndexes();

                AdviceIndexInfo adviceIndexInfo = new AdviceIndexInfo(
                        aroundAdviceIndexes,
                        beforeAdviceIndexes,
                        afterFinallyAdviceIndexes,
                        afterReturningAdviceIndexes,
                        afterThrowingAdviceIndexes
                );

                // compute target args to advice args mapping, it is a property of each *advice*

                // refresh the arg index map
                pointcut.getExpressionInfo().getArgsIndexMapper().match(ctx);

                //TODO can we do cache, can we do in another visitor
                //TODO skip map when no args()

                for (int j = 0; j < aroundAdviceIndexes.length; j++) {
                    setMethodArgumentIndexes(pointcut.getAroundAdviceName(j), pointcut, ctx, aroundAdviceIndexes[j]);
                }

                for (int j = 0; j < beforeAdviceIndexes.length; j++) {
                    setMethodArgumentIndexes(pointcut.getBeforeAdviceName(j), pointcut, ctx, beforeAdviceIndexes[j]);
                }

                for (int j = 0; j < afterFinallyAdviceIndexes.length; j++) {
                    setMethodArgumentIndexes(
                            pointcut.getAfterFinallyAdviceName(j), pointcut, ctx, afterFinallyAdviceIndexes[j]
                    );
                }

                for (int j = 0; j < afterReturningAdviceIndexes.length; j++) {
                    setMethodArgumentIndexes(
                            pointcut.getAfterReturningAdviceName(j), pointcut, ctx, afterReturningAdviceIndexes[j]
                    );
                }

                for (int j = 0; j < afterThrowingAdviceIndexes.length; j++) {
                    setMethodArgumentIndexes(
                            pointcut.getAfterThrowingAdviceName(j), pointcut, ctx, afterThrowingAdviceIndexes[j]
                    );

                }

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
            adviceIndexInfo[i] = (AdviceIndexInfo) iterator.next();
        }
        JoinPointMetaData metaData = new JoinPointMetaData();
        metaData.adviceIndexes = adviceIndexInfo;
        metaData.cflowExpressions = cflowExpressionList;
        metaData.cflowPointcut = cflowPointcut;
        metaData.expressionContext = ctx;
        return metaData;
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
            paramNamesArray[index] = (String) it.next();
        }
        return paramNamesArray;
    }

    /**
     * Sets the method argument indexes.
     * 
     * @param adviceName
     * @param pointcut
     * @param ctx
     * @param indexTuple
     */
    private static void setMethodArgumentIndexes(final String adviceName,
                                                 final Pointcut pointcut,
                                                 final ExpressionContext ctx,
                                                 final AdviceInfo indexTuple) {
        //grab the parameters names
        String[] adviceArgNames = JoinPointMetaData.getParameterNames(adviceName);

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
        indexTuple.setMethodToArgIndexes(adviceToTargetArgs);
    }

}
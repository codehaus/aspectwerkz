/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.IndexTuple;
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
 * FIXME: a bunch of system.out and public field must be refactor for args() support at weave time.
 * Lack of time - time for some rest - do a grep on "XXXARGS"
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
    public static JoinPointMetaData getJoinPointMetaData(
        final PointcutType type,
        final AspectSystem system,
        final ReflectionInfo reflectInfo,
        final ReflectionInfo withinInfo) {
        
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
                    cflowPointcut = (Pointcut) cflowPointcuts.get(0);
                }
            }
    
            // get all matching pointcuts from all managers
            for (Iterator it = aspectManager.getPointcuts(ctx).iterator(); it.hasNext();) {
                Pointcut pointcut = (Pointcut) it.next();
                AdviceIndexInfo adviceIndexInfo = new AdviceIndexInfo(pointcut
                        .getAroundAdviceIndexes(), pointcut.getBeforeAdviceIndexes(), pointcut
                        .getAfterAdviceIndexes());
                // compute target args to advice args mapping
                // it is a property of each *advice*
                System.out.println("XXXARGS JoinPointMetaData.getJoinPointMetaData " + pointcut.getExpressionInfo().getExpressionAsString());
                System.out.println("XXXARGS JoinPointMetaData.getJoinPointMetaData " + pointcut.getBeforeAdviceIndexes().length);

                //TODO can we do cache, can we do in another visitor
                //TODO skip map when no args()
                for (int j = 0; j < pointcut.getBeforeAdviceIndexes().length; j++) {
                    IndexTuple indexTuple = pointcut.getBeforeAdviceIndexes()[j];
                    String adviceName = pointcut.getBeforeAdviceName(j);
                    System.out.println("  XXXARGS " + adviceName);
                    //grab the parameters names
                    String[] adviceArgNames = JoinPointMetaData.getParameterNames(adviceName);

                    // map them from the ctx info
                    // ctx has pcIndex -> targetIndex mapping and has pcIndex <--> argName
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
                    System.out.println("    mapped " + indexTuple);
                    //debug:
                    for (int k = 0; k < adviceToTargetArgs.length; k++) {
                        int adviceToTargetArg = adviceToTargetArgs[k];
                        System.out.println("      " + k + " -> " + adviceToTargetArg);
                    }
                    indexTuple.m_methodToArgIndexes = adviceToTargetArgs;
                }

                //FIXME: do the same for after and around !

                adviceIndexInfoList.add(adviceIndexInfo);
    
                // collect the cflow expressions for the matching pointcuts (if they have one)
                if (pointcut.getExpressionInfo().hasCflowPointcut()) {
                    cflowExpressionList.add(pointcut.getExpressionInfo()
                            .getCflowExpressionRuntime());
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
     * @return
     */
    private static String[] getParameterNames(String expression) {
        //TODO - refactor out of this class - used in JPMetaData for inlining
        int paren = expression.indexOf('(');
        List paramNames = new ArrayList();
        if (paren > 0) {
            String params = expression.substring(paren+1, expression.lastIndexOf(')')).trim();
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

}
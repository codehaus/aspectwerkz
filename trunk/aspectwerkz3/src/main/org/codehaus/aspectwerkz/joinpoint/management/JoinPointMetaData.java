/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.AspectSystem;
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
}
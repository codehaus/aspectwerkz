/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.*;
import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.aspect.CFlowSystemAspect;
import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;
import org.codehaus.aspectwerkz.aspect.management.PointcutManager;
import org.codehaus.aspectwerkz.aspect.management.Aspects;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @TODO consider moving all these methods to the AspectRegistry and delete the class
 * <p/>
 * Manages the startup procedure, walks through the definition and instantiates the
 * aspects/advices/introduction/pointcuts. <p/>Reads the definition, either as a class of as an XML file. <p/>To use
 * your XML definition file pass <code>-Daspectwerkz.definition.file=PathToFile</code> as parameter to the JVM. <p/>If
 * the above given parameter is not specified, the <code>StartupManager</code> tries locate a file called
 * <code>aspectwerkz.xml</code> in the classpath and if this fails the last attempt is to use the
 * <code>ASPECTWERKZ_HOME/config/aspectwerkz.xml</code> file (if there is one).
 */
public class StartupManager {
    /**
     * The default aspect container class.
     */
    public static final String DEFAULT_ASPECT_CONTAINER = "org.codehaus.aspectwerkz.aspect.DefaultAspectContainerStrategy";

    /**
     * Private constructor to prevent instantiability.
     */
    private StartupManager() {
    }

    /**
     * Loads the system definition.
     *
     * @param aspectManager the aspectManager for the system
     * @param definition    the definition for the system
     */
//    public static void initializeSystem(final SystemDefinition definition) {
//        Aspects.register(definition);
//    }

    /**
     * FIXME XXX handle cflow
     *
     * Registers the cflow pointcuts.
     *
     * @param aspectManager the aspectManager for the system
     * @param definition    the AspectWerkz definition
     */
//    private static void registerCflowPointcuts(final AspectManager aspectManager, final SystemDefinition definition) {
//        // get all aspects to be able to get all poincuts defined
//        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
//            AspectDefinition aspectDef = (AspectDefinition)it1.next();
//            PointcutManager pointcutManager = aspectManager.getPointcutManager(aspectDef.getName());
//            List cflowPointcuts = pointcutManager.getCflowPointcuts();
//            for (Iterator it2 = cflowPointcuts.iterator(); it2.hasNext();) {
//                Pointcut m_cflowPointcut = (Pointcut)it2.next();
//                ExpressionInfo expressionInfo = m_cflowPointcut.getExpressionInfo();
//
//                // register the cflow advices in the system and create the cflow system
//                // pointcutManager
//                // (if it does not already exist)
//                if (!aspectManager.hasAspect(CFlowSystemAspect.NAME)) {
//                    AspectDefinition cflowAspectDef = new AspectDefinition(
//                            CFlowSystemAspect.NAME,
//                            CFlowSystemAspect.CLASS_NAME,
//                            aspectManager.getUuid()
//                    );
//                    PointcutDefinition pointcutDef = new PointcutDefinition(expressionInfo.asString());
//                    cflowAspectDef.setDeploymentModel(CFlowSystemAspect.DEPLOYMENT_MODEL);
//                    cflowAspectDef.addPointcutDefinition(pointcutDef);
//                    try {
//                        AdviceDefinition beforeAdviceDefinition = new AdviceDefinition(
//                                CFlowSystemAspect.PRE_ADVICE,
//                                AdviceType.BEFORE,
//                                null,
//                                cflowAspectDef.getName(),
//                                cflowAspectDef.getClassName(),
//                                expressionInfo,
//                                CFlowSystemAspect.class.getDeclaredMethod(
//                                        CFlowSystemAspect.PRE_ADVICE, new Class[]{
//                                            JoinPoint.class
//                                        }
//                                ),
//                                CFlowSystemAspect.PRE_ADVICE_INDEX,
//                                cflowAspectDef
//                        );
//                        cflowAspectDef.addBeforeAdviceDefinition(beforeAdviceDefinition);
//                        AdviceDefinition afterAdviceDefinition = new AdviceDefinition(
//                                CFlowSystemAspect.POST_ADVICE,
//                                AdviceType.AFTER_FINALLY,
//                                null,
//                                cflowAspectDef.getName(),
//                                cflowAspectDef.getClassName(),
//                                expressionInfo,
//                                CFlowSystemAspect.class.getDeclaredMethod(
//                                        CFlowSystemAspect.POST_ADVICE, new Class[]{
//                                            JoinPoint.class
//                                        }
//                                ),
//                                CFlowSystemAspect.POST_ADVICE_INDEX,
//                                cflowAspectDef
//                        );
//                        cflowAspectDef.addAfterAdviceDefinition(afterAdviceDefinition);
//                    } catch (NoSuchMethodException e) {
//                        ; // TODO: why ignore exception? ALEX??
//                    }
//                    definition.addAspect(cflowAspectDef);
//                    registerAspect(aspectManager, cflowAspectDef, new HashMap());
//                }
//                m_cflowPointcut.addBeforeAdvice(CFlowSystemAspect.NAME + '/' + CFlowSystemAspect.PRE_ADVICE);
//                m_cflowPointcut.addAfterFinallyAdvices(CFlowSystemAspect.NAME + '/' + CFlowSystemAspect.POST_ADVICE);
//            }
//        }
//    }
}
/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.JoinPointFactory;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.AdviceInfo;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.expression.ArgsIndexVisitor;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.lang.reflect.Method;

/**
 * Manages the join point compilation, loading and instantiation for the target classes.
 * This implementation relies on the SystemDefinitionContainer.
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
    public static void loadJoinPoint(final int joinPointType,
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
        final ClassLoader classLoader = callerClass.getClassLoader();
        boolean generateJoinPoint = false;
        try {
            if (calleeClass == null) {
                throw new RuntimeException("callee class [" + calleeClassName + "] is NULL");
            }
            ContextClassLoader.loadClass(classLoader, joinPointClassName.replace('/', '.'));
        } catch (ClassNotFoundException e) {
            generateJoinPoint = true;
        }
        if (!generateJoinPoint) {
            return;
        }

        //Aspects.initialize(calleeClassLoader);

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
    private static void doLoadJoinPoint(final String joinPointClassName,
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
        // FIXME: refactor getMethodInfo in INFO so that we can apply it on "<init>" and that it delegates to ctor
        // instead of checking things here.
        switch (joinPointType) {
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                withinInfo = callerClassInfo.getConstructor(AsmHelper.calculateConstructorHash(callerMethodDesc));
                break;
            default:
                // TODO - support for withincode <clinit>
                if (TransformationConstants.INIT_METHOD_NAME.equals(callerMethodName)) {
                    withinInfo = callerClassInfo.getConstructor(AsmHelper.calculateConstructorHash(callerMethodDesc));
                } else {
                    withinInfo =
                    callerClassInfo.getMethod(AsmHelper.calculateMethodHash(callerMethodName, callerMethodDesc));
                }
        }

        AdviceInfoStruct adviceInfos = getAdviceInfosForJoinPoint(
                callerClass.getClassLoader(), pointcutType, reflectionInfo, withinInfo
        );

        JoinPointFactory.newJoinPoint(
                joinPointType,
                joinPointClassName,
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
     * Retrieves the advice info wrapped up in a struct.
     *
     * @param loader
     * @param type
     * @param reflectInfo
     * @param withinInfo
     * @return the advice info
     */
    public static AdviceInfoStruct getAdviceInfosForJoinPoint(final ClassLoader loader,
                                                              final PointcutType type,
                                                              final ReflectionInfo reflectInfo,
                                                              final ReflectionInfo withinInfo) {

        // FIXME XXX handle cflow

        ExpressionContext exprCtx = new ExpressionContext(type, reflectInfo, withinInfo);

        List beforeAdvices = new ArrayList();
        List aroundAdvices = new ArrayList();
        List afterFinallyAdvices = new ArrayList();
        List afterReturningAdvices = new ArrayList();
        List afterThrowingAdvices = new ArrayList();

        List systemDefinitions = SystemDefinitionContainer.getHierarchicalDefs(loader);
        for (Iterator iterator = systemDefinitions.iterator(); iterator.hasNext();) {
            SystemDefinition systemDefinition = (SystemDefinition) iterator.next();

            Collection aspects = systemDefinition.getAspectDefinitions();
            for (Iterator iterator1 = aspects.iterator(); iterator1.hasNext();) {
                AspectDefinition aspectDefinition = (AspectDefinition) iterator1.next();

                //TODO - do we care about non bounded pointcut ?
                for (Iterator iterator2 = aspectDefinition.getAdviceDefinitions().iterator(); iterator2.hasNext();) {
                    AdviceDefinition adviceDefinition = (AdviceDefinition) iterator2.next();
                    if (adviceDefinition.getExpressionInfo().getExpression().match(exprCtx)) {
                        // compute the target method to advice method arguments map, and grab information about this
                        // and target bindings
                        exprCtx.resetRuntimeState();
                        ArgsIndexVisitor.updateContextForRuntimeInformation(adviceDefinition.getExpressionInfo(),
                                                                            exprCtx, loader);
                        // Note that the exprCtx dynamic information updated here should only be used
                        // in the scope of this code block, since at the next iteration, the data will be
                        // updated for another advice binding
                        // [hence see setMethodArgumentIndexes below]

                        // create a lightweight representation of the bounded advices to pass to the compiler
                        final MethodInfo adviceMethodInfo = adviceDefinition.getMethodInfo();
                        AdviceInfo info = new AdviceInfo(
                                aspectDefinition.getQualifiedName(),
                                aspectDefinition.getClassName(),
                                DeploymentModel.getDeploymentModelAsInt(aspectDefinition.getDeploymentModel()),
                                adviceMethodInfo.getName(),
                                AsmHelper.getMethodDescriptor(adviceMethodInfo),
                                AsmHelper.getArgumentTypes(adviceMethodInfo),
                                adviceDefinition.getType(),
                                adviceDefinition.getSpecialArgumentType(),
                                adviceDefinition.getName(),
                                exprCtx.m_targetWithRuntimeCheck,
                                adviceDefinition.getExpressionInfo(),
                                exprCtx
                        );

                        setMethodArgumentIndexes(adviceDefinition.getExpressionInfo(), exprCtx, info);

                        if (AdviceType.BEFORE.equals(adviceDefinition.getType())) {
                            beforeAdvices.add(info);
                        } else if (AdviceType.AROUND.equals(adviceDefinition.getType())) {
                            aroundAdvices.add(info);
                        } else if (AdviceType.AFTER_FINALLY.equals(adviceDefinition.getType())) {
                            afterFinallyAdvices.add(info);
                        } else if (AdviceType.AFTER_RETURNING.equals(adviceDefinition.getType())) {
                            afterReturningAdvices.add(info);
                        } else if (AdviceType.AFTER_THROWING.equals(adviceDefinition.getType())) {
                            afterThrowingAdvices.add(info);
                        } else if (AdviceType.AFTER.equals(adviceDefinition.getType())) {
                            afterReturningAdvices.add(info);//special case for "after only"
                        }
                    }
                }
            }
        }

        AdviceInfoStruct adviceInfo = new AdviceInfoStruct(
                aroundAdvices,
                beforeAdvices,
                afterFinallyAdvices,
                afterReturningAdvices,
                afterThrowingAdvices
        );
        return adviceInfo;
    }

    /**
     * Get the parameter names from a "method declaration" signature like pc(type a, type2 b) => 0:a, 1:b
     *
     * @param adviceName
     * @return the parameter names
     */
    public static String[] getParameterNames(final String adviceName) {
        int paren = adviceName.indexOf('(');
        List paramNames = new ArrayList();
        if (paren > 0) {
            String params = adviceName.substring(paren + 1, adviceName.lastIndexOf(')')).trim();
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
     * @param expressionInfo
     * @param ctx
     * @param adviceInfo
     */
    private static void setMethodArgumentIndexes(final ExpressionInfo expressionInfo,
                                                 final ExpressionContext ctx,
                                                 final AdviceInfo adviceInfo) {

        // grab the parameters names
        String[] adviceArgNames = getParameterNames(adviceInfo.getName());

        // map them from the ctx info
        int[] adviceToTargetArgs = new int[adviceInfo.getMethodParameterTypes().length];
        for (int k = 0; k < adviceArgNames.length; k++) {
            String adviceArgName = adviceArgNames[k];
            int exprArgIndex = expressionInfo.getArgumentIndex(adviceArgName);
            if (exprArgIndex >= 0 && ctx.m_exprIndexToTargetIndex.containsKey(exprArgIndex)) {
                adviceToTargetArgs[k] = ctx.m_exprIndexToTargetIndex.get(exprArgIndex);
            } else {
                // does not appears to be an argument of the advised target
                // It can be StaticJP / JP / This binding / Target binding
                if (isJoinPoint(adviceInfo.getMethodParameterTypes()[k])) {
                    //TODO adapt for custom JoinPoint with custom proceed(..)
                    adviceToTargetArgs[k] = AdviceInfo.JOINPOINT_ARG;
                } else if (isStaticJoinPoint(adviceInfo.getMethodParameterTypes()[k])) {
                    adviceToTargetArgs[k] = AdviceInfo.STATIC_JOINPOINT_ARG;
                } else if (isTarget(adviceArgName, ctx)) {
                    adviceToTargetArgs[k] = AdviceInfo.TARGET_ARG;
                } else if (isThis(adviceArgName, ctx)) {
                    adviceToTargetArgs[k] = AdviceInfo.THIS_ARG;
                } else {
                    throw new Error("Unbound advice parameter at index " + k +
                            " in " + adviceInfo.getMethodName() + adviceInfo.getMethodSignature() + " named " + adviceArgName);
                }
            }
        }

        // support for old style advices in XML whose name does not contain the call signature
        if (adviceArgNames.length == 0) {
            Type[] adviceArgTypes = adviceInfo.getMethodParameterTypes();
            for (int i = 0; i < adviceArgTypes.length; i++) {
                if (isJoinPoint(adviceArgTypes[i])) {
                    adviceToTargetArgs[i] = AdviceInfo.JOINPOINT_ARG;
                } else if (isStaticJoinPoint(adviceArgTypes[i])) {
                    adviceToTargetArgs[i] = AdviceInfo.STATIC_JOINPOINT_ARG;
                } else {
                    throw new Error("Unbound unnamed advice parameter at index " + i +
                            " in " + adviceInfo.getMethodSignature());
                }
            }
        }

        adviceInfo.setMethodToArgIndexes(adviceToTargetArgs);
    }

    private static boolean isJoinPoint(Type type) {
        return Type.getType(JoinPoint.class).getDescriptor().equals(type.getDescriptor());
    }

    private static boolean isStaticJoinPoint(Type type) {
        return Type.getType(StaticJoinPoint.class).getDescriptor().equals(type.getDescriptor());
    }

    private static boolean isTarget(String adviceArgName, ExpressionContext ctx) {
        return adviceArgName.equals(ctx.m_targetBoundedName);
    }

    private static boolean isThis(String adviceArgName, ExpressionContext ctx) {
        return adviceArgName.equals(ctx.m_thisBoundedName);
    }
}
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
import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;

/**
 * Manages the join point compilation, loading and instantiation for the target classes.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class InlinedJoinPointManager {

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
        boolean generateJoinPoint = false;
        try {
            calleeClass.getClassLoader().loadClass(joinPointClassName.replace('/', '.'));
        } catch (ClassNotFoundException e) {
            generateJoinPoint = true;
        }
        if (!generateJoinPoint) {
            return;
        }

        AspectSystem system = SystemLoader.getSystem(calleeClass.getClassLoader());
        system.initialize();

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
                        system,
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
                        system,
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
                        system,
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
                        system,
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
                        system,
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
                        system,
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
                        system,
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
     * @param system
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
                                        final AspectSystem system,
                                        final ClassInfo thisClassInfo) {

        ClassInfo callerClassInfo = JavaClassInfo.getClassInfo(callerClass);
        ReflectionInfo withinInfo = callerClassInfo.getMethod(
                AsmHelper.calculateMethodHash(callerMethodName, callerMethodDesc)
        );

        JoinPointMetaData metaData = JoinPointMetaData.getJoinPointMetaData(
                pointcutType, system, reflectionInfo, withinInfo
        );

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


                metaData.adviceIndexes,
                calleeClass.getClassLoader(),
                joinPointSequence
        );
        System.out.println("loading join point class: " + clazz);
    }
}
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
     * Ensures that the joinPointBase class for the given target class is generated. This call is added to the weaved
     * class as a "clinit" block
     *
     * @param klass
     */
    public static void loadJoinPointBase(final Class klass) {
        //TODO: do a load test to avoid useless JITgen
        //TODO: could be yet another option "-XoverridePackagedJITWithJITGen"
        JoinPointCompiler.compileJoinPointBase(klass.getName(), klass.getClassLoader());
    }

    /**
     * Ensures that the specific joinPoint class for the given target class and joinPoint info is generated. This call
     * is added to the weaved class as a "clinit" block
     *
     * @param joinPointType
     * @param calleeClass
     * @param calleeMemberName
     * @param calleeMemberDesc
     * @param calleeMemberModifiers
     * @param callerClassName
     * @param callerMethodName
     * @param callerMethodDesc
     * @param joinPointSequence
     * @param joinPointHash
     * @param joinPointClassName
     */
    public static void loadJoinPoint(final int joinPointType,
                                     final Class calleeClass,
                                     final String calleeMemberName,
                                     final String calleeMemberDesc,
                                     final int calleeMemberModifiers,
                                     final String callerClassName,
                                     final String callerMethodName,
                                     final String callerMethodDesc,
                                     final int callerMemberModifiers,
                                     final int joinPointSequence,
                                     final int joinPointHash,
                                     final String joinPointClassName) {

        Class callerClass = null;
        try {
            if (callerClassName != null) {
                callerClass = calleeClass.getClassLoader().loadClass(callerClassName.replace('/', '.'));
            }
        } catch (ClassNotFoundException callerNotFound) {
            throw new RuntimeException(
                    "caller class [" + callerClassName + "] can not be found in class loader [" +
                    calleeClass.getClassLoader() +
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

        ClassInfo thisClassInfo = JavaClassInfo.getClassInfo(calleeClass);

        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
                doLoadJoinPoint(
                        JoinPointType.METHOD_EXECUTION,
                        PointcutType.EXECUTION,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        system,
                        thisClassInfo
                );
                break;

            case JoinPointType.METHOD_CALL:
                doLoadJoinPoint(
                        JoinPointType.METHOD_CALL,
                        PointcutType.CALL,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        system,
                        thisClassInfo
                );
                break;
            case JoinPointType.FIELD_GET:
                doLoadJoinPoint(
                        JoinPointType.FIELD_GET,
                        PointcutType.GET,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        system,
                        thisClassInfo
                );
                break;

            case JoinPointType.FIELD_SET:
                doLoadJoinPoint(
                        JoinPointType.FIELD_SET,
                        PointcutType.SET,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        system,
                        thisClassInfo
                );
                break;

            case JoinPointType.CONSTRUCTOR_EXECUTION:
                doLoadJoinPoint(
                        JoinPointType.CONSTRUCTOR_EXECUTION,
                        PointcutType.EXECUTION,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        system,
                        thisClassInfo
                );
                break;

            case JoinPointType.CONSTRUCTOR_CALL:
                doLoadJoinPoint(
                        JoinPointType.CONSTRUCTOR_CALL,
                        PointcutType.CALL,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        system,
                        thisClassInfo
                );
                break;

            case JoinPointType.HANDLER:
                doLoadJoinPoint(
                        JoinPointType.HANDLER,
                        PointcutType.HANDLER,
                        calleeClass,
                        calleeMemberName,
                        calleeMemberDesc,
                        calleeMemberModifiers,
                        callerClass,
                        callerMethodName,
                        callerMethodDesc,
                        callerMemberModifiers,
                        joinPointSequence,
                        joinPointHash,
                        system,
                        thisClassInfo
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
     * @param joinPointType
     * @param pointcutType
     * @param calleeClass
     * @param calleeMemberName
     * @param calleeMemberDesc
     * @param calleeMemberModifiers
     * @param callerClass
     * @param callerMethodName
     * @param callerMethodDesc
     * @param callerMethodModifiers
     * @param joinPointSequence
     * @param joinPointHash
     * @param system
     * @param thisClassInfo
     */
    private static void doLoadJoinPoint(final int joinPointType,
                                        final PointcutType pointcutType,
                                        final Class calleeClass,
                                        final String calleeMemberName,
                                        final String calleeMemberDesc,
                                        final int calleeMemberModifiers,
                                        final Class callerClass,
                                        final String callerMethodName,
                                        final String callerMethodDesc,
                                        final int callerMethodModifiers,
                                        final int joinPointSequence,
                                        final int joinPointHash,
                                        final AspectSystem system,
                                        final ClassInfo thisClassInfo) {

        ClassInfo targetClassInfo = JavaClassInfo.getClassInfo(callerClass);

        ReflectionInfo reflectionInfo = thisClassInfo.getMethod(joinPointHash);

        ReflectionInfo withinInfo = targetClassInfo.getMethod(
                AsmHelper.calculateMethodHash(callerMethodName, callerMethodDesc)
        );

        JoinPointMetaData metaData = JoinPointMetaData.getJoinPointMetaData(
                pointcutType, system, reflectionInfo, withinInfo
        );

        JoinPointCompiler.loadJoinPoint(
                joinPointType,
                joinPointHash,

                calleeClass.getName(),
                calleeMemberName,
                calleeMemberDesc,
                calleeMemberModifiers,

                callerClass.getName(),
                callerMethodName,
                callerMethodDesc,
                callerMethodModifiers,

                metaData.adviceIndexes,
                calleeClass.getClassLoader(),
                joinPointSequence
        );
    }
}
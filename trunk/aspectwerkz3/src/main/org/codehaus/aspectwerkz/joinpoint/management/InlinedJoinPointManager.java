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
 * Manages the join point compilation, loading and instantiation for the target classes.s
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
     * @param klass
     */
    public static void loadJoinPoint(
        final Class klass,
        final int jpType,
        final String jpName,
        final String jpDesc,
        final int jpMod,
        final String callerClassName,
        final String callerName,
        final String callerDesc,
        final int jpSequence,
        final int jpHash,
        final String jpClassName) {

        Class callerClass = null;
        try {
            if (callerClassName != null) {
                callerClass = klass.getClassLoader().loadClass(callerClassName.replace('/', '.'));
            }
        } catch (ClassNotFoundException callerNotFound) {
            //TODO: what do to? - can that really happen?
            callerNotFound.printStackTrace();
        }

        // check if the JP is already loaded
        // this can occurs if user packaged its JIT classes, or if we are using multiweaving
        boolean generateJoinPoint = false;
        try {
            klass.getClassLoader().loadClass(jpClassName.replace('/', '.'));
        } catch (ClassNotFoundException e) {
            generateJoinPoint = true;
        }
        if (!generateJoinPoint) {
            return;
        }

        AspectSystem system = SystemLoader.getSystem(klass.getClassLoader());
        system.initialize();

        ClassInfo classInfo = JavaClassInfo.getClassInfo(klass);

        loadMethodExecutionJoinPoint(
            klass,
            jpName,
            jpDesc,
            jpMod,
            callerClass,
            callerName,
            callerDesc,
            jpSequence,
            jpHash,
            system,
            classInfo);
    }

    /**
     * Loads the method execution join point.
     * 
     * @param klass
     * @param jpName
     * @param jpDesc
     * @param jpMod
     * @param callerKlass
     * @param callerName
     * @param callerDesc
     * @param jpSequence
     * @param jpHash
     * @param system
     * @param classInfo
     */
    private static void loadMethodExecutionJoinPoint(
        final Class klass,
        final String jpName,
        final String jpDesc,
        final int jpMod,
        final Class callerKlass,
        final String callerName,
        final String callerDesc,
        final int jpSequence,
        final int jpHash,
        final AspectSystem system,
        final ClassInfo classInfo) {
        
        ReflectionInfo reflectionInfo = classInfo.getMethod(jpHash);
        ReflectionInfo withinInfo = null;
        if (callerKlass != null) {
            withinInfo = JavaClassInfo.getClassInfo(callerKlass);
        }
        if (callerName != null && callerDesc != null) {
            // TODO: same for field, ctor etc - depends on jpType
            int withinHash = AsmHelper.calculateMethodHash(callerName, callerDesc);
            withinInfo = ((ClassInfo) withinInfo).getMethod(withinHash);
        }

        // TODO - is that JPType ?? - Do we really need that
        JoinPointMetaData metaData = JoinPointMetaData.getJoinPointMetaData(PointcutType.EXECUTION, 
            system, reflectionInfo, withinInfo);

        JoinPointCompiler.loadJoinPoint(
            JoinPointType.METHOD_EXECUTION,
            jpHash,
            jpName,
            jpDesc,
            jpMod,
            klass.getName(),
            metaData.adviceIndexes,
            klass.getClassLoader(),
            jpSequence);
    }
}
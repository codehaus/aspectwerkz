/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
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
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class InlinedJoinPointManager {
    
    /**
     * Ensure that the joinPointBase class for the given target class is generated.
     * This call is added to the weaved class as a "clinit" block
     *  
     * @param klass
     */ 
    public static void loadJoinPointBase(Class klass) {
        //TODO: do a load test to avoid useless JITgen
        //TODO: could be yet another option "-XoverridePackagedJITWithJITGen"
        JoinPointCompiler.compileJoinPointBase(klass.getName(), klass.getClassLoader());
    }
    
    /**
     * Ensure that the specific joinPoint class for the given target class and joinPoint info is generated.
     * This call is added to the weaved class as a "clinit" block
     *
     * @param klass
     */
    public static void loadJoinPoint(Class klass, int jpType, String jpName, String jpDesc, int jpMod,
                                     String callerClassName, String callerName, String callerDesc, int jpSequence,
                                     int jpHash, String jpClassName) {
        //FIXME: other jp type
        Class callerKlass = null;
        try {
            if (callerClassName!=null) {
                callerKlass = klass.getClassLoader().loadClass(callerClassName.replace('/', '.'));
            }
        } catch (ClassNotFoundException callerNotFound) {
            //TODO: what do to - can that really happen ?
            callerNotFound.printStackTrace();
        }
        loadMethodExecutionJoinPoint(klass, jpName, jpDesc, jpMod, callerKlass, callerName, callerDesc,
                                     jpSequence, jpHash, jpClassName);
    }

    private static void loadMethodExecutionJoinPoint(Class klass, String jpName, String jpDesc, int jpMod,
                                                     Class callerKlass, String callerName, String callerDesc,
                                                     int jpSequence, int jpHash, String joinPointClassName) {

        // check if the JP is already loaded
        // this can occurs if user packaged its JIT classes, or if we are using multiweaving
        boolean generateJoinPoint = false;
        try {
            klass.getClassLoader().loadClass(joinPointClassName.replace('/', '.'));
        } catch (ClassNotFoundException e) {
            generateJoinPoint = true;
        }
        if (!generateJoinPoint) {
            return;
        }

        // system
        AspectSystem system = SystemLoader.getSystem(klass.getClassLoader());
        system.initialize();

        // ClassInfo
        ClassInfo classInfo = JavaClassInfo.getClassInfo(klass);
        // TODO: same for field, ctor etc - depends on jpType
        ReflectionInfo reflectionInfo = classInfo.getMethod(jpHash);

        // optional within
        ReflectionInfo withinInfo = null;
        if (callerKlass != null) {
            withinInfo = JavaClassInfo.getClassInfo(callerKlass);
        }
        if (callerName != null && callerDesc != null) {
            // TODO: same for field, ctor etc - depends on jpType
            int withinHash = AsmHelper.calculateMethodHash(callerName, callerDesc);
            withinInfo = ((ClassInfo)withinInfo).getMethod(withinHash);
        }

        // JpMetaData
        JoinPointMetaData metaData = JoinPointMetaData.getJoinPointMetaData(
            PointcutType.EXECUTION, //TODO - is that JPType ?? - Do we really need that ?
            system,
            reflectionInfo,
            withinInfo);

        JoinPointCompiler.loadJoinPoint(JoinPointType.METHOD_EXECUTION, jpHash, jpName, jpDesc, jpMod, klass.getName(), metaData.adviceIndexes, klass.getClassLoader(), jpSequence);

    }
        
}

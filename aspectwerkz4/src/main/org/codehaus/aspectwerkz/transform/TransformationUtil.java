/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;

/**
 * Utility method used by the transformers.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class TransformationUtil {

    /**
     * Returns the prefixed method name.
     *
     * @param methodName the method name
     * @param className  the class name
     * @return the name of the join point
     */
    public static String getPrefixedOriginalMethodName(final String methodName,
                                                       final String className) {
        final StringBuffer buf = new StringBuffer();
        buf.append(TransformationConstants.ORIGINAL_METHOD_PREFIX);
        buf.append(methodName);
        buf.append(TransformationConstants.DELIMITER);
        buf.append(className.replace('.', '_').replace('/', '_'));
        return buf.toString();
    }

    /**
     * Returns the prefixed method name.
     *
     * @param methodName the method name
     * @param methodDesc the method desc
     * @param className  the class name
     * @return the name of the join point
     */
    public static String getWrapperMethodName(final String methodName,
                                              final String methodDesc,
                                              final String className,
                                              final String prefix) {
        final StringBuffer buf = new StringBuffer();
        //FIXME: double check me
        // we use the javaC convention for hidden synthetic method
        // is the methodSequence enough ?
        // [ Alex: looks like it will change between each RW since tied to ctx match ]
        buf.append(TransformationConstants.WRAPPER_METHOD_PREFIX);
        buf.append(prefix);
        buf.append(methodName);
        buf.append(methodDesc.hashCode());//??
        buf.append(className.replace('.', '_').replace('/', '_'));
        return buf.toString().replace('-', '_');
    }

    /**
     * Build the join point invoke method descriptor for code (method or constructor) join points.
     * Depends if the target method is static or not.
     *
     * @param codeModifiers
     * @param codeDesc
     * @param callerTypeName
     * @param calleeTypeName
     * @return
     */
    public static String getInvokeSignatureForCodeJoinPoints(final int codeModifiers,
                                                             final String codeDesc,
                                                             final String callerTypeName,
                                                             final String calleeTypeName) {
        StringBuffer sig = new StringBuffer("(");
        if (!Modifier.isStatic(codeModifiers)) {
            // callee is arg0 for non static target method invoke call
            // else it is skept
            sig.append('L');
            sig.append(calleeTypeName);
            sig.append(';');
        }
        int index = codeDesc.lastIndexOf(')');
        sig.append(codeDesc.substring(1, index));
        sig.append('L');
        sig.append(callerTypeName);
        sig.append(';');
        sig.append(codeDesc.substring(index, codeDesc.length()));
        return sig.toString();
    }

    /**
     * Build the join point invoke method descriptor for field join points.
     * Depends if the target field is static or not.
     *
     * @param fieldModifiers
     * @param fieldDesc
     * @param callerTypeName
     * @param calleeTypeName
     * @return the signature
     */
    public static String getInvokeSignatureForFieldJoinPoints(final int fieldModifiers,
                                                              final String fieldDesc,
                                                              final String callerTypeName,
                                                              final String calleeTypeName) {
        StringBuffer sig = new StringBuffer("(");
        if (!Modifier.isStatic(fieldModifiers)) {
            // callee is arg0 for non static target method invoke call
            // else it is skept
            sig.append('L');
            sig.append(calleeTypeName);
            sig.append(';');
        }
        sig.append(fieldDesc);
        sig.append('L');
        sig.append(callerTypeName);
        sig.append(';');
        sig.append(')');
        sig.append(fieldDesc);
        return sig.toString();
    }

    /**
     * Build the join point invoke method descriptor for handler join points.
     *
     * @param withinTypeName
     * @param exceptionTypeName
     * @return the signature
     */
    public static String getInvokeSignatureForHandlerJoinPoints(final String withinTypeName,
                                                                final String exceptionTypeName) {
        StringBuffer sig = new StringBuffer("(");
        sig.append('L');
        sig.append(withinTypeName);
        sig.append(';');
        sig.append('L');
        sig.append(exceptionTypeName);
        sig.append(';');
        sig.append(')');
        sig.append('V');
        return sig.toString();
    }

    /**
     * Build the join point invoke method descriptor for ctor call join points.
     *
     * @param calleeConstructorDesc
     * @param callerTypeName
     * @param calleeTypeName
     * @return the signature
     */
    public static String getInvokeSignatureForConstructorCallJoinPoints(final String calleeConstructorDesc,
                                                                        final String callerTypeName,
                                                                        final String calleeTypeName) {
        StringBuffer sig = new StringBuffer("(");
        int index = calleeConstructorDesc.lastIndexOf(')');
        // callee ctor args
        sig.append(calleeConstructorDesc.substring(1, index));
        // caller
        sig.append('L');
        sig.append(callerTypeName);
        sig.append(';');
        sig.append(")L");
        sig.append(calleeTypeName);
        sig.append(';');
        return sig.toString();
    }

    /**
     * Returns the method name used for constructor body
     *
     * @param calleeTypeName
     * @return
     */
    public static String getConstructorBodyMethodName(final String calleeTypeName) {
        final StringBuffer buf = new StringBuffer();
        buf.append(TransformationConstants.ORIGINAL_METHOD_PREFIX);
        buf.append("init");
        buf.append(TransformationConstants.DELIMITER);
        buf.append(calleeTypeName.replace('.', '_').replace('/', '_'));
        return buf.toString();
    }

    /**
     * Returns the method used for constructor body signature
     * The callee type name is prepended to the constructor signature
     *
     * @param ctorDesc
     * @param calleeTypeName
     * @return
     */
    public static String getConstructorBodyMethodSignature(final String ctorDesc, final String calleeTypeName) {
        StringBuffer sig = new StringBuffer("(L");
        sig.append(calleeTypeName);
        sig.append(";");
        sig.append(ctorDesc.substring(1));
        return sig.toString();
    }

    /**
     * Computes the joinpoint classname : "caller/class_type_hash_suffix"
     * For constructor call joinpoints, the hash of callee name is used as well.
     *
     * @param thisClassName
     * @param targetClassName
     * @param joinPointType
     * @param joinPointHash
     * @return the JIT joinpoint classname
     */
    public static String getJoinPointClassName(final String thisClassName,
                                               final String targetClassName,
                                               final int joinPointType,
                                               final int joinPointHash) {
        StringBuffer classNameBuf = new StringBuffer(thisClassName);
        // TODO: INNER CLASS OR NOT?
//        classNameBuf.append("$");
        classNameBuf.append('_');
        classNameBuf.append(joinPointType);
        classNameBuf.append('_');
        classNameBuf.append(joinPointHash);
        //FIXME needed for method call jp ?
        if (joinPointType == JoinPointType.CONSTRUCTOR_CALL || joinPointType == JoinPointType.METHOD_CALL) {
            classNameBuf.append('_').append(targetClassName.hashCode());
        }
        classNameBuf.append(TransformationConstants.JOIN_POINT_CLASS_SUFFIX);

        //replace minus signs on m_joinPointHash
        return classNameBuf.toString().replace('-', '_').replace('.', '/');
    }

    /**
     * Checks if a set of interfaces has any clashes, meaning any methods with the same name and signature.
     *
     * @param interfacesToAdd
     * @param loader
     * @return boolean
     */
    public static boolean hasMethodClash(final Set interfacesToAdd, final ClassLoader loader) {
        // build up the validation structure
        Map methodMap = new HashMap();
        for (Iterator it = interfacesToAdd.iterator(); it.hasNext();) {
            ClassInfo classInfo = AsmClassInfo.getClassInfo((String) it.next(), loader);

            List methods = ClassInfoHelper.collectMethodsFromInterface(classInfo);

            for (Iterator it2 = methods.iterator(); it2.hasNext();) {
                MethodInfo methodInfo = (MethodInfo) it2.next();
                String key = methodInfo.getName() + ':' + methodInfo.getSignature();
                if (!methodMap.containsKey(key)) {
                    methodMap.put(key, new ArrayList());
                }
                ((List) methodMap.get(key)).add(classInfo.getName());
            }
        }

        // validate the structure
        for (Iterator it = methodMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            List interfaceNames = (List) entry.getValue();
            if (interfaceNames.size() > 1) {
                StringBuffer msg = new StringBuffer();
                msg.append("can not add interfaces [");
                for (Iterator it2 = interfaceNames.iterator(); it2.hasNext();) {
                    String interfaceName = (String) it2.next();
                    msg.append(interfaceName);
                    if (it2.hasNext()) {
                        msg.append(',');
                    }
                }
                msg.append("] since they all have method [");
                msg.append(key);
                msg.append(']');
                System.out.println("AW:WARNING - " + msg.toString());
                return true;
            }
        }
        return false;
    }
}
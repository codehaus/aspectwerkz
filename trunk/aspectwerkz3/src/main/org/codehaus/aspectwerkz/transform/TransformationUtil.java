/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.lang.reflect.Modifier;

/**
 * Utility method used by the transformers.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class TransformationUtil {

    /**
     * Returns the prefixed method name.
     *
     * @param methodName     the method name
     * @param methodSequence the method sequence
     * @param className      the class name
     * @return the name of the join point
     */
    public static String getPrefixedOriginalMethodName(final String methodName,
                                                       final int methodSequence,
                                                       final String className) {
        final StringBuffer buf = new StringBuffer();
        buf.append(TransformationConstants.ORIGINAL_METHOD_PREFIX);
        buf.append(methodName);
        buf.append(TransformationConstants.DELIMITER);
        buf.append(methodSequence);
        buf.append(TransformationConstants.DELIMITER);
        buf.append(className.replace('.', '_').replace('/', '_'));
        return buf.toString();
    }

    /**
     * Returns the prefixed method name.
     *
     * @param methodName     the method name
     * @param methodSequence the method sequence
     * @param className      the class name
     * @return the name of the join point
     */
    public static String getWrapperMethodName(final String methodName,
                                                    final int methodSequence,
                                                    final String className,
                                                    final String prefix) {
        final StringBuffer buf = new StringBuffer();
        //FIXME: double check me
        // we use the javaC convention for hidden synthetic method
        // is the methodSequence enough ?
        // [ Alex: looks like it will change between each RW since tied to ctx match ]
        buf.append(TransformationConstants.WRAPPER_METHOD_PREFIX);
        buf.append(methodSequence);
        buf.append("$");
        buf.append(prefix);
        buf.append(methodName);
        buf.append(TransformationConstants.DELIMITER);
        buf.append(methodSequence);
        buf.append(TransformationConstants.DELIMITER);
        buf.append(className.replace('.', '_').replace('/', '_'));
        return buf.toString();
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
     * @return
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
}
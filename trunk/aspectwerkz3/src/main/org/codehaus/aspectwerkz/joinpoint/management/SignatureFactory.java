/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.aspect.management.AspectRegistry;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;

import java.lang.reflect.Field;

/**
 * Factory class for the signature hierarchy.
 *  
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class SignatureFactory {
    public static final Signature newMethodSignature(final Class declaringClass, final int joinPointHash) {
        MethodTuple methodTuple = AspectRegistry.getMethodTuple(declaringClass, joinPointHash);
        return new MethodSignatureImpl(methodTuple.getDeclaringClass(), methodTuple);
    }

    public static final Signature newFieldSignature(
        final Class declaringClass,
        final int joinPointHash) {
        Field field = AspectRegistry.getField(declaringClass, joinPointHash);
        return new FieldSignatureImpl(field.getDeclaringClass(), field);
    }

    public static final Signature newConstructorSignature(
        final Class declaringClass,
        final int joinPointHash) {
        ConstructorTuple constructorTuple = AspectRegistry.getConstructorTuple(
            declaringClass,
            joinPointHash);
        return new ConstructorSignatureImpl(constructorTuple.getDeclaringClass(), constructorTuple);
  }

    public static final Signature newCatchClauseSignature(
        final Class declaringClass,
        final int joinPointHash) {
        return new CatchClauseSignatureImpl(declaringClass, declaringClass, "");
    }
}
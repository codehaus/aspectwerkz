/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.joinpoint.impl.MethodTuple;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.reflect.ReflectHelper;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.joinpoint.impl.CatchClauseSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodTuple;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.reflect.ReflectHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import gnu.trove.TIntObjectHashMap;

/**
 * Factory class for the signature hierarchy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class SignatureFactory {

    /**
     * Holds references to the methods to the advised classes in the system.
     */
    private final static Map s_methods = new WeakHashMap();

    /**
     * @param declaringClass
     * @param joinPointHash
     * @return
     */
    public static final MethodSignatureImpl newMethodSignature(final Class declaringClass, final int joinPointHash) {
        Method[] methods = declaringClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (ReflectHelper.calculateHash(method) == joinPointHash) {
                return new MethodSignatureImpl(declaringClass, method);
            }
        }
        // lookup in the hierarchy
        MethodSignatureImpl signature = null;
        for (int i = 0; i < declaringClass.getInterfaces().length; i++) {
            signature = newMethodSignature(declaringClass.getInterfaces()[i], joinPointHash);
            if (signature != null) {
                return signature;
            }
        }
        if (declaringClass.getSuperclass() != null) {
            signature = newMethodSignature(declaringClass.getSuperclass(), joinPointHash);
        } else {
            return null;
        }
        return signature;
    }

    /**
     * @param declaringClass
     * @param joinPointHash
     * @return
     */
    public static final FieldSignatureImpl newFieldSignature(final Class declaringClass, final int joinPointHash) {
        Field[] fields = declaringClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (ReflectHelper.calculateHash(field) == joinPointHash) {
                return new FieldSignatureImpl(declaringClass, field);
            }
        }
        // lookup in the hierarchy
        if (declaringClass.getSuperclass() != null) {
            return newFieldSignature(declaringClass.getSuperclass(), joinPointHash);
        } else {
            return null;
        }
    }

    /**
     * @param declaringClass
     * @param joinPointHash
     * @return
     */
    public static final ConstructorSignatureImpl newConstructorSignature(final Class declaringClass,
                                                                         final int joinPointHash) {
        Constructor constructor = null;
        for (int i = 0; i < declaringClass.getDeclaredConstructors().length; i++) {
            Constructor c = declaringClass.getDeclaredConstructors()[i];
            if (ReflectHelper.calculateHash(c) == joinPointHash) {
                return new ConstructorSignatureImpl(declaringClass, c);
            }
        }
        // lookup in the hierarchy
        if (declaringClass.getSuperclass() != null) {
            return newConstructorSignature(declaringClass.getSuperclass(), joinPointHash);
        } else {
            return null;
        }
    }

    public static final CatchClauseSignatureImpl newCatchClauseSignature(final Class exceptionClass) {
        return new CatchClauseSignatureImpl(exceptionClass);
    }

    /**
     * Creates a new method repository for the class specified.
     *
     * @param klass the class
     */
    protected static void createMethodRepository(final Class klass) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        if (s_methods.containsKey(klass)) {
            return;
        }
        Method[] methods = klass.getDeclaredMethods();
        TIntObjectHashMap methodMap = new TIntObjectHashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method wrapperMethod = methods[i];
            if (!wrapperMethod.getName().startsWith(TransformationConstants.ASPECTWERKZ_PREFIX)) {
                Method prefixedMethod = null;
                for (int j = 0; j < methods.length; j++) {
                    Method method2 = methods[j];
                    if (method2.getName().startsWith(TransformationConstants.ASPECTWERKZ_PREFIX)) {
                        String[] tokens = Strings.splitString(method2.getName(), TransformationConstants.DELIMITER);
                        String methodName = (tokens.length <= 1) ? "" : tokens[1];//ctor exe wrapper - FIXME do better
                        if (!methodName.equals(wrapperMethod.getName())) {
                            continue;
                        }
                        Class[] parameterTypes1 = wrapperMethod.getParameterTypes();
                        Class[] parameterTypes2 = method2.getParameterTypes();
                        if (parameterTypes2.length != parameterTypes1.length) {
                            continue;
                        }
                        boolean match = true;
                        for (int k = 0; k < parameterTypes1.length; k++) {
                            if (parameterTypes1[k] != parameterTypes2[k]) {
                                match = false;
                                break;
                            }
                        }
                        if (!match) {
                            continue;
                        }
                        prefixedMethod = method2;
                        break;
                    }
                }

                // create a method tuple with 'wrapped method' and 'prefixed method'
                MethodTuple methodTuple = new MethodTuple(wrapperMethod, prefixedMethod);

                // map the tuple to the hash for the 'wrapper method'
                int methodHash = ReflectHelper.calculateHash(wrapperMethod);
                methodMap.put(methodHash, methodTuple);
            }
        }
        synchronized (s_methods) {
            s_methods.put(klass, methodMap);
        }
    }
}
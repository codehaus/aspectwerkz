/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.CtNewMethod;
import javassist.bytecode.CodeAttribute;

/**
 * Helper for Javassist
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class JavassistHelper {

    /**
     * Helper method for bogus CtMethod.make in Javassist for static methods
     *
     * @param returnType
     * @param name
     * @param parameters
     * @param exceptions
     * @param body
     * @param declaring
     * @return new method
     * @throws CannotCompileException
     */
    public static CtMethod makeStatic(
            final CtClass returnType,
            final String name,
            final CtClass[] parameters,
            final CtClass[] exceptions,
            final String body,
            final CtClass declaring)
            throws CannotCompileException {
        try {
            CtMethod cm = new CtMethod(returnType, name, parameters, declaring);
            cm.setModifiers(cm.getModifiers() | Modifier.STATIC);
            cm.setExceptionTypes(exceptions);
            cm.setBody(body);
            return cm;
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }

    /**
     * Gets the default value for primitive types
     *
     * @param type
     * @return
     */
    public static String getDefaultPrimitiveValue(CtClass type) {
        if (type == CtClass.booleanType) {
            return "false";
        }
        else if (type == CtClass.intType) {
            return "0";
        }
        else if (type == CtClass.longType) {
            return "0L";
        }
        else if (type == CtClass.floatType) {
            return "0.0f";
        }
        else if (type == CtClass.shortType) {
            return "(short)0";
        }
        else if (type == CtClass.byteType) {
            return "(byte)0";
        }
        else if (type == CtClass.charType) {
            return "''";//TODO should be '\u0000'
        }
        else if (type == CtClass.doubleType) {
            return "(double)0";
        }
        else {
            return "null";
        }
    }

    /**
     * Checks if the given Class as already a method methodName
     * Does not take into account the signature
     *
     * @param klass
     * @param methodName
     * @return true if klass has methodName
     */
    public static boolean hasMethod(CtClass klass, String methodName) {
        try {
            klass.getDeclaredMethod(methodName);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if the given Class as already a method methodName
     * Does not take into account the signature
     *
     * @param klass
     * @param fieldName
     * @return true if klass has methodName
     */
    public static boolean hasField(CtClass klass, String fieldName) {
        try {
            klass.getDeclaredField(fieldName);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if the given Class as already a method methodName
     * Does not take into account the signature
     *
     * @param klass
     * @param methodName
     * @return true if klass has methodName
     */
    public static boolean hasMethod(CtClass klass, String methodName, CtClass[] args) {
        try {
            klass.getDeclaredMethod(methodName, args);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Swapp bodies of the two given methods of the same declaring class
     *
     * @param methodA
     * @param methodB
     */
    public static void swapBodies(CtMethod methodA, CtMethod methodB) {
        String nameA = methodA.getName();
        int modifiersA = methodA.getModifiers();
        //TODO support for Attributes ?
        methodA.setName(methodB.getName());
        methodA.setModifiers(methodB.getModifiers());
        methodB.setName(nameA);
        methodB.setModifiers(modifiersA);
    }

    /**
     * Converts a Javassist type signature to a reflect type signature.
     * <p/>
     * Since <b>sucky</b> Javassist does not use the standard.
     *
     * @param typeName
     * @return
     */
    public static String convertJavassistTypeSignatureToReflectTypeSignature(String typeName) {
        int index = typeName.indexOf("[]");
        if (index >= 0) {
            typeName = typeName.substring(0, index);
            if (typeName.equals("short")) {
                typeName = "[S";
            }
            else if (typeName.equals("int")) {
                typeName = "[I";
            }
            else if (typeName.equals("long")) {
                typeName = "[J";
            }
            else if (typeName.equals("float")) {
                typeName = "[F";
            }
            else if (typeName.equals("double")) {
                typeName = "[D";
            }
            else if (typeName.equals("char")) {
                typeName = "[C";
            }
            else if (typeName.equals("byte")) {
                typeName = "[B";
            }
            else if (typeName.equals("boolean")) {
                typeName = "[Z";
            }
            else {
                typeName = "[L" + typeName + ';';
            }
        }
        return typeName;
    }

    /**
     * Checks if a method is marked as an empty wrapper (runtime weaving)
     *
     * @param method
     * @return true if empty wrapper
     */
    public static boolean isAnnotatedEmpty(CtMethod method) {
        byte[] emptyBytes = method.getAttribute(TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE);
        return (emptyBytes != null && emptyBytes[0] == TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE_VALUE_EMPTY);
    }

    /**
     * Checks if a method is marked as a non empty wrapper (runtime unweaving)
     *
     * @param method
     * @return true if non empty wrapper
     */
    public static boolean isAnnotatedNotEmpty(CtMethod method) {
        byte[] emptyBytes = method.getAttribute(TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE);
        return (emptyBytes == null || emptyBytes[0] == TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE_VALUE_NOTEMPTY);
    }

    /**
     * Sets a method as beeing an empty wrapper
     *
     * @param method
     */
    public static void setAnnotatedEmpty(CtMethod method) {
        method.setAttribute(TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE,
                            new byte[]{TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE_VALUE_EMPTY});
    }

    /**
     * Sets a method as beeing a non empty wrapper
     *
     * @param method
     */
    public static void setAnnotatedNotEmpty(CtMethod method) {
        method.setAttribute(TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE,
                            new byte[]{TransformationUtil.EMPTY_WRAPPER_ATTRIBUTE_VALUE_NOTEMPTY});
    }

    /**
     * Creates an empty wrapper method to allow HotSwap without schema change
     *
     * TODO refactor PrepareTransformer
     * CAUTION: does not check the wrapped method already exists while
     * PrepareTransformer version does
     *
     * @param ctClass        the ClassGen
     * @param originalMethod the current method
     * @param methodSequence the method hash
     * @return the wrapper method
     */
    public static CtMethod createEmptyWrapperMethod(
            final CtClass ctClass,
            final CtMethod originalMethod,
            final int methodSequence)
            throws NotFoundException, CannotCompileException {

        String wrapperMethodName = TransformationUtil.getPrefixedMethodName(
                originalMethod.getName(), methodSequence, ctClass.getName().replace('/', '.')
        );

        // determine the method access flags (should always be set to protected)
        int accessFlags = originalMethod.getModifiers();
        if ((accessFlags & Modifier.PROTECTED) == 0) {
            // set the protected flag
            accessFlags |= Modifier.PROTECTED;
        }
        if ((accessFlags & Modifier.PRIVATE) != 0) {
            // clear the private flag
            accessFlags &= ~Modifier.PRIVATE;
        }
        if ((accessFlags & Modifier.PUBLIC) != 0) {
            // clear the public flag
            accessFlags &= ~Modifier.PUBLIC;
        }

        // add an empty body
        StringBuffer body = new StringBuffer();
        if (originalMethod.getReturnType() == CtClass.voidType) {
            // special handling for void return type leads to cleaner bytecode generation with Javassist
            body.append("{}");
        }
        else if (!originalMethod.getReturnType().isPrimitive()) {
            body.append("{ return null;}");
        }
        else {
            body.append("{ return ");
            body.append(JavassistHelper.getDefaultPrimitiveValue(originalMethod.getReturnType()));
            body.append("; }");
        }

        CtMethod method = null;
        if (Modifier.isStatic(originalMethod.getModifiers())) {
            method = JavassistHelper.makeStatic(
                    originalMethod.getReturnType(),
                    wrapperMethodName,
                    originalMethod.getParameterTypes(),
                    originalMethod.getExceptionTypes(),
                    body.toString(),
                    ctClass
            );
        }
        else {
            method = CtNewMethod.make(
                    originalMethod.getReturnType(),
                    wrapperMethodName,
                    originalMethod.getParameterTypes(),
                    originalMethod.getExceptionTypes(),
                    body.toString(),
                    ctClass
            );
            method.setModifiers(accessFlags);
        }

        // add a method level attribute so that we remember it is an empty method
        JavassistHelper.setAnnotatedEmpty(method);

        return method;
    }

}

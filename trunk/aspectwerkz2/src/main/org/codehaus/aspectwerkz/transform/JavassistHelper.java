/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

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

}

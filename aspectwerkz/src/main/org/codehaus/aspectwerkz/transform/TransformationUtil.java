/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.transform;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.definition.metadata.FieldMetaData;

/**
 * Holds the constants and utility method used by the transformers.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bon�r</a>
 * @version $Id: TransformationUtil.java,v 1.2 2003-05-12 09:20:46 jboner Exp $
 */
public final class TransformationUtil {

    public static final String DELIMITER = "$";
    public static final String CALL_SIDE_DELIMITER = "#";
    public static final String UUID_FIELD = "___uuid";
    public static final String META_DATA_FIELD = "___metaData";
    public static final String STATIC_CLASS_FIELD = "___clazz";
    public static final String JOIN_POINT_PREFIX = "___jp";
    public static final String HIDDEN_METHOD_PREFIX = "___hidden" + DELIMITER;
    public static final String ORIGINAL_METHOD_PREFIX = "___originalMethod" + DELIMITER;
    public static final String MEMBER_METHOD_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "memberMethod" + DELIMITER;
    public static final String STATIC_METHOD_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "staticMethod" + DELIMITER;
    public static final String MEMBER_FIELD_GET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "memberField" + DELIMITER + "get" + DELIMITER;
    public static final String MEMBER_FIELD_SET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "memberField" + DELIMITER + "set" + DELIMITER;
    public static final String STATIC_FIELD_GET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "staticField" + DELIMITER + "get" + DELIMITER;
    public static final String STATIC_FIELD_SET_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "staticField" + DELIMITER + "set" + DELIMITER;
    public static final String CALLER_SIDE_JOIN_POINT_PREFIX = JOIN_POINT_PREFIX + DELIMITER + "callerSideMethod" + DELIMITER;

    public static final String FIELD_JOIN_POINT_PRE_EXECUTION_METHOD = "pre";
    public static final String FIELD_JOIN_POINT_POST_EXECUTION_METHOD = "post";
    public static final String CALLER_SIDE_JOIN_POINT_PRE_EXECUTION_METHOD = "pre";
    public static final String CALLER_SIDE_JOIN_POINT_POST_EXECUTION_METHOD = "post";
    public static final String HANDLER_JOIN_POINT_EXECUTION_METHOD = "proceed";
    public static final String GET_JOIN_POINTS_EXECUTION_METHOD = "getJoinPoints";
    public static final String UUID_EXECUTION_METHOD = "generate";
    public static final String GET_UUID_METHOD = "getUuid";
    public static final String GET_META_DATA_METHOD = HIDDEN_METHOD_PREFIX + "getMetaData";
    public static final String SET_META_DATA_METHOD = HIDDEN_METHOD_PREFIX + "addMetaData";

    public static final String ASPECT_WERKZ_CLASS = "org.codehaus.aspectwerkz.AspectWerkz";
    public static final String INTRODUCTION_CLASS = "org.codehaus.aspectwerkz.introduction.Introduction";
    public static final String MEMBER_METHOD_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.MemberMethodJoinPoint";
    public static final String STATIC_METHOD_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.StaticMethodJoinPoint";
    public static final String MEMBER_FIELD_GET_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.MemberFieldGetJoinPoint";
    public static final String MEMBER_FIELD_SET_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.MemberFieldSetJoinPoint";
    public static final String STATIC_FIELD_GET_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.StaticFieldGetJoinPoint";
    public static final String STATIC_FIELD_SET_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.StaticFieldSetJoinPoint";
    public static final String CALLER_SIDE_JOIN_POINT_CLASS = "org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint";

    public static final String IDENTIFIABLE_INTERFACE = "org.codehaus.aspectwerkz.Identifiable";
    public static final String META_DATA_INTERFACE = "org.codehaus.aspectwerkz.MetaDataEnhanceable";
    public static final String UUID_CLASS = "org.codehaus.aspectwerkz.util.UuidGenerator";

    public static final ObjectType MEMBER_METHOD_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.MemberMethodJoinPoint");
    public static final ObjectType STATIC_METHOD_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.StaticMethodJoinPoint");
    public static final ObjectType MEMBER_FIELD_GET_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.MemberFieldGetJoinPoint");
    public static final ObjectType MEMBER_FIELD_SET_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.MemberFieldSetJoinPoint");
    public static final ObjectType STATIC_FIELD_GET_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.StaticFieldGetJoinPoint");
    public static final ObjectType STATIC_FIELD_SET_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.StaticFieldSetJoinPoint");
    public static final ObjectType CALLER_SIDE_JOIN_POINT_TYPE = new ObjectType("org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint");

    /**
     * Creates a MethodMetaData class out of the BCEL Method class
     *
     * @param instruction the instruction
     * @return the method meta-data
     */
    public static MethodMetaData createMethodMetaData(
            final InvokeInstruction instruction,
            final ConstantPoolGen cpg) {

        MethodMetaData methodMetaData = new MethodMetaData();

        String signature = instruction.getSignature(cpg);
        methodMetaData.setName(instruction.getName(cpg));

        Type[] parameterTypes = Type.getArgumentTypes(signature);
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int j = 0; j < parameterTypes.length; j++) {
            parameterTypeNames[j] = parameterTypes[j].toString();

        }
        methodMetaData.setParameterTypes(parameterTypeNames);
        methodMetaData.setReturnType(Type.getReturnType(signature).toString());
        return methodMetaData;
    }

    /**
     * Creates a MethodMetaData class out of the BCEL Method class
     *
     * @param method the method
     * @return
     */
    public static MethodMetaData createMethodMetaData(final Method method) {
        MethodMetaData methodMetaData =
                new MethodMetaData();
        methodMetaData.setName(method.getName());

        Type[] parameterTypes = method.getArgumentTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int j = 0; j < parameterTypes.length; j++) {
            parameterTypeNames[j] = parameterTypes[j].toString();

        }
        methodMetaData.setParameterTypes(parameterTypeNames);
        methodMetaData.setReturnType(method.getReturnType().toString());
        return methodMetaData;
    }

    /**
     * Creates a FieldMetaData class out of the BCEL Method class
     *
     * @param instruction the field instruction
     * @param cpg the constant pool
     * @return the field meta-data
     */
    public static FieldMetaData createFieldMetaData(
            final FieldInstruction instruction,
            final ConstantPoolGen cpg) {
        FieldMetaData fieldMetaData = new FieldMetaData();
        fieldMetaData.setName(instruction.getFieldName(cpg));
        fieldMetaData.setType(instruction.getFieldType(cpg).toString());
        return fieldMetaData;
    }

    /**
     * Converts String access types to BCEL access types.
     *
     * @param modifiers the modifiers as strings
     * @return the BCEL modifiers (int)
     */
    public static int getModifiersAsInt(final String[] modifiers) {
        int accessFlags = 0;
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i].equals("abstract")) {
                accessFlags |= Constants.ACC_ABSTRACT;
            }
            else if (modifiers[i].equals("final")) {
                accessFlags |= Constants.ACC_FINAL;
            }
            else if (modifiers[i].equals("interface")) {
                accessFlags |= Constants.ACC_INTERFACE;
            }
            else if (modifiers[i].equals("native")) {
                accessFlags |= Constants.ACC_NATIVE;
            }
            else if (modifiers[i].equals("private")) {
                accessFlags |= Constants.ACC_PRIVATE;
            }
            else if (modifiers[i].equals("protected")) {
                accessFlags |= Constants.ACC_PROTECTED;
            }
            else if (modifiers[i].equals("public")) {
                accessFlags |= Constants.ACC_PUBLIC;
            }
            else if (modifiers[i].equals("static")) {
                accessFlags |= Constants.ACC_STATIC;
            }
            else if (modifiers[i].equals("strict")) {
                accessFlags |= Constants.ACC_STRICT;
            }
            else if (modifiers[i].equals("super")) {
                accessFlags |= Constants.ACC_SUPER;
            }
            else if (modifiers[i].equals("synchronized")) {
                accessFlags |= Constants.ACC_SYNCHRONIZED;
            }
            else if (modifiers[i].equals("transient")) {
                accessFlags |= Constants.ACC_TRANSIENT;
            }
            else if (modifiers[i].equals("volatile")) {
                accessFlags |= Constants.ACC_VOLATILE;
            }
        }
        return accessFlags;
    }

    /**
     * Converts a type represented as a string to a BCEL type.
     *
     * @param type the type as a string
     * @return the BCEL type
     */
    public static Type getBcelType(final String type) {
        Type bcelReturnType;
        if (type.equals("void")) {
            bcelReturnType = Type.VOID;
        }
        else if (type.equals("int")) {
            bcelReturnType = Type.INT;
        }
        else if (type.equals("long")) {
            bcelReturnType = Type.LONG;
        }
        else if (type.equals("short")) {
            bcelReturnType = Type.SHORT;
        }
        else if (type.equals("double")) {
            bcelReturnType = Type.DOUBLE;
        }
        else if (type.equals("float")) {
            bcelReturnType = Type.FLOAT;
        }
        else if (type.equals("char")) {
            bcelReturnType = Type.CHAR;
        }
        else if (type.equals("boolean")) {
            bcelReturnType = Type.BOOLEAN;
        }
        else if (type.equals("byte")) {
            bcelReturnType = Type.BYTE;
        }
        else {
            bcelReturnType = new ObjectType(type);
        }
        return bcelReturnType;
    }

    /**
     * Converts a BCEL type to a class.
     *
     * @todo how do I create an array class of certain type from a type?
     * @param bcelType the BCEL type
     * @return the class
     */
    public static Class convertBcelTypeToClass(final Type bcelType) {

        final String type = bcelType.toString();

        Class klass;
        if (type.equals("void")) {
            klass = null;
        }
        else if (type.equals("long")) {
            klass = long.class;
        }
        else if (type.equals("int")) {
            klass = int.class;
        }
        else if (type.equals("short")) {
            klass = short.class;
        }
        else if (type.equals("double")) {
            klass = double.class;
        }
        else if (type.equals("float")) {
            klass = float.class;
        }
        else if (type.equals("boolean")) {
            klass = boolean.class;
        }
        else if (type.equals("byte")) {
            klass = byte.class;
        }
        else if (type.equals("char")) {
            klass = char.class;
        }
        else if (type.endsWith("[]")) {
            // array type
            klass = new Object[]{}.getClass();
        }
        else {
            // regular object type
            try {
                klass = Thread.currentThread().
                        getContextClassLoader().loadClass(type);
            }
            catch (Exception e1) {
                try {
                    klass = Class.forName(type);
                }
                catch (ClassNotFoundException e2) {
                    throw new WrappedRuntimeException(e2);
                }
            }
        }
        return klass;
    }
}

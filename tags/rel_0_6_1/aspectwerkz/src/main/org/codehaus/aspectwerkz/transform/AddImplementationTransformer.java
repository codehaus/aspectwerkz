/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
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

import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;

import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.Constants;

import org.cs3.jmangler.bceltransformer.AbstractInterfaceTransformer;
import org.cs3.jmangler.bceltransformer.UnextendableClassSet;
import org.cs3.jmangler.bceltransformer.ExtensionSet;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.WeaveModel;
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Adds an Introductions to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AddImplementationTransformer.java,v 1.8 2003-06-17 16:07:55 jboner Exp $
 */
public class AddImplementationTransformer extends AbstractInterfaceTransformer {
    ///CLOVER:OFF

    /**
     * Holds references to the classes that have already been transformed.
     */
    private final Set m_transformed = new HashSet();

    /**
     * Holds the createWeaveModel model.
     */
    private final WeaveModel m_weaveModel;

    /**
     * Retrieves the createWeaveModel model.
     */
    public AddImplementationTransformer() {
        super();
        List weaveModels = WeaveModel.loadModels();
        if (weaveModels.size() > 1) {
            throw new RuntimeException("more than one createWeaveModel model is specified");
        }
        else {
            m_weaveModel = (WeaveModel)weaveModels.get(0);
        }
    }

    /**
     * Adds introductions to a class.
     *
     * @param es the extension set
     * @param cs the unextendable class set
     */
    public void transformInterface(final ExtensionSet es,
                                   final UnextendableClassSet cs) {
        Iterator it = cs.getIteratorForTransformableClasses();
        while (it.hasNext()) {
            final ClassGen cg = (ClassGen)it.next();
            if (classFilter(cg)) {
                continue;
            }
            if (m_transformed.contains(cg.getClassName())) {
                continue;
            }
            m_transformed.add(cg.getClassName());

            final ConstantPoolGen cpg = cg.getConstantPool();
            final InstructionFactory factory = new InstructionFactory(cg);
            addIntroductions(es, cg, cpg, factory);
        }
    }

    /**
     * Adds introductions to the class.
     *
     * @param es the extension set
     * @param cg the class gen
     * @param cpg the constant pool gen
     * @param factory the instruction objectfactory
     */
    private void addIntroductions(final ExtensionSet es,
                                  final ClassGen cg,
                                  final ConstantPoolGen cpg,
                                  final InstructionFactory factory) {

        for (Iterator it = m_weaveModel.getIntroductionNames(
                cg.getClassName()).iterator(); it.hasNext();) {

            String introductionName = (String)it.next();

            int introductionIndex = 0;
            List methodMetaDataList = null;
            try {
                introductionIndex = m_weaveModel.
                        getIntroductionIndex(introductionName);
                methodMetaDataList = m_weaveModel.
                        getIntroductionMethodsMetaData(introductionName);
            }
            catch (Exception e) {
                throw new DefinitionException("trying to createWeaveModel introduction with null or emtpy string as name to class " + cg.getClassName() + ": definition file is not consistent");
            }

            if (methodMetaDataList == null) continue; // interface introduction

            for (Iterator it2 = methodMetaDataList.iterator(); it2.hasNext();) {
                MethodMetaData methodMetaData = (MethodMetaData)it2.next();
                // remove the getUuid, ___hidden$getMetaData and setMetaData methods
                if (methodMetaData.getName().equals(
                        TransformationUtil.GET_UUID_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.GET_META_DATA_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.SET_META_DATA_METHOD)) {
                    methodMetaDataList.remove(methodMetaData);
                }
            }
            // sort the list so that we can enshure that the indexes are in synch
            // see AbstractIntroductionContainerStrategy#AbstractIntroductionContainerStrategy
            Collections.sort(methodMetaDataList, MethodComparator.
                    getInstance(MethodComparator.METHOD_META_DATA));

            int methodIndex = -1; // start with -1 since the method array is 0 indexed
            for (Iterator it2 = methodMetaDataList.iterator(); it2.hasNext();) {
                MethodMetaData methodMetaData = (MethodMetaData)it2.next();
                if (methodMetaData.getReturnType() == null) {
                    continue; // constructor => skip
                }
                methodIndex++;
                createProxyMethod(
                        es, cg, cpg, factory,
                        methodMetaData,
                        introductionIndex,
                        methodIndex,
                        m_weaveModel.getUuid());
            }
        }
    }

    /**
     * Creates a proxy method for the introduces method.
     *
     * @param es the extension set
     * @param cg the class gen
     * @param cpg the constant pool gen
     * @param factory the instruction objectfactory
     * @param methodMetaData the meta-data for the method
     * @param introductionIndex the introduction index
     * @param methodIndex the method index
     * @param uuid the uuid for the createWeaveModel model
     */
    private void createProxyMethod(final ExtensionSet es,
                                   final ClassGen cg,
                                   final ConstantPoolGen cpg,
                                   final InstructionFactory factory,
                                   final MethodMetaData methodMetaData,
                                   final int introductionIndex,
                                   final int methodIndex,
                                   final String uuid) {
        InstructionList il = new InstructionList();

        String methodName = methodMetaData.getName();
        String[] parameters = methodMetaData.getParameterTypes();
        String returnType = methodMetaData.getReturnType();
        String[] exceptionTypes = methodMetaData.getExceptionTypes();
        int modifiers = methodMetaData.getModifiers();

        final Type[] bcelParameterTypes = new Type[parameters.length];
        final String[] parameterNames = new String[parameters.length];

        final Type bcelReturnType = TransformationUtil.getBcelType(returnType);
        if (bcelReturnType == Type.NULL) return; // we have a constructor => skip

        for (int i = 0; i < parameters.length; i++) {
            bcelParameterTypes[i] = TransformationUtil.
                    getBcelType(parameters[i]);
            parameterNames[i] = "arg" + i;
        }

        final MethodGen methodGen = new MethodGen(
                modifiers,
                bcelReturnType,
                bcelParameterTypes,
                parameterNames,
                methodName,
                cg.getClassName(),
                il, cpg);

        if (isMethodStatic(methodMetaData)) return; // introductions can't be static (not for the moment at least)

        for (int i = 0; i < exceptionTypes.length; i++) {
            methodGen.addException(exceptionTypes[i]);
        }

        int idxParam = 1;
        int idxStack = 0;

        // if we have any parameters; wrap them up
        if (parameters.length != 0) {

            // create and allocate the parameters array
            il.append(new PUSH(cpg, parameters.length));
            il.append(factory.createNewArray(Type.OBJECT, (short)1));

            il.append(InstructionConstants.DUP);
            il.append(new PUSH(cpg, idxStack));
            idxStack++;

            // add all the parameters, wrap the primitive types in their object counterparts
            for (int count = 0; count < parameters.length; count++) {

                String wrapperClass = null;
                BasicType type = null;
                boolean hasLongOrDouble = false;

                if (bcelParameterTypes[count] instanceof ObjectType) {
                    // we have an object
                    il.append(factory.createLoad(Type.OBJECT, idxParam));
                    il.append(InstructionConstants.AASTORE);
                    idxParam++;
                }
                else if (bcelParameterTypes[count] instanceof BasicType) {
                    hasLongOrDouble = false;
                    // we have a primitive type
                    if ((bcelParameterTypes[count]).equals(Type.LONG)) {
                        wrapperClass = "java.lang.Long";
                        type = Type.LONG;
                        hasLongOrDouble = true;
                    }
                    else if ((bcelParameterTypes[count]).equals(Type.INT)) {
                        wrapperClass = "java.lang.Integer";
                        type = Type.INT;
                    }
                    else if ((bcelParameterTypes[count]).equals(Type.SHORT)) {
                        wrapperClass = "java.lang.Short";
                        type = Type.SHORT;
                    }
                    else if ((bcelParameterTypes[count]).equals(Type.DOUBLE)) {
                        wrapperClass = "java.lang.Double";
                        type = Type.DOUBLE;
                        hasLongOrDouble = true;
                    }
                    else if ((bcelParameterTypes[count]).equals(Type.FLOAT)) {
                        wrapperClass = "java.lang.Float";
                        type = Type.FLOAT;
                    }
                    else if ((bcelParameterTypes[count]).equals(Type.CHAR)) {
                        wrapperClass = "java.lang.Character";
                        type = Type.CHAR;
                    }
                    else if ((bcelParameterTypes[count]).equals(Type.BYTE)) {
                        wrapperClass = "java.lang.Byte";
                        type = Type.BYTE;
                    }
                    else if ((bcelParameterTypes[count]).equals(Type.BOOLEAN)) {
                        wrapperClass = "java.lang.Boolean";
                        type = Type.BOOLEAN;
                    }
                    else {
                        throw new RuntimeException("unknown parameter type: " + parameters[count]);
                    }
                    il.append(factory.createNew(wrapperClass));
                    il.append(InstructionConstants.DUP);
                    il.append(factory.createLoad(type, idxParam));
                    il.append(factory.createInvoke(
                            wrapperClass,
                            "<init>",
                            Type.VOID,
                            new Type[]{type},
                            Constants.INVOKESPECIAL));
                    il.append(InstructionConstants.AASTORE);
                    idxParam++;
                } // end handle basic or object type

                if (count != bcelParameterTypes.length - 1) {
                    // if we don't have the last parameter, create the parameter on the stack
                    il.append(InstructionConstants.DUP);
                    il.append(new PUSH(cpg, idxStack));
                    idxStack++;

                    // long or double needs two registers to fit
                    if (hasLongOrDouble) idxParam++;
                }
            }

            // create the object array
            il.append(factory.createStore(Type.OBJECT, idxParam));

            // get the aspectwerkz system
            il.append(new PUSH(cpg, uuid));
            il.append(factory.createInvoke(
                    TransformationUtil.ASPECT_WERKZ_CLASS,
                    "getSystem",
                    new ObjectType(TransformationUtil.ASPECT_WERKZ_CLASS),
                    new Type[]{Type.STRING},
                    Constants.INVOKESTATIC));

            // get the introduction
            il.append(new PUSH(cpg, introductionIndex));
            il.append(factory.createInvoke(
                    TransformationUtil.ASPECT_WERKZ_CLASS,
                    "getIntroduction",
                    new ObjectType(TransformationUtil.INTRODUCTION_CLASS),
                    new Type[]{Type.INT},
                    Constants.INVOKEVIRTUAL));

            il.append(new PUSH(cpg, methodIndex));

            il.append(factory.createLoad(Type.OBJECT, idxParam));
            il.append(factory.createLoad(Type.OBJECT, 0));

            il.append(factory.createInvoke(
                    TransformationUtil.INTRODUCTION_CLASS,
                    "invoke",
                    Type.OBJECT,
                    new Type[]{Type.INT, new ArrayType(Type.OBJECT, 1), Type.OBJECT},
                    Constants.INVOKEVIRTUAL));
        }
        else {
            // get the aspectwerkz system
            il.append(new PUSH(cpg, uuid));
            il.append(factory.createInvoke(
                    TransformationUtil.ASPECT_WERKZ_CLASS,
                    "getSystem",
                    new ObjectType(TransformationUtil.ASPECT_WERKZ_CLASS),
                    new Type[]{Type.STRING},
                    Constants.INVOKESTATIC));

            // no parameters
            il.append(new PUSH(cpg, introductionIndex));
            il.append(factory.createInvoke(
                    TransformationUtil.ASPECT_WERKZ_CLASS,
                    "getIntroduction",
                    new ObjectType(TransformationUtil.INTRODUCTION_CLASS),
                    new Type[]{Type.INT},
                    Constants.INVOKEVIRTUAL));

            il.append(new PUSH(cpg, methodIndex));
            il.append(factory.createLoad(Type.OBJECT, 0));

            il.append(factory.createInvoke(
                    TransformationUtil.INTRODUCTION_CLASS,
                    "invoke",
                    Type.OBJECT,
                    new Type[]{Type.INT, Type.OBJECT},
                    Constants.INVOKEVIRTUAL));
        }

        // take care of the return type
        if (!bcelReturnType.equals(Type.VOID)) {

            // cast the result and return it, if the return type is a
            // primitive type, retrieve it from the wrapped object first
            if (bcelReturnType instanceof BasicType) {
                if (bcelReturnType.equals(Type.LONG)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Long")));
                    il.append(factory.createInvoke(
                            "java.lang.Long",
                            "longValue",
                            Type.LONG,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (bcelReturnType.equals(Type.INT)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Integer")));
                    il.append(factory.createInvoke(
                            "java.lang.Integer",
                            "intValue",
                            Type.INT,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (bcelReturnType.equals(Type.SHORT)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Short")));
                    il.append(factory.createInvoke(
                            "java.lang.Short",
                            "shortValue",
                            Type.SHORT,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (bcelReturnType.equals(Type.DOUBLE)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Double")));
                    il.append(factory.createInvoke(
                            "java.lang.Double",
                            "doubleValue",
                            Type.DOUBLE,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (bcelReturnType.equals(Type.FLOAT)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Float")));
                    il.append(factory.createInvoke(
                            "java.lang.Float",
                            "floatValue",
                            Type.FLOAT,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (bcelReturnType.equals(Type.CHAR)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Character")));
                    il.append(factory.createInvoke(
                            "java.lang.Character",
                            "charValue",
                            Type.CHAR,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (bcelReturnType.equals(Type.BYTE)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Byte")));
                    il.append(factory.createInvoke(
                            "java.lang.Byte",
                            "byteValue",
                            Type.BYTE,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (bcelReturnType.equals(Type.BOOLEAN)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Boolean")));
                    il.append(factory.createInvoke(
                            "java.lang.Boolean",
                            "booleanValue",
                            Type.BOOLEAN,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (bcelReturnType.equals(Type.VOID)) {
                    // skip
                }
                else {
                    throw new Error("unknown return type: " + bcelReturnType);
                }
            }
            else {
                // cast the result to the right type
                il.append(factory.createCast(Type.OBJECT, bcelReturnType));
            }
        }
        il.append(factory.createReturn(bcelReturnType));

        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        es.addMethod(cg.getClassName(), methodGen.getMethod());
        il.dispose();
    }

    /**
     * Checks if a method is static or not.
     *
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    private boolean isMethodStatic(final MethodMetaData methodMetaData) {
        int modifiers = methodMetaData.getModifiers();
        if ((modifiers & Constants.ACC_STATIC) != 0) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param cg the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final ClassGen cg) {
        if (cg.isInterface()) {
            return true;
        }
        if (m_weaveModel.isAdvised(cg.getClassName()) &&
                m_weaveModel.hasIntroductions(cg.getClassName())) {
            return false;
        }
        return true;
    }
    ///CLOVER:ON
}

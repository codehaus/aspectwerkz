/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

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

import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.xmldef.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Adds an Introductions to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AddImplementationTransformer implements AspectWerkzInterfaceTransformerComponent {
    ///CLOVER:OFF

    /**
     * The references to the classes that have already been transformed.
     */
    private final Set m_transformed = new HashSet();

    /**
     * The definition.
     */
    private final AspectWerkzDefinition m_definition;

    /**
     * Retrieves the weave model.
     */
    public AddImplementationTransformer() {
        super();
        m_definition = AspectWerkzDefinition.getDefinitionForTransformation();
    }

    /**
     * Adds introductions to a class.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transformInterface(final Context context, final Klass klass) {
        final ClassGen cg = klass.getClassGen();
        if (classFilter(cg)) {
            return;
        }
        if (m_transformed.contains(cg.getClassName())) {
            return;
        }
        m_transformed.add(cg.getClassName());

        final ConstantPoolGen cpg = cg.getConstantPool();
        final InstructionFactory factory = new InstructionFactory(cg);
        addIntroductions(context, cg, cpg, factory);
    }

    /**
     * Adds introductions to the class.
     *
     * @param context the transformation context
     * @param cg the class gen
     * @param cpg the constant pool gen
     * @param factory the instruction objectfactory
     */
    private void addIntroductions(final Context context,
                                  final ClassGen cg,
                                  final ConstantPoolGen cpg,
                                  final InstructionFactory factory) {

        for (Iterator it = m_definition.getIntroductionNames(cg.getClassName()).iterator(); it.hasNext();) {

            String introductionName = (String)it.next();
            String introductionImplName = m_definition.getIntroductionImplName(introductionName);

            if (introductionImplName == null) {
                continue;
            }

            int introductionIndex = 0;
            List methodMetaDataList = Collections.synchronizedList(new ArrayList());
            try {
                introductionIndex = m_definition.getIntroductionIndex(introductionName);

                // get the method meta-data for the class
                boolean match = false;
                Map metaDataRepository = context.getMetaDataRepository();
                for (Iterator it2 = metaDataRepository.values().iterator(); it2.hasNext();) {
                    if (match) break;
                    Set metaDataSet = (Set)it2.next();
                    for (Iterator it3 = metaDataSet.iterator(); it3.hasNext();) {
                        ClassMetaData classMetaData = (ClassMetaData)it3.next();
                        if (classMetaData.getName().equals(introductionImplName)) {
                            methodMetaDataList = classMetaData.getMethods();
                            match = true;
                            break;
                        }
                    }
                }
                if (methodMetaDataList == null) {
                    throw new RuntimeException("no meta-data for introduction " + introductionImplName + " could be found in repository");
                }
            }
            catch (Exception e) {
                throw new DefinitionException("trying to weave introduction with null or empty string as name to class " + cg.getClassName() + ": definition file is not consistent");
            }

            if (methodMetaDataList == null) {
                continue; // interface introduction
            }

            // the iterator is on a list and the loop body does list.remove
            // which is forbidden
            List methodMetaDataListFiltered = new ArrayList();
            for (Iterator it2 = methodMetaDataList.iterator(); it2.hasNext();) {
                MethodMetaData methodMetaData = (MethodMetaData)it2.next();

                // remove the ___AW_getUuid, ___AW_getMetaData, ___AW_addMetaData and class$ methods
                // as well as some other methods before sorting the method list
                if (!(
                        methodMetaData.getName().equals("equals") ||
                        methodMetaData.getName().equals("hashCode") ||
                        methodMetaData.getName().equals("getClass") ||
                        methodMetaData.getName().equals("toString") ||
                        methodMetaData.getName().equals("wait") ||
                        methodMetaData.getName().equals("notify") ||
                        methodMetaData.getName().equals("notifyAll") ||
                        methodMetaData.getName().equals(
                                TransformationUtil.GET_UUID_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.GET_UUID_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.GET_META_DATA_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.SET_META_DATA_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.CLASS_LOOKUP_METHOD) ||
                        methodMetaData.getName().startsWith(
                                TransformationUtil.ORIGINAL_METHOD_PREFIX))) {
                    methodMetaDataListFiltered.add(methodMetaData);
                }
            }

            // sort the list so that we can enshure that the indexes are in synch
            // see AbstractIntroductionContainerStrategy#AbstractIntroductionContainerStrategy
            Collections.sort(methodMetaDataListFiltered, MethodComparator.
                    getInstance(MethodComparator.METHOD_META_DATA));

            int methodIndex = -1; // start with -1 since the method array is 0 indexed
            for (Iterator it2 = methodMetaDataListFiltered.iterator(); it2.hasNext();) {
                MethodMetaData methodMetaData = (MethodMetaData)it2.next();
                if (methodMetaData.getReturnType() == null || methodMetaData.getName().equals("<init>")) {
                    continue;
                }
                methodIndex++;
                createProxyMethod(
                        cg, cpg, factory,
                        methodMetaData,
                        introductionIndex,
                        methodIndex,
                        m_definition.getUuid());
            }
        }
    }

    /**
     * Creates a proxy method for the introduces method.
     *
     * @param cg the class gen
     * @param cpg the constant pool gen
     * @param factory the instruction objectfactory
     * @param methodMetaData the meta-data for the method
     * @param introductionIndex the introduction index
     * @param methodIndex the method index
     * @param uuid the uuid for the weave model
     */
    private void createProxyMethod(final ClassGen cg,
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

        final String[] parameterNames = new String[parameters.length];
        final Type[] bcelParameterTypes = new Type[parameters.length];
        final Type bcelReturnType = TransformationUtil.getBcelType(returnType);

        if (bcelReturnType == Type.NULL) {
            return; // we have a constructor => skip
        }

        for (int i = 0; i < parameters.length; i++) {
            bcelParameterTypes[i] = TransformationUtil.getBcelType(parameters[i]);
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

        if (isMethodStatic(methodMetaData)) {
            return; // introductions can't be static (not for the moment at least)
        }

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

                if (bcelParameterTypes[count] instanceof ObjectType
                        || bcelParameterTypes[count] instanceof ArrayType) {
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
                    ;// skip
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

        TransformationUtil.addMethod(cg, methodGen.getMethod());
        il.dispose();
    }

    /**
     * Checks if a method is static or not.
     *
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    private static boolean isMethodStatic(final MethodMetaData methodMetaData) {
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
        if (m_definition.inTransformationScope(cg.getClassName()) &&
                m_definition.hasIntroductions(cg.getClassName())) {
            return false;
        }
        return true;
    }

    /**
     * Callback method. Is being called before each transformation.
     */
    public void sessionStart() {
    }

    /**
     * Callback method. Is being called after each transformation.
     */
    public void sessionEnd() {
    }

    /**
     * Callback method. Prints a log/status message at
     * each transformation.
     *
     * @return a log string
     */
    public String verboseMessage() {
        return this.getClass().getName();
    }
    ///CLOVER:ON
}

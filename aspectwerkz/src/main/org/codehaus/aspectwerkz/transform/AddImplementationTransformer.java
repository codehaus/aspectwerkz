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
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.WeaveModel;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Adds an Introductions to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AddImplementationTransformer extends AspectWerkzAbstractInterfaceTransformer {
    ///CLOVER:OFF

    /**
     * Holds references to the classes that have already been transformed.
     */
    private final Set m_transformed = new HashSet();

    /**
     * Holds the weave model.
     */
    private final WeaveModel m_weaveModel;

    /**
     * Retrieves the weave model.
     */
    public AddImplementationTransformer() {
        super();
        List weaveModels = WeaveModel.loadModels();
        if (weaveModels.isEmpty()) {
            throw new RuntimeException("no weave model (online) or no classes to transform (offline) is specified");
        }
        if (weaveModels.size() > 1) {
            throw new RuntimeException("more than one weave model is specified, if you need more that one weave model you currently have to use the -offline mode and put each weave model on the classpath");
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
    public void transformInterface(final AspectWerkzExtensionSet es,
                                   final AspectWerkzUnextendableClassSet cs) {
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
    private void addIntroductions(final AspectWerkzExtensionSet es,
                                  final ClassGen cg,
                                  final ConstantPoolGen cpg,
                                  final InstructionFactory factory) {

        for (Iterator it = m_weaveModel.getIntroductionNames(cg.getClassName()).iterator();
             it.hasNext();) {

            String introductionName = (String)it.next();

            int introductionIndex = 0;
            List methodMetaDataList = null;
            try {
                introductionIndex = m_weaveModel.getIntroductionIndex(introductionName);
                methodMetaDataList = m_weaveModel.getIntroductionMethodsMetaData(introductionName);

// TODO: loading the class from the repository at runtime does not seem to work. Load the classes needed (the introductions) in the class preprocessor and then pass it to the transformers.
//                String className = m_weaveModel.getIntroductionImplementationName(introductionName);
//                JavaClass klass = Repository.getRepository().loadClass(className);
//                ClassMetaData introductionMetaData = BcelMetaDataMaker.createClassMetaData(klass);
//                methodMetaDataList = introductionMetaData.getMethods();
            }
            catch (Exception e) {
                System.out.println("e = " + e);
                throw new DefinitionException("trying to weave introduction with null or empty string as name to class " + cg.getClassName() + ": definition file is not consistent");
            }

            if (methodMetaDataList == null) {
                continue; // interface introduction
            }

            for (Iterator it2 = methodMetaDataList.iterator(); it2.hasNext();) {
                MethodMetaData methodMetaData = (MethodMetaData)it2.next();

                // remove the ___AW_getUuid, ___AW_getMetaData, ___AW_addMetaData and class$ methods
                // as well as the added proxy methods before sorting the method list
                if (methodMetaData.getName().equals(
                        TransformationUtil.GET_UUID_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.GET_META_DATA_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.SET_META_DATA_METHOD) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.ORIGINAL_METHOD_PREFIX) ||
                        methodMetaData.getName().equals(
                                TransformationUtil.CLASS_LOOKUP_METHOD)) {
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
     * @param uuid the uuid for the weave model
     */
    private void createProxyMethod(final AspectWerkzExtensionSet es,
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

        es.addMethod(cg, methodGen.getMethod());
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
        if (m_weaveModel.inTransformationScope(cg.getClassName()) &&
                m_weaveModel.hasIntroductions(cg.getClassName())) {
            return false;
        }
        return true;
    }
    ///CLOVER:ON
}

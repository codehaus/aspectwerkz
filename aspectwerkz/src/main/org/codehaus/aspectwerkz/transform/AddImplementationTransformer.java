/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;

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

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;

/**
 * Adds an Introductions to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AddImplementationTransformer implements AspectWerkzInterfaceTransformerComponent {

    /**
     * The references to the classes that have already been transformed.
     */
    //private final Set m_transformed = new HashSet();

    /**
     * The definitions.
     */
    private final List m_definitions;

    /**
     * Retrieves the weave model.
     */
    public AddImplementationTransformer() {
        super();

        m_definitions = DefinitionLoader.getDefinitionsForTransformation();
    }

    /**
     * Adds introductions to a class.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transformInterface(final Context context, final Klass klass) {

        // loop over all the definitions
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            AspectWerkzDefinition definition = (AspectWerkzDefinition)it.next();

            definition.loadAspects(context.getLoader());

            final ClassGen cg = klass.getClassGen();
            ClassMetaData classMetaData = BcelMetaDataMaker.createClassMetaData(context.getJavaClass(cg));
            if (classFilter(cg, classMetaData, definition)) {
                return;
            }
            //todo: what is this cache for ? not compliant for 0.10
            //if (m_transformed.contains(cg.getClassName())) {
            //    return;
            //}
            //m_transformed.add(cg.getClassName());

            ConstantPoolGen cpg = cg.getConstantPool();
            InstructionFactory factory = new InstructionFactory(cg);

            if (definition.isAttribDef()) {
                org.codehaus.aspectwerkz.attribdef.transform.IntroductionTransformer.addMethodIntroductions(
                        definition, context, classMetaData, cg, cpg, factory, this
                );
            }
            else if (definition.isXmlDef()) {
                org.codehaus.aspectwerkz.xmldef.transform.IntroductionTransformer.addMethodIntroductions(
                        definition, context, cg, cpg, factory, this
                );
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
     * @param mixinIndex the mixin index
     * @param methodIndex the method index
     * @param uuid the uuid for the weave model
     */
    public void createProxyMethod(final ClassGen cg,
                                  final ConstantPoolGen cpg,
                                  final InstructionFactory factory,
                                  final MethodMetaData methodMetaData,
                                  final int mixinIndex,
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
                    TransformationUtil.SYSTEM_LOADER_CLASS,
                    TransformationUtil.RETRIEVE_SYSTEM_METHOD,
                    new ObjectType(TransformationUtil.SYSTEM_CLASS),
                    new Type[]{Type.STRING},
                    Constants.INVOKESTATIC));

            // get the mixin
            il.append(new PUSH(cpg, mixinIndex));
            il.append(factory.createInvoke(
                    TransformationUtil.SYSTEM_CLASS,
                    TransformationUtil.RETRIEVE_MIXIN_METHOD,
                    new ObjectType(TransformationUtil.MIXIN_CLASS),
                    new Type[]{Type.INT},
                    Constants.INVOKEINTERFACE));

            il.append(new PUSH(cpg, methodIndex));

            il.append(factory.createLoad(Type.OBJECT, idxParam));
            il.append(factory.createLoad(Type.OBJECT, 0));

            il.append(factory.createInvoke(
                    TransformationUtil.MIXIN_CLASS,
                    TransformationUtil.INVOKE_MIXIN_METHOD,
                    Type.OBJECT,
                    new Type[]{Type.INT, new ArrayType(Type.OBJECT, 1), Type.OBJECT},
                    Constants.INVOKEINTERFACE));
        }
        else {
            // get the aspectwerkz system
            il.append(new PUSH(cpg, uuid));
            il.append(factory.createInvoke(
                    TransformationUtil.SYSTEM_LOADER_CLASS,
                    TransformationUtil.RETRIEVE_SYSTEM_METHOD,
                    new ObjectType(TransformationUtil.SYSTEM_CLASS),
                    new Type[]{Type.STRING},
                    Constants.INVOKESTATIC));

            // no parameters
            il.append(new PUSH(cpg, mixinIndex));
            il.append(factory.createInvoke(
                    TransformationUtil.SYSTEM_CLASS,
                    TransformationUtil.RETRIEVE_MIXIN_METHOD,
                    new ObjectType(TransformationUtil.MIXIN_CLASS),
                    new Type[]{Type.INT},
                    Constants.INVOKEINTERFACE));

            il.append(new PUSH(cpg, methodIndex));
            il.append(factory.createLoad(Type.OBJECT, 0));

            il.append(factory.createInvoke(
                    TransformationUtil.MIXIN_CLASS,
                    TransformationUtil.INVOKE_MIXIN_METHOD,
                    Type.OBJECT,
                    new Type[]{Type.INT, Type.OBJECT},
                    Constants.INVOKEINTERFACE));
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
     * @param classMetaData the class meta-data
     * @param definition the definition
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final ClassGen cg,
                                final ClassMetaData classMetaData,
                                final AspectWerkzDefinition definition) {
        if (cg.isInterface()) {
            return true;
        }
        String className = cg.getClassName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (definition.inIncludePackage(className) &&
                definition.hasIntroductions(classMetaData)) {
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
}

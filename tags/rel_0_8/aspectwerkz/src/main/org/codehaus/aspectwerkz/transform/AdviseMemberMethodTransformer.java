/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

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
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.JavaClass;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Transforms member methods to become "aspect-aware".
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviseMemberMethodTransformer implements AspectWerkzCodeTransformerComponent {
    ///CLOVER:OFF

    /**
     * The definition.
     */
    private final AspectWerkzDefinition m_definition;

    /**
     * Retrieves the weave model.
     */
    public AdviseMemberMethodTransformer() {
        super();
        m_definition = AspectWerkzDefinition.getDefinitionForTransformation();
    }

    /**
     * Makes the member method transformations.
     *
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transformCode(final Context context, final Klass klass) {

        final ClassGen cg = klass.getClassGen();
        ClassMetaData classMetaData =
                BcelMetaDataMaker.createClassMetaData(context.getJavaClass(cg));

        if (classFilter(classMetaData, cg)) {
            return;
        }

        final InstructionFactory factory = new InstructionFactory(cg);
        final ConstantPoolGen cpg = cg.getConstantPool();
        final Method[] methods = cg.getMethods();

        // get the indexes for the <init> methods
        List initIndexes = new ArrayList();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("<init>")) {
                initIndexes.add(new Integer(i));
            }
        }

        // build and sort the method lookup list
        final List methodLookupList = new ArrayList();
        for (int i = 0; i < methods.length; i++) {
            if (methodFilter(classMetaData, methods[i]) == null) {
                continue;
            }
            methodLookupList.add(methods[i]);
        }

        Collections.sort(methodLookupList, BCELMethodComparator.getInstance());

        final Map methodSequences = new HashMap();
        final List proxyMethods = new ArrayList();
        for (int i = 0; i < methods.length; i++) {

            // filter the methods
            String uuid = methodFilter(classMetaData, methods[i]);
            if (methods[i].isStatic() || uuid == null) {
                continue;
            }

            final MethodGen mg = new MethodGen(methods[i], cg.getClassName(), cpg);

            // take care of identification of overloaded methods by inserting a sequence number
            if (methodSequences.containsKey(methods[i].getName())) {
                int sequence = ((Integer)methodSequences.get(methods[i].getName())).intValue();
                methodSequences.remove(methods[i].getName());
                sequence++;
                methodSequences.put(methods[i].getName(), new Integer(sequence));
            }
            else {
                methodSequences.put(methods[i].getName(), new Integer(1));
            }

            final int methodLookupId = methodLookupList.indexOf(methods[i]);
            final int methodSequence = ((Integer)methodSequences.
                    get(methods[i].getName())).intValue();

            handleCallToOverriddenSuperClassMethod(mg, cg, cpg, factory, methodSequence, context);

            // check if the pointcut should be deployed as thread safe or not

            addJoinPointField(cpg, cg, mg, methodSequence);

            // get the join point controller
            MethodMetaData methodMetaData = BcelMetaDataMaker.createMethodMetaData(methods[i]);

            final String controllerClassName =
                    m_definition.getJoinPointController(classMetaData, methodMetaData);

            // advise all the constructors
            for (Iterator it = initIndexes.iterator(); it.hasNext();) {
                final int initIndex = ((Integer)it.next()).intValue();

                methods[initIndex] = createJoinPointField(
                        cpg, cg,
                        methods[initIndex],
                        methods[i],
                        factory,
                        methodSequence
                ).getMethod();
            }

            proxyMethods.add(createProxyMethod(
                    cpg, cg, mg,
                    factory,
                    methodLookupId,
                    methodSequence,
                    methods[i].getAccessFlags(),
                    uuid,
                    controllerClassName
            ));

            methods[i] = addPrefixToMethod(mg, methods[i], methodSequence);

            mg.setMaxStack();
        }

        // update the old methods
        cg.setMethods(methods);

        // add the proxy methods
        for (Iterator it = proxyMethods.iterator(); it.hasNext();) {
            Method method = (Method)it.next();
            cg.addMethod(method);
        }
    }

    /**
     * Searches for a invocation to the super class' method that the current
     * method has overridden.
     *
     * @todo make this feature configurable per class in the definition
     *
     * @param mg the method gen
     * @param cg the class gen
     * @param cpg the constant pool gen
     * @param factory the instruction factory
     * @param methodSequence the sequence number for the method
     * @param context the context
     */
    private void handleCallToOverriddenSuperClassMethod(final MethodGen mg,
                                                        final ClassGen cg,
                                                        final ConstantPoolGen cpg,
                                                        final InstructionFactory factory,
                                                        final int methodSequence,
                                                        final Context context) {
        InstructionList il = mg.getInstructionList();
        if (il == null) return;

        InstructionHandle ih = il.getStart();
        while (ih != null) {
            Instruction ins = ih.getInstruction();

            // TODO: are both INVOKESPECIAL and INVOKEVIRTUAL needed?
            if (ins instanceof INVOKESPECIAL || ins instanceof INVOKEVIRTUAL) {

                InvokeInstruction invokeInstruction = (InvokeInstruction)ins;

                // get the method name and class name of the method being invoked
                String methodName = invokeInstruction.getName(cpg);
                String className = invokeInstruction.getClassName(cpg);
                String signature = invokeInstruction.getSignature(cpg);
                String superClassName = cg.getSuperclassName();

                if (methodName.equals(mg.getMethod().getName()) &&
                        className.equals(superClassName)) {
                    JavaClass superClass = context.getSuperClass(cg);
                    Method[] methods = superClass.getMethods();
                    MethodMetaData methodMetaData = null;
                    for (int i = 0; i < methods.length; i++) {
                        Method method = methods[i];
                        if (method.getSignature().equals(signature) && method.getName().equals(methodName)) {
                            methodMetaData = BcelMetaDataMaker.createMethodMetaData(method);
                            break;
                        }
                    }
                    if (methodMetaData == null) {
                        return;
                    }

                    ClassMetaData classMetaData = BcelMetaDataMaker.createClassMetaData(superClass);

                    if (m_definition.hasMethodPointcut(classMetaData, methodMetaData)) {
                        StringBuffer prefixedOriginalMethod = new StringBuffer();
                        prefixedOriginalMethod.append(TransformationUtil.ORIGINAL_METHOD_PREFIX);
                        prefixedOriginalMethod.append(methodName);
                        prefixedOriginalMethod.append(TransformationUtil.DELIMITER);
                        prefixedOriginalMethod.append(methodSequence);

                        ih.swapInstruction(factory.createInvoke(
                                superClassName,
                                prefixedOriginalMethod.toString(),
                                mg.getReturnType(),
                                mg.getArgumentTypes(),
                                Constants.INVOKESPECIAL)
                        );
                    }
                }
            }
            ih = ih.getNext();
        }
    }

    /**
     * Adds a join point member field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param mg the MethodGen
     * @param methodSequence the methods sequence number
     */
    private void addJoinPointField(final ConstantPoolGen cp,
                                   final ClassGen cg,
                                   final MethodGen mg,
                                   final int methodSequence) {

        final StringBuffer joinPoint = getJoinPointName(mg.getMethod(), methodSequence);

        if (cg.containsField(joinPoint.toString()) != null) {
            return;
        }

        final FieldGen field = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_FINAL,
                new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                joinPoint.toString(),
                cp
        );
        cg.addField(field.getField());
    }

    /**
     * Transforms the init method to create the newly added join point member field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param init the constructor for the class
     * @param method the current method
     * @param factory the objectfactory
     * @param methodSequence the methods sequence number
     * @return the modified constructor
     */
    private MethodGen createJoinPointField(final ConstantPoolGen cp,
                                           final ClassGen cg,
                                           final Method init,
                                           final Method method,
                                           final InstructionFactory factory,
                                           final int methodSequence) {

        final MethodGen mg = new MethodGen(init, cg.getClassName(), cp);
        final InstructionList il = mg.getInstructionList();
        final InstructionHandle[] ihs = il.getInstructionHandles();

        // grab the handle to the the call to this(..) or super(..) (if is one),
        // otherwise grab the handle to the beginning of the constructor
        InstructionHandle ih = ihs[0];
        for (int i = 0; i < ihs.length; i++) {
            Instruction instruction = ihs[i].getInstruction();
            if (instruction instanceof InvokeInstruction) {
                InvokeInstruction invokeInstruction = (InvokeInstruction)instruction;
                String methodName = invokeInstruction.getMethodName(cp);
                if (methodName.equals("<init>")) {
                    i++; // step over the call to be able to insert *after* the call
                    ih = ihs[i]; // set the instruction handle to the super/this call
                    break;
                }
            }
        }

        // insert the join point initializations after the call to this(..) or super(..)
        // or in the beginning of the constructor
        final StringBuffer joinPoint = getJoinPointName(method, methodSequence);

        final InstructionHandle ihPost;
        ihPost = il.insert(ih, factory.createLoad(Type.OBJECT, 0));
        il.insert(ih, factory.createNew(TransformationUtil.THREAD_LOCAL_CLASS));

        il.insert(ih, InstructionConstants.DUP);

        il.insert(ih, factory.createInvoke(
                TransformationUtil.THREAD_LOCAL_CLASS,
                "<init>",
                Type.VOID,
                new Type[]{},
                Constants.INVOKESPECIAL
        ));

        il.insert(ih, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                Constants.PUTFIELD
        ));
        il.redirectBranches(ih, ihPost);

        mg.setMaxStack();
        mg.setMaxLocals();
        return mg;
    }

    /**
     * Adds a prefix to the original method.
     * To make it callable only from within the framework itself.
     *
     * @param mg the MethodGen
     * @param method the current method
     * @param methodSequence the methods sequence number
     * @return the modified method
     */
    private Method addPrefixToMethod(final MethodGen mg,
                                     final Method method,
                                     final int methodSequence) {

        // change the method access flags (should always be set to protected)
        int accessFlags = mg.getAccessFlags();
        if ((accessFlags & Constants.ACC_PROTECTED) == 0) {
            // set the protected flag
            accessFlags |= Constants.ACC_PROTECTED;
        }
        if ((accessFlags & Constants.ACC_PRIVATE) != 0) {
            // clear the private flag
            accessFlags &= ~Constants.ACC_PRIVATE;
        }
        if ((accessFlags & Constants.ACC_PUBLIC) != 0) {
            // clear the public flag
            accessFlags &= ~Constants.ACC_PUBLIC;
        }

        mg.setName(getPrefixedMethodName(method, methodSequence).toString());
        mg.setAccessFlags(accessFlags);
        mg.setMaxStack();
        mg.setMaxLocals();
        return mg.getMethod();
    }

    /**
     * Creates a proxy method for the original method specified.
     * This method has the same signature as the original method and
     * catches the invocation for further processing by the framework
     * before redirecting to the original method.
     *
     * @todo pass the 'class' as a Class instance not a String to the join point. Add the class field to the class using BCEL (see AdviseStaticMethodTransformer.java)
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param originalMethod the current method
     * @param factory the objectfactory
     * @param methodId the id of the current method in the lookup tabl
     * @param methodSequence the methods sequence number
     * @param accessFlags the access flags of the original method
     * @param uuid the uuid for the weave model defining the pointcut
     * @param controllerClassName the class name of the controller class to use
     * @return the proxy method
     */
    private Method createProxyMethod(final ConstantPoolGen cp,
                                     final ClassGen cg,
                                     final MethodGen originalMethod,
                                     final InstructionFactory factory,
                                     final int methodId,
                                     final int methodSequence,
                                     final int accessFlags,
                                     final String uuid,
                                     final String controllerClassName) {

        InstructionList il = new InstructionList();

        StringBuffer joinPoint = getJoinPointName(
                originalMethod.getMethod(), methodSequence);

        final MethodGen method = new MethodGen(
                accessFlags,
                Type.getReturnType(originalMethod.getSignature()),
                Type.getArgumentTypes(originalMethod.getSignature()),
                originalMethod.getArgumentNames(),
                originalMethod.getName(),
                cg.getClassName(),
                il, cp
        );

        String[] exceptions = originalMethod.getExceptions();
        for (int i = 0; i < exceptions.length; i++) {
            method.addException(exceptions[i]);
        }

        int indexParam = 1;
        int indexStack = 0;
        int indexJoinPoint = Type.getArgumentTypes(
                originalMethod.getSignature()).length * 2 + 1;

        BranchInstruction biIfNotNull = null;
        InstructionHandle ihIfNotNull = null;
        // Object joinPoint = ___jp.get();
        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                Constants.GETFIELD
        ));
        il.append(factory.createInvoke(
                TransformationUtil.THREAD_LOCAL_CLASS,
                "get",
                Type.OBJECT,
                Type.NO_ARGS,
                Constants.INVOKEVIRTUAL
        ));
        il.append(factory.createStore(Type.OBJECT, indexJoinPoint));

        // if (joinPoint == null) {
        il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
        biIfNotNull = factory.createBranchInstruction(Constants.IFNONNULL, null);
        il.append(biIfNotNull);

        // joinPoint = new MemberMethodJoinPoint(uuid, this, "foo.bar.Baz", 10);
        il.append(factory.createNew(TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS));
        il.append(InstructionConstants.DUP);

        il.append(new PUSH(cp, uuid));
        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(new PUSH(cp, cg.getClassName()));
        il.append(new PUSH(cp, methodId));
        il.append(new PUSH(cp, controllerClassName));

        il.append(factory.createInvoke(
                TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS,
                "<init>",
                Type.VOID,
                new Type[]{Type.STRING, Type.OBJECT, Type.STRING, Type.INT, Type.STRING},
                Constants.INVOKESPECIAL
        ));
        il.append(factory.createStore(Type.OBJECT, indexJoinPoint));

        // ___jp.set(joinPoint);
        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                Constants.GETFIELD
        ));
        il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
        il.append(factory.createInvoke(
                TransformationUtil.THREAD_LOCAL_CLASS,
                "set",
                Type.VOID,
                new Type[]{Type.OBJECT},
                Constants.INVOKEVIRTUAL
        ));

        ihIfNotNull = il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
        il.append(factory.createCheckCast(TransformationUtil.MEMBER_METHOD_JOIN_POINT_TYPE));
        indexJoinPoint += 2;
        il.append(factory.createStore(Type.OBJECT, indexJoinPoint));

        biIfNotNull.setTarget(ihIfNotNull);

        // if we have parameters, wrap them up
        if (Type.getArgumentTypes(originalMethod.getSignature()).length != 0) {

            // create and allocate the parameters array
            il.append(new PUSH(cp, Type.getArgumentTypes(originalMethod.getSignature()).length));
            il.append(factory.createNewArray(Type.OBJECT, (short)1));

            il.append(InstructionConstants.DUP);
            il.append(new PUSH(cp, indexStack));
            indexStack++;

            // add all the parameters, wrap the primitive types in their object counterparts
            for (int count = 0; count < Type.getArgumentTypes(originalMethod.getSignature()).length; count++) {

                String wrapperClass = null;
                BasicType type = null;
                boolean hasLongOrDouble = false;

                if (Type.getArgumentTypes(originalMethod.getSignature())[count] instanceof ObjectType ||
                        Type.getArgumentTypes(originalMethod.getSignature())[count] instanceof ArrayType) {
                    // we have an object or an array
                    il.append(factory.createLoad(Type.OBJECT, indexParam));
                    il.append(InstructionConstants.AASTORE);
                    indexParam++;
                }
                else if (Type.getArgumentTypes(originalMethod.getSignature())[count] instanceof ArrayType) {
                    // we have an array
                    il.append(factory.createLoad(Type.OBJECT, indexParam));
                    il.append(InstructionConstants.AASTORE);
                    indexParam++;
                }
                else if (Type.getArgumentTypes(originalMethod.getSignature())[count] instanceof BasicType) {
                    hasLongOrDouble = false;
                    // we have a primitive type
                    if ((Type.getArgumentTypes(originalMethod.getSignature())[count]).equals(Type.LONG)) {
                        wrapperClass = "java.lang.Long";
                        type = Type.LONG;
                        hasLongOrDouble = true;
                    }
                    else if ((Type.getArgumentTypes(originalMethod.getSignature())[count]).equals(Type.INT)) {
                        wrapperClass = "java.lang.Integer";
                        type = Type.INT;
                    }
                    else if ((Type.getArgumentTypes(originalMethod.getSignature())[count]).equals(Type.SHORT)) {
                        wrapperClass = "java.lang.Short";
                        type = Type.SHORT;
                    }
                    else if ((Type.getArgumentTypes(originalMethod.getSignature())[count]).equals(Type.DOUBLE)) {
                        wrapperClass = "java.lang.Double";
                        type = Type.DOUBLE;
                        hasLongOrDouble = true;
                    }
                    else if ((Type.getArgumentTypes(originalMethod.getSignature())[count]).equals(Type.FLOAT)) {
                        wrapperClass = "java.lang.Float";
                        type = Type.FLOAT;
                    }
                    else if ((Type.getArgumentTypes(originalMethod.getSignature())[count]).equals(Type.CHAR)) {
                        wrapperClass = "java.lang.Character";
                        type = Type.CHAR;
                    }
                    else if ((Type.getArgumentTypes(originalMethod.getSignature())[count]).equals(Type.BYTE)) {
                        wrapperClass = "java.lang.Byte";
                        type = Type.BYTE;
                    }
                    else if ((Type.getArgumentTypes(originalMethod.getSignature())[count]).equals(Type.BOOLEAN)) {
                        wrapperClass = "java.lang.Boolean";
                        type = Type.BOOLEAN;
                    }
                    else {
                        throw new RuntimeException("unknown parameter type: " + Type.getArgumentTypes(originalMethod.getSignature())[count]);
                    }
                    il.append(factory.createNew(wrapperClass));
                    il.append(InstructionConstants.DUP);
                    il.append(factory.createLoad(type, indexParam));
                    il.append(factory.createInvoke(
                            wrapperClass,
                            "<init>",
                            Type.VOID,
                            new Type[]{type},
                            Constants.INVOKESPECIAL
                    ));
                    il.append(InstructionConstants.AASTORE);
                    indexParam++;
                } // end handle basic or object type

                if (count != Type.getArgumentTypes(originalMethod.getSignature()).length - 1) {
                    // if we don't have the last parameter, create the parameter on the stack
                    il.append(InstructionConstants.DUP);
                    il.append(new PUSH(cp, indexStack));
                    indexStack++;

                    // long or double needs two registers to fit
                    if (hasLongOrDouble) indexParam++;
                }
            }

            // create the object array
            il.append(factory.createStore(Type.OBJECT, indexParam));

            // if threadsafe grab the newly retrieved local join point field from the stack
            il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));

            // invoke joinPoint.setParameter(..)
            il.append(factory.createLoad(Type.OBJECT, indexParam));
            il.append(factory.createInvoke(
                    TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS,
                    "setParameters",
                    Type.VOID,
                    new Type[]{new ArrayType(Type.OBJECT, 1)},
                    Constants.INVOKEVIRTUAL
            ));
            indexParam++;

        } // end - if parameters.length != 0

        // if threadsafe grab the newly retrieved local join point field from the stack
        il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
        il.append(factory.createInvoke(
                TransformationUtil.MEMBER_METHOD_JOIN_POINT_CLASS,
                "proceed",
                Type.OBJECT,
                Type.NO_ARGS,
                Constants.INVOKEVIRTUAL
        ));

        if (!Type.getReturnType(originalMethod.getSignature()).equals(Type.VOID)) {
            // create the result from the invocation
            il.append(factory.createStore(Type.OBJECT, indexParam));
            il.append(factory.createLoad(Type.OBJECT, indexParam));

            // cast the result and return it, if the return type is a
            // primitive type, retrieve it from the wrapped object first
            if (Type.getReturnType(originalMethod.getSignature()) instanceof BasicType) {
                if (Type.getReturnType(originalMethod.getSignature()).equals(Type.LONG)) {
                    il.append(factory.createCheckCast(new ObjectType("java.lang.Long")));
                    il.append(factory.createInvoke(
                            "java.lang.Long",
                            "longValue",
                            Type.LONG,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL
                    ));
                }
                else if (Type.getReturnType(originalMethod.getSignature()).equals(Type.INT)) {
                    il.append(factory.createCheckCast(new ObjectType("java.lang.Integer")));
                    il.append(factory.createInvoke(
                            "java.lang.Integer",
                            "intValue",
                            Type.INT,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL
                    ));
                }
                else if (Type.getReturnType(originalMethod.getSignature()).equals(Type.SHORT)) {
                    il.append(factory.createCheckCast(new ObjectType("java.lang.Short")));
                    il.append(factory.createInvoke(
                            "java.lang.Short",
                            "shortValue",
                            Type.SHORT,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL
                    ));
                }
                else if (Type.getReturnType(originalMethod.getSignature()).equals(Type.DOUBLE)) {
                    il.append(factory.createCheckCast(new ObjectType("java.lang.Double")));
                    il.append(factory.createInvoke(
                            "java.lang.Double",
                            "doubleValue",
                            Type.DOUBLE,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL
                    ));
                }
                else if (Type.getReturnType(originalMethod.getSignature()).equals(Type.FLOAT)) {
                    il.append(factory.createCheckCast(new ObjectType("java.lang.Float")));
                    il.append(factory.createInvoke(
                            "java.lang.Float",
                            "floatValue",
                            Type.FLOAT,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL
                    ));
                }
                else if (Type.getReturnType(originalMethod.getSignature()).equals(Type.CHAR)) {
                    il.append(factory.createCheckCast(new ObjectType("java.lang.Character")));
                    il.append(factory.createInvoke(
                            "java.lang.Character",
                            "charValue",
                            Type.CHAR,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL
                    ));
                }
                else if (Type.getReturnType(originalMethod.getSignature()).equals(Type.BYTE)) {
                    il.append(factory.createCheckCast(new ObjectType("java.lang.Byte")));
                    il.append(factory.createInvoke(
                            "java.lang.Byte",
                            "byteValue",
                            Type.BYTE,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL
                    ));
                }
                else if (Type.getReturnType(originalMethod.getSignature()).equals(Type.BOOLEAN)) {
                    il.append(factory.createCheckCast(new ObjectType("java.lang.Boolean")));
                    il.append(factory.createInvoke(
                            "java.lang.Boolean",
                            "booleanValue",
                            Type.BOOLEAN,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL
                    ));
                }
                else if (Type.getReturnType(originalMethod.getSignature()).equals(Type.VOID)) {
                    ;// skip
                }
                else {
                    throw new Error("unknown return type: " + Type.getReturnType(originalMethod.getSignature()));
                }
            }
            else {
                // cast the result to the right type
                il.append(factory.createCast(Type.OBJECT, Type.getReturnType(originalMethod.getSignature())));
            }
        }
        il.append(factory.createReturn(Type.getReturnType(originalMethod.getSignature())));

        method.setMaxStack();
        method.setMaxLocals();
        return method.getMethod();
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
     * Callback method. Prints a log/status message at each transformation.
     *
     * @return a log string
     */
    public String verboseMessage() {
        return this.getClass().getName();
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param classMetaData the meta-data for the class
     * @param cg the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final ClassMetaData classMetaData, final ClassGen cg) {
        if (cg.isInterface() ||
                cg.getSuperclassName().equals("org.codehaus.aspectwerkz.advice.AroundAdvice") ||
                cg.getSuperclassName().equals("org.codehaus.aspectwerkz.advice.PreAdvice") ||
                cg.getSuperclassName().equals("org.codehaus.aspectwerkz.advice.PostAdvice")) {
            return true;
        }
        if (!m_definition.inTransformationScope(cg.getClassName())) {
            return true;
        }
        if (m_definition.hasMethodPointcut(classMetaData) ||
                m_definition.hasThrowsPointcut(classMetaData)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the methods to be transformed.
     *
     * @param classMetaData the class meta-data
     * @param method the method to filter
     * @return the UUID for the weave model
     */
    private String methodFilter(final ClassMetaData classMetaData, final Method method) {
        String uuid = null;
        if (method.isAbstract() ||
                method.getName().equals("<init>") ||
                method.getName().equals("<clinit>") ||
                method.getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX) ||
                method.getName().equals(TransformationUtil.GET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.SET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
                method.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            uuid = null;
        }
        else {
            MethodMetaData methodMetaData = BcelMetaDataMaker.createMethodMetaData(method);
            if (m_definition.hasMethodPointcut(classMetaData, methodMetaData)) {
                uuid = m_definition.getUuid();
            }
            if (m_definition.hasThrowsPointcut(classMetaData, methodMetaData)) {
                uuid = m_definition.getUuid();
            }
        }
        return uuid;
    }

    /**
     * Returns the name of the join point.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @return the name of the join point
     */
    private static StringBuffer getJoinPointName(final Method method,
                                                 final int methodSequence) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(TransformationUtil.MEMBER_METHOD_JOIN_POINT_PREFIX);
        joinPoint.append(method.getName());
        joinPoint.append(TransformationUtil.DELIMITER);
        joinPoint.append(methodSequence);
        return joinPoint;
    }

    /**
     * Returns the prefixed method name.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @return the name of the join point
     */
    private StringBuffer getPrefixedMethodName(final Method method,
                                               final int methodSequence) {
        final StringBuffer methodName = new StringBuffer();
        methodName.append(TransformationUtil.ORIGINAL_METHOD_PREFIX);
        methodName.append(method.getName());
        methodName.append(TransformationUtil.DELIMITER);
        methodName.append(methodSequence);
        return methodName;
    }
///CLOVER:ON
}

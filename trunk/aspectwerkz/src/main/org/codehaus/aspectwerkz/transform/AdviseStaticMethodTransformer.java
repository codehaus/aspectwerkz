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
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Field;

import org.codehaus.aspectwerkz.metadata.WeaveModel;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;

/**
 * Transforms static methods to become "aspect-aware".
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviseStaticMethodTransformer implements AspectWerkzCodeTransformerComponent {
    ///CLOVER:OFF

    /**
     * Holds the weave model.
     */
    private final WeaveModel m_weaveModel;

    /**
     * Retrieves the weave model.
     */
    public AdviseStaticMethodTransformer() {
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
     * Makes the static method transformations.
     *
     * @todo refactor so that we don't have to loop over all the methods twice (and create a method meta-data object twice)
     * @todo remove all thread-safe stuff
     *
     * @param cs the class set.
     */
    public void transformCode(final AspectWerkzUnextendableClassSet cs) {
        final Iterator iterator = cs.getIteratorForTransformableClasses();
        while (iterator.hasNext()) {
            final ClassGen cg = (ClassGen)iterator.next();

            if (classFilter(cg)) {
                continue;
            }

            //@todo alex
            org.apache.bcel.classfile.JavaClass alex = cg.getJavaClass();
            alex.setRepository(new org.apache.bcel.util.ClassLoaderRepository(AspectWerkzPreProcessor.alexContextGet()));
            ClassMetaData classMetaData = BcelMetaDataMaker.createClassMetaData(alex);
            //ClassMetaData classMetaData = BcelMetaDataMaker.createClassMetaData(cg.getJavaClass());

            final InstructionFactory factory = new InstructionFactory(cg);
            final ConstantPoolGen cpg = cg.getConstantPool();
            final Method[] methods = cg.getMethods();

            // get the index for the <clinit> method (if there is one)
            boolean noClinitMethod = true;
            int indexClinit = -1;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("<clinit>")) {
                    indexClinit = i;
                    noClinitMethod = false;
                    break;
                }
            }

            // build and sort the method lookup list
            final List methodLookupList = new ArrayList();
            for (int i = 0; i < methods.length; i++) {
                MethodMetaData methodMetaData = BcelMetaDataMaker.createMethodMetaData(methods[i]);
                if (methodFilter(classMetaData, methodMetaData, methods[i]) == null) {
                    continue;
                }
                methodLookupList.add(methods[i]);
            }
            Collections.sort(methodLookupList, BCELMethodComparator.getInstance());

            final Map methodSequences = new HashMap();
            final List newMethods = new ArrayList();
            Method clInitMethod = null;
            for (int i = 0; i < methods.length; i++) {

                MethodMetaData methodMetaData = BcelMetaDataMaker.createMethodMetaData(methods[i]);

                String uuid = methodFilter(classMetaData, methodMetaData, methods[i]);
                if (!methods[i].isStatic() || uuid == null) {
                    continue;
                }

                final MethodGen mg = new MethodGen(methods[i], cg.getClassName(), cpg);

                // take care of identification of overloaded methods by
                // inserting a sequence number
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
                final int methodSequence =
                        ((Integer)methodSequences.get(methods[i].getName())).intValue();

                // check if the pointcut should be deployed as thread safe or not
                final boolean isThreadSafe = true; //isThreadSafe(cg, methods[i]);

                addStaticJoinPointField(cpg, cg, mg, methodSequence, isThreadSafe);

                // get the join point controller
                final String controllerClassName =
                        m_weaveModel.getJoinPointController(classMetaData, methodMetaData);

                if (noClinitMethod) {
                    // no <clinit> method exists
                    if (clInitMethod == null) {
                        clInitMethod = createClInitMethodWithStaticJoinPointField(
                                cpg, cg, methods[i], factory, methodLookupId,
                                methodSequence, isThreadSafe);
                    }
                    else {
                        clInitMethod = createStaticJoinPointField(
                                cpg, cg, clInitMethod, methods[i], factory,
                                methodLookupId, methodSequence, isThreadSafe);
                    }
                }
                else {
                    // we have a <clinit> method
                    methods[indexClinit] = createStaticJoinPointField(
                            cpg, cg, methods[indexClinit], methods[i], factory,
                            methodLookupId, methodSequence, isThreadSafe);
                }

                // create a proxy method for the original method
                newMethods.add(createProxyMethod(
                        cpg, cg, mg, factory, methodLookupId, methodSequence,
                        methods[i].getAccessFlags(), isThreadSafe, uuid, controllerClassName));

                // add a prefix to the original method
                methods[i] = addPrefixToMethod(cpg, cg, mg, methods[i], methodSequence);

                mg.setMaxLocals();
                mg.setMaxStack();
            }

            // if we have transformed methods, create the static class field
            if (noClinitMethod && clInitMethod != null) {
                addStaticClassField(cpg, cg);
                clInitMethod = createStaticClassField(cpg, cg, clInitMethod, factory);

                newMethods.add(clInitMethod);
            }
            else if (newMethods.size() != 0) {
                addStaticClassField(cpg, cg);
                methods[indexClinit] = createStaticClassField(cpg, cg, methods[indexClinit], factory);
            }
            else {
                // skip
            }

            // update the old methods
            cg.setMethods(methods);

            // add the new methods
            for (Iterator it = newMethods.iterator(); it.hasNext();) {
                Method method = (Method)it.next();
                cg.addMethod(method);
            }
        }
    }

    /**
     * Creates a static class field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     */
    private void addStaticClassField(final ConstantPoolGen cp, final ClassGen cg) {

        final Field[] fields = cg.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(TransformationUtil.STATIC_CLASS_FIELD)) {
                return;
            }
        }

        final FieldGen field = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_FINAL | Constants.ACC_STATIC,
                new ObjectType("java.lang.Class"),
                TransformationUtil.STATIC_CLASS_FIELD,
                cp);

        cg.addField(field.getField());
    }

    /**
     * Creates a static join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param mg the MethodGen
     * @param methodSequence the methods sequence number
     * @param isThreadSafe
     */
    private void addStaticJoinPointField(final ConstantPoolGen cp,
                                         final ClassGen cg,
                                         final MethodGen mg,
                                         final int methodSequence,
                                         final boolean isThreadSafe) {
        final StringBuffer joinPoint = getJoinPointName(mg.getMethod(), methodSequence);

        final FieldGen field;
        if (isThreadSafe) {
            field = new FieldGen(
                    Constants.ACC_PRIVATE | Constants.ACC_FINAL | Constants.ACC_STATIC,
                    new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                    joinPoint.toString(),
                    cp);
        }
        else {
            field = new FieldGen(
                    Constants.ACC_PRIVATE | Constants.ACC_FINAL | Constants.ACC_STATIC,
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_TYPE,
                    joinPoint.toString(),
                    cp);
        }
        cg.addField(field.getField());
    }

    /**
     * Creates a new <clinit> method and creates a join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param method the current method
     * @param factory the objectfactory
     * @param methodLookupId the id of the current method in the lookup tabl
     * @param methodSequence the methods sequence number
     * @param isThreadSafe
     * @return the new method
     */
    private Method createClInitMethodWithStaticJoinPointField(
            final ConstantPoolGen cp,
            final ClassGen cg,
            final Method method,
            final InstructionFactory factory,
            final int methodLookupId,
            final int methodSequence,
            final boolean isThreadSafe) {

        final String className = cg.getClassName();

        final StringBuffer joinPoint = getJoinPointName(method, methodSequence);

        final InstructionList il = new InstructionList();
        final MethodGen clInit = new MethodGen(
                Constants.ACC_STATIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[]{},
                "<clinit>",
                className,
                il, cp);

        if (isThreadSafe) {
            il.append(factory.createNew(TransformationUtil.THREAD_LOCAL_CLASS));
            il.append(InstructionConstants.DUP);

            il.append(factory.createInvoke(
                    TransformationUtil.THREAD_LOCAL_CLASS,
                    "<init>",
                    Type.VOID,
                    new Type[]{},
                    Constants.INVOKESPECIAL));

            il.append(factory.createFieldAccess(
                    cg.getClassName(),
                    joinPoint.toString(),
                    new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                    Constants.PUTSTATIC));
        }
        else {
            il.append(factory.createNew(
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS));
            il.append(InstructionConstants.DUP);

            il.append(factory.createFieldAccess(
                    cg.getClassName(),
                    TransformationUtil.STATIC_CLASS_FIELD,
                    new ObjectType("java.lang.Class"),
                    Constants.GETSTATIC));

            il.append(new PUSH(cp, methodLookupId));

            il.append(factory.createInvoke(
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS,
                    "<init>",
                    Type.VOID,
                    new Type[]{new ObjectType("java.lang.Class"), Type.INT},
                    Constants.INVOKESPECIAL));

            il.append(factory.createFieldAccess(
                    cg.getClassName(),
                    joinPoint.toString(),
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_TYPE,
                    Constants.PUTSTATIC));
        }
        il.append(factory.createReturn(Type.VOID));

        clInit.setMaxLocals();
        clInit.setMaxStack();
        return clInit.getMethod();
    }

    /**
     * Creates a new static class field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param clInit the constructor for the class
     * @param factory the objectfactory
     * @return the modified clinit method
     */
    private Method createStaticClassField(final ConstantPoolGen cp,
                                          final ClassGen cg,
                                          final Method clInit,
                                          final InstructionFactory factory) {
        final String className = cg.getClassName();

        final MethodGen mg = new MethodGen(clInit, cg.getClassName(), cp);
        final InstructionList il = mg.getInstructionList();

        final InstructionHandle ih = il.getStart();

        // invoke Class.forName(..)
        il.insert(ih, new PUSH(cp, cg.getClassName()));
        il.insert(ih, factory.createInvoke(
                "java.lang.Class",
                "forName",
                new ObjectType("java.lang.Class"),
                new Type[]{Type.STRING},
                Constants.INVOKESTATIC));

        // set the result to the static class field
        il.insert(ih, factory.createFieldAccess(
                className,
                TransformationUtil.STATIC_CLASS_FIELD,
                new ObjectType("java.lang.Class"),
                Constants.PUTSTATIC));

        mg.setMaxStack();
        mg.setMaxLocals();
        return mg.getMethod();
    }

    /**
     * Creates a new static join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param clInit the constructor for the class
     * @param method the current method
     * @param factory the objectfactory
     * @param methodLookupId the id of the current method in the lookup tabl
     * @param methodSequence the methods sequence number
     * @param isThreadSafe
     * @return the modified clinit method
     */
    private Method createStaticJoinPointField(final ConstantPoolGen cp,
                                              final ClassGen cg,
                                              final Method clInit,
                                              final Method method,
                                              final InstructionFactory factory,
                                              final int methodLookupId,
                                              final int methodSequence,
                                              final boolean isThreadSafe) {

        final StringBuffer joinPoint = getJoinPointName(method, methodSequence);

        final MethodGen mg = new MethodGen(clInit, cg.getClassName(), cp);
        final InstructionList il = mg.getInstructionList();

        final InstructionHandle ih = il.getStart();

        if (isThreadSafe) {
            il.insert(ih, factory.createNew(TransformationUtil.THREAD_LOCAL_CLASS));

            il.insert(ih, InstructionConstants.DUP);

            il.insert(ih, factory.createInvoke(
                    TransformationUtil.THREAD_LOCAL_CLASS,
                    "<init>",
                    Type.VOID,
                    new Type[]{},
                    Constants.INVOKESPECIAL));

            il.insert(ih, factory.createFieldAccess(
                    cg.getClassName(),
                    joinPoint.toString(),
                    new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                    Constants.PUTSTATIC));
        }
        else {
            il.insert(ih, factory.createNew(TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS));
            il.insert(ih, InstructionConstants.DUP);

            il.insert(ih, factory.createFieldAccess(
                    cg.getClassName(),
                    TransformationUtil.STATIC_CLASS_FIELD,
                    new ObjectType("java.lang.Class"),
                    Constants.GETSTATIC));

            il.insert(ih, new PUSH(cp, methodLookupId));

            il.insert(ih, factory.createInvoke(
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS,
                    "<init>",
                    Type.VOID,
                    new Type[]{new ObjectType("java.lang.Class"), Type.INT},
                    Constants.INVOKESPECIAL));

            il.insert(ih, factory.createFieldAccess(
                    cg.getClassName(),
                    joinPoint.toString(),
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_TYPE,
                    Constants.PUTSTATIC));
        }

        mg.setMaxStack();
        mg.setMaxLocals();
        return mg.getMethod();
    }

    /**
     * Adds a prefix to the original method.
     * To make it callable only from within the framework itself.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param mg the MethodGen
     * @param method the current method
     * @param methodSequence the methods sequence number
     * @return the modified method
     */
    private Method addPrefixToMethod(final ConstantPoolGen cp,
                                     final ClassGen cg,
                                     final MethodGen mg,
                                     final Method method,
                                     final int methodSequence) {

        final StringBuffer methodName =
                getPrefixedMethodName(method, methodSequence);

        // change the method access flags (should always be set to public)
        int accessFlags = mg.getAccessFlags();
        if ((accessFlags & Constants.ACC_PRIVATE) != 0) {
            // clear the private flag
            accessFlags &= ~Constants.ACC_PRIVATE;
        }
        if ((accessFlags & Constants.ACC_PROTECTED) != 0) {
            // clear the protected flag
            accessFlags &= ~Constants.ACC_PROTECTED;
        }
        if ((accessFlags & Constants.ACC_PUBLIC) == 0) {
            // set the public flag
            accessFlags |= Constants.ACC_PUBLIC;
        }

        // update the method
        final MethodGen prefixedMethod = new MethodGen(
                accessFlags,
                mg.getReturnType(),
                mg.getArgumentTypes(),
                mg.getArgumentNames(),
                methodName.toString(),
                cg.getClassName(),
                mg.getInstructionList(),
                cp);

        // add the exceptions
        final String[] exceptions = mg.getExceptions();
        for (int i = 0; i < exceptions.length; i++) {
            prefixedMethod.addException(exceptions[i]);
        }

        // add the exception handlers
        final CodeExceptionGen[] exceptionHandlers = mg.getExceptionHandlers();
        for (int i = 0; i < exceptionHandlers.length; i++) {
            prefixedMethod.addExceptionHandler(
                    exceptionHandlers[i].getStartPC(),
                    exceptionHandlers[i].getEndPC(),
                    exceptionHandlers[i].getHandlerPC(),
                    exceptionHandlers[i].getCatchType());
        }

        prefixedMethod.setMaxStack();
        prefixedMethod.setMaxLocals();
        return prefixedMethod.getMethod();
    }

    /**
     * Creates a proxy method for the original method specified.
     * This method has the same signature as the original method and
     * catches the invocation for further processing by the framework
     * before redirecting to the original method.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param originalMethod the current method
     * @param factory the objectfactory
     * @param methodId the id of the current method in the lookup tabl
     * @param methodSequence the methods sequence number
     * @param accessFlags the access flags for the original method
     * @param isThreadSafe
     * @param uuid the UUID for the weave model
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
                                     final boolean isThreadSafe,
                                     final String uuid,
                                     final String controllerClassName) {
        final InstructionList il = new InstructionList();

        final Type[] parameterTypes = Type.getArgumentTypes(originalMethod.getSignature());
        final String[] parameterNames = originalMethod.getArgumentNames();
        final Type returnType = Type.getReturnType(originalMethod.getSignature());
        final StringBuffer joinPoint = getJoinPointName(originalMethod.getMethod(), methodSequence);

        final MethodGen method = new MethodGen(
                accessFlags,
                returnType,
                parameterTypes,
                parameterNames,
                originalMethod.getName(),
                cg.getClassName(),
                il, cp);

        String[] exceptions = originalMethod.getExceptions();
        for (int i = 0; i < exceptions.length; i++) {
            method.addException(exceptions[i]);
        }

        int idxParam = 0;
        int idxStack = 0;

        int indexJoinPoint = parameterTypes.length * 2 + 1;

        BranchInstruction biIfNotNull = null;
        InstructionHandle ihIfNotNull = null;
        if (isThreadSafe) {
            // Object joinPoint = ___jp.get();
            il.append(factory.createFieldAccess(
                    cg.getClassName(),
                    joinPoint.toString(),
                    new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                    Constants.GETSTATIC));
            il.append(factory.createInvoke(
                    TransformationUtil.THREAD_LOCAL_CLASS,
                    "get",
                    Type.OBJECT,
                    Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
            il.append(factory.createStore(Type.OBJECT, indexJoinPoint));

            // if (joinPoint == null) {
            il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
            biIfNotNull = factory.createBranchInstruction(Constants.IFNONNULL, null);
            il.append(biIfNotNull);

            // joinPoint = new StaticMethodJoinPoint(uuid, this, 10);
            il.append(factory.createNew(TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS));

            // loads the parameters (uuid, the class, the method id)
            il.append(InstructionConstants.DUP);
            il.append(new PUSH(cp, uuid));
            il.append(factory.createFieldAccess(
                    cg.getClassName(),
                    TransformationUtil.STATIC_CLASS_FIELD,
                    new ObjectType("java.lang.Class"),
                    Constants.GETSTATIC));
            il.append(new PUSH(cp, methodId));
            il.append(new PUSH(cp, controllerClassName));

            // invokes the constructor
            il.append(factory.createInvoke(
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS,
                    "<init>",
                    Type.VOID,
                    new Type[]{Type.STRING, new ObjectType("java.lang.Class"), Type.INT, Type.STRING},
                    Constants.INVOKESPECIAL));

            il.append(factory.createStore(Type.OBJECT, indexJoinPoint));

            // ___jp.set(joinPoint);
            il.append(factory.createFieldAccess(
                    cg.getClassName(),
                    joinPoint.toString(),
                    new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                    Constants.GETSTATIC));
            il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
            il.append(factory.createInvoke(
                    TransformationUtil.THREAD_LOCAL_CLASS,
                    "set",
                    Type.VOID,
                    new Type[]{Type.OBJECT},
                    Constants.INVOKEVIRTUAL));

            ihIfNotNull = il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
            il.append(factory.createCheckCast(
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_TYPE));
            indexJoinPoint += 2;
            il.append(factory.createStore(Type.OBJECT, indexJoinPoint));

            biIfNotNull.setTarget(ihIfNotNull);
        }

        // if we have parameters, wrap them up
        if (parameterTypes.length != 0) {

            // create and allocate the parameters array
            il.append(new PUSH(cp, parameterTypes.length));
            il.append(factory.createNewArray(Type.OBJECT, (short)1));

            // put it on the stack
            il.append(InstructionConstants.DUP);
            il.append(new PUSH(cp, idxStack));
            idxStack++;

            // add all the parameters, wrap the primitive types in their
            // object counterparts
            for (int count = 0; count < parameterTypes.length; count++) {

                String wrapperClass = null;
                BasicType type = null;
                boolean hasLongOrDouble = false;

                if (parameterTypes[count] instanceof ObjectType ||
                        parameterTypes[count] instanceof ArrayType) {
                    // we have an object or an array
                    il.append(factory.createLoad(Type.OBJECT, idxParam));
                    il.append(InstructionConstants.AASTORE);
                    idxParam++;
                }
                else if (parameterTypes[count] instanceof BasicType) {
                    // we have a primitive type
                    hasLongOrDouble = false;
                    if ((parameterTypes[count]).equals(Type.LONG)) {
                        wrapperClass = "java.lang.Long";
                        type = Type.LONG;
                        hasLongOrDouble = true;
                    }
                    else if ((parameterTypes[count]).equals(Type.INT)) {
                        wrapperClass = "java.lang.Integer";
                        type = Type.INT;
                    }
                    else if ((parameterTypes[count]).equals(Type.SHORT)) {
                        wrapperClass = "java.lang.Short";
                        type = Type.SHORT;
                    }
                    else if ((parameterTypes[count]).equals(Type.DOUBLE)) {
                        wrapperClass = "java.lang.Double";
                        type = Type.DOUBLE;
                        hasLongOrDouble = true;
                    }
                    else if ((parameterTypes[count]).equals(Type.FLOAT)) {
                        wrapperClass = "java.lang.Float";
                        type = Type.FLOAT;
                    }
                    else if ((parameterTypes[count]).equals(Type.CHAR)) {
                        wrapperClass = "java.lang.Character";
                        type = Type.CHAR;
                    }
                    else if ((parameterTypes[count]).equals(Type.BYTE)) {
                        wrapperClass = "java.lang.Byte";
                        type = Type.BYTE;
                    }
                    else if ((parameterTypes[count]).equals(Type.BOOLEAN)) {
                        wrapperClass = "java.lang.Boolean";
                        type = Type.BOOLEAN;
                    }
                    else {
                        throw new RuntimeException("unknown parameter type: " + parameterTypes[count]);
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

                if (count != parameterTypes.length - 1) {
                    // if we don't have the last parameter,
                    // create the parameter on the stack
                    il.append(InstructionConstants.DUP);
                    il.append(new PUSH(cp, idxStack));
                    idxStack++;

                    // long's and double's needs two registers to fit
                    if (hasLongOrDouble) {
                        idxParam++;
                    }
                }
            }

            // create the object array
            il.append(factory.createStore(Type.OBJECT, idxParam));

            if (isThreadSafe) {
                // if threadsafe grab the newly retrieved local join point field from the stack
                il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
            }
            else {
                // grab the join point member field
                il.append(factory.createFieldAccess(
                        cg.getClassName(),
                        joinPoint.toString(),
                        TransformationUtil.STATIC_METHOD_JOIN_POINT_TYPE,
                        Constants.GETSTATIC));
            }

            // invoke joinPoint.setParameter(..)
            il.append(factory.createLoad(Type.OBJECT, idxParam));
            il.append(factory.createInvoke(
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS,
                    "setParameters",
                    Type.VOID,
                    new Type[]{new ArrayType(Type.OBJECT, 1)},
                    Constants.INVOKEVIRTUAL));
            idxParam++;
        } // end - if parameters.length != 0

        if (isThreadSafe) {
            // if threadsafe grab the newly retrieved local join point field from the stack
            il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
            il.append(factory.createInvoke(
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS,
                    "proceed",
                    Type.OBJECT,
                    Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
        }
        else {
            // grab the join point member field
            il.append(factory.createFieldAccess(
                    cg.getClassName(),
                    joinPoint.toString(),
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_TYPE,
                    Constants.GETSTATIC));

            // invoke joinPoint.proceed()
            il.append(factory.createInvoke(
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS,
                    "proceed",
                    Type.OBJECT,
                    Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
        }
        if (!returnType.equals(Type.VOID)) {

            // create the result from the invocation
            il.append(factory.createStore(Type.OBJECT, idxParam));
            il.append(factory.createLoad(Type.OBJECT, idxParam));

            // cast the result and return it, if the return type is a
            // primitive type, retrieve it from the wrapped object first
            if (returnType instanceof BasicType) {
                if (returnType.equals(Type.LONG)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Long")));
                    il.append(factory.createInvoke(
                            "java.lang.Long",
                            "longValue",
                            Type.LONG,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (returnType.equals(Type.INT)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Integer")));
                    il.append(factory.createInvoke(
                            "java.lang.Integer",
                            "intValue",
                            Type.INT,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (returnType.equals(Type.SHORT)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Short")));
                    il.append(factory.createInvoke(
                            "java.lang.Short",
                            "shortValue",
                            Type.SHORT,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (returnType.equals(Type.DOUBLE)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Double")));
                    il.append(factory.createInvoke(
                            "java.lang.Double",
                            "doubleValue",
                            Type.DOUBLE,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (returnType.equals(Type.FLOAT)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Float")));
                    il.append(factory.createInvoke(
                            "java.lang.Float",
                            "floatValue",
                            Type.FLOAT,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (returnType.equals(Type.CHAR)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Character")));
                    il.append(factory.createInvoke(
                            "java.lang.Character",
                            "charValue",
                            Type.CHAR,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (returnType.equals(Type.BYTE)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Byte")));
                    il.append(factory.createInvoke(
                            "java.lang.Byte",
                            "byteValue",
                            Type.BYTE,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (returnType.equals(Type.BOOLEAN)) {
                    il.append(factory.createCheckCast(
                            new ObjectType("java.lang.Boolean")));
                    il.append(factory.createInvoke(
                            "java.lang.Boolean",
                            "booleanValue",
                            Type.BOOLEAN,
                            Type.NO_ARGS,
                            Constants.INVOKEVIRTUAL));
                }
                else if (returnType.equals(Type.VOID)) {
                    // skip
                }
                else {
                    throw new RuntimeException("unknown return type: " + returnType);
                }
            }
            else {
                // cast the result to the right type
                il.append(factory.createCast(Type.OBJECT, returnType));
            }
        }
        il.append(factory.createReturn(returnType));

        method.setMaxStack();
        method.setMaxLocals();
        return method.getMethod();
    }

    /**
     * JMangler callback method. Is being called before each transformation.
     */
    public void sessionStart() {
    }

    /**
     * JMangler callback method. Is being called after each transformation.
     */
    public void sessionEnd() {
    }

    /**
     * JMangler callback method. Prints a log/status message at each transformation.
     *
     * @return a log string
     */
    public String verboseMessage() {
        return this.getClass().getName();
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param cg the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final ClassGen cg) {
        if (cg.isInterface() ||
                cg.getSuperclassName().equals("org.codehaus.aspectwerkz.advice.AroundAdvice") ||
                cg.getSuperclassName().equals("org.codehaus.aspectwerkz.advice.PreAdvice") ||
                cg.getSuperclassName().equals("org.codehaus.aspectwerkz.advice.PostAdvice")) {
            return true;
        }
        else if (m_weaveModel.inTransformationScope(cg.getClassName())) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Filters the methods to be transformed.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param method the method to filter
     * @return the UUID for the weave model
     */
    private String methodFilter(final ClassMetaData classMetaData,
                                final MethodMetaData methodMetaData,
                                final Method method) {
        String uuid = null;
        if (method.getName().equals("<init>") ||
                method.getName().equals("<clinit>") ||
                method.getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX) ||
                method.getName().equals(TransformationUtil.GET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.SET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
                method.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            uuid = null;
        }
        else {
            if (m_weaveModel.hasMethodPointcut(classMetaData, methodMetaData)) {
                uuid = m_weaveModel.getUuid();
            }
            if (m_weaveModel.hasThrowsPointcut(classMetaData, methodMetaData)) {
                uuid = m_weaveModel.getUuid();
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
    private StringBuffer getJoinPointName(final Method method,
                                          final int methodSequence) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(TransformationUtil.STATIC_METHOD_JOIN_POINT_PREFIX);
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
    private static StringBuffer getPrefixedMethodName(final Method method,
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


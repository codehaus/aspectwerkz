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
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Field;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;

/**
 * Transforms static methods to become "aspect-aware".
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviseStaticMethodTransformer implements AspectWerkzCodeTransformerComponent {

    /**
     * The definitions.
     */
    private final List m_definitions;

    /**
     * Retrieves the weave model.
     */
    public AdviseStaticMethodTransformer() {
        super();
        m_definitions = DefinitionLoader.getDefinitionsForTransformation();
    }

    /**
     * Makes the static method transformations.
     *
     * @todo refactor so that we don't have to loop over all the methods twice (and create a method meta-data object twice)
     *
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transformCode(final Context context, final Klass klass) {

        // loop over all the definitions
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            AspectWerkzDefinition definition = (AspectWerkzDefinition)it.next();

            definition.loadAspects(context.getLoader());

            final ClassGen cg = klass.getClassGen();
            ClassMetaData classMetaData = BcelMetaDataMaker.
                    createClassMetaData(context.getJavaClass(cg));

            if (classFilter(definition, classMetaData, cg)) {
                return;
            }

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
                Method method = methods[i];

                MethodMetaData methodMetaData = BcelMetaDataMaker.createMethodMetaData(methods[i]);
                if (methodFilter(definition, classMetaData, methodMetaData, method)) {
                    continue;
                }
                methodLookupList.add(methods[i]);

                // TODO: does not work, needs to be thought through more
                // if advised swap add the prefixed one as well to enable second-round instrumentation
//                String originalPrefixedName =
//                        TransformationUtil.ORIGINAL_METHOD_PREFIX +
//                        methods[i].getName();
//                Method[] declaredMethods = cg.getMethods();
//                for (int j = 0; j < declaredMethods.length; j++) {
//                    Method declaredMethod = declaredMethods[j];
//                    if (declaredMethod.getName().startsWith(originalPrefixedName)) {
//                        methodLookupList.add(declaredMethod);
//                    }
//                }
            }

            Collections.sort(methodLookupList, BCELMethodComparator.getInstance());

            final Map methodSequences = new HashMap();
            final List newMethods = new ArrayList();
            Method clInitMethod = null;

            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                MethodMetaData methodMetaData = BcelMetaDataMaker.createMethodMetaData(method);

                if (methodFilter(definition, classMetaData, methodMetaData, method)
                        || !method.isStatic()) {
                    continue;
                }

                // TODO: does not work, needs to be thought through more
                // if advised swap the method to the prefixed one
//                String originalPrefixedName =
//                        TransformationUtil.ORIGINAL_METHOD_PREFIX +
//                        methods[i].getName();
//                Method[] declaredMethods = cg.getMethods();
//                for (int j = 0; j < declaredMethods.length; j++) {
//                    Method declaredMethod = declaredMethods[j];
//                    if (declaredMethod.getName().startsWith(originalPrefixedName)) {
//                        method = declaredMethod;
//                    }
//                }

                final MethodGen mg = new MethodGen(method, cg.getClassName(), cpg);

                // take care of identification of overloaded methods by
                // inserting a sequence number
                if (methodSequences.containsKey(method.getName())) {
                    int sequence = ((Integer)methodSequences.get(methods[i].getName())).intValue();
                    methodSequences.remove(method.getName());
                    sequence++;
                    methodSequences.put(method.getName(), new Integer(sequence));
                }
                else {
                    methodSequences.put(method.getName(), new Integer(1));
                }

                final int methodLookupId = methodLookupList.indexOf(method);
                final int methodSequence = ((Integer)methodSequences.get(method.getName())).intValue();

                addStaticJoinPointField(cpg, cg, mg, methodSequence);

                // get the join point controller
                final String controllerClassName = definition.getJoinPointController(
                        classMetaData, methodMetaData
                );

                if (noClinitMethod) {
                    // no <clinit> method exists
                    if (clInitMethod == null) {
                        clInitMethod = createClInitMethodWithStaticJoinPointField(
                                cpg, cg,
                                method,
                                factory,
                                methodSequence
                        );
                    }
                    else {
                        clInitMethod = createStaticJoinPointField(
                                cpg, cg, clInitMethod,
                                method,
                                factory,
                                methodSequence
                        );
                    }
                }
                else {
                    // we have a <clinit> method
                    methods[indexClinit] = createStaticJoinPointField(
                            cpg, cg, methods[indexClinit],
                            method,
                            factory,
                            methodSequence
                    );
                }

                // create a proxy method for the original method
                newMethods.add(createProxyMethod(
                        cpg, cg,
                        methodMetaData.getName(),
                        mg, factory,
                        methodLookupId,
                        methodSequence,
                        method.getAccessFlags(),
                        definition.getUuid(),
                        controllerClassName
                ));

                // add a prefix to the original method
                methods[i] = addPrefixToMethod(
                        mg, method, methodSequence, definition.getUuid()
                );

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

            // update the old methods
            cg.setMethods(methods);

            // add the new methods
            for (Iterator it2 = newMethods.iterator(); it2.hasNext();) {
                Method method = (Method)it2.next();
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
                cp
        );

        cg.addField(field.getField());
    }

    /**
     * Creates a static join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param mg the MethodGen
     * @param methodSequence the methods sequence number
     */
    private void addStaticJoinPointField(final ConstantPoolGen cp,
                                         final ClassGen cg,
                                         final MethodGen mg,
                                         final int methodSequence) {
        final String joinPoint = getJoinPointName(mg.getMethod(), methodSequence);

        if (cg.containsField(joinPoint) != null) {
            return;
        }

        final FieldGen field = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_FINAL | Constants.ACC_STATIC,
                new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                joinPoint,
                cp
        );

        cg.addField(field.getField());
    }

    /**
     * Creates a new <clinit> method and creates a join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param method the current method
     * @param factory the objectfactory
     * @param methodSequence the methods sequence number
     * @return the new method
     */
    private Method createClInitMethodWithStaticJoinPointField(
            final ConstantPoolGen cp,
            final ClassGen cg,
            final Method method,
            final InstructionFactory factory,
            final int methodSequence) {

        final String className = cg.getClassName();

        final String joinPoint = getJoinPointName(method, methodSequence);

        final InstructionList il = new InstructionList();
        final MethodGen clInit = new MethodGen(
                Constants.ACC_STATIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[]{},
                "<clinit>",
                className,
                il, cp
        );

        il.append(factory.createNew(TransformationUtil.THREAD_LOCAL_CLASS));
        il.append(InstructionConstants.DUP);

        il.append(factory.createInvoke(
                TransformationUtil.THREAD_LOCAL_CLASS,
                "<init>",
                Type.VOID,
                new Type[]{},
                Constants.INVOKESPECIAL
        ));

        il.append(factory.createFieldAccess(
                cg.getClassName(),
                joinPoint,
                new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                Constants.PUTSTATIC
        ));

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
                Constants.INVOKESTATIC
        ));

        // set the result to the static class field
        il.insert(ih, factory.createFieldAccess(
                className,
                TransformationUtil.STATIC_CLASS_FIELD,
                new ObjectType("java.lang.Class"),
                Constants.PUTSTATIC
        ));

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
     * @param methodSequence the methods sequence number
     * @return the modified clinit method
     */
    private Method createStaticJoinPointField(final ConstantPoolGen cp,
                                              final ClassGen cg,
                                              final Method clInit,
                                              final Method method,
                                              final InstructionFactory factory,
                                              final int methodSequence) {

        final String joinPoint = getJoinPointName(method, methodSequence);

        final MethodGen mg = new MethodGen(clInit, cg.getClassName(), cp);
        final InstructionList il = mg.getInstructionList();

        final InstructionHandle ih = il.getStart();

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
                joinPoint,
                new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                Constants.PUTSTATIC
        ));

        mg.setMaxStack();
        mg.setMaxLocals();

        return mg.getMethod();
    }

    /**
     * Adds a prefix to the original method.
     * To make it callable only from within the framework itself.
     *
     * @param mg the MethodGen
     * @param method the current method
     * @param methodSequence the methods sequence number
     * @param uuid the definition UUID
     * @return the modified method
     */
    private Method addPrefixToMethod(final MethodGen mg,
                                     final Method method,
                                     final int methodSequence,
                                     final String uuid) {

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

        String prefixedMethodName = getPrefixedMethodName(
                method, methodSequence, mg.getClassName());

        mg.setName(prefixedMethodName);
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
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param originalMethod the current method
     * @param factory the objectfactory
     * @param methodId the id of the current method in the lookup tabl
     * @param methodSequence the methods sequence number
     * @param accessFlags the access flags for the original method
     * @param uuid the UUID for the weave model
     * @param controllerClassName the class name of the controller class to use
     * @return the proxy method
     */
    private Method createProxyMethod(final ConstantPoolGen cp,
                                     final ClassGen cg,
                                     final String originalMethodName,
                                     final MethodGen originalMethod,
                                     final InstructionFactory factory,
                                     final int methodId,
                                     final int methodSequence,
                                     final int accessFlags,
                                     final String uuid,
                                     final String controllerClassName) {
        final InstructionList il = new InstructionList();

        final Type[] parameterTypes = Type.getArgumentTypes(originalMethod.getSignature());
        final String[] parameterNames = originalMethod.getArgumentNames();
        final Type returnType = Type.getReturnType(originalMethod.getSignature());
        final String joinPoint = getJoinPointName(originalMethod.getMethod(), methodSequence);

        final MethodGen method = new MethodGen(
                accessFlags,
                returnType,
                parameterTypes,
                parameterNames,
                originalMethodName,
                cg.getClassName(),
                il, cp
        );

        String[] exceptions = originalMethod.getExceptions();
        for (int i = 0; i < exceptions.length; i++) {
            method.addException(exceptions[i]);
        }

        int idxParam = 0;
        int idxStack = 0;

        int indexJoinPoint = parameterTypes.length * 2 + 1;

        BranchInstruction biIfNotNull = null;
        InstructionHandle ihIfNotNull = null;

        // Object joinPoint = ___jp.get();
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                joinPoint,
                new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                Constants.GETSTATIC
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

        // joinPoint = new WeakReference(new MemberMethodJoinPoint(uuid, this, "foo.bar.Baz", 10));
//        il.append(factory.createNew(TransformationUtil.WEAK_REFERENCE_CLASS));
//        il.append(InstructionConstants.DUP);

        il.append(factory.createNew(TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS));
        il.append(InstructionConstants.DUP);

        // loads the parameters (uuid, the class, the method id)
        il.append(new PUSH(cp, uuid));
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                TransformationUtil.STATIC_CLASS_FIELD,
                new ObjectType("java.lang.Class"),
                Constants.GETSTATIC
        ));
        il.append(new PUSH(cp, methodId));
        il.append(new PUSH(cp, controllerClassName));

        il.append(factory.createInvoke(
                TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS,
                "<init>",
                Type.VOID,
                new Type[]{Type.STRING, new ObjectType("java.lang.Class"), Type.INT, Type.STRING},
                Constants.INVOKESPECIAL
        ));
//        il.append(factory.createInvoke(
//                TransformationUtil.WEAK_REFERENCE_CLASS,
//                "<init>",
//                Type.VOID,
//                new Type[]{Type.OBJECT},
//                Constants.INVOKESPECIAL)
//        );
        il.append(factory.createStore(Type.OBJECT, indexJoinPoint));

        // ___jp.set(joinPoint);
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                new ObjectType(TransformationUtil.THREAD_LOCAL_CLASS),
                Constants.GETSTATIC
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
        indexJoinPoint += 2;

        // cast the weak ref, retrieve the join point from the weak ref and cast the join point
//        il.append(factory.createCheckCast(TransformationUtil.WEAK_REFERENCE_TYPE));
//        il.append(factory.createStore(Type.OBJECT, indexJoinPoint));
//        il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
//        indexJoinPoint += 1;
//        il.append(factory.createInvoke(
//                TransformationUtil.WEAK_REFERENCE_CLASS,
//                "get",
//                Type.OBJECT,
//                Type.NO_ARGS,
//                Constants.INVOKEVIRTUAL)
//        );
        il.append(factory.createCheckCast(TransformationUtil.STATIC_METHOD_JOIN_POINT_TYPE));
        il.append(factory.createStore(Type.OBJECT, indexJoinPoint));

        biIfNotNull.setTarget(ihIfNotNull);

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
                            Constants.INVOKESPECIAL
                    ));
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

            // if threadsafe grab the newly retrieved local join point field from the stack
            il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));

            // invoke joinPoint.setParameter(..)
            il.append(factory.createLoad(Type.OBJECT, idxParam));
            il.append(factory.createInvoke(
                    TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS,
                    "setParameters",
                    Type.VOID,
                    new Type[]{new ArrayType(Type.OBJECT, 1)},
                    Constants.INVOKEVIRTUAL
            ));
            idxParam++;
        } // end - if parameters.length != 0

        // if threadsafe grab the newly retrieved local join point field from the stack
        il.append(factory.createLoad(Type.OBJECT, indexJoinPoint));
        il.append(factory.createInvoke(
                TransformationUtil.STATIC_METHOD_JOIN_POINT_CLASS,
                "proceed",
                Type.OBJECT,
                Type.NO_ARGS,
                Constants.INVOKEVIRTUAL
        ));

        if (!returnType.equals(Type.VOID)) {

            // create the result from the invocation
            il.append(factory.createStore(Type.OBJECT, idxParam));
            il.append(factory.createLoad(Type.OBJECT, idxParam));

            // cast the result and return it, if the return type is a
            // primitive type, retrieve it from the wrapped object first
            // unless the return object is null (AW-100)
            if (returnType instanceof BasicType) {
                if (returnType.equals(Type.VOID)) {
                    ;//skip
                } else {
                    BranchInstruction ifNullBranch = factory.createBranchInstruction(Constants.IFNONNULL, null);
                    InstructionHandle elseBranch = null;
                    il.append(ifNullBranch);
                    if (returnType.equals(Type.LONG)) {
                        il.append(new PUSH(cp, 0L));
                        il.append(factory.createReturn(returnType));
                        elseBranch = il.append(factory.createLoad(Type.OBJECT, idxParam));
                        il.append(factory.createCheckCast(
                                new ObjectType("java.lang.Long")
                        ));
                        il.append(factory.createInvoke(
                                "java.lang.Long",
                                "longValue",
                                Type.LONG,
                                Type.NO_ARGS,
                                Constants.INVOKEVIRTUAL
                        ));
                    }
                    else if (returnType.equals(Type.INT)) {
                        il.append(new PUSH(cp, 0));
                        il.append(factory.createReturn(returnType));
                        elseBranch = il.append(factory.createLoad(Type.OBJECT, idxParam));
                        il.append(factory.createCheckCast(
                                new ObjectType("java.lang.Integer")
                        ));
                        il.append(factory.createInvoke(
                                "java.lang.Integer",
                                "intValue",
                                Type.INT,
                                Type.NO_ARGS,
                                Constants.INVOKEVIRTUAL
                        ));
                    }
                    else if (returnType.equals(Type.SHORT)) {
                        il.append(new PUSH(cp, (short)0));
                        il.append(factory.createReturn(returnType));
                        elseBranch = il.append(factory.createLoad(Type.OBJECT, idxParam));
                        il.append(factory.createCheckCast(
                                new ObjectType("java.lang.Short")
                        ));
                        il.append(factory.createInvoke(
                                "java.lang.Short",
                                "shortValue",
                                Type.SHORT,
                                Type.NO_ARGS,
                                Constants.INVOKEVIRTUAL
                        ));
                    }
                    else if (returnType.equals(Type.DOUBLE)) {
                        il.append(new PUSH(cp, 0.0d));
                        il.append(factory.createReturn(returnType));
                        elseBranch = il.append(factory.createLoad(Type.OBJECT, idxParam));
                        il.append(factory.createCheckCast(
                                new ObjectType("java.lang.Double")
                        ));
                        il.append(factory.createInvoke(
                                "java.lang.Double",
                                "doubleValue",
                                Type.DOUBLE,
                                Type.NO_ARGS,
                                Constants.INVOKEVIRTUAL
                        ));
                    }
                    else if (returnType.equals(Type.FLOAT)) {
                        il.append(new PUSH(cp, 0.0f));
                        il.append(factory.createReturn(returnType));
                        elseBranch = il.append(factory.createLoad(Type.OBJECT, idxParam));
                        il.append(factory.createCheckCast(
                                new ObjectType("java.lang.Float")
                        ));
                        il.append(factory.createInvoke(
                                "java.lang.Float",
                                "floatValue",
                                Type.FLOAT,
                                Type.NO_ARGS,
                                Constants.INVOKEVIRTUAL
                        ));
                    }
                    else if (returnType.equals(Type.CHAR)) {
                        il.append(new PUSH(cp, '\u0000'));
                        il.append(factory.createReturn(returnType));
                        elseBranch = il.append(factory.createLoad(Type.OBJECT, idxParam));
                        il.append(factory.createCheckCast(
                                new ObjectType("java.lang.Character")
                        ));
                        il.append(factory.createInvoke(
                                "java.lang.Character",
                                "charValue",
                                Type.CHAR,
                                Type.NO_ARGS,
                                Constants.INVOKEVIRTUAL
                        ));
                    }
                    else if (returnType.equals(Type.BYTE)) {
                        il.append(new PUSH(cp, (byte)0));
                        il.append(factory.createReturn(returnType));
                        elseBranch = il.append(factory.createLoad(Type.OBJECT, idxParam));
                        il.append(factory.createCheckCast(
                                new ObjectType("java.lang.Byte")
                        ));
                        il.append(factory.createInvoke(
                                "java.lang.Byte",
                                "byteValue",
                                Type.BYTE,
                                Type.NO_ARGS,
                                Constants.INVOKEVIRTUAL
                        ));
                    }
                    else if (returnType.equals(Type.BOOLEAN)) {
                        il.append(new PUSH(cp, false));
                        il.append(factory.createReturn(returnType));
                        elseBranch = il.append(factory.createLoad(Type.OBJECT, idxParam));
                        il.append(factory.createCheckCast(
                                new ObjectType("java.lang.Boolean")
                        ));
                        il.append(factory.createInvoke(
                                "java.lang.Boolean",
                                "booleanValue",
                                Type.BOOLEAN,
                                Type.NO_ARGS,
                                Constants.INVOKEVIRTUAL
                        ));
                    }
                    else {
                        throw new RuntimeException("unknown return type: " + returnType);
                    }
                    // return block in the else branch
                    ifNullBranch.setTarget(elseBranch);
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
     * @param definition the definition
     * @param classMetaData the meta-data for the class
     * @param cg the class to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final AspectWerkzDefinition definition,
                                final ClassMetaData classMetaData,
                                final ClassGen cg) {
        if (cg.isInterface() ||
                cg.getSuperclassName().equals("org.codehaus.aspectwerkz.advice.AroundAdvice") ||
                cg.getSuperclassName().equals("org.codehaus.aspectwerkz.advice.PreAdvice") ||
                cg.getSuperclassName().equals("org.codehaus.aspectwerkz.advice.PostAdvice")) {
            return true;
        }
        String className = cg.getClassName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
        if (definition.hasMethodPointcut(classMetaData) ||
                definition.hasThrowsPointcut(classMetaData)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the methods to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @param method the method to filter
     * @return boolean
     */
    private boolean methodFilter(final AspectWerkzDefinition definition,
                                 final ClassMetaData classMetaData,
                                 final MethodMetaData methodMetaData,
                                 final Method method) {
        if (method.isAbstract() || method.isNative() ||
                method.getName().equals("<init>") ||
                method.getName().equals("<clinit>") ||
                method.getName().startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX) ||
                method.getName().equals(TransformationUtil.GET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.SET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
                method.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        }
        else if (definition.hasMethodPointcut(classMetaData, methodMetaData)) {
            return false;
        }
        else if (definition.hasThrowsPointcut(classMetaData, methodMetaData)) {
            return false;
        }
//        //TODO: not sufficient, need to match the method signature as well (without the prefixes)
//        else if (method.getName().startsWith("___AW_wrapped_original_method")) {
//            return false;
//        }
        else {
            return true;
        }
    }

    /**
     * Returns the name of the join point.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @return the name of the join point
     */
    private String getJoinPointName(final Method method,
                                    final int methodSequence) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(TransformationUtil.STATIC_METHOD_JOIN_POINT_PREFIX);
        joinPoint.append(method.getName());
        joinPoint.append(TransformationUtil.DELIMITER);
        joinPoint.append(methodSequence);
        return joinPoint.toString();
    }

    /**
     * Returns the prefixed method name.
     *
     * @param method the method
     * @param methodSequence the method sequence
     * @param className FQN of the declaring class
     * @return the name of the join point
     */
    private static String getPrefixedMethodName(final Method method,
                                                final int methodSequence,
                                                final String className) {
        final StringBuffer methodName = new StringBuffer();
        methodName.append(TransformationUtil.ORIGINAL_METHOD_PREFIX);
        methodName.append(method.getName());
        methodName.append(TransformationUtil.DELIMITER);
        methodName.append(methodSequence);
        methodName.append(TransformationUtil.DELIMITER);
        methodName.append(className.replace('.', '_'));
        return methodName.toString();
    }
}


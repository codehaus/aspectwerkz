/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transformj;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.JavassistMetaDataMaker;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.transformj.Transformer;
import org.codehaus.aspectwerkz.transformj.Context;
import org.codehaus.aspectwerkz.transformj.Klass;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.Type;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.CtField;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Advises caller side method invocations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class AdviseCallerSideMethodTransformer implements Transformer {

    /**
     * The definition.
     */
    private final AspectWerkzDefinition m_definition;

    /**
     * Constructor.
     */
    public AdviseCallerSideMethodTransformer() {
        super();
        // TODO: fix loop over definitions
        m_definition = (AspectWerkzDefinition)DefinitionLoader.getDefinitionsForTransformation().get(0);
    }

    /**
     * Transforms the call side pointcuts.
     *
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transform(final Context context, final Klass klass) throws NotFoundException, CannotCompileException {
        m_definition.loadAspects(context.getLoader());

        final CtClass cg = klass.getClassGen();
        ClassMetaData classMetaData = JavassistMetaDataMaker.createClassMetaData(cg);

        // filter caller classes
        if (classFilter(classMetaData, cg)) {
            return;
        }
        final CtMethod[] methods = cg.getDeclaredMethods();

        // get the index for the <clinit> method (if there is one)
        boolean hasClInitMethod = false;
        int clinitIndex = -1;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("<clinit>")) {
                clinitIndex = i;
                hasClInitMethod = true;
                break;
            }
        }
        final String className = cg.getName();

        final Set callerSideJoinPoints = new HashSet();

        CtMethod clInitMethod = null;
        final Map methodSequences = new HashMap();
        final List newMethods = new ArrayList();
        //boolean isClassAdvised = false;
        //boolean isMethodChanged = false;



        ExprEditor callerSideEd = new ExprEditor() {

            public boolean isClassAdvised = false;
            //boolean isMethodChanged = false;

            public void edit(MethodCall m) throws CannotCompileException {
                try {
                    CtBehavior callerBehavior = m.where();

                // filter caller methods
                if (methodFilterCaller(callerBehavior)) {
                    return;
                }

                // get the callee method name, signature and class name
                final String calleeMethodName = m.getMethodName();
                final String calleeClassName = m.getClassName();
                final String calleeMethodSignature = m.getMethod().getSignature();

                // filter callee classes
                if (!m_definition.inIncludePackage(calleeClassName)) {
                    return;
                }
                // filter callee methods
                if (methodFilterCallee(calleeMethodName)) {
                    return;
                }

                // create the class meta-data
                ClassMetaData calleeSideClassMetaData;
                try {
                    calleeSideClassMetaData = JavassistMetaDataMaker.createClassMetaData(context.getClassPool().get(calleeClassName));
                }
                catch (NotFoundException e) {
                    throw new WrappedRuntimeException(e);
                }

                // create the method meta-data
                MethodMetaData calleeSideMethodMetaData = JavassistMetaDataMaker.createMethodMetaData(
                        m.getMethod()
                );

                // is this a caller side method pointcut?
                if (m_definition.isPickedOutByCallPointcut(calleeSideClassMetaData, calleeSideMethodMetaData)) {

                    // get the caller method name and signature
                    String callerMethodName = callerBehavior.getName();
                    String callerMethodSignature = callerBehavior.getSignature();

                    final String  joinPointType = //TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE;
                    "org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint";

                    // take care of identification of overloaded methods
                    // by inserting a sequence number
                    if (methodSequences.containsKey(calleeMethodName)) {
                        int sequence = ((Integer)methodSequences.get(calleeMethodName)).intValue();

                        methodSequences.remove(calleeMethodName);
                        sequence++;
                        methodSequences.put(calleeMethodName, new Integer(sequence));
                    }
                    else {
                        methodSequences.put(calleeMethodName, new Integer(1));
                    }
                    int methodSequence = ((Integer)methodSequences.get(calleeMethodName)).intValue();

                    if (!isClassAdvised) {
                        isClassAdvised = true;
                        createStaticClassField(cg);
                    }
                    //isMethodChanged = true;

                    // create JP field first
                    final String joinPointPrefix = getJoinPointPrefix(joinPointType);
                    final String joinPointClass = getJoinPointClass(joinPointType);
                    final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, calleeMethodName, methodSequence);

                    // skip the creation of the join point if we already have one
                    if (!callerSideJoinPoints.contains(joinPoint.toString())) {
                        callerSideJoinPoints.add(joinPoint.toString());
                        addStaticJoinPointField(cg, joinPoint.toString());
                        //system
                        //caller
                        //callee
                    }

                    m.replace("{" +
                            joinPoint + ".pre();" +
                            "$_ = $proceed($$);" +
                            joinPoint + ".post();" +
                            "}"
                    );

                    // advise
//                    m.replace("{" +
//                            "" +
//                            "" +
//                            "" +
//                            "");


//                    insertPreAdvice(cg,
//                            calleeMethodName,
//                            methodSequence,
//                            m,
//                            joinPointType
//                    );
//
//                    insertPostAdvice(cg,
//                            calleeMethodName,
//                            methodSequence,
//                            m,
//                            joinPointType
//                    );
                    }
                } catch (NotFoundException nfe) {
                    ;
                }
            }
        };

        cg.instrument(callerSideEd);

//
//
//
//        for (int i = 0; i < methods.length; i++) {
//
//            // filter caller methods
//            if (methodFilterCaller(methods[i])) {
//                continue;
//            }
//
//            final MethodGen mg = new MethodGen(methods[i], className, cpg);
//
//            final InstructionList il = mg.getInstructionList();
//            if (il == null) {
//                continue;
//            }
//            InstructionHandle ih = il.getStart();
//            isMethodChanged = false;
//            // search for all InvokeInstruction instructions and
//            // inserts the call side pointcuts
//            while (ih != null) {
//                final Instruction ins = ih.getInstruction();
//
//                if (ins instanceof INVOKESPECIAL ||
//                        ins instanceof INVOKESTATIC ||
//                        ins instanceof INVOKEVIRTUAL) {
//
//                    final InvokeInstruction invokeInstruction = (InvokeInstruction)ins;
//
//                    // get the callee method name, signature and class name
//                    final String calleeMethodName = invokeInstruction.getName(cpg);
//                    final String calleeClassName = invokeInstruction.getClassName(cpg);
//                    final String calleeMethodSignature = invokeInstruction.getSignature(cpg);
//
//                    // filter callee classes
//                    if (!m_definition.inIncludePackage(calleeClassName)) {
//                        ih = ih.getNext();
//                        continue;
//                    }
//                    // filter callee methods
//                    if (methodFilterCallee(calleeMethodName)) {
//                        ih = ih.getNext();
//                        continue;
//                    }
//
//                    // create the class meta-data
//                    ClassMetaData calleeSideClassMetaData;
//                    try {
//                        JavaClass javaClass = context.getClassPool().loadClass(calleeClassName);
//                        calleeSideClassMetaData = BcelMetaDataMaker.createClassMetaData(javaClass);
//                    }
//                    catch (ClassNotFoundException e) {
//                        throw new WrappedRuntimeException(e);
//                    }
//
//                    // create the method meta-data
//                    MethodMetaData calleeSideMethodMetaData = BcelMetaDataMaker.createMethodMetaData(
//                            invokeInstruction, cpg
//                    );
//
//                    // is this a caller side method pointcut?
//                    if (m_definition.isPickedOutByCallPointcut(calleeSideClassMetaData, calleeSideMethodMetaData)) {
//
//                        // get the caller method name and signature
//                        Method method = mg.getMethod();
//                        String callerMethodName = method.getName();
//                        String callerMethodSignature = method.getSignature();
//
//                        final Type joinPointType = TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE;
//
//                        // take care of identification of overloaded methods
//                        // by inserting a sequence number
//                        if (methodSequences.containsKey(calleeMethodName)) {
//                            int sequence = ((Integer)methodSequences.get(calleeMethodName)).intValue();
//
//                            methodSequences.remove(calleeMethodName);
//                            sequence++;
//                            methodSequences.put(calleeMethodName, new Integer(sequence));
//                        }
//                        else {
//                            methodSequences.put(calleeMethodName, new Integer(1));
//                        }
//                        int methodSequence = ((Integer)methodSequences.get(calleeMethodName)).intValue();
//
//                        isClassAdvised = true;
//                        isMethodChanged = true;
//
//                        insertPreAdvice(
//                                il, ih, cg,
//                                calleeMethodName,
//                                methodSequence,
//                                factory,
//                                joinPointType
//                        );
//
//                        insertPostAdvice(
//                                il, ih.getNext(), cg,
//                                calleeMethodName,
//                                methodSequence,
//                                factory,
//                                joinPointType
//                        );
//
//                        StringBuffer key = new StringBuffer();
//                        key.append(className);
//                        key.append(TransformationUtil.DELIMITER);
//                        key.append(calleeMethodName);
//                        key.append(TransformationUtil.DELIMITER);
//                        key.append(methodSequence);
//
//                        // skip the creation of the join point if we already have one
//                        if (!callerSideJoinPoints.contains(key.toString())) {
//                            callerSideJoinPoints.add(key.toString());
//
//                            addStaticJoinPointField(
//                                    cpg, cg, calleeMethodName,
//                                    methodSequence, joinPointType
//                            );
//
//                            if (hasClInitMethod) {
//                                methods[clinitIndex] = createStaticJoinPointField(
//                                        cpg, cg,
//                                        methods[clinitIndex],
//                                        callerMethodName,
//                                        calleeClassName,
//                                        calleeMethodName,
//                                        methodSequence,
//                                        callerMethodSignature,
//                                        calleeMethodSignature,
//                                        factory,
//                                        joinPointType,
//                                        m_definition.getUuid()
//                                );
//                            }
//                            else if (clInitMethod == null) {
//                                clInitMethod = createClInitMethodWithStaticJoinPointField(
//                                        cpg, cg,
//                                        callerMethodName,
//                                        calleeClassName,
//                                        calleeMethodName,
//                                        methodSequence,
//                                        callerMethodSignature,
//                                        calleeMethodSignature,
//                                        factory,
//                                        joinPointType,
//                                        m_definition.getUuid()
//                                );
//                            }
//                            else {
//                                clInitMethod = createStaticJoinPointField(
//                                        cpg, cg,
//                                        clInitMethod,
//                                        callerMethodName,
//                                        calleeClassName,
//                                        calleeMethodName,
//                                        methodSequence,
//                                        callerMethodSignature,
//                                        calleeMethodSignature,
//                                        factory,
//                                        joinPointType,
//                                        m_definition.getUuid()
//                                );
//                            }
//                        }
//                    }
//                }
//                ih = ih.getNext();
//            }
//
//            if (isMethodChanged) {
//                mg.setMaxStack();
//                methods[i] = mg.getMethod();
//            }
//        }


        if (true/*callerSideEd.isClassAdvised*/) {
            context.markAsAdvised();
            // if we have transformed methods, create the static class field
//            if (!hasClInitMethod && clInitMethod != null) {
//                addStaticClassField(cpg, cg);
//                clInitMethod = createStaticClassField(
//                        cpg, cg,
//                        clInitMethod,
//                        factory
//                );
//
//                newMethods.add(clInitMethod);
//            }
//            else {
//                addStaticClassField(cpg, cg);
//                methods[clinitIndex] = createStaticClassField(
//                        cpg, cg,
//                        methods[clinitIndex],
//                        factory
//                );
//            }
//        //}//TODO CHECK THIS
//        // update the old methods
//        cg.setMethods(methods);
//
//        // add the new methods
//        for (Iterator it = newMethods.iterator(); it.hasNext();) {
//            Method method = (Method)it.next();
//            cg.addMethod(method);
//        }
        }//TODO CHECK THIS
    }

    /**
     * Creates a static class field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     */
//    private void addStaticClassField(final ConstantPoolGen cp, final ClassGen cg) {
//
//        final Field[] fields = cg.getFields();
//
//        // check if we already have added this field
//        for (int i = 0; i < fields.length; i++) {
//            if (fields[i].getName().equals(TransformationUtil.STATIC_CLASS_FIELD))
//                return;
//        }
//
//        final FieldGen field = new FieldGen(
//                Constants.ACC_PRIVATE | Constants.ACC_FINAL | Constants.ACC_STATIC,
//                new ObjectType("java.lang.Class"),
//                TransformationUtil.STATIC_CLASS_FIELD,
//                cp
//        );
//
//        cg.addField(field.getField());
//    }

    /**
     * Creates a static join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param methodName the name of the method
     * @param methodSequence the sequence number for the method
     * @param joinPointType the type of the join point
     */
//    private void addStaticJoinPointField(final ConstantPoolGen cp,
//                                         final ClassGen cg,
//                                         final String methodName,
//                                         final int methodSequence,
//                                         final Type joinPointType) {
//
//        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
//        final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, methodName, methodSequence);
//
//        if (cg.containsField(joinPoint.toString()) != null) {
//            return;
//        }
//
//        final FieldGen field = new FieldGen(
//                Constants.ACC_PRIVATE | Constants.ACC_FINAL | Constants.ACC_STATIC,
//                TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE,
//                joinPoint.toString(),
//                cp
//        );
//
//        cg.addField(field.getField());
//    }

    /**
     * Creates a new static class field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param clInit the constructor for the class
     * @param factory the objectfactory
     * @return the modified clinit method
     */
//    private static Method createStaticClassField(final ConstantPoolGen cp,
//                                                 final ClassGen cg,
//                                                 final Method clInit,
//                                                 final InstructionFactory factory) {
//
//        final String className = cg.getClassName();
//
//        final MethodGen mg = new MethodGen(clInit, cg.getClassName(), cp);
//        final InstructionList il = mg.getInstructionList();
//
//        final InstructionHandle ih = il.getStart();
//
//        // invoke Class.forName(..)
//        il.insert(ih, new PUSH(cp, cg.getClassName()));
//        il.insert(ih, factory.createInvoke(
//                "java.lang.Class",
//                "forName",
//                new ObjectType("java.lang.Class"),
//                new Type[]{Type.STRING},
//                Constants.INVOKESTATIC
//        ));
//
//        // set the result to the static class field
//        il.insert(ih, factory.createFieldAccess(
//                className,
//                TransformationUtil.STATIC_CLASS_FIELD,
//                new ObjectType("java.lang.Class"),
//                Constants.PUTSTATIC
//        ));
//
//        mg.setMaxStack();
//        mg.setMaxLocals();
//        return mg.getMethod();
//    }

    /**
     * Creates a new <clinit> method and creates a join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param callerMethodName the caller method name
     * @param calleeClassName the calle class name
     * @param calleeMethodName the callee method name
     * @param methodSequence the sequence number for the method
     * @param callerMethodSignature the signature for the caller method
     * @param calleeMethodSignature the signature for the callee method
     * @param factory the objectfactory
     * @param joinPointType the type of the joinpoint
     * @param uuid the UUID for the weave model
     * @return the new method
     */
//    private Method createClInitMethodWithStaticJoinPointField(
//            final ConstantPoolGen cp,
//            final ClassGen cg,
//            final String callerMethodName,
//            final String calleeClassName,
//            final String calleeMethodName,
//            final int methodSequence,
//            final String callerMethodSignature,
//            final String calleeMethodSignature,
//            final InstructionFactory factory,
//            final Type joinPointType,
//            final String uuid) {
//
//        final String className = cg.getClassName();
//
//        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
//        final StringBuffer joinPoint = getJoinPointName(
//                joinPointPrefix, calleeMethodName, methodSequence
//        );
//
//        StringBuffer fullCalleeMethodName = new StringBuffer();
//        fullCalleeMethodName.append(calleeClassName);
//        fullCalleeMethodName.append(TransformationUtil.
//                CALL_SIDE_DELIMITER);
//        fullCalleeMethodName.append(calleeMethodName);
//
//        final InstructionList il = new InstructionList();
//        final MethodGen clInit = new MethodGen(
//                Constants.ACC_STATIC,
//                Type.VOID,
//                Type.NO_ARGS,
//                new String[]{},
//                "<clinit>",
//                className,
//                il, cp
//        );
//
//        il.append(factory.createNew(TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS));
//        il.append(InstructionConstants.DUP);
//
//        il.append(new PUSH(cp, uuid));
//        il.append(factory.createFieldAccess(
//                cg.getClassName(),
//                TransformationUtil.STATIC_CLASS_FIELD,
//                new ObjectType("java.lang.Class"),
//                Constants.GETSTATIC
//        ));
//        il.append(new PUSH(cp, callerMethodName));
//        il.append(new PUSH(cp, callerMethodSignature));
//        il.append(new PUSH(cp, fullCalleeMethodName.toString()));
//        il.append(new PUSH(cp, calleeMethodSignature));
//
//        il.append(factory.createInvoke(
//                TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS,
//                "<init>",
//                Type.VOID,
//                new Type[]{
//                    Type.STRING, new ObjectType("java.lang.Class"), Type.STRING,
//                    Type.STRING, Type.STRING, Type.STRING
//                },
//                Constants.INVOKESPECIAL
//        ));
//
//        il.append(factory.createFieldAccess(
//                cg.getClassName(),
//                joinPoint.toString(),
//                TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE,
//                Constants.PUTSTATIC
//        ));
//
//        il.append(factory.createReturn(Type.VOID));
//
//        clInit.setMaxLocals();
//        clInit.setMaxStack();
//
//        return clInit.getMethod();
//    }

    /**
     * Creates a new static join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param clInit the constructor for the class
     * @param callerMethodName the caller method name
     * @param calleeClassName the calle class name
     * @param calleeMethodName the callee method name
     * @param methodSequence the sequence number for the method
     * @param callerMethodSignature the signature for the caller method
     * @param calleeMethodSignature the signature for the callee method
     * @param factory the objectfactory
     * @param joinPointType the type of the joinpoint
     * @param uuid the UUID for the weave model
     * @return the modified clinit method
     */
//    private Method createStaticJoinPointField(
//            final ConstantPoolGen cp,
//            final ClassGen cg,
//            final Method clInit,
//            final String callerMethodName,
//            final String calleeClassName,
//            final String calleeMethodName,
//            final int methodSequence,
//            final String callerMethodSignature,
//            final String calleeMethodSignature,
//            final InstructionFactory factory,
//            final Type joinPointType,
//            final String uuid) {
//
//        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
//        final StringBuffer joinPoint = getJoinPointName(
//                joinPointPrefix, calleeMethodName, methodSequence);
//
//        StringBuffer fullCalleeMethodName = new StringBuffer();
//        fullCalleeMethodName.append(calleeClassName);
//        fullCalleeMethodName.append(TransformationUtil.CALL_SIDE_DELIMITER);
//        fullCalleeMethodName.append(calleeMethodName);
//
//        final MethodGen mg = new MethodGen(clInit, cg.getClassName(), cp);
//        final InstructionList il = mg.getInstructionList();
//
//        final InstructionHandle ih = il.getStart();
//
//        il.insert(ih, factory.createNew(TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS));
//        il.insert(ih, InstructionConstants.DUP);
//
//        il.insert(ih, new PUSH(cp, uuid));
//        il.insert(ih, factory.createFieldAccess(
//                cg.getClassName(),
//                TransformationUtil.STATIC_CLASS_FIELD,
//                new ObjectType("java.lang.Class"),
//                Constants.GETSTATIC
//        ));
//        il.insert(ih, new PUSH(cp, callerMethodName));
//        il.insert(ih, new PUSH(cp, callerMethodSignature));
//        il.insert(ih, new PUSH(cp, fullCalleeMethodName.toString()));
//        il.insert(ih, new PUSH(cp, calleeMethodSignature));
//
//        il.insert(ih, factory.createInvoke(
//                TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS,
//                "<init>",
//                Type.VOID,
//                new Type[]{
//                    Type.STRING, new ObjectType("java.lang.Class"), Type.STRING,
//                    Type.STRING, Type.STRING, Type.STRING
//                },
//                Constants.INVOKESPECIAL));
//
//        il.insert(ih, factory.createFieldAccess(
//                cg.getClassName(),
//                joinPoint.toString(),
//                TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE,
//                Constants.PUTSTATIC
//        ));
//
//        mg.setMaxStack();
//        mg.setMaxLocals();
//        return mg.getMethod();
//    }

    /**
     * Insert the pre advices for the field.
     *
     * @param il the instruction list
     * @param before the instruction handler to insert it before
     * @param cg the ClassGen
     * @param fieldName the name of the field
     * @param methodSequence the sequence number for the method
     * @param factory the objectfactory
     * @param joinPointType the type of the join point
     */
//    private void insertPreAdvice(final InstructionList il,
//                                 final InstructionHandle before,
//                                 final ClassGen cg,
//                                 final String fieldName,
//                                 final int methodSequence,
//                                 final InstructionFactory factory,
//                                 final Type joinPointType) {
//
//        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
//        final String joinPointClass = getJoinPointClass(joinPointType);
//
//        final StringBuffer joinPoint = getJoinPointName(
//                joinPointPrefix, fieldName, methodSequence);
//
//        il.insert(before, factory.createFieldAccess(
//                cg.getClassName(),
//                joinPoint.toString(),
//                joinPointType,
//                Constants.GETSTATIC
//        ));
//
//        il.insert(before, factory.createInvoke(joinPointClass,
//                TransformationUtil.CALLER_SIDE_JOIN_POINT_PRE_EXECUTION_METHOD,
//                Type.VOID,
//                Type.NO_ARGS,
//                Constants.INVOKEVIRTUAL
//        ));
//    }

    /**
     * Insert the post advices for the field.
     *
     * @param il the instruction list
     * @param before the instruction handle to insert before
     * @param cg the ClassGen
     * @param fieldName the name of the field
     * @param methodSequence the sequence number for the method
     * @param factory the objectfactory
     * @param joinPointType the type of the join point
     */
//    private void insertPostAdvice(final InstructionList il,
//                                  final InstructionHandle before,
//                                  final ClassGen cg,
//                                  final String calleeMethodName,
//                                  final int methodSequence,
//                                  final InstructionFactory factory,
//                                  final Type joinPointType) {
//
//        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
//        final String joinPointClass = getJoinPointClass(joinPointType);
//
//        final StringBuffer joinPoint = getJoinPointName(
//                joinPointPrefix, calleeMethodName, methodSequence);
//
//        il.insert(before, factory.createFieldAccess(
//                cg.getClassName(),
//                joinPoint.toString(),
//                joinPointType,
//                Constants.GETSTATIC
//        ));
//
//        il.insert(before, factory.createInvoke(
//                joinPointClass,
//                TransformationUtil.CALLER_SIDE_JOIN_POINT_POST_EXECUTION_METHOD,
//                Type.VOID,
//                Type.NO_ARGS,
//                Constants.INVOKEVIRTUAL
//        ));
//    }

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
    private boolean classFilter(final ClassMetaData classMetaData, final CtClass cg) {
        if (cg.isInterface()) {
            return true;
        }
        String className = cg.getName();
        if (m_definition.inExcludePackage(className)) {
            return true;
        }
        if (!m_definition.inIncludePackage(className)) {
            return true;
        }
        if (m_definition.hasCallPointcut(classMetaData)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the caller methods.
     *
     * @param method the method to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean methodFilterCaller(final CtBehavior method) {
        if (Modifier.isNative(method.getModifiers()) ||
                Modifier.isInterface(method.getModifiers()) ||
                method.getName().equals(TransformationUtil.GET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.SET_META_DATA_METHOD) ||
                method.getName().equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
                method.getName().equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Filters the callee methods.
     *
     * @param methodName the name of method to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean methodFilterCallee(final String methodName) {
        if (methodName.equals("<init>") ||
                methodName.equals("<clinit>") ||
                methodName.startsWith(TransformationUtil.ORIGINAL_METHOD_PREFIX) ||
                methodName.equals(TransformationUtil.GET_META_DATA_METHOD) ||
                methodName.equals(TransformationUtil.SET_META_DATA_METHOD) ||
                methodName.equals(TransformationUtil.CLASS_LOOKUP_METHOD) ||
                methodName.equals(TransformationUtil.GET_UUID_METHOD)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns the join point prefix.
     *
     * @param joinPointType the join point type
     * @return the join point prefix
     */
    private String getJoinPointPrefix(final String joinPointType) {
//        String joinPointPrefix;
//        if (joinPointType.equals(TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE)) {
//            joinPointPrefix = TransformationUtil.CALLER_SIDE_JOIN_POINT_PREFIX;
//        }
//        else {
//            throw new RuntimeException("call side join point type unknown: " + joinPointType);
//        }
//        return joinPointPrefix;
        return TransformationUtil.CALLER_SIDE_JOIN_POINT_PREFIX;
    }

    /**
     * Returns the join point class.
     *
     * @param joinPointType the join point type
     * @return the join point class
     */
    private String getJoinPointClass(final String joinPointType) {
//        String joinPointClass;
//        if (joinPointType.equals(TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE)) {
//            joinPointClass = TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS;
//        }
//        else {
//            throw new RuntimeException("call side join point type unknown: " + joinPointType);
//        }
//        return joinPointClass;
        return joinPointType;
    }

    /**
     * Returns the name of the join point.
     *
     * @param joinPointPrefix the prefix
     * @param methodName the name of the method
     * @param methodSequence the sequence number for the method
     * @return the name of the join point
     */
    private static StringBuffer getJoinPointName(final String joinPointPrefix,
                                                 final String methodName,
                                                 final int methodSequence) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(joinPointPrefix);
        joinPoint.append(methodName);
        joinPoint.append(TransformationUtil.DELIMITER);
        joinPoint.append(methodSequence);
        return joinPoint;
    }

    private void createStaticClassField(
                                          final CtClass cg) throws NotFoundException, CannotCompileException {
        final String className = cg.getName();

        CtField field = new CtField(cg.getClassPool().get("java.lang.Class"),
                TransformationUtil.STATIC_CLASS_FIELD,
                cg);
        field.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
        cg.addField(field, "java.lang.Class.forName(\""+className+"\")");
    }

    private void addStaticJoinPointField(final CtClass cg,
                                             final String joinPoint)
        throws NotFoundException, CannotCompileException {

            try {
                cg.getField(joinPoint);
                return;
            } catch (NotFoundException e) {
                ;//go on to add it
            }

            CtField field = new CtField(
                    cg.getClassPool().get("org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint"),
                    joinPoint,
                    cg);
            field.setModifiers(Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC);
            cg.addField(field, "new org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint(" +
                    "\"tests\", ___AW_clazz, \"testPrePostAdvicedStaticMethod\", \"()V\", \"test.attribdef.CallerSideTestHelper#invokeStaticMethodPrePost\", \"()Ljava/lang/String;\");");
        }


}
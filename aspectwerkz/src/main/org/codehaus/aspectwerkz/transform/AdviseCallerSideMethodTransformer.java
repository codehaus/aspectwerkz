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

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Field;

import org.cs3.jmangler.bceltransformer.UnextendableClassSet;
import org.cs3.jmangler.bceltransformer.CodeTransformerComponent;

import org.codehaus.aspectwerkz.definition.metadata.WeaveModel;
import org.codehaus.aspectwerkz.definition.metadata.MethodMetaData;

/**
 * Advises caller side method invocations.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bon�r</a>
 * @version $Id: AdviseCallerSideMethodTransformer.java,v 1.1.1.1 2003-05-11 15:15:09 jboner Exp $
 */
public class AdviseCallerSideMethodTransformer implements CodeTransformerComponent {
    ///CLOVER:OFF

    /**
     * Holds the weave model.
     */
    private WeaveModel m_weaveModel = WeaveModel.loadModel();

    /**
     * Constructor.
     */
    public AdviseCallerSideMethodTransformer() {
        super();
    }

    /**
     * Transforms the call side pointcuts.
     *
     * @param cs the class set.
     */
    public void transformCode(final UnextendableClassSet cs) {

        final Iterator iterator = cs.getIteratorForTransformableClasses();
        while (iterator.hasNext()) {

            final ClassGen cg = (ClassGen)iterator.next();

            if (classFilter(cg)) continue;

            final Method[] methods = cg.getMethods();

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
            final ConstantPoolGen cpg = cg.getConstantPool();
            final String className = cg.getClassName();
            final InstructionFactory factory = new InstructionFactory(cg);

            addStaticClassField(cpg, cg);

            final Set callerSideJoinPoints = new HashSet();

            Method clInitMethod = null;
            final Map methodSequences = new HashMap();
            final List newMethods = new ArrayList();
            boolean isClassAdvised = false;

            for (int i = 0; i < methods.length; i++) {
                if (methodFilter(methods[i])) continue;

                final MethodGen mg = new MethodGen(methods[i], className, cpg);
                if (methodFilter(mg.getMethod())) continue;

                final InstructionList il = mg.getInstructionList();
                if (il == null) continue;
                InstructionHandle ih = il.getStart();

                // search for all InvokeInstruction instructions and
                // inserts the call side pointcuts
                while (ih != null) {
                    final Instruction ins = ih.getInstruction();

                    if (ins instanceof INVOKESPECIAL ||
                            ins instanceof INVOKESTATIC ||
                            ins instanceof INVOKEVIRTUAL) {

                        final InvokeInstruction invokeInstruction =
                                (InvokeInstruction)ins;
                        final String callerSideMethodName =
                                invokeInstruction.getName(cpg);
                        final String callerSideMethodClassName =
                                invokeInstruction.getClassName(cpg);
                        final String signature =
                                invokeInstruction.getSignature(cpg);

                        // create the meta-data for the method
                        MethodMetaData callerSideMethodMetaData =
                                TransformationUtil.
                                createMethodMetaData(invokeInstruction, cpg);

                        // is this a caller side method pointcut?
                        if (m_weaveModel.isCallerSideMethod(
                                callerSideMethodClassName,
                                callerSideMethodMetaData)) {

                            final Type joinPointType =
                                    TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE;

                            // take care of identification of overloaded methods
                            // by inserting a sequence number
                            if (methodSequences.containsKey(callerSideMethodName)) {
                                int sequence = ((Integer)methodSequences.
                                        get(callerSideMethodName)).intValue();
                                methodSequences.remove(callerSideMethodName);
                                sequence++;
                                methodSequences.put(
                                        callerSideMethodName,
                                        new Integer(sequence));
                            }
                            else {
                                methodSequences.put(
                                        callerSideMethodName,
                                        new Integer(1));
                            }
                            final int methodSequence = ((Integer)methodSequences.
                                    get(callerSideMethodName)).intValue();

                            isClassAdvised = true;

                            insertPreAdvice(
                                    il, ih, cg,
                                    callerSideMethodName, methodSequence,
                                    factory, joinPointType);

                            insertPostAdvice(
                                    il, ih.getNext(), cg,
                                    callerSideMethodName, methodSequence,
                                    factory, joinPointType);

                            // skip the creation of the join point if we
                            // already have one
                            StringBuffer key = new StringBuffer();
                            key.append(className);
                            key.append(TransformationUtil.DELIMITER);
                            key.append(callerSideMethodName);
                            key.append(TransformationUtil.DELIMITER);
                            key.append(methodSequence);

                            if (!callerSideJoinPoints.contains(key.toString())) {
                                callerSideJoinPoints.add(key.toString());

                                addStaticJoinPointField(
                                        cpg, cg,
                                        callerSideMethodName,
                                        methodSequence,
                                        joinPointType);

                                if (hasClInitMethod) {
                                    methods[clinitIndex] =
                                            createStaticJoinPointField(
                                                    cpg, cg,
                                                    methods[clinitIndex],
                                                    callerSideMethodClassName,
                                                    callerSideMethodName,
                                                    methodSequence,
                                                    signature,
                                                    factory,
                                                    joinPointType);
                                }
                                else if (clInitMethod == null) {
                                    clInitMethod =
                                            createClInitMethodWithStaticJoinPointField(
                                                    cpg, cg,
                                                    callerSideMethodClassName,
                                                    callerSideMethodName,
                                                    methodSequence,
                                                    signature,
                                                    factory,
                                                    joinPointType);
                                }
                                else {
                                    clInitMethod =
                                            createStaticJoinPointField(
                                                    cpg, cg,
                                                    clInitMethod,
                                                    callerSideMethodClassName,
                                                    callerSideMethodName,
                                                    methodSequence,
                                                    signature,
                                                    factory,
                                                    joinPointType);
                                }
                            }
                        }
                    }
                    ih = ih.getNext();
                }

                mg.setMaxStack();
                methods[i] = mg.getMethod();
            }

            if (isClassAdvised) {
                // if we have transformed methods, create the static class field
                if (!hasClInitMethod && clInitMethod != null) {
                    clInitMethod = createStaticClassField(
                            cpg, cg,
                            clInitMethod,
                            factory);

                    newMethods.add(clInitMethod);
                }
                else {
                    methods[clinitIndex] = createStaticClassField(
                            cpg, cg,
                            methods[clinitIndex],
                            factory);
                }
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
    private void addStaticClassField(final ConstantPoolGen cp,
                                     final ClassGen cg) {

        final Field[] fields = cg.getFields();

        // check if we already have added this field
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(
                    TransformationUtil.STATIC_CLASS_FIELD))
                return;
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
     * @param methodName the name of the method
     * @param methodSequence the sequence number for the method
     * @param joinPointType the type of the join point
     */
    private void addStaticJoinPointField(final ConstantPoolGen cp,
                                         final ClassGen cg,
                                         final String methodName,
                                         final int methodSequence,
                                         final Type joinPointType) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final StringBuffer joinPoint =
                getJoinPointName(joinPointPrefix, methodName, methodSequence);

        final FieldGen field = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_FINAL | Constants.ACC_STATIC,
                TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE,
                joinPoint.toString(),
                cp);

        cg.addField(field.getField());
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
    private static Method createStaticClassField(final ConstantPoolGen cp,
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
     * Creates a new <clinit> method and creates a join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param methodsClassName the class name for the class holding the method
     * @param methodName the current method name
     * @param methodSequence the sequence number for the method
     * @param signature the signature of the method
     * @param factory the objectfactory
     * @param joinPointType the type of the joinpoint
     * @return the new method
     */
    private Method createClInitMethodWithStaticJoinPointField(
            final ConstantPoolGen cp,
            final ClassGen cg,
            final String methodsClassName,
            final String methodName,
            final int methodSequence,
            final String signature,
            final InstructionFactory factory,
            final Type joinPointType) {

        final String className = cg.getClassName();

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final StringBuffer joinPoint =
                getJoinPointName(joinPointPrefix, methodName, methodSequence);

        StringBuffer fullMethodName = new StringBuffer();
        fullMethodName.append(methodsClassName);
        fullMethodName.append(TransformationUtil.
                CALL_SIDE_DELIMITER);
        fullMethodName.append(methodName);

        final InstructionList il = new InstructionList();
        final MethodGen clInit = new MethodGen(
                Constants.ACC_STATIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[]{},
                "<clinit>",
                className,
                il, cp);

        il.append(factory.createNew(
                TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS));
        il.append(InstructionConstants.DUP);

        il.append(factory.createFieldAccess(
                cg.getClassName(),
                TransformationUtil.STATIC_CLASS_FIELD,
                new ObjectType("java.lang.Class"),
                Constants.GETSTATIC));

        il.append(new PUSH(cp, fullMethodName.toString()));
        il.append(new PUSH(cp, signature));

        il.append(factory.createInvoke(
                TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS,
                "<init>",
                Type.VOID,
                new Type[]{new ObjectType("java.lang.Class"), Type.STRING, Type.STRING},
                Constants.INVOKESPECIAL));

        il.append(factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE,
                Constants.PUTSTATIC));

        il.append(factory.createReturn(Type.VOID));

        clInit.setMaxLocals();
        clInit.setMaxStack();

        return clInit.getMethod();
    }

    /**
     * Creates a new static join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param clInit the constructor for the class
     * @param methodsClassName the class name for the class holding the method
     * @param methodName the current method name
     * @param methodSequence the sequence number for the method
     * @param signature the signature of the method
     * @param factory the objectfactory
     * @param joinPointType the type of the joinpoint
     * @return the modified clinit method
     */
    private Method createStaticJoinPointField(final ConstantPoolGen cp,
                                              final ClassGen cg,
                                              final Method clInit,
                                              final String methodsClassName,
                                              final String methodName,
                                              final int methodSequence,
                                              final String signature,
                                              final InstructionFactory factory,
                                              final Type joinPointType) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final StringBuffer joinPoint =
                getJoinPointName(joinPointPrefix, methodName, methodSequence);

        StringBuffer fullMethodName = new StringBuffer();
        fullMethodName.append(methodsClassName);
        fullMethodName.append(TransformationUtil.
                CALL_SIDE_DELIMITER);
        fullMethodName.append(methodName);

        final MethodGen mg = new MethodGen(clInit, cg.getClassName(), cp);
        final InstructionList il = mg.getInstructionList();

        final InstructionHandle ih = il.getStart();

        il.insert(ih, factory.createNew(
                TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS));
        il.insert(ih, InstructionConstants.DUP);

        il.insert(ih, factory.createFieldAccess(
                cg.getClassName(),
                TransformationUtil.STATIC_CLASS_FIELD,
                new ObjectType("java.lang.Class"),
                Constants.GETSTATIC));

        il.insert(ih, new PUSH(cp, fullMethodName.toString()));
        il.insert(ih, new PUSH(cp, signature));

        il.insert(ih, factory.createInvoke(
                TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS,
                "<init>",
                Type.VOID,
                new Type[]{new ObjectType("java.lang.Class"), Type.STRING, Type.STRING},
                Constants.INVOKESPECIAL));

        il.insert(ih, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE,
                Constants.PUTSTATIC));

        mg.setMaxStack();
        mg.setMaxLocals();
        return mg.getMethod();
    }

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
    private void insertPreAdvice(final InstructionList il,
                                 final InstructionHandle before,
                                 final ClassGen cg,
                                 final String fieldName,
                                 final int methodSequence,
                                 final InstructionFactory factory,
                                 final Type joinPointType) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final String joinPointClass = getJoinPointClass(joinPointType);

        final StringBuffer joinPoint =
                getJoinPointName(joinPointPrefix, fieldName, methodSequence);

        il.insert(before, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                joinPointType,
                Constants.GETSTATIC));

        il.insert(before, factory.createInvoke(joinPointClass,
                TransformationUtil.CALLER_SIDE_JOIN_POINT_PRE_EXECUTION_METHOD,
                Type.VOID,
                Type.NO_ARGS,
                Constants.INVOKEVIRTUAL));
    }

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
    private void insertPostAdvice(final InstructionList il,
                                  final InstructionHandle before,
                                  final ClassGen cg,
                                  final String fieldName,
                                  final int methodSequence,
                                  final InstructionFactory factory,
                                  final Type joinPointType) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final String joinPointClass = getJoinPointClass(joinPointType);

        final StringBuffer joinPoint =
                getJoinPointName(joinPointPrefix, fieldName, methodSequence);

        il.insert(before, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                joinPointType,
                Constants.GETSTATIC));

        il.insert(before, factory.createInvoke(
                joinPointClass,
                TransformationUtil.CALLER_SIDE_JOIN_POINT_POST_EXECUTION_METHOD,
                Type.VOID,
                Type.NO_ARGS,
                Constants.INVOKEVIRTUAL));
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
     * JMangler callback method. Prints a log/status message at
     * each transformation.
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
        if (cg.isInterface()) {
            return true;
        }
        else if (m_weaveModel.hasCallerSidePointcut(cg.getClassName())) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Filters the methods.
     *
     * @param method the method to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean methodFilter(final Method method) {
        if (method.isNative() || method.isInterface()) {
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
    private String getJoinPointPrefix(final Type joinPointType) {
        String joinPointPrefix;
        if (joinPointType.equals(TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE)) {
            joinPointPrefix = TransformationUtil.CALLER_SIDE_JOIN_POINT_PREFIX;
        }
        else {
            throw new RuntimeException("call side join point type unknown: " + joinPointType);
        }
        return joinPointPrefix;
    }

    /**
     * Returns the join point class.
     *
     * @param joinPointType the join point type
     * @return the join point class
     */
    private String getJoinPointClass(final Type joinPointType) {
        String joinPointClass;
        if (joinPointType.equals(TransformationUtil.CALLER_SIDE_JOIN_POINT_TYPE)) {
            joinPointClass = TransformationUtil.CALLER_SIDE_JOIN_POINT_CLASS;
        }
        else {
            throw new RuntimeException("call side join point type unknown: " + joinPointType);
        }
        return joinPointClass;
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
    ///CLOVER:ON
}

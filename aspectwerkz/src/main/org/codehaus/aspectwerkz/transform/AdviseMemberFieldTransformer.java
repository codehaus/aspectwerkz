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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;

import org.cs3.jmangler.bceltransformer.UnextendableClassSet;
import org.cs3.jmangler.bceltransformer.CodeTransformerComponent;

import org.codehaus.aspectwerkz.definition.metadata.WeaveModel;
import org.codehaus.aspectwerkz.definition.metadata.FieldMetaData;

/**
 * Transforms member fields to become "aspect-aware".
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: AdviseMemberFieldTransformer.java,v 1.1.1.1 2003-05-11 15:15:13 jboner Exp $
 */
public class AdviseMemberFieldTransformer implements CodeTransformerComponent {
    ///CLOVER:OFF

    /**
     * Holds the weave model.
     */
    private WeaveModel m_weaveModel = WeaveModel.loadModel();

    /**
     * Constructor.
     */
    public AdviseMemberFieldTransformer() {
        super();
    }

    /**
     * Transforms the fields.
     *
     * @param cs the class set.
     */
    public void transformCode(final UnextendableClassSet cs) {

        final Iterator iterator = cs.getIteratorForTransformableClasses();
        while (iterator.hasNext()) {

            final ClassGen cg = (ClassGen)iterator.next();
            if (classFilter(cg)) continue;

            final Method[] methods = cg.getMethods();

            // get the indexes for the <init> methods
            List initIndexes = new ArrayList();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("<init>")) {
                    initIndexes.add(new Integer(i));
                }
            }
            if (initIndexes.size() == 0) throw new RuntimeException("class corrupt: no <init> method found in class " + cg.getClassName());

            final ConstantPoolGen cpg = cg.getConstantPool();
            final String className = cg.getClassName();
            final InstructionFactory factory = new InstructionFactory(cg);

            final Set setFieldJoinPoints = new HashSet();
            final Set getFieldJoinPoints = new HashSet();

            for (int i = 0; i < methods.length; i++) {
                if (methodFilter(methods[i])) continue;

                final MethodGen mg = new MethodGen(
                        methods[i],
                        className,
                        cpg);

                final InstructionList il = mg.getInstructionList();
                InstructionHandle ih = il.getStart();

                // search for all GETFIELD and GETSTATIC instructions and
                // inserts the pre and post advices
                while (ih != null) {
                    final Instruction ins = ih.getInstruction();
                    if (ins instanceof GETFIELD || ins instanceof GETSTATIC) {
                        final FieldInstruction gfIns = (FieldInstruction)ins;

                        final String fieldName = gfIns.getName(cpg);
                        final String signature = gfIns.getFieldType(cpg).
                                toString() + " " + fieldName;
                        final Type joinPointType = TransformationUtil.
                                MEMBER_FIELD_GET_JOIN_POINT_TYPE;

                        final FieldMetaData fieldMetaData =
                                TransformationUtil.createFieldMetaData(gfIns, cpg);

                        if (!getFieldFilter(cg, fieldMetaData)) {

                            final String fieldClassName = gfIns.getClassName(cpg);
                            if (fieldClassName.equals(cg.getClassName())) {

                                // is NOT in static context
                                if (!mg.isStatic()) {
                                    insertPreAdvice(
                                            il, ih, cg,
                                            fieldName,
                                            factory,
                                            joinPointType);

                                    insertPostAdvice(
                                            il, ih.getNext(), cg,
                                            fieldName,
                                            factory,
                                            joinPointType);

                                    // skip the creation of the join point if we
                                    // already have one
                                    if (!getFieldJoinPoints.contains(fieldName)) {
                                        getFieldJoinPoints.add(fieldName);

                                        addJoinPointMemberField(
                                                cpg, cg,
                                                fieldName,
                                                joinPointType);

                                        // advise all the constructors
                                        for (Iterator it = initIndexes.iterator(); it.hasNext();) {
                                            final int initIndex = ((Integer)it.next()).intValue();

                                            methods[initIndex] = createJoinPointMemberField(
                                                    cpg, cg,
                                                    methods[initIndex],
                                                    factory,
                                                    fieldName,
                                                    signature,
                                                    joinPointType);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else if (ins instanceof PUTFIELD || ins instanceof PUTSTATIC) {
                        final FieldInstruction pfIns = (FieldInstruction)ins;

                        final String fieldName = pfIns.getName(cpg);
                        final String signature = pfIns.getFieldType(cpg).
                                toString() + " " + fieldName;
                        final Type joinPointType =
                                TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_TYPE;

                        final FieldMetaData fieldMetaData =
                                TransformationUtil.createFieldMetaData(pfIns, cpg);

                        if (!setFieldFilter(cg, fieldMetaData)) {

                            final String fieldClassName = pfIns.getClassName(cpg);
                            if (fieldClassName.equals(cg.getClassName())) {

                                // is NOT in static context
                                if (!mg.isStatic()) {
                                    insertPreAdvice(il, ih, cg,
                                            fieldName, factory, joinPointType);

                                    insertPostAdvice(il, ih.getNext(), cg,
                                            fieldName, factory, joinPointType);

                                    // skip the creation of the join point if we
                                    // already have one
                                    if (!setFieldJoinPoints.contains(fieldName)) {
                                        setFieldJoinPoints.add(fieldName);

                                        addJoinPointMemberField(cpg, cg,
                                                fieldName, joinPointType);

                                        // advise all the constructors
                                        for (Iterator it = initIndexes.iterator(); it.hasNext();) {
                                            final int initIndex = ((Integer)it.next()).intValue();

                                            methods[initIndex] = createJoinPointMemberField(
                                                    cpg, cg,
                                                    methods[initIndex],
                                                    factory,
                                                    fieldName,
                                                    signature,
                                                    joinPointType);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ih = ih.getNext();
                }

                mg.setMaxStack();
                methods[i] = mg.getMethod();
            }
            cg.setMethods(methods);
        }
    }

    /**
     * Adds a new join point member field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param fieldName the name of the field
     * @param joinPointType the type of join point
     */
    private void addJoinPointMemberField(final ConstantPoolGen cp,
                                         final ClassGen cg,
                                         final String fieldName,
                                         final Type joinPointType) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, fieldName);

        final FieldGen field = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_FINAL,
                joinPointType,
                joinPoint.toString(),
                cp);

        cg.addField(field.getField());
    }

    /**
     * Creates a join point member field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param init the constructor for the class
     * @param factory the objectfactory
     * @param fieldName the name of the field
     * @param signature the signature of the field
     * @param joinPointType the type of join point
     * @return the modified init method
     */
    private Method createJoinPointMemberField(final ConstantPoolGen cp,
                                              final ClassGen cg,
                                              final Method init,
                                              final InstructionFactory factory,
                                              final String fieldName,
                                              final String signature,
                                              final Type joinPointType) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final String joinPointClass = getJoinPointClass(joinPointType);

        final MethodGen mg = new MethodGen(init, cg.getClassName(), cp);
        final InstructionList il = mg.getInstructionList();

        final InstructionHandle[] ihs = il.getInstructionHandles();
        for (int i = 0; i < ihs.length; i++) {

            // inserts field instantiation into the constructor
            final InstructionHandle ih = ihs[i];
            if (!(ih.getInstruction() instanceof ReturnInstruction)) continue;

            // build the right join point member field name
            final StringBuffer joinPoint =
                    getJoinPointName(joinPointPrefix, fieldName);

            // create an new join point
            final InstructionHandle ihPost = il.insert(
                    ih, factory.createLoad(Type.OBJECT, 0));
            il.insert(ih, factory.createNew(joinPointClass));

            // load the parameters (this, methodId and the method signature)
            il.insert(ih, InstructionConstants.DUP);
            il.insert(ih, factory.createLoad(Type.OBJECT, 0));

            il.insert(ih, new PUSH(cp, signature));

            // invokes the constructor
            il.insert(ih, factory.createInvoke(
                    joinPointClass,
                    "<init>",
                    Type.VOID,
                    new Type[]{Type.OBJECT, Type.STRING},
                    Constants.INVOKESPECIAL));

            // set the join point to the member field specified
            il.insert(ih, factory.createFieldAccess(
                    cg.getClassName(),
                    joinPoint.toString(),
                    joinPointType,
                    Constants.PUTFIELD));

            il.redirectBranches(ih, ihPost);
        }
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
     * @param factory the objectfactory
     * @param joinPointType the type of the join point
     */
    private void insertPreAdvice(final InstructionList il,
                                 final InstructionHandle before,
                                 final ClassGen cg,
                                 final String fieldName,
                                 final InstructionFactory factory,
                                 final Type joinPointType) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final String joinPointClass = getJoinPointClass(joinPointType);

        final StringBuffer joinPoint =
                getJoinPointName(joinPointPrefix, fieldName);

        il.insert(before, factory.createLoad(Type.OBJECT, 0));

        il.insert(before, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                joinPointType,
                Constants.GETFIELD));

        il.insert(before, factory.createInvoke(joinPointClass,
                TransformationUtil.FIELD_JOIN_POINT_PRE_EXECUTION_METHOD,
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
     * @param factory the objectfactory
     * @param joinPointType the type of the join point
     */
    private void insertPostAdvice(final InstructionList il,
                                  final InstructionHandle before,
                                  final ClassGen cg,
                                  final String fieldName,
                                  final InstructionFactory factory,
                                  final Type joinPointType) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final String joinPointClass = getJoinPointClass(joinPointType);

        final StringBuffer joinPoint =
                getJoinPointName(joinPointPrefix, fieldName);

        il.insert(before, factory.createLoad(Type.OBJECT, 0));

        il.insert(before, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                joinPointType,
                Constants.GETFIELD));

        il.insert(before, factory.createInvoke(
                joinPointClass,
                TransformationUtil.FIELD_JOIN_POINT_POST_EXECUTION_METHOD,
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
        else {
            return !m_weaveModel.hasAspect(cg.getClassName());
        }
    }

    /**
     * Filters the PUTFIELD's to be transformed.
     *
     * @param cg the class to filter
     * @param fieldMetaData the field to filter
     * @return boolean true if the field access should be filtered away
     */
    private boolean setFieldFilter(final ClassGen cg,
                                   final FieldMetaData fieldMetaData) {
        return !m_weaveModel.hasSetFieldPointcut(cg.getClassName(), fieldMetaData);
    }

    /**
     * Filters the GETFIELD's to be transformed.
     *
     * @param cg the class to filter
     * @param fieldMetaData the field to filter
     * @return boolean true if the field access should be filtered away
     */
    private boolean getFieldFilter(final ClassGen cg,
                                   final FieldMetaData fieldMetaData) {

        return !m_weaveModel.hasGetFieldPointcut(cg.getClassName(), fieldMetaData);
    }

    /**
     * Filters the methods.
     *
     * @param method the method to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean methodFilter(final Method method) {
        return method.isNative() ||
                method.isAbstract() ||
                method.getName().equals("<init>") ||
                method.getName().equals("<clinit>");
    }

    /**
     * Returns the join point prefix.
     *
     * @param joinPointType the join point type
     * @return the join point prefix
     */
    private String getJoinPointPrefix(final Type joinPointType) {
        String joinPointPrefix;
        if (joinPointType.equals(TransformationUtil.MEMBER_FIELD_GET_JOIN_POINT_TYPE)) {
            joinPointPrefix = TransformationUtil.MEMBER_FIELD_GET_JOIN_POINT_PREFIX;
        }
        else if (joinPointType.equals(TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_TYPE)) {
            joinPointPrefix = TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_PREFIX;
        }
        else {
            throw new RuntimeException("field join point type unknown: " + joinPointType);
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
        if (joinPointType.equals(TransformationUtil.MEMBER_FIELD_GET_JOIN_POINT_TYPE)) {
            joinPointClass = TransformationUtil.MEMBER_FIELD_GET_JOIN_POINT_CLASS;
        }
        else if (joinPointType.equals(TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_TYPE)) {
            joinPointClass = TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_CLASS;
        }
        else {
            throw new RuntimeException("field join point type unknown: " + joinPointType);
        }
        return joinPointClass;
    }

    /**
     * Returns the name of the join point.
     *
     * @param joinPointPrefix the prefix
     * @param fieldName the name of the field
     * @return the name of the join point
     */
    private static StringBuffer getJoinPointName(final String joinPointPrefix,
                                                 final String fieldName) {
        final StringBuffer joinPoint = new StringBuffer();
        joinPoint.append(joinPointPrefix);
        joinPoint.append(fieldName);
        return joinPoint;
    }
    ///CLOVER:ON
}

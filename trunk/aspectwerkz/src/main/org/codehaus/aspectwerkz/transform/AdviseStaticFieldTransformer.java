/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Field;

import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;

/**
 * Transforms member fields to become "aspect-aware".
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AdviseStaticFieldTransformer implements AspectWerkzCodeTransformerComponent {

    /**
     * The definitions.
     */
    private final List m_definitions;

    /**
     * Retrieves the weave model.
     */
    public AdviseStaticFieldTransformer() {
        super();
        m_definitions = DefinitionLoader.getDefinitionsForTransformation();
    }

    /**
     * Transforms the fields.
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

            Method[] methods = cg.getMethods();

            // get the index for the <clinit> method (if there is one)
            boolean noClInitMethod = true;
            int clinitIndex = -1;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("<clinit>")) {
                    clinitIndex = i;
                    noClInitMethod = false;
                    break;
                }
            }
            ConstantPoolGen cpg = cg.getConstantPool();
            String className = cg.getClassName();
            InstructionFactory factory = new InstructionFactory(cg);

            final Set setFieldJoinPoints = new HashSet();
            final Set getFieldJoinPoints = new HashSet();

            Method clInitMethod = null;//only used if noclinitMethod
            boolean isClassAdvised = false;
            boolean isMethodAdvised = false;
            for (int i = 0; i < methods.length; i++) {

                if (methodFilter(methods[i])) {
                    continue;
                }
                isMethodAdvised = false;
                MethodGen mg = new MethodGen(methods[i], className, cpg);

                // get the current field instruction
                FieldInstruction currentGetFieldIns = null;

                InstructionList il = mg.getInstructionList();
                InstructionHandle ih = il.getStart();

                // search for all GETSTATIC and PUTSTATIC instructions and
                // inserts the pre and post advices
                // handle GETFIELD followed by INVOKEINTERFACE on Collection like carrefully
                while (ih != null) {
                    Instruction ins = ih.getInstruction();

                    // handle the java.util.Collection classes
                    if (ins instanceof GETSTATIC) {
                        FieldInstruction checkMe = (FieldInstruction)ins;
                        // if the field is an added join point field => skip it
                        // needed if a field of type collection is both setField
                        // and getField advised
                        if (!checkMe.getFieldName(cpg).startsWith(TransformationUtil.JOIN_POINT_PREFIX)) {
                            currentGetFieldIns = checkMe;
                            Instruction next = ih.getNext().getInstruction();
                            if (next instanceof INVOKEINTERFACE) {
                                // handle the INVOKEINTERFACE instruction
                                final InvokeInstruction invokeIns = (InvokeInstruction)next;

                                // do we have a collection?
                                if (invokeIns.getClassName(cpg).equals("java.util.Collection") ||
                                        invokeIns.getClassName(cpg).equals("java.util.Enumeration") ||
                                        invokeIns.getClassName(cpg).equals("java.util.Iterator") ||
                                        invokeIns.getClassName(cpg).equals("java.util.List") ||
                                        invokeIns.getClassName(cpg).equals("java.util.Map") ||
                                        invokeIns.getClassName(cpg).equals("java.util.Set") ||
                                        invokeIns.getClassName(cpg).equals("java.util.SortedMap") ||
                                        invokeIns.getClassName(cpg).equals("java.util.SortedSet")) {

                                    String methodName = invokeIns.getName(cpg);

                                    // is the collection modified (not only accessed or single PUTSTATIC instr)?
                                    if (methodName.equals("add") ||
                                            methodName.equals("addAll") ||
                                            methodName.equals("set") ||
                                            methodName.equals("remove") ||
                                            methodName.equals("removeAll") ||
                                            methodName.equals("retainAll") ||
                                            methodName.equals("clear") ||
                                            methodName.equals("put") ||
                                            methodName.equals("putAll")) {

                                        final String fieldName = currentGetFieldIns.getName(cpg);
                                        final String signature = currentGetFieldIns.getFieldType(cpg).
                                                toString() + " " + fieldName;
                                        final Type joinPointType = TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_TYPE;
                                        final String joinPointClass = TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_CLASS;

                                        FieldMetaData fieldMetaData =
                                                BcelMetaDataMaker.createFieldMetaData(currentGetFieldIns, cpg);

                                        String uuid = setFieldFilter(definition, classMetaData, fieldMetaData);

                                        // do we have to set a joinpoint ?
                                        if (uuid != null) {
                                            final String fieldClassName = currentGetFieldIns.getClassName(cpg);

                                            if (fieldClassName.equals(cg.getClassName())) {
                                                isMethodAdvised = true;

                                                insertPreAdvice(il, ih, cg, fieldName, factory, joinPointType);
                                                insertPostAdvice(il, ih.getNext().getNext(), cg, fieldName, factory, joinPointType);

                                                // skip the creation of the set join point if we
                                                // already have one
                                                if (!setFieldJoinPoints.contains(fieldName)) {
                                                    setFieldJoinPoints.add(fieldName);

                                                    addStaticJoinPointField(cpg, cg, fieldName, joinPointType);

                                                    if (noClInitMethod && clInitMethod == null) {
                                                        // were no clinit and first creation of clinit
                                                        clInitMethod = createClInitMethodWithStaticJoinPointField(
                                                                cpg, cg, fieldName, signature, factory,
                                                                joinPointType, joinPointClass, uuid);
                                                    }
                                                    else if (noClInitMethod) {
                                                        // we are modyfing the newly created clinit
                                                        clInitMethod = createStaticJoinPointField(
                                                                cpg, cg, clInitMethod, fieldName, signature,
                                                                factory, joinPointType, joinPointClass, uuid);
                                                    }
                                                    else {
                                                        // the clinit method was existing
                                                        methods[clinitIndex] = createStaticJoinPointField(
                                                                cpg, cg, methods[clinitIndex], fieldName, signature,
                                                                factory, joinPointType, joinPointClass, uuid);
                                                    }
                                                }

                                                // add one step more to the InstructionList (GETFIELD(current) INVOKEINTERFACE with jp)
                                                ih = ih.getNext();
                                                ins = ih.getInstruction();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // handle the GETSTATIC instructions
                    // (if we have set a getstatic for collection modification jp the ins has been altered)
                    if (ins instanceof GETSTATIC) {
                        FieldInstruction gfIns = (FieldInstruction)ins;

                        String fieldName = gfIns.getName(cpg);
                        String signature = gfIns.getFieldType(cpg).toString() + " " + fieldName;
                        Type joinPointType = TransformationUtil.STATIC_FIELD_GET_JOIN_POINT_TYPE;
                        String joinPointClass = TransformationUtil.STATIC_FIELD_GET_JOIN_POINT_CLASS;

                        FieldMetaData fieldMetaData =
                                BcelMetaDataMaker.createFieldMetaData(gfIns, cpg);

                        String uuid = getFieldFilter(definition, classMetaData, fieldMetaData);
                        if (uuid != null) {

                            String fieldClassName = gfIns.getClassName(cpg);
                            if (fieldClassName.equals(cg.getClassName())) {
                                isMethodAdvised = true;

                                insertPreAdvice(il, ih, cg, fieldName, factory, joinPointType);
                                insertPostAdvice(il, ih.getNext(), cg, fieldName, factory, joinPointType);

                                // skip the creation of the join point if we
                                // already have one
                                if (!getFieldJoinPoints.contains(fieldName)) {
                                    getFieldJoinPoints.add(fieldName);

                                    addStaticJoinPointField(cpg, cg, fieldName, joinPointType);

                                    if (noClInitMethod && clInitMethod == null) {
                                        // were no clinit and first creation of clinit
                                        clInitMethod = createClInitMethodWithStaticJoinPointField(
                                                cpg, cg, fieldName, signature, factory,
                                                joinPointType, joinPointClass, uuid);
                                    }
                                    else if (noClInitMethod) {
                                        // we are modyfing the newly created clinit
                                        clInitMethod = createStaticJoinPointField(
                                                cpg, cg, clInitMethod, fieldName, signature,
                                                factory, joinPointType, joinPointClass, uuid);
                                    }
                                    else {
                                        // the clinit method was existing
                                        methods[clinitIndex] = createStaticJoinPointField(
                                                cpg, cg, methods[clinitIndex], fieldName, signature,
                                                factory, joinPointType, joinPointClass, uuid);
                                    }
                                }
                            }
                        }
                    }
                    else if (ins instanceof PUTSTATIC) {
                        FieldInstruction pfIns = (FieldInstruction)ins;

                        String fieldName = pfIns.getName(cpg);
                        String signature = pfIns.getFieldType(cpg).toString() + " " + fieldName;
                        Type joinPointType = TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_TYPE;
                        String joinPointClass = TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_CLASS;

                        FieldMetaData fieldMetaData =
                                BcelMetaDataMaker.createFieldMetaData(pfIns, cpg);

                        String uuid = setFieldFilter(definition, classMetaData, fieldMetaData);
                        if (uuid != null) {

                            String fieldClassName = pfIns.getClassName(cpg);
                            if (fieldClassName.equals(cg.getClassName())) {
                                isMethodAdvised = true;

                                insertPreAdvice(il, ih, cg, fieldName, factory, joinPointType);
                                insertPostAdvice(il, ih.getNext(), cg, fieldName, factory, joinPointType);

                                // skip the creation of the join point if we
                                // already have one
                                if (!setFieldJoinPoints.contains(fieldName)) {
                                    setFieldJoinPoints.add(fieldName);

                                    addStaticJoinPointField(
                                            cpg, cg,
                                            fieldName,
                                            joinPointType);

                                    if (noClInitMethod && clInitMethod == null) {
                                        clInitMethod = createClInitMethodWithStaticJoinPointField(
                                                cpg, cg, fieldName, signature, factory,
                                                joinPointType, joinPointClass, uuid);
                                    }
                                    else if (noClInitMethod) {
                                        clInitMethod = createStaticJoinPointField(
                                                cpg, cg, clInitMethod, fieldName, signature,
                                                factory, joinPointType, joinPointClass, uuid);
                                    }
                                    else {
                                        methods[clinitIndex] = createStaticJoinPointField(
                                                cpg, cg, methods[clinitIndex], fieldName, signature,
                                                factory, joinPointType, joinPointClass, uuid);
                                    }
                                }
                            }
                        }
                    }
                    ih = ih.getNext();
                }

                if (isMethodAdvised) {
                    mg.setMaxStack();
                    methods[i] = mg.getMethod();
                    isClassAdvised = true;
                }
            }
            if (isClassAdvised) {
                context.markAsAdvised();
                // if we have transformed methods, create the static class field
                addStaticClassField(cpg, cg);
                if (noClInitMethod) {
                    // clinitMethod was created during TF since isClassAdvise=true in this scope
                    clInitMethod = createStaticClassField(cpg, cg, clInitMethod, factory);
                }
                else {
                    methods[clinitIndex] = createStaticClassField(cpg, cg, methods[clinitIndex], factory);
                }

                // update the old methods
                cg.setMethods(methods);

                // did we had to create clinit method ?
                if (noClInitMethod) {
                    cg.addMethod(clInitMethod);
                }
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
            if (fields[i].getName().equals(TransformationUtil.STATIC_CLASS_FIELD))
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
     * Creates a static join point get field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param fieldName the name of the field
     * @param joinPointType the type of the join point
     */
    private void addStaticJoinPointField(final ConstantPoolGen cp,
                                         final ClassGen cg,
                                         final String fieldName,
                                         final Type joinPointType) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, fieldName);

        if (cg.containsField(joinPoint.toString()) != null) {
            return;
        }

        final FieldGen field = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_FINAL | Constants.ACC_STATIC,
                joinPointType,
                joinPoint.toString(),
                cp);

        cg.addField(field.getField());
    }

    /**
     * Creates a new <clinit> method and creates a join point field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param fieldName the current method name
     * @param signature the signature of the method
     * @param factory the objectfactory
     * @param joinPointType the type of the joinpoint
     * @param joinPointClass the class for the joinpoint
     * @param uuid the UUID for the weave model
     * @return the new method
     */
    private Method createClInitMethodWithStaticJoinPointField(
            final ConstantPoolGen cp,
            final ClassGen cg,
            final String fieldName,
            final String signature,
            final InstructionFactory factory,
            final Type joinPointType,
            final String joinPointClass,
            final String uuid) {

        final String className = cg.getClassName();

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, fieldName);

        final InstructionList il = new InstructionList();
        final MethodGen clInit = new MethodGen(
                Constants.ACC_STATIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[]{},
                "<clinit>",
                className,
                il, cp);

        // create an new join point
        il.append(factory.createNew(joinPointClass));

        // load the parameters (uuid, this, field signature)
        il.append(InstructionConstants.DUP);
        il.append(new PUSH(cp, uuid));
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                TransformationUtil.STATIC_CLASS_FIELD,
                new ObjectType("java.lang.Class"),
                Constants.GETSTATIC));
        il.append(new PUSH(cp, signature));

        // invokes the constructor
        il.append(factory.createInvoke(
                joinPointClass,
                "<init>",
                Type.VOID,
                new Type[]{Type.STRING, new ObjectType("java.lang.Class"), Type.STRING},
                Constants.INVOKESPECIAL));

        // set the join point to the static field specified
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                joinPointType,
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
     * @param methodName the current method name
     * @param signature the signature of the method
     * @param factory the objectfactory
     * @param joinPointType the type of the joinpoint
     * @param joinPointClass the class for the joinpoint
     * @param uuid the UUID for the weave model
     * @return the modified clinit method
     */
    private Method createStaticJoinPointField(final ConstantPoolGen cp,
                                              final ClassGen cg,
                                              final Method clInit,
                                              final String methodName,
                                              final String signature,
                                              final InstructionFactory factory,
                                              final Type joinPointType,
                                              final String joinPointClass,
                                              final String uuid) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, methodName);

        final MethodGen mg = new MethodGen(clInit, cg.getClassName(), cp);
        final InstructionList il = mg.getInstructionList();

        final InstructionHandle ih = il.getStart();

        // create an new join point
        il.insert(ih, factory.createNew(joinPointClass));

        // load the parameters (uuid, this, field signature)
        il.insert(ih, InstructionConstants.DUP);
        il.insert(ih, new PUSH(cp, uuid));
        il.insert(ih, factory.createFieldAccess(
                cg.getClassName(),
                TransformationUtil.STATIC_CLASS_FIELD,
                new ObjectType("java.lang.Class"),
                Constants.GETSTATIC));
        il.insert(ih, new PUSH(cp, signature));

        // invokes the constructor
        il.insert(ih, factory.createInvoke(
                joinPointClass,
                "<init>",
                Type.VOID,
                new Type[]{Type.STRING, new ObjectType("java.lang.Class"), Type.STRING},
                Constants.INVOKESPECIAL));

        // set the join point to the static field specified
        il.insert(ih, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                joinPointType,
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

        final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, fieldName);

        il.insert(before, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                joinPointType,
                Constants.GETSTATIC));

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

        final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, fieldName);

        il.insert(before, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                joinPointType,
                Constants.GETSTATIC));

        il.insert(before, factory.createInvoke(
                joinPointClass,
                TransformationUtil.FIELD_JOIN_POINT_POST_EXECUTION_METHOD,
                Type.VOID,
                Type.NO_ARGS,
                Constants.INVOKEVIRTUAL));
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
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.attribdef.aspect.Aspect") ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice") ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.xmldef.advice.PreAdvice") ||
                TransformationUtil.hasSuperClass(classMetaData, "org.codehaus.aspectwerkz.xmldef.advice.PostAdvice")) {
            return true;
        }
        String className = cg.getClassName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
        if (definition.hasSetPointcut(classMetaData) ||
                definition.hasGetPointcut(classMetaData)) {
            return false;
        }
        return true;
    }

    /**
     * Filters the methods.
     *
     * @param method the method to filter
     * @return boolean true if the method should be filtered away
     */
    private boolean methodFilter(final Method method) {
        return method.isNative() || method.isAbstract();
    }

    /**
     * Filters the PUTFIELD's to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return the UUID for the weave model
     */
    private String setFieldFilter(final AspectWerkzDefinition definition,
                                  final ClassMetaData classMetaData,
                                  final FieldMetaData fieldMetaData) {
        if (fieldMetaData.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
            return null;
        }
        if (definition.hasSetPointcut(classMetaData, fieldMetaData)) {
            return definition.getUuid();
        }
        return null;
    }

    /**
     * Filters the GETFIELD's to be transformed.
     *
     * @param definition the definition
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return the UUID for the weave model
     */
    private String getFieldFilter(final AspectWerkzDefinition definition,
                                  final ClassMetaData classMetaData,
                                  final FieldMetaData fieldMetaData) {
        if (fieldMetaData.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
            return null;
        }
        if (definition.hasGetPointcut(classMetaData, fieldMetaData)) {
            return definition.getUuid();
        }
        return null;
    }

    /**
     * Returns the join point prefix.
     *
     * @param joinPointType the join point type
     * @return the join point prefix
     */
    private String getJoinPointPrefix(final Type joinPointType) {
        String joinPointPrefix;
        if (joinPointType.equals(TransformationUtil.STATIC_FIELD_GET_JOIN_POINT_TYPE)) {
            joinPointPrefix = TransformationUtil.STATIC_FIELD_GET_JOIN_POINT_PREFIX;
        }
        else if (joinPointType.equals(TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_TYPE)) {
            joinPointPrefix = TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_PREFIX;
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
        if (joinPointType.equals(TransformationUtil.STATIC_FIELD_GET_JOIN_POINT_TYPE)) {
            joinPointClass = TransformationUtil.STATIC_FIELD_GET_JOIN_POINT_CLASS;
        }
        else if (joinPointType.equals(TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_TYPE)) {
            joinPointClass = TransformationUtil.STATIC_FIELD_SET_JOIN_POINT_CLASS;
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
}

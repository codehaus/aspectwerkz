/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
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
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;

import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.xmldef.definition.AspectWerkzDefinition;

/**
 * Transforms member fields to become "aspect-aware".
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviseMemberFieldTransformer implements AspectWerkzCodeTransformerComponent {
    ///CLOVER:OFF

    /**
     * The definition.
     */
    private final AspectWerkzDefinition m_definition;

    /**
     * Retrieves the weave model.
     */
    public AdviseMemberFieldTransformer() {
        super();
        m_definition = AspectWerkzDefinition.getDefinitionForTransformation();
    }

    /**
     * Transforms the fields.
     *
     * @param context the transformation context
     * @param klass the class set.
     */
    public void transformCode(final Context context, final Klass klass) {
        final ClassGen cg = klass.getClassGen();
        ClassMetaData classMetaData = BcelMetaDataMaker.
                createClassMetaData(context.getJavaClass(cg));

        if (classFilter(classMetaData, cg)) {
            return;
        }

        final Method[] methods = cg.getMethods();

        // get the indexes for the <init> methods
        List initIndexes = new ArrayList();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("<init>")) {
                initIndexes.add(new Integer(i));
            }
        }

        final ConstantPoolGen cpg = cg.getConstantPool();
        final String className = cg.getClassName();
        final InstructionFactory factory = new InstructionFactory(cg);

        final Set setFieldJoinPoints = new HashSet();
        final Set getFieldJoinPoints = new HashSet();

        boolean isClassAdvised = false;
        for (int i = 0; i < methods.length; i++) {

            // filter methods
            if (methodFilter(methods[i])) {
                continue;
            }

            MethodGen mg = new MethodGen(methods[i], className, cpg);

            // do not modify anything within the hidden system methods
            if (mg.getMethod().getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                continue;
            }

            InstructionList il = mg.getInstructionList();
            InstructionHandle ih = il.getStart();

            // get the current field instruction
            FieldInstruction currentGetFieldIns = null;

            // search for all GETFIELD and GETSTATIC instructions and
            // inserts the pre and post advices
            while (ih != null) {
                Instruction ins = ih.getInstruction();

                // handle the java.util.Collection classes
                if (ins instanceof GETFIELD || ins instanceof GETSTATIC) {
                    FieldInstruction checkMe = (FieldInstruction)ins;
                    // if the field is an added join point field => skip it
                    // needed if a field of type collection is both setField
                    // and getField advised
                    if (!checkMe.getFieldName(cpg).startsWith(TransformationUtil.JOIN_POINT_PREFIX)) {
                        currentGetFieldIns = checkMe;
                    }
                }
                if (ins instanceof INVOKEINTERFACE) {
                    final InvokeInstruction invokeIns = (InvokeInstruction)ins;

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

                        // is the collection modified?
                        if (methodName.equals("add") ||
                                methodName.equals("addAll") ||
                                methodName.equals("set") ||
                                methodName.equals("remove") ||
                                methodName.equals("removeAll") ||
                                methodName.equals("retainAll") ||
                                methodName.equals("clear") ||
                                methodName.equals("put") ||
                                methodName.equals("putAll")) {

                            if (currentGetFieldIns == null) {
                                // is not a member field, continue
                                ih = ih.getNext();
                                continue;
                            }

                            final String fieldName = currentGetFieldIns.getName(cpg);
                            final String signature = currentGetFieldIns.getFieldType(cpg).
                                    toString() + " " + fieldName;
                            final Type joinPointType = TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_TYPE;

                            FieldMetaData fieldMetaData =
                                    BcelMetaDataMaker.createFieldMetaData(currentGetFieldIns, cpg);

                            String uuid = setFieldFilter(classMetaData, fieldMetaData);

                            if (uuid != null) {
                                final String fieldClassName = currentGetFieldIns.getClassName(cpg);

                                if (fieldClassName.equals(cg.getClassName())) {

                                    // is NOT in static context
                                    if (!mg.isStatic()) {
                                        isClassAdvised = true;

                                        insertPreAdvice(
                                                il, ih, cg, fieldName,
                                                factory, joinPointType);

                                        insertPostAdvice(
                                                il, ih.getNext(), cg,
                                                fieldName, factory, joinPointType);

                                        // store the join point field data
                                        JoinPointFieldData data = new JoinPointFieldData(
                                                fieldName, signature, joinPointType, uuid);

                                        if (!setFieldJoinPoints.contains(data)) {
                                            setFieldJoinPoints.add(data);
                                        }
                                    }
                                }
                                // set the current get field instruction to null
                                currentGetFieldIns = null;
                            }
                        }
                    }
                }
                // handle the getField instructions
                else if (ins instanceof GETFIELD || ins instanceof GETSTATIC) {
                    final FieldInstruction gfIns = (FieldInstruction)ins;

                    String fieldName = gfIns.getName(cpg);
                    String signature = gfIns.getFieldType(cpg).toString() + " " + fieldName;
                    Type joinPointType = TransformationUtil.MEMBER_FIELD_GET_JOIN_POINT_TYPE;

                    FieldMetaData fieldMetaData = BcelMetaDataMaker.createFieldMetaData(gfIns, cpg);

                    String uuid = getFieldFilter(classMetaData, fieldMetaData);
                    if (uuid != null) {

                        final String fieldClassName = gfIns.getClassName(cpg);
                        if (fieldClassName.equals(cg.getClassName())) {

                            // is NOT in static context
                            if (!mg.isStatic()) {
                                isClassAdvised = true;

                                insertPreAdvice(
                                        il, ih, cg, fieldName,
                                        factory, joinPointType);

                                insertPostAdvice(
                                        il, ih.getNext(), cg,
                                        fieldName, factory, joinPointType);

                                // store the join point field data
                                JoinPointFieldData data = new JoinPointFieldData(
                                        fieldName, signature, joinPointType, uuid);

                                if (!getFieldJoinPoints.contains(data)) {
                                    getFieldJoinPoints.add(data);
                                }
                            }
                        }
                    }
                }
                // handle the setField instructions
                else if (ins instanceof PUTFIELD || ins instanceof PUTSTATIC) {
                    final FieldInstruction pfIns = (FieldInstruction)ins;

                    String fieldName = pfIns.getName(cpg);
                    String signature = pfIns.getFieldType(cpg).toString() + " " + fieldName;
                    Type joinPointType = TransformationUtil.MEMBER_FIELD_SET_JOIN_POINT_TYPE;

                    FieldMetaData fieldMetaData =
                            BcelMetaDataMaker.createFieldMetaData(pfIns, cpg);

                    String uuid = setFieldFilter(classMetaData, fieldMetaData);
                    if (uuid != null) {

                        final String fieldClassName = pfIns.getClassName(cpg);
                        if (fieldClassName.equals(cg.getClassName())) {

                            // is NOT in static context
                            if (!mg.isStatic()) {
                                isClassAdvised = true;

                                insertPreAdvice(
                                        il, ih, cg, fieldName,
                                        factory, joinPointType);

                                insertPostAdvice(
                                        il, ih.getNext(), cg,
                                        fieldName, factory, joinPointType);

                                // store the join point field data
                                JoinPointFieldData data = new JoinPointFieldData(
                                        fieldName, signature, joinPointType, uuid);

                                if (!setFieldJoinPoints.contains(data)) {
                                    setFieldJoinPoints.add(data);
                                }
                            }
                        }
                    }
                }
                ih = ih.getNext();
            }

            if (isClassAdvised) {
                mg.setMaxStack();
                methods[i] = mg.getMethod();
            }
        }

        // create the set field join point member fields
        for (Iterator it = setFieldJoinPoints.iterator(); it.hasNext();) {
            JoinPointFieldData data = (JoinPointFieldData)it.next();
            addJoinPointMemberField(cpg, cg, data.getName(), data.getType());

            // advise all the constructors
            for (Iterator it2 = initIndexes.iterator(); it2.hasNext();) {
                final int initIndex = ((Integer)it2.next()).intValue();

                methods[initIndex] = createJoinPointMemberField(
                        cpg, cg, methods[initIndex], factory,
                        data.getName(), data.getSignature(),
                        data.getType(), data.getUuid());

            }
        }
        // create the get field join point member fields
        for (Iterator it = getFieldJoinPoints.iterator(); it.hasNext();) {
            JoinPointFieldData data = (JoinPointFieldData)it.next();
            addJoinPointMemberField(cpg, cg, data.getName(), data.getType());

            // advise all the constructors
            for (Iterator it2 = initIndexes.iterator(); it2.hasNext();) {
                final int initIndex = ((Integer)it2.next()).intValue();

                methods[initIndex] = createJoinPointMemberField(
                        cpg, cg, methods[initIndex], factory,
                        data.getName(), data.getSignature(),
                        data.getType(), data.getUuid());

            }
        }

        cg.setMethods(methods);
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

        if (cg.containsField(joinPoint.toString()) != null) {
            return;
        }

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
     * @param the UUID for the weave model
     * @return the modified init method
     */
    private Method createJoinPointMemberField(final ConstantPoolGen cp,
                                              final ClassGen cg,
                                              final Method init,
                                              final InstructionFactory factory,
                                              final String fieldName,
                                              final String signature,
                                              final Type joinPointType,
                                              final String uuid) {

        final String joinPointPrefix = getJoinPointPrefix(joinPointType);
        final String joinPointClass = getJoinPointClass(joinPointType);

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

        // build the right join point member field name
        final StringBuffer joinPoint = getJoinPointName(joinPointPrefix, fieldName);

        // create an new join point
        final InstructionHandle ihPost = il.insert(ih, factory.createLoad(Type.OBJECT, 0));
        il.insert(ih, factory.createNew(joinPointClass));

        // load the parameters (uuid, this, field signature)
        il.insert(ih, InstructionConstants.DUP);
        il.insert(ih, new PUSH(cp, uuid));
        il.insert(ih, factory.createLoad(Type.OBJECT, 0));
        il.insert(ih, new PUSH(cp, signature));

        // invokes the constructor
        il.insert(ih, factory.createInvoke(
                joinPointClass,
                "<init>",
                Type.VOID,
                new Type[]{Type.STRING, Type.OBJECT, Type.STRING},
                Constants.INVOKESPECIAL));

        // set the join point to the member field specified
        il.insert(ih, factory.createFieldAccess(
                cg.getClassName(),
                joinPoint.toString(),
                joinPointType,
                Constants.PUTFIELD));

        il.redirectBranches(ih, ihPost);

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
        if (cg.isInterface()) {
            return true;
        }
        if (!m_definition.inTransformationScope(cg.getClassName())) {
            return true;
        }

        if (m_definition.hasSetFieldPointcut(classMetaData) ||
                m_definition.hasGetFieldPointcut(classMetaData)) {
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
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return the UUID for the weave model
     */
    private String setFieldFilter(final ClassMetaData classMetaData,
                                  final FieldMetaData fieldMetaData) {
        if (m_definition.hasSetFieldPointcut(classMetaData, fieldMetaData)) {
            return m_definition.getUuid();
        }
        return null;
    }

    /**
     * Filters the GETFIELD's to be transformed.
     *
     * @param classMetaData the class to filter
     * @param fieldMetaData the field to filter
     * @return the UUID for the weave model
     */
    private String getFieldFilter(final ClassMetaData classMetaData,
                                  final FieldMetaData fieldMetaData) {
        if (m_definition.hasGetFieldPointcut(classMetaData, fieldMetaData)) {
            return m_definition.getUuid();
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

    /**
     * Container for the join point field data.
     */
    private static class JoinPointFieldData {

        private String m_name;
        private String m_signature;
        private Type m_type;
        private String m_uuid;

        public JoinPointFieldData(final String name,
                                  final String signature,
                                  final Type type,
                                  final String uuid) {
            m_name = name;
            m_signature = signature;
            m_type = type;
            m_uuid = uuid;
        }

        public String getName() {
            return m_name;
        }

        public String getSignature() {
            return m_signature;
        }

        public Type getType() {
            return m_type;
        }

        public String getUuid() {
            return m_uuid;
        }

        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof JoinPointFieldData)) return false;
            final JoinPointFieldData obj = (JoinPointFieldData)o;
            return areEqualsOrBothNull(obj.m_name, this.m_name) &&
                    areEqualsOrBothNull(obj.m_uuid, this.m_uuid) &&
                    areEqualsOrBothNull(obj.m_signature, this.m_signature);
        }

        public int hashCode() {
            int result = 17;
            result = 37 * result + hashCodeOrZeroIfNull(m_name);
            result = 37 * result + hashCodeOrZeroIfNull(m_uuid);
            result = 37 * result + hashCodeOrZeroIfNull(m_signature);
            return result;
        }

        protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
            if (null == o1) return (null == o2);
            return o1.equals(o2);
        }

        protected static int hashCodeOrZeroIfNull(final Object o) {
            if (null == o) return 19;
            return o.hashCode();
        }
    }

///CLOVER:ON
}

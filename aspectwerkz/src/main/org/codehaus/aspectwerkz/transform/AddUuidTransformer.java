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
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.ConstantClass;

import org.codehaus.aspectwerkz.metadata.WeaveModel;

/**
 * Adds an UuidGenerator to all transformed classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public final class AddUuidTransformer
        implements AspectWerkzCodeTransformerComponent, AspectWerkzInterfaceTransformerComponent {
    ///CLOVER:OFF

    /**
     * Holds references to the classes that have already been transformed by this
     * transformer.
     */
    private final Set m_hasBeenTransformed = new HashSet();

    /**
     * Holds the weave model.
     */
    private final WeaveModel m_weaveModel;

    /**
     * Flag to tell the transformer to do transformations or not.
     */
    private static final String ADD_UUID = System.getProperty("aspectwerkz.add.uuid", null);

    /**
     * Retrieves the weave model.
     */
    public AddUuidTransformer() {
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
     * Adds a UUID to all the transformed classes.
     *
     * @param context the transformation context
     * @param klass the unextendable class set
     */
    public void transformInterface(final Context context, final Klass klass) {
        if (ADD_UUID == null) return; // do not do any transformations

        final ClassGen cg = klass.getClassGen();
        final ConstantPoolGen cpg = cg.getConstantPool();
        final InstructionFactory factory = new InstructionFactory(cg);

        if (classFilter(cg)) {
            return;
        }
        if (m_hasBeenTransformed.contains(cg.getClassName())) {
            return;
        }

        // mark the class as transformed
        m_hasBeenTransformed.add(cg.getClassName());

        addIdentifiableInterface(cg, cpg);
        addUuidField(cg);
        addUuidGetterMethod(cg, cpg, factory);
    }

    /**
     * Makes the member method transformations.
     *
     * @param context the transformation context
     * @param klass the class.
     */
    public void transformCode(final Context context, final Klass klass) {
        if (ADD_UUID == null) return; // do not do any transformations

        final ClassGen cg = klass.getClassGen();
        if (classFilter(cg)) {
            return;
        }
        if (cg.containsField(TransformationUtil.UUID_FIELD) == null) {
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

        // advise all the constructors
        for (Iterator it = initIndexes.iterator(); it.hasNext();) {
            final int initIndex = ((Integer)it.next()).intValue();

            methods[initIndex] = createUuidField(
                    cpg, cg,
                    methods[initIndex],
                    factory).getMethod();
        }

        // update the old methods
        cg.setMethods(methods);
    }

    /**
     * Adds a the Identifiable interface to the class.
     *
     * @param cg the class gen
     * @param cpg the constant pool
     */
    private void addIdentifiableInterface(final ClassGen cg, final ConstantPoolGen cpg) {
        final int[] interfaces = cg.getInterfaces();
        final String interfaceName = TransformationUtil.IDENTIFIABLE_INTERFACE;

        boolean addInterface = true;
        for (int i = 0; i < interfaces.length; i++) {
            final ConstantClass cc = (ConstantClass)cpg.getConstant(interfaces[i]);
            final ConstantUtf8 cu = (ConstantUtf8)cpg.getConstant(cc.getNameIndex());

            if (implementsInterface(cu, interfaceName)) {
                addInterface = false;
                break;
            }
        }
        if (addInterface) {
            TransformationUtil.addInterfaceToClass(cg, interfaceName);
        }
    }

    /**
     * Adds a field holding an UUID.
     *
     * @param cg the classgen
     */
    private void addUuidField(final ClassGen cg) {
        if (cg.containsField(TransformationUtil.UUID_FIELD) == null) {

            FieldGen field = new FieldGen(
                    Constants.ACC_PRIVATE | Constants.ACC_FINAL,
                    Type.STRING,
                    TransformationUtil.UUID_FIELD,
                    cg.getConstantPool());

            TransformationUtil.addField(cg, field.getField());
        }
    }

    /**
     * Adds a getter method for the UUID.
     *
     * @param cg the classgen
     * @param cpg the constant pool
     * @param factory the instruction factory
     */
    private void addUuidGetterMethod(final ClassGen cg,
                                     final ConstantPoolGen cpg,
                                     final InstructionFactory factory) {

        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(
                Constants.ACC_PUBLIC,
                Type.STRING,
                Type.NO_ARGS,
                new String[]{},
                TransformationUtil.GET_UUID_METHOD,
                cg.getClassName(),
                il, cpg);

        if (cg.containsMethod(
                TransformationUtil.GET_UUID_METHOD,
                method.getSignature()) != null) {
            return;
        }

        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                TransformationUtil.UUID_FIELD,
                Type.STRING,
                Constants.GETFIELD));
        il.append(factory.createReturn(Type.OBJECT));

        method.setMaxStack();
        method.setMaxLocals();

        TransformationUtil.addMethod(cg, method.getMethod());
    }

    /**
     * Transforms the init method to create the newly added join point member field.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param init the constructor for the class
     * @param factory the objectfactory
     * @return the modified constructor
     */
    private MethodGen createUuidField(final ConstantPoolGen cp,
                                      final ClassGen cg,
                                      final Method init,
                                      final InstructionFactory factory) {

        final MethodGen mg = new MethodGen(init, cg.getClassName(), cp);
        final InstructionList il = mg.getInstructionList();

        final InstructionHandle[] ihs = il.getInstructionHandles();
        for (int i = 0; i < ihs.length; i++) {

            // inserts field instantiation into the constructor
            final InstructionHandle ih = ihs[i];
            if (!(ih.getInstruction() instanceof ReturnInstruction)) continue;

            final InstructionHandle ihPost =
                    il.insert(ih, factory.createLoad(Type.OBJECT, 0));

            il.insert(ih, factory.createLoad(Type.OBJECT, 0));

            il.insert(ih, factory.createInvoke(
                    TransformationUtil.UUID_CLASS,
                    TransformationUtil.UUID_EXECUTION_METHOD,
                    Type.STRING,
                    new Type[]{Type.OBJECT},
                    Constants.INVOKESTATIC));

            il.insert(ih, factory.createFieldAccess(
                    cg.getClassName(),
                    TransformationUtil.UUID_FIELD,
                    Type.STRING,
                    Constants.PUTFIELD));

            il.redirectBranches(ih, ihPost);
        }
        mg.setMaxStack();
        mg.setMaxLocals();
        return mg;
    }

    /**
     * Checks if a class implements an interface.
     *
     * @param cu ConstantUtf8 constant
     * @return true if the class implements the interface
     */
    private boolean implementsInterface(final ConstantUtf8 cu,
                                        final String interfaceName) {
        return cu.getBytes().equals(interfaceName.replace('.', '/'));
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
        if (m_weaveModel.inTransformationScope(cg.getClassName())) {
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
     * Callback method. Prints a log/status message at each transformation.
     *
     * @return a log string
     */
    public String verboseMessage() {
        return this.getClass().getName();
    }
    ///CLOVER:ON
}

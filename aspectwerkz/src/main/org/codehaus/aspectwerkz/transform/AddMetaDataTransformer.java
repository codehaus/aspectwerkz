/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
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
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.ConstantClass;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;

/**
 * Adds meta-data storage for the target classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class AddMetaDataTransformer
        implements AspectWerkzCodeTransformerComponent, AspectWerkzInterfaceTransformerComponent {
    /**
     * Holds references to the classes that have already been transformed by this
     * transformer.
     */
    private final Set m_hasBeenTransformed = new HashSet();

    /**
     * The definition.
     */
    private final AspectWerkzDefinition m_definition;

    /**
     * Flag to tell the transformer to do transformations or not.
     */
    private static final String ADD_METADATA = System.getProperty("aspectwerkz.add.metadata", null);

    /**
     * Retrieves the weave model.
     */
    public AddMetaDataTransformer() {
        super();
        // TODO: fix loop over definitions
        m_definition = (AspectWerkzDefinition)DefinitionLoader.getDefinitionsForTransformation().get(0);
    }

    /**
     * Adds a map for meta-data storage to all the transformed classes.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transformInterface(final Context context, final Klass klass) {
        if (ADD_METADATA == null) return; // do not do any transformations

        m_definition.loadAspects(context.getLoader());

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
        context.markAsAdvised();

        addMetaDataEnhancableInterface(cg, cpg);
        addMapField(cg);
        addMetaDataGetterMethod(cg, cpg, factory);
        addMetaDataSetterMethod(cg, cpg, factory);
    }

    /**
     * Creates a HashMap and sets it to the Map field.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transformCode(final Context context, final Klass klass) {
        if (ADD_METADATA == null) return; // do not do any transformations

        final ClassGen cg = klass.getClassGen();
        if (classFilter(cg)) {
            return;
        }
        if (cg.containsField(TransformationUtil.META_DATA_FIELD) == null) {
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

            methods[initIndex] = createMetaDataField(
                    cpg, cg,
                    methods[initIndex],
                    factory).getMethod();
        }

        // update the old methods
        cg.setMethods(methods);
        context.markAsAdvised();
    }

    /**
     * Adds a the MetaDataEnhanceable interface to the class.
     *
     * @param cg the class gen
     * @param cpg the constant pool
     */
    private void addMetaDataEnhancableInterface(final ClassGen cg, final ConstantPoolGen cpg) {
        final int[] interfaces = cg.getInterfaces();
        final String interfaceName = TransformationUtil.META_DATA_INTERFACE;

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
     * Adds a field holding a Map.
     *
     * @param cg the classgen
     */
    private void addMapField(final ClassGen cg) {
        if (cg.containsField(TransformationUtil.META_DATA_FIELD) == null) {

            FieldGen field = new FieldGen(
                    Constants.ACC_PRIVATE | Constants.ACC_FINAL,
                    new ObjectType("java.util.Map"),
                    TransformationUtil.META_DATA_FIELD,
                    cg.getConstantPool());

            TransformationUtil.addField(cg, field.getField());
        }
    }

    /**
     * Adds a getter method for the meta-data storage.
     *
     * @param cg the classgen
     * @param cpg the constant pool
     * @param factory the instruction factory
     */
    private void addMetaDataGetterMethod(final ClassGen cg,
                                         final ConstantPoolGen cpg,
                                         final InstructionFactory factory) {
        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(
                Constants.ACC_PUBLIC,
                Type.OBJECT,
                new Type[]{Type.OBJECT},
                new String[]{"arg0"},
                TransformationUtil.GET_META_DATA_METHOD,
                cg.getClassName(),
                il, cpg);

        if (cg.containsMethod(
                TransformationUtil.GET_META_DATA_METHOD,
                method.getSignature()) != null) {
            return;
        }

        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                TransformationUtil.META_DATA_FIELD,
                new ObjectType("java.util.Map"),
                Constants.GETFIELD));

        il.append(factory.createLoad(Type.OBJECT, 1));
        il.append(factory.createInvoke(
                "java.util.Map",
                "get",
                Type.OBJECT,
                new Type[]{Type.OBJECT},
                Constants.INVOKEINTERFACE));

        il.append(factory.createReturn(Type.OBJECT));

        method.setMaxStack();
        method.setMaxLocals();

        TransformationUtil.addMethod(cg, method.getMethod());
    }

    /**
     * Adds a setter method for the meta-data storage.
     *
     * @param cg the classgen
     * @param cpg the constant pool
     * @param factory the instruction factory
     */
    private void addMetaDataSetterMethod(final ClassGen cg,
                                         final ConstantPoolGen cpg,
                                         final InstructionFactory factory) {

        InstructionList il = new InstructionList();

        MethodGen method = new MethodGen(
                Constants.ACC_PUBLIC,
                Type.VOID,
                new Type[]{Type.OBJECT, Type.OBJECT},
                new String[]{"key", "value"},
                TransformationUtil.SET_META_DATA_METHOD,
                cg.getClassName(),
                il, cpg);

        if (cg.containsMethod(
                TransformationUtil.SET_META_DATA_METHOD,
                method.getSignature()) != null) {
            return;
        }

        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(factory.createFieldAccess(
                cg.getClassName(),
                TransformationUtil.META_DATA_FIELD,
                new ObjectType("java.util.Map"),
                Constants.GETFIELD));

        il.append(factory.createLoad(Type.OBJECT, 1));
        il.append(factory.createLoad(Type.OBJECT, 2));
        il.append(factory.createInvoke(
                "java.util.Map",
                "put",
                Type.OBJECT,
                new Type[]{Type.OBJECT, Type.OBJECT},
                Constants.INVOKEINTERFACE));

        il.append(InstructionConstants.POP);
        il.append(factory.createReturn(Type.VOID));

        method.setMaxStack();
        method.setMaxLocals();

        TransformationUtil.addMethod(cg, method.getMethod());
    }

    /**
     * Transforms the init method to create the newly added member field for
     * meta-data storage.
     *
     * @param cp the ConstantPoolGen
     * @param cg the ClassGen
     * @param init the constructor for the class
     * @param factory the objectfactory
     * @return the modified constructor
     */
    private MethodGen createMetaDataField(final ConstantPoolGen cp,
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

            il.insert(ih, factory.createNew("java.util.HashMap"));
            il.insert(ih, InstructionConstants.DUP);

            il.insert(ih, factory.createInvoke("java.util.HashMap",
                    "<init>",
                    Type.VOID,
                    Type.NO_ARGS,
                    Constants.INVOKESPECIAL));

            il.insert(ih, factory.createFieldAccess(
                    cg.getClassName(),
                    TransformationUtil.META_DATA_FIELD,
                    new ObjectType("java.util.Map"),
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
        String className = cg.getClassName();
        if (m_definition.inExcludePackage(className)) {
            return true;
        }
        if (m_definition.inIncludePackage(className)) {
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

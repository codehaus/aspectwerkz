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
 * MERCHANTABILITY or FITNESS FOR and PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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

import org.codehaus.aspectwerkz.metadata.WeaveModel;

/**
 * Adds meta-data storage for the target classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AddMetaDataTransformer.java,v 1.11.2.3 2003-07-20 10:38:37 avasseur Exp $
 */
public final class AddMetaDataTransformer
        extends AspectWerkzAbstractInterfaceTransformer implements AspectWerkzCodeTransformerComponent {
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
    private static final String ADD_METADATA = System.getProperty("aspectwerkz.add.metadata", null);

    /**
     * Retrieves the weave model.
     */
    public AddMetaDataTransformer() {
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
     * Adds a map for meta-data storage to all the transformed classes.
     *
     * @param es the extension set
     * @param cs the unextendable class set
     */
    public void transformInterface(final AspectWerkzExtensionSet es,
                                   final AspectWerkzUnextendableClassSet cs) {
        if (ADD_METADATA == null) return; // do not do any transformations

        final Iterator it = cs.getIteratorForTransformableClasses();
        while (it.hasNext()) {

            final ClassGen cg = (ClassGen)it.next();
            final ConstantPoolGen cpg = cg.getConstantPool();
            final InstructionFactory factory = new InstructionFactory(cg);

            if (classFilter(cg)) {
                continue;
            }
            if (m_hasBeenTransformed.contains(cg.getClassName())) {
                continue;
            }

            // mark the class as transformed
            m_hasBeenTransformed.add(cg.getClassName());

            addMetaDataEnhancableInterface(cg, cpg, es);
            addMapField(cg, es);
            addMetaDataGetterMethod(cg, cpg, factory, es);
            addMetaDataSetterMethod(cg, cpg, factory, es);
        }
    }

    /**
     * Creates a HashMap and sets it to the Map field.
     *
     * @param cs the class set.
     */
    public void transformCode(final AspectWerkzUnextendableClassSet cs) {
        if (ADD_METADATA == null) return; // do not do any transformations

        final Iterator iterator = cs.getIteratorForTransformableClasses();
        while (iterator.hasNext()) {
            final ClassGen cg = (ClassGen)iterator.next();

            if (classFilter(cg)) continue;
            if (cg.containsField(TransformationUtil.META_DATA_FIELD) == null) continue;

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
        }
    }

    /**
     * Adds a the MetaDataEnhanceable interface to the class.
     *
     * @param cg the class gen
     * @param cpg the constant pool
     * @param es the extension set
     */
    private void addMetaDataEnhancableInterface(final ClassGen cg,
                                                final ConstantPoolGen cpg,
                                                final AspectWerkzExtensionSet es) {
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
            es.addInterfaceToClass(cg, interfaceName);
        }
    }

    /**
     * Adds a field holding a Map.
     *
     * @param cg the classgen
     * @param es the extension set
     */
    private void addMapField(final ClassGen cg, final AspectWerkzExtensionSet es) {
        if (cg.containsField(TransformationUtil.META_DATA_FIELD) == null) {

            FieldGen field = new FieldGen(
                    Constants.ACC_PRIVATE | Constants.ACC_FINAL,
                    new ObjectType("java.util.Map"),
                    TransformationUtil.META_DATA_FIELD,
                    cg.getConstantPool());

            es.addField(cg, field.getField());
        }
    }

    /**
     * Adds a getter method for the meta-data storage.
     *
     * @param cg the classgen
     * @param es the extension set
     */
    private void addMetaDataGetterMethod(final ClassGen cg,
                                         final ConstantPoolGen cpg,
                                         final InstructionFactory factory,
                                         final AspectWerkzExtensionSet es) {
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

        es.addMethod(cg, method.getMethod());
    }

    /**
     * Adds a setter method for the meta-data storage.
     *
     * @param cg the classgen
     * @param es the extension set
     */
    private void addMetaDataSetterMethod(final ClassGen cg,
                                         final ConstantPoolGen cpg,
                                         final InstructionFactory factory,
                                         final AspectWerkzExtensionSet es) {

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

        es.addMethod(cg, method.getMethod());
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
        if (m_weaveModel.inTransformationScope(cg.getClassName())) {
            return false;
        }
        return true;
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
     * Logs a message.
     *
     * @return the log message
     */
    public String verboseMessage() {
        return getClass().getName();
    }
    ///CLOVER:ON
}

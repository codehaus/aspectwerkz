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
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import gnu.trove.THashSet;

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

import org.cs3.jmangler.bceltransformer.AbstractInterfaceTransformer;
import org.cs3.jmangler.bceltransformer.UnextendableClassSet;
import org.cs3.jmangler.bceltransformer.ExtensionSet;
import org.cs3.jmangler.bceltransformer.CodeTransformerComponent;

import org.codehaus.aspectwerkz.definition.metadata.WeaveModel;

/**
 * Adds an UuidGenerator to all transformed classes.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bon�r</a>
 * @version $Id: AddUuidTransformer.java,v 1.2 2003-05-12 09:20:46 jboner Exp $
 */
public final class AddUuidTransformer extends AbstractInterfaceTransformer
        implements CodeTransformerComponent {
    ///CLOVER:OFF

    /**
     * Holds references to the classes that have already been transformed by this
     * transformer.
     */
    private final Set m_hasBeenTransformed = new THashSet();

    /**
     * Holds references to the classes that have already been transformed by
     * other transformers.
     */
    private final List m_classesToTransform = new ArrayList();

    /**
     * Holds the weave model.
     */
    private WeaveModel m_weaveModel = WeaveModel.loadModel();

    /**
     * Constructor.
     */
    public AddUuidTransformer() {
        List advisedClasses = m_weaveModel.getAspectPatterns();
        for (Iterator it = advisedClasses.iterator(); it.hasNext();) {
            m_classesToTransform.add(it.next());
        }
    }

    /**
     * Adds a UUID to all the transformed classes.
     *
     * @param es the extension set
     * @param cs the unextendable class set
     */
    public void transformInterface(final ExtensionSet es,
                                   final UnextendableClassSet cs) {

        final Iterator it = cs.getIteratorForTransformableClasses();
        while (it.hasNext()) {

            final ClassGen cg = (ClassGen)it.next();
            final ConstantPoolGen cpg = cg.getConstantPool();
            final InstructionFactory factory = new InstructionFactory(cg);

            if (classFilter(cg)) continue;
            if (m_hasBeenTransformed.contains(cg.getClassName())) continue;

            // mark the class as transformed
            m_hasBeenTransformed.add(cg.getClassName());

            addIdentifiableInterface(cg, cpg, es);

            addUuidField(cg, es);

            addUuidGetterMethod(cg, cpg, factory, es);
        }
    }

    /**
     * Makes the member method transformations.
     *
     * @param cs the class set.
     */
    public void transformCode(final UnextendableClassSet cs) {

        final Iterator iterator = cs.getIteratorForTransformableClasses();
        while (iterator.hasNext()) {
            final ClassGen cg = (ClassGen)iterator.next();

            if (classFilter(cg)) continue;
            if (cg.containsField(TransformationUtil.UUID_FIELD) == null) continue;

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
            if (initIndexes.size() == 0) throw new RuntimeException("class corrupt: no <init> method found in class " + cg.getClassName());

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
    }

    /**
     * Adds a the Identifiable interface to the class.
     *
     * @param cg the class gen
     * @param cpg the constant pool
     * @param es the extension set
     */
    private void addIdentifiableInterface(final ClassGen cg,
                                          final ConstantPoolGen cpg,
                                          final ExtensionSet es) {
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
            es.addInterfaceToClass(cg.getClassName(), interfaceName);
        }
    }

    /**
     * Adds a field holding an UUID.
     *
     * @param cg the classgen
     * @param es the extension set
     */
    private void addUuidField(final ClassGen cg, final ExtensionSet es) {
        if (cg.containsField(TransformationUtil.UUID_FIELD) == null) {

            FieldGen field = new FieldGen(
                    Constants.ACC_PRIVATE | Constants.ACC_FINAL,
                    Type.STRING,
                    TransformationUtil.UUID_FIELD,
                    cg.getConstantPool());

            es.addField(cg.getClassName(), field.getField());
        }
    }

    /**
     * Adds a getter method for the UUID.
     *
     * @param cg the classgen
     * @param es the extension set
     */
    private void addUuidGetterMethod(final ClassGen cg,
                                     final ConstantPoolGen cpg,
                                     final InstructionFactory factory,
                                     final ExtensionSet es) {

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

        es.addMethod(cg.getClassName(), method.getMethod());
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
        else if (m_weaveModel.hasAspect(cg.getClassName())) {
            return false;
        }
        else {
            return true;
        }
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

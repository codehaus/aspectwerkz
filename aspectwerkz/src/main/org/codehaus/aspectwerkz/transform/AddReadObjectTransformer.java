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

import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

import org.cs3.jmangler.bceltransformer.UnextendableClassSet;
import org.cs3.jmangler.bceltransformer.ExtensionSet;
import org.cs3.jmangler.bceltransformer.AbstractInterfaceTransformer;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.Constants;

import org.codehaus.aspectwerkz.metadata.WeaveModel;

/**
 * Adds a <code>private void readObject(final ObjectInputStream stream) throws Exception</code>
 * to all target objects.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: AddReadObjectTransformer.java,v 1.4 2003-06-17 15:00:00 jboner Exp $
 */
public class AddReadObjectTransformer extends AbstractInterfaceTransformer {
    ///CLOVER:ON

    /**
     * Holds references to the classes that have already been transformed by this
     * transformer.
     */
    private final Set m_hasBeenTransformed = new HashSet();

    /**
     * Holds a list with all the createWeaveModel models.
     */
    private final WeaveModel m_weaveModel;

    /**
     * Retrieves the createWeaveModel model.
     */
    public AddReadObjectTransformer() {
        super();
        List weaveModels = WeaveModel.loadModels();
        if (weaveModels.size() > 1) {
            throw new RuntimeException("more than one createWeaveModel model is specified");
        }
        else {
            m_weaveModel = (WeaveModel)weaveModels.get(0);
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

            addReadObjectMethod(cg, cpg, factory, es);
        }
    }

    /**
     * Adds a <code>private void readObject(final ObjectInputStream stream) throws Exception</code>
     * to the transformed class.
     *
     * @todo implement the addReadObjectMethod
     *   Should generate:
     *
     *   these fields along with the user defined:
     *   ___metaData = new HashMap();
     *   ___uuid = UuidGenerator.generate(this);
     *   ___jp$... = new ThreadLocal(); // loop over these
     *   ___jp$... = new ThreadLocal();
     *
     *   the user defined fields (based on metadata for the class)
     *
     * @param cg the class gen
     * @param cpg the constant pool gen
     * @param factory the instruction objectfactory
     * @param es the extension set
     */
    private void addReadObjectMethod(final ClassGen cg,
                                     final ConstantPoolGen cpg,
                                     final InstructionFactory factory,
                                     final ExtensionSet es) {

        InstructionList il = new InstructionList();
        MethodGen method = new MethodGen(
                Constants.ACC_PRIVATE,
                Type.VOID,
                new Type[]{new ObjectType("java.io.ObjectInputStream")},
                new String[]{"stream"},
                "readObject",
                cg.getClassName(),
                il, cpg);

        if (cg.containsMethod("readObject", method.getSignature()) != null) {
            return;
        }

        il.append(factory.createReturn(Type.VOID));

        method.setMaxStack();
        method.setMaxLocals();

        es.addMethod(cg.getClassName(), method.getMethod());
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
        if (m_weaveModel.isAdvised(cg.getClassName())) {
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

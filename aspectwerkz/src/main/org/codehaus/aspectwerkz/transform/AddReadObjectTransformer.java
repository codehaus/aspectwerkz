/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.Constants;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;

/**
 * Adds a <code>private void readObject(final ObjectInputStream stream) throws Exception</code>
 * to all target objects.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AddReadObjectTransformer implements AspectWerkzInterfaceTransformerComponent {

    /**
     * Holds references to the classes that have already been transformed by this
     * transformer.
     */
    private final Set m_hasBeenTransformed = new HashSet();

    /**
     * The definitions.
     */
    private final List m_definitions;

    /**
     * Retrieves the weave model.
     */
    public AddReadObjectTransformer() {
        super();
        m_definitions = DefinitionLoader.getDefinitionsForTransformation();
    }

    /**
     * Adds a UUID to all the transformed classes.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transformInterface(final Context context, final Klass klass) {
        // loop over all the definitions
        for (Iterator it = m_definitions.iterator(); it.hasNext();) {
            AspectWerkzDefinition definition = (AspectWerkzDefinition)it.next();

            definition.loadAspects(context.getLoader());

            final ClassGen cg = klass.getClassGen();
            final ConstantPoolGen cpg = cg.getConstantPool();
            final InstructionFactory factory = new InstructionFactory(cg);

            if (classFilter(cg, definition)) {
                return;
            }
            if (m_hasBeenTransformed.contains(cg.getClassName())) {
                return;
            }

            // mark the class as transformed
            m_hasBeenTransformed.add(cg.getClassName());
            context.markAsAdvised();

            addReadObjectMethod(cg, cpg, factory);
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
     */
    private void addReadObjectMethod(final ClassGen cg,
                                     final ConstantPoolGen cpg,
                                     final InstructionFactory factory) {

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

        TransformationUtil.addMethod(cg, method.getMethod());
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param cg the class to filter
     * @param definition the definition
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final ClassGen cg, final AspectWerkzDefinition definition) {
        if (cg.isInterface()) {
            return true;
        }
        String className = cg.getClassName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (definition.inIncludePackage(className)) {
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

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
import java.util.HashSet;
import java.util.List;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;

/**
 * Adds an interfaces to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class AddInterfaceTransformer implements AspectWerkzInterfaceTransformerComponent {
    ///CLOVER:OFF
    /**
     * Holds references to the classes that have already been transformed.
     */
    private final Set m_transformed = new HashSet();

    /**
     * The definition.
     */
    private final AspectWerkzDefinition m_definition;

    /**
     * Retrieves the weave model.
     */
    public AddInterfaceTransformer() {
        super();
        m_definition = AspectWerkzDefinition.getDefinitionForTransformation();
    }

    /**
     * Adds an interfaces to the classes specified.
     *
     * @TODO: support for def 2 needs to be added
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transformInterface(final Context context, final Klass klass) {
        m_definition.loadAspects(context.getLoader());

        final ClassGen cg = klass.getClassGen();
        ClassMetaData classMetaData = BcelMetaDataMaker.createClassMetaData(context.getJavaClass(cg));

        if (classFilter(cg, classMetaData)) {
            return;
        }
        if (m_transformed.contains(cg.getClassName())) {
            return;
        }
        m_transformed.add(cg.getClassName());

        ConstantPoolGen cpg = cg.getConstantPool();
        int[] interfaces = cg.getInterfaces();

        List introductionNames = m_definition.getIntroductionNamesForClass(cg.getClassName());
        for (Iterator it2 = introductionNames.iterator(); it2.hasNext();) {

            String introductionName = (String)it2.next();
            String interfaceName = m_definition.getIntroductionInterfaceName(introductionName);

            boolean addInterface = true;

            for (int l = 0; l < interfaces.length; l++) {
                ConstantClass cc = (ConstantClass)cpg.getConstant(interfaces[l]);
                ConstantUtf8 cu = (ConstantUtf8)cpg.getConstant(cc.getNameIndex());

                if (implementsInterface(cu, interfaceName)) {
                    addInterface = false;
                    break;
                }
            }
            if (addInterface) {
                if (interfaceName == null || interfaceName.equals("")) {
                    throw new DefinitionException("trying to weave null interface to " + cg.getClassName() + ": definition file is not consistentadd");
                }
                TransformationUtil.addInterfaceToClass(cg, interfaceName);
            }
        }
    }

    /**
     * Checks if a class implements an interface.
     *
     * @param cu ConstantUtf8 constant
     * @return true if the class implements the interface
     */
    private static boolean implementsInterface(final ConstantUtf8 cu, final String interfaceName) {
        return cu.getBytes().equals(interfaceName.replace('.', '/'));
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param cg the class to filter
     * @param classMetaData the class meta-data
     * @return boolean true if the method should be filtered away
     */
    private boolean classFilter(final ClassGen cg, final ClassMetaData classMetaData) {
        if (cg.isInterface()) {
            return true;
        }
        if (m_definition.inTransformationScope(cg.getClassName()) &&
                m_definition.hasIntroductions(classMetaData)) {
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

    ///CLOVER:ON
}

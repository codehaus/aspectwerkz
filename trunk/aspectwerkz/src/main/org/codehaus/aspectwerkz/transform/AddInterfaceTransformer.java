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

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ClassGen;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.BcelMetaDataMaker;

/**
 * Adds an interfaces to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class AddInterfaceTransformer implements AspectWerkzInterfaceTransformerComponent {

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
        // TODO: fix loop over definitions
        m_definition = (AspectWerkzDefinition)DefinitionLoader.getDefinitionsForTransformation().get(0);
    }

    /**
     * Adds an interfaces to the classes specified.
     *
     * @param context the transformation context
     * @param klass the class
     */
    public void transformInterface(final Context context, final Klass klass) {
        m_definition.loadAspects(context.getLoader());

        final ClassGen cg = klass.getClassGen();
        final ConstantPoolGen cpg = cg.getConstantPool();

        ClassMetaData classMetaData = BcelMetaDataMaker.createClassMetaData(context.getJavaClass(cg));

        if (classFilter(cg, classMetaData)) {
            return;
        }
        if (m_transformed.contains(cg.getClassName())) {
            return;
        }
        m_transformed.add(cg.getClassName());

        if (m_definition.isAttribDef()) {
            org.codehaus.aspectwerkz.attribdef.transform.IntroductionTransformer.addInterfaceIntroductions(
                    m_definition, cg, cpg, classMetaData
            );
        }
        else if (m_definition.isXmlDef()) {
            org.codehaus.aspectwerkz.xmldef.transform.IntroductionTransformer.addInterfaceIntroductions(
                    m_definition, cg, cpg
            );
        }
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
}

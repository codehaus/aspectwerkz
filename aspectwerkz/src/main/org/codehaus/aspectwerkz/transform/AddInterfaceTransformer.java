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

import gnu.trove.THashSet;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;

import org.cs3.jmangler.bceltransformer.AbstractInterfaceTransformer;
import org.cs3.jmangler.bceltransformer.UnextendableClassSet;
import org.cs3.jmangler.bceltransformer.ExtensionSet;

import org.codehaus.aspectwerkz.definition.metadata.WeaveModel;

/**
 * Adds an interfaces to classes.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: AddInterfaceTransformer.java,v 1.1.1.1 2003-05-11 15:15:01 jboner Exp $
 */
public final class AddInterfaceTransformer extends AbstractInterfaceTransformer {
    ///CLOVER:OFF
    /**
     * Holds references to the classes that have already been transformed.
     */
    private final Set m_transformed = new THashSet();

    /**
     * Holds the weave model.
     */
    private WeaveModel m_weaveModel = WeaveModel.loadModel();

    /**
     * Adds an interfaces to the classes specified.
     *
     * @param es the extension set
     * @param cs the unextendable class set
     */
    public void transformInterface(final ExtensionSet es,
                                   final UnextendableClassSet cs) {
        final Iterator it = cs.getIteratorForTransformableClasses();
        while (it.hasNext()) {

            final ClassGen cg = (ClassGen)it.next();
            if (classFilter(cg)) continue;

            if (m_transformed.contains(cg.getClassName())) continue;
            m_transformed.add(cg.getClassName());

            final ConstantPoolGen cpg = cg.getConstantPool();
            final int[] interfaces = cg.getInterfaces();

            final List introductionNames =
                    m_weaveModel.getIntroductionNames(cg.getClassName());

            for (Iterator it2 = introductionNames.iterator(); it2.hasNext();) {
                String introductionName = (String)it2.next();

                final String interfaceName = m_weaveModel.
                        getIntroductionInterfaceName(introductionName);

                boolean addInterface = true;

                for (int l = 0; l < interfaces.length; l++) {
                    final ConstantClass cc = (ConstantClass)cpg.
                            getConstant(interfaces[l]);
                    final ConstantUtf8 cu = (ConstantUtf8)cpg.
                            getConstant(cc.getNameIndex());

                    if (implementsInterface(cu, interfaceName)) {
                        addInterface = false;
                        break;
                    }
                }
                if (addInterface) {
                    es.addInterfaceToClass(cg.getClassName(), interfaceName);
                }
            }
        }
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
        else if (m_weaveModel.hasAspect(cg.getClassName()) &&
                m_weaveModel.hasIntroductions(cg.getClassName())) {
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

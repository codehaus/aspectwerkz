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
import java.util.HashSet;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;

import org.cs3.jmangler.bceltransformer.AbstractInterfaceTransformer;
import org.cs3.jmangler.bceltransformer.UnextendableClassSet;
import org.cs3.jmangler.bceltransformer.ExtensionSet;

import org.codehaus.aspectwerkz.metadata.WeaveModel;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Adds an interfaces to classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AddInterfaceTransformer.java,v 1.13 2003-07-09 05:21:28 jboner Exp $
 */
public final class AddInterfaceTransformer extends AbstractInterfaceTransformer {
    ///CLOVER:OFF
    /**
     * Holds references to the classes that have already been transformed.
     */
    private final Set m_transformed = new HashSet();

    /**
     * The weave model.
     */
    private final WeaveModel m_weaveModel;

    /**
     * Retrieves the weave model.
     */
    public AddInterfaceTransformer() {
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
     * Adds an interfaces to the classes specified.
     *
     * @param es the extension set
     * @param cs the unextendable class set
     */
    public void transformInterface(final ExtensionSet es,
                                   final UnextendableClassSet cs) {
        Iterator it = cs.getIteratorForTransformableClasses();
        while (it.hasNext()) {

            ClassGen cg = (ClassGen)it.next();
            if (classFilter(cg)) continue;

            if (m_transformed.contains(cg.getClassName())) continue;
            m_transformed.add(cg.getClassName());

            ConstantPoolGen cpg = cg.getConstantPool();
            int[] interfaces = cg.getInterfaces();

            for (Iterator it2 = m_weaveModel.getIntroductionNames(
                    cg.getClassName()).iterator(); it2.hasNext();) {

                String introductionName = (String)it2.next();
                String interfaceName = m_weaveModel.
                        getIntroductionInterfaceName(introductionName);

                boolean addInterface = true;

                for (int l = 0; l < interfaces.length; l++) {
                    ConstantClass cc = (ConstantClass)cpg.
                            getConstant(interfaces[l]);
                    ConstantUtf8 cu = (ConstantUtf8)cpg.
                            getConstant(cc.getNameIndex());

                    if (implementsInterface(cu, interfaceName)) {
                        addInterface = false;
                        break;
                    }
                }
                if (addInterface) {
                    if (interfaceName == null || interfaceName.equals("")) {
                        throw new DefinitionException("trying to weave null interface to " + cg.getClassName() + ": definition file is not consistentadd");
                    }
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
        if (m_weaveModel.isAdvised(cg.getClassName()) &&
                m_weaveModel.hasIntroductions(cg.getClassName())) {
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

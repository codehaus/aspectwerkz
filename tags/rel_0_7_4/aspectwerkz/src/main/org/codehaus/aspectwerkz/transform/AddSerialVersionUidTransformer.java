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

import java.util.List;
import java.util.Iterator;

import org.cs3.jmangler.bceltransformer.ExtensionSet;
import org.cs3.jmangler.bceltransformer.UnextendableClassSet;
import org.cs3.jmangler.bceltransformer.AbstractInterfaceTransformer;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.Constants;

import org.codehaus.aspectwerkz.metadata.WeaveModel;

/**
 * Adds a new serialVersionUID to the class (if the class is serializable and does not
 * have a UID already defined).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AddSerialVersionUidTransformer.java,v 1.1 2003-07-19 20:36:16 jboner Exp $
 */
public class AddSerialVersionUidTransformer extends AbstractInterfaceTransformer {

    /**
     * Holds the weave model.
     */
    private final WeaveModel m_weaveModel;

    /**
     * Retrieves the weave model.
     */
    public AddSerialVersionUidTransformer() {
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
     * @param es the extension set
     * @param cs the unextendable class set
     */
    public void transformInterface(final ExtensionSet es, final UnextendableClassSet cs) {
        Iterator it = cs.getIteratorForTransformableClasses();
        while (it.hasNext()) {

            final ClassGen cg = (ClassGen)it.next();
            if (classFilter(cg)) {
                continue;
            }
            if (!TransformationUtil.isSerializable(cg)) {
                return;
            }
            if (TransformationUtil.hasSerialVersionUid(cg)) {
                return;
            }

            addSerialVersionUidField(cg, es);
        }
    }

    /**
     * Adds a new serialVersionUID to the class (if the class is serializable and does not
     * have a UID already defined).
     *
     * @param cg the class gen
     * @param es the extension set
     */
    private void addSerialVersionUidField(final ClassGen cg, final ExtensionSet es) {
        FieldGen field = new FieldGen(
                Constants.ACC_FINAL | Constants.ACC_STATIC,
                Type.LONG,
                TransformationUtil.SERIAL_VERSION_UID_FIELD,
                cg.getConstantPool());
        final long uid = TransformationUtil.calculateSerialVersionUid(cg);
        field.setInitValue(uid);
        es.addField(cg.getClassName(), field.getField());
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
}

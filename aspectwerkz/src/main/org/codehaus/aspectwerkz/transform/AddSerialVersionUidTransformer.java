/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.Constants;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.DefinitionLoader;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.io.IOException;

/**
 * Adds a new serialVersionUID to the class (if the class is serializable and does not
 * have a UID already defined).
 * The calculation is based on the initial bytecode.
 * For this transformer, the exclude and include does not matters.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AddSerialVersionUidTransformer implements AspectWerkzInterfaceTransformerComponent {

    public AddSerialVersionUidTransformer() {
        super();
    }

    /**
     * Adds a serial ver uid to the transformed classes
     *
     * @param context the transformation context
     * @param klass the class weaved
     */
    public void transformInterface(final Context context, final Klass klass) {
        final ClassGen cg = klass.getClassGen();
        if (classFilter(cg)) {
            return;
        }
        if (!TransformationUtil.isSerializable(context, cg)) {
            return;
        }
        if (TransformationUtil.hasSerialVersionUid(cg)) {
            return;
        }
        try {
            addSerialVersionUidField(context, cg, klass.getInitialClassGen());
        } catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Adds a new serialVersionUID to the class (if the class is serializable and does not
     * have a UID already defined).
     *
     * @param context the transformation context
     * @param cg the class gen
     * @param initialCg the initial class gen
     */
    private void addSerialVersionUidField(final Context context, ClassGen cg, final ClassGen initialCg) {
        FieldGen field = new FieldGen(
                Constants.ACC_FINAL | Constants.ACC_STATIC,
                Type.LONG,
                TransformationUtil.SERIAL_VERSION_UID_FIELD,
                cg.getConstantPool());
        final long uid = TransformationUtil.calculateSerialVersionUid(context, initialCg);
        field.setInitValue(uid);
        TransformationUtil.addField(cg, field.getField());
    }

    /**
     * Filters the classes to be transformed.
     *
     * @param cg the class to filter
     * @return boolean true if the class should be filtered away
     */
    private boolean classFilter(final ClassGen cg) {
        if (cg.isInterface()) {
            return true;
        }
        return false;
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
     * Callback method. Prints a log/status message at each transformation.
     *
     * @return a log string
     */
    public String verboseMessage() {
        return this.getClass().getName();
    }
}
